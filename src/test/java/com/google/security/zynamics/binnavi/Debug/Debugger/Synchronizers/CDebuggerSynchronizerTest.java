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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Debug.Debugger.DebuggerMessageBuilder;
import com.google.security.zynamics.binnavi.Debug.Debugger.MockDebugger;
import com.google.security.zynamics.binnavi.Debug.Debugger.MockEventListener;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.debug.connection.packets.parsers.MessageParserException;
import com.google.security.zynamics.binnavi.debug.connection.packets.parsers.RegisterValuesParser;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.AttachReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.AuthenticationFailedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.BreakpointSetReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.BreakpointsRemovedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.DebuggerClosedUnexpectedlyReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.DetachReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.EchoBreakpointSetReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.EchoBreakpointsRemovedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ExceptionOccurredReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.MemoryMapReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ModuleLoadedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ModuleUnloadedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ProcessClosedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ReadMemoryReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.RegistersReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ResumeReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ResumeThreadReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SetRegisterReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SingleStepReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.StepBreakpointHitReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.StepBreakpointSetReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SuspendThreadReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.TargetInformationReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.TerminateReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ThreadClosedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ThreadCreatedReply;
import com.google.security.zynamics.binnavi.debug.debugger.AbstractDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.ModuleTargetSettings;
import com.google.security.zynamics.binnavi.debug.debugger.synchronizers.DebuggerSynchronizer;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryMap;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerOptions;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterDescription;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValue;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValues;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.ThreadRegisters;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Test class for functions related to debugger synchronization.
 */
@RunWith(JUnit4.class)
public final class CDebuggerSynchronizerTest {
  private MockDebugger mockDebugger;
  private DebuggerSynchronizer debuggerSynchronizer;
  private BreakpointManager breakpointManager;
  private final MockEventListener listener = new MockEventListener();

