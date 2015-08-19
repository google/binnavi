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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.API.reil.InternalTranslationException;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Database.CModuleViewGenerator;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.MockTagManager;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.CFunction;
import com.google.security.zynamics.binnavi.disassembly.CFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.CGroupNode;
import com.google.security.zynamics.binnavi.disassembly.CTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.MockInstruction;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.FunctionType;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.types.graphs.MutableDirectedGraph;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;
import com.google.security.zynamics.zylib.types.trees.Tree;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@RunWith(JUnit4.class)
public final class ViewTest {
  private View m_view;
  private Date m_creationDate;
  private Date m_modificationDate;

  private final SQLProvider m_provider = new MockSqlProvider();
  private TagManager m_viewTagManager;
  private CModule internalModule;
  private Module module;

  @SuppressWarnings("deprecation")
  @Before
  public void setUp() throws LoadCancelledException,
      com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException,
      FileReadException, CouldntLoadDataException {
    ConfigManager.instance().read();

    final TagManager tagManager = new TagManager(new MockTagManager(TagType.NODE_TAG));

    final CTagManager internalTagManager = new CTagManager(
        new Tree<CTag>(new TreeNode<CTag>(new CTag(1, "", "", TagType.VIEW_TAG, m_provider))),
        TagType.VIEW_TAG, m_provider);

    m_viewTagManager = new TagManager(internalTagManager);

    final Database database = new Database(new MockDatabase());

    internalModule = new CModule(1,
        "",
        "",
        new Date(),
        new Date(),
        "00000000000000000000000000000000",
        "0000000000000000000000000000000000000000",
        0,
        0,
        new CAddress(0),
        new CAddress(0),
        null,
        null,
        Integer.MAX_VALUE,
        false,
        m_provider);

    internalModule.load();

    module = new Module(database, internalModule, tagManager, m_viewTagManager);

    final Calendar cal = Calendar.getInstance();
    cal.setTime(module.getModificationDate());
    cal.add(Calendar.DATE, 1);
    m_modificationDate = cal.getTime();

    m_creationDate = new Date();

    final ITreeNode<CTag> tag = internalTagManager.addTag(internalTagManager.getRootTag(), "foo");
    final CModuleViewGenerator generator = new CModuleViewGenerator(m_provider, internalModule);
    final INaviView internalView = generator.generate(1,
        "My View",
        "My View Description",
        com.google.security.zynamics.zylib.disassembly.ViewType.NonNative,
        GraphType.MIXED_GRAPH,
        m_creationDate,
        m_modificationDate,
        1,
        2,
        Sets.newHashSet(tag.getObject()),
        new HashSet<CTag>(),
        false);

    m_view = new View(module, internalView, tagManager, m_viewTagManager);
  }

  @Test
  public void testConstructor()
      throws com.google.security.zynamics.binnavi.API.disassembly.PartialLoadException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException {
    final MockViewListener listener = new MockViewListener();

    assertEquals("View 'My View'", m_view.toString());
    assertEquals("My View", m_view.getName());
    assertEquals("My View Description", m_view.getDescription());
    assertEquals(ViewType.NonNative, m_view.getType());
    assertEquals(1, m_view.getNodeCount());
    assertEquals(2, m_view.getEdgeCount());

    m_view.addListener(listener);

    m_view.load();
    assertTrue(m_view.isLoaded());

    assertTrue(m_view.close());
    assertFalse(m_view.isLoaded());

    assertEquals(listener.events, "closingView;closedView;");

    m_view.removeListener(listener);
  }

