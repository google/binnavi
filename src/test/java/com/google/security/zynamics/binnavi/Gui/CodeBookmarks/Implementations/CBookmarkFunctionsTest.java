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
package com.google.security.zynamics.binnavi.Gui.CodeBookmarks.Implementations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.security.zynamics.binnavi.Gui.CodeBookmarks.Implemementations.CBookmarkFunctions;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.models.Bookmarks.code.CCodeBookmarkManager;
import com.google.security.zynamics.zylib.disassembly.MockAddress;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CBookmarkFunctionsTest {
  @Test
  public void test1AddBookmark() {
    final CCodeBookmarkManager bookmarkManager = new CCodeBookmarkManager();
    final MockAddress mockAddress = new MockAddress();
    final MockModule mockModule = new MockModule();

    assertEquals(0, bookmarkManager.size());

    CBookmarkFunctions.addBookmark(bookmarkManager, mockModule, mockAddress);

    assertEquals(1, bookmarkManager.size());
  }

  @Test
  public void test2DeleteBookmarks() {
    final CCodeBookmarkManager bookmarkManager = new CCodeBookmarkManager();
    final MockAddress mockAddress = new MockAddress();
    final MockModule mockModule = new MockModule();

    CBookmarkFunctions.addBookmark(bookmarkManager, mockModule, mockAddress);
    CBookmarkFunctions.addBookmark(bookmarkManager, mockModule, mockAddress);
    CBookmarkFunctions.addBookmark(bookmarkManager, mockModule, mockAddress);
    CBookmarkFunctions.addBookmark(bookmarkManager, mockModule, mockAddress);
    CBookmarkFunctions.addBookmark(bookmarkManager, mockModule, mockAddress);
    CBookmarkFunctions.addBookmark(bookmarkManager, mockModule, mockAddress);
    CBookmarkFunctions.addBookmark(bookmarkManager, mockModule, mockAddress);
    CBookmarkFunctions.addBookmark(bookmarkManager, mockModule, mockAddress);

    assertEquals(8, bookmarkManager.size());

    final int[] rows = {1, 2, 3, 4, 5, 6, 7};

    CBookmarkFunctions.deleteBookmarks(bookmarkManager, rows);

    assertEquals(1, bookmarkManager.size());
  }

  @Test
  public void test3DeleteBookmarksNoManager() {
    final CCodeBookmarkManager bookmarkManager = new CCodeBookmarkManager();
    final MockAddress mockAddress = new MockAddress();
    final MockModule mockModule = new MockModule();

    CBookmarkFunctions.addBookmark(bookmarkManager, mockModule, mockAddress);

    final int[] foo = {0};

    try {
      CBookmarkFunctions.deleteBookmarks(null, foo);
      fail();
    } catch (final NullPointerException e) {
    }
  }

  @Test
  public void test4DeleteBookmarksNoRows() {
    final CCodeBookmarkManager bookmarkManager = new CCodeBookmarkManager();
    final MockAddress mockAddress = new MockAddress();
    final MockModule mockModule = new MockModule();

    CBookmarkFunctions.addBookmark(bookmarkManager, mockModule, mockAddress);

    try {
      CBookmarkFunctions.deleteBookmarks(bookmarkManager, null);
      fail();
    } catch (final NullPointerException e) {
    }
  }
}
