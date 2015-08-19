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
package com.google.security.zynamics.binnavi.Debug.Models.Breakpoints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test class for the class CBreakpointManager
 */
@RunWith(JUnit4.class)
public final class CBreakpointManagerTest {
  private BreakpointManager m_manager;

  private final MockModule m_module = CommonTestObjects.MODULE;

  @Before
  public void setUp() {
    m_manager = new BreakpointManager();

    m_manager.addBreakpoints(BreakpointType.REGULAR, CommonTestObjects.BP_ADDRESS_123_SET);
    m_manager.addBreakpoints(BreakpointType.REGULAR, Sets.newHashSet(new BreakpointAddress(
        m_module, new UnrelocatedAddress(new CAddress(0x456)))));
    m_manager.addBreakpoints(BreakpointType.REGULAR, Sets.newHashSet(new BreakpointAddress(
        m_module, new UnrelocatedAddress(new CAddress(0x789)))));

    m_manager.addBreakpoints(BreakpointType.ECHO, Sets.newHashSet(new BreakpointAddress(m_module,
        new UnrelocatedAddress(new CAddress(0x111)))));
    m_manager.addBreakpoints(BreakpointType.ECHO, Sets.newHashSet(new BreakpointAddress(m_module,
        new UnrelocatedAddress(new CAddress(0x222)))));

    m_manager.addBreakpoints(BreakpointType.STEP, Sets.newHashSet(new BreakpointAddress(m_module,
        new UnrelocatedAddress(new CAddress(0x1000)))));
  }

  @Test
  public void testBreakpointOverwriting() {
    final MockBreakpointManagerListener listener = new MockBreakpointManagerListener();
    m_manager.addListener(listener);

    // Echo breakpoints can not overwrite regular breakpoints.
    m_manager.addBreakpoints(BreakpointType.ECHO, CommonTestObjects.BP_ADDRESS_123_SET);

    assertEquals(0, listener.size());
    assertEquals(3, m_manager.getNumberOfBreakpoints(BreakpointType.REGULAR));
    assertEquals(2, m_manager.getNumberOfBreakpoints(BreakpointType.ECHO));
    assertEquals(1, m_manager.getNumberOfBreakpoints(BreakpointType.STEP));

    // Echo breakpoints can not overwrite stepping breakpoints
    m_manager.addBreakpoints(BreakpointType.ECHO, Sets.newHashSet(new BreakpointAddress(m_module,
        new UnrelocatedAddress(new CAddress(0x1000)))));

    assertEquals(0, listener.size());
    assertEquals(3, m_manager.getNumberOfBreakpoints(BreakpointType.REGULAR));
    assertEquals(2, m_manager.getNumberOfBreakpoints(BreakpointType.ECHO));
    assertEquals(1, m_manager.getNumberOfBreakpoints(BreakpointType.STEP));

    // Stepping breakpoints can not overwrite regular breakpoints
    m_manager.addBreakpoints(BreakpointType.STEP, CommonTestObjects.BP_ADDRESS_123_SET);

    assertEquals(0, listener.size());
    assertEquals(3, m_manager.getNumberOfBreakpoints(BreakpointType.REGULAR));
    assertEquals(2, m_manager.getNumberOfBreakpoints(BreakpointType.ECHO));
    assertEquals(1, m_manager.getNumberOfBreakpoints(BreakpointType.STEP));

    // Stepping breakpoints can overwrite echo breakpoints
    m_manager.addBreakpoints(BreakpointType.STEP, Sets.newHashSet(new BreakpointAddress(m_module,
        new UnrelocatedAddress(new CAddress(0x111)))));

    assertEquals(2, listener.size());
    assertEquals("Remove: 00000111", listener.getEvent(0));
    assertEquals("Add: 00000111", listener.getEvent(1));
    assertEquals(3, m_manager.getNumberOfBreakpoints(BreakpointType.REGULAR));
    assertEquals(1, m_manager.getNumberOfBreakpoints(BreakpointType.ECHO));
    assertEquals(2, m_manager.getNumberOfBreakpoints(BreakpointType.STEP));

    // Regular breakpoints can overwrite stepping breakpoints
    m_manager.addBreakpoints(BreakpointType.REGULAR, Sets.newHashSet(new BreakpointAddress(
        m_module, new UnrelocatedAddress(new CAddress(0x1000)))));

    assertEquals(4, listener.size());
    assertEquals("Remove: 00001000", listener.getEvent(2));
    assertEquals("Add: 00001000", listener.getEvent(3));
    assertEquals(4, m_manager.getNumberOfBreakpoints(BreakpointType.REGULAR));
    assertEquals(1, m_manager.getNumberOfBreakpoints(BreakpointType.ECHO));
    assertEquals(1, m_manager.getNumberOfBreakpoints(BreakpointType.STEP));

    // Regular breakpoints can overwrite echo breakpoints
    m_manager.addBreakpoints(BreakpointType.REGULAR, Sets.newHashSet(new BreakpointAddress(
        m_module, new UnrelocatedAddress(new CAddress(0x222)))));

    assertEquals(6, listener.size());
    assertEquals("Remove: 00000222", listener.getEvent(4));
    assertEquals("Add: 00000222", listener.getEvent(5));
    assertEquals(5, m_manager.getNumberOfBreakpoints(BreakpointType.REGULAR));
    assertEquals(0, m_manager.getNumberOfBreakpoints(BreakpointType.ECHO));
    assertEquals(1, m_manager.getNumberOfBreakpoints(BreakpointType.STEP));
  }

