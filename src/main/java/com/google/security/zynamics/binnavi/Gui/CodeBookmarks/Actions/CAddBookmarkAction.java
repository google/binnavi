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
package com.google.security.zynamics.binnavi.Gui.CodeBookmarks.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.CodeBookmarks.Implemementations.CBookmarkFunctions;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.models.Bookmarks.code.CCodeBookmarkManager;
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Action class used for creating a new code bookmark.
 */
public final class CAddBookmarkAction extends AbstractAction {

  /**
   * Code bookmark manager that manages the new bookmark.
   */
  private final CCodeBookmarkManager m_manager;

  /**
   * Module the new bookmark belongs to.
   */
  private final INaviModule m_module;

  /**
   * Address of the new bookmark.
   */
  private final IAddress m_address;

  /**
   * Creates a new action object.
   *
   * @param manager Code bookmark manager that manages the new bookmark.
   * @param module Module the new bookmark belongs to.
   * @param address Address of the new bookmark.
   */
  public CAddBookmarkAction(
      final CCodeBookmarkManager manager, final INaviModule module, final IAddress address) {
    super("Add Bookmark");

    m_manager = Preconditions.checkNotNull(manager, "IE01248: Manager argument can not be null");
    m_module = Preconditions.checkNotNull(module, "IE01249: Module argument can not be null");
    m_address = Preconditions.checkNotNull(address, "IE01250: Address argument can not be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CBookmarkFunctions.addBookmark(m_manager, m_module, m_address);
  }
}
