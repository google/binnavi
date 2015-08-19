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
import com.google.security.zynamics.binnavi.Database.CModuleViewGenerator;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.MockTagManager;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.disassembly.MockEdge;
import com.google.security.zynamics.binnavi.disassembly.MockTextNode;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.types.trees.Tree;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

@RunWith(JUnit4.class)
public final class ViewGraphTest {
  @Test
  public void testConstructor() throws CouldntLoadDataException, LoadCancelledException {
    final MockSqlProvider provider = new MockSqlProvider();

    final TagManager tagManager = new TagManager(new MockTagManager(TagType.NODE_TAG));
    final TagManager viewTagManager =
        new TagManager(new CTagManager(new Tree<CTag>(new TreeNode<CTag>(new CTag(1, "", "",
            TagType.VIEW_TAG, provider))), TagType.VIEW_TAG, provider));

    final Database database = new Database(new MockDatabase());

    final CModule internalModule =
        new CModule(1, "", "", new Date(), new Date(), "00000000000000000000000000000000",
            "0000000000000000000000000000000000000000", 0, 0, new CAddress(0), new CAddress(0),
            null, null, Integer.MAX_VALUE, false, provider);

    internalModule.load();

    final Module module = new Module(database, internalModule, tagManager, viewTagManager);

    final CModuleViewGenerator generator = new CModuleViewGenerator(provider, internalModule);
    final INaviView internalView =
        generator.generate(1, "My View", "My View Description",
            com.google.security.zynamics.zylib.disassembly.ViewType.NonNative,
            GraphType.MIXED_GRAPH, new Date(), new Date(), 1, 2, new HashSet<CTag>(),
            new HashSet<CTag>(), false);

    final View view = new View(module, internalView, tagManager, viewTagManager);

    final List<ViewNode> nodes =
        Lists.newArrayList((ViewNode) new TextNode(view, new MockTextNode(), tagManager));
    final List<ViewEdge> edges =
        Lists.newArrayList(new ViewEdge(new MockEdge(1, provider), nodes.get(0), nodes.get(0)));

    final ViewGraph graph = new ViewGraph(nodes, edges);

    assertEquals(1, graph.getNodes().size());
    assertEquals(nodes.get(0), graph.getNodes().get(0));
    assertEquals(1, graph.getEdges().size());
    assertEquals(edges.get(0), graph.getEdges().get(0));
    assertEquals("View Graph [1 nodes, 1 edges]", graph.toString());
  }
}
