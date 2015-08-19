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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Debug.Debugger.MockDebugger;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.debug.debugger.DebugTargetSettings;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.ModuleTargetSettings;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.CFunction;
import com.google.security.zynamics.binnavi.disassembly.CFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.CInstruction;
import com.google.security.zynamics.binnavi.disassembly.COperandTree;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.FunctionType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@RunWith(JUnit4.class)
public final class CGraphDebuggerTest {
  private final MockSqlProvider m_provider = new MockSqlProvider();

  private final CAddress m_fileBase = new CAddress(0);
  private final CAddress m_imageBase = new CAddress(0x1000);

  private final CModule m_module = new CModule(123,
      "Name",
      "Comment",
      new Date(),
      new Date(),
      CommonTestObjects.MD5,
      CommonTestObjects.SHA1,
      55,
      66,
      m_fileBase,
      m_imageBase,
      new DebuggerTemplate(1, "Mock Debugger", "localhaus", 88, m_provider),
      null,
      Integer.MAX_VALUE,
      false,
      m_provider);

  private final MockDebugger m_debugger = new MockDebugger(new ModuleTargetSettings(m_module));

  @Before
  public void setUp() {
    m_debugger.setAddressTranslator(m_module, m_fileBase, m_imageBase);
  }

  @Test
  public void testGetBreakpointStatus() {
    m_debugger.getBreakpointManager().addBreakpoints(BreakpointType.REGULAR, Sets.newHashSet(
        new BreakpointAddress(m_module, new UnrelocatedAddress(new CAddress(0x123)))));

    assertEquals(BreakpointStatus.BREAKPOINT_INACTIVE, CGraphDebugger.getBreakpointStatus(
        m_debugger.getBreakpointManager(), m_module, new UnrelocatedAddress(new CAddress(0x123))));
  }

  @Test
  public void testGetDebugger() {
    final MockModule module = new MockModule();
    module.getConfiguration().setDebugger(m_debugger);

    final DebugTargetSettings target = new ModuleTargetSettings(module);
    final DebuggerProvider debuggerProvider = new DebuggerProvider(target);
    debuggerProvider.addDebugger(m_debugger);

    final CFunction function = new CFunction(module,
        new MockView(),
        new CAddress(0x123),
        "Mock Function",
        "Mock Function",
        "Mock Description",
        0,
        0,
        0,
        0,
        FunctionType.NORMAL,
        "",
        0,
        null,
        null,
        null, m_provider);
    final CFunctionNode functionNode = new CFunctionNode(0,
        function,
        0,
        0,
        0,
        0,
        Color.RED,
        false,
        false,
        null,
        new HashSet<CTag>(),
        m_provider);

    assertEquals(m_debugger, CGraphDebugger.getDebugger(debuggerProvider, functionNode));
  }

  @Test
  public void testGetDebugger2() {
    final MockModule module = new MockModule();
    module.getConfiguration().setDebugger(m_debugger);

    final DebugTargetSettings target = new ModuleTargetSettings(module);
    final DebuggerProvider debuggerProvider = new DebuggerProvider(target);
    debuggerProvider.addDebugger(m_debugger);

    final CFunction function = new CFunction(module,
        new MockView(),
        new CAddress(0x123),
        "Mock Function",
        "Mock Function",
        "Mock Description",
        0,
        0,
        0,
        0,
        FunctionType.NORMAL,
        "",
        0,
        null,
        null,
        null, m_provider);

    final ArrayList<IComment> comments = Lists.<IComment>newArrayList(
        new CComment(null, CommonTestObjects.TEST_USER_1, null, "Mock Comment"));

    final INaviCodeNode codeNode = new CCodeNode(0,
        0,
        0,
        0,
        0,
        Color.RED,
        Color.RED,
        false,
        false,
        comments,
        function,
        new HashSet<CTag>(),
        new MockSqlProvider());
    codeNode.addInstruction(new CInstruction(true,
        module,
        new CAddress(0x123),
        "nop",
        new ArrayList<COperandTree>(),
        new byte[] {(byte) 0x90},
        "x86-32",
        m_provider), null);

    assertEquals(m_debugger, CGraphDebugger.getDebugger(debuggerProvider,
        Iterables.getFirst(codeNode.getInstructions(), null)));
  }

