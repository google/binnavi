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
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.ModuleTargetSettings;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryMap;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerOptions;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterDescription;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValue;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.gui.JStackView.AddressMode;
import com.google.security.zynamics.zylib.types.lists.FilledList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;
import java.util.ArrayList;

@RunWith(JUnit4.class)
public final class CStackMemoryProviderTest {
  private MockDebugger m_debugger;

  private final CStackMemoryProvider m_provider = new CStackMemoryProvider();

  @Before
  public void setUp() {
    m_debugger = new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));
  }

  @After
  public void tearDown() {
    m_debugger.close();
  }

  @Test
  public void testAddressMode() throws DebugExceptionWrapper {
    m_provider.setDebugger(m_debugger);
    m_debugger.connect();

    final TargetProcessThread thread = new TargetProcessThread(0, ThreadState.SUSPENDED);

    thread.setRegisterValues(Lists.newArrayList(
        new RegisterValue("esp", BigInteger.valueOf(0x123), new byte[0], false, true)));

    m_provider.setActiveThread(thread);

    m_debugger.getProcessManager().setMemoryMap(new MemoryMap(
        Lists.newArrayList(new MemorySection(new CAddress(0x100), new CAddress(0x180)))));
    m_debugger.getProcessManager().getMemory()
        .store(0x120, new byte[] {0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});

    // Bytes + 32
    assertEquals("01000000", m_provider.getElement(0x120));

    m_provider.setDataLayout(StackDataLayout.Dwords);

    // Dwords + 32
    assertEquals("00000001", m_provider.getElement(0x120));

    m_provider.setAddressMode(AddressMode.BIT64);

    // Dwords + 64
    assertEquals("0000000000000001", m_provider.getElement(0x120));

    m_provider.setDataLayout(StackDataLayout.Bytes);

    // Bytes + 64
    assertEquals("0100000000000000", m_provider.getElement(0x120));
  }

  @Test
  public void testElement() throws DebugExceptionWrapper {
    m_provider.setDebugger(m_debugger);
    m_debugger.connect();

    final TargetProcessThread thread = new TargetProcessThread(0, ThreadState.SUSPENDED);

    thread.setRegisterValues(Lists.newArrayList(
        new RegisterValue("esp", BigInteger.valueOf(0x123), new byte[0], false, true)));

    m_provider.setActiveThread(thread);

    m_debugger.getProcessManager().setMemoryMap(new MemoryMap(
        Lists.newArrayList(new MemorySection(new CAddress(0x100), new CAddress(0x180)))));
    m_debugger.getProcessManager().getMemory().store(0x100,
        new byte[] {0x12, 0x34, 0x56, 0x78, (byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF0});

    assertEquals("12345678", m_provider.getElement(0x100));

    m_provider.setDataLayout(StackDataLayout.Dwords);

    assertEquals("78563412", m_provider.getElement(0x100));
  }

  @Test
  public void testInitial() throws DebugExceptionWrapper {
    m_provider.setDebugger(m_debugger);

    assertEquals(0, m_provider.getNumberOfEntries());
    assertFalse(m_provider.keepTrying());
    assertEquals(-1, m_provider.getStartAddress());
    assertEquals(-1, m_provider.getStackPointer());

    m_debugger.connect();

    assertEquals(0, m_provider.getNumberOfEntries());
    assertTrue(m_provider.keepTrying());
  }

  @Test
  public void testNumberOfEntries() throws DebugExceptionWrapper {
    m_provider.setDebugger(m_debugger);

    m_debugger.getProcessManager().setTargetInformation(new TargetInformation(5,
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
            12,
            0,
            new ArrayList<DebuggerException>(),
            false,
            false,
            false)));

    assertEquals(0, m_provider.getNumberOfEntries());

    m_debugger.connect();

    assertEquals(0, m_provider.getNumberOfEntries());

    final TargetProcessThread thread = new TargetProcessThread(0, ThreadState.SUSPENDED);

    thread.setRegisterValues(Lists.newArrayList(
        new RegisterValue("esp", BigInteger.valueOf(0x123), new byte[0], false, true)));

    m_provider.setActiveThread(thread);

    assertEquals(0, m_provider.getNumberOfEntries());

    m_debugger.getProcessManager().setMemoryMap(new MemoryMap(
        Lists.newArrayList(new MemorySection(new CAddress(0x100), new CAddress(0x180)))));

    assertEquals(0x80 / 0x04, m_provider.getNumberOfEntries());
  }

  @Test
  public void testStackPointer() throws DebugExceptionWrapper {
    m_provider.setDebugger(m_debugger);

    assertEquals(-1, m_provider.getStackPointer());

    m_debugger.connect();

    assertEquals(-1, m_provider.getStackPointer());

    final TargetProcessThread thread = new TargetProcessThread(0, ThreadState.SUSPENDED);

    thread.setRegisterValues(Lists.newArrayList(
        new RegisterValue("esp", BigInteger.valueOf(0x123), new byte[0], false, true)));

    m_provider.setActiveThread(thread);

    assertEquals(0x123, m_provider.getStackPointer());
  }

  @Test
  public void testStartAddress() throws DebugExceptionWrapper {
    m_provider.setDebugger(m_debugger);
    m_debugger.getProcessManager().setMemoryMap(new MemoryMap(
        Lists.newArrayList(new MemorySection(new CAddress(0x100), new CAddress(0x200)))));

    assertEquals(-1, m_provider.getStartAddress());

    m_debugger.connect();

    assertEquals(-1, m_provider.getStartAddress());

    final TargetProcessThread thread = new TargetProcessThread(0, ThreadState.SUSPENDED);

    thread.setRegisterValues(Lists.newArrayList(
        new RegisterValue("esp", BigInteger.valueOf(0x123), new byte[0], false, true)));

    assertEquals(-1, m_provider.getStartAddress());

    m_provider.setActiveThread(thread);

    assertEquals(0x100, m_provider.getStartAddress());
  }
}
