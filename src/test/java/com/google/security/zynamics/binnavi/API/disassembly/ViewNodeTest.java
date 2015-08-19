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

import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.MockTagManager;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.CFunction;
import com.google.security.zynamics.binnavi.disassembly.CFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.FunctionType;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;
import com.google.security.zynamics.zylib.types.trees.Tree;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.Color;
import java.util.Date;

@RunWith(JUnit4.class)
public final class ViewNodeTest {
  private FunctionNode m_node;
  private Tag m_initialTag;
  private TagManager m_nodeTagManager;
  private View m_view;

  @Before
  public void setUp() throws CouldntSaveDataException, CouldntLoadDataException,
  LoadCancelledException, FileReadException {
    ConfigManager.instance().read();

    final MockSqlProvider provider = new MockSqlProvider();

    final Database database = new Database(new MockDatabase());

    final CModule internalModule =
        new CModule(123, "Name", "Comment", new Date(), new Date(), CommonTestObjects.MD5,
            CommonTestObjects.SHA1, 55, 66, new CAddress(0x555), new CAddress(0x666),
            new DebuggerTemplate(1, "Mock Debugger", "localhaus", 88, provider), null,
            Integer.MAX_VALUE, false, provider);
    internalModule.load();

    final CTagManager mockTagManager =
        new CTagManager(new Tree<CTag>(new TreeNode<CTag>(new CTag(1, "Root", "", TagType.NODE_TAG,
            provider))), TagType.NODE_TAG, provider);

    m_nodeTagManager = new TagManager(mockTagManager);
    final TagManager viewTagManager =
        new TagManager(new MockTagManager(com.google.security.zynamics.binnavi.Tagging.TagType.VIEW_TAG));

    final CFunction parentFunction = new CFunction(
        internalModule, new MockView(), new CAddress(0x123), "Mock Function", "Mock Function",
        "Mock Description", 0, 0, 0, 0, FunctionType.NORMAL, "", 0, null, null, null, provider);

    final Function function = new Function(ModuleFactory.get(), parentFunction);

    final Module module = new Module(database, internalModule, m_nodeTagManager, viewTagManager) {
      @Override
      public Function getFunction(final INaviFunction internalFunction) {
        return function;
      }

      @Override
      public boolean isLoaded() {
        return true;
      }
    };

    final CFunction internalFunction = new CFunction(
        internalModule, new MockView(), new CAddress(0x123), "", "", "", 0, 0, 0, 0,
        FunctionType.NORMAL, "", 0, null, null, null, provider);

    final ITreeNode<CTag> tag = mockTagManager.addTag(mockTagManager.getRootTag(), "Initial Tag");

    m_initialTag = m_nodeTagManager.getRootTags().get(0);

    final CView internalView = internalModule.getContent().getViewContainer().createView("", "");
    final CFunctionNode node = internalView.getContent().createFunctionNode(internalFunction);

    node.setColor(Color.MAGENTA);
    node.setX(10);
    node.setY(20);
    node.tagNode(tag.getObject());

    m_view = module.getViews().get(2);
    m_node = (FunctionNode) m_view.getGraph().getNodes().get(0);

    final CFunctionNode node2 = internalView.getContent().createFunctionNode(internalFunction);
    internalView.getContent().createEdge(node, node2, EdgeType.INTER_MODULE);
  }

  @Test
  public void testBorderColor() {
    final MockViewNodeListener listener = new MockViewNodeListener();

    m_node.addListener(listener);

    assertEquals(Color.BLACK, m_node.getBorderColor());

    m_node.setBorderColor(Color.WHITE);

    assertEquals(Color.WHITE, m_node.getBorderColor());
    assertEquals("changedBorderColor;", listener.events);

    m_node.removeListener(listener);
  }

  @Test
  public void testColor() {
    final MockViewNodeListener listener = new MockViewNodeListener();

    m_node.addListener(listener);

    assertEquals(Color.MAGENTA, m_node.getColor());

    m_node.setColor(Color.WHITE);

    assertEquals(Color.WHITE, m_node.getColor());
    assertEquals("changedColor;", listener.events);

    m_node.removeListener(listener);
  }

  @Test
  public void testConstructor() {
    assertEquals(m_view, m_node.getView());
    assertTrue(m_node.isVisible());
  }

  @Test
  public void testNeighbors() {
    assertEquals(1, m_node.getOutgoingEdges().size());
    assertEquals(1, m_node.getOutgoingEdges().get(0).getTarget().getIncomingEdges().size());
    assertEquals(1, m_node.getChildren().size());
    assertEquals(1, m_node.getChildren().get(0).getParents().size());
  }

  @Test
  public void testSelected() {
    final MockViewNodeListener listener = new MockViewNodeListener();

    m_node.addListener(listener);

    assertFalse(m_node.isSelected());

    m_node.setSelected(true);

    assertTrue(m_node.isSelected());
    assertEquals("changedSelection;", listener.events);

    m_node.removeListener(listener);
  }

  @Test
  public void testTagging() throws com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException {
    final MockViewNodeListener listener = new MockViewNodeListener();

    m_node.addListener(listener);

    assertEquals(1, m_node.getTags().size());
    assertEquals(m_initialTag, m_node.getTags().get(0));
    assertTrue(m_node.isTagged(m_initialTag));

    final Tag newTag = m_nodeTagManager.addTag(null, "New Tag");

    assertFalse(m_node.isTagged(newTag));

    m_node.addTag(newTag);

    assertEquals(2, m_node.getTags().size());
    assertTrue(m_node.isTagged(m_initialTag));
    assertTrue(m_node.isTagged(newTag));
    assertEquals("addedTag;", listener.events);

    m_node.removeTag(newTag);

    assertEquals(1, m_node.getTags().size());
    assertEquals(m_initialTag, m_node.getTags().get(0));
    assertTrue(m_node.isTagged(m_initialTag));
    assertFalse(m_node.isTagged(newTag));
    assertEquals("addedTag;removedTag;", listener.events);

    m_node.removeTag(m_initialTag);

    assertEquals(0, m_node.getTags().size());
    assertFalse(m_node.isTagged(m_initialTag));
    assertFalse(m_node.isTagged(newTag));
    assertEquals("addedTag;removedTag;removedTag;", listener.events);

    m_node.removeListener(listener);
  }

  @Test
  public void testVisibility() {
    final MockViewNodeListener listener = new MockViewNodeListener();

    m_node.addListener(listener);

    assertTrue(m_node.isVisible());

    m_node.getNative().setVisible(false);

    assertFalse(m_node.isVisible());
    assertEquals("changedVisibility;", listener.events);

    m_node.removeListener(listener);
  }

  @Test
  public void testX() {
    final MockViewNodeListener listener = new MockViewNodeListener();

    m_node.addListener(listener);

    assertEquals(10, m_node.getX(), 0.1);

    m_node.setX(15);

    assertEquals(15, m_node.getX(), 0.1);
    assertEquals("changedX;", listener.events);

    m_node.removeListener(listener);
  }

  @Test
  public void testY() {
    final MockViewNodeListener listener = new MockViewNodeListener();

    m_node.addListener(listener);

    assertEquals(20, m_node.getY(), 0.1);

    m_node.setY(15);

    assertEquals(15, m_node.getY(), 0.1);
    assertEquals("changedY;", listener.events);

    m_node.removeListener(listener);
  }
}
