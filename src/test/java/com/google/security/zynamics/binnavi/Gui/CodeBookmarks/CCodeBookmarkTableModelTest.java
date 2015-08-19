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
package com.google.security.zynamics.binnavi.Gui.CodeBookmarks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.security.zynamics.binnavi.Gui.CodeBookmarks.CCodeBookmarkTableModel;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.models.Bookmarks.code.CCodeBookmark;
import com.google.security.zynamics.binnavi.models.Bookmarks.code.CCodeBookmarkManager;
import com.google.security.zynamics.zylib.disassembly.MockAddress;
import com.google.security.zynamics.zylib.reflection.ReflectionHelpers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.LinkedHashSet;

@RunWith(JUnit4.class)
public class CCodeBookmarkTableModelTest {
  @Test
  public void test1Simple() {
    final CCodeBookmarkManager bookmarkManager = new CCodeBookmarkManager();
    final CCodeBookmarkTableModel tableModel = new CCodeBookmarkTableModel(bookmarkManager);

    assertEquals(3, tableModel.getColumnCount());
    assertEquals(0, tableModel.getRowCount());
  }

  @Test
  public void test2setValueAtWorking() {
    final CCodeBookmarkManager bookmarkManager = new CCodeBookmarkManager();
    final CCodeBookmarkTableModel tableModel = new CCodeBookmarkTableModel(bookmarkManager);


    final CCodeBookmark bookmark = new CCodeBookmark(new MockModule(), new MockAddress(), "burzel");
    bookmarkManager.addBookmark(bookmark);

    assertEquals("burzel", tableModel.getValueAt(0, 2));

    tableModel.setValueAt("working", 0, 2);

    assertEquals(1, tableModel.getRowCount());
    assertEquals("working", tableModel.getValueAt(0, 2));
  }

  @Test
  public void test3setValueAtFail() {
    final CCodeBookmarkManager bookmarkManager = new CCodeBookmarkManager();
    final CCodeBookmarkTableModel tableModel = new CCodeBookmarkTableModel(bookmarkManager);


    final CCodeBookmark bookmark = new CCodeBookmark(new MockModule(), new MockAddress(), "burzel");
    bookmarkManager.addBookmark(bookmark);

    assertEquals("burzel", tableModel.getValueAt(0, 2));

    try {
      tableModel.setValueAt("working", 0, 1);
      fail();
    } catch (final IllegalArgumentException e) {
    }
  }

  @Test
  public void test4getValueAtWorking() {
    final CCodeBookmarkManager bookmarkManager = new CCodeBookmarkManager();
    final CCodeBookmarkTableModel tableModel = new CCodeBookmarkTableModel(bookmarkManager);


    final CCodeBookmark bookmark =
        new CCodeBookmark(new MockModule(), new MockAddress(), "burzel");
    bookmarkManager.addBookmark(bookmark);

    final MockAddress mock = new MockAddress();

    assertEquals("burzel", tableModel.getValueAt(0, 2));
    assertEquals(mock.toHexString(), tableModel.getValueAt(0, 1));
    assertEquals("Mock Module", tableModel.getValueAt(0, 0));
  }

  @Test
  public void test5getValueAtFail() {
    final CCodeBookmarkManager bookmarkManager = new CCodeBookmarkManager();
    final CCodeBookmarkTableModel tableModel = new CCodeBookmarkTableModel(bookmarkManager);


    final CCodeBookmark bookmark =
        new CCodeBookmark(new MockModule(), new MockAddress(), "burzel");
    bookmarkManager.addBookmark(bookmark);

    try {
      tableModel.getValueAt(0, 4);
      fail();
    } catch (final IllegalArgumentException e) {
    }
  }

  @Test
  public void test6getColumnName() {
    final CCodeBookmarkManager bookmarkManager = new CCodeBookmarkManager();
    final CCodeBookmarkTableModel tableModel = new CCodeBookmarkTableModel(bookmarkManager);


    final CCodeBookmark bookmark =
        new CCodeBookmark(new MockModule(), new MockAddress(), "burzel");
    bookmarkManager.addBookmark(bookmark);

    assertEquals("Module", tableModel.getColumnName(0));
    assertEquals("Address", tableModel.getColumnName(1));
    assertEquals("Description", tableModel.getColumnName(2));
  }

  @Test
  public void test7removeBookmarkWorking() {
    final CCodeBookmarkManager bookmarkManager = new CCodeBookmarkManager();
    final CCodeBookmarkTableModel tableModel = new CCodeBookmarkTableModel(bookmarkManager);


    final CCodeBookmark bookmark = new CCodeBookmark(new MockModule(), new MockAddress(), "burzel");
    bookmarkManager.addBookmark(bookmark);

    assertEquals("burzel", tableModel.getValueAt(0, 2));

    tableModel.setValueAt("working", 0, 2);

    assertEquals(1, tableModel.getRowCount());
    assertEquals("working", tableModel.getValueAt(0, 2));

    bookmarkManager.removeBookmark(bookmark);

    assertEquals(0, tableModel.getRowCount());
  }

  @Test
  public void test8checkEditable() {
    final CCodeBookmarkManager bookmarkManager = new CCodeBookmarkManager();
    final CCodeBookmarkTableModel tableModel = new CCodeBookmarkTableModel(bookmarkManager);


    final CCodeBookmark bookmark = new CCodeBookmark(new MockModule(), new MockAddress(), "burzel");
    bookmarkManager.addBookmark(bookmark);

    assertFalse(tableModel.isCellEditable(0, 0));
    assertFalse(tableModel.isCellEditable(0, 1));
    assertTrue(tableModel.isCellEditable(0, 2));
  }

  @Test
  public void test9checkRemoveListeners() throws IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    final CCodeBookmarkManager bookmarkManager = new CCodeBookmarkManager();
    final CCodeBookmarkTableModel tableModel = new CCodeBookmarkTableModel(bookmarkManager);

    final LinkedHashSet<?> m_daListeners =
        (LinkedHashSet<?>) ReflectionHelpers.getField(
            ReflectionHelpers.getField(bookmarkManager, "m_listeners"), "m_listeners");

    assertEquals(1, m_daListeners.size());

    final CCodeBookmark bookmark = new CCodeBookmark(new MockModule(), new MockAddress(), "burzel");
    bookmarkManager.addBookmark(bookmark);

    final LinkedHashSet<?> m_bmListeners =
        (LinkedHashSet<?>) ReflectionHelpers.getField(
            ReflectionHelpers.getField(bookmark, "m_listeners"), "m_listeners");

    assertEquals(1, m_bmListeners.size());

    tableModel.dispose();

    assertTrue(m_daListeners.isEmpty());
    assertTrue(m_bmListeners.isEmpty());
  }
}
