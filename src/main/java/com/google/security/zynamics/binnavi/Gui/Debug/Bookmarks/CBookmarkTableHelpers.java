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


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.models.Bookmarks.memory.BookmarkManager;
import com.google.security.zynamics.zylib.general.Triple;

/**
 * Provides convenience functions that are required in more than one class of the bookmark table
 * package.
 */
public final class CBookmarkTableHelpers {
  /**
   * You are not supposed to instantiate this class.
   */
  private CBookmarkTableHelpers() {
  }

  /**
   * Returns the corresponding bookmark manager and index for a row in the bookmarks table.
   *
   * @param debuggerProvider Provides the debuggers where breakpoints can be set.
   * @param row The row of the bookmark table.
   *
   * @return The bookmark manager/bookmark index pair for the bookmark in the given row.
   */
  public static Triple<IDebugger, BookmarkManager, Integer> findBookmark(
      final BackEndDebuggerProvider debuggerProvider, final int row) {
    Preconditions.checkNotNull(
        debuggerProvider, "IE01322: Debugger provider argument can't be null");
    Preconditions.checkArgument(row >= 0, "IE01323: Row arguments can not be negative");

    int bookmarks = 0;

    for (final IDebugger debugger : debuggerProvider.getDebuggers()) {
      if ((row >= bookmarks)
          && (row < bookmarks + debugger.getBookmarkManager().getNumberOfBookmarks())) {
        return Triple.make(debugger, debugger.getBookmarkManager(), row - bookmarks);
      } else {
        bookmarks += debugger.getBookmarkManager().getNumberOfBookmarks();
      }
    }

    throw new IllegalArgumentException("IE01324: Invalid row number");
  }
}
