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
package com.google.security.zynamics.binnavi.Gui.Debug.MemorySectionPanel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Debug.Debugger.MockDebugger;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.MockGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.DetachReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.MemoryMapReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ProcessStartReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.TargetInformationReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.ModuleTargetSettings;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryMap;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessStart;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerOptions;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterDescription;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.types.lists.FilledList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;

@RunWith(JUnit4.class)
public final class CMemorySectionPanelSynchronizerTest {
  private final CMemorySectionBox m_sectionBox = new CMemorySectionBox();

  private final IGraphModel m_graphModel = new MockGraphModel();

  private final CDebugPerspectiveModel m_model = new CDebugPerspectiveModel(m_graphModel);

  private final CMemorySectionPanelSynchronizer m_synchronizer =
      new CMemorySectionPanelSynchronizer(m_sectionBox, m_model);

  @Test
  public void testChangingAddress() throws DebugExceptionWrapper {
    // Makes sure to update the combo box when the active address changes

    final MockDebugger debugger =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));

    debugger.connect();

    m_model.setActiveDebugger(debugger);

    final MemorySection section1 = new MemorySection(new CAddress(0x1000), new CAddress(0x1FF0));
    final MemorySection section2 = new MemorySection(new CAddress(0x3000), new CAddress(0x3FF0));

    final MemoryMap memoryMap = new MemoryMap(Lists.newArrayList(section1, section2));

    debugger.connection.m_synchronizer.receivedEvent(new MemoryMapReply(0, 0, memoryMap));

    assertEquals(2, m_sectionBox.getItemCount());
    assertEquals(section1, m_sectionBox.getItemAt(0).getObject());
    assertEquals(section2, m_sectionBox.getItemAt(1).getObject());

    assertEquals(section1, m_sectionBox.getSelectedItem().getObject());

    m_model.setActiveMemoryAddress(new CAddress(0x3000), false);

    assertEquals(section2, m_sectionBox.getSelectedItem().getObject());

    m_synchronizer.dispose();

    debugger.close();
  }

  @Test
  public void testDetach() throws DebugExceptionWrapper {
    final MockDebugger debugger =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));

    final TargetProcessThread thread = new TargetProcessThread(1, ThreadState.SUSPENDED);
    debugger.getProcessManager().addThread(thread);
    debugger.getProcessManager().setActiveThread(thread);

    debugger.connect();

    m_model.setActiveDebugger(debugger);

    debugger.connection.m_synchronizer.receivedEvent(new TargetInformationReply(0,
        0,
        new TargetInformation(32, new FilledList<RegisterDescription>(), new DebuggerOptions(false,
            false,
            false,
            true,
            false,
            false,
            false,
            false,
            false,
            false,
            1,
            0,
            new ArrayList<DebuggerException>(),
            false,
            false,
            false))));

    final MemorySection section1 = new MemorySection(new CAddress(0x100), new CAddress(0x1FF));
    final MemorySection section2 = new MemorySection(new CAddress(0x300), new CAddress(0x3FF));

    final MemoryMap memoryMap = new MemoryMap(Lists.newArrayList(section1, section2));

    debugger.connection.m_synchronizer.receivedEvent(new MemoryMapReply(0, 0, memoryMap));

    assertEquals(2, m_sectionBox.getItemCount());
    assertEquals(section1, m_sectionBox.getItemAt(0).getObject());
    assertEquals(section2, m_sectionBox.getItemAt(1).getObject());
    assertTrue(m_sectionBox.isEnabled());

    debugger.connection.m_synchronizer.receivedEvent(new DetachReply(0, 0));

    assertEquals(0, m_sectionBox.getItemCount());
    assertFalse(m_sectionBox.isEnabled());

    m_synchronizer.dispose();

    debugger.close();
  }

  @Test
  public void testReceivedMemoryMap() throws DebugExceptionWrapper {
    final MockDebugger debugger =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));

    debugger.connect();

    m_model.setActiveDebugger(debugger);

    final MemorySection section1 = new MemorySection(new CAddress(0x100), new CAddress(0x1FF));
    final MemorySection section2 = new MemorySection(new CAddress(0x300), new CAddress(0x3FF));

    final MemoryMap memoryMap = new MemoryMap(Lists.newArrayList(section1, section2));

    debugger.connection.m_synchronizer.receivedEvent(new MemoryMapReply(0, 0, memoryMap));

    assertEquals(2, m_sectionBox.getItemCount());
    assertEquals(section1, m_sectionBox.getItemAt(0).getObject());
    assertEquals(section2, m_sectionBox.getItemAt(1).getObject());

    m_synchronizer.dispose();

    debugger.close();
  }

  @Test
  public void testReceiveTargetInformation() throws DebugExceptionWrapper {
    final MockDebugger debugger =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));

    final TargetProcessThread thread = new TargetProcessThread(1, ThreadState.SUSPENDED);
    debugger.getProcessManager().addThread(thread);
    debugger.getProcessManager().setActiveThread(thread);

    final MemorySection section1 = new MemorySection(new CAddress(0x100), new CAddress(0x1FF));
    final MemorySection section2 = new MemorySection(new CAddress(0x300), new CAddress(0x3FF));

    final MemoryMap memoryMap = new MemoryMap(Lists.newArrayList(section1, section2));

    debugger.connection.m_synchronizer.receivedEvent(new MemoryMapReply(0, 0, memoryMap));

    debugger.connect();

    m_model.setActiveDebugger(debugger);

    debugger.getProcessManager().setTargetInformation(new TargetInformation(32,
        new FilledList<RegisterDescription>(), new DebuggerOptions(false,
            false,
            false,
            true,
            false,
            false,
            false,
            false,
            false,
            false,
            1,
            0,
            new ArrayList<DebuggerException>(),
            false,
            false,
            false)));

    assertTrue(m_sectionBox.isEnabled());

    debugger.getProcessManager().setTargetInformation(new TargetInformation(32,
        new FilledList<RegisterDescription>(), new DebuggerOptions(false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            1,
            0,
            new ArrayList<DebuggerException>(),
            false,
            false,
            false)));

    assertFalse(m_sectionBox.isEnabled());

    m_synchronizer.dispose();

    debugger.close();
  }

  @Test
  public void testSwitchDebugger() throws DebugExceptionWrapper {
    final MemorySection section1 = new MemorySection(new CAddress(0x100), new CAddress(0x1FF));
    final MemorySection section2 = new MemorySection(new CAddress(0x300), new CAddress(0x3FF));
    final MemoryMap memoryMap = new MemoryMap(Lists.newArrayList(section1, section2));

    final TargetProcessThread thread = new TargetProcessThread(0x666, ThreadState.RUNNING);
    final MemoryModule module = new MemoryModule("narf.exe", "C:\\zort\\narf.exe",
        new RelocatedAddress(new CAddress(0x1000)), 123345);

    final MockDebugger debugger =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));
    debugger.connect();
    debugger.connection.m_synchronizer.receivedEvent(new TargetInformationReply(0,
        0,
        new TargetInformation(32, new FilledList<RegisterDescription>(), new DebuggerOptions(false,
            false,
            false,
            true,
            false,
            false,
            false,
            false,
            false,
            false,
            1,
            0,
            new ArrayList<DebuggerException>(),
            false,
            false,
            false))));
    debugger.connection.m_synchronizer.receivedEvent(new MemoryMapReply(0, 0, memoryMap));
    debugger.connection.m_synchronizer.receivedEvent(
        new ProcessStartReply(0, 0, new ProcessStart(thread, module)));

    final MockDebugger debugger2 =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));
    debugger2.connect();
    debugger2.connection.m_synchronizer.receivedEvent(new TargetInformationReply(0,
        0,
        new TargetInformation(32, new FilledList<RegisterDescription>(), new DebuggerOptions(false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            1,
            0,
            new ArrayList<DebuggerException>(),
            false,
            false,
            false))));
    debugger2.connection.m_synchronizer.receivedEvent(new MemoryMapReply(0, 0, memoryMap));
    debugger2.connection.m_synchronizer.receivedEvent(
        new ProcessStartReply(0, 0, new ProcessStart(thread, module)));

    m_model.setActiveDebugger(debugger);
    assertTrue(m_sectionBox.isEnabled());

    m_model.setActiveDebugger(debugger2);
    assertFalse(m_sectionBox.isEnabled());

    m_synchronizer.dispose();

    debugger.close();
    debugger2.close();
  }

  @Test
  public void testThreads() throws DebugExceptionWrapper {
    final MockDebugger debugger =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));

    debugger.connect();
    debugger.getProcessManager().setAttached(true);
    debugger.connection.m_synchronizer.receivedEvent(new TargetInformationReply(0,
        0,
        new TargetInformation(32, new FilledList<RegisterDescription>(), new DebuggerOptions(false,
            false,
            false,
            true,
            false,
            false,
            false,
            false,
            false,
            false,
            1,
            0,
            new ArrayList<DebuggerException>(),
            false,
            false,
            false))));

    final MemorySection section1 = new MemorySection(new CAddress(0x100), new CAddress(0x1FF));
    final MemorySection section2 = new MemorySection(new CAddress(0x300), new CAddress(0x3FF));

    final MemoryMap memoryMap = new MemoryMap(Lists.newArrayList(section1, section2));

    debugger.connection.m_synchronizer.receivedEvent(new MemoryMapReply(0, 0, memoryMap));

    final TargetProcessThread thread = new TargetProcessThread(1, ThreadState.RUNNING);

    m_model.setActiveDebugger(debugger);
    debugger.getProcessManager().setActiveThread(null);

    assertFalse(m_sectionBox.isEnabled());

    debugger.getProcessManager().addThread(thread);

    assertFalse(m_sectionBox.isEnabled());

    debugger.getProcessManager().setActiveThread(thread);

    assertTrue(m_sectionBox.isEnabled());

    debugger.getProcessManager().setActiveThread(null);

    assertFalse(m_sectionBox.isEnabled());

    debugger.getProcessManager().removeThread(thread);

    assertFalse(m_sectionBox.isEnabled());

    debugger.getProcessManager().setAttached(false);

    m_synchronizer.dispose();

    debugger.close();
  }
}