  @Test
  public void testHasBreakpoint() {
    m_debugger.getBreakpointManager().addBreakpoints(BreakpointType.REGULAR, Sets.newHashSet(
        new BreakpointAddress(m_module, new UnrelocatedAddress(new CAddress(0x123)))));

    assertTrue(CGraphDebugger.hasBreakpoint(m_debugger.getBreakpointManager(), m_module,
        new UnrelocatedAddress(new CAddress(0x123))));
    assertFalse(CGraphDebugger.hasBreakpoint(m_debugger.getBreakpointManager(), m_module,
        new UnrelocatedAddress(new CAddress(0x124))));
  }

  @Test
  public void testRemoveBreakpoint() {
    final BreakpointManager manager = new BreakpointManager();

    manager.addBreakpoints(BreakpointType.REGULAR, CommonTestObjects.BP_ADDRESS_123_SET);
    manager.addBreakpoints(BreakpointType.REGULAR, CommonTestObjects.BP_ADDRESS_456_SET);

    manager.setBreakpointStatus(BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_ACTIVE, 1);

    CGraphDebugger.removeBreakpoints(CommonTestObjects.BP_ADDRESS_123_SET, manager);
    CGraphDebugger.removeBreakpoints(CommonTestObjects.BP_ADDRESS_456_SET, manager);

    assertEquals(1, manager.getNumberOfBreakpoints(BreakpointType.REGULAR));
    assertEquals(0x123, manager
        .getBreakpoint(BreakpointType.REGULAR, 0)
        .getAddress()
        .getAddress()
        .getAddress()
        .toLong());
    assertEquals(BreakpointStatus.BREAKPOINT_DELETING,
        manager.getBreakpointStatus(BreakpointType.REGULAR, 1));
  }

  @Test
  public void testRemoveBreakpoint3() {
    final Set<BreakpointAddress> breakpointAddresses = Sets.newHashSet(
        new BreakpointAddress(m_module, new UnrelocatedAddress(new CAddress(0x1123))),
        new BreakpointAddress(m_module, new UnrelocatedAddress(new CAddress(0x1124))));
    m_debugger.getBreakpointManager().addBreakpoints(BreakpointType.REGULAR, breakpointAddresses);
    m_debugger.getBreakpointManager().setBreakpointStatus(BreakpointType.REGULAR,
        BreakpointStatus.BREAKPOINT_ACTIVE, 1);
    CGraphDebugger.removeBreakpoint(m_debugger.getBreakpointManager(), m_module,
        new UnrelocatedAddress(new CAddress(0x1123)));
    CGraphDebugger.removeBreakpoint(m_debugger.getBreakpointManager(), m_module,
        new UnrelocatedAddress(new CAddress(0x1124)));
    assertEquals(0,
        m_debugger.getBreakpointManager().getNumberOfBreakpoints(BreakpointType.REGULAR));
  }

  @Test
  public void testSetBreakpoint() {
    CGraphDebugger.setBreakpoint(m_debugger.getBreakpointManager(), m_module,
        new UnrelocatedAddress(new CAddress(0x123)));
    CGraphDebugger.setBreakpoint(m_debugger.getBreakpointManager(), m_module,
        new UnrelocatedAddress(new CAddress(0x124)));

    assertEquals(2,
        m_debugger.getBreakpointManager().getNumberOfBreakpoints(BreakpointType.REGULAR));
    assertEquals(0x123, m_debugger
        .getBreakpointManager()
        .getBreakpoint(BreakpointType.REGULAR, 0)
        .getAddress()
        .getAddress()
        .getAddress()
        .toLong());
    assertEquals(BreakpointStatus.BREAKPOINT_INACTIVE,
        m_debugger.getBreakpointManager().getBreakpointStatus(BreakpointType.REGULAR, 0));
    assertEquals(0x124, m_debugger
        .getBreakpointManager()
        .getBreakpoint(BreakpointType.REGULAR, 1)
        .getAddress()
        .getAddress()
        .getAddress()
        .toLong());
    assertEquals(BreakpointStatus.BREAKPOINT_INACTIVE,
        m_debugger.getBreakpointManager().getBreakpointStatus(BreakpointType.REGULAR, 1));
  }

