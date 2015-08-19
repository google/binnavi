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
package com.google.security.zynamics.binnavi.ZyGraph;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.ZyGraph.Implementations.CGraphFunctions;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.MockFunction;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.MockViewGenerator;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Loader.CGraphBuilder;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.zygraph.functions.NodeFunctions;
import com.google.security.zynamics.zylib.gui.zygraph.proximity.ProximityUpdater;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collection;

@RunWith(JUnit4.class)
public final class ZyProximityBrowserTest {
  private ZyGraph m_graph;
  private MockSqlProvider m_provider;
  private MockView m_view;

  @Before
  public void setUp() throws IllegalStateException, FileReadException, LoadCancelledException {
    ConfigManager.instance().read();

    final ZyGraphViewSettings settings = ConfigManager.instance().getDefaultFlowGraphSettings();
    settings.getProximitySettings().setProximityBrowsingActivationThreshold(50);
    settings.getProximitySettings().setProximityBrowsingChildren(2);
    settings.getProximitySettings().setProximityBrowsingParents(2);
    ConfigManager.instance().updateFlowgraphSettings(settings);

    m_provider = new MockSqlProvider();

    final INaviModule module = new MockModule(m_provider);
    final MockFunction function = new MockFunction(m_provider);
    m_view = MockViewGenerator.generate(m_provider, module, function);
    m_graph = CGraphBuilder.buildGraph(m_view);
  }

  @Test
  public void testConstructor() {
    final ProximityUpdater<NaviNode> updater = new ProximityUpdater<NaviNode>(m_graph) {
      @Override
      protected void showNodes(final Collection<NaviNode> selectedNodes,
          final Collection<NaviNode> allNodes) {
        CGraphFunctions.showNodes(null, m_graph, selectedNodes, allNodes);
      }
    };

    m_graph.addListener(updater);

    assertEquals(7, m_graph.visibleNodeCount());
    assertEquals(89, NodeFunctions.getInvisibleNodes(m_graph).size());
    assertEquals(96, m_graph.getRawView().getNodeCount());

    m_graph.getRawView().getGraph().getNodes().get(2).setSelected(true);

    assertEquals(9, m_graph.visibleNodeCount());
    assertEquals(87, NodeFunctions.getInvisibleNodes(m_graph).size());
    assertEquals(96, m_graph.getRawView().getNodeCount());

    m_graph.removeListener(updater);
  }
}