  @Test
  public void testClearEchoBreakpointsPassive() {
    assertEquals(2, m_manager.getNumberOfBreakpoints(BreakpointType.ECHO));

    final MockBreakpointManagerListener listener = new MockBreakpointManagerListener();
    m_manager.addListener(listener);

    m_manager.clearBreakpointsPassive(BreakpointType.ECHO);

    assertEquals(0, m_manager.getNumberOfBreakpoints(BreakpointType.ECHO));

    // Make sure that no message was sent
    assertEquals(0, listener.size());
  }

  @Test
  public void testGetBreakpoint() {
    assertEquals(new CAddress(new CAddress(0x123)),
        m_manager.getBreakpoint(BreakpointType.REGULAR, 0).getAddress().getAddress().getAddress());
    assertEquals(new CAddress(new CAddress(0x456)),
        m_manager.getBreakpoint(BreakpointType.REGULAR, 1).getAddress().getAddress().getAddress());

    // Error condition: Invalid index
    try {
      m_manager.getBreakpoint(BreakpointType.REGULAR, -1).getAddress();
      fail("Exception not raised");
    } catch (final IllegalArgumentException ex) {
    }
  }

  @Test
  public void testGetBreakpoint2() {
    assertEquals(BreakpointStatus.BREAKPOINT_INACTIVE, m_manager.getBreakpointStatus(
        new BreakpointAddress(m_module, new UnrelocatedAddress(new CAddress(0x123))),
        BreakpointType.REGULAR));
    assertEquals(BreakpointStatus.BREAKPOINT_INACTIVE, m_manager.getBreakpointStatus(
        new BreakpointAddress(m_module, new UnrelocatedAddress(new CAddress(0x456))),
        BreakpointType.REGULAR));

    assertEquals(null, m_manager.getBreakpointStatus(null, BreakpointType.REGULAR));

    assertEquals(null, m_manager.getBreakpointStatus(new BreakpointAddress(m_module,
        new UnrelocatedAddress(new CAddress(0))), BreakpointType.REGULAR));
  }

  @Test
  public void testGetBreakpointDescription() {
    try {
      m_manager.getBreakpoint(BreakpointType.REGULAR, -1).setDescription("Argl");
      fail();
    } catch (final IllegalArgumentException exception) {
    }

    m_manager.getBreakpoint(BreakpointType.REGULAR, 0).setDescription(null);

    m_manager.getBreakpoint(BreakpointType.REGULAR, 0).setDescription("Hannes");

    assertEquals("Hannes", m_manager.getBreakpoint(BreakpointType.REGULAR, 0).getDescription());
  }

