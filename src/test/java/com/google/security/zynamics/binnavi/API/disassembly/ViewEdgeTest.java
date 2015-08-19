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
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Tagging.MockTagManager;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;

@RunWith(JUnit4.class)
public final class ViewEdgeTest {
  private ViewEdge m_edge;
  private TextNode m_source;
  private TextNode m_target;

  @Before
  public void setUp() throws CouldntLoadDataException, LoadCancelledException {
    final MockSqlProvider provider = new MockSqlProvider();

    final Database database = new Database(new MockDatabase());

    final CModule internalModule =
        new CModule(123, "Name", "Comment", new Date(), new Date(),
            "12345678123456781234567812345678", "1234567812345678123456781234567812345678", 55, 66,
            new CAddress(0x555), new CAddress(0x666), new DebuggerTemplate(1, "Mock Debugger",
                "localhaus", 88, provider), null, Integer.MAX_VALUE, false, provider);
    internalModule.load();

    final TagManager nodeTagManager =
        new TagManager(new MockTagManager(
            com.google.security.zynamics.binnavi.Tagging.TagType.NODE_TAG));
    final TagManager viewTagManager =
        new TagManager(new MockTagManager(
            com.google.security.zynamics.binnavi.Tagging.TagType.VIEW_TAG));

    final Module module = new Module(database, internalModule, nodeTagManager, viewTagManager);

    internalModule.getContent().getViewContainer().createView("", "");

    final View view = module.getViews().get(2); // new View(module, mockView, nodeTagManager,
                                                // viewTagManager);

    final ArrayList<IComment> comment =
        Lists.<IComment>newArrayList(new CComment(null, CommonTestObjects.TEST_USER_1, null,
            " COMMENT "));

    m_source = view.createTextNode(comment);
    m_target = view.createTextNode(comment);

    // m_internalEdge = new CNaviEdge(1, internalNode, internalNode,
    // com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType.JUMP_UNCONDITIONAL, 0, 0, 0, 0,
    // Color.MAGENTA,
    // false, true, "", new FilledList<CBend>(), new MockSqlProvider());

    m_edge = view.createEdge(m_source, m_target, EdgeType.JumpUnconditional); // new
                                                                              // ViewEdge(m_internalEdge,
                                                                              // m_source,
                                                                              // m_target);
  }

  @Test
  public void testConstructor() {
    assertEquals(m_source, m_edge.getSource());
    assertEquals(m_target, m_edge.getTarget());
    assertEquals(EdgeType.JumpUnconditional, m_edge.getType());
    assertTrue(m_edge.isVisible());
    assertEquals("View Edge [Text Node with: '1' comments. -> Text Node with: '1' comments.]",
        m_edge.toString());
  }

  @Test
  public void testSetColor() {
    final MockViewEdgeListener listener = new MockViewEdgeListener();

    m_edge.addListener(listener);

    m_edge.setColor(Color.RED);

    assertEquals(Color.RED, m_edge.getColor());
    assertEquals("changedColor;", listener.events);

    m_edge.removeListener(listener);
  }

  @Test
  public void testSetVisibility() {
    final MockViewEdgeListener listener = new MockViewEdgeListener();

    m_edge.addListener(listener);

    m_edge.setVisible(false);

    assertFalse(m_edge.isVisible());
    assertEquals("changedVisibility;", listener.events);

    m_edge.removeListener(listener);
  }
}