  @Before
  public void setUp() throws SecurityException, NoSuchFieldException, IllegalArgumentException,
      IllegalAccessException {
    mockDebugger = new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));
    mockDebugger.setAddressTranslator(CommonTestObjects.MODULE, new CAddress(0),
        new CAddress(0x1000));

    breakpointManager = mockDebugger.getBreakpointManager();

    debuggerSynchronizer = new DebuggerSynchronizer(mockDebugger);

    debuggerSynchronizer.addListener(listener);

    // assign the synchronizer to the internally used one by the debugger so we can test the
    // synchronizer itself
    // as well as the combination of synchronizer and debugger
    final Field synchronizerField = AbstractDebugger.class.getDeclaredField("synchronizer");
    synchronizerField.setAccessible(true);
    synchronizerField.set(mockDebugger, debuggerSynchronizer);
  }

  @After
  public void tearDown() {
    mockDebugger.close();
  }

  @Test
  public void testAddBreakpointEcho() throws DebugExceptionWrapper {
    // It is not possible to set echo breakpoint in unconnected debuggers

    mockDebugger.connect();

    breakpointManager.addBreakpoints(BreakpointType.ECHO, CommonTestObjects.BP_ADDRESS_456_SET);

    // Immediately try to set the breakpoint in the target process if the debugger is active
    assertEquals(BreakpointStatus.BREAKPOINT_INACTIVE, breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.ECHO));
  }

  @Test
  public void testAddBreakpointRegular() throws DebugExceptionWrapper {
    breakpointManager.addBreakpoints(BreakpointType.REGULAR, CommonTestObjects.BP_ADDRESS_123_SET);

    // Nothing happens to new breakpoints when the debugger is not connected
    assertEquals(BreakpointStatus.BREAKPOINT_INACTIVE, breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_123, BreakpointType.REGULAR));

    mockDebugger.connect();

    debuggerSynchronizer.receivedEvent(
        DebuggerMessageBuilder.buildProcessStartReply(CommonTestObjects.MEMORY_MODULE));

    // Immediately try to set the breakpoint in the target process if the debugger is active
    breakpointManager.addBreakpoints(BreakpointType.REGULAR, CommonTestObjects.BP_ADDRESS_456_SET);

    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));
  }

  @Test
  public void testAddBreakpointStep() throws DebugExceptionWrapper {
    mockDebugger.connect();

    debuggerSynchronizer.receivedEvent(
        DebuggerMessageBuilder.buildProcessStartReply(CommonTestObjects.MEMORY_MODULE));

    // Immediately try to set the breakpoint in the target process if the debugger is active
    breakpointManager.addBreakpoints(BreakpointType.STEP, CommonTestObjects.BP_ADDRESS_456_SET);

    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.STEP));
  }

  @Test
  public void testAttachError() throws DebugExceptionWrapper {
    mockDebugger.connect();

    debuggerSynchronizer.receivedEvent(new AttachReply(0, 5));

    assertFalse(mockDebugger.getProcessManager().isAttached());

    assertEquals("ERROR_ATTACH/5;", listener.events);
  }

  @Test
  public void testAttachSuccess() throws DebugExceptionWrapper {
    // Connect the debugger

    mockDebugger.connect();

    assertTrue(mockDebugger.isConnected());

    debuggerSynchronizer.receivedEvent(new AttachReply(0, 0));

    assertTrue(mockDebugger.getProcessManager().isAttached());
  }

  /**
   * This test makes sure that authentication failure replies are handled correctly.
   *
   *  This message is sent if the client can not authenticate itself as a BinNavi debug client. In
   * that case BinNavi has to close the connection to the debug client and reset the internal state
   * of the process.
   *
   * @throws DebugExceptionWrapper Thrown if something goes wrong.
   */
  @Test
  public void testAuthenticationFailed() throws DebugExceptionWrapper {
    mockDebugger.connect();

    assertTrue(mockDebugger.isConnected());

    debuggerSynchronizer.receivedEvent(new AuthenticationFailedReply());

    assertFalse(mockDebugger.isConnected());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testBreakpointRemoveErr() {
    breakpointManager.addBreakpoints(BreakpointType.REGULAR, CommonTestObjects.BP_ADDRESS_123_SET);

    debuggerSynchronizer.receivedEvent(new BreakpointsRemovedReply(0, 0, Lists.newArrayList(
        new Pair<RelocatedAddress, Integer>(CommonTestObjects.BP_ADDRESS_123_RELOC, 5))));

    assertEquals(BreakpointStatus.BREAKPOINT_INVALID, breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_123, BreakpointType.REGULAR));

    assertEquals("ERROR_REMOVE_BREAKPOINTS/00001123/5;", listener.events);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testBreakpointRemoveSucc() throws DebugExceptionWrapper {
    mockDebugger.connect();

    breakpointManager.addBreakpoints(BreakpointType.REGULAR, CommonTestObjects.BP_ADDRESS_123_SET);

    debuggerSynchronizer.receivedEvent(new BreakpointSetReply(0, 0, Lists.newArrayList(
        new Pair<RelocatedAddress, Integer>(CommonTestObjects.BP_ADDRESS_123_RELOC, 0))));

    breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_123_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DELETING);

    debuggerSynchronizer.receivedEvent(new BreakpointsRemovedReply(0, 0, Lists.newArrayList(
        new Pair<RelocatedAddress, Integer>(CommonTestObjects.BP_ADDRESS_123_RELOC, 0))));

    assertEquals(0, breakpointManager.getNumberOfBreakpoints(BreakpointType.REGULAR));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testBreakpointSetErr() {
    breakpointManager.addBreakpoints(BreakpointType.REGULAR, CommonTestObjects.BP_ADDRESS_123_SET);

    debuggerSynchronizer.receivedEvent(new BreakpointSetReply(0, 0, Lists.newArrayList(
        new Pair<RelocatedAddress, Integer>(CommonTestObjects.BP_ADDRESS_123_RELOC, 5))));

    assertEquals(BreakpointStatus.BREAKPOINT_INVALID, breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_123, BreakpointType.REGULAR));

    assertEquals("ERROR_SET_BREAKPOINTS/00001123/5;", listener.events);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testBreakpointSetSucc() {
    breakpointManager.addBreakpoints(BreakpointType.REGULAR, CommonTestObjects.BP_ADDRESS_123_SET);

    debuggerSynchronizer.receivedEvent(new BreakpointSetReply(0, 0, Lists.newArrayList(
        new Pair<RelocatedAddress, Integer>(CommonTestObjects.BP_ADDRESS_123_RELOC, 0))));

    assertEquals(1, breakpointManager.getNumberOfBreakpoints(BreakpointType.REGULAR));
    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_123, BreakpointType.REGULAR));
  }

  @Test
  public void testDetachError() {
    debuggerSynchronizer.receivedEvent(new DetachReply(0, 5));

    assertFalse(mockDebugger.isConnected());
    assertEquals("ERROR_DETACH/5;", listener.events);
  }

  @Test
  public void testDetachSucc() throws DebugExceptionWrapper {
    mockDebugger.connect();

    debuggerSynchronizer.receivedEvent(new DetachReply(0, 0));

    assertFalse(mockDebugger.isConnected());
  }

  @Test
  public void testDisableBreakpointRegularDisabledDebugger() {
    breakpointManager.addBreakpoints(BreakpointType.REGULAR, CommonTestObjects.BP_ADDRESS_456_SET);

    assertEquals(BreakpointStatus.BREAKPOINT_INACTIVE, breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DISABLED);

    assertEquals("", mockDebugger.requests);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testEchoBreakpointRemoveSucc() throws DebugExceptionWrapper {
    mockDebugger.connect();

    breakpointManager.addBreakpoints(BreakpointType.ECHO, CommonTestObjects.BP_ADDRESS_123_SET);

    debuggerSynchronizer.receivedEvent(new EchoBreakpointsRemovedReply(0, 0, Lists.newArrayList(
        new Pair<RelocatedAddress, Integer>(CommonTestObjects.BP_ADDRESS_123_RELOC, 0))));

    assertEquals(0, breakpointManager.getNumberOfBreakpoints(BreakpointType.ECHO));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testEchoBreakpointSetErr() throws DebugExceptionWrapper {
    mockDebugger.connect();

    breakpointManager.addBreakpoints(BreakpointType.ECHO, CommonTestObjects.BP_ADDRESS_123_SET);

    debuggerSynchronizer.receivedEvent(new EchoBreakpointSetReply(0, 0, Lists.newArrayList(
        new Pair<RelocatedAddress, Integer>(CommonTestObjects.BP_ADDRESS_123_RELOC, 5))));

    assertEquals(0, breakpointManager.getNumberOfBreakpoints(BreakpointType.ECHO));

    assertEquals("ERROR_SET_ECHO_BREAKPOINT/00001123/5;", listener.events);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testEchoBreakpointSetSucc() throws DebugExceptionWrapper {
    mockDebugger.connect();

    breakpointManager.addBreakpoints(BreakpointType.ECHO, CommonTestObjects.BP_ADDRESS_123_SET);

    debuggerSynchronizer.receivedEvent(new EchoBreakpointSetReply(0, 0, Lists.newArrayList(
        new Pair<RelocatedAddress, Integer>(CommonTestObjects.BP_ADDRESS_123_RELOC, 0))));

    assertEquals(1, breakpointManager.getNumberOfBreakpoints(BreakpointType.ECHO));
    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_123, BreakpointType.ECHO));
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

    mockDebugger.connect();

    debuggerSynchronizer.receivedEvent(
        DebuggerMessageBuilder.buildProcessStartReply(CommonTestObjects.MEMORY_MODULE));

    breakpointManager.addBreakpoints(BreakpointType.REGULAR, CommonTestObjects.BP_ADDRESS_456_SET);

    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));

    breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DISABLED);

    // If the debugger is active, the breakpoint is removed from the target process
    assertEquals("CONNECT;READREGS;RESUME;SET_BREAKPOINTS/00000456/REGULAR;"
        + "REMOVE_BREAKPOINTS/00000456/REGULAR;", mockDebugger.requests);
  }

  @Test
  public void testErrorConnectionClosed() throws DebugExceptionWrapper {
    mockDebugger.connect();

    debuggerSynchronizer.receivedEvent(new DebuggerClosedUnexpectedlyReply());

    assertFalse(mockDebugger.isConnected());

    assertEquals("DEBUGGER_CLOSED/0;", listener.events);
  }

  @Test
  public void testHandleExceptionOccured() throws DebugExceptionWrapper, MaybeNullException {
    mockDebugger.connect();

    final TargetProcessThread thread = new TargetProcessThread(18, ThreadState.RUNNING);

    mockDebugger.getProcessManager().addThread(thread);

    debuggerSynchronizer.receivedEvent(new ExceptionOccurredReply(0,
        0,
        18,
        5,
        CommonTestObjects.BP_ADDRESS_123_RELOC,
        "Test exception"));

    assertEquals(thread, mockDebugger.getProcessManager().getActiveThread());
    assertEquals(ThreadState.RUNNING, mockDebugger.getProcessManager().getThread(18).getState());
    assertEquals(CommonTestObjects.BP_ADDRESS_123_RELOC,
        mockDebugger.getProcessManager().getThread(18).getCurrentAddress());
    assertEquals("CONNECT;READREGS;", mockDebugger.requests);
    assertEquals("EXCEPTION_OCCURRED/5;", listener.events);
  }

  @Test
  public void testHitBreakpoint_UnknownBreakpoint() throws MessageParserException {
    mockDebugger.getProcessManager().addThread(new TargetProcessThread(123, ThreadState.SUSPENDED));

    debuggerSynchronizer.receivedEvent(
        DebuggerMessageBuilder.buildRegularBreakpointHit(CommonTestObjects.BP_ADDRESS_456_RELOC));

    assertEquals(0, listener.exception);
  }

  @Test
  public void testHitBreakpoint_UnknownThread() throws MessageParserException {
    debuggerSynchronizer.receivedEvent(
        DebuggerMessageBuilder.buildRegularBreakpointHit(CommonTestObjects.BP_ADDRESS_123_RELOC));

    assertEquals(0, listener.exception);
  }

  @Test
  public void testHitBreakpoint_Wellformed() throws MessageParserException {
    breakpointManager.addBreakpoints(BreakpointType.REGULAR, CommonTestObjects.BP_ADDRESS_456_SET);
    breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_ACTIVE);

    mockDebugger.getProcessManager().addThread(
        new TargetProcessThread(CommonTestObjects.THREAD_ID, ThreadState.SUSPENDED));

    debuggerSynchronizer.receivedEvent(
        DebuggerMessageBuilder.buildRegularBreakpointHit(CommonTestObjects.BP_ADDRESS_456_RELOC));

    assertEquals(0, listener.exception);
    assertEquals(BreakpointStatus.BREAKPOINT_HIT, breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));
  }

  @Test
  public void testHitEchoBreakpoint() throws DebugExceptionWrapper {
    mockDebugger.connect();

    breakpointManager.addBreakpoints(BreakpointType.ECHO, CommonTestObjects.BP_ADDRESS_456_SET);

    assertEquals(BreakpointStatus.BREAKPOINT_INACTIVE, breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.ECHO));
  }

  @Test
  public void testHitEchoBreakpoint_Wellformed() throws DebugExceptionWrapper,
      MessageParserException {
    mockDebugger.connect();

    breakpointManager.addBreakpoints(BreakpointType.ECHO, CommonTestObjects.BP_ADDRESS_456_SET);
    breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET, BreakpointType.ECHO,
        BreakpointStatus.BREAKPOINT_ACTIVE);

    mockDebugger.getProcessManager().addThread(new TargetProcessThread(123, ThreadState.SUSPENDED));

    assertEquals(1, breakpointManager.getNumberOfBreakpoints(BreakpointType.ECHO));

    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.ECHO));

    debuggerSynchronizer.receivedEvent(
        DebuggerMessageBuilder.buildEchoBreakpointHit(CommonTestObjects.BP_ADDRESS_456_RELOC));

    assertEquals(0, listener.exception);
    assertEquals(1, breakpointManager.getNumberOfBreakpoints(BreakpointType.ECHO));

    debuggerSynchronizer.receivedEvent(DebuggerMessageBuilder.buildEchoBreakpointRemoveSucc(
        CommonTestObjects.BP_ADDRESS_456_RELOC));

    assertEquals(0, breakpointManager.getNumberOfBreakpoints(BreakpointType.ECHO));
  }

  @Test
  public void testInfoString_Malformed() {
    debuggerSynchronizer.receivedEvent(new TargetInformationReply(0, 5, null));

    assertEquals(0, listener.exception);
  }

  @Test
  public void testInfoString_Wellformed() throws DebugExceptionWrapper, MaybeNullException {
    // Set breakpoints while the debugger is not connected

    breakpointManager.addBreakpoints(BreakpointType.REGULAR, CommonTestObjects.BP_ADDRESS_123_SET);
    breakpointManager.addBreakpoints(BreakpointType.REGULAR, CommonTestObjects.BP_ADDRESS_456_SET);

    breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DISABLED);

    assertEquals(BreakpointStatus.BREAKPOINT_INACTIVE,
        breakpointManager.getBreakpointStatus(BreakpointType.REGULAR, 0));

    mockDebugger.connect();

    debuggerSynchronizer.receivedEvent(
        DebuggerMessageBuilder.buildProcessStartReply(CommonTestObjects.MEMORY_MODULE));

    assertEquals(0, listener.exception);
    assertNotNull(mockDebugger.getProcessManager().getThread(CommonTestObjects.THREAD_ID));
    assertEquals(ThreadState.RUNNING,
        mockDebugger.getProcessManager().getThread(CommonTestObjects.THREAD_ID).getState());

    debuggerSynchronizer.receivedEvent(new ThreadClosedReply(0, 0, CommonTestObjects.THREAD_ID));

    try {
      mockDebugger.getProcessManager().getThread(CommonTestObjects.THREAD_ID);
      fail();
    } catch (final MaybeNullException exception) {
      CUtilityFunctions.logException(exception);
    }
    // On receiving an info string we request the memory map and the thread created reply triggers
    // read registers
    assertEquals("CONNECT;READREGS;SET_BREAKPOINTS/"
        + String.format("%08d", CommonTestObjects.THREAD_ID) + "/REGULAR;RESUME;",
        mockDebugger.requests);

    // Enabled breakpoints are activated
    assertEquals(BreakpointStatus.BREAKPOINT_ENABLED,
        breakpointManager.getBreakpointStatus(BreakpointType.REGULAR, 0));
    assertEquals(BreakpointStatus.BREAKPOINT_DISABLED,
        breakpointManager.getBreakpointStatus(BreakpointType.REGULAR, 1));
  }

  @Test
  public void testMemmap() {
    final IFilledList<MemorySection> sections = new FilledList<MemorySection>();
    sections.add(new MemorySection(new CAddress(100), new CAddress(200)));
    sections.add(new MemorySection(new CAddress(300), new CAddress(400)));

    debuggerSynchronizer.receivedEvent(new MemoryMapReply(0, 0, new MemoryMap(sections)));

    assertEquals(0, listener.exception);
    assertEquals(2, mockDebugger.getProcessManager().getMemoryMap().getNumberOfSections());
  }

  @Test
  public void testMemoryErr() {
    debuggerSynchronizer.receivedEvent(new ReadMemoryReply(0, 5, null, null));

    assertEquals(0, listener.exception);
    assertEquals("ERROR_READING_MEMORY/5;", listener.events);
    assertFalse(mockDebugger.getProcessManager().getMemory()
        .hasData(CommonTestObjects.BP_ADDRESS_123.getAddress().getAddress().toLong(), 6));
  }

  @Test
  public void testMemorySucc() {
    debuggerSynchronizer.receivedEvent(new ReadMemoryReply(0, 0,
        CommonTestObjects.BP_ADDRESS_123.getAddress().getAddress(), "Hannes".getBytes()));

    assertEquals(0, listener.exception);
    assertEquals("RECEIVED_MEMORY/00000123/6;", listener.events);
    assertTrue(mockDebugger.getProcessManager().getMemory().hasData(0x123, 6));
  }

  /**
   * This test makes sure that the memory module lifecycle (Module Loaded -> Module Unloaded) is
   * working and that the process manager of the debugger is updated correctly.
   *
   * @throws DebugExceptionWrapper
   */
  @Test
  public void testModuleLifecycle() throws DebugExceptionWrapper {
    assertTrue(mockDebugger.getProcessManager().getModules().isEmpty());

    mockDebugger.connect();
    mockDebugger.getProcessManager().getThreads().clear();

    debuggerSynchronizer.receivedEvent(new ThreadCreatedReply(0, 0, 1000, ThreadState.RUNNING));

    final MemoryModule module = new MemoryModule("hannes.dll", "C:\\hannes.dll",
        new RelocatedAddress(new CAddress(0x1000000)), 1000);
    debuggerSynchronizer.receivedEvent(
        new ModuleLoadedReply(0, 0, module, new TargetProcessThread(1000, ThreadState.RUNNING)));

    mockDebugger.getProcessManager().setTargetInformation(new TargetInformation(
        5, Lists.newArrayList(new RegisterDescription("eax", 4, true),
            new RegisterDescription("ebx", 4, false)), new DebuggerOptions(false,
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

    assertTrue(mockDebugger.getProcessManager().getModules().size() == 1);
    assertTrue(mockDebugger.getProcessManager().getModules().get(0) == module);

    debuggerSynchronizer.receivedEvent(new ModuleUnloadedReply(0, 0, module));
    assertTrue(mockDebugger.getProcessManager().getModules().isEmpty());
  }

  @Test
  public void testProcessClosed() throws DebugExceptionWrapper {
    mockDebugger.connect();

    debuggerSynchronizer.receivedEvent(new ProcessClosedReply(0, 0));

    assertFalse(mockDebugger.isConnected());
    assertEquals("PROCESS_CLOSED;", listener.events);
  }

  @Test
  public void testRegisterValues_Malformed() {
    mockDebugger.getProcessManager().addThread(new TargetProcessThread(123, ThreadState.RUNNING));

    NaviLogger.setLevel(Level.OFF);

    try {
      debuggerSynchronizer.receivedEvent(
          new RegistersReply(0, 0, RegisterValuesParser.parse("Hannes".getBytes())));
      fail();
    } catch (final MessageParserException exception) {
      CUtilityFunctions.logException(exception);
    } finally {
      NaviLogger.setLevel(Level.SEVERE);
    }
  }

  @Test
  public void testRegisterValues_UnknownTID() throws MessageParserException {
    mockDebugger.getProcessManager().addThread(new TargetProcessThread(123, ThreadState.SUSPENDED));

    debuggerSynchronizer.receivedEvent(new RegistersReply(0, 0, RegisterValuesParser.parse((
        "<Registers><Thread id=\"123\"><Register name=\"EAX\" "
        + "value=\"123\" memory=\"\" /><Register name=\"EBX\" value=\"456\" memory=\"\" "
        + "/><Register name=\"EIP\" value=\"999\" memory=\"\" pc=\"true\" /></Thread>"
        + "</Registers>").getBytes())));

    assertEquals(0, listener.exception);
  }

  @Test
  public void testRegisterValues_Wellformed() throws MessageParserException, MaybeNullException {
    mockDebugger.getProcessManager().addThread(new TargetProcessThread(123, ThreadState.RUNNING));

    debuggerSynchronizer.receivedEvent(new RegistersReply(0, 0, RegisterValuesParser.parse((
        "<Registers><Thread id=\"123\"><Register name=\"EAX\" "
        + "value=\"123\" memory=\"\" /><Register name=\"EBX\" value=\"456\" memory=\"\" "
        + "/><Register name=\"EIP\" value=\"999\" memory=\"\" pc=\"true\" /></Thread>"
        + "</Registers>").getBytes())));

    assertEquals(0, listener.exception);
    assertEquals(0x456, mockDebugger
        .getProcessManager()
        .getThread(123)
        .getRegisterValues()
        .get(1)
        .getValue()
        .longValue());
    assertEquals(0x999,
        mockDebugger.getProcessManager().getThread(123).getCurrentAddress().getAddress().toLong());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testRemoveBreakpoint_Active() throws DebugExceptionWrapper {
    mockDebugger.connect();

    breakpointManager.addBreakpoints(BreakpointType.REGULAR, CommonTestObjects.BP_ADDRESS_456_SET);
    breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_ACTIVE);

    mockDebugger.getProcessManager().addThread(new TargetProcessThread(123, ThreadState.SUSPENDED));

    breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DELETING);

    debuggerSynchronizer.receivedEvent(new BreakpointsRemovedReply(0, 0, Lists.newArrayList(
        new Pair<RelocatedAddress, Integer>(CommonTestObjects.BP_ADDRESS_456_RELOC, 0))));

    assertEquals(0, listener.exception);
    assertEquals(0, breakpointManager.getNumberOfBreakpoints(BreakpointType.REGULAR));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testRemoveBreakpoint_Disabled() {
    breakpointManager.addBreakpoints(BreakpointType.REGULAR, CommonTestObjects.BP_ADDRESS_456_SET);
    breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DISABLED);

    mockDebugger.getProcessManager().addThread(new TargetProcessThread(123, ThreadState.SUSPENDED));

    debuggerSynchronizer.receivedEvent(new BreakpointsRemovedReply(0, 0, Lists.newArrayList(
        new Pair<RelocatedAddress, Integer>(CommonTestObjects.BP_ADDRESS_456_RELOC, 0))));

    assertEquals(0, listener.exception);
    assertEquals(1, breakpointManager.getNumberOfBreakpoints(BreakpointType.REGULAR));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testRemoveBreakpointError_Invalid() {
    debuggerSynchronizer.receivedEvent(new BreakpointsRemovedReply(0, 5, Lists.newArrayList(
        new Pair<RelocatedAddress, Integer>(CommonTestObjects.BP_ADDRESS_456_RELOC, 0))));

    assertEquals(0, listener.exception);
    assertEquals(0, breakpointManager.getNumberOfBreakpoints(BreakpointType.REGULAR));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testRemoveBreakpointError_Valid() {
    breakpointManager.addBreakpoints(BreakpointType.REGULAR, CommonTestObjects.BP_ADDRESS_456_SET);
    breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_ACTIVE);

    debuggerSynchronizer.receivedEvent(new BreakpointsRemovedReply(0, 0, Lists.newArrayList(
        new Pair<RelocatedAddress, Integer>(CommonTestObjects.BP_ADDRESS_456_RELOC, 5))));

    assertEquals(0, listener.exception);
    assertEquals(1, breakpointManager.getNumberOfBreakpoints(BreakpointType.REGULAR));
    assertEquals(BreakpointStatus.BREAKPOINT_INVALID, breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));
  }

  @Test
  public void testResume_UnknownTID() {
    debuggerSynchronizer.receivedEvent(new ResumeReply(0, 0));

    assertEquals(0, listener.exception);
  }

  @Test
  public void testResume_Wellformed() throws MaybeNullException {
    breakpointManager.addBreakpoints(BreakpointType.REGULAR, CommonTestObjects.BP_ADDRESS_456_SET);
    breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_456_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_HIT);

    final TargetProcessThread thread = new TargetProcessThread(123, ThreadState.RUNNING);

    mockDebugger.getProcessManager().addThread(thread);
    mockDebugger.getProcessManager().setActiveThread(thread);

    debuggerSynchronizer.receivedEvent(new ResumeReply(0, 0));

    assertEquals(0, listener.exception);
    assertNull(mockDebugger.getProcessManager().getActiveThread());
    assertEquals(ThreadState.RUNNING, mockDebugger.getProcessManager().getThread(123).getState());
    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_456, BreakpointType.REGULAR));
  }

  @Test
  public void testSetRegisterErr() {
    debuggerSynchronizer.receivedEvent(new SetRegisterReply(0, 5, 0, 0));
    assertEquals("ERROR_SET_REGISTERS/5;", listener.events);
  }

  @Test
  public void testSetRegisterSucc() throws DebugExceptionWrapper {
    mockDebugger.connect();

    debuggerSynchronizer.receivedEvent(new SetRegisterReply(0, 0, 0, 0));

    assertEquals("CONNECT;READREGS;", mockDebugger.requests);
  }

  @Test
  public void testSingleStep_Err() {
    debuggerSynchronizer.receivedEvent(new SingleStepReply(0, 5, 0,
        new RelocatedAddress(CommonTestObjects.BP_ADDRESS_123.getAddress().getAddress()), null));
    assertEquals("ERROR_SINGLE_STEP/5;", listener.events);
  }

  @Test
  public void testSingleStep_Valid() throws MessageParserException, MaybeNullException {
    breakpointManager.addBreakpoints(BreakpointType.REGULAR, CommonTestObjects.BP_ADDRESS_333_SET);
    breakpointManager.setBreakpointStatus(CommonTestObjects.BP_ADDRESS_333_SET,
        BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_HIT);

    mockDebugger.getProcessManager().addThread(new TargetProcessThread(123, ThreadState.SUSPENDED));

    debuggerSynchronizer.receivedEvent(new SingleStepReply(0, 0, 123,
        new RelocatedAddress(new CAddress(0x999)), RegisterValuesParser.parse((
            "<Registers><Thread id=\"123\">" + "<Register name=\"EAX\" value=\"123\" memory=\"\" />"
            + "<Register name=\"EBX\" value=\"456\" memory=\"\" />"
            + "<Register name=\"EIP\" value=\"999\" memory=\"\" pc=\"true\" />"
            + "</Thread></Registers>").getBytes())));

    assertEquals(ThreadState.SUSPENDED, mockDebugger.getProcessManager().getThread(123).getState());
    assertEquals(0x999,
        mockDebugger.getProcessManager().getThread(123).getCurrentAddress().getAddress().toLong());
    assertEquals(0x456, mockDebugger
        .getProcessManager()
        .getThread(123)
        .getRegisterValues()
        .get(1)
        .getValue()
        .longValue());
    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, breakpointManager.getBreakpointStatus(
        CommonTestObjects.BP_ADDRESS_333, BreakpointType.REGULAR));
  }

  /**
   * This test is used to determine whether the step breakpoint lifecycle (Set Step BP -> Hit Step
   * BP -> Remove Step BP) works correctly.
   *
   * @throws DebugExceptionWrapper Thrown if something goes wrong.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testStepBreakpointLifecycle() throws DebugExceptionWrapper {
    mockDebugger.connect();

    final TargetProcessThread thread = new TargetProcessThread(0, ThreadState.RUNNING);
    mockDebugger.getProcessManager().addThread(thread);

    mockDebugger.getBreakpointManager().addBreakpoints(BreakpointType.STEP,
        CommonTestObjects.BP_ADDRESS_123_SET);
    mockDebugger.getBreakpointManager().addBreakpoints(BreakpointType.STEP,
        CommonTestObjects.BP_ADDRESS_456_SET);

    debuggerSynchronizer.receivedEvent(new StepBreakpointSetReply(0, 0, Lists.newArrayList(
        new Pair<RelocatedAddress, Integer>(CommonTestObjects.BP_ADDRESS_123_RELOC, 0),
        new Pair<RelocatedAddress, Integer>(CommonTestObjects.BP_ADDRESS_456_RELOC, 0))));

    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, mockDebugger.getBreakpointManager()
        .getBreakpointStatus(CommonTestObjects.BP_ADDRESS_123, BreakpointType.STEP));
    assertEquals(BreakpointStatus.BREAKPOINT_ACTIVE, mockDebugger.getBreakpointManager()
        .getBreakpointStatus(CommonTestObjects.BP_ADDRESS_456, BreakpointType.STEP));

    final RegisterValues registerValues =
        new RegisterValues(Lists.<ThreadRegisters>newArrayList(new ThreadRegisters(
            0, Lists.newArrayList(
                new RegisterValue("esp", BigInteger.valueOf(0x123), new byte[0], true, false)))));
    debuggerSynchronizer.receivedEvent(new StepBreakpointHitReply(0, 0, 0, registerValues));

    listener.toString();

    assertTrue(
        Iterables.isEmpty(mockDebugger.getBreakpointManager().getBreakpoints(BreakpointType.STEP)));
    assertEquals(thread, mockDebugger.getProcessManager().getActiveThread());
    assertEquals(0x123, thread.getCurrentAddress().getAddress().toLong());
  }

  @Test
  public void testTerminate() throws DebugExceptionWrapper {
    mockDebugger.connect();

    assertTrue(mockDebugger.isConnected());

    debuggerSynchronizer.receivedEvent(new TerminateReply(0, 0));

    assertFalse(mockDebugger.isConnected());
  }

  @Test
  public void testThreadClosed() throws DebugExceptionWrapper {
    mockDebugger.connect();
    debuggerSynchronizer.receivedEvent(new ThreadCreatedReply(0, 0, 18, ThreadState.RUNNING));

    assertEquals(1, mockDebugger.getProcessManager().getThreads().size());

    debuggerSynchronizer.receivedEvent(new ThreadClosedReply(0, 0, 18));

    assertEquals(0, mockDebugger.getProcessManager().getThreads().size());
  }

  @Test
  public void testThreadCreated() throws MaybeNullException, DebugExceptionWrapper {
    mockDebugger.connect();
    debuggerSynchronizer.receivedEvent(new ThreadCreatedReply(0, 0, 18, ThreadState.RUNNING));
    debuggerSynchronizer.receivedEvent(new ThreadCreatedReply(0, 0, 19, ThreadState.SUSPENDED));

    assertEquals(ThreadState.RUNNING, mockDebugger.getProcessManager().getThread(18).getState());
    assertEquals(ThreadState.SUSPENDED, mockDebugger.getProcessManager().getThread(19).getState());
  }

  /**
   * This test is used to test the thread lifecycle of suspending and resuming threads.
   *
   * @throws DebugExceptionWrapper Thrown if something goes wrong.
   */
  @Test
  public void testThreadLifecycle() throws DebugExceptionWrapper {
    mockDebugger.connect();

    final TargetProcessThread thread = new TargetProcessThread(123, ThreadState.RUNNING);

    mockDebugger.getProcessManager().addThread(thread);

    debuggerSynchronizer.receivedEvent(new SuspendThreadReply(0, 0, 123));

    assertEquals(ThreadState.SUSPENDED, thread.getState());

    debuggerSynchronizer.receivedEvent(new ResumeThreadReply(0, 0, 123));

    assertEquals(ThreadState.RUNNING, thread.getState());
  }
}
