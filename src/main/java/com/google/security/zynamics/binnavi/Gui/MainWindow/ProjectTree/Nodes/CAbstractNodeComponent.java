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

import java.awt.LayoutManager;

import javax.swing.JPanel;

/**
 * Abstract base class for all components shown when project tree nodes are selected.
 */
public abstract class CAbstractNodeComponent extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -1669418717578999886L;

  /**
   * Creates a new component object.
   * 
   * @param layout The LayoutManager to use.
   */
  public CAbstractNodeComponent(final LayoutManager layout) {
    super(layout);
  }

  /**
   * Frees allocated resources.
   */
  public abstract void dispose();
}
