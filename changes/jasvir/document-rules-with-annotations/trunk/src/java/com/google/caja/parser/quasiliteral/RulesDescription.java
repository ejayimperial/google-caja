// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.caja.parser.quasiliteral;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes each a set of transformation rules
 * 
 * @author jasvir@google.com (Jasvir Nagra)
 *
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RulesDescription {
  /**
   * @return Name of this set of transformation rules
   */
  String name();

  /**
   * @return Summary of the effect of this transformation
   */
  String synopsis();
}
