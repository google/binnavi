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
package com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Implementations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Debug.Debugger.MockDebugger;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Implementations.CDumpAllWaiter;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.DetachReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ReadMemoryReply;
import com.google.security.zynamics.binnavi.debug.debugger.ModuleTargetSettings;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class CDumpAllWaiterTest {
  @Test
  public void testCloseRequested() throws Exception {
    final MockDebugger debugger =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));

    debugger.connect();

    final CDumpAllWaiter waiter = new CDumpAllWaiter(debugger, new CAddress(0x300), 0x100);

    waiter.start();

    new Thread() {
      @Override
      public void run() {
        try {
          waiter.runExpensiveCommand();
        } catch (final Exception e) {
        }
      }
    }.start();

    Thread.sleep(250);

    waiter.closeRequested();

    Thread.sleep(250);

    debugger.close();

    assertFalse(waiter.isAlive());
    assertTrue(waiter.success());
  }

  @Test
  public void testDetach() throws Exception {
    final MockDebugger debugger =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));

    debugger.connect();

    final CDumpAllWaiter waiter = new CDumpAllWaiter(debugger, new CAddress(0x300), 0x100);

    waiter.start();

    new Thread() {
      @Override
      public void run() {
        try {
          waiter.runExpensiveCommand();
        } catch (final Exception e) {
        }
      }
    }.start();

    Thread.sleep(250);

    debugger.connection.m_synchronizer.receivedEvent(new DetachReply(0, 0));

    Thread.sleep(250);

    assertFalse(waiter.isAlive());
    assertTrue(waiter.success());

    debugger.close();
  }

  @Test
  public void testFailure() throws Exception {
    final MockDebugger debugger =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));

    debugger.connect();

    final CDumpAllWaiter waiter = new CDumpAllWaiter(debugger, new CAddress(0x300), 0x100);

    waiter.start();

    new Thread() {
      @Override
      public void run() {
        try {
          waiter.runExpensiveCommand();
        } catch (final Exception e) {
        }
      }
    }.start();

    Thread.sleep(250);

    debugger.connection.m_synchronizer.receivedEvent(new ReadMemoryReply(0, 3, null, null));

    Thread.sleep(250);

    assertFalse(waiter.isAlive());
    assertFalse(waiter.success());

    debugger.close();
  }

  @Test
  public void testSuccess() throws Exception {
    final MockDebugger debugger =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));

    debugger.connect();

    final CDumpAllWaiter waiter = new CDumpAllWaiter(debugger, new CAddress(0x300), 0x100);

    waiter.start();

    new Thread() {
      @Override
      public void run() {
        try {
          waiter.runExpensiveCommand();
        } catch (final Exception e) {
        }
      }
    }.start();

    Thread.sleep(250);

    debugger.connection.m_synchronizer.receivedEvent(new ReadMemoryReply(0, 0, new CAddress(
        0x300), new byte[] {0x50, 0x50, 0x50, 0x50}));

    Thread.sleep(250);

    assertFalse(waiter.isAlive());
    assertTrue(waiter.success());

    debugger.close();
  }
}
