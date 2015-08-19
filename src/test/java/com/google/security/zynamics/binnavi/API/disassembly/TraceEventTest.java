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
package com.google.security.zynamics.binnavi.API.disassembly;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceEventType;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;

@RunWith(JUnit4.class)
public final class TraceEventTest {
  @Test
  public void testConstructor() {
    final MockModule module = new MockModule();

    final TraceEvent event =
        new TraceEvent(new com.google.security.zynamics.binnavi.debug.models.trace.TraceEvent(0,
            new BreakpointAddress(module, new UnrelocatedAddress(new CAddress(0x123))),
            TraceEventType.ECHO_BREAKPOINT, new ArrayList<
                com.google.security.zynamics.binnavi.debug.models.trace.TraceRegister>()));

    assertEquals(0x123, event.getAddress().toLong());
    assertEquals(com.google.security.zynamics.binnavi.API.disassembly.TraceEventType.EchoBreakpoint,
        event.getType());
    assertEquals("Trace Event [EchoBreakpoint : 123]", event.toString());
  }
}
