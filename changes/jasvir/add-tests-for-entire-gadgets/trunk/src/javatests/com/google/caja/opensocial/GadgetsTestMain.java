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
import com.google.caja.lexer.InputSource;
import com.google.caja.plugin.Config;
import com.google.caja.reporting.BuildInfo;
import com.google.caja.reporting.Message;
import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.MessageLevel;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.reporting.SimpleMessageQueue;
import com.google.caja.reporting.SnippetProducer;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * @author jasvir@google.com (Jasvir Nagra)
 */
public class GadgetsTestMain {
  
  private MessageContext mc = new MessageContext();
  private Map<InputSource, CharSequence> originalSources
      = new HashMap<InputSource, CharSequence>();
  private ArrayList<URI> gadgetList;
  private Document resultDoc;

  private GadgetsTestMain() throws ParserConfigurationException {
    DocumentBuilderFactory factory
      = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    DOMImplementation impl = builder.getDOMImplementation();

    resultDoc = impl.createDocument(null, null, null);
    
    mc.inputSources = new ArrayList<InputSource>();
  }

  public static void main(String[] argv)
      throws UriCallbackException, URISyntaxException, ParserConfigurationException {
    System.exit(new GadgetsTestMain().run(argv));
  }

  class Callback implements UriCallback {
    private Config config;
    
    public Callback ( Config config ) {
      this.config = config;
    }
    
    public UriCallbackOption getOption(ExternalReference extref,
                                       String mimeType) {
      return UriCallbackOption.RETRIEVE;
    }

    public Reader retrieve(ExternalReference extref, String mimeType)
        throws UriCallbackException {
      System.err.println("Retrieving " + extref);
      final Reader in;
      URI uri;
      try {
        uri = config.getBaseUri().resolve(extref.getUri());
        in = new InputStreamReader(uri.toURL().openStream(), "UTF-8");
      } catch (IOException e) {
        throw new UriCallbackException(extref, e);
      }
      
      final StringBuilder originalSource = new StringBuilder();
      InputSource is = new InputSource(uri);
      originalSources.put(is, originalSource);
      mc.inputSources.add(is);

      // Tee the content out to a buffer so that we can keep track of the
      // original content so we can show error message snippets later.
      return new Reader() {
          @Override
          public void close() throws IOException { in.close(); }
          @Override
          public int read(char[] cbuf, int off, int len) throws IOException {
            int n = in.read(cbuf, off, len);
            if (n > 0) { originalSource.append(cbuf, off, n); }
            return n;
          }
          @Override
          public int read() throws IOException {
            int ch = in.read();
            if (ch >= 0) { originalSource.append((char) ch); }
            return ch;
          }
        };
    }

    public URI rewrite(ExternalReference extref, String mimeType) {
      return extref.getUri();
    }
  }

