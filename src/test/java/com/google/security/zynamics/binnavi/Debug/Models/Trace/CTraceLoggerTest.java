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
package com.google.security.zynamics.binnavi.Debug.Models.Trace;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Debug.Debugger.DebuggerMessageBuilder;
import com.google.security.zynamics.binnavi.Debug.Debugger.MockDebugger;
import com.google.security.zynamics.binnavi.debug.connection.packets.parsers.MessageParserException;
import com.google.security.zynamics.binnavi.debug.debugger.AbstractDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.ModuleTargetSettings;
import com.google.security.zynamics.binnavi.debug.debugger.synchronizers.DebuggerSynchronizer;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceLogger;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListProvider;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.reflection.ReflectionHelpers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashSet;
import java.util.Set;

@RunWith(JUnit4.class)
public final class CTraceLoggerTest {
  final MockModule module = CommonTestObjects.MODULE;

  @Test
  public void testAllBreakpointsBlocked() throws DebugExceptionWrapper {
    // Scenario: All given addresses are already blocked by regular breakpoints

    final ITraceListProvider provider = new MockTraceListProvider();
    final MockDebugger debugger = new MockDebugger(new ModuleTargetSettings(module));
    debugger.setAddressTranslator(module, new CAddress(0), new CAddress(0x1000));
    debugger.connect();

    debugger.getBreakpointManager()
        .addBreakpoints(
            BreakpointType.REGULAR,
            Sets.newHashSet(new BreakpointAddress(module, new UnrelocatedAddress(new CAddress(
                0x100)))));

    final TraceLogger logger = new TraceLogger(provider, debugger);

    final MockTraceLoggerListener listener = new MockTraceLoggerListener();
    logger.addListener(listener);

    final MockSqlProvider sqlProvider = new MockSqlProvider();

    final TraceList trace = new TraceList(1, "Foo", "Bar", sqlProvider);

    final Set<BreakpointAddress> addresses = new HashSet<BreakpointAddress>();

    addresses.add(new BreakpointAddress(module, new UnrelocatedAddress(new CAddress(0x100))));

    logger.start(trace, addresses, 1);

    logger.stop();

    assertEquals("", listener.events);

    // Start the trace mode again to make sure all resources were cleaned
    // up in the previous run.
    logger.start(trace, addresses, 1);

    logger.stop();

    debugger.close();
  }

  @Test
  public void testCase1637() throws DebugExceptionWrapper, InterruptedException {
    // This test tries to check for a race condition that happens like this:
    //
    // 1. Trace mode is stopping
    // 2. Stopping trace mode removes listeners of hit breakpoint before breakpoint is hit
    // 3. Echo breakpoint is hit => listener is removed again
    //
    // Result: Crash

    final ITraceListProvider provider = new MockTraceListProvider();
    final MockDebugger debugger = new MockDebugger(new ModuleTargetSettings(module));
    debugger.connect();

    final MockModule module = new MockModule();
    debugger.setAddressTranslator(module, new CAddress(0), new CAddress(0x1000));

    final Set<BreakpointAddress> addresses = new HashSet<BreakpointAddress>();

    for (int i = 0; i < 1000; i++) {
      addresses.add(new BreakpointAddress(module, new UnrelocatedAddress(new CAddress(i))));
    }

    final TraceLogger logger = new TraceLogger(provider, debugger);

    final MockTraceLoggerListener listener = new MockTraceLoggerListener();
    logger.addListener(listener);

    final MockSqlProvider sqlProvider = new MockSqlProvider();

    final TraceList trace = new TraceList(1, "Foo", "Bar", sqlProvider);

    logger.start(trace, addresses, 1);

    final Breakpoint breakpoint =
        debugger.getBreakpointManager().getBreakpoint(BreakpointType.ECHO,
            new BreakpointAddress(module, new UnrelocatedAddress(new CAddress(999))));

    new Thread() {
      @Override
      public void run() {
        logger.stop();
      }
    }.start();

    new Thread() {
      @Override
      public void run() {
        debugger.getBreakpointManager().setBreakpointStatus(
            Sets.newHashSet(breakpoint.getAddress()), breakpoint.getType(),
            BreakpointStatus.BREAKPOINT_HIT);
      }
    }.start();

    while (logger.activeEchoBreakpointCount() != 0) {
      Thread.sleep(100);
    }
  }

