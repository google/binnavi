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
package com.google.security.zynamics.binnavi.Gui.CodeBookmarks.Implemementations;

import java.util.ArrayList;
import java.util.List;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.models.Bookmarks.code.CCodeBookmark;
import com.google.security.zynamics.binnavi.models.Bookmarks.code.CCodeBookmarkManager;
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Contains functions that are available for working with code bookmarks.
 */
public final class CBookmarkFunctions {
  /**
   * You are not supposed to instantiate this.
   */
  private CBookmarkFunctions() {
  }

  /**
   * Adds a code bookmark.
   *
   * @param manager The bookmark manager the code bookmark is added to.
   * @param module Module the code bookmark belongs to.
   * @param address Address where the code bookmark is set.
   */
  public static void addBookmark(
      final CCodeBookmarkManager manager, final INaviModule module, final IAddress address) {
    manager.addBookmark(new CCodeBookmark(module, address, ""));
  }

  /**
   * Deletes a list of bookmarks.
   *
   * @param manager Code bookmark manager from which the bookmarks are deleted.
   * @param rows Indices of the table rows of the bookmarks to delete.
   */
  public static void deleteBookmarks(final CCodeBookmarkManager manager, final int[] rows) {
    Preconditions.checkNotNull(manager, "IE01262: Manager argument can not be null");
    Preconditions.checkNotNull(rows, "IE01263: Rows argument can not be null");

    final List<CCodeBookmark> bookmarks = new ArrayList<CCodeBookmark>();

    for (final int row : rows) {
      bookmarks.add(manager.get(row));
    }

    for (final CCodeBookmark bookmark : bookmarks) {
      manager.removeBookmark(bookmark);
    }
  }
}
