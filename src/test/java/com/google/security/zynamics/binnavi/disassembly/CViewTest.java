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
package com.google.security.zynamics.binnavi.disassembly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Database.CModuleViewGenerator;
import com.google.security.zynamics.binnavi.Database.CProjectViewGenerator;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.ViewType;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import com.google.security.zynamics.zylib.types.graphs.MutableDirectedGraph;

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
import java.util.Set;

@RunWith(JUnit4.class)
public final class CViewTest {
  private MockSqlProvider m_sql;
  private CView m_view;
  private MockViewListener m_listener;
  private CFunction m_function;
  private CInstruction m_instruction;
  private CModule m_module;
  private CView m_unsavedView;
  private final SecureRandom random = new SecureRandom();

  @Before
  public void setUp() throws CouldntLoadDataException, LoadCancelledException, FileReadException {
    ConfigManager.instance().read();

    m_sql = new MockSqlProvider();
    m_module = MockCreator.createModule(m_sql);
    m_module.load();
    m_view = MockCreator.createView(m_sql, m_module);
    m_unsavedView = m_module.getContent().getViewContainer().createView("Unsaved", "");
    m_function = MockCreator.createFunction(m_module, m_sql);
    m_instruction = MockCreator.createInstruction(m_module, m_sql);
    m_listener = new MockViewListener();
    m_view.addListener(m_listener);
    m_unsavedView.addListener(m_listener);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor00() {
    new CView(0, m_module, "New View", "New View Description", ViewType.Native,
        GraphType.FLOWGRAPH, new Date(), new Date(), 0, 0, new HashSet<CTag>(),
        new HashSet<CTag>(), false, m_sql);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor01() {
    new CView(new BigInteger(31, random).intValue(), (CModule) null, "New View",
        "New View Description", ViewType.Native, GraphType.FLOWGRAPH, new Date(), new Date(), 0, 0,
        new HashSet<CTag>(), new HashSet<CTag>(), false, m_sql);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor02() {
    new CView(new BigInteger(31, random).intValue(), m_module, null, "New View Description",
        ViewType.Native, GraphType.FLOWGRAPH, new Date(), new Date(), 0, 0, new HashSet<CTag>(),
        new HashSet<CTag>(), false, m_sql);
  }

  @Test
  public void testConstructor03() {
    new CView(new BigInteger(31, random).intValue(), m_module, "New View", null, ViewType.Native,
        GraphType.FLOWGRAPH, new Date(), new Date(), 0, 0, new HashSet<CTag>(),
        new HashSet<CTag>(), false, m_sql);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor04() {
    new CView(new BigInteger(31, random).intValue(), m_module, "New View", "New View Description",
        null, GraphType.FLOWGRAPH, new Date(), new Date(), 0, 0, new HashSet<CTag>(),
        new HashSet<CTag>(), false, m_sql);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor05() {
    new CView(new BigInteger(31, random).intValue(), m_module, "New View", "New View Description",
        ViewType.Native, null, new Date(), new Date(), 0, 0, new HashSet<CTag>(),
        new HashSet<CTag>(), false, m_sql);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor06() {
    new CView(new BigInteger(31, random).intValue(), m_module, "New View", "New View Description",
        ViewType.Native, null, new Date(), new Date(), 0, 0, new HashSet<CTag>(),
        new HashSet<CTag>(), false, m_sql);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor07() {
    new CView(new BigInteger(31, random).intValue(), m_module, "New View", "New View Description",
        ViewType.Native, GraphType.FLOWGRAPH, null, new Date(), 0, 0, new HashSet<CTag>(),
        new HashSet<CTag>(), false, m_sql);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor08() {
    new CView(new BigInteger(31, random).intValue(), m_module, "New View", "New View Description",
        ViewType.Native, GraphType.FLOWGRAPH, null, new Date(), 0, 0, new HashSet<CTag>(),
        new HashSet<CTag>(), false, m_sql);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor09() {
    new CView(new BigInteger(31, random).intValue(), m_module, "New View", "New View Description",
        ViewType.Native, GraphType.FLOWGRAPH, new Date(), null, 0, 0, new HashSet<CTag>(),
        new HashSet<CTag>(), false, m_sql);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor10() {
    new CView(new BigInteger(31, random).intValue(), m_module, "New View", "New View Description",
        ViewType.Native, GraphType.FLOWGRAPH, new Date(), new Date(), -1, 0, new HashSet<CTag>(),
        new HashSet<CTag>(), false, m_sql);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor11() {
    new CView(new BigInteger(31, random).intValue(), m_module, "New View", "New View Description",
        ViewType.Native, GraphType.FLOWGRAPH, new Date(), new Date(), 0, -1, new HashSet<CTag>(),
        new HashSet<CTag>(), false, m_sql);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor12() {
    new CView(new BigInteger(31, random).intValue(), m_module, "New View", "New View Description",
        ViewType.Native, GraphType.FLOWGRAPH, new Date(), new Date(), 0, -1, new HashSet<CTag>(),
        new HashSet<CTag>(), false, m_sql);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor13() {
    new CView(new BigInteger(31, random).intValue(), m_module, "New View", "New View Description",
        ViewType.Native, GraphType.FLOWGRAPH, new Date(), new Date(), 0, 0, null,
        new HashSet<CTag>(), false, m_sql);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructor14() {
    new CView(new BigInteger(31, random).intValue(), m_module, "New View", "New View Description",
        ViewType.Native, GraphType.FLOWGRAPH, new Date(), new Date(), 0, 0, new HashSet<CTag>(),
        new HashSet<CTag>(), false, null);
  }

  @Test
  public void testConstructor15() {
    final MockSqlProvider sql = new MockSqlProvider();
    final CModule module = MockCreator.createModule(sql);

    final Date creationDate = new Date();
    Date modificationDate = new Date();
    final int viewId = new BigInteger(31, random).intValue();

    final Calendar cal = Calendar.getInstance();
    cal.setTime(modificationDate);
    cal.add(Calendar.DATE, 1);
    modificationDate = cal.getTime();

    final CView view =
        new CView(viewId, module, "New View", "New View Description", ViewType.Native,
            GraphType.FLOWGRAPH, creationDate, modificationDate, 33, 44, new HashSet<CTag>(),
            new HashSet<CTag>(), false, sql);

    assertEquals(viewId, view.getConfiguration().getId());
    assertEquals("New View", view.getName());
    assertEquals("New View Description", view.getConfiguration().getDescription());
    assertEquals(ViewType.Native, view.getType());
    assertEquals(GraphType.FLOWGRAPH, view.getGraphType());
    assertEquals(33, view.getNodeCount());
    assertEquals(44, view.getEdgeCount());
    assertEquals(creationDate, view.getConfiguration().getCreationDate());
    assertEquals(modificationDate, view.getConfiguration().getModificationDate());

    final Set<CTag> tags = new HashSet<CTag>();

    final CTag tag1 = new CTag(1, "Tag1", "Tag1 Tag1", TagType.VIEW_TAG, sql);
    final CTag tag2 = new CTag(2, "Tag2", "Tag2 Tag2", TagType.VIEW_TAG, sql);
    final CTag tag3 = new CTag(3, "Tag3", "Tag3 Tag3", TagType.VIEW_TAG, sql);
    final CTag tag4 = new CTag(4, "Tag4", "Tag4 Tag4", TagType.VIEW_TAG, sql);

    tags.add(tag1);
    tags.add(tag2);
    tags.add(tag3);
    tags.add(tag4);

    new CView(1, module, "New View", "New View Description", ViewType.Native, GraphType.FLOWGRAPH,
        creationDate, modificationDate, 33, 44, new HashSet<CTag>(), tags, false, sql);

    tags.add(null);

    try {
      new CView(1, module, "New View", "New View Description", ViewType.Native,
          GraphType.FLOWGRAPH, creationDate, modificationDate, 33, 44, tags, new HashSet<CTag>(),
          false, sql);
      fail();
    } catch (final NullPointerException e) {
    }

    tags.clear();
    tags.add(tag1);
    tags.add(tag2);
    tags.add(tag3);
    tags.add(tag4);

    final CTag tag5 = new CTag(5, "Tag1", "Tag1 Tag1", TagType.NODE_TAG, sql);

    tags.add(tag5);

    try {
      new CView(1, module, "New View", "New View Description", ViewType.Native,
          GraphType.FLOWGRAPH, creationDate, modificationDate, 33, 44, tags, new HashSet<CTag>(),
          false, sql);
      fail();
    } catch (final IllegalArgumentException e) {
    }

    tags.clear();
    final CTag tagWrongDB =
        new CTag(4, "Tag4", "Tag4 Tag4", TagType.VIEW_TAG, new MockSqlProvider());
    tags.add(tagWrongDB);

    try {
      new CView(1, module, "New View", "New View Description", ViewType.Native,
          GraphType.FLOWGRAPH, creationDate, modificationDate, 33, 44, tags, new HashSet<CTag>(),
          false, sql);
      fail();
    } catch (final IllegalArgumentException e) {
    }
  }

  @Test
  public void testConstructor16() {
    final MutableDirectedGraph<INaviViewNode, INaviEdge> graph =
        new MutableDirectedGraph<INaviViewNode, INaviEdge>(new ArrayList<INaviViewNode>(),
            new ArrayList<INaviEdge>());
    final Set<CTag> tags = new HashSet<CTag>();
    final MockSqlProvider provider = new MockSqlProvider();
    final MockModule module = new MockModule();
    final CView view =
        new CView(2, module, "View", "Description", ViewType.Native, new Date(1234),
            new Date(12345), graph, tags, false, provider);

    assertEquals(2, view.getConfiguration().getId());
    assertEquals("View", view.getName());
    assertEquals("Description", view.getConfiguration().getDescription());
    assertEquals(ViewType.Native, view.getType());
    assertEquals(0, view.getNodeCount());
    assertEquals(0, view.getEdgeCount());

    try {
      new CView(-2, module, "View", "Description", ViewType.Native, new Date(1234),
          new Date(12345), graph, tags, false, provider);
      fail();
    } catch (final IllegalArgumentException e) {
    }

    try {
      new CView(new BigInteger(31, random).intValue(), (CModule) null, "View", "Description",
          ViewType.Native, new Date(1234), new Date(12345), graph, tags, false, provider);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      new CView(new BigInteger(31, random).intValue(), module, null, "Description",
          ViewType.Native, new Date(1234), new Date(12345), graph, tags, false, provider);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      new CView(new BigInteger(31, random).intValue(), module, "Description", null,
          ViewType.Native, new Date(1234), new Date(12345), graph, tags, false, provider);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      new CView(new BigInteger(31, random).intValue(), module, "stuuf", "Description", null,
          new Date(1234), new Date(12345), graph, tags, false, provider);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      new CView(new BigInteger(31, random).intValue(), module, "stuuf", "Description",
          ViewType.Native, null, new Date(12345), graph, tags, false, provider);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      new CView(new BigInteger(31, random).intValue(), module, "stuuf", "Description",
          ViewType.Native, new Date(12345), null, graph, tags, false, provider);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      new CView(new BigInteger(31, random).intValue(), module, "stuuf", "Description",
          ViewType.Native, new Date(12345), new Date(12345), null, tags, false, provider);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      new CView(new BigInteger(31, random).intValue(), module, "stuuf", "Description",
          ViewType.Native, new Date(12345), new Date(12345), graph, null, false, provider);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      new CView(new BigInteger(31, random).intValue(), module, "stuuf", "Description",
          ViewType.Native, new Date(12345), new Date(12345), graph, tags, false, null);
      fail();
    } catch (final NullPointerException e) {
    }

    final CTag tag1 = new CTag(1, "Tag1", "Tag1 Tag1", TagType.VIEW_TAG, provider);
    final CTag tag2 = new CTag(2, "Tag2", "Tag2 Tag2", TagType.VIEW_TAG, provider);
    final CTag tag3 = new CTag(3, "Tag3", "Tag3 Tag3", TagType.VIEW_TAG, provider);
    final CTag tag4 = new CTag(4, "Tag4", "Tag4 Tag4", TagType.VIEW_TAG, provider);

    tags.add(tag1);
    tags.add(tag2);
    tags.add(tag3);
    tags.add(tag4);

    new CView(new BigInteger(31, random).intValue(), module, "View", "Description",
        ViewType.Native, new Date(1234), new Date(12345), graph, tags, false, provider);

    tags.add(null);

    try {
      new CView(new BigInteger(31, random).intValue(), module, "View", "Description",
          ViewType.Native, new Date(1234), new Date(12345), graph, tags, false, provider);
      fail();
    } catch (final NullPointerException e) {
    }

    tags.clear();
    tags.add(tag1);
    tags.add(tag2);
    tags.add(tag3);
    tags.add(tag4);

    final CTag tag5 = new CTag(5, "Tag1", "Tag1 Tag1", TagType.NODE_TAG, provider);

    tags.add(tag5);

    try {
      new CView(new BigInteger(31, random).intValue(), module, "View", "Description",
          ViewType.Native, new Date(1234), new Date(12345), graph, tags, false, provider);
      fail();
    } catch (final IllegalArgumentException e) {
    }

    tags.clear();
    final CTag tagWrongDB =
        new CTag(new BigInteger(31, random).intValue(), "Tag4", "Tag4 Tag4", TagType.VIEW_TAG,
            new MockSqlProvider());
    tags.add(tagWrongDB);

    try {
      new CView(new BigInteger(31, random).intValue(), module, "View", "Description",
          ViewType.Native, new Date(1234), new Date(12345), graph, tags, false, provider);
      fail();
    } catch (final IllegalArgumentException e) {
    }

  }

  @Test
  public void testConstructor2() {
    try {
      new CView(new BigInteger(31, random).intValue(), (INaviProject) null, "Foo", "Bar",
          ViewType.Native, new Date(1234), new Date(12345),
          new MutableDirectedGraph<INaviViewNode, INaviEdge>(new ArrayList<INaviViewNode>(),
              new ArrayList<INaviEdge>()), new HashSet<CTag>(), false, new MockSqlProvider());
      fail();
    } catch (final NullPointerException e) {
    }

    final int viewId = new BigInteger(31, random).intValue();

    final CView view =
        new CView(viewId, new MockProject(), "Foo", "Bar", ViewType.Native, new Date(1234),
            new Date(12345), new MutableDirectedGraph<INaviViewNode, INaviEdge>(
                new ArrayList<INaviViewNode>(), new ArrayList<INaviEdge>()), new HashSet<CTag>(),
            false, new MockSqlProvider());
    assertEquals(viewId, view.getConfiguration().getId());
  }

  @Test
  public void testConstructor3() {
    try {
      new CView(new BigInteger(31, random).intValue(), (INaviProject) null, "Blub", "Bla",
          ViewType.Native, GraphType.FLOWGRAPH, new Date(1234), new Date(12345), 123, 700,
          new HashSet<CTag>(), new HashSet<CTag>(), false, new MockSqlProvider());
      fail();
    } catch (final NullPointerException e) {
    }

    final int viewId = new BigInteger(31, random).intValue();

    final CView view =
        new CView(viewId, new MockProject(), "Blub", "Bla", ViewType.Native, GraphType.FLOWGRAPH,
            new Date(1234), new Date(12345), 123, 700, new HashSet<CTag>(), new HashSet<CTag>(),
            false, new MockSqlProvider());
    assertEquals(viewId, view.getConfiguration().getId());
  }

  @Test
  public void testCreateCodeNode() throws CouldntLoadDataException, CPartialLoadException,
      LoadCancelledException {
    try {
      m_view.getContent().createCodeNode(m_function, new ArrayList<INaviInstruction>());
      fail();
    } catch (final NullPointerException exception) {
    }

    m_view.load();

    try {
      m_view.getContent().createCodeNode(m_function, null);
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      m_view.getContent().createCodeNode(m_function, Lists.newArrayList((INaviInstruction) null));
      fail();
    } catch (final NullPointerException exception) {
    }

    assertEquals(0, m_view.getNodeCount());
    assertEquals("loadedView/", m_listener.eventList);

    m_view.getContent().createCodeNode(m_function, Lists.newArrayList(m_instruction));

    assertEquals(1, m_view.getNodeCount());
    assertEquals("loadedView/addedNode/changedGraphType/", m_listener.eventList);
  }

  @Test
  public void testCreateCodeNode_Unsaved() {
    assertEquals(0, m_unsavedView.getNodeCount());
    assertEquals("", m_listener.eventList);

    m_unsavedView.getContent().createCodeNode(m_function, Lists.newArrayList(m_instruction));

    assertEquals(1, m_unsavedView.getNodeCount());
    assertEquals("addedNode/changedGraphType/", m_listener.eventList);
  }

  @Test
  public void testCreateEdge() throws CouldntLoadDataException, CPartialLoadException,
      LoadCancelledException {
    m_view.load();

    try {
      m_view.getContent().createEdge(null, null, EdgeType.JUMP_UNCONDITIONAL);
      fail();
    } catch (final NullPointerException exception) {
    }

    final CCodeNode codeNode =
        m_view.getContent().createCodeNode(m_function, Lists.newArrayList(m_instruction));
    final CFunctionNode functionNode = m_view.getContent().createFunctionNode(m_function);

    assertEquals(0, m_view.getEdgeCount());
    assertEquals("loadedView/addedNode/changedGraphType/addedNode/changedGraphType/",
        m_listener.eventList);

    final CNaviViewEdge edge1 =
        m_view.getContent().createEdge(codeNode, codeNode, EdgeType.JUMP_CONDITIONAL_FALSE);

    // Check listener events
    assertEquals("loadedView/addedNode/changedGraphType/addedNode/changedGraphType/addedEdge/",
        m_listener.eventList);

    // Check edge
    assertEquals(codeNode, edge1.getSource());
    assertEquals(codeNode, edge1.getTarget());
    assertEquals(EdgeType.JUMP_CONDITIONAL_FALSE, edge1.getType());

    // Check view
    assertEquals(1, m_view.getEdgeCount());

    final CNaviViewEdge edge2 =
        m_view.getContent().createEdge(codeNode, functionNode, EdgeType.JUMP_CONDITIONAL_TRUE);

    // Check listener events
    assertEquals(
        "loadedView/addedNode/changedGraphType/addedNode/changedGraphType/addedEdge/addedEdge/",
        m_listener.eventList);

    // Check edge
    assertEquals(codeNode, edge2.getSource());
    assertEquals(functionNode, edge2.getTarget());
    assertEquals(EdgeType.JUMP_CONDITIONAL_TRUE, edge2.getType());

    // Check view
    assertEquals(2, m_view.getEdgeCount());

  }

  @Test
  public void testCreateEdge_Unsaved() {
    try {
      m_unsavedView.getContent().createEdge(null, null, EdgeType.JUMP_UNCONDITIONAL);
      fail();
    } catch (final NullPointerException exception) {
    }

    final CCodeNode codeNode =
        m_unsavedView.getContent().createCodeNode(m_function, Lists.newArrayList(m_instruction));
    final CFunctionNode functionNode = m_unsavedView.getContent().createFunctionNode(m_function);

    assertEquals(0, m_unsavedView.getEdgeCount());
    assertEquals("addedNode/changedGraphType/addedNode/changedGraphType/", m_listener.eventList);

    final CNaviViewEdge edge1 =
        m_unsavedView.getContent().createEdge(codeNode, codeNode, EdgeType.JUMP_CONDITIONAL_FALSE);

    // Check listener events
    assertEquals("addedNode/changedGraphType/addedNode/changedGraphType/addedEdge/",
        m_listener.eventList);

    // Check edge
    assertEquals(codeNode, edge1.getSource());
    assertEquals(codeNode, edge1.getTarget());
    assertEquals(EdgeType.JUMP_CONDITIONAL_FALSE, edge1.getType());

    // Check view
    assertEquals(1, m_unsavedView.getEdgeCount());

    final CNaviViewEdge edge2 =
        m_unsavedView.getContent().createEdge(codeNode, functionNode,
            EdgeType.JUMP_CONDITIONAL_TRUE);

    // Check listener events
    assertEquals("addedNode/changedGraphType/addedNode/changedGraphType/addedEdge/addedEdge/",
        m_listener.eventList);

    // Check edge
    assertEquals(codeNode, edge2.getSource());
    assertEquals(functionNode, edge2.getTarget());
    assertEquals(EdgeType.JUMP_CONDITIONAL_TRUE, edge2.getType());

    // Check view
    assertEquals(2, m_unsavedView.getEdgeCount());

  }

  @Test
  public void testCreateFunctionNode() throws CouldntLoadDataException, CPartialLoadException,
      LoadCancelledException {
    try {
      m_view.getContent().createFunctionNode(m_function);
      fail();
    } catch (final NullPointerException exception) {
    }

    m_view.load();

    try {
      m_view.getContent().createFunctionNode(null);
      fail();
    } catch (final NullPointerException exception) {
    }

    assertEquals(0, m_view.getNodeCount());
    assertEquals("loadedView/", m_listener.eventList);

    m_view.getContent().createFunctionNode(m_function);

    assertEquals(1, m_view.getNodeCount());
    assertEquals("loadedView/addedNode/changedGraphType/", m_listener.eventList);
  }

  @Test
  public void testCreateGroupNode() throws CouldntLoadDataException, CPartialLoadException,
      LoadCancelledException {
    m_view.load();

    assertEquals(0, m_view.getNodeCount());
    assertEquals("loadedView/", m_listener.eventList);

    final INaviViewNode node1 =
        m_view.getContent().createCodeNode(m_function, Lists.newArrayList(m_instruction));
    final INaviViewNode node2 =
        m_view.getContent().createCodeNode(m_function, Lists.newArrayList(m_instruction));

    final CGroupNode groupNode =
        m_view.getContent().createGroupNode(Lists.newArrayList(node1, node2));

    assertEquals(3, m_view.getNodeCount());
    assertEquals(
        "loadedView/addedNode/changedGraphType/addedNode/changedParentGroup/changedParentGroup/addedNode/",
        m_listener.eventList);

    assertEquals(groupNode, node1.getParentGroup());
    assertEquals(groupNode, node2.getParentGroup());
    assertEquals(2, groupNode.getElements().size());
  }

  @Test
  public void testCreateTextNode() throws CouldntLoadDataException, CPartialLoadException,
      LoadCancelledException {
    try {
      m_view.getContent().createTextNode(Lists.<IComment>newArrayList());
      fail();
    } catch (final NullPointerException exception) {
    }

    m_view.load();

    assertEquals(0, m_view.getNodeCount());
    assertEquals("loadedView/", m_listener.eventList);

    m_view.getContent().createTextNode(
        Lists.<IComment>newArrayList(new CComment(null, CommonTestObjects.TEST_USER_1, null,
            " TEXT NODE COMMENTS ")));

    assertEquals(1, m_view.getNodeCount());
    assertEquals("loadedView/addedNode/", m_listener.eventList);
  }

  @Test
  public void testDeleteEdge() throws CouldntLoadDataException, CPartialLoadException,
      LoadCancelledException {
    m_view.load();

    try {
      m_view.getContent().deleteEdge(null);
      fail();
    } catch (final NullPointerException exception) {
    }

    final CCodeNode codeNode =
        m_view.getContent().createCodeNode(m_function, Lists.newArrayList(m_instruction));
    final CFunctionNode functionNode = m_view.getContent().createFunctionNode(m_function);

    final CNaviViewEdge edge1 =
        m_view.getContent().createEdge(codeNode, codeNode, EdgeType.JUMP_CONDITIONAL_FALSE);
    final CNaviViewEdge edge2 =
        m_view.getContent().createEdge(codeNode, functionNode, EdgeType.JUMP_CONDITIONAL_TRUE);

    assertEquals(2, m_view.getEdgeCount());

    m_view.getContent().deleteEdge(edge1);

    // Check listener events
    assertEquals(
        "loadedView/addedNode/changedGraphType/addedNode/changedGraphType/addedEdge/addedEdge/deletedEdge/",
        m_listener.eventList);

    // Check view
    assertEquals(1, m_view.getEdgeCount());

    m_view.getContent().deleteEdge(edge2);

    // Check listener events
    assertEquals(
        "loadedView/addedNode/changedGraphType/addedNode/changedGraphType/addedEdge/addedEdge/deletedEdge/deletedEdge/",
        m_listener.eventList);

    // Check view
    assertEquals(0, m_view.getEdgeCount());

    try {
      m_view.getContent().deleteEdge(edge2);
      fail();
    } catch (final IllegalArgumentException exception) {
    }

    // Check listener events
    assertEquals(
        "loadedView/addedNode/changedGraphType/addedNode/changedGraphType/addedEdge/addedEdge/deletedEdge/deletedEdge/",
        m_listener.eventList);

    // Check view
    assertEquals(0, m_view.getEdgeCount());
  }

  @Test
  public void testDeleteGroupNode() throws CouldntLoadDataException, CPartialLoadException,
      LoadCancelledException {
    m_view.load();

    assertEquals(0, m_view.getNodeCount());
    assertEquals("loadedView/", m_listener.eventList);

    final INaviViewNode node1 =
        m_view.getContent().createCodeNode(m_function, Lists.newArrayList(m_instruction));
    final INaviViewNode node2 =
        m_view.getContent().createCodeNode(m_function, Lists.newArrayList(m_instruction));

    final CGroupNode groupNode =
        m_view.getContent().createGroupNode(Lists.newArrayList(node1, node2));

    final CGroupNode outerGroupNode =
        m_view.getContent().createGroupNode(Lists.newArrayList((INaviViewNode) groupNode));

    assertEquals(4, m_view.getNodeCount());

    assertEquals(groupNode, node1.getParentGroup());
    assertEquals(groupNode, node2.getParentGroup());
    assertEquals(2, groupNode.getElements().size());

    m_view.getContent().deleteNode(groupNode);

    assertEquals(3, m_view.getNodeCount());
    assertEquals(outerGroupNode, node1.getParentGroup());
    assertEquals(outerGroupNode, node2.getParentGroup());

    m_view.getContent().deleteNode(outerGroupNode);

    assertEquals(2, m_view.getNodeCount());
    assertEquals(null, node1.getParentGroup());
    assertEquals(null, node2.getParentGroup());
  }

  @Test
  public void testDeleteGroupNodes() throws CouldntLoadDataException, CPartialLoadException,
      LoadCancelledException {
    m_view.load();

    assertEquals(0, m_view.getNodeCount());
    assertEquals("loadedView/", m_listener.eventList);

    final INaviViewNode node1 =
        m_view.getContent().createCodeNode(m_function, Lists.newArrayList(m_instruction));
    final INaviViewNode node2 =
        m_view.getContent().createCodeNode(m_function, Lists.newArrayList(m_instruction));

    final CGroupNode groupNode =
        m_view.getContent().createGroupNode(Lists.newArrayList(node1, node2));

    final CGroupNode outerGroupNode =
        m_view.getContent().createGroupNode(Lists.newArrayList((INaviViewNode) groupNode));

    assertEquals(4, m_view.getNodeCount());

    assertEquals(groupNode, node1.getParentGroup());
    assertEquals(groupNode, node2.getParentGroup());
    assertEquals(2, groupNode.getElements().size());

    m_view.getContent().deleteNodes(Lists.newArrayList((INaviViewNode) groupNode, outerGroupNode));

    assertEquals(2, m_view.getNodeCount());
    assertEquals(null, node1.getParentGroup());
    assertEquals(null, node2.getParentGroup());
  }

  @Test
  public void testDeleteNode() throws CouldntLoadDataException, CPartialLoadException,
      LoadCancelledException {
    m_view.load();

    try {
      m_view.getContent().deleteNode(null);
      fail();
    } catch (final NullPointerException exception) {
    }

    final CCodeNode codeNode =
        m_view.getContent().createCodeNode(m_function, Lists.newArrayList(m_instruction));
    final CFunctionNode functionNode = m_view.getContent().createFunctionNode(m_function);

    m_view.getContent().createEdge(codeNode, codeNode, EdgeType.JUMP_CONDITIONAL_FALSE);
    m_view.getContent().createEdge(codeNode, functionNode, EdgeType.JUMP_CONDITIONAL_TRUE);

    assertEquals(2, m_view.getNodeCount());
    assertEquals(2, m_view.getEdgeCount());

    m_view.getContent().deleteNode(functionNode);

    // Check listener events
    assertEquals(
        "loadedView/addedNode/changedGraphType/addedNode/changedGraphType/addedEdge/addedEdge/deletedEdge/deletedNode/changedGraphType/",
        m_listener.eventList);

    // Check view
    assertEquals(1, m_view.getNodeCount());
    assertEquals(1, m_view.getEdgeCount());

    m_view.getContent().deleteNode(codeNode);

    // Check listener events
    assertEquals(
        "loadedView/addedNode/changedGraphType/addedNode/changedGraphType/addedEdge/addedEdge/deletedEdge/deletedNode/changedGraphType/deletedEdge/deletedNode/changedGraphType/",
        m_listener.eventList);

    // Check view
    assertEquals(0, m_view.getNodeCount());
    assertEquals(0, m_view.getEdgeCount());

    try {
      m_view.getContent().deleteNode(codeNode);
      fail();
    } catch (final IllegalArgumentException exception) {
    }

    // Check listener events
    assertEquals(
        "loadedView/addedNode/changedGraphType/addedNode/changedGraphType/addedEdge/addedEdge/deletedEdge/deletedNode/changedGraphType/deletedEdge/deletedNode/changedGraphType/",
        m_listener.eventList);

    // Check view
    assertEquals(0, m_view.getNodeCount());
  }

  @Test
  public void testDeleteNodes() throws CouldntLoadDataException, CPartialLoadException,
      LoadCancelledException {
    m_view.load();

    try {
      m_view.getContent().deleteNodes(null);
      fail();
    } catch (final NullPointerException exception) {
    }

    final CCodeNode codeNode =
        m_view.getContent().createCodeNode(m_function, Lists.newArrayList(m_instruction));
    final CFunctionNode functionNode = m_view.getContent().createFunctionNode(m_function);

    m_view.getContent().createEdge(codeNode, codeNode, EdgeType.JUMP_CONDITIONAL_FALSE);
    m_view.getContent().createEdge(codeNode, functionNode, EdgeType.JUMP_CONDITIONAL_TRUE);

    assertEquals(2, m_view.getNodeCount());
    assertEquals(2, m_view.getEdgeCount());

    m_view.getContent().deleteNodes(Lists.newArrayList((INaviViewNode) codeNode, functionNode));

    // Check listener events
    assertEquals(
        "loadedView/addedNode/changedGraphType/addedNode/changedGraphType/addedEdge/addedEdge/deletedEdge/deletedEdge/deletedNodes/",
        m_listener.eventList);

    // Check view
    assertEquals(0, m_view.getNodeCount());
    assertEquals(0, m_view.getEdgeCount());

    try {
      m_view.getContent().deleteNodes(Lists.newArrayList((INaviViewNode) codeNode, functionNode));
      fail();
    } catch (final IllegalArgumentException exception) {
    }

    // Check listener events
    assertEquals(
        "loadedView/addedNode/changedGraphType/addedNode/changedGraphType/addedEdge/addedEdge/deletedEdge/deletedEdge/deletedNodes/",
        m_listener.eventList);

    // Check view
    assertEquals(0, m_view.getNodeCount());
  }

  @Test
  public void testLifeCycle() throws CouldntLoadDataException, CPartialLoadException,
      LoadCancelledException {
    final MockSqlProvider sql = new MockSqlProvider();

    final Set<CTag> tags = new HashSet<CTag>();

    tags.add(new CTag(1, "", "", TagType.VIEW_TAG, sql));

    final CModuleViewGenerator generator = new CModuleViewGenerator(sql, m_module);

    final CView view =
        generator.generate(1, "New View", "New View Description", ViewType.Native,
            GraphType.FLOWGRAPH, new Date(), new Date(), 33, 44, tags, new HashSet<CTag>(), false);

    final MockViewListener listener = new MockViewListener();

    view.addListener(listener);

    view.load();

    listener.m_closing = false;

    view.close();

    assertTrue(view.isLoaded());

    listener.m_closing = true;

    view.close();

    assertFalse(view.isLoaded());
  }

  @Test
  public void testSave() throws CouldntSaveDataException, CouldntLoadDataException,
      CPartialLoadException, LoadCancelledException {
    try {
      m_view.save();
      fail();
    } catch (final IllegalStateException exception) {
    }

    m_view.load();

    m_view.save();

    // Check listener events
    assertEquals("loadedView/savedView/changedModificationDate/", m_listener.eventList);

    // --------------------------------------- SAVE A NATIVE VIEW
    // -------------------------------------------------

    final CView view = MockCreator.createNativeView(m_sql, m_module);

    try {
      view.save();
      fail();
    } catch (final IllegalArgumentException exception) {
    }
  }

  @Test(expected = NullPointerException.class)
  public void testSetDescription1() throws CouldntSaveDataException {
    m_view.getConfiguration().setDescription(null);
  }


  @Test
  public void testSetDescription2() throws CouldntSaveDataException {
    m_view.getConfiguration().setDescription("New Description");

    // Check listener events
    assertEquals("changedDescription/changedModificationDate/", m_listener.eventList);

    // Check view
    assertEquals("New Description", m_view.getConfiguration().getDescription());

    m_view.getConfiguration().setDescription("New Description");

    // Check listener events
    assertEquals("changedDescription/changedModificationDate/", m_listener.eventList);

    // Check view
    assertEquals("New Description", m_view.getConfiguration().getDescription());
  }

  @Test(expected = NullPointerException.class)
  public void testSetDescriptionUnsaved1() throws CouldntSaveDataException {
    m_unsavedView.getConfiguration().setDescription(null);
  }

  @Test
  public void testSetDescriptionUnsaved2() throws CouldntSaveDataException {
    m_unsavedView.getConfiguration().setDescription("New Description");

    // Check listener events
    assertEquals("changedDescription/changedModificationDate/", m_listener.eventList);

    // Check view
    assertEquals("New Description", m_unsavedView.getConfiguration().getDescription());

    m_unsavedView.getConfiguration().setDescription("New Description");

    // Check listener events
    assertEquals("changedDescription/changedModificationDate/", m_listener.eventList);

    // Check view
    assertEquals("New Description", m_unsavedView.getConfiguration().getDescription());
  }

  @Test
  public void testSetName() throws CouldntSaveDataException {
    try {
      m_view.getConfiguration().setName(null);
      fail();
    } catch (final NullPointerException exception) {
    }

    m_view.getConfiguration().setName("New Name");

    // Check listener events
    assertEquals("changedName/changedModificationDate/", m_listener.eventList);

    // Check view
    assertEquals("New Name", m_view.getName());

    m_view.getConfiguration().setName("New Name");

    // Check listener events
    assertEquals("changedName/changedModificationDate/", m_listener.eventList);

    // Check view
    assertEquals("New Name", m_view.getName());
  }

  @Test
  public void testSomeOverwrittenMethods() throws CouldntLoadDataException, CPartialLoadException,
      LoadCancelledException, CouldntSaveDataException {
    final CProjectViewGenerator generator = new CProjectViewGenerator(m_sql, new MockProject());

    final CView view =
        generator.generate(6, "Blub", "Bla", ViewType.Native, GraphType.FLOWGRAPH, new Date(1234),
            new Date(12345), 123, 700, new HashSet<CTag>(), new HashSet<CTag>(), false);
    assertEquals(6, view.getConfiguration().getId());

    view.load();

    assertNotNull(view.getBasicBlockEdges());
    assertNotNull(view.getBasicBlocks());
    assertNotNull(view.getClass());
    assertNotNull(view.getConfiguration());
    assertNotNull(view.getContent());
    assertNotNull(view.getDerivedViews());
    assertEquals(0, view.getEdgeCount());
    assertNotNull(view.getGraph());
    assertEquals(GraphType.MIXED_GRAPH, view.getGraphType());
    assertEquals(-1, view.getLoadState());
    assertEquals("Blub", view.getName());
    assertEquals(0, view.getNodeCount());
    assertNotNull(view.getNodeTags());
    assertEquals(ViewType.Native, view.getType());
    assertTrue(view.isLoaded());
    assertFalse(view.isStared());

    try {
      view.save();
      fail();
    } catch (final IllegalArgumentException e) {
    }

    assertFalse(view.wasModified());
    view.close();
  }

  @Test
  public void testTagging() throws CouldntSaveDataException {
    final CTag newTag = MockCreator.createViewTag(m_sql);

    // ------------------------------------ TAG VIEW --------------------------------

    m_view.getConfiguration().tagView(newTag);

    // Check listener events
    assertEquals("taggedView/", m_listener.eventList);

    // Check view
    assertTrue(m_view.getConfiguration().isTagged());
    assertTrue(m_view.getConfiguration().isTagged(newTag));

    m_view.getConfiguration().tagView(newTag);

    // Check listener events
    assertEquals("taggedView/", m_listener.eventList);

    // Check view
    assertTrue(m_view.getConfiguration().isTagged());
    assertTrue(m_view.getConfiguration().isTagged(newTag));

    // --------------------------------------- UNTAG VIEW -----------------------------------

    m_view.getConfiguration().untagView(newTag);

    // Check listener events
    assertEquals("taggedView/untaggedView/", m_listener.eventList);

    // Check view
    assertFalse(m_view.getConfiguration().isTagged());
    assertFalse(m_view.getConfiguration().isTagged(newTag));
  }
}
