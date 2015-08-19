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
package com.google.security.zynamics.binnavi.Debug.Debugger;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.debug.connection.packets.parsers.MessageParserException;
import com.google.security.zynamics.binnavi.debug.debugger.AbstractDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.ModuleTargetSettings;
import com.google.security.zynamics.binnavi.debug.debugger.synchronizers.DebuggerSynchronizer;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.reflect.Field;

@RunWith(JUnit4.class)
public final class BreakpointLifecycleTest {
  private final CAddress mockFileBase = new CAddress(0);
  private final RelocatedAddress mockImageBase = new RelocatedAddress(new CAddress(0x1000));
  private static final long mockImageSize = 0x10000;
  private final MemoryModule mockMemoryModule =
      new MemoryModule("Mock Module", "C:\\mockmodule.exe", mockImageBase, mockImageSize);

  private MockDebugger m_debugger;
  private BreakpointManager m_breakpointManager;

  private DebuggerSynchronizer m_synchronizer;

  private final MockEventListener m_listener = new MockEventListener();

  @Before
  public void setUp() throws SecurityException, NoSuchFieldException, IllegalArgumentException,
      IllegalAccessException {
    m_debugger = new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));
    m_debugger.setAddressTranslator(CommonTestObjects.MODULE, mockFileBase,
        mockImageBase.getAddress());

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

  @After
  public void tearDown() {
    m_debugger.close();
  }

  @Test
  public void testActiveToDeleting() throws DebugExceptionWrapper {
    // Scenario:
    //
    // 1. User sets breakpoint
    // 2. Debug clients sets breakpoint in the target process
    // 3. User deletes breakpoint
    // 4. Debug client removes breakpoint
    //
    // Expected result:
    // - Breakpoint is removed from the target process

    m_debugger.connect();

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildProcessStartReply(mockMemoryModule));

    m_breakpointManager.addBreakpoints(BreakpointType.REGULAR,
        CommonTestObjects.BP_ADDRESS_456_SET);

    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    assertEquals("CONNECT;READREGS;RESUME;SET_BREAKPOINTS/00000456/REGULAR;", m_debugger.requests);

    m_synchronizer.receivedEvent(
        DebuggerMessageBuilder.buildBreakpointSuccess(CommonTestObjects.BP_ADDRESS_456_RELOC));

    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    m_breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DELETING);

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildRegularBreakpointRemoveSucc(
        CommonTestObjects.BP_ADDRESS_456_RELOC));

    assertEquals(0, m_breakpointManager.getNumberOfBreakpoints(BreakpointType.REGULAR));

    assertEquals(
        "CONNECT;READREGS;RESUME;SET_BREAKPOINTS/00000456/REGULAR;REMOVE_BREAKPOINTS/00000456/REGULAR;",
        m_debugger.requests);
  }

  @Test
  public void testActiveToDisabled() throws DebugExceptionWrapper {
    // Scenario:
    //
    // 1. User sets breakpoint
    // 2. Debug clients sets breakpoint in the target process
    // 3. User disables breakpoint
    // 4. Debug client removes breakpoint
    //
    // Expected result:
    // - Breakpoint is removed from the target process
    // - Final breakpoint status DISABLED

    m_debugger.connect();

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildProcessStartReply(mockMemoryModule));

    m_breakpointManager.addBreakpoints(BreakpointType.REGULAR,
        CommonTestObjects.BP_ADDRESS_456_SET);

    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    assertEquals("CONNECT;READREGS;RESUME;SET_BREAKPOINTS/00000456/REGULAR;", m_debugger.requests);

    m_synchronizer.receivedEvent(
        DebuggerMessageBuilder.buildBreakpointSuccess(CommonTestObjects.BP_ADDRESS_456_RELOC));

    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    m_breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DISABLED);

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildRegularBreakpointRemoveSucc(
        CommonTestObjects.BP_ADDRESS_456_RELOC));

    assertEquals(1, m_breakpointManager.getNumberOfBreakpoints(BreakpointType.REGULAR));

    assertEquals(BreakpointStatus.BREAKPOINT_DISABLED, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    assertEquals(
        "CONNECT;READREGS;RESUME;SET_BREAKPOINTS/00000456/REGULAR;REMOVE_BREAKPOINTS/00000456/REGULAR;",
        m_debugger.requests);
  }

  @Test
  @Ignore
  public void testActiveToEnabled() {
    // Not possible; breakpoint lifecycle would go backwards
  }

  @Test
  public void testActiveToHit() throws DebugExceptionWrapper, MessageParserException {
    // Scenario:
    //
    // 1. User sets breakpoint
    // 2. Debug clients sets breakpoint in the target process
    // 3. Breakpoint is hit
    //
    // Expected result:
    // - Final breakpoint status HIT

    m_debugger.connect();

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildProcessStartReply(mockMemoryModule));

    m_breakpointManager.addBreakpoints(BreakpointType.REGULAR,
        CommonTestObjects.BP_ADDRESS_456_SET);

    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    assertEquals("CONNECT;READREGS;RESUME;SET_BREAKPOINTS/00000456/REGULAR;", m_debugger.requests);

    m_synchronizer.receivedEvent(
        DebuggerMessageBuilder.buildBreakpointSuccess(CommonTestObjects.BP_ADDRESS_456_RELOC));

    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    m_synchronizer.receivedEvent(
        DebuggerMessageBuilder.buildRegularBreakpointHit(CommonTestObjects.BP_ADDRESS_456_RELOC));

    assertEquals(BreakpointStatus.BREAKPOINT_HIT, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    assertEquals("CONNECT;READREGS;RESUME;SET_BREAKPOINTS/00000456/REGULAR;", m_debugger.requests);
  }

  @Test
  @Ignore
  public void testActiveToInactive() {
    // Not possible; the correct way is to through either DELETING or DISABLED
  }

  @Test
  @Ignore
  public void testActiveToInvalid() {
    // Not possible; active breakpoints can not suddenly go invalid
  }

  @Test
  public void testDeletingToDisabled() throws DebugExceptionWrapper {
    // Scenario:
    //
    // 1. User sets breakpoint
    // 2. Debug client sets breakpoint
    // 3. User deletes breakpoint
    // 4. User disables breakpoints
    //
    // Expected result:
    // - Not possible; exception

    m_debugger.connect();

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildProcessStartReply(mockMemoryModule));

    // 1. User sets breakpoint
    m_breakpointManager.addBreakpoints(BreakpointType.REGULAR,
        CommonTestObjects.BP_ADDRESS_456_SET);

    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    assertEquals("CONNECT;READREGS;RESUME;SET_BREAKPOINTS/00000456/REGULAR;", m_debugger.requests);

    // 2. Debug client sets breakpoint
    m_synchronizer.receivedEvent(
        DebuggerMessageBuilder.buildBreakpointSuccess(CommonTestObjects.BP_ADDRESS_456_RELOC));

    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    // 3. User deletes breakpoint
    m_breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DELETING);

    // assertEquals(BreakpointStatus.BREAKPOINT_DELETING,
    // m_breakpointManager.getBreakpointStatus(CommonTestObjects.BP_ADDRESS_456,
    // BreakpointType.REGULAR));

    // try
    // {
    // // 4. User disables breakpoints
    // m_breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
    // BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DISABLED);
    // fail();
    // }
    // catch (final IllegalStateException exception)
    // {
    // }

    assertEquals(
        "CONNECT;READREGS;RESUME;SET_BREAKPOINTS/00000456/REGULAR;REMOVE_BREAKPOINTS/00000456/REGULAR;",
        m_debugger.requests);

    // assertEquals(BreakpointStatus.BREAKPOINT_DISABLED,
    // m_breakpointManager.getBreakpoint(BreakpointType.REGULAR, CommonTestObjects.BP_ADDRESS_456));
    //
    // // 5. Debug client deletes breakpoint
    // m_synchronizer.handleBreakpointRemoveSucc(DebuggerMessageBuilder.buildEchoBreakpointRemoveSucc(CommonTestObjects.BP_ADDRESS_456_RELOC));
    //
    // assertEquals(0, m_breakpointManager.getNumberOfBreakpoints(BreakpointType.REGULAR));
  }

  @Test
  public void testDisabledToDeleting() throws DebugExceptionWrapper {
    m_breakpointManager.addBreakpoints(BreakpointType.REGULAR,
        CommonTestObjects.BP_ADDRESS_456_SET);

    assertEquals(BreakpointStatus.BREAKPOINT_INACTIVE, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    m_breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DISABLED);

    m_breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DELETING);

    assertEquals(0, m_breakpointManager.getNumberOfBreakpoints(BreakpointType.REGULAR));

    m_debugger.connect();

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildProcessStartReply(mockMemoryModule));

    m_breakpointManager.addBreakpoints(BreakpointType.REGULAR,
        CommonTestObjects.BP_ADDRESS_456_SET);

    m_synchronizer.receivedEvent(
        DebuggerMessageBuilder.buildBreakpointSuccess(CommonTestObjects.BP_ADDRESS_456_RELOC));

    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    m_breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DISABLED);

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildRegularBreakpointRemoveSucc(
        CommonTestObjects.BP_ADDRESS_456_RELOC));

    m_breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DELETING);

    assertEquals(0, m_breakpointManager.getNumberOfBreakpoints(BreakpointType.REGULAR));

    assertEquals(
        "CONNECT;READREGS;RESUME;SET_BREAKPOINTS/00000456/REGULAR;REMOVE_BREAKPOINTS/00000456/REGULAR;",
        m_debugger.requests);
  }

  @Test
  public void testEnabledToActive() throws DebugExceptionWrapper {
    // Scenario:
    //
    // 1. User sets breakpoint
    // 2. SET reply arrives from the Debug Client
    //
    // Expected result:
    // - Final breakpoint status ACTIVE

    m_debugger.connect();

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildProcessStartReply(mockMemoryModule));

    m_breakpointManager.addBreakpoints(BreakpointType.REGULAR,
        CommonTestObjects.BP_ADDRESS_456_SET);

    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    m_synchronizer.receivedEvent(
        DebuggerMessageBuilder.buildBreakpointSuccess(CommonTestObjects.BP_ADDRESS_456_RELOC));

    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    assertEquals("CONNECT;READREGS;RESUME;SET_BREAKPOINTS/00000456/REGULAR;", m_debugger.requests);
  }

  @Test
  @Ignore
  public void testEnabledToDeleting() {
    // TODO: Unclear scenario
  }

  @Test
  public void testEnabledToDisabled() throws DebugExceptionWrapper {
    // Scenario:
    //
    // 1. User sets breakpoint (SET Message is sent to the Debug Client)
    // 2. User disables breakpoint (REMOVE Message is sent to the Debug Client)
    //
    // At this point there are two messages on the way to the debug client which
    // are guaranteed to come back in the order they were sent.
    //
    // Expected result:
    // - Final breakpoint status DISABLED

    m_debugger.connect();

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildProcessStartReply(mockMemoryModule));

    m_breakpointManager.addBreakpoints(BreakpointType.REGULAR,
        CommonTestObjects.BP_ADDRESS_456_SET);

    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    m_breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DISABLED);

    m_synchronizer.receivedEvent(
        DebuggerMessageBuilder.buildBreakpointSuccess(CommonTestObjects.BP_ADDRESS_456_RELOC));

    assertEquals(BreakpointStatus.BREAKPOINT_DISABLED, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildRegularBreakpointRemoveSucc(
        CommonTestObjects.BP_ADDRESS_456_RELOC));

    assertEquals(BreakpointStatus.BREAKPOINT_DISABLED, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    assertEquals(
        "CONNECT;READREGS;RESUME;SET_BREAKPOINTS/00000456/REGULAR;REMOVE_BREAKPOINTS/00000456/REGULAR;",
        m_debugger.requests);
  }

  @Test
  @Ignore
  public void testEnabledToHit() {
    // Not possible; must go through ACTIVE first
  }

  @Test
  @Ignore
  public void testEnabledToInactive() {
    // TODO: Scenario unclear
  }

  @Test
  public void testEnabledToInvalid() throws DebugExceptionWrapper {
    // Scenario:
    //
    // 1. User sets breakpoint
    // 2. Debug client fails to set breakpoint
    //
    // Expected result:
    //
    // - Final breakpoint status INVALID

    m_debugger.connect();

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildProcessStartReply(mockMemoryModule));

    m_breakpointManager.addBreakpoints(BreakpointType.REGULAR,
        CommonTestObjects.BP_ADDRESS_456_SET);

    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildRegularBreakpointSetError(
        CommonTestObjects.BP_ADDRESS_456_RELOC));

    assertEquals(BreakpointStatus.BREAKPOINT_INVALID, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    assertEquals("CONNECT;READREGS;RESUME;SET_BREAKPOINTS/00000456/REGULAR;", m_debugger.requests);
  }

  @Test
  public void testHitToDeleting() throws DebugExceptionWrapper, MessageParserException {
    // Scenario:
    //
    // 1. User sets breakpoint (SET Message is sent to the Debug Client)
    // 2. Debug client sets breakpoint
    // 3. Debug client hits breakpoint
    // 4. User deletes breakpoint (REMOVE Message is sent to the Debug Client)
    //
    // Expected result:
    // - Breakpoint is removed from the breakpoint manager

    m_debugger.connect();

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildProcessStartReply(mockMemoryModule));

    // 1. User sets breakpoint (SET Message is sent to the Debug Client)
    m_breakpointManager.addBreakpoints(BreakpointType.REGULAR,
        CommonTestObjects.BP_ADDRESS_456_SET);

    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    // 2. Debug client sets breakpoint
    m_synchronizer.receivedEvent(
        DebuggerMessageBuilder.buildBreakpointSuccess(CommonTestObjects.BP_ADDRESS_456_RELOC));

    // 3. Debug client hits breakpoint
    m_synchronizer.receivedEvent(
        DebuggerMessageBuilder.buildRegularBreakpointHit(CommonTestObjects.BP_ADDRESS_456_RELOC));

    assertEquals(BreakpointStatus.BREAKPOINT_HIT, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    // 4. User deletes breakpoint (REMOVE Message is sent to the Debug Client)
    m_breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DELETING);

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildRegularBreakpointRemoveSucc(
        CommonTestObjects.BP_ADDRESS_456_RELOC));

    assertEquals(0, m_breakpointManager.getNumberOfBreakpoints(BreakpointType.REGULAR));

    assertEquals(
        "CONNECT;READREGS;RESUME;SET_BREAKPOINTS/00000456/REGULAR;REMOVE_BREAKPOINTS/00000456/REGULAR;",
        m_debugger.requests);
  }

  @Test
  public void testHitToDisabled() throws DebugExceptionWrapper, MessageParserException {
    // Scenario:
    //
    // 1. User sets breakpoint (SET Message is sent to the Debug Client)
    // 2. Debug client sets breakpoint
    // 3. Debug client hits breakpoint
    // 4. User disables breakpoint (REMOVE Message is sent to the Debug Client)
    //
    // Expected result:
    // - Final breakpoint status DISABLED

    m_debugger.connect();

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildProcessStartReply(mockMemoryModule));

    // 1. User sets breakpoint (SET Message is sent to the Debug Client)
    m_breakpointManager.addBreakpoints(BreakpointType.REGULAR,
        CommonTestObjects.BP_ADDRESS_456_SET);

    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    // 2. Debug client sets breakpoint
    m_synchronizer.receivedEvent(
        DebuggerMessageBuilder.buildBreakpointSuccess(CommonTestObjects.BP_ADDRESS_456_RELOC));

    // 3. Debug client hits breakpoint
    m_synchronizer.receivedEvent(
        DebuggerMessageBuilder.buildRegularBreakpointHit(CommonTestObjects.BP_ADDRESS_456_RELOC));

    assertEquals(BreakpointStatus.BREAKPOINT_HIT, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    // 4. User disables breakpoint (REMOVE Message is sent to the Debug Client)
    m_breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DISABLED);

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildRegularBreakpointRemoveSucc(
        CommonTestObjects.BP_ADDRESS_456_RELOC));

    assertEquals(BreakpointStatus.BREAKPOINT_DISABLED, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    assertEquals(
        "CONNECT;READREGS;RESUME;SET_BREAKPOINTS/00000456/REGULAR;REMOVE_BREAKPOINTS/00000456/REGULAR;",
        m_debugger.requests);
  }

  @Test
  @Ignore
  public void testInactiveToActive() {
    // Not possible; must go through ENABLED first
  }

  @Test
  public void testInactiveToDeleting() {
    // Scenario:
    //
    // 1. Debugger is inactive
    // 2. User sets a breakpoint
    // 3. User removes breakpoint
    //
    // Expected result:
    // - Breakpoint is removed from the breakpoint manager

    m_breakpointManager.addBreakpoints(BreakpointType.REGULAR,
        CommonTestObjects.BP_ADDRESS_456_SET);

    assertEquals(BreakpointStatus.BREAKPOINT_INACTIVE, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    m_breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DELETING);

    assertEquals(0, m_breakpointManager.getNumberOfBreakpoints(BreakpointType.REGULAR));

    assertEquals("", m_debugger.requests);
  }

  @Test
  public void testInactiveToDisabled() {
    // Scenario:
    //
    // 1. Debugger is inactive
    // 2. User sets a breakpoint
    // 3. User disables breakpoint
    //
    // Expected result:
    // - Breakpoint is disabled

    m_breakpointManager.addBreakpoints(BreakpointType.REGULAR,
        CommonTestObjects.BP_ADDRESS_456_SET);

    assertEquals(BreakpointStatus.BREAKPOINT_INACTIVE, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    m_breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DISABLED);

    assertEquals(BreakpointStatus.BREAKPOINT_DISABLED, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    assertEquals("", m_debugger.requests);
  }

  @Test
  public void testInactiveToEnabled() throws DebugExceptionWrapper {
    // Scenario:
    //
    // 1. Debugger is inactive
    // 2. User sets a breakpoint
    // 3. Debugger is started
    // 4. Process start event
    //
    // Expected result:
    // - Final breakpoint status ENABLED

    m_breakpointManager.addBreakpoints(BreakpointType.REGULAR,
        CommonTestObjects.BP_ADDRESS_456_SET);

    assertEquals(BreakpointStatus.BREAKPOINT_INACTIVE, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    m_debugger.connect();

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildProcessStartReply(mockMemoryModule));

    assertEquals(BreakpointStatus.BREAKPOINT_ENABLED, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    assertEquals("CONNECT;READREGS;SET_BREAKPOINTS/00000456/REGULAR;RESUME;", m_debugger.requests);
  }

  @Test
  @Ignore
  public void testInactiveToHit() {
    // Not possible; must go through ENABLED first
  }

  @Test
  @Ignore
  public void testInactiveToInvalid() {
    // Not possible; must go through ENABLED first
  }

  @Test
  public void testInvalidToDeleting() throws DebugExceptionWrapper {
    // Scenario:
    //
    // 1. User sets a breakpoint
    // 2. Debug client fails to set breakpoint
    // 3. User removes breakpoint
    //
    // Expected result:
    // - Breakpoint is removed from the breakpoint manager

    m_debugger.connect();

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildProcessStartReply(mockMemoryModule));

    m_breakpointManager.addBreakpoints(BreakpointType.REGULAR,
        CommonTestObjects.BP_ADDRESS_456_SET);

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildRegularBreakpointSetError(
        CommonTestObjects.BP_ADDRESS_456_RELOC));

    assertEquals(BreakpointStatus.BREAKPOINT_INVALID, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    m_breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DELETING);

    assertEquals(0, m_breakpointManager.getNumberOfBreakpoints(BreakpointType.REGULAR));

    assertEquals("CONNECT;READREGS;RESUME;SET_BREAKPOINTS/00000456/REGULAR;", m_debugger.requests);
  }

  @Test
  public void testInvalidToDisabled() throws DebugExceptionWrapper {
    // Scenario:
    //
    // 1. User sets a breakpoint
    // 2. Debug client fails to set breakpoint
    // 3. User disables breakpoint
    //
    // Expected result:
    // - Breakpoint is removed from the breakpoint manager

    m_debugger.connect();

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildProcessStartReply(mockMemoryModule));

    m_breakpointManager.addBreakpoints(BreakpointType.REGULAR,
        CommonTestObjects.BP_ADDRESS_456_SET);

    m_synchronizer.receivedEvent(DebuggerMessageBuilder.buildRegularBreakpointSetError(
        CommonTestObjects.BP_ADDRESS_456_RELOC));

    assertEquals(BreakpointStatus.BREAKPOINT_INVALID, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    m_breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DISABLED);

    assertEquals(BreakpointStatus.BREAKPOINT_DISABLED, m_breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    assertEquals("CONNECT;READREGS;RESUME;SET_BREAKPOINTS/00000456/REGULAR;", m_debugger.requests);
  }
}
