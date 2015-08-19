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
package com.google.security.zynamics.binnavi.Gui.Debug.ModulesPanel;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Debug.Debugger.MockDebugger;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.MockGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.DetachReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.MemoryMapReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ModuleLoadedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ModuleUnloadedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.TargetInformationReply;
import com.google.security.zynamics.binnavi.debug.debugger.AbstractDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.ModuleTargetSettings;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryMap;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerOptions;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterDescription;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.reflection.ReflectionHelpers;
import com.google.security.zynamics.zylib.types.lists.FilledList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.LinkedHashSet;

@RunWith(JUnit4.class)
public final class CModulesTableModelSynchronizerTest {
  private final CDebugPerspectiveModel m_model = new CDebugPerspectiveModel(new MockGraphModel());
  private final CModulesTable m_table = new CModulesTable();
  private final CModulesTableModel m_tableModel = m_table.getTreeTableModel();
  private final CModulesTableModelSynchronizer m_synchronizer =
      new CModulesTableModelSynchronizer(m_table, m_model);

  @Test
  public void testDetach() throws DebugExceptionWrapper {
    final MockDebugger debugger =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));
    debugger.connect();
    debugger.getProcessManager().addModule(new MemoryModule("Hannes", "C:\\Hannes.dll",
        new RelocatedAddress(new CAddress(0x200)), 0x100));
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
    debugger.connection.m_synchronizer.receivedEvent(new DetachReply(0, 0));
    m_synchronizer.dispose();
    debugger.close();
  }

  @Test
  public void testModuleLifecycle()
      throws DebugExceptionWrapper,
      IllegalArgumentException,
      SecurityException,
      IllegalAccessException,
      NoSuchFieldException {
    final MockDebugger debugger =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));

    debugger.connect();

    m_model.setActiveDebugger(debugger);

    assertEquals(0, m_tableModel.getRowCount());

    final MemoryModule module = new MemoryModule("Hannes", "C:\\Hannes.dll",
        new RelocatedAddress(new CAddress(0x100)), 0x200);

    debugger.connection.m_synchronizer.receivedEvent(
        new ModuleLoadedReply(0, 0, module, new TargetProcessThread(123, ThreadState.SUSPENDED)));

    assertEquals(1, m_tableModel.getRowCount());
    assertEquals("Hannes", m_tableModel.getValueAt(0, 0));
    assertEquals("00000100", m_tableModel.getValueAt(0, 1));
    assertEquals(0x200L, m_tableModel.getValueAt(0, 2));

    debugger.getProcessManager().setTargetInformation(new TargetInformation(5, Lists.newArrayList(
        new RegisterDescription("eax", 4, true), new RegisterDescription("ebx", 4, false)),
        new DebuggerOptions(false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            12,
            0,
            new ArrayList<DebuggerException>(),
            false,
            false,
            false)));

    debugger.connection.m_synchronizer.receivedEvent(new ModuleUnloadedReply(0, 0, module));

    assertEquals(0, m_tableModel.getRowCount());

    m_synchronizer.dispose();

    debugger.close();

    final LinkedHashSet<?> debuggerListeners =
        (LinkedHashSet<?>) ReflectionHelpers.getField(ReflectionHelpers.getField(
            ReflectionHelpers.getField(AbstractDebugger.class, debugger, "synchronizer"),
            "listeners"), "m_listeners");
    final LinkedHashSet<?> processListeners =
        (LinkedHashSet<?>) ReflectionHelpers.getField(ReflectionHelpers.getField(
            ReflectionHelpers.getField(AbstractDebugger.class, debugger, "processManager"),
            "listeners"), "m_listeners");

    // The debugger only has one internal memory synchronizer
    assertEquals(1, debuggerListeners.size());
    // The process manager only has one thread state synchronizer
    assertEquals(1, processListeners.size());
  }

  @Test
  public void testSwitchDebugger() throws DebugExceptionWrapper {
    final MockDebugger debugger =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));
    debugger.connect();
    debugger.getProcessManager().addModule(new MemoryModule("Hannes", "C:\\Hannes.dll",
        new RelocatedAddress(new CAddress(0x200)), 0x100));

    final MockDebugger debugger2 =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));
    debugger2.connect();
    debugger2.getProcessManager().addModule(new MemoryModule("Hannes", "C:\\Hannes.dll",
        new RelocatedAddress(new CAddress(0x200)), 0x100));
    debugger2.getProcessManager().addModule(new MemoryModule("Hannes", "C:\\Hannes.dll",
        new RelocatedAddress(new CAddress(0x200)), 0x100));

    m_model.setActiveDebugger(debugger);

    assertEquals(1, m_tableModel.getRowCount());

    m_model.setActiveDebugger(debugger2);

    assertEquals(2, m_tableModel.getRowCount());

    m_model.setActiveDebugger(null);

    assertEquals(0, m_tableModel.getRowCount());

    m_synchronizer.dispose();

    debugger.close();
    debugger2.close();
  }
}
