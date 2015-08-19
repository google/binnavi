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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Database.CModuleViewGenerator;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.MockTagManager;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.disassembly.CFunction;
import com.google.security.zynamics.binnavi.disassembly.CFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.CInstruction;
import com.google.security.zynamics.binnavi.disassembly.COperandTree;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.MockEdge;
import com.google.security.zynamics.binnavi.disassembly.MockTextNode;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.FunctionType;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.types.trees.Tree;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@RunWith(JUnit4.class)
public final class ViewGraphHelpersTest {
  private CodeNode m_codeNode;
  private FunctionNode m_functionNode;
  private TextNode m_textNode;
  private ViewGraph m_graph;
  private View m_view;

  @Before
  public void setUp() throws CouldntLoadDataException, LoadCancelledException,
  com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException,
  PartialLoadException {
    final MockSqlProvider provider = new MockSqlProvider();

    final TagManager tagManager = new TagManager(new MockTagManager(TagType.NODE_TAG));
    final TagManager viewTagManager =
        new TagManager(new CTagManager(new Tree<CTag>(new TreeNode<CTag>(new CTag(1, "", "",
            TagType.VIEW_TAG, provider))), TagType.VIEW_TAG, provider));

    final Database database = new Database(new MockDatabase());

    final CModule internalModule =
        new CModule(1, "", "", new Date(), new Date(), "00000000000000000000000000000000",
            "0000000000000000000000000000000000000000", 0, 0, new CAddress(0), new CAddress(0),
            null, null, Integer.MAX_VALUE, false, provider);

    internalModule.load();

    final Module module = new Module(database, internalModule, tagManager, viewTagManager);

    final CModuleViewGenerator generator = new CModuleViewGenerator(provider, internalModule);
    final INaviView internalView =
        generator.generate(1, "My View", "My View Description",
            com.google.security.zynamics.zylib.disassembly.ViewType.NonNative,
            GraphType.MIXED_GRAPH, new Date(), new Date(), 1, 2, new HashSet<CTag>(),
            new HashSet<CTag>(), false);

    m_view = new View(module, internalView, tagManager, viewTagManager);
    m_view.load();

    final List<INaviInstruction> instructions = new ArrayList<INaviInstruction>();

    instructions.add(new CInstruction(false, internalModule, new CAddress(0x123), "nop",
        new ArrayList<COperandTree>(), new byte[] {(byte) 0x90}, "x86-32", provider));
    instructions.add(new CInstruction(false, internalModule, new CAddress(0x124), "nop",
        new ArrayList<COperandTree>(), new byte[] {(byte) 0x90}, "x86-32", provider));
    instructions.add(new CInstruction(false, internalModule, new CAddress(0x125), "nop",
        new ArrayList<COperandTree>(), new byte[] {(byte) 0x90}, "x86-32", provider));

    final INaviCodeNode codeNode = internalView.getContent().createCodeNode(null, instructions);

    final List<INaviViewNode> nodes1 = new ArrayList<INaviViewNode>();
    nodes1.add(codeNode);
    final List<INaviEdge> edges1 = new ArrayList<INaviEdge>();
    final CFunction internalFunction = new CFunction(internalModule,
        new MockView(nodes1, edges1, provider), new CAddress(0x123), "Mock Function",
        "Mock Function", "Mock Description", 0, 0, 0, 0, FunctionType.NORMAL, "", 0, null, null,
        null, provider);
    internalFunction.load();

    final Function function = new Function(module, internalFunction);

    final CFunctionNode functionNode =
        new CFunctionNode(0, internalFunction, 0, 0, 0, 0, Color.RED, false, false, null,
            new HashSet<CTag>(), provider);

    m_codeNode = new CodeNode(m_view, codeNode, tagManager);
    m_functionNode = new FunctionNode(m_view, functionNode, function, tagManager);
    m_textNode = new TextNode(m_view, new MockTextNode(), tagManager);

    final List<ViewNode> nodes = Lists.newArrayList(m_codeNode, m_functionNode, m_textNode);
    final List<ViewEdge> edges =
        Lists.newArrayList(new ViewEdge(new MockEdge(1, provider), nodes.get(0), nodes.get(0)));

    m_graph = new ViewGraph(nodes, edges);
  }

  @Test
  public void testGetCodeNode() {
    assertEquals(m_codeNode, ViewGraphHelpers.getCodeNode(m_graph, new Address(0x123)));
    assertNull(ViewGraphHelpers.getCodeNode(m_graph, new Address(0x124)));

    assertEquals(m_codeNode, ViewGraphHelpers.getCodeNode(m_graph, 0x123));
    assertNull(ViewGraphHelpers.getCodeNode(m_graph, 0x124));
  }

  @Test
  public void testGetCodeNodes() {
    final List<CodeNode> codeNodes = ViewGraphHelpers.getCodeNodes(m_graph);

    assertEquals(1, codeNodes.size());
    assertEquals(m_codeNode, codeNodes.get(0));
  }

  @Test
  public void testGetFunctionNode() {
    assertEquals(m_functionNode, ViewGraphHelpers.getFunctionNode(m_graph, "Mock Function"));
    assertNull(ViewGraphHelpers.getFunctionNode(m_graph, "Sock Function"));
  }

  @Test
  public void testGetFunctionNodes() {
    final List<FunctionNode> functionNodes = ViewGraphHelpers.getFunctionNodes(m_graph);

    assertEquals(1, functionNodes.size());
    assertEquals(m_functionNode, functionNodes.get(0));
  }

  @Test
  public void testGetInstruction() {
    assertEquals(m_codeNode.getInstructions().get(0),
        ViewGraphHelpers.getInstruction(m_graph, new Address(0x123)));
    assertNull(ViewGraphHelpers.getInstruction(m_graph, new Address(0x129)));

    assertEquals(m_codeNode.getInstructions().get(0),
        ViewGraphHelpers.getInstruction(m_graph, 0x123));
    assertNull(ViewGraphHelpers.getInstruction(m_graph, 0x129));
  }

  @Test
  public void testInline()
      throws com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException,
      PartialLoadException {
    m_view.load();

    final InliningResult result =
        ViewGraphHelpers.inlineFunctionCall(m_view, m_codeNode,
            m_codeNode.getInstructions().get(1), m_functionNode.getFunction());

    assertNotNull(result.getFirstNode());
    assertNotNull(result.getSecondNode());

    assertEquals(2, result.getFirstNode().getInstructions().size());
    assertEquals(1, result.getSecondNode().getInstructions().size());
  }
}
