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
package com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Debug.Debugger.MockDebugger;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.MockGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.TargetInformationReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.ModuleTargetSettings;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryMap;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerOptions;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterDescription;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.gui.JHexPanel.JHexView;
import com.google.security.zynamics.zylib.gui.JHexPanel.JHexView.AddressMode;
import com.google.security.zynamics.zylib.types.lists.FilledList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;

@RunWith(JUnit4.class)
public final class CMemoryViewerSynchronizerTest {
  private CMemoryViewerSynchronizer m_synchronizer;

  private MockDebugger m_debugger;

  private CDebugPerspectiveModel m_model;

  private final JHexView m_hexView = new JHexView();

  @Before
  public void setUp() {
    final IGraphModel graphModel = new MockGraphModel();

    m_model = new CDebugPerspectiveModel(graphModel);

    final CMemoryProvider provider = new CMemoryProvider();

    m_hexView.setData(provider);

    m_synchronizer = new CMemoryViewerSynchronizer(m_hexView, provider, m_model);

    m_debugger = new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));

    m_model.setActiveDebugger(m_debugger);
  }

  @After
  public void tearDown() {
    m_debugger.close();
  }

  @Test
  public void testActiveDebugger() {
    final IGraphModel graphModel = new MockGraphModel();

    final CDebugPerspectiveModel model = new CDebugPerspectiveModel(graphModel);

    final JHexView hexView = new JHexView();

    final CMemoryProvider provider = new CMemoryProvider();

    final MockDebugger debugger =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));

    model.setActiveDebugger(debugger);

    final CMemoryViewerSynchronizer synchronizer =
        new CMemoryViewerSynchronizer(hexView, provider, model);

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
    assertEquals(AddressMode.BIT32, hexView.getAddressMode());

    debugger.connection.m_synchronizer.receivedEvent(new TargetInformationReply(0,
        0,
        new TargetInformation(64, new FilledList<RegisterDescription>(), new DebuggerOptions(false,
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
    assertEquals(AddressMode.BIT64, hexView.getAddressMode());

    synchronizer.dispose();
  }

  @Test
  public void testLifeCycle() throws DebugExceptionWrapper {
    m_debugger.connect();
    m_debugger.getProcessManager().setAttached(true);

    m_debugger.getProcessManager().setAttached(true);

    final MockMemoryViewerSynchronizerListener listener =
        new MockMemoryViewerSynchronizerListener();

    m_synchronizer.addListener(listener);

    m_model.setActiveMemoryAddress(new CAddress(0x100), false);

    assertEquals("requestedUnsectionedAddress;", listener.events);

    m_debugger.getProcessManager().setMemoryMap(new MemoryMap(
        Lists.newArrayList(new MemorySection(new CAddress(0x100), new CAddress(0x1FF)))));

    assertEquals("requestedUnsectionedAddress;", listener.events);

    m_model.setActiveMemoryAddress(new CAddress(0x100), false);

    assertEquals(0x100, m_hexView.getCurrentOffset());
    assertEquals("requestedUnsectionedAddress;", listener.events);

    m_synchronizer.removeListener(listener);

    m_debugger.getProcessManager().setAttached(false);

    m_synchronizer.dispose();
  }

  /**
   * This test makes sure that the synchronizer can handle new threads.
   *
   * Fix for Case 2036: Listener issue in the memory viewer
   */
  @Test
  public void testNewThread() {
    final IGraphModel graphModel = new MockGraphModel();

    final CDebugPerspectiveModel model = new CDebugPerspectiveModel(graphModel);

    final JHexView hexView = new JHexView();

    final CMemoryProvider provider = new CMemoryProvider();

    model.setActiveDebugger(m_debugger);

    final TargetProcessThread thread = new TargetProcessThread(0, ThreadState.RUNNING);
    m_debugger.getProcessManager().addThread(thread);
    m_debugger.getProcessManager().setActiveThread(thread);

    final CMemoryViewerSynchronizer synchronizer =
        new CMemoryViewerSynchronizer(hexView, provider, model);

    synchronizer.dispose();
  }

  @Test
  public void testNoDebugger() {
    m_model.setActiveDebugger(m_debugger);

    m_debugger.connection.m_synchronizer.receivedEvent(new TargetInformationReply(0,
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
    assertEquals(AddressMode.BIT32, m_hexView.getAddressMode());

    m_debugger.connection.m_synchronizer.receivedEvent(new TargetInformationReply(0,
        0,
        new TargetInformation(64, new FilledList<RegisterDescription>(), new DebuggerOptions(false,
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
    assertEquals(AddressMode.BIT64, m_hexView.getAddressMode());

    m_synchronizer.dispose();
  }

  @Test
  public void testResizeSection() throws DebugExceptionWrapper {
    m_debugger.connect();
    m_debugger.getProcessManager().setAttached(true);

    m_debugger.getProcessManager().setMemoryMap(new MemoryMap(
        Lists.newArrayList(new MemorySection(new CAddress(0x100), new CAddress(0x1FF)))));

    m_model.setActiveMemoryAddress(new CAddress(0x100), false);

    assertEquals(0x100, m_hexView.getCurrentOffset());
    assertEquals(0x100, m_hexView.getData().getDataLength());

    m_debugger.getProcessManager().setMemoryMap(new MemoryMap(Lists.newArrayList(
        new MemorySection(new CAddress(0x100), new CAddress(0x1FF)),
        new MemorySection(new CAddress(0x500), new CAddress(0x5FF)))));

    assertEquals(0x100, m_hexView.getCurrentOffset());
    assertEquals(0x100, m_hexView.getData().getDataLength());

    m_debugger.getProcessManager().setMemoryMap(new MemoryMap(Lists.newArrayList(
        new MemorySection(new CAddress(0x0), new CAddress(0x2FF)),
        new MemorySection(new CAddress(0x500), new CAddress(0x5FF)))));

    assertEquals(0x100, m_hexView.getCurrentOffset());
    assertEquals(0x300, m_hexView.getData().getDataLength());

    final MockMemoryViewerSynchronizerListener listener =
        new MockMemoryViewerSynchronizerListener();

    m_synchronizer.addListener(listener);

    m_debugger.getProcessManager().setMemoryMap(new MemoryMap(Lists.newArrayList(
        new MemorySection(new CAddress(0x0), new CAddress(0xFF)),
        new MemorySection(new CAddress(0x500), new CAddress(0x5FF)))));

    assertEquals(0x000, m_hexView.getCurrentOffset());
    assertEquals(0x100, m_hexView.getData().getDataLength());
    assertEquals("addressTurnedInvalid;", listener.events);

    m_synchronizer.removeListener(listener);

    m_debugger.getProcessManager().setAttached(false);

    m_synchronizer.dispose();
  }

  @Test
  public void testSwitchDebuggers() throws DebugExceptionWrapper {
    m_debugger.connect();
    m_debugger.getProcessManager().setAttached(true);

    m_debugger.getProcessManager().setMemoryMap(new MemoryMap(
        Lists.newArrayList(new MemorySection(new CAddress(0x100), new CAddress(0x1FF)))));

    m_model.setActiveMemoryAddress(new CAddress(0x100), false);

    assertEquals(0x100, m_hexView.getCurrentOffset());
    assertEquals(0x100, m_hexView.getData().getDataLength());

    final IDebugger debugger2 =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));

    debugger2.connect();

    debugger2.getProcessManager().setMemoryMap(new MemoryMap(
        Lists.newArrayList(new MemorySection(new CAddress(0x10), new CAddress(0x50)))));

    m_model.setActiveDebugger(debugger2);

    assertEquals(0x10, m_hexView.getCurrentOffset());
    assertEquals(0x41, m_hexView.getData().getDataLength());

    m_debugger.getProcessManager().setAttached(false);

    m_synchronizer.dispose();

    debugger2.close();
  }

  @Test
  public void testSwitchSection() throws DebugExceptionWrapper {
    m_debugger.connect();
    m_debugger.getProcessManager().setAttached(true);

    m_debugger.getProcessManager().setMemoryMap(new MemoryMap(Lists.newArrayList(
        new MemorySection(new CAddress(0x100), new CAddress(0x1FF)),
        new MemorySection(new CAddress(0x200), new CAddress(0x4FF)))));

    m_model.setActiveMemoryAddress(new CAddress(0x400), false);

    assertEquals(0x400, m_hexView.getCurrentOffset());
    assertEquals(0x300, m_hexView.getData().getDataLength());

    m_model.setActiveMemoryAddress(new CAddress(0x100), false);

    assertEquals(0x100, m_hexView.getCurrentOffset());
    assertEquals(0x100, m_hexView.getData().getDataLength());

    m_debugger.getProcessManager().setAttached(false);

    m_synchronizer.dispose();
  }

  @Test
  public void testThreads() throws DebugExceptionWrapper {
    m_debugger.connect();
    m_debugger.getProcessManager().setAttached(true);

    final MockMemoryViewerSynchronizerListener listener =
        new MockMemoryViewerSynchronizerListener();

    m_synchronizer.addListener(listener);

    final TargetProcessThread thread = new TargetProcessThread(1, ThreadState.RUNNING);

    m_debugger.getProcessManager().addThread(thread);

    m_debugger.getProcessManager().setActiveThread(thread);

    thread.setState(ThreadState.SUSPENDED);

    thread.setState(ThreadState.RUNNING);

    final MockDebugger debugger2 =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));

    m_model.setActiveDebugger(debugger2);

    m_model.setActiveDebugger(m_debugger);

    m_debugger.getProcessManager().removeThread(thread);

    m_debugger.getProcessManager().setAttached(false);

    m_synchronizer.removeListener(listener);

    m_synchronizer.dispose();
  }
}
