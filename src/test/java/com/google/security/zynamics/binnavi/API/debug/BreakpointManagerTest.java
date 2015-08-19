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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.API.disassembly.ModuleFactory;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

@RunWith(JUnit4.class)
public final class BreakpointManagerTest {
  private final
      com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager
      internalManager =
          new com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager();
  private final MockBreakpointManagerListener mockListener = new MockBreakpointManagerListener();
  private final com.google.security.zynamics.binnavi.API.debug.BreakpointManager apiManager =
      new com.google.security.zynamics.binnavi.API.debug.BreakpointManager(internalManager);
  final Module module = ModuleFactory.get(CommonTestObjects.MODULE);

  @Test
  public void testAddMultipleNative() {
    internalManager.addBreakpoints(BreakpointType.ECHO, Sets.newHashSet(new BreakpointAddress(
        CommonTestObjects.MODULE, new UnrelocatedAddress(new CAddress(0x124)))));
    assertTrue(apiManager.hasEchoBreakpoint(module, new Address(0x124)));
    internalManager.clearBreakpointsPassive(BreakpointType.ECHO);
  }

  @Test
  public void testGetBreakpoint() {
    try {
      apiManager.getBreakpoint(null, null);
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      apiManager.getBreakpoint(null, new Address(0x123));
      fail();
    } catch (final NullPointerException exception) {
    }

    apiManager.setBreakpoint(module, new Address(0x123));
    final Breakpoint breakpoint = apiManager.getBreakpoint(module, new Address(0x123));
    assertEquals(0x123, breakpoint.getAddress().toLong());
    final Module module = ModuleFactory.get();
    apiManager.setBreakpoint(module, new Address(0x123));
    assertTrue(apiManager.hasBreakpoint(module, new Address(0x123)));
    final Breakpoint breakpoint2 = apiManager.getBreakpoint(module, new Address(0x123));
    assertEquals(0x123, breakpoint2.getAddress().toLong());
    apiManager.removeBreakpoint(module, new Address(0x123));
  }

  @Test
  public void testGetBreakpoints() {
    apiManager.setBreakpoint(module, new Address(0x123));
    final List<Breakpoint> breakpoints = apiManager.getBreakpoints();
    assertEquals(0x123, breakpoints.get(0).getAddress().toLong());
  }

  @Test
  public void testGetEchoBreakpoint() {
    try {
      apiManager.getEchoBreakpoint(null, null);

      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      apiManager.getEchoBreakpoint(null, new Address(0x123));
      fail();
    } catch (final NullPointerException exception) {
    }

    apiManager.setEchoBreakpoint(module, new Address(0x123));

    final Breakpoint breakpoint = apiManager.getEchoBreakpoint(module, new Address(0x123));

    assertEquals(0x123, breakpoint.getAddress().toLong());

    final Module module = ModuleFactory.get();

    apiManager.setEchoBreakpoint(module, new Address(0x123));

    assertTrue(apiManager.hasEchoBreakpoint(module, new Address(0x123)));

    final Breakpoint breakpoint2 = apiManager.getEchoBreakpoint(module, new Address(0x123));

    assertEquals(0x123, breakpoint2.getAddress().toLong());

    apiManager.removeEchoBreakpoint(module, new Address(0x123));
  }

  @Test
  public void testGetEchoBreakpoints() {
    apiManager.setEchoBreakpoint(module, new Address(0x123));

    final List<Breakpoint> breakpoints = apiManager.getEchoBreakpoints();

    assertEquals(0x123, breakpoints.get(0).getAddress().toLong());
  }

  @Test
  public void testHasBreakpoint() {
    try {
      apiManager.hasBreakpoint(null, null);

      fail();
    } catch (final NullPointerException exception) {
    }

    assertFalse(apiManager.hasBreakpoint(module, new Address(0x123)));

    apiManager.setBreakpoint(module, new Address(0x123));

    assertTrue(apiManager.hasBreakpoint(module, new Address(0x123)));
  }

  @Test
  public void testHasEchoBreakpoint() {
    try {
      apiManager.hasEchoBreakpoint(null, null);

      fail();
    } catch (final NullPointerException exception) {
    }

    assertFalse(apiManager.hasEchoBreakpoint(module, new Address(0x123)));

    apiManager.setEchoBreakpoint(module, new Address(0x123));

    assertTrue(apiManager.hasEchoBreakpoint(module, new Address(0x123)));
  }

