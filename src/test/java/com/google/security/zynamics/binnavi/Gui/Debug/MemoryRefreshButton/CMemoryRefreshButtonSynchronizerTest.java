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
package com.google.security.zynamics.binnavi.Gui.Debug.MemoryRefreshButton;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Debug.Debugger.MockDebugger;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.MockGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.DetachReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ProcessStartReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.TargetInformationReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.ModuleTargetSettings;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessStart;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerOptions;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterDescription;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.types.lists.FilledList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;

@RunWith(JUnit4.class)
public final class CMemoryRefreshButtonSynchronizerTest {
  private final CMemoryRefreshButton m_refreshButton = new CMemoryRefreshButton();

  private final IGraphModel m_graphModel = new MockGraphModel();

  private final CDebugPerspectiveModel m_model = new CDebugPerspectiveModel(m_graphModel);

  @SuppressWarnings("unused")
  private final IRefreshRangeProvider m_rangeProvider = new IRefreshRangeProvider() {
    @Override
    public IAddress getAddress() {
      return new CAddress(0x200);
    }

    @Override
    public int getSize() {
      return 100;
    }
  };

  private final AbstractAction m_defaultAction = new AbstractAction() {
    @Override
    public void actionPerformed(final ActionEvent e) {}
  };

  private final AbstractAction m_askAction = new AbstractAction() {
    @Override
    public void actionPerformed(final ActionEvent e) {}
  };

  private final CMemoryRefreshButtonSynchronizer m_synchronizer =
      new CMemoryRefreshButtonSynchronizer(m_refreshButton, m_model, m_defaultAction, m_askAction);

  @Test
  public void testDetach() throws DebugExceptionWrapper {
    final MockDebugger debugger =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));

    debugger.connect();

    final TargetProcessThread thread = new TargetProcessThread(0, ThreadState.SUSPENDED);
    debugger.getProcessManager().addThread(thread);

    m_model.setActiveDebugger(debugger);
    debugger.getProcessManager().setActiveThread(thread);

    debugger.connection.m_synchronizer.receivedEvent(new TargetInformationReply(0,
        0,
        new TargetInformation(32, new FilledList<RegisterDescription>(), new DebuggerOptions(false,
            false,
            false,
            false,
            false,
            true,
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

    assertEquals(thread, debugger.getProcessManager().getActiveThread());
    assertTrue(m_refreshButton.isEnabled());
    assertEquals(m_defaultAction, m_refreshButton.getAction());

    debugger.connection.m_synchronizer.receivedEvent(new DetachReply(0, 0));

    assertFalse(m_refreshButton.isEnabled());

    m_synchronizer.dispose();

    debugger.close();
  }

  @Test
  public void testReceiveTargetInformation() throws DebugExceptionWrapper {
    final MockDebugger debugger =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));

    debugger.connect();

    m_model.setActiveDebugger(debugger);

    debugger.connection.m_synchronizer.receivedEvent(new TargetInformationReply(0,
        0,
        new TargetInformation(32, new FilledList<RegisterDescription>(), new DebuggerOptions(false,
            false,
            false,
            false,
            false,
            true,
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

    assertFalse(m_refreshButton.isEnabled());
    assertEquals(m_defaultAction, m_refreshButton.getAction());

    debugger.connection.m_synchronizer.receivedEvent(new TargetInformationReply(0,
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

    assertFalse(m_refreshButton.isEnabled());
    assertEquals(m_askAction, m_refreshButton.getAction());

    m_synchronizer.dispose();

    debugger.close();
  }

  @Test
  public void testSwitchDebugger() throws DebugExceptionWrapper {
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
            false,
            false,
            true,
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
    debugger2.connection.m_synchronizer.receivedEvent(
        new ProcessStartReply(0, 0, new ProcessStart(thread, module)));

    m_model.setActiveDebugger(debugger);
    debugger.getProcessManager().setActiveThread(thread);
    assertTrue(m_refreshButton.isEnabled());
    assertEquals(m_defaultAction, m_refreshButton.getAction());

    m_model.setActiveDebugger(debugger2);
    debugger2.getProcessManager().setActiveThread(thread);
    assertTrue(m_refreshButton.isEnabled());
    assertEquals(m_askAction, m_refreshButton.getAction());

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

    final TargetProcessThread thread = new TargetProcessThread(1, ThreadState.RUNNING);

    m_model.setActiveDebugger(debugger);
    debugger.getProcessManager().setActiveThread(null);

    assertFalse(m_refreshButton.isEnabled());

    debugger.getProcessManager().addThread(thread);

    assertFalse(m_refreshButton.isEnabled());

    debugger.getProcessManager().setActiveThread(thread);

    assertTrue(m_refreshButton.isEnabled());

    debugger.getProcessManager().setActiveThread(null);

    assertFalse(m_refreshButton.isEnabled());

    debugger.getProcessManager().removeThread(thread);

    assertFalse(m_refreshButton.isEnabled());

    debugger.getProcessManager().setAttached(false);

    m_synchronizer.dispose();

    debugger.close();
  }
}
