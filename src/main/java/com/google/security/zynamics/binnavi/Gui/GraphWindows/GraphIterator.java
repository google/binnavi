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
package com.google.security.zynamics.binnavi.Gui.GraphWindows;

import java.util.Iterator;

import javax.swing.JTabbedPane;

/**
 * Iterator for iterating over the graph panels of a single graph window.
 */
public final class GraphIterator implements Iterator<IGraphPanel> {
  /**
   * Current iterator position.
   */
  private int position = 0;

  /**
   * Holds the panel objects to iterate through.
   */
  private final JTabbedPane m_tabbedPane;

  /**
   * Creates a new iterator object.
   *
   * @param tabbedPane Holds the panel objects to iterate through.
   */
  public GraphIterator(final JTabbedPane tabbedPane) {
    m_tabbedPane = tabbedPane;
  }

  @Override
  public boolean hasNext() {
    return position != m_tabbedPane.getTabCount();
  }

  @Override
  public IGraphPanel next() {
    position++;

    return (IGraphPanel) m_tabbedPane.getComponentAt(position - 1);
  }

  @Override
  public void remove() {
    // do nothing here
  }
}
