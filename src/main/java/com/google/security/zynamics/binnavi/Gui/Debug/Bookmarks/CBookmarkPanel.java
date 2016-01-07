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
package com.google.security.zynamics.binnavi.Gui.Debug.Bookmarks;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.CAbstractResultsPanel;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;

/**
 * Panel class that is used to display bookmark information.
 */
public final class CBookmarkPanel extends CAbstractResultsPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -3264423088606199474L;

  /**
   * The bookmark table that is shown in the panel.
   */
  private final CBookmarkTable m_bookmarkTable;

  /**
   * Creates a new bookmark panel.
   *
   * @param debuggerProvider Provides the debuggers where breakpoints can be set.
   */
  public CBookmarkPanel(final BackEndDebuggerProvider debuggerProvider) {
    super(new BorderLayout());

    Preconditions.checkNotNull(debuggerProvider, "IE01320: Debugger provider can't be null");

    m_bookmarkTable = new CBookmarkTable(debuggerProvider);

    add(new JScrollPane(m_bookmarkTable), BorderLayout.CENTER);
  }

  /**
   * Frees allocated resources.
   */
  @Override
  public void dispose() {
    m_bookmarkTable.dispose();
  }

  @Override
  public String getTitle() {
    return "Bookmarks";
  }
}