  @Test
  public void testToggleBreakpoint() {
    CGraphDebugger.toggleBreakpoint(m_debugger.getBreakpointManager(), m_module,
        new UnrelocatedAddress(new CAddress(0x123)));

    assertEquals(1,
        m_debugger.getBreakpointManager().getNumberOfBreakpoints(BreakpointType.REGULAR));
    assertEquals(0x123, m_debugger
        .getBreakpointManager()
        .getBreakpoint(BreakpointType.REGULAR, 0)
        .getAddress()
        .getAddress()
        .getAddress()
        .toLong());
    assertEquals(BreakpointStatus.BREAKPOINT_INACTIVE,
        m_debugger.getBreakpointManager().getBreakpointStatus(BreakpointType.REGULAR, 0));

    CGraphDebugger.toggleBreakpoint(m_debugger.getBreakpointManager(), m_module,
        new UnrelocatedAddress(new CAddress(0x123)));

    assertEquals(0,
        m_debugger.getBreakpointManager().getNumberOfBreakpoints(BreakpointType.REGULAR));
  }

  @Test
  public void testToggleBreakpoint2() {
    final MockModule module = new MockModule();
    module.getConfiguration().setDebugger(m_debugger);

    m_debugger.setAddressTranslator(module, m_fileBase, m_imageBase);

    final DebugTargetSettings target = new ModuleTargetSettings(module);
    final DebuggerProvider debuggerProvider = new DebuggerProvider(target);
    debuggerProvider.addDebugger(m_debugger);

    final CFunction function = new CFunction(module,
        new MockView(),
        new CAddress(0x123),
        "Mock Function",
        "Mock Function",
        "Mock Description",
        0,
        0,
        0,
        0,
        FunctionType.NORMAL,
        "",
        0,
        null,
        null,
        null, m_provider);
    final CFunctionNode functionNode = new CFunctionNode(0,
        function,
        0,
        0,
        0,
        0,
        Color.RED,
        false,
        false,
        null,
        new HashSet<CTag>(),
        m_provider);

    CGraphDebugger.toggleBreakpoint(debuggerProvider, functionNode);

    assertEquals(1,
        m_debugger.getBreakpointManager().getNumberOfBreakpoints(BreakpointType.REGULAR));
    assertEquals(0x123, m_debugger
        .getBreakpointManager()
        .getBreakpoint(BreakpointType.REGULAR, 0)
        .getAddress()
        .getAddress()
        .getAddress()
        .toLong());
    assertEquals(BreakpointStatus.BREAKPOINT_INACTIVE,
        m_debugger.getBreakpointManager().getBreakpointStatus(BreakpointType.REGULAR, 0));

    CGraphDebugger.toggleBreakpoint(debuggerProvider, functionNode);

    assertEquals(0,
        m_debugger.getBreakpointManager().getNumberOfBreakpoints(BreakpointType.REGULAR));
  }