  @Test
  public void testHitEchoBreakpoint() throws DebugExceptionWrapper, MessageParserException,
      SecurityException, IllegalAccessException, NoSuchFieldException {
    // Scenario: All echo breakpoints are hit

    final ITraceListProvider provider = new MockTraceListProvider();
    final MockDebugger debugger = new MockDebugger(new ModuleTargetSettings(module));

    final MockModule module = new MockModule();

    debugger.setAddressTranslator(module, new CAddress(0), new CAddress(0x1000));

    debugger.connect();

    final TraceLogger logger = new TraceLogger(provider, debugger);

    final MockTraceLoggerListener listener = new MockTraceLoggerListener();
    logger.addListener(listener);

    final MockSqlProvider sqlProvider = new MockSqlProvider();

    final TraceList trace = new TraceList(1, "Foo", "Bar", sqlProvider);

    final Set<BreakpointAddress> addresses = new HashSet<BreakpointAddress>();

    addresses.add(new BreakpointAddress(module, new UnrelocatedAddress(new CAddress(0x100))));
    addresses.add(new BreakpointAddress(module, new UnrelocatedAddress(new CAddress(0x200))));

    logger.start(trace, addresses, 3);

    final DebuggerSynchronizer m_synchronizer =
        (DebuggerSynchronizer) ReflectionHelpers.getField(AbstractDebugger.class, debugger,
            "synchronizer");

    m_synchronizer.receivedEvent(DebuggerMessageBuilder
        .buildEchoBreakpointHit(new RelocatedAddress(new CAddress(0x1100))));
    m_synchronizer.receivedEvent(DebuggerMessageBuilder
        .buildEchoBreakpointHit(new RelocatedAddress(new CAddress(0x1200))));

    assertEquals(2, trace.getEventCount());
    assertEquals("++", listener.events);

    m_synchronizer.receivedEvent(DebuggerMessageBuilder
        .buildEchoBreakpointHit(new RelocatedAddress(new CAddress(0x1100))));
    m_synchronizer.receivedEvent(DebuggerMessageBuilder
        .buildEchoBreakpointHit(new RelocatedAddress(new CAddress(0x1200))));

    assertEquals(4, trace.getEventCount());
    assertEquals("++", listener.events);

    m_synchronizer.receivedEvent(DebuggerMessageBuilder
        .buildEchoBreakpointHit(new RelocatedAddress(new CAddress(0x1100))));
    m_synchronizer.receivedEvent(DebuggerMessageBuilder
        .buildEchoBreakpointHit(new RelocatedAddress(new CAddress(0x1200))));

    assertEquals(6, trace.getEventCount());
    assertEquals("++--!", listener.events);

    m_synchronizer.receivedEvent(DebuggerMessageBuilder
        .buildEchoBreakpointHit(new RelocatedAddress(new CAddress(0x1100))));
    m_synchronizer.receivedEvent(DebuggerMessageBuilder
        .buildEchoBreakpointHit(new RelocatedAddress(new CAddress(0x1200))));

    assertEquals(6, trace.getEventCount());
    assertEquals("++--!", listener.events);

    logger.stop();

    assertEquals(6, trace.getEventCount());
    assertEquals("++--!", listener.events);

    logger.start(trace, addresses, 1);

    m_synchronizer.receivedEvent(DebuggerMessageBuilder
        .buildEchoBreakpointHit(new RelocatedAddress(new CAddress(0x1100))));
    m_synchronizer.receivedEvent(DebuggerMessageBuilder
        .buildEchoBreakpointHit(new RelocatedAddress(new CAddress(0x1200))));

    logger.stop();

    assertEquals(8, trace.getEventCount());

    debugger.close();
  }

  @Test
  public void testOverwrittenAllEchoBreakpoints() throws DebugExceptionWrapper {
    // Scenario: All echo breakpoints of the trace are overwritten by regular breakpoints

    final ITraceListProvider provider = new MockTraceListProvider();
    final MockDebugger debugger = new MockDebugger(new ModuleTargetSettings(module));
    debugger.setAddressTranslator(module, new CAddress(0), new CAddress(0x1000));
    debugger.connect();

    final TraceLogger logger = new TraceLogger(provider, debugger);

    final MockTraceLoggerListener listener = new MockTraceLoggerListener();
    logger.addListener(listener);

    final MockSqlProvider sqlProvider = new MockSqlProvider();

    final TraceList trace = new TraceList(1, "Foo", "Bar", sqlProvider);

    final Set<BreakpointAddress> addresses = new HashSet<BreakpointAddress>();

    addresses.add(new BreakpointAddress(module, new UnrelocatedAddress(new CAddress(0x100))));
    addresses.add(new BreakpointAddress(module, new UnrelocatedAddress(new CAddress(0x200))));

    logger.start(trace, addresses, 1);

    debugger.getBreakpointManager()
        .addBreakpoints(
            BreakpointType.REGULAR,
            Sets.newHashSet(new BreakpointAddress(module, new UnrelocatedAddress(new CAddress(
                0x100)))));
    debugger.getBreakpointManager()
        .addBreakpoints(
            BreakpointType.REGULAR,
            Sets.newHashSet(new BreakpointAddress(module, new UnrelocatedAddress(new CAddress(
                0x200)))));

    logger.stop();

    assertEquals("++--!", listener.events);

    // Start the trace mode again to make sure all resources were cleaned
    // up in the previous run.
    logger.start(trace, addresses, 1);

    logger.stop();

    debugger.close();
  }

