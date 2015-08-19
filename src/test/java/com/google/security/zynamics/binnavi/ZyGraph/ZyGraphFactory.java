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

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabaseManager;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.ZyCodeNodeBuilder;
import com.google.security.zynamics.binnavi.config.CallGraphSettingsConfigItem;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.disassembly.COperandTree;
import com.google.security.zynamics.binnavi.disassembly.COperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.CReference;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.MockInstruction;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;
import com.google.security.zynamics.zylib.disassembly.IReference;
import com.google.security.zynamics.zylib.disassembly.ReferenceType;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyGraph2DView;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyNormalNodeRealizer;

import y.base.Edge;
import y.base.Node;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

public class ZyGraphFactory {
  final static ArrayList<IComment> m_globalComment = Lists.<IComment>newArrayList(new CComment(
      null, CommonTestObjects.TEST_USER_1, null, "Global Comment\nTest Case\nZyGraphFactory"));

  public static ZyGraph generateTestGraph() throws FileReadException, CouldntLoadDataException,
      LoadCancelledException, CouldntSaveDataException {
    ConfigManager.instance().read();

    final MockDatabase database = new MockDatabase();
    final SQLProvider provider = new MockSqlProvider();

    final CModule module =
        new CModule(1, "", "", new Date(), new Date(), CommonTestObjects.MD5,
            CommonTestObjects.SHA1, 0, 0, new CAddress(0), new CAddress(0), null, null,
            Integer.MAX_VALUE, false, provider);

    final ZyGraphViewSettings settings = new ZyGraphViewSettings(new CallGraphSettingsConfigItem());
    settings.getLayoutSettings().setDefaultGraphLayout(LayoutStyle.CIRCULAR);

    database.getContent().addModule(module);

    final MockDatabaseManager manager = new MockDatabaseManager();

    manager.addDatabase(database);

    module.load();

    final CView m_view = module.getContent().getViewContainer().createView("name", "description");

    final LinkedHashMap<Node, NaviNode> nodeMap = new LinkedHashMap<Node, NaviNode>();

    final ZyGraph2DView g2dView = new ZyGraph2DView();

    final LinkedHashMap<Edge, NaviEdge> edgeMap = new LinkedHashMap<Edge, NaviEdge>();

    final Node node1 = g2dView.getGraph2D().createNode();
    final COperandTreeNode rootNode1 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SIZE_PREFIX_ID, "b4", null,
            new ArrayList<IReference>(), provider, module.getTypeManager(), module.getContent()
                .getTypeInstanceContainer());
    final COperandTreeNode childNode1 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_REGISTER_ID, "eax", null,
            new ArrayList<IReference>(), provider, module.getTypeManager(), module.getContent()
                .getTypeInstanceContainer());
    COperandTreeNode.link(rootNode1, childNode1);

    final COperandTreeNode rootNode2 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SIZE_PREFIX_ID, "b4", null,
            new ArrayList<IReference>(), provider, module.getTypeManager(), module.getContent()
                .getTypeInstanceContainer());
    final COperandTreeNode childNode2 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_REGISTER_ID, "ebx", null,
            new ArrayList<IReference>(), provider, module.getTypeManager(), module.getContent()
                .getTypeInstanceContainer());
    COperandTreeNode.link(rootNode2, childNode2);

    final COperandTree operand1 =
        new COperandTree(rootNode1, provider, module.getTypeManager(), module.getContent()
            .getTypeInstanceContainer());
    final COperandTree operand2 =
        new COperandTree(rootNode2, provider, module.getTypeManager(), module.getContent()
            .getTypeInstanceContainer());
    final ArrayList<COperandTree> operands1 = Lists.newArrayList(operand1, operand2);

    final List<INaviInstruction> instructions1 =
        Lists.newArrayList((INaviInstruction) new MockInstruction(new CAddress(0x123456), "mov",
            operands1, null, module));
    final INaviCodeNode rawNode1 = m_view.getContent().createCodeNode(null, instructions1);
    final ZyLabelContent content =
        ZyCodeNodeBuilder.buildContent(rawNode1, settings, null);
    nodeMap
        .put(node1, new NaviNode(node1, new ZyNormalNodeRealizer<NaviNode>(content), rawNode1));

    final Node node2 = g2dView.getGraph2D().createNode();
    final List<INaviInstruction> instructions2 =
        Lists.newArrayList((INaviInstruction) new MockInstruction());
    final INaviCodeNode rawNode2 = m_view.getContent().createCodeNode(null, instructions2);
    final ZyLabelContent content2 =
        ZyCodeNodeBuilder.buildContent(rawNode2, settings, null);
    nodeMap.put(node2,
        new NaviNode(node2, new ZyNormalNodeRealizer<NaviNode>(content2), rawNode2));

    rawNode2.setColor(Color.RED);
    rawNode2.setVisible(false);
    final CTag tag = new CTag(0, "Tag", "Description", TagType.NODE_TAG, new MockSqlProvider());
    rawNode2.tagNode(tag);

    final Node node3 = g2dView.getGraph2D().createNode();
    final String mnemonicName = "call";
    final List<COperandTree> operandTrees = new ArrayList<COperandTree>();

    final int type = ExpressionType.REGISTER.ordinal();
    final CReference reference = new CReference(new CAddress(0x123), ReferenceType.CALL_DIRECT);
    final List<IReference> referencea = new ArrayList<IReference>();
    referencea.add(reference);
    final COperandTreeNode root =
        new COperandTreeNode(0, type, "eax", null, referencea, provider, module.getTypeManager(),
            module.getContent().getTypeInstanceContainer());
    final COperandTree operandTree =
        new COperandTree(root, provider, module.getTypeManager(), module.getContent()
            .getTypeInstanceContainer());
    operandTrees.add(0, operandTree);
    final List<INaviInstruction> instructions3 =
        Lists.newArrayList((INaviInstruction) new MockInstruction(mnemonicName, operandTrees,
            m_globalComment));
    final INaviCodeNode rawNode3 = m_view.getContent().createCodeNode(null, instructions3);
    final ZyLabelContent content3 =
        ZyCodeNodeBuilder.buildContent(rawNode3, settings, null);
    nodeMap.put(node3,
        new NaviNode(node3, new ZyNormalNodeRealizer<NaviNode>(content3), rawNode3));

    final Edge edge = g2dView.getGraph2D().createEdge(node1, node2);
    final INaviEdge rawEdge =
        m_view.getContent().createEdge(rawNode1, rawNode2, EdgeType.JUMP_UNCONDITIONAL);
    edgeMap.put(edge, new NaviEdge(nodeMap.get(node1), nodeMap.get(node2), edge,
        new ZyEdgeRealizer<NaviEdge>(new ZyLabelContent(null), null), rawEdge));

    final Edge edge2 = g2dView.getGraph2D().createEdge(node2, node3);
    final INaviEdge rawEdge2 =
        m_view.getContent().createEdge(rawNode2, rawNode3, EdgeType.JUMP_UNCONDITIONAL);
    edgeMap.put(edge2, new NaviEdge(nodeMap.get(node2), nodeMap.get(node3), edge,
        new ZyEdgeRealizer<NaviEdge>(new ZyLabelContent(null), null), rawEdge2));

    return new ZyGraph(m_view, nodeMap, edgeMap, settings, g2dView);
  }

  public static ZyGraph get() {
    return new ZyGraph(new MockView(), new LinkedHashMap<Node, NaviNode>(),
        new LinkedHashMap<Edge, NaviEdge>(), ZyGraphViewSettingsFactory.get(), new ZyGraph2DView());
  }
}
