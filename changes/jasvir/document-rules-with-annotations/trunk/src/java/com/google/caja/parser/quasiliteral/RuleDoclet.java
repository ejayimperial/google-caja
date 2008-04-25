// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.caja.parser.quasiliteral;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.internal.toolkit.util.DocletAbortException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;

/**
 * Generates list of the rules
 * TODO(jasvir): Generate javadoc style document pages
 * 
 * @author jasvir@google.com (Jasvir Nagra)
 */
public class RuleDoclet extends Doclet {
  
  public static LanguageVersion languageVersion() {
    return LanguageVersion.JAVA_1_5;
  }
  
  public static boolean validOptions(String[][] options,
                                     DocErrorReporter reporter) {
    return true;
  }

  public static int optionLength(String option) {
    if ( option.equals("-d") ) {
      return 2;
    }
    return Doclet.optionLength(option);
  }
  
  public static boolean start(RootDoc root) {
    try { 
      (new RuleDoclet()).processRoot(root);
    } catch (DocletAbortException exc) {
      return false;
    }
    return true;
  }
  
  public void processRoot(RootDoc root) {
    DefaultCajaRewriter dcr = new DefaultCajaRewriter();
    
    for ( Object oc : dcr.cajaRules ) {
      Class c = oc.getClass();

      for ( Method mm : c.getDeclaredMethods() ) {
        RuleDescription anno = mm.getAnnotation(RuleDescription.class);
        if ( anno != null ) {
          System.out.println("Rule: " + anno.name());
          System.out.println("Synopsis: " + anno.synopsis());
          System.out.println("Reason: " + anno.reason());          
          System.out.println();
        }
      }
    }
  }
}   