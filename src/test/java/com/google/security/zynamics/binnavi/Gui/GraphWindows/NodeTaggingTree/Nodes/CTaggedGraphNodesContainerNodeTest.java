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
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.CTagsTreeModel;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphFactory;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CTaggedGraphNodesContainerNodeTest {
  @Test
  public void test1Simple() throws FileReadException, CouldntLoadDataException,
      LoadCancelledException, CouldntSaveDataException {
    final ZyGraph graph = ZyGraphFactory.generateTestGraph();
    final CTagsTreeModel model = new CTagsTreeModel(null);
    final CTag tag = new CTag(0, "tag", "description", TagType.NODE_TAG, new MockSqlProvider());
    final TreeNode<CTag> treeNode = new TreeNode<CTag>(tag);
    final CTaggedGraphNodesContainerNode containerNode =
        new CTaggedGraphNodesContainerNode(graph, model, treeNode);

    assertEquals("Tagged Nodes (0/0/1/1)", containerNode.toString());
    assertEquals(null, containerNode.getIconName());
    containerNode.getIcon();
  }
}
