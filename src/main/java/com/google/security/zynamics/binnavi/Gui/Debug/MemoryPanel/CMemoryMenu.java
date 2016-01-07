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
package com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Actions.CCreateBookmarkAction;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Actions.CDeleteBookmarkAction;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Actions.CDumpMemoryRangeAction;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Actions.CFollowDumpAction;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Actions.CGotoAction;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Actions.CGotoBookmarkAction;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Actions.CLoadAllAction;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Actions.CSearchAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Data.Component.HexViewOptionsMenu;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryMap;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessHelpers;
import com.google.security.zynamics.binnavi.models.Bookmarks.memory.CBookmark;
import com.google.security.zynamics.binnavi.models.Bookmarks.memory.BookmarkManager;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ByteHelpers;
import com.google.security.zynamics.zylib.gui.JHexPanel.IMenuCreator;

/**
 * Class that is used to create the context menu of the memory view component.
 */
public final class CMemoryMenu implements IMenuCreator {
  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Debugger that provides the memory data.
   */
  private final CDebugPerspectiveModel m_debugger;

  /**
   * Hex panel the menu belongs to.
   */
  private final CMemoryViewer m_memoryView;

  /**
   * Creates a new context menu for memory viewers.
   * 
   * @param parent Parent window used for dialogs.
   * @param debugger Debugger that provides the memory data.
   * @param memoryView Hex panel the menu belongs to.
   */
  public CMemoryMenu(final JFrame parent, final CDebugPerspectiveModel debugger,
      final CMemoryViewer memoryView) {
    m_parent = Preconditions.checkNotNull(parent, "IE01396: Parent argument can not be null");
    m_debugger = Preconditions.checkNotNull(debugger, "IE01397: Debugger argument can not be null");
    m_memoryView =
        Preconditions.checkNotNull(memoryView, "IE01398: Memory view argument can not be null");
  }

  /**
   * Tests whether a DWORD value can be read from memory.
   * 
   * @param memoryMap The memory map to check.
   * @param offset Start offset of the DWORD to read.
   * 
   * @return True, if a DWORD can be read. False, otherwise.
   */
  private boolean canReadDword(final MemoryMap memoryMap, final long offset) {
    return containsOffset(memoryMap, new CAddress(offset))
        && containsOffset(memoryMap, new CAddress(offset + 3));
  }

  /**
   * Checks whether the simulated memory map of a target process contains a memory section with the
   * given given offset.
   * 
   * @param memoryMap The memory map to check.
   * @param offset The memory address to check for.
   * 
   * @return True, if the memory map contains a memory section with the given offset. False,
   *         otherwise.
   */
  private boolean containsOffset(final MemoryMap memoryMap, final IAddress offset) {
    return ProcessHelpers.getSectionWith(memoryMap, offset) != null;
  }

  /**
   * Creates the context menu of a memory viewer component.
   * 
   * @param offset The memory offset where the context menu will be shown.
   * 
   * @return The context menu for the specified address.
   */
  @Override
  public JPopupMenu createMenu(final long offset) {
    final JPopupMenu menu = new JPopupMenu();

    final IDebugger debugger = m_debugger.getCurrentSelectedDebugger();

    if (debugger == null) {
      return null;
    }

    menu.add(CActionProxy.proxy(new CSearchAction(m_parent, m_debugger, m_memoryView)));
    menu.add(CActionProxy.proxy(new CGotoAction(m_parent, m_memoryView, m_debugger)));

    if (canReadDword(debugger.getProcessManager().getMemoryMap(), offset)) {
      final byte[] data = debugger.getProcessManager().getMemory().getData(offset, 4);
      final IAddress dword = new CAddress(ByteHelpers.readDwordLittleEndian(data, 0));

      if (canReadDword(debugger.getProcessManager().getMemoryMap(), dword.toLong())) {
        menu.add(CActionProxy.proxy(new CFollowDumpAction(m_debugger, dword)));
      }
    }

    menu.addSeparator();

    final long firstOffset = m_memoryView.getHexView().getBaseAddress();
    final int size = m_memoryView.getHexView().getData().getDataLength();

    menu.add(new CLoadAllAction(m_parent, debugger, new CAddress(firstOffset), size));

    // Offer the option to dump memory
    final JMenu dumpMenu = new JMenu("Dump to file");

    dumpMenu.add(CActionProxy.proxy(new CDumpMemoryRangeAction(m_parent, debugger, m_memoryView
        .getHexView().getData(), new CAddress(firstOffset), size)));

    menu.add(dumpMenu);

    menu.addSeparator();

    final BookmarkManager manager = debugger.getBookmarkManager();

    // At first offer the option to add or remove a bookmark
    // at the specified position.
    final CBookmark bookmark = manager.getBookmark(new CAddress(offset));

    if (bookmark == null) {
      menu.add(new JMenuItem(CActionProxy.proxy(new CCreateBookmarkAction(manager, new CAddress(
          offset)))));
    } else {
      menu.add(new JMenuItem(CActionProxy.proxy(new CDeleteBookmarkAction(manager, bookmark))));
    }

    if (manager.getNumberOfBookmarks() != 0) {
      // Afterwards list all currently active bookmarks.

      menu.addSeparator();

      final JMenu bookmarksItem = new JMenu("Bookmarks");

      for (int i = 0; i < manager.getNumberOfBookmarks(); i++) {
        bookmarksItem.add(CActionProxy.proxy(new CGotoBookmarkAction(m_debugger, manager
            .getBookmark(i))));
      }

      menu.add(bookmarksItem);
    }

    menu.addSeparator();

    menu.add(HexViewOptionsMenu.createHexViewOptionsMenu(m_memoryView.getHexView()));

    return menu;
  }
}
