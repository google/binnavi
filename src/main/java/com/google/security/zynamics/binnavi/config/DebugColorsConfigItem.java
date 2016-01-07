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
package com.google.security.zynamics.binnavi.config;

import com.google.security.zynamics.common.config.AbstractConfigItem;
import com.google.security.zynamics.common.config.TypedPropertiesWrapper;

import java.awt.Color;

public class DebugColorsConfigItem extends AbstractConfigItem {
  private static final String BREAKPOINT_ACTIVE_COLOR = "BreakpointActive";
  private static final Color BREAKPOINT_ACTIVE_COLOR_DEFAULT = new Color(-16740608);
  private Color breakpointActive = BREAKPOINT_ACTIVE_COLOR_DEFAULT;

  private static final String BREAKPOINT_INACTIVE_COLOR = "BreakpointInactive";
  private static final Color BREAKPOINT_INACTIVE_COLOR_DEFAULT = new Color(-16763956);
  private Color breakpointInactive = BREAKPOINT_INACTIVE_COLOR_DEFAULT;

  private static final String BREAKPOINT_DISABLED_COLOR = "BreakpointDisabled";
  private static final Color BREAKPOINT_DISABLED_COLOR_DEFAULT = new Color(-5592663);
  private Color breakpointDisabled = BREAKPOINT_DISABLED_COLOR_DEFAULT;

  private static final String BREAKPOINT_HIT_COLOR = "BreakpointHit";
  private static final Color BREAKPOINT_HIT_COLOR_DEFAULT = new Color(-5046272);
  private Color breakpointHit = BREAKPOINT_HIT_COLOR_DEFAULT;

  private static final String BREAKPOINT_ENABLED_COLOR = "BreakpointEnabled";
  private static final Color BREAKPOINT_ENABLED_COLOR_DEFAULT = new Color(-16740608);
  private Color breakpointEnabled = BREAKPOINT_ENABLED_COLOR_DEFAULT;

  private static final String BREAKPOINT_INVALID_COLOR = "BreakpointInvalid";
  private static final Color BREAKPOINT_INVALID_COLOR_DEFAULT = new Color(-16777216);
  private Color breakpointInvalid = BREAKPOINT_INVALID_COLOR_DEFAULT;

  private static final String BREAKPOINT_DELETING_COLOR = "BreakpointDeleting";
  private static final Color BREAKPOINT_DELETING_COLOR_DEFAULT = new Color(-3328);
  private Color breakpointDeleting = BREAKPOINT_DELETING_COLOR_DEFAULT;

  private static final String ACTIVE_LINE_COLOR = "ActiveLine";
  private static final Color ACTIVE_LINE_COLOR_DEFAULT = new Color(-65536);
  private Color activeLine = ACTIVE_LINE_COLOR_DEFAULT;

  @Override
  public void load(final TypedPropertiesWrapper properties) {
    breakpointActive =
        properties.getColor(BREAKPOINT_ACTIVE_COLOR, BREAKPOINT_ACTIVE_COLOR_DEFAULT);
    breakpointInactive =
        properties.getColor(BREAKPOINT_INACTIVE_COLOR, BREAKPOINT_INACTIVE_COLOR_DEFAULT);
    breakpointDisabled =
        properties.getColor(BREAKPOINT_DISABLED_COLOR, BREAKPOINT_DISABLED_COLOR_DEFAULT);
    breakpointHit = properties.getColor(BREAKPOINT_HIT_COLOR, BREAKPOINT_HIT_COLOR_DEFAULT);
    breakpointEnabled =
        properties.getColor(BREAKPOINT_ENABLED_COLOR, BREAKPOINT_ENABLED_COLOR_DEFAULT);
    breakpointInvalid =
        properties.getColor(BREAKPOINT_INVALID_COLOR, BREAKPOINT_INVALID_COLOR_DEFAULT);
    breakpointDeleting =
        properties.getColor(BREAKPOINT_DELETING_COLOR, BREAKPOINT_DELETING_COLOR_DEFAULT);
    activeLine = properties.getColor(ACTIVE_LINE_COLOR, ACTIVE_LINE_COLOR_DEFAULT);
  }

  @Override
  public void store(final TypedPropertiesWrapper properties) {
    properties.setColor(BREAKPOINT_ACTIVE_COLOR, breakpointActive);
    properties.setColor(BREAKPOINT_INACTIVE_COLOR, breakpointInactive);
    properties.setColor(BREAKPOINT_DISABLED_COLOR, breakpointDisabled);
    properties.setColor(BREAKPOINT_HIT_COLOR, breakpointHit);
    properties.setColor(BREAKPOINT_ENABLED_COLOR, breakpointEnabled);
    properties.setColor(BREAKPOINT_INVALID_COLOR, breakpointInvalid);
    properties.setColor(BREAKPOINT_DELETING_COLOR, breakpointDeleting);
    properties.setColor(ACTIVE_LINE_COLOR, activeLine);
  }

  public Color getBreakpointActive() {
    return breakpointActive;
  }

  public void setBreakpointActive(final Color value) {
    this.breakpointActive = value;
  }

  public Color getBreakpointInactive() {
    return breakpointInactive;
  }

  public void setBreakpointInactive(final Color value) {
    this.breakpointInactive = value;
  }

  public Color getBreakpointDisabled() {
    return breakpointDisabled;
  }

  public void setBreakpointDisabled(final Color value) {
    this.breakpointDisabled = value;
  }

  public Color getBreakpointHit() {
    return breakpointHit;
  }

  public void setBreakpointHit(final Color value) {
    this.breakpointHit = value;
  }

  public Color getBreakpointEnabled() {
    return breakpointEnabled;
  }

  public void setBreakpointEnabled(final Color value) {
    this.breakpointEnabled = value;
  }

  public Color getBreakpointInvalid() {
    return breakpointInvalid;
  }

  public void setBreakpointInvalid(final Color value) {
    this.breakpointInvalid = value;
  }

  public Color getBreakpointDeleting() {
    return breakpointDeleting;
  }

  public void setBreakpointDeleting(final Color value) {
    this.breakpointDeleting = value;
  }

  public Color getActiveLine() {
    return activeLine;
  }

  public void setActiveLine(final Color value) {
    this.activeLine = value;
  }
}
