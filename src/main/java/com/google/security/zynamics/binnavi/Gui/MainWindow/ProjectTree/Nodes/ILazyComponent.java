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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes;

import java.awt.Component;

/**
 * This interface is used for lazy loading of component. This was introduced to avoid loading the
 * components of project tree nodes as soon as the nodes themselves are created. Rather, the
 * components are now created right before they are first shown on the screen.
 */
public interface ILazyComponent {
  /**
   * Disposes the created component.
   */
  void dispose();

  /**
   * Returns the created component.
   * 
   * @return The created component.
   */
  Component getComponent();
}
