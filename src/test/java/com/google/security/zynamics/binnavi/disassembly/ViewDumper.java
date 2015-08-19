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

import java.io.IOException;

import com.google.security.zynamics.binnavi.CConfigLoader;
import com.google.security.zynamics.binnavi.Database.CDatabase;
import com.google.security.zynamics.binnavi.Database.CJdbcDriverNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntConnectException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntInitializeDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDriverException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseVersionException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidExporterDatabaseFormatException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.binnavi.ZyGraph.Settings.ZyGraphProximitySettings;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;


public final class ViewDumper {
  public static void main(final String[] args) throws IOException, FileReadException,
      CouldntLoadDataException, InvalidDatabaseVersionException, CouldntLoadDriverException,
      CouldntConnectException, InvalidDatabaseException, CouldntInitializeDatabaseException,
      InvalidExporterDatabaseFormatException, CPartialLoadException, LoadCancelledException {
    ConfigManager.instance().read();

    final ZyGraphViewSettings viewSettings = ConfigManager.instance().getDefaultFlowGraphSettings();
    final ZyGraphProximitySettings proximitySettings = viewSettings.getProximitySettings();
    proximitySettings.setProximityBrowsingActivationThreshold(50);
    proximitySettings.setProximityBrowsingChildren(2);
    proximitySettings.setProximityBrowsingParents(2);
    ConfigManager.instance().updateFlowgraphSettings(viewSettings);

    @SuppressWarnings("unused")
    final MockSqlProvider m_provider = new MockSqlProvider();

    final String[] parts = CConfigLoader.loadPostgreSQL();

    final CDatabase m_database =
        new CDatabase("None", CJdbcDriverNames.jdbcPostgreSQLDriverName, parts[0],
            "test_disassembly", parts[1], parts[2], parts[3], false, false);

    m_database.connect();
    m_database.load();

    final INaviModule m_module = m_database.getContent().getModules().get(0);

    m_module.load();

    final IFlowgraphView m_view =
        m_database.getContent().getModules().get(0).getContent().getViewContainer()
            .getNativeFlowgraphViews().get(199);

    m_view.load();

    System.out.println("MockSqlProvider provider = new MockSqlProvider();");
    System.out.println("MockModule module = new MockModule();");
    System.out.println("MockFunction function = new MockFunction();");

    System.out.println("List<INaviViewNode> nodes = new ArrayList<INaviViewNode>();");
    System.out.println("List<INaviEdge> edges = new ArrayList<INaviEdge>();");

    for (final INaviViewNode node : m_view.getGraph()) {
      final CCodeNode oldNode = (CCodeNode) node;

      System.out
          .printf(
              "CCodeNode node_%d = new CCodeNode(%d, 0, 0, Color.BLUE, Color.BLACK, false, true, \"\", function, new HashSet<CTag>(), provider);\n",
              node.getId(), node.getId());

      for (final INaviInstruction instruction : oldNode.getInstructions()) {

        System.out
            .printf(
                "final CInstruction newInstruction_%s = new CInstruction(module, new CAddress(%d), \"%s\", new ArrayList<COperandTree>(), new byte[0], provider);\n",
                instruction.getAddress(), instruction.getAddress().toLong(),
                instruction.getMnemonic());
        System.out.printf("node_%d.addInstruction(newInstruction_%s, \"\");\n", oldNode.getId(),
            instruction.getAddress());
      }

      System.out.printf("nodes.add(node_%d);\n", node.getId());
    }

    for (final INaviEdge edge : m_view.getGraph().getEdges()) {
      System.out
          .printf(
              "CNaviEdge edge_%d = new CNaviEdge(%s, node_%d, node_%d, EdgeType.%s, 0, 0, 0, 0, Color.BLACK, false, true, \"\", new ArrayList<CBend>(), provider);\n",
              edge.getId(), edge.getId(), edge.getSource().getId(), edge.getTarget().getId(),
              edge.getType());
      System.out.printf("edges.add(edge_%d);\n", edge.getId());
      System.out.printf("CNaviViewNode.link(node_%d, node_%d);\n", edge.getSource().getId(), edge
          .getTarget().getId());
      System.out.printf("node_%d.addOutgoingEdge(edge_%d);\n", edge.getSource().getId(),
          edge.getId());
      System.out.printf("node_%d.addIncomingEdge(edge_%d);\n", edge.getTarget().getId(),
          edge.getId());
    }

    System.out.println("final INaviView view = new MockView(nodes, edges);");

    m_database.close();
  }
}
