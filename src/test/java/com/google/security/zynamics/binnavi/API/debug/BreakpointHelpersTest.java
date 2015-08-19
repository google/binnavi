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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.API.disassembly.CodeNode;
import com.google.security.zynamics.binnavi.API.disassembly.Database;
import com.google.security.zynamics.binnavi.API.disassembly.Function;
import com.google.security.zynamics.binnavi.API.disassembly.Instruction;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.API.disassembly.ModuleFactory;
import com.google.security.zynamics.binnavi.API.disassembly.TagManager;
import com.google.security.zynamics.binnavi.API.disassembly.View;
import com.google.security.zynamics.binnavi.API.disassembly.ViewContainer;
import com.google.security.zynamics.binnavi.Database.CDatabase;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Debug.Debugger.MockDebugger;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.DebugTargetSettings;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.disassembly.CInstruction;
import com.google.security.zynamics.binnavi.disassembly.COperandTree;
import com.google.security.zynamics.binnavi.disassembly.COperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;
import com.google.security.zynamics.zylib.disassembly.IReference;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;
import com.google.security.zynamics.zylib.types.trees.Tree;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(JUnit4.class)
public final class BreakpointHelpersTest {
  private Debugger m_debugger;
  private CodeNode m_node;
  private View m_view;
  private Object m_functionNode;

  private CModule m_module;
  private DebugTargetSettings m_moduleDebugSettings;
  private MockDebugger m_mockDebugger;

  public Object getM_functionNode() {
    return m_functionNode;
  }

  public void setM_functionNode(final Object m_functionNode) {
    this.m_functionNode = m_functionNode;
  }

  @Before
  public void setUp() throws DebugExceptionWrapper, CouldntLoadDataException,
      LoadCancelledException, FileReadException {
    ConfigManager.instance().read();

    final CDatabase database = new CDatabase("", "", "", "", "", "", "", false, false);

    final Database apiDatabase = new Database(database);

    final SQLProvider mockProvider = new MockSqlProvider();

    final ITreeNode<CTag> nodeRootNode =
        new TreeNode<CTag>(new CTag(0, "", "", TagType.NODE_TAG, mockProvider));
    final Tree<CTag> nodeTagTree = new Tree<CTag>(nodeRootNode);
    final TagManager nodeTagManager =
        new TagManager(new CTagManager(nodeTagTree, TagType.NODE_TAG, mockProvider));

    final ITreeNode<CTag> viewRootNode =
        new TreeNode<CTag>(new CTag(0, "", "", TagType.VIEW_TAG, mockProvider));
    final Tree<CTag> viewTagTree = new Tree<CTag>(viewRootNode);
    final TagManager viewTagManager =
        new TagManager(new CTagManager(viewTagTree, TagType.VIEW_TAG, mockProvider));

    m_module =
        new CModule(1, "", "", new Date(), new Date(), "00000000000000000000000000000000",
            "0000000000000000000000000000000000000000", 0, 0, new CAddress(0), new CAddress(0),
            null, null, Integer.MAX_VALUE, false, mockProvider);
    m_module.load();

    m_mockDebugger = new MockDebugger(m_moduleDebugSettings);
    m_mockDebugger.connect();
    m_debugger = new Debugger(m_mockDebugger);

    final INaviFunction parentFunction =
        m_module.getContent().getFunctionContainer().getFunctions().get(0);

    m_mockDebugger.setAddressTranslator(m_module, new CAddress(0), new CAddress(0x1000));

    final ViewContainer viewContainer =
        new Module(apiDatabase, m_module, nodeTagManager, viewTagManager);

    final INaviView naviView = new MockView(mockProvider);

    final Function apiFunction = new Function(ModuleFactory.get(), parentFunction);

    final COperandTreeNode rootNode1 =
        new COperandTreeNode(1, IOperandTree.NODE_TYPE_REGISTER_ID, "eax", null,
            new ArrayList<IReference>(), mockProvider, m_module.getTypeManager(), m_module
                .getContent().getTypeInstanceContainer());
    final COperandTreeNode rootNode2 =
        new COperandTreeNode(1, IOperandTree.NODE_TYPE_REGISTER_ID, "ebx", null,
            new ArrayList<IReference>(), mockProvider, m_module.getTypeManager(), m_module
                .getContent().getTypeInstanceContainer());

    final COperandTree operand1 =
        new COperandTree(rootNode1, mockProvider, m_module.getTypeManager(), m_module.getContent()
            .getTypeInstanceContainer());
    final COperandTree operand2 =
        new COperandTree(rootNode2, mockProvider, m_module.getTypeManager(), m_module.getContent()
            .getTypeInstanceContainer());
    final List<COperandTree> operands = Lists.newArrayList(operand1, operand2);
    final CInstruction internalInstruction =
        new CInstruction(true, m_module, new CAddress(0x1234), "mov", operands,
            new byte[] {1, 2, 3}, "x86-32", mockProvider);

    m_view = new View(viewContainer, naviView, nodeTagManager, viewTagManager);
    m_node =
        m_view
            .createCodeNode(apiFunction, Lists.newArrayList(new Instruction(internalInstruction)));
    setM_functionNode(m_view.createFunctionNode(apiFunction));
  }

