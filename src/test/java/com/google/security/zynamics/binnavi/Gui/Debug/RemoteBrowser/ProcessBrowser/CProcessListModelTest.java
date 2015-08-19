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
package com.google.security.zynamics.binnavi.Gui.Debug.RemoteBrowser.ProcessBrowser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.security.zynamics.binnavi.Gui.Debug.RemoteBrowser.ProcessBrowser.CProcessListModel;
import com.google.security.zynamics.binnavi.debug.models.processlist.ProcessList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;


@RunWith(JUnit4.class)
public class CProcessListModelTest {
  @Test
  public void test1Simple() throws ParserConfigurationException, SAXException, IOException {
    final ProcessList processList = ProcessList.parse("<foo></foo>".getBytes());
    @SuppressWarnings("unused")
    final CProcessListModel listModel = new CProcessListModel(processList);
  }

  @Test
  public void test2SimpleFail() {
    try {
      @SuppressWarnings("unused")
      final CProcessListModel listModel = new CProcessListModel(null);
      fail();
    } catch (final NullPointerException e) {
    }
  }

  @Test
  public void test3ColumnCount() throws ParserConfigurationException, SAXException, IOException {
    final ProcessList processList = ProcessList.parse("<foo></foo>".getBytes());
    final CProcessListModel listModel = new CProcessListModel(processList);
    assertEquals(2, listModel.getColumnCount());
  }

  @Test
  public void test4ColumnNames() throws ParserConfigurationException, SAXException, IOException {
    final ProcessList processList = ProcessList.parse("<foo></foo>".getBytes());
    final CProcessListModel listModel = new CProcessListModel(processList);

    assertEquals("PID", listModel.getColumnName(0));
    assertEquals("Name", listModel.getColumnName(1));
  }

  @Test
  public void test5RowCount() throws ParserConfigurationException, SAXException, IOException {
    final ProcessList processList = ProcessList.parse("<foo></foo>".getBytes());
    final CProcessListModel listModel = new CProcessListModel(processList);

    assertEquals(0, listModel.getRowCount());
  }
}