  @Test
  public void testOverwrittenEchoBreakpoint() throws DebugExceptionWrapper {
    // Scenario: Echo breakpoint of the trace is overwritten by a regular breakpoint

    final ITraceListProvider provider = new MockTraceListProvider();
    final MockDebugger debugger = new MockDebugger(new ModuleTargetSettings(module));
    debugger.setAddressTranslator(module, new CAddress(0), new CAddress(0x1000));
    debugger.connect();

    final TraceLogger logger = new TraceLogger(provider, debugger);

    final MockTraceLoggerListener listener = new MockTraceLoggerListener();
    logger.addListener(listener);

    final MockSqlProvider sqlProvider = new MockSqlProvider();

    final TraceList trace = new TraceList(1, "Foo", "Bar", sqlProvider);

    final Set<BreakpointAddress> addresses = new HashSet<BreakpointAddress>();

    addresses.add(new BreakpointAddress(module, new UnrelocatedAddress(new CAddress(0x100))));
    addresses.add(new BreakpointAddress(module, new UnrelocatedAddress(new CAddress(0x200))));

    logger.start(trace, addresses, 1);

    debugger.getBreakpointManager()
        .addBreakpoints(
            BreakpointType.REGULAR,
            Sets.newHashSet(new BreakpointAddress(module, new UnrelocatedAddress(new CAddress(
                0x100)))));

    assertEquals("++-", listener.events);

    logger.stop();

    assertEquals("++--!", listener.events);

    // Start the trace mode again to make sure all resources were cleaned
    // up in the previous run.
    logger.start(trace, addresses, 1);

    logger.stop();

    debugger.close();
  }

  @Test
  public void testTargetClosed() throws DebugExceptionWrapper {
    final ITraceListProvider provider = new MockTraceListProvider();
    final MockDebugger debugger = new MockDebugger(new ModuleTargetSettings(module));
    debugger.setAddressTranslator(module, new CAddress(0), new CAddress(0x1000));
    debugger.connect();

    final TraceLogger logger = new TraceLogger(provider, debugger);

    final MockTraceLoggerListener listener = new MockTraceLoggerListener();
    logger.addListener(listener);

    final MockSqlProvider sqlProvider = new MockSqlProvider();

    final TraceList trace = new TraceList(1, "Foo", "Bar", sqlProvider);

    final Set<BreakpointAddress> addresses = new HashSet<BreakpointAddress>();

    addresses.add(new BreakpointAddress(module, new UnrelocatedAddress(new CAddress(0x100))));

    logger.start(trace, addresses, 1);

    debugger.getProcessManager().setAttached(false);

    assertEquals("+!", listener.events);

    logger.stop();

    // assertEquals(0, debugger.getBreakpointManager().getNumberOfBreakpoints(BreakpointType.ECHO));

    // Start the trace mode again to make sure all resources were cleaned
    // up in the previous run.
    logger.start(trace, addresses, 1);

    logger.stop();

    debugger.close();
  }

  @Test
  public void testWithoutHits() throws DebugExceptionWrapper {
    final ITraceListProvider provider = new MockTraceListProvider();
    final MockDebugger debugger = new MockDebugger(new ModuleTargetSettings(module));
    debugger.setAddressTranslator(module, new CAddress(0), new CAddress(0x1000));
    debugger.connect();

    final TraceLogger logger = new TraceLogger(provider, debugger);

    final MockTraceLoggerListener listener = new MockTraceLoggerListener();
    logger.addListener(listener);

    final MockSqlProvider sqlProvider = new MockSqlProvider();

    final TraceList trace = new TraceList(1, "Foo", "Bar", sqlProvider);

    final Set<BreakpointAddress> addresses = new HashSet<BreakpointAddress>();

    addresses.add(new BreakpointAddress(module, new UnrelocatedAddress(new CAddress(0x100))));

    logger.start(trace, addresses, 1);

    assertEquals("+", listener.events);

    logger.stop();

    assertEquals("+-!", listener.events);

    // Start the trace mode again to make sure all resources were cleaned
    // up in the previous run.
    logger.start(trace, addresses, 1);

    logger.stop();

    debugger.close();
  }
}
