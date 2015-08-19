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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Nodes;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphFactory;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class CTaggedGraphNodeNodeTest {
  @Test
  public void test1Simple() throws FileReadException, CouldntLoadDataException,
      LoadCancelledException, CouldntSaveDataException {
    final ZyGraph graph = ZyGraphFactory.generateTestGraph();
    final CCodeNode node = graph.getRawView().getBasicBlocks().get(0);
    final NaviNode naviNode = graph.getNode(node);
    @SuppressWarnings("unused")
    final CTaggedGraphNodeNode taggedNode = new CTaggedGraphNodeNode(graph, naviNode);

  }

  @Test
  public void test2getGraphNode() throws FileReadException, CouldntLoadDataException,
      LoadCancelledException, CouldntSaveDataException {
    final ZyGraph graph = ZyGraphFactory.generateTestGraph();
    final CCodeNode node = graph.getRawView().getBasicBlocks().get(0);
    final NaviNode naviNode = graph.getNode(node);
    final CTaggedGraphNodeNode taggedNode = new CTaggedGraphNodeNode(graph, naviNode);
    assertEquals(naviNode, taggedNode.getGraphNode());
  }
}
