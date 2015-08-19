/*
Copyright 2014 Google Inc. All Rights Reserved.

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

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Debug.Debugger.MockDebugger;
import com.google.security.zynamics.binnavi.Gui.Debug.Bookmarks.CBookmarkTableModel;
import com.google.security.zynamics.binnavi.Gui.Debug.Bookmarks.Implementations.CBookmarkFunctions;
import com.google.security.zynamics.binnavi.debug.debugger.DebugTargetSettings;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.ModuleTargetSettings;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.models.Bookmarks.memory.BookmarkManager;
import com.google.security.zynamics.binnavi.models.Bookmarks.memory.CBookmark;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.MockAddress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CBookmarksTableModelTest {
  @Test
  public void test1DynamicDebugger() {
    final DebugTargetSettings target = new ModuleTargetSettings(CommonTestObjects.MODULE);
    final DebuggerProvider debuggerProvider = new DebuggerProvider(target);

    final CBookmarkTableModel model = new CBookmarkTableModel(debuggerProvider);

    final MockDebugger debugger =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));
    debugger.getBreakpointManager().addBreakpoints(BreakpointType.REGULAR,
        CommonTestObjects.BP_ADDRESS_0_SET);
    debuggerProvider.addDebugger(debugger);

    model.dispose();
  }

  @Test
  public void test2getValueAt() {
    final DebugTargetSettings target = new ModuleTargetSettings(CommonTestObjects.MODULE);
    final DebuggerProvider debuggerProvider = new DebuggerProvider(target);

    final CBookmarkTableModel model = new CBookmarkTableModel(debuggerProvider);

    final IAddress address = new MockAddress();
    final CBookmark bookmark = new CBookmark(address, "foo");

    final MockDebugger debugger =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));
    debugger.getBreakpointManager().addBreakpoints(BreakpointType.REGULAR,
        CommonTestObjects.BP_ADDRESS_0_SET);
    debuggerProvider.addDebugger(debugger);

    final BookmarkManager bookmarkManager = debugger.getBookmarkManager();
    bookmarkManager.addBookmark(bookmark);

    assertEquals(1, bookmarkManager.getNumberOfBookmarks());
    assertEquals("foo", model.getValueAt(0, 2));
    assertEquals(address.toHexString(), model.getValueAt(0, 1));
    assertEquals(debugger.getPrintableString(), model.getValueAt(0, 0));

    final int[] rows = {0};
    CBookmarkFunctions.deleteBookmarks(debuggerProvider, rows);

    assertEquals(0, bookmarkManager.getNumberOfBookmarks());

    model.dispose();
  }
}
