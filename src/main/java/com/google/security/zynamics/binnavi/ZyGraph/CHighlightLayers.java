/*
Copyright 2011-2016 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.google.security.zynamics.binnavi.ZyGraph;

/**
 * To avoid duplicate layer identifiers, this class contains identifiers for the standard
 * highlighting layers like breakpoint highlighting, program counter highlighting, and so on.
 */
public final class CHighlightLayers {
  /**
   * Line highlighting happens on this layer.
   */
  public static final int HIGHLIGHTING_LAYER = 400;

  /**
   * Breakpoints are displayed on this layer.
   */
  public static final int BREAKPOINT_LAYER = 500;

  /**
   * Variables are highlighted on this layer.
   */
  public static final int VARIABLE_LAYER = 600;

  /**
   * Register tracking results are displayed on this layer.
   */
  public static final int REGISTER_TRACKING_LAYER = 800;

  /**
   * During debugging, the current PC is displayed on this layer.
   */
  public static final int PROGRAM_COUNTER_LAYER = 1000;

  /**
   * Layer used to highlight special instructions.
   */
  public static final int SPECIAL_INSTRUCTION_LAYER = 1200;

  /**
   * You are not supposed to instantiate this class.
   */
  private CHighlightLayers() {
  }
}
