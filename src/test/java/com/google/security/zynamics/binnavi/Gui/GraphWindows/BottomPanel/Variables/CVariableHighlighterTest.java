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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.Variables;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Debug.Debugger.MockDebuggerProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.viewReferences.CVariableHighlighter;
import com.google.security.zynamics.binnavi.ZyGraph.CHighlightLayers;
import com.google.security.zynamics.binnavi.ZyGraph.Updaters.CodeNodes.CCodeNodeUpdater;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphFactory;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class CVariableHighlighterTest {
  @Test
  public void test1Simple() throws FileReadException, CouldntLoadDataException,
      LoadCancelledException, CouldntSaveDataException {
    final ZyGraph graph = ZyGraphFactory.generateTestGraph();
    final List<INaviInstruction> mockInstructions = new ArrayList<INaviInstruction>();
    final CCodeNode node = graph.getRawView().getBasicBlocks().get(0);
    final NaviNode naviNode = graph.getNode(node);

    mockInstructions.add(Iterables.get(node.getInstructions(), 0));

    final BackEndDebuggerProvider provider = new MockDebuggerProvider();
    @SuppressWarnings("unused")
    final CCodeNodeUpdater updater = new CCodeNodeUpdater(graph, naviNode, node, provider);

    assertFalse(naviNode.getRealizer().getNodeContent().getLineContent(0)
        .hasHighlighting(CHighlightLayers.VARIABLE_LAYER));

    CVariableHighlighter.highlightInstructions(graph, mockInstructions);

    assertTrue(naviNode.getRealizer().getNodeContent().getLineContent(0)
        .hasHighlighting(CHighlightLayers.VARIABLE_LAYER));
  }
}
