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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ViewSearcher;



import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.google.common.base.Preconditions;

/**
 * Action class for the button the user hits for searching for a view with an address.
 */
public final class SearchAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3119186724113350891L;

  /**
   * View searcher dialog where the search operation takes place.
   */
  private final CViewSearcherDialog m_dialog;

  /**
   * Creates a new action object.
   * 
   * @param dialog View searcher dialog where the search operation takes place.
   */
  public SearchAction(final CViewSearcherDialog dialog) {
    super("Search");
    m_dialog = Preconditions.checkNotNull(dialog, "IE01295: Dialog argument can not be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    m_dialog.search();
  }
}
