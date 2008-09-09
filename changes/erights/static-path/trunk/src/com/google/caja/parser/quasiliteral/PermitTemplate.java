package com.google.caja.parser.quasiliteral;

import java.util.HashMap;
import java.util.Map;

final class PermitTemplate {

  final static PermitTemplate CanRead = new PermitTemplate();
  final static PermitTemplate CanCall = new PermitTemplate("()", CanRead);

  final Map<String, PermitTemplate> myTemplates;

  PermitTemplate(Object... pairs) {
    Map<String, PermitTemplate> templates = new HashMap<String, PermitTemplate>();
    for (int i = 0; i < pairs.length; i += 2) {
      templates.put((String)pairs[i], (PermitTemplate)pairs[i+1]);
    }
    myTemplates = templates;
  }
}