  @After
  public void tearDown() {
    m_mockDebugger.close();
  }

  @Test
  public void testGetBreakpointsNode() {
    assertTrue(BreakpointHelpers.getBreakpoints(m_debugger, m_node).isEmpty());

    m_debugger
        .getBreakpointManager()
        .getNative()
        .addBreakpoints(
            BreakpointType.REGULAR,
            Sets.newHashSet(new BreakpointAddress(m_module, new UnrelocatedAddress(new CAddress(
                0x1234)))));

    final List<Address> breakpoints = BreakpointHelpers.getBreakpoints(m_debugger, m_node);

    assertEquals(1, breakpoints.size());
    assertEquals(0x1234, breakpoints.get(0).toLong());

    try {
      BreakpointHelpers.getBreakpoints(null, m_node);
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      BreakpointHelpers.getBreakpoints(m_debugger, (CodeNode) null);
      fail();
    } catch (final NullPointerException exception) {
    }
  }

  @Test
  public void testGetBreakpointsView() {
    assertTrue(BreakpointHelpers.getBreakpoints(m_debugger, m_view).isEmpty());

    m_debugger
        .getBreakpointManager()
        .getNative()
        .addBreakpoints(
            BreakpointType.REGULAR,
            Sets.newHashSet(new BreakpointAddress(m_module, new UnrelocatedAddress(new CAddress(
                0x1234)))));
    m_debugger
        .getBreakpointManager()
        .getNative()
        .addBreakpoints(
            BreakpointType.REGULAR,
            Sets.newHashSet(new BreakpointAddress(m_module, new UnrelocatedAddress(new CAddress(
                0x123)))));

    final List<Address> breakpoints = BreakpointHelpers.getBreakpoints(m_debugger, m_view);

    assertEquals(2, breakpoints.size());
    assertEquals(0x1234, breakpoints.get(0).toLong());
    assertEquals(0x123, breakpoints.get(1).toLong());

    try {
      BreakpointHelpers.getBreakpoints(null, m_view);
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      BreakpointHelpers.getBreakpoints(m_debugger, (View) null);
      fail();
    } catch (final NullPointerException exception) {
    }
  }

  @Test
  public void testGetEchoBreakpointsNode() {
    assertTrue(BreakpointHelpers.getEchoBreakpoints(m_debugger, m_node).isEmpty());

    m_debugger
        .getBreakpointManager()
        .getNative()
        .addBreakpoints(
            BreakpointType.ECHO,
            Sets.newHashSet(new BreakpointAddress(m_module, new UnrelocatedAddress(new CAddress(
                0x1234)))));

    final List<Address> breakpoints = BreakpointHelpers.getEchoBreakpoints(m_debugger, m_node);

    assertEquals(1, breakpoints.size());
    assertEquals(0x1234, breakpoints.get(0).toLong());

    try {
      BreakpointHelpers.getEchoBreakpoints(null, m_node);
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      BreakpointHelpers.getEchoBreakpoints(m_debugger, (CodeNode) null);
      fail();
    } catch (final NullPointerException exception) {
    }
  }

  @Test
  public void testGetEchoBreakpointsView() {
    assertTrue(BreakpointHelpers.getEchoBreakpoints(m_debugger, m_view).isEmpty());

    m_debugger
        .getBreakpointManager()
        .getNative()
        .addBreakpoints(
            BreakpointType.ECHO,
            Sets.newHashSet(new BreakpointAddress(m_module, new UnrelocatedAddress(new CAddress(
                0x1234)))));
    m_debugger
        .getBreakpointManager()
        .getNative()
        .addBreakpoints(
            BreakpointType.ECHO,
            Sets.newHashSet(new BreakpointAddress(m_module, new UnrelocatedAddress(new CAddress(
                0x123)))));

    final List<Address> breakpoints = BreakpointHelpers.getEchoBreakpoints(m_debugger, m_view);

    assertEquals(2, breakpoints.size());
    assertEquals(0x1234, breakpoints.get(0).toLong());
    assertEquals(0x123, breakpoints.get(1).toLong());

    try {
      BreakpointHelpers.getEchoBreakpoints(null, m_view);
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      BreakpointHelpers.getEchoBreakpoints(m_debugger, (View) null);
      fail();
    } catch (final NullPointerException exception) {
    }
  }
}
