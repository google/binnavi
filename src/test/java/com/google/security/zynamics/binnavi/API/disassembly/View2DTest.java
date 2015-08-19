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

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabaseManager;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Plugins.MockPluginInterface;
import com.google.security.zynamics.binnavi.ZyGraph.LayoutStyle;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.binnavi.api2.IPluginInterface;
import com.google.security.zynamics.binnavi.config.CallGraphSettingsConfigItem;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.disassembly.CTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleContainer;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.yfileswrap.API.disassembly.View2D;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyGraph2DView;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyNormalNodeRealizer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import y.base.Edge;
import y.base.Node;

import java.util.Date;
import java.util.LinkedHashMap;

@RunWith(JUnit4.class)
public final class View2DTest {
  private View2D m_view2d;

  private final MockDatabaseManager manager = new MockDatabaseManager();

  private final IPluginInterface pluginInterface = new MockPluginInterface(manager);

  private CView m_view;

  @Before
  public void setUp() throws CouldntLoadDataException, LoadCancelledException, FileReadException {
    ConfigManager.instance().read();

    final MockDatabase database = new MockDatabase();

    final CModule module =
        new CModule(1, "", "", new Date(), new Date(), "00000000000000000000000000000000",
            "0000000000000000000000000000000000000000", 0, 0, new CAddress(0), new CAddress(0),
            null, null, Integer.MAX_VALUE, false, new MockSqlProvider());

    database.getContent().addModule(module);

    manager.addDatabase(database);

    module.load();

    m_view = module.getContent().getViewContainer().createView("name", "description");

    final ZyGraphViewSettings settings = new ZyGraphViewSettings(new CallGraphSettingsConfigItem());
    settings.getLayoutSettings().setDefaultGraphLayout(LayoutStyle.CIRCULAR);

    final ZyGraph2DView g2dView = new ZyGraph2DView();

    final LinkedHashMap<Node, NaviNode> nodeMap = new LinkedHashMap<Node, NaviNode>();
    final LinkedHashMap<Edge, NaviEdge> edgeMap = new LinkedHashMap<Edge, NaviEdge>();

    final Node node1 = g2dView.getGraph2D().createNode();
    final CTextNode rawNode1 =
        m_view.getContent().createTextNode(
            Lists.<IComment>newArrayList(new CComment(null, CommonTestObjects.TEST_USER_1, null,
                " TEXT NODE ")));
    nodeMap.put(node1, new NaviNode(node1, new ZyNormalNodeRealizer<NaviNode>(new ZyLabelContent(
        null)), rawNode1));

    final Node node2 = g2dView.getGraph2D().createNode();
    final CTextNode rawNode2 =
        m_view.getContent().createTextNode(
            Lists.<IComment>newArrayList(new CComment(null, CommonTestObjects.TEST_USER_1, null,
                " TEXT COMMENT ")));
    nodeMap.put(node2, new NaviNode(node2, new ZyNormalNodeRealizer<NaviNode>(new ZyLabelContent(
        null)), rawNode2));

    final Edge edge = g2dView.getGraph2D().createEdge(node1, node2);
    final INaviEdge rawEdge =
        m_view.getContent().createEdge(rawNode1, rawNode2,
            com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType.JUMP_CONDITIONAL_FALSE);
    edgeMap.put(edge, new NaviEdge(nodeMap.get(node1), nodeMap.get(node2), edge,
        new ZyEdgeRealizer<NaviEdge>(new ZyLabelContent(null), null), rawEdge));

    final ZyGraph graph = new ZyGraph(m_view, nodeMap, edgeMap, settings, g2dView);

    m_view2d = new View2D(database, new CModuleContainer(database, module), graph, pluginInterface);
  }

  @Test
  public void testConstructor() {
    assertEquals(pluginInterface.getDatabaseManager().getDatabases().get(0).getModules().get(0),
        m_view2d.getContainer());
    assertEquals("View2D 'name'", m_view2d.toString());
  }

  @Test
  public void testLayouts() {
    m_view2d.doCircularLayout();
    m_view2d.doHierarchicalLayout();
    m_view2d.doOrthogonalLayout();
    m_view2d.zoomToScreen();
    m_view2d.updateUI();
  }

  @Test
  public void testSave()
      throws com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException {
    m_view2d.save();
  }

  @Test
  public void testSaveAs()
      throws com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException {
    m_view2d.saveAs("Fark", "Fork");
  }
}
