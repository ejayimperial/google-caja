// Copyright 2007 Google Inc. All Rights Reserved.
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.caja.opensocial;

import com.google.caja.lexer.ExternalReference;
import com.google.caja.lexer.FilePosition;
import com.google.caja.lexer.InputSource;
import com.google.caja.plugin.Config;
import com.google.caja.reporting.BuildInfo;
import com.google.caja.reporting.Message;
import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.MessageLevel;
import com.google.caja.reporting.MessagePart;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.reporting.SimpleMessageQueue;
import com.google.caja.reporting.SnippetProducer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jasvir@google.com (Jasvir Nagra)
 */
public class GadgetsTestMain {
  
  private MessageContext mc = new MessageContext();
  private Map<InputSource, CharSequence> originalSources
      = new HashMap<InputSource, CharSequence>();
  private ArrayList<URI> gadgetList;
  private JSONObject resultDoc;
  private BufferedWriter jsonOutput;

  private GadgetsTestMain() {
    resultDoc = new JSONObject();
    mc.inputSources = new ArrayList<InputSource>();
  }

  public static void main(String[] argv)
      throws UriCallbackException, URISyntaxException {
    System.exit(new GadgetsTestMain().run(argv));
  }

  public boolean processArguments(String[] argv) {
    gadgetList = new ArrayList<URI>();
    if ( argv.length == 0 ) {
      usage("GadgetsTestMain urls-of-gadgets.txt [outputfile]");
      return false;
    }
    try {
      String uri;
      BufferedReader ur = new BufferedReader( new FileReader( argv[0] ) );
      
      // Skip blank lines or comments
      while ( null != (uri = ur.readLine()) ) {
        if ( uri.matches("^[ \t]*$"))
          continue;
        if ( uri.matches("^[ \t]*#"))
          continue;
        
        URI inputUri;
        try {
          if (uri.indexOf(':') >= 0) {
            inputUri = new URI(uri);
          } else {
            File inputFile = new File(uri);
  
            if (!inputFile.exists()) {
              System.err.println("WARNING: File \"" + uri + "\" does not exist");
              return false;
            }
            if (!inputFile.isFile()) {
              usage("File \"" + uri + "\" is not a regular file");
              return false;
            }
  
            inputUri = inputFile.getAbsoluteFile().toURI();
          }
          gadgetList.add(inputUri);
        } catch (URISyntaxException e ) {
          System.err.println("WARNING: URI \"" + uri + "\" malformed");
        }
      }
    } catch ( FileNotFoundException e ) {
      usage("ERROR: Could not find file of urls:" + e.toString());
      return false;
    } catch ( IOException e ) {
      usage("ERROR: Could not read urls:" + e.toString());
      return false;
    }
    
    try {
      if ( argv.length > 1 ) {
        jsonOutput = new BufferedWriter ( new FileWriter ( new File ( argv[1] ) ) );      
      } else {
        jsonOutput = new BufferedWriter ( new OutputStreamWriter ( System.out ) );              
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return true;
  }
  
  private void writeResults ( Writer w ) {
    try {
      String json = resultDoc.toString();
      w.write(json);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private String getExceptionTrace ( Exception e ) {
    StringBuffer result = new StringBuffer("");
    for ( StackTraceElement st : e.getStackTrace() ) {
      result.append(st.toString());
      result.append("\n");
    }
    return result.toString();
  }
  
  private void testGadget ( URI gadget, JSONArray testResults ) 
      throws IOException, URISyntaxException, UriCallbackException {
    
    String[] argv = {
        "-o", "/tmp/xx",
        "-p", "xx",
//        "--css_prop_schema", "resource:///xom/google/caja/lang/css/css-extensions.json",
        "-i", gadget.toASCIIString() 
    };

    GadgetRewriterMain grm = new GadgetRewriterMain ();
    grm.init(argv);
    
    Config config = grm.getConfig();

    MessageQueue mq = new SimpleMessageQueue();
    DefaultGadgetRewriter rewriter = new DefaultGadgetRewriter(mq);
    rewriter.setCssSchema(config.getCssSchema(mq));
    rewriter.setHtmlSchema(config.getHtmlSchema(mq));
    
    JSONObject gadgetElement= new JSONObject();
    gadgetElement.put("url", gadget.toString());
    gadgetElement.put("title", "TODO");
    MessageLevel worstErrorLevel = MessageLevel.LOG;
    testResults.add(gadgetElement);
    
    JSONArray messages = new JSONArray();
    gadgetElement.put("messages", messages);
  
    Writer w = new BufferedWriter(new FileWriter(config.getOutputBase()));
    
    try {
      Callback cb = new Callback(config, mc, originalSources);
      URI baseUri = config.getBaseUri();
      for (URI input : config.getInputUris()) {
        Reader r = cb.retrieve(new ExternalReference(input, null), null);
        try {
          rewriter.rewrite(baseUri, r, cb, config.getGadgetView(), w);
        } catch (GadgetRewriteException e) {
          addMessageNode(messages,"Compiler threw uncaught exception",
              MessageLevel.FATAL_ERROR.toString(),
              getExceptionTrace(e));
          worstErrorLevel = MessageLevel.FATAL_ERROR;
          gadgetElement.put("worstErrorLevel", worstErrorLevel.toString());
        } catch (RuntimeException e) {
          addMessageNode(messages,"Compiler threw uncaught runtime exception",
              MessageLevel.FATAL_ERROR.toString(),
              getExceptionTrace(e));
          worstErrorLevel = MessageLevel.FATAL_ERROR;
          gadgetElement.put("worstErrorLevel", worstErrorLevel.toString());
        }  finally {
          SnippetProducer sp = new SnippetProducer(originalSources, mc);
          for (Message msg : mq.getMessages()) {
            addMessageNode(messages, msg, mc, sp );
            if ( msg.getMessageLevel().compareTo(worstErrorLevel) > 0 ) {
              worstErrorLevel = msg.getMessageLevel();
              gadgetElement.put("worstErrorLevel", worstErrorLevel.toString());
            } 
          }
          r.close();
        }
      }
    } catch ( RuntimeException e ) {
      addMessageNode(messages,"Compiler threw uncaught runtime exception",
          MessageLevel.FATAL_ERROR.toString(),
          getExceptionTrace(e));
      gadgetElement.put("worstErrorLevel", MessageLevel.FATAL_ERROR.toString());
    }
    finally {
      w.close();
    }    
    
  }
  
  private int run(String[] argv)
      throws UriCallbackException, URISyntaxException {

    if ( !processArguments(argv) ) {
      return -1;
    }  

    JSONArray testResults = new JSONArray ();
    String timestamp = (new Date()).toString();
    resultDoc.put("buildInfo", JSONObject.escape(BuildInfo.getInstance().getBuildInfo()));
    resultDoc.put("timestamp", JSONObject.escape(timestamp));
    System.out.print(timestamp);
    resultDoc.put("gadgets", testResults);

    try {
      for (URI gadgetUri : gadgetList) {
        try {
          testGadget(gadgetUri, testResults);
        } catch ( RuntimeException e ) {
          e.printStackTrace();
        }
      }
    } catch ( IOException e ) {
      e.printStackTrace();
    } finally {
      writeResults(jsonOutput);
      try {
        jsonOutput.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return 0;
  }

  private void addMessageNode ( JSONArray messages, String position,
                                String level, String text ) {
    
    JSONObject message = new JSONObject();

    message.put("position", position);
    message.put("level", level);
    message.put("text", text);
    
    messages.add(message);
    
  }
  
  private void addMessageNode ( JSONArray messages, Message msg, MessageContext mc,
                                SnippetProducer sp ) {

    MessageLevel messageLevel = msg.getMessageLevel();
    MessagePart topMessage = msg.getMessageParts().get(0);
    StringBuffer position = new StringBuffer();
    
    if ( topMessage instanceof FilePosition ) {
      FilePosition filePosition = (FilePosition) topMessage;
      try {
        filePosition.format(mc, position);
      } catch (IOException e) {
        e.printStackTrace();
      }  
    } else {
      position = new StringBuffer( "Unknown");
    }

    String snippet = msg.format(mc);   
    addMessageNode(messages, position.toString(), messageLevel.name(), snippet);
    
  }
  
  public void usage(String msg) {
    System.err.println(BuildInfo.getInstance().getBuildInfo());
      System.err.println();
    if (msg != null && !"".equals(msg)) {
      System.err.println(msg);
      System.err.println();
    }
    System.err.println("usage: GadgetsTestMain listofurls.txt output.json");
  }

}