  @Test
  public void testCreateCodeNode() throws PartialLoadException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException {
    final MockViewListener listener = new MockViewListener();

    m_view.addListener(listener);

    final CModule internalModule = new CModule(1,
        "",
        "",
        new Date(),
        new Date(),
        "00000000000000000000000000000000",
        "0000000000000000000000000000000000000000",
        0,
        0,
        new CAddress(0),
        new CAddress(0),
        null,
        null,
        Integer.MAX_VALUE,
        false,
        m_provider);
    final CFunction parentFunction = new CFunction(internalModule,
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
    final Function function = new Function(module, parentFunction);

    final List<Instruction> instructions =
        Lists.newArrayList(new Instruction(new MockInstruction()));

    m_view.load();

    final CodeNode node = m_view.createCodeNode(function, instructions);

    assertEquals(node, m_view.getGraph().getNodes().get(0));
    assertEquals(listener.events, "addedNode;changedGraphType;");
    assertEquals(com.google.security.zynamics.binnavi.API.disassembly.GraphType.Flowgraph,
        m_view.getGraphType());

    m_view.removeListener(listener);
  }

  @Test
  public void testCreateEdge() throws PartialLoadException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException {
    final MockViewListener listener = new MockViewListener();

    final Function function = module.getFunctions().get(0);

    m_view.load();

    final FunctionNode node1 = m_view.createFunctionNode(function);
    final FunctionNode node2 = m_view.createFunctionNode(function);

    m_view.addListener(listener);

    final ViewEdge edge = m_view.createEdge(node1, node2, EdgeType.JumpUnconditional);

    assertEquals(edge, m_view.getGraph().getEdges().get(0));
    assertEquals(node1, edge.getSource());
    assertEquals(node2, edge.getTarget());
    assertEquals(node1, node2.getParents().get(0));
    assertEquals(node2, node1.getChildren().get(0));
    assertEquals(EdgeType.JumpUnconditional, edge.getType());
    assertEquals(listener.events, "addedEdge;");

    m_view.removeListener(listener);
  }

  @Test
  public void testCreateFunctionNode() throws PartialLoadException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException {
    final MockViewListener listener = new MockViewListener();

    m_view.addListener(listener);

    final Function function = module.getFunctions().get(0);

    m_view.load();

    final FunctionNode node = m_view.createFunctionNode(function);

    assertEquals(node, m_view.getGraph().getNodes().get(0));
    assertEquals(listener.events, "addedNode;changedGraphType;");
    assertEquals(com.google.security.zynamics.binnavi.API.disassembly.GraphType.Callgraph,
        m_view.getGraphType());

    m_view.removeListener(listener);
  }

  @Test
  public void testCreateGroupNode() throws PartialLoadException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException {
    final MockViewListener listener = new MockViewListener();

    m_view.addListener(listener);

    final Function function = module.getFunctions().get(0);

    m_view.load();

    final ViewNode node = m_view.createFunctionNode(function);
    final GroupNode groupNode = m_view.createGroupNode("Word", Lists.newArrayList(node));

    assertEquals(groupNode, m_view.getGraph().getNodes().get(1));
    assertEquals(listener.events, "addedNode;changedGraphType;addedNode;");
    assertEquals(com.google.security.zynamics.binnavi.API.disassembly.GraphType.Callgraph,
        m_view.getGraphType());

    assertEquals(groupNode, node.getParentGroup());

    m_view.removeListener(listener);
  }

  @Test
  public void testCreateNode() throws PartialLoadException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException {
    final MockViewListener listener = new MockViewListener();

    final Function function = module.getFunctions().get(0);

    final List<Instruction> instructions =
        Lists.newArrayList(new Instruction(new MockInstruction()));

    m_view.load();

    final CodeNode codeNode = m_view.createCodeNode(function, instructions);
    final FunctionNode functionNode = m_view.createFunctionNode(function);

    final ArrayList<IComment> comment = Lists.<IComment>newArrayList(
        new CComment(null, CommonTestObjects.TEST_USER_1, null, " COMMENT VIEW TEST "));

    final TextNode textNode = m_view.createTextNode(comment);

    assertEquals(3, m_view.getNodeCount());

    m_view.addListener(listener);

    @SuppressWarnings("unused")
    final CodeNode clonedCodeNode = (CodeNode) m_view.createNode(codeNode);
    @SuppressWarnings("unused")
    final FunctionNode clonedFunctionNode = (FunctionNode) m_view.createNode(functionNode);
    @SuppressWarnings("unused")
    final TextNode clonedTextNode = (TextNode) m_view.createNode(textNode);

    m_view.removeListener(listener);
  }

  @Test
  public void testDeleteEdge()
      throws com.google.security.zynamics.binnavi.API.disassembly.PartialLoadException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException {
    final MockViewListener listener = new MockViewListener();

    final Function function = module.getFunctions().get(0);

    m_view.load();

    final FunctionNode node1 = m_view.createFunctionNode(function);
    final FunctionNode node2 = m_view.createFunctionNode(function);

    final ViewEdge edge = m_view.createEdge(node1, node2, EdgeType.JumpUnconditional);

    m_view.addListener(listener);

    assertEquals(edge, m_view.getGraph().getEdges().get(0));

    m_view.deleteEdge(edge);

    assertTrue(m_view.getGraph().getEdges().isEmpty());
    assertEquals(0, m_view.getEdgeCount());
    assertEquals("deletedEdge;", listener.events);

    m_view.removeListener(listener);
  }

  @Test
  public void testDeleteNode() throws PartialLoadException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException {
    final MockViewListener listener = new MockViewListener();

    final Function function = module.getFunctions().get(0);

    m_view.load();

    final FunctionNode node = m_view.createFunctionNode(function);

    m_view.addListener(listener);

    assertEquals(node, m_view.getGraph().getNodes().get(0));

    m_view.deleteNode(node);

    assertTrue(m_view.getGraph().getNodes().isEmpty());
    assertEquals(0, m_view.getNodeCount());
    assertEquals("deletedNode;changedGraphType;", listener.events);

    m_view.removeListener(listener);
  }

  @Test
  public void testGraph() {
    final List<INaviViewNode> nodes = new ArrayList<INaviViewNode>();
    final List<INaviEdge> edges = new ArrayList<INaviEdge>();
    final MutableDirectedGraph<INaviViewNode, INaviEdge> graph =
        new MutableDirectedGraph<INaviViewNode, INaviEdge>(nodes, edges);

    final int viewId = new BigInteger(31, new SecureRandom()).intValue();

    final INaviView internalView = new CView(viewId,
        internalModule,
        "My View",
        "My View Description",
        com.google.security.zynamics.zylib.disassembly.ViewType.NonNative,
        m_creationDate,
        m_modificationDate,
        graph,
        new HashSet<CTag>(),
        false,
        m_provider);

    final INaviFunction internalFunction =
        internalModule.getContent().getFunctionContainer().getFunctions().get(0);

    final CCodeNode codeNode = internalView.getContent().createCodeNode(internalFunction,
        Lists.newArrayList(new MockInstruction()));
    final CFunctionNode functionNode =
        internalView.getContent().createFunctionNode(internalFunction);
    @SuppressWarnings("unused")
    final CTextNode textNode = internalView.getContent().createTextNode(Lists
        .<IComment>newArrayList(new CComment(null, CommonTestObjects.TEST_USER_1, null, "Foo")));
    @SuppressWarnings("unused")
    final CGroupNode groupNode =
        internalView.getContent().createGroupNode(internalView.getGraph().getNodes());
    internalView.getContent().createEdge(codeNode, functionNode,
        com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType.JUMP_UNCONDITIONAL);

    final TagManager tagManager = new TagManager(new MockTagManager(TagType.NODE_TAG));

    final MockViewListener listener = new MockViewListener();

    final View view = new View(module, internalView, tagManager, m_viewTagManager);
    view.addListener(listener);

    assertEquals(4, view.getGraph().getNodes().size());
    assertEquals(1, view.getGraph().getEdges().size());

    internalView.getContent().deleteNodes(internalView.getContent().getGraph().getNodes());

    assertEquals("deletedEdge;deletedNode;deletedNode;deletedNode;", listener.events);
  }

  @Test
  public void testReil()
      throws com.google.security.zynamics.binnavi.API.disassembly.PartialLoadException,
      InternalTranslationException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException {
    m_view.load();

    m_view.getReilCode();
  }

  @Test
  public void testSave() throws CouldntSaveDataException,
      com.google.security.zynamics.binnavi.API.disassembly.PartialLoadException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException {
    m_view.load();

    m_view.save();
  }

  @Test
  public void testSetDescription() throws CouldntSaveDataException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException,
      PartialLoadException {
    final MockViewListener listener = new MockViewListener();

    m_view.addListener(listener);
    m_view.load();
    m_view.setDescription("New Description");

    assertEquals("New Description", m_view.getDescription());
    assertEquals("changedDescription;changedModificationDate;", listener.events);

    m_view.removeListener(listener);
  }

  @Test
  public void testSetName() throws CouldntSaveDataException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException,
      PartialLoadException {
    final MockViewListener listener = new MockViewListener();

    m_view.addListener(listener);
    m_view.load();
    m_view.setName("New Name");

    assertEquals("New Name", m_view.getName());
    assertEquals("changedName;changedModificationDate;", listener.events);

    m_view.removeListener(listener);
  }

  @Test
  public void testTags() throws CouldntSaveDataException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException,
      PartialLoadException {
    final MockViewListener listener = new MockViewListener();

    final Tag tag = m_viewTagManager.addTag(null, "Hannes");

    m_view.addListener(listener);
    m_view.load();
    m_view.addTag(tag);

    assertEquals(2, m_view.getTags().size());
    assertEquals(tag, m_view.getTags().get(1));
    assertEquals(listener.events, "taggedView;");

    m_view.removeTag(tag);

    assertEquals(1, m_view.getTags().size());
    assertEquals(listener.events, "taggedView;untaggedView;");

    m_view.removeListener(listener);
  }

  @Test
  public void testTextNode() throws PartialLoadException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException {
    final MockViewListener listener = new MockViewListener();

    m_view.addListener(listener);

    m_view.load();

    final ArrayList<IComment> comment = Lists.<IComment>newArrayList(
        new CComment(null, CommonTestObjects.TEST_USER_1, null, " COMMENT VIEW TEST "));

    final TextNode textNode = m_view.createTextNode(comment);

    assertEquals(textNode, m_view.getGraph().getNodes().get(0));
    assertEquals(listener.events, "addedNode;");
    assertEquals(com.google.security.zynamics.binnavi.API.disassembly.GraphType.MixedGraph,
        m_view.getGraphType());
    assertEquals(" COMMENT VIEW TEST ", textNode.getComments().get(0).getComment());

    m_view.removeListener(listener);
  }
}
