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
package com.google.security.zynamics.binnavi.API.debug;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValue;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;

@RunWith(JUnit4.class)
public final class ThreadTest {
  private final TargetProcessThread m_internalThread = new TargetProcessThread(0,
      com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState.RUNNING);

  private final Thread m_thread = new Thread(m_internalThread);

  @Test
  public void testConstructor() {
    assertEquals(0, m_thread.getThreadId());
    assertEquals(null, m_thread.getCurrentAddress());
    assertEquals(0, m_thread.getRegisters().size());
    assertEquals(ThreadState.Running, m_thread.getState());

    assertEquals("Thread (TID: 0)", m_thread.toString());
  }

  @Test
  public void testGetCurrentAddress() {
    final MockThreadListener listener = new MockThreadListener();

    m_thread.addListener(listener);

    m_internalThread.setCurrentAddress(new RelocatedAddress(new CAddress(0x200)));

    assertEquals("changedProgramCounter;", listener.events);
    assertEquals(0x200, m_thread.getCurrentAddress().toLong());

    m_thread.removeListener(listener);
  }

  @Test
  public void testGetRegisters() {
    final MockThreadListener listener = new MockThreadListener();

    m_thread.addListener(listener);

    m_internalThread.setRegisterValues(Lists.newArrayList(new RegisterValue("eax", BigInteger.TEN,
        new byte[0], false, false)));

    assertEquals("changedRegisters;", listener.events);
    assertEquals(1, m_thread.getRegisters().size());

    m_thread.removeListener(listener);
  }

  @Test
  public void testGetState() {
    final MockThreadListener listener = new MockThreadListener();

    m_thread.addListener(listener);

    m_internalThread.setState(com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState.SUSPENDED);

    assertEquals("changedState;", listener.events);
    assertEquals(ThreadState.Suspended, m_thread.getState());

    m_thread.removeListener(listener);
  }
}
