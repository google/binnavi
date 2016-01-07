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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.InstructionHighlighter;

import java.awt.Color;

/**
 * Listener interface for objects that want to be notified abotu changes in type descriptions.
 */
public interface ITypeDescriptionListener {
  /**
   * Invoked after the color of a type description changed.
   *
   * @param color The new color.
   */
  void changedColor(Color color);

  /**
   * Invoked after the inclusion state of a type description changed.
   *
   * @param enabled True, to include the instructions. False, to exclude them.
   */
  void changedStatus(boolean enabled);
}
