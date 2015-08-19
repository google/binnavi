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
package com.google.security.zynamics.binnavi.Debug.Debugger.Synchronizers;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Debug.Debugger.DebuggerMessageBuilder;
import com.google.security.zynamics.binnavi.Debug.Debugger.MockDebugger;
import com.google.security.zynamics.binnavi.Debug.Debugger.MockEventListener;
import com.google.security.zynamics.binnavi.debug.debugger.AbstractDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.ModuleTargetSettings;
import com.google.security.zynamics.binnavi.debug.debugger.synchronizers.DebuggerSynchronizer;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.reflect.Field;

@RunWith(JUnit4.class)
public final class CBreakpointSynchronizerTest {
  private final MockModule mockModule = CommonTestObjects.MODULE;

  private final CAddress mockFileBase = new CAddress(0);
  private final RelocatedAddress mockImageBase = new RelocatedAddress(new CAddress(0x1000));
  private static final long mockImageSize = 0x10000;
  private final MemoryModule mockMemoryModule = new MemoryModule("Mock Module",
      "C:\\mockmodule.exe", mockImageBase, mockImageSize);

  private MockDebugger m_debugger;
  private BreakpointManager m_breakpointManager;

  private DebuggerSynchronizer m_synchronizer;

  private final MockEventListener m_listener = new MockEventListener();

  @Before
  public void setUp() throws SecurityException, NoSuchFieldException, IllegalArgumentException,
      IllegalAccessException {
    m_debugger = new MockDebugger(new ModuleTargetSettings(mockModule));
    m_debugger.setAddressTranslator(mockModule, mockFileBase, mockImageBase.getAddress());

    m_breakpointManager = m_debugger.getBreakpointManager();

    m_synchronizer = new DebuggerSynchronizer(m_debugger);

    m_synchronizer.addListener(m_listener);

    // assign the synchronizer to the internally used one by the debugger so we can test the
    // synchronizer itself
    // as well as the combination of synchronizer and debugger
    final Field synchronizerField = AbstractDebugger.class.getDeclaredField("synchronizer");
    synchronizerField.setAccessible(true);
    synchronizerField.set(m_debugger, m_synchronizer);

  }

  @Test
  public void testSetRegularBreakpoint() throws DebugExceptionWrapper {
    m_debugger.connect();

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildProcessStartReply(mockMemoryModule));

    m_breakpointManager
        .addBreakpoints(BreakpointType.REGULAR, CommonTestObjects.BP_ADDRESS_123_SET);

    assertEquals("CONNECT;READREGS;RESUME;SET_BREAKPOINTS/00000123/REGULAR;", m_debugger.requests);
  }

  @Test
  public void testSetStepBreakpoint() throws DebugExceptionWrapper {
    m_debugger.connect();

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildProcessStartReply(mockMemoryModule));

    m_breakpointManager.addBreakpoints(BreakpointType.STEP, CommonTestObjects.BP_ADDRESS_123_SET);

    assertEquals("CONNECT;READREGS;RESUME;SET_BREAKPOINTS/00000123/STEP;", m_debugger.requests);
  }
}