  @Test
  public void testGetBreakpointStatus() {
    assertEquals(BreakpointStatus.BREAKPOINT_INACTIVE,
        m_manager.getBreakpointStatus(BreakpointType.REGULAR, 0));
    assertEquals(BreakpointStatus.BREAKPOINT_INACTIVE,
        m_manager.getBreakpointStatus(BreakpointType.REGULAR, 1));

    // Error condition: Invalid index
    try {
      m_manager.getBreakpointStatus(BreakpointType.REGULAR, -1);
      fail("Exception not raised");
    } catch (final IllegalArgumentException ex) {
    }
  }

  @Test
  public void testRemoveEchoBreakpoint() {
    // Error condition: Null argument
    try {
      m_manager.removeBreakpoints(BreakpointType.ECHO, null);
      fail("Exception not raised");
    } catch (final NullPointerException ex) {
    }

    assertEquals(2, m_manager.getNumberOfBreakpoints(BreakpointType.ECHO));

    m_manager.removeBreakpoints(BreakpointType.ECHO, Sets.newHashSet(new BreakpointAddress(
        m_module, new UnrelocatedAddress(new CAddress(0x111)))));

    assertEquals(1, m_manager.getNumberOfBreakpoints(BreakpointType.ECHO));

    m_manager.removeBreakpoints(BreakpointType.ECHO, Sets.newHashSet(new BreakpointAddress(
        m_module, new UnrelocatedAddress(new CAddress(0x222)))));

    assertEquals(0, m_manager.getNumberOfBreakpoints(BreakpointType.ECHO));
  }

  @Test
  public void testRemoveRegularBreakpoint() {
    // Error condition: Null argument
    try {
      m_manager.removeBreakpoints(BreakpointType.REGULAR, null);
      fail("Exception not raised");
    } catch (final NullPointerException ex) {
    }

    assertEquals(3, m_manager.getNumberOfBreakpoints(BreakpointType.REGULAR));

    m_manager.removeBreakpoints(BreakpointType.REGULAR, Sets.newHashSet(new BreakpointAddress(
        m_module, new UnrelocatedAddress(new CAddress(0x123)))));

    assertEquals(2, m_manager.getNumberOfBreakpoints(BreakpointType.REGULAR));

    m_manager.removeBreakpoints(BreakpointType.REGULAR, Sets.newHashSet(new BreakpointAddress(
        m_module, new UnrelocatedAddress(new CAddress(0x456)))));

    assertEquals(1, m_manager.getNumberOfBreakpoints(BreakpointType.REGULAR));

    m_manager.removeBreakpoints(BreakpointType.REGULAR, Sets.newHashSet(new BreakpointAddress(
        m_module, new UnrelocatedAddress(new CAddress(0x789)))));

    assertEquals(0, m_manager.getNumberOfBreakpoints(BreakpointType.REGULAR));
  }

  @Test
  public void testRemoveSteppingBreakpoint() {
    assertEquals(1, m_manager.getNumberOfBreakpoints(BreakpointType.STEP));

    final MockBreakpointManagerListener listener = new MockBreakpointManagerListener();
    m_manager.addListener(listener);

    m_manager.removeBreakpoints(BreakpointType.STEP, Sets.newHashSet(new BreakpointAddress(
        m_module, new UnrelocatedAddress(new CAddress(0x1000)))));

    // The breakpoint was removed
    assertEquals(0, m_manager.getNumberOfBreakpoints(BreakpointType.STEP));

    // Make sure that the message was sent
    assertEquals(1, listener.size());
  }

  @Test
  public void testSetup() {
    assertEquals(3, m_manager.getNumberOfBreakpoints(BreakpointType.REGULAR));
    assertEquals(2, m_manager.getNumberOfBreakpoints(BreakpointType.ECHO));
    assertEquals(1, m_manager.getNumberOfBreakpoints(BreakpointType.STEP));
  }

  @Test
  public void testStepEchoBreakpointsPassive() {
    assertEquals(1, m_manager.getNumberOfBreakpoints(BreakpointType.STEP));

    final MockBreakpointManagerListener listener = new MockBreakpointManagerListener();
    m_manager.addListener(listener);

    m_manager.clearBreakpointsPassive(BreakpointType.STEP);

    assertEquals(0, m_manager.getNumberOfBreakpoints(BreakpointType.STEP));

    // Make sure that no message was sent
    assertEquals(0, listener.size());
  }
}
