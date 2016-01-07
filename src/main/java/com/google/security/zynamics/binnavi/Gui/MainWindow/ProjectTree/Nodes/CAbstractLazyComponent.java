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
 * Abstract base class for lazy component loading.
 */
public abstract class CAbstractLazyComponent implements ILazyComponent {
  /**
   * The lazily created component.
   */
  private CAbstractNodeComponent m_component = null;

  /**
   * Concretely creates the component.
   * 
   * @return The created component.
   */
  protected abstract CAbstractNodeComponent createComponent();

  @Override
  public void dispose() {
    if (m_component != null) {
      m_component.dispose();
    }
  }

  @Override
  public final Component getComponent() {
    if (m_component == null) {
      m_component = createComponent();
    }

    return m_component;
  }
}