  @Test
  public void testToggleBreakpoint3() {
    final MockModule module = new MockModule();
    module.getConfiguration().setDebugger(m_debugger);

    m_debugger.setAddressTranslator(module, m_fileBase, m_imageBase);

    final DebugTargetSettings target = new ModuleTargetSettings(module);
    final DebuggerProvider debuggerProvider = new DebuggerProvider(target);
    debuggerProvider.addDebugger(m_debugger);

    final CFunction function = new CFunction(module,
        new MockView(),
        new CAddress(0x123),
        "Mock Function",
        "Mock Function",
        "Mock Description",
        0,
        0,
        0,
        0,
        FunctionType.NORMAL,
        "",
        0,
        null,
        null,
        null, m_provider);

    final ArrayList<IComment> comments = Lists.<IComment>newArrayList(
        new CComment(null, CommonTestObjects.TEST_USER_1, null, "Mock Comment"));

    final INaviCodeNode codeNode = new CCodeNode(0,
        0,
        0,
        0,
        0,
        Color.RED,
        Color.RED,
        false,
        false,
        comments,
        function,
        new HashSet<CTag>(),
        new MockSqlProvider());
    codeNode.addInstruction(new CInstruction(true,
        module,
        new CAddress(0x123),
        "nop",
        new ArrayList<COperandTree>(),
        new byte[] {(byte) 0x90},
        "x86-32",
        m_provider), null);
    codeNode.addInstruction(new CInstruction(true,
        module,
        new CAddress(0x124),
        "nop",
        new ArrayList<COperandTree>(),
        new byte[] {(byte) 0x90},
        "x86-32",
        m_provider), null);

    CGraphDebugger.toggleBreakpoint(debuggerProvider, codeNode, 2);

    assertEquals(1,
        m_debugger.getBreakpointManager().getNumberOfBreakpoints(BreakpointType.REGULAR));
    assertEquals(0x124, m_debugger
        .getBreakpointManager()
        .getBreakpoint(BreakpointType.REGULAR, 0)
        .getAddress()
        .getAddress()
        .getAddress()
        .toLong());
    assertEquals(BreakpointStatus.BREAKPOINT_INACTIVE,
        m_debugger.getBreakpointManager().getBreakpointStatus(BreakpointType.REGULAR, 0));

    CGraphDebugger.toggleBreakpoint(debuggerProvider, codeNode, 2);

    assertEquals(0,
        m_debugger.getBreakpointManager().getNumberOfBreakpoints(BreakpointType.REGULAR));
  }

  @Test
  public void testToggleBreakpointStatus() {
    CGraphDebugger.toggleBreakpoint(m_debugger.getBreakpointManager(), m_module,
        new UnrelocatedAddress(new CAddress(0x123)));

    assertEquals(1,
        m_debugger.getBreakpointManager().getNumberOfBreakpoints(BreakpointType.REGULAR));
    assertEquals(0x123, m_debugger
        .getBreakpointManager()
        .getBreakpoint(BreakpointType.REGULAR, 0)
        .getAddress()
        .getAddress()
        .getAddress()
        .toLong());
    assertEquals(BreakpointStatus.BREAKPOINT_INACTIVE,
        m_debugger.getBreakpointManager().getBreakpointStatus(BreakpointType.REGULAR, 0));

    CGraphDebugger.toggleBreakpointStatus(m_debugger.getBreakpointManager(), m_module,
        new UnrelocatedAddress(new CAddress(0x123)));

    assertEquals(1,
        m_debugger.getBreakpointManager().getNumberOfBreakpoints(BreakpointType.REGULAR));
    assertEquals(0x123, m_debugger
        .getBreakpointManager()
        .getBreakpoint(BreakpointType.REGULAR, 0)
        .getAddress()
        .getAddress()
        .getAddress()
        .toLong());
    assertEquals(BreakpointStatus.BREAKPOINT_DISABLED,
        m_debugger.getBreakpointManager().getBreakpointStatus(BreakpointType.REGULAR, 0));

    CGraphDebugger.toggleBreakpointStatus(m_debugger.getBreakpointManager(), m_module,
        new UnrelocatedAddress(new CAddress(0x123)));

    assertEquals(1,
        m_debugger.getBreakpointManager().getNumberOfBreakpoints(BreakpointType.REGULAR));
    assertEquals(0x123, m_debugger
        .getBreakpointManager()
        .getBreakpoint(BreakpointType.REGULAR, 0)
        .getAddress()
        .getAddress()
        .getAddress()
        .toLong());
    assertEquals(BreakpointStatus.BREAKPOINT_INACTIVE,
        m_debugger.getBreakpointManager().getBreakpointStatus(BreakpointType.REGULAR, 0));
  }
}
