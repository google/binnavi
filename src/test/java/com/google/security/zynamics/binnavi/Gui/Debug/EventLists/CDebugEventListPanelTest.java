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
package com.google.security.zynamics.binnavi.Gui.Debug.EventLists;

import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.CDebugEventListPanel;
import com.google.security.zynamics.binnavi.debug.models.trace.ProjectTraceProvider;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListProvider;
import com.google.security.zynamics.binnavi.disassembly.MockProject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class CDebugEventListPanelTest {
  /**
   * This test makes sure that listeners are removed correctly, unlike seen in Case 2063.
   */
  @Test
  public void test_2063() {
    final ITraceListProvider provider = new ProjectTraceProvider(new MockProject());

    final CDebugEventListPanel panel = new CDebugEventListPanel(provider);

    panel.dispose();
  }
}
