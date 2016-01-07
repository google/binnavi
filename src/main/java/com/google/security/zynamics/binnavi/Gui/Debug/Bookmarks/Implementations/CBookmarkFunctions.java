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
package com.google.security.zynamics.binnavi.Gui.Debug.Bookmarks.Implementations;

import java.util.ArrayList;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.Bookmarks.CBookmarkTableHelpers;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.models.Bookmarks.memory.CBookmark;
import com.google.security.zynamics.binnavi.models.Bookmarks.memory.BookmarkManager;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.Triple;

/**
 * Contains the implementations of all actions that are available from the bookmarks table.
 */
public final class CBookmarkFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CBookmarkFunctions() {
  }

  /**
   * Deletes a list of bookmarks.
   *
   * @param provider Contains the debuggers where bookmarks can be set.
   * @param rows The rows of the bookmarks to delete.
   */
  public static void deleteBookmarks(final BackEndDebuggerProvider provider, final int[] rows) {
    Preconditions.checkNotNull(provider, "IE01329: Provider argument can not be null");
    Preconditions.checkNotNull(rows, "IE01330: Rows argument can not be null");

    final ArrayList<Pair<BookmarkManager, CBookmark>> bookmarks =
        new ArrayList<Pair<BookmarkManager, CBookmark>>();

    for (final int row : rows) {
      final Triple<IDebugger, BookmarkManager, Integer> bookmarkTriple =
          CBookmarkTableHelpers.findBookmark(provider, row);

      final BookmarkManager manager = bookmarkTriple.second();
      final int index = bookmarkTriple.third();

      bookmarks.add(Pair.make(manager, manager.getBookmark(index)));
    }

    for (final Pair<BookmarkManager, CBookmark> p : bookmarks) {
      p.first().removeBookmark(p.second());
    }
  }
}
