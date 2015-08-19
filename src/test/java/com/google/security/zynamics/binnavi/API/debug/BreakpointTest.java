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

import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class BreakpointTest {
  private final MockModule module = new MockModule();
  private final
      com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint m_internalBreakpoint = new com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint(BreakpointType.REGULAR, new BreakpointAddress(module, new UnrelocatedAddress(new CAddress(0x123))));
  private final Breakpoint m_apiBreakpoint = new Breakpoint(m_internalBreakpoint);

  @Test
  public void testChangeDescription() {
    m_apiBreakpoint.setDescription(null);

    m_apiBreakpoint.setDescription("Fork");

    assertEquals("Fork", m_apiBreakpoint.getDescription());
    assertEquals("Fork", m_internalBreakpoint.getDescription());
  }

  @Test
  public void testConstructor() {
    assertEquals(0x123, m_apiBreakpoint.getAddress().toLong());

    assertEquals("Breakpoint 123", m_apiBreakpoint.toString());
  }
}
