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

import java.awt.Color;

/**
 * Interface that provides debugger settings. Right now only color settings are implemented.
 */
public interface IDebugSettings {

  // Getters/Setters for the various color settings. They should be fairly self-describing.
  Color getActiveBreakpointColor();
  void setActiveBreakpointColor(Color color);
  Color getActiveLineColor();
  void setActiveLineColor(Color color);
  Color getDeletingBreakpointColor();
  void setDeletingBreakpointColor(Color color);
  Color getDisabledBreakpointColor();
  void setDisabledBreakpointColor(Color color);
  Color getEnabledBreakpointColor();
  void setEnabledBreakpointColor(Color color);
  Color getHitBreakpointColor();
  void setHitBreakpointColor(Color color);
  Color getInactiveBreakpointColor();
  void setInactiveBreakpointColor(Color color);
  Color getInvalidBreakpointColor();
  void setInvalidBreakpointColor(Color color);
}