  public boolean processArguments(String[] argv) {
    gadgetList = new ArrayList<URI>();
    if ( argv.length == 0 ) {
      usage("Please pass a list of urls of gadgets to test");
      return false;
    }
    try {
      String uri;
      BufferedReader ur = new BufferedReader( new FileReader( argv[0] ) );
      while ( null != (uri = ur.readLine()) ) {
        if ( uri.matches("^[ \t]*$"))
          continue;
        URI inputUri;
        try {
          if (uri.indexOf(':') >= 0) {
            inputUri = new URI(uri);
          } else {
            File inputFile = new File(uri);
  
            if (!inputFile.exists()) {
              usage("File \"" + uri + "\" does not exist");
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
          e.printStackTrace();      
        }
      }
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
      return false;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return true;
  }
  
  private void writeResults ( Writer w ) {
    try {
      DOMSource domSource = new DOMSource(resultDoc);
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty
          ("{http://xml.apache.org/xslt}indent-amount", "4");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      java.io.StringWriter sw = new java.io.StringWriter();
      StreamResult sr = new StreamResult(sw);
      transformer.transform(domSource, sr);
      String xml = sw.toString();
      w.write(xml);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (TransformerException e) {
      e.printStackTrace();
    }
  }
  
  private void testGadget ( URI gadget, Document result, Element testResults ) 
      throws IOException, URISyntaxException,
            UriCallbackException, GadgetRewriteException {
    Config config = new Config(
        getClass(), System.err, 
          "Test cajoling OpenSocial gadgets content.");
    config.setInputUris(gadget.toString());
    config.setCssPrefix("xxx");
    config.setOutputCssFile("/tmp/css");
    config.setOutputJsFile("/tmp/js");
    config.setOutputBase("/tmp/output");
    
    MessageQueue mq = new SimpleMessageQueue();
    DefaultGadgetRewriter rewriter = new DefaultGadgetRewriter(mq);
    rewriter.setCssSchema(config.getCssSchema(mq));
    rewriter.setHtmlSchema(config.getHtmlSchema(mq));

    Element gadgetElement = result.createElement("gadget");
    Element url = result.createElement("url");
    url.appendChild(result.createTextNode(gadget.toString()));
    gadgetElement.appendChild(url);
    
    testResults.appendChild(gadgetElement);
  
    Writer w = new BufferedWriter(new FileWriter(config.getOutputBase()));
    
    try {
      Callback cb = new Callback(config);
      URI baseUri = config.getBaseUri();
      for (URI input : config.getInputUris()) {
        Reader r = cb.retrieve(new ExternalReference(input, null), null);
        try {
          rewriter.rewrite(baseUri, r, cb, config.getGadgetView(), w);
        } finally {
          SnippetProducer sp = new SnippetProducer(originalSources, mc);
          for (Message msg : mq.getMessages()) {
            addMessageNode(gadgetElement, msg, mc, sp );
          }
          r.close();
        }
      }
    } finally {
      w.close();
    }    
    
  }
  
  private int run(String[] argv)
      throws UriCallbackException, URISyntaxException {

    if ( !processArguments(argv) ) {
      return -1;
    }  

    Element testResults = resultDoc.createElement("testResults");
    resultDoc.appendChild(testResults);
    Writer xmlOutput = null;
    try {
      xmlOutput = new BufferedWriter(new FileWriter("/tmp/results.xml"));
      for (URI gadgetUri : gadgetList) {
        try {
          testGadget(gadgetUri, resultDoc, testResults);
        } catch ( GadgetRewriteException e ) {
          e.printStackTrace();
        } catch ( RuntimeException e ) {
          //TODO(jas): Gadgets sometimes throw runtime exceptions
          //This information should be included in the xml
          e.printStackTrace();
        }
      }
    } catch ( IOException e ) {
      e.printStackTrace();
    } finally {
      writeResults(xmlOutput);
    }
    return 0;
  }

  private void addMessageNode ( Element gadget, Message msg, MessageContext mc,
                                SnippetProducer sp ) {

    MessageLevel messageLevel = msg.getMessageLevel();
    String pos = msg.getMessageParts().toString();
    String snippet = sp.getSnippet(msg);
    
    Element message = resultDoc.createElement("message");
    Element position = resultDoc.createElement("position");
    Element level = resultDoc.createElement("level");
    Element text = resultDoc.createElement("text");
    position.appendChild(resultDoc.createTextNode(pos));
    level.appendChild(resultDoc.createTextNode(messageLevel.name()));
    text.appendChild(resultDoc.createTextNode(snippet));

    message.appendChild(position);
    message.appendChild(level);
    message.appendChild(text);
    
    gadget.appendChild(message);
  }
  
  public void usage(String msg) {
    System.err.println(BuildInfo.getInstance().getBuildInfo());
      System.err.println();
    if (msg != null && !"".equals(msg)) {
      System.err.println(msg);
      System.err.println();
    }
    System.err.println("usage: GadgetsTestMain listofurls.txt");
  }

}
