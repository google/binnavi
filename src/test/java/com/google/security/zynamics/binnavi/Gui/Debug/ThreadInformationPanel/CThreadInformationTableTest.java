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
package com.google.security.zynamics.binnavi.Gui.Debug.ThreadInformationPanel;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.Gui.Debug.ThreadInformationPanel.CThreadInformationTable;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class CThreadInformationTableTest {
  @Test
  public void test() {
    final CThreadInformationTable table = new CThreadInformationTable();

    final TargetProcessThread thread = new TargetProcessThread(0, ThreadState.RUNNING);

    assertEquals(0, table.getModel().getRowCount());

    table.getModel().addThread(thread);

    assertEquals(1, table.getModel().getRowCount());

    for (int column = 0; column < table.getModel().getColumnCount(); column++) {
      table.getModel().getValueAt(0, column);
    }

    table.getModel().removeThread(thread);

    assertEquals(0, table.getModel().getRowCount());

    table.getModel().reset();
  }
}
