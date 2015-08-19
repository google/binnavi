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
package com.google.security.zynamics.binnavi.Gui.Debug.StackPanel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Debug.Debugger.MockDebugger;
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
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValue;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.gui.JStackView.JStackView;
import com.google.security.zynamics.zylib.types.lists.FilledList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;
import java.util.ArrayList;

@RunWith(JUnit4.class)
public final class CStackViewSynchronizerTest {
  private final CDebugPerspectiveModel m_model = new CDebugPerspectiveModel(new MockGraphModel());

  private final CStackMemoryProvider m_stackModel = new CStackMemoryProvider();

  private final JStackView m_stackView = new JStackView(m_stackModel);

  private final CStackViewSynchronizer m_synchronizer =
      new CStackViewSynchronizer(m_stackView, m_stackModel, m_model);

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
        0, new TargetInformation(32, new FilledList<RegisterDescription>(),
            new DebuggerOptions(false,
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

    assertTrue(m_stackView.isEnabled());

    debugger.connection.m_synchronizer.receivedEvent(new DetachReply(0, 0));

    assertFalse(m_stackView.isEnabled());

    m_synchronizer.dispose();

    debugger.close();
  }

  /**
   * This test makes sure that freshly created threads have listeners attached.
   *
   * Test for 2035: Listener issue in the stack view synchronizer
   *
   * @throws DebugExceptionWrapper
   */
  @Test
  public void testNewThread() throws DebugExceptionWrapper {
    final MockDebugger debugger =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));

    debugger.connect();
    debugger.getProcessManager().setAttached(true);
    debugger.connection.m_synchronizer.receivedEvent(new TargetInformationReply(0,
        0, new TargetInformation(32, new FilledList<RegisterDescription>(),
            new DebuggerOptions(false,
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

    m_model.setActiveDebugger(debugger);

    final TargetProcessThread thread = new TargetProcessThread(1, ThreadState.RUNNING);

    debugger.getProcessManager().addThread(thread);

    m_synchronizer.dispose();

    debugger.close();
  }

  /**
   * This test makes sure that the stack view is filled properly when switching from no debugger to
   * a debugger with an active thread.
   *
   *  Fix for Case 2034: Stack view is not initialized correctly when opening graph windows with
   * connected debuggers
   *
   * @throws DebugExceptionWrapper
   */
  @Test
  public void testNewThread2() throws DebugExceptionWrapper {
    final MockDebugger debugger =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));

    debugger.connect();
    debugger.getProcessManager().setAttached(true);
    debugger.connection.m_synchronizer.receivedEvent(new TargetInformationReply(0,
        0, new TargetInformation(32, new FilledList<RegisterDescription>(),
            new DebuggerOptions(false,
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

    thread.setRegisterValues(Lists.newArrayList(
        new RegisterValue("esp", BigInteger.valueOf(0x110), new byte[0], false, true)));

    debugger.getProcessManager().addThread(thread);
    debugger.getProcessManager().setActiveThread(thread);

    m_model.setActiveDebugger(debugger);

    assertEquals(0x110, m_stackModel.getStackPointer());

    m_synchronizer.dispose();

    debugger.close();
  }

  @Test
  public void testSwitchDebugger() throws DebugExceptionWrapper {
    final MemorySection section1 = new MemorySection(new CAddress(0x100), new CAddress(0x1FF));
    final MemorySection section2 = new MemorySection(new CAddress(0x300), new CAddress(0x3FF));
    final MemoryMap memoryMap = new MemoryMap(Lists.newArrayList(section1, section2));

    final MockDebugger debugger =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));

    final TargetProcessThread thread = new TargetProcessThread(0x666, ThreadState.RUNNING);
    final MemoryModule module = new MemoryModule("narf.exe", "C:\\zort\\narf.exe",
        new RelocatedAddress(new CAddress(0x1000)), 123345);

    debugger.connect();
    debugger.connection.m_synchronizer.receivedEvent(new TargetInformationReply(0,
        0, new TargetInformation(32, new FilledList<RegisterDescription>(),
            new DebuggerOptions(false,
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
        0, new TargetInformation(32, new FilledList<RegisterDescription>(),
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
    assertTrue(m_stackView.isEnabled());
    m_model.setActiveDebugger(debugger2);
    assertTrue(m_stackView.isEnabled());
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
        0, new TargetInformation(32, new FilledList<RegisterDescription>(),
            new DebuggerOptions(false,
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
    assertFalse(m_stackView.isEnabled());
    debugger.getProcessManager().addThread(thread);
    assertFalse(m_stackView.isEnabled());
    debugger.getProcessManager().setActiveThread(thread);
    assertTrue(m_stackView.isEnabled());
    debugger.getProcessManager().setActiveThread(null);
    assertFalse(m_stackView.isEnabled());
    debugger.getProcessManager().removeThread(thread);
    assertFalse(m_stackView.isEnabled());
    debugger.getProcessManager().setAttached(false);
    m_synchronizer.dispose();
    debugger.close();
  }
}
