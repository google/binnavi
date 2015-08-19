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

import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.CConfigLoader;
import com.google.security.zynamics.binnavi.Database.CDatabase;
import com.google.security.zynamics.binnavi.Database.CJdbcDriverNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntConnectException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntInitializeDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDriverException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseVersionException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidExporterDatabaseFormatException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphView;
import com.google.security.zynamics.binnavi.disassembly.IFlowgraphView;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleContainer;
import com.google.security.zynamics.binnavi.disassembly.views.CViewFilter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Loader.CGraphBuilder;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.Color;
import java.io.IOException;

@RunWith(JUnit4.class)
public class CPostgreSQLZyGraphTest2 {
  private ZyGraph m_graph;
  private IFlowgraphView m_view;
  private INaviModule m_module;
  private CDatabase m_database;
  private CDatabase m_database2;

  @Before
  public void setUp() throws IOException, CouldntLoadDriverException, CouldntConnectException,
      IllegalStateException, CouldntLoadDataException, InvalidDatabaseException,
      CouldntInitializeDatabaseException, FileReadException,
      InvalidExporterDatabaseFormatException, InvalidDatabaseVersionException,
      CPartialLoadException, LoadCancelledException, MaybeNullException {
    ConfigManager.instance().read();

    final ZyGraphViewSettings settings = ConfigManager.instance().getDefaultFlowGraphSettings();
    settings.getProximitySettings().setProximityBrowsingActivationThreshold(50);
    settings.getProximitySettings().setProximityBrowsingChildren(2);
    settings.getProximitySettings().setProximityBrowsingParents(2);
    ConfigManager.instance().updateFlowgraphSettings(settings);

    final String[] parts = CConfigLoader.loadPostgreSQL();

    m_database =
        new CDatabase("None", CJdbcDriverNames.jdbcPostgreSQLDriverName, parts[0],
            "test_disassembly", parts[1], parts[2], parts[3], false, false);
    m_database2 =
        new CDatabase("None", CJdbcDriverNames.jdbcPostgreSQLDriverName, parts[0],
            "test_disassembly", parts[1], parts[2], parts[3], false, false);

    m_database.connect();
    m_database.load();

    m_module = m_database.getContent().getModules().get(0);

    m_module.load();

    final INaviFunction function =
        m_module.getContent().getFunctionContainer().getFunction("sub_1002B87");
    m_view = (IFlowgraphView) m_module.getContent().getViewContainer().getView(function);
    m_view.load();

    m_graph = CGraphBuilder.buildGraph(m_view);
  }

  @After
  public void tearDown() {
    m_database.close();
  }

  @Test
  public void testLayouting() throws CouldntLoadDataException, CPartialLoadException,
      LoadCancelledException {
    final ICallgraphView cg = m_module.getContent().getViewContainer().getNativeCallgraphView();

    final ZyGraphViewSettings settings = ConfigManager.instance().getDefaultFlowGraphSettings();
    settings.getProximitySettings().setProximityBrowsingActivationThreshold(50);

    cg.load();

    final ZyGraph g = CGraphBuilder.buildGraph(cg);

    g.getSettings().getLayoutSettings().setDefaultGraphLayout(LayoutStyle.HIERARCHIC);
    g.doLayout();

    g.getSettings().getLayoutSettings().setDefaultGraphLayout(LayoutStyle.CIRCULAR);
    g.doLayout();
  }

  @Test
  public void testSave() throws CouldntSaveDataException, CouldntLoadDriverException,
      CouldntConnectException, InvalidDatabaseException, CouldntInitializeDatabaseException,
      CouldntLoadDataException, InvalidExporterDatabaseFormatException,
      InvalidDatabaseVersionException, CPartialLoadException, LoadCancelledException {
    m_view.getGraph().getNodes().get(0).setSelected(true);
    m_view.getGraph().getNodes().get(1).setColor(new Color(123));
    m_view.getGraph().getNodes().get(2).setX(456);
    m_view.getGraph().getNodes().get(3).setY(789);

    final INaviView newView =
        m_graph.saveAs(new CModuleContainer(m_database, m_module), "New View",
            "New View Description");

    m_database2.connect();
    m_database2.load();

    final INaviModule module = m_database2.getContent().getModules().get(0);

    module.load();

    final Iterable<INaviView> views =
        CViewFilter.getFlowgraphViews(module.getContent().getViewContainer().getViews());
    final INaviView loadedNewView = Iterables.getLast(views);

    loadedNewView.load();

    assertEquals(loadedNewView.getNodeCount(), newView.getNodeCount());
    assertEquals(true, loadedNewView.getGraph().getNodes().get(0).isSelected());
    assertEquals(0xFF00007B, loadedNewView.getGraph().getNodes().get(1).getColor().getRGB());
    assertEquals(456, loadedNewView.getGraph().getNodes().get(2).getX(), 0);
    assertEquals(789, loadedNewView.getGraph().getNodes().get(3).getY(), 0);

    m_database2.close();
  }
}