  @Test
  public void testPreinitialized() {
    final com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager
        internalManager =
        new com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager();

    internalManager.addBreakpoints(BreakpointType.REGULAR, CommonTestObjects.BP_ADDRESS_0_SET);
    internalManager.addBreakpoints(BreakpointType.ECHO, Sets.newHashSet(
        new BreakpointAddress(CommonTestObjects.MODULE, new UnrelocatedAddress(new CAddress(1)))));

    final BreakpointManager apiManager = new BreakpointManager(internalManager);

    assertEquals(0, apiManager.getBreakpoints().get(0).getAddress().toLong());
    assertEquals(1, apiManager.getEchoBreakpoints().get(0).getAddress().toLong());
  }

  @Test
  public void testRemoveBreakpoint() {
    try {
      apiManager.removeBreakpoint(null, null);
      fail();
    } catch (final NullPointerException exception) {
    }

    apiManager.setBreakpoint(module, new Address(0x123));
    apiManager.setBreakpoint(module, new Address(0x124));

    apiManager.addListener(mockListener);

    apiManager.removeBreakpoint(module, new Address(0x123));
    internalManager.removeBreakpoints(
        BreakpointType.REGULAR, Sets.newHashSet(new BreakpointAddress(CommonTestObjects.MODULE,
            new UnrelocatedAddress(new CAddress(0x124)))));

    assertEquals("removedBreakpoint/124;", mockListener.events);

    assertEquals(1, apiManager.getBreakpoints().size());

    apiManager.removeListener(mockListener);
  }

  @Test
  public void testRemoveEchoBreakpoint() {
    try {
      apiManager.removeEchoBreakpoint(null, null);
      fail();
    } catch (final NullPointerException exception) {
    }

    apiManager.setEchoBreakpoint(module, new Address(0x123));
    apiManager.setEchoBreakpoint(module, new Address(0x124));

    apiManager.addListener(mockListener);

    apiManager.removeEchoBreakpoint(module, new Address(0x123));
    internalManager.removeBreakpoints(BreakpointType.ECHO, Sets.newHashSet(new BreakpointAddress(
        CommonTestObjects.MODULE, new UnrelocatedAddress(new CAddress(0x124)))));

    assertEquals("removedEchoBreakpoint/123;removedEchoBreakpoint/124;", mockListener.events);

    assertEquals(0, apiManager.getBreakpoints().size());

    apiManager.removeListener(mockListener);
  }

  @Test
  public void testSetBreakpoint() {
    apiManager.addListener(mockListener);

    try {
      apiManager.setBreakpoint(null, null);
    } catch (final NullPointerException exception) {
    }

    apiManager.setBreakpoint(module, new Address(0x123));
    assertEquals("addedBreakpoint/123;", mockListener.events);

    try {
      apiManager.setBreakpoint(null, new Address(0x123));
    } catch (final NullPointerException exception) {
    }

    internalManager.addBreakpoints(BreakpointType.REGULAR, Sets.newHashSet(new BreakpointAddress(
        CommonTestObjects.MODULE, new UnrelocatedAddress(new CAddress(0x124)))));

    assertEquals("addedBreakpoint/123;addedBreakpoint/124;", mockListener.events);

    assertTrue(apiManager.hasBreakpoint(module, new Address(0x124)));

    apiManager.removeListener(mockListener);
  }

  @Test
  public void testSetEchoBreakpoint() {
    apiManager.addListener(mockListener);

    try {
      apiManager.setEchoBreakpoint(null, null);
    } catch (final NullPointerException exception) {
    }

    apiManager.setEchoBreakpoint(module, new Address(0x123));
    assertEquals("addedEchoBreakpoint/123;", mockListener.events);

    try {
      apiManager.setEchoBreakpoint(null, new Address(0x123));
    } catch (final NullPointerException exception) {
    }

    internalManager.addBreakpoints(BreakpointType.ECHO, Sets.newHashSet(new BreakpointAddress(
        CommonTestObjects.MODULE, new UnrelocatedAddress(new CAddress(0x124)))));

    assertEquals("addedEchoBreakpoint/123;addedEchoBreakpoint/124;", mockListener.events);

    assertTrue(apiManager.hasEchoBreakpoint(module, new Address(0x124)));

    apiManager.removeListener(mockListener);
  }

  @Test
  public void testToString() {
    final String test = apiManager.toString();

    assertNotNull(test);
  }
}
