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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.parsers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.CodeNodeCommentNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.CommentNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.EdgeCommentNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.FunctionCommentNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.FunctionNodeCommentNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.GroupNodeCommentNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.InstructionCommentNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.TextNodeCommentNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.TypeInstanceCommentNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.interfaces.CommentNotification;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.parsers.PostgreSQLCommentNotificationParser;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.CommentManager;
import com.google.security.zynamics.binnavi.disassembly.CommentManager.CommentOperation;
import com.google.security.zynamics.binnavi.disassembly.CommentManager.CommentScope;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;
import com.google.security.zynamics.binnavi.disassembly.MockFunction;
import com.google.security.zynamics.binnavi.disassembly.MockInstruction;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.disassembly.types.SectionPermission;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstance;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.ViewType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.postgresql.PGNotification;

import java.util.ArrayList;
import java.util.Collection;

@RunWith(JUnit4.class)
public class PostgreSQLNotificationParserTest {
  private final SQLProvider provider = new MockSqlProvider();
  private final INaviView mockView = MockView.getFullView(provider, ViewType.Native, null);
  private final INaviFunction mockFunction = new MockFunction(provider, 4608);
  private final Collection<PGNotification> notifications = new ArrayList<PGNotification>();
  private final PostgreSQLCommentNotificationParser parser =
      new PostgreSQLCommentNotificationParser();

  // Strings for the group node comment parsing tests.

  private final MockInstruction instruction = new MockInstruction();
  private final IComment comment1 = new CComment(1111, CommonTestObjects.TEST_USER_1, null, "AAA");
  private final IComment comment2 =
      new CComment(2222, CommonTestObjects.TEST_USER_2, comment1, "BBB");
  private final IComment comment3 =
      new CComment(3333, CommonTestObjects.TEST_USER_3, comment2, "CCC");


  @Test
  public void testCommentTableNotificationParser1() {
    CommentManager.get(provider).initializeGlobalInstructionComment(instruction,
        Lists.newArrayList(comment1, comment2, comment3));
    final String COMMENTS_TABLE_NOTIFICATION_1 =
        "bn_comments UPDATE 3333 2222 " + CommonTestObjects.TEST_USER_3.getUserId() + " AAA";
    final CommentNotification result =
        PostgreSQLCommentNotificationParser.processCommentNotification(
            new MockPGNotification("comment_changes", COMMENTS_TABLE_NOTIFICATION_1), provider);
    assertNotNull(result);
    final CommentNotificationContainer container = (CommentNotificationContainer) result;
    final IComment comment = container.getCurrentComment();
    assertEquals(comment3, comment);
    final IComment testComment = new CComment(3333, CommonTestObjects.TEST_USER_3, comment2, "AAA");
    assertEquals(testComment, container.getNewComment());
  }

  @Test
  public void testCommentTableNotificationParserDeleteUnknownComment() {
    CommentManager.get(provider).initializeGlobalInstructionComment(instruction,
        Lists.newArrayList(comment1, comment2, comment3));
    final CommentNotification result =
        PostgreSQLCommentNotificationParser.processCommentNotification(new MockPGNotification(
            "comment_changes",
            "bn_comments UPDATE 1 2222 " + CommonTestObjects.TEST_USER_3.getUserId() + " AAA"),
            provider);
    assertNull(result);
  }

  @Test(expected = IllegalStateException.class)
  public void testCommentTableNotificationParserWrongParentComment() {
    CommentManager.get(provider).initializeGlobalInstructionComment(instruction,
        Lists.newArrayList(comment1, comment2, comment3));
    final String COMMENTS_TABLE_NOTIFICATION_2 =
        "bn_comments UPDATE 3333 null " + CommonTestObjects.TEST_USER_1.getUserId() + " AAA";
    notifications.add(new MockPGNotification("comment_changes", COMMENTS_TABLE_NOTIFICATION_2));
    parser.parse(notifications, provider);
  }

  @Test(expected = IllegalStateException.class)
  public void testCommentTableNotificationParserWrongUser() {
    CommentManager.get(provider).initializeGlobalInstructionComment(instruction,
        Lists.newArrayList(comment1, comment2, comment3));

    final String COMMENTS_TABLE_NOTIFICATION_4 =
        "bn_comments DELETE 3333 2222 " + CommonTestObjects.TEST_USER_1.getUserId() + " AAA";

    notifications.add(new MockPGNotification("comment_changes", COMMENTS_TABLE_NOTIFICATION_4));

    parser.parse(notifications, provider);
  }

  @Test
  public void testFunctionCommentParsingAppendComment() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification =
        new MockPGNotification("comment_changes", "bn_functions UPDATE 1 4608 3333");
    final CommentNotification result = PostgreSQLCommentNotificationParser
        .processFunctionCommentNotification(notification, provider);

    assertNotNull(result);

    final FunctionCommentNotificationContainer container =
        (FunctionCommentNotificationContainer) result;
    final INaviFunction function = container.getFunction();

    assertEquals(new CAddress(4608), function.getAddress());
    assertEquals(CommentOperation.APPEND, container.getOperation());
    assertEquals(new Integer(3333), container.getCommentId());
  }

  @Test
  public void testFunctionCommentParsingDeleteComment() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification =
        new MockPGNotification("comment_changes", "bn_functions UPDATE 1 4608 null");
    final CommentNotification result = PostgreSQLCommentNotificationParser
        .processFunctionCommentNotification(notification, provider);

    assertNotNull(result);

    final FunctionCommentNotificationContainer container =
        (FunctionCommentNotificationContainer) result;
    final INaviFunction function = container.getFunction();

    assertEquals(new CAddress(4608), function.getAddress());
    assertEquals(CommentOperation.DELETE, container.getOperation());
    assertNull(container.getCommentId());
  }

  @Test
  public void testFunctionNodeCommentParsingAppendComment() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification =
        new MockPGNotification("comment_changes", "bn_function_nodes UPDATE 1 6666 4608 3333");
    final CommentNotification result = PostgreSQLCommentNotificationParser
        .processFunctionNodeCommentNotification(notification, provider);

    assertNotNull(result);

    final FunctionNodeCommentNotificationContainer container =
        (FunctionNodeCommentNotificationContainer) result;
    final INaviFunction function = container.getNode().getFunction();

    assertEquals(new CAddress(4608), function.getAddress());
    assertEquals(CommentOperation.APPEND, container.getOperation());
    assertEquals(new Integer(3333), container.getCommentId());
  }

  @Test
  public void testFunctionNodeCommentParsingAppendCommentUnknownModule() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification =
        new MockPGNotification("comment_changes", "bn_function_nodes UPDATE 2 6666 4608 3333");
    final CommentNotification result = PostgreSQLCommentNotificationParser
        .processFunctionNodeCommentNotification(notification, provider);
    assertNull(result);
  }

  @Test
  public void testFunctionNodeCommentParsingAppendCommentUnknownNodeId() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification =
        new MockPGNotification("comment_changes", "bn_function_nodes UPDATE 1 6667 4608 3333");
    final CommentNotification result = PostgreSQLCommentNotificationParser
        .processFunctionNodeCommentNotification(notification, provider);
    assertNull(result);
  }

  @Test
  public void testFunctionNodeCommentParsingCompleteGarbageInput() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));

    // Normal input "bn_function_nodes UPDATE 1 6666 4608 null"
    notifications.add(new MockPGNotification("comment_changes", "bn_function_nodes 1 1 1 1 1"));
    notifications.add(new MockPGNotification("comment_changes", "bn_function_nodes UPDATE"));
    notifications.add(new MockPGNotification("comment_changes", "bn_function_nodes UPDATE 1 "));
    notifications.add(new MockPGNotification("comment_changes", "bn_function_nodes"));
    notifications.add(new MockPGNotification("comment_changes",
        "bn_function_nodes 1 FOO FOO FOOO ooqwkepqwpoekpqowkep"
        + "oqw\\n\\\n\\\n\\\\\\n\\n\n\\n\\\n\\n\\\n\\c\\c\\c\\c"
        + "ckepokqwpekpqwokepoaksjeofijsoiefjosejfosjoefjsoisje" + "foisjefoisjeofijsoeifjsoeifj"));

    for (PGNotification notification : notifications) {
      assertNull(PostgreSQLCommentNotificationParser.processFunctionCommentNotification(
          notification, provider));
    }
  }

  @Test
  public void testFunctionNodeCommentParsingDeleteComment() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification =
        new MockPGNotification("comment_changes", "bn_function_nodes UPDATE 1 6666 4608 null");
    final CommentNotification result = PostgreSQLCommentNotificationParser
        .processFunctionNodeCommentNotification(notification, provider);

    assertNotNull(result);

    final FunctionNodeCommentNotificationContainer container =
        (FunctionNodeCommentNotificationContainer) result;
    final INaviFunctionNode functionNode = container.getNode();

    assertEquals(new CAddress(4608), functionNode.getAddress());
    assertEquals(CommentOperation.DELETE, container.getOperation());
    assertNull(container.getCommentId());
  }

  @Test
  public void testGlobalCodeNodeCommentParsingAppendFirstComment() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification =
        new MockPGNotification("comment_changes", "bn_global_node_comments INSERT 1 4608 3333");
    final Collection<CommentNotification> result = PostgreSQLCommentNotificationParser
        .processNodeGlobalCommentNotification(notification, provider);

    assertEquals(1, result.size());

    final CodeNodeCommentNotificationContainer container =
        (CodeNodeCommentNotificationContainer) Iterables.getFirst(result, null);
    final INaviCodeNode node = container.getNode();

    assertEquals(1111, node.getId());
    assertEquals(new CAddress(4608), node.getAddress());
    assertEquals(CommentOperation.APPEND, container.getOperation());
    assertEquals(new Integer(3333), container.getCommentId());
    assertEquals(CommentScope.GLOBAL, container.getScope());
  }

  @Test
  public void testGlobalCodeNodeCommentParsingAppendNewComment() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification =
        new MockPGNotification("comment_changes", "bn_global_node_comments UPDATE 1 4608 3333");
    final Collection<CommentNotification> result = PostgreSQLCommentNotificationParser
        .processNodeGlobalCommentNotification(notification, provider);

    assertEquals(1, result.size());

    final CodeNodeCommentNotificationContainer container =
        (CodeNodeCommentNotificationContainer) Iterables.getFirst(result, null);
    final INaviCodeNode node = container.getNode();

    assertEquals(1111, node.getId());
    assertEquals(new CAddress(4608), node.getAddress());
    assertEquals(CommentOperation.APPEND, container.getOperation());
    assertEquals(new Integer(3333), container.getCommentId());
    assertEquals(CommentScope.GLOBAL, container.getScope());
  }

  @Test
  public void testGlobalCodeNodeCommentParsingDeleteComment() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification =
        new MockPGNotification("comment_changes", "bn_global_node_comments DELETE 1 4608");
    final Collection<CommentNotification> result = PostgreSQLCommentNotificationParser
        .processNodeGlobalCommentNotification(notification, provider);

    assertEquals(1, result.size());

    final CodeNodeCommentNotificationContainer container =
        (CodeNodeCommentNotificationContainer) Iterables.getFirst(result, null);
    final INaviCodeNode node = container.getNode();

    assertEquals(1111, node.getId());
    assertEquals(new CAddress(4608), node.getAddress());
    assertEquals(CommentOperation.DELETE, container.getOperation());
    assertNull(container.getCommentId());
    assertEquals(CommentScope.GLOBAL, container.getScope());
  }

  @Test
  public void testGlobalEdgeCommentParsingAppendFirstComment() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification = new MockPGNotification("comment_changes",
        "bn_global_edge_comments INSERT 1 1 4608 4614 3333");
    final Collection<CommentNotification> result = PostgreSQLCommentNotificationParser
        .processEdgeGlobalCommentNotification(notification, provider);

    assertEquals(1, result.size());

    final EdgeCommentNotificationContainer container =
        (EdgeCommentNotificationContainer) Iterables.getFirst(result, null);
    final INaviEdge edge = container.getEdge();

    assertEquals(1111, edge.getId());
    assertEquals(CommentOperation.APPEND, container.getOperation());
    assertEquals(new Integer(3333), container.getCommentId());
    assertEquals(CommentScope.GLOBAL, container.getScope());
  }

  @Test
  public void testGlobalEdgeCommentParsingAppendNewComment() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification = new MockPGNotification("comment_changes",
        "bn_global_edge_comments UPDATE 1 1 4608 4614 3333");
    final Collection<CommentNotification> result = PostgreSQLCommentNotificationParser
        .processEdgeGlobalCommentNotification(notification, provider);

    assertEquals(1, result.size());

    final EdgeCommentNotificationContainer container =
        (EdgeCommentNotificationContainer) Iterables.getFirst(result, null);
    final INaviEdge edge = container.getEdge();

    assertEquals(1111, edge.getId());
    assertEquals(CommentOperation.APPEND, container.getOperation());
    assertEquals(new Integer(3333), container.getCommentId());
    assertEquals(CommentScope.GLOBAL, container.getScope());
  }

  @Test
  public void testGlobalEdgeCommentParsingDeleteLastComment() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification =
        new MockPGNotification("comment_changes", "bn_global_edge_comments DELETE 1 1 4608 4614");
    Collection<CommentNotification> result = PostgreSQLCommentNotificationParser
        .processEdgeGlobalCommentNotification(notification, provider);

    assertEquals(1, result.size());

    final EdgeCommentNotificationContainer container =
        (EdgeCommentNotificationContainer) Iterables.getFirst(result, null);
    final INaviEdge edge = container.getEdge();

    assertEquals(1111, edge.getId());
    assertEquals(CommentOperation.DELETE, container.getOperation());
    assertNull(container.getCommentId());
    assertEquals(CommentScope.GLOBAL, container.getScope());
  }

  @Test
  public void testGlobalInstructionCommentParsingAppend() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification =
        new MockPGNotification("comment_changes", "bn_instructions UPDATE 1 4608 3333");
    final CommentNotification result = PostgreSQLCommentNotificationParser
        .processInstructionGlobalCommentNotification(notification, provider);

    assertNotNull(result);

    final InstructionCommentNotificationContainer container =
        (InstructionCommentNotificationContainer) result;
    final INaviInstruction instruction = container.getInstruction();

    assertEquals(new CAddress(4608), instruction.getAddress());
    assertEquals(CommentOperation.APPEND, container.getOperation());
    assertEquals(new Integer(3333), container.getCommentId());
    assertEquals(CommentScope.GLOBAL, container.getScope());
  }

  @Test
  public void testGlobalInstructionCommentParsingDelete() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification =
        new MockPGNotification("comment_changes", "bn_instructions UPDATE 1 4608 null");
    final CommentNotification result = PostgreSQLCommentNotificationParser
        .processInstructionGlobalCommentNotification(notification, provider);

    assertNotNull(result);

    final InstructionCommentNotificationContainer container =
        (InstructionCommentNotificationContainer) result;
    final INaviInstruction instruction = container.getInstruction();

    assertEquals(new CAddress(4608), instruction.getAddress());
    assertEquals(CommentOperation.DELETE, container.getOperation());
    assertNull(container.getCommentId());
    assertEquals(CommentScope.GLOBAL, container.getScope());
  }

  @Test
  public void testGroupNodeCommentParsingAppendComment() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification =
        new MockPGNotification("comment_changes", "bn_group_nodes UPDATE 8888 3333");
    final CommentNotification result = PostgreSQLCommentNotificationParser
        .processGroupNodeCommentNotification(notification, provider);

    assertNotNull(result);

    final GroupNodeCommentNotificationContainer container =
        (GroupNodeCommentNotificationContainer) result;
    final INaviGroupNode groupNode = container.getNode();

    assertEquals(8888, groupNode.getId());
    assertEquals(CommentOperation.APPEND, container.getOperation());
    assertEquals(new Integer(3333), container.getCommentId());
  }

  @Test
  public void testGroupNodeCommentParsingDeleteComment() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification =
        new MockPGNotification("comment_changes", "bn_group_nodes UPDATE 8888 null");
    final CommentNotification result = PostgreSQLCommentNotificationParser
        .processGroupNodeCommentNotification(notification, provider);

    assertNotNull(result);

    final GroupNodeCommentNotificationContainer container =
        (GroupNodeCommentNotificationContainer) result;
    final INaviGroupNode groupNode = container.getNode();

    assertEquals(8888, groupNode.getId());
    assertEquals(CommentOperation.DELETE, container.getOperation());
    assertNull(container.getCommentId());
  }

  @Test
  public void testLocalCodeNodeCommentParsingAppend() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification =
        new MockPGNotification("comment_changes", "bn_code_nodes UPDATE 1 1111 4608 3333");
    CommentNotification result = PostgreSQLCommentNotificationParser
        .processNodeLocalNodeCommentNotification(notification, provider);
    assertNotNull(result);

    final CodeNodeCommentNotificationContainer container =
        (CodeNodeCommentNotificationContainer) result;
    final INaviCodeNode node = container.getNode();

    assertEquals(1111, node.getId());
    assertEquals(new CAddress(4608), node.getAddress());
    assertEquals(CommentOperation.APPEND, container.getOperation());
    assertEquals(new Integer(3333), container.getCommentId());
    assertEquals(CommentScope.LOCAL, container.getScope());
  }

  @Test
  public void testLocalCodeNodeCommentParsingDelete() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification =
        new MockPGNotification("comment_changes", "bn_code_nodes UPDATE 1 1111 4608 null");
    final CommentNotification result = PostgreSQLCommentNotificationParser
        .processNodeLocalNodeCommentNotification(notification, provider);

    assertNotNull(result);

    final CodeNodeCommentNotificationContainer container =
        (CodeNodeCommentNotificationContainer) result;
    final INaviCodeNode node = container.getNode();

    assertEquals(1111, node.getId());
    assertEquals(new CAddress(4608), node.getAddress());
    assertEquals(CommentOperation.DELETE, container.getOperation());
    assertNull(container.getCommentId());
    assertEquals(CommentScope.LOCAL, container.getScope());
  }

  @Test
  public void testLocalEdgeCommentParsingAppend() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification =
        new MockPGNotification("comment_changes", "bn_edges UPDATE 4444 3333");
    final CommentNotification result = PostgreSQLCommentNotificationParser
        .processEdgeLocalCommentNotification(notification, provider);

    assertNotNull(result);

    final EdgeCommentNotificationContainer container = (EdgeCommentNotificationContainer) result;
    final INaviEdge edge = container.getEdge();

    assertEquals(4444, edge.getId());
    assertEquals(CommentOperation.APPEND, container.getOperation());
    assertEquals(new Integer(3333), container.getCommentId());
    assertEquals(CommentScope.LOCAL, container.getScope());
  }

  @Test
  public void testLocalEdgeCommentParsingDelete() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification =
        new MockPGNotification("comment_changes", "bn_edges UPDATE 4444 null");
    final CommentNotification result = PostgreSQLCommentNotificationParser
        .processEdgeLocalCommentNotification(notification, provider);

    assertNotNull(result);

    final EdgeCommentNotificationContainer container = (EdgeCommentNotificationContainer) result;
    final INaviEdge edge = container.getEdge();

    assertEquals(4444, edge.getId());
    assertEquals(CommentOperation.DELETE, container.getOperation());
    assertNull(container.getCommentId());
    assertEquals(CommentScope.LOCAL, container.getScope());
  }

  @Test
  public void testLocalInstructionCommentParsingAppend() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification = new MockPGNotification("comment_changes",
        "bn_codenode_instructions UPDATE 1 1111 0 4608 3333");
    CommentNotification result = PostgreSQLCommentNotificationParser
        .processNodeLocalInstructionCommentNotification(notification, provider);

    assertNotNull(result);

    final InstructionCommentNotificationContainer container =
        (InstructionCommentNotificationContainer) result;
    final INaviInstruction instruction = container.getInstruction();

    assertEquals(new CAddress(4608), instruction.getAddress());
    assertEquals(CommentOperation.APPEND, container.getOperation());
    assertEquals(new Integer(3333), container.getCommentId());
    assertEquals(CommentScope.LOCAL, container.getScope());
  }

  @Test
  public void testLocalInstructionCommentParsingDelete() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification = new MockPGNotification("comment_changes",
        "bn_codenode_instructions UPDATE 1 1111 0 4608 null");
    CommentNotification result = PostgreSQLCommentNotificationParser
        .processNodeLocalInstructionCommentNotification(notification, provider);
    assertNotNull(result);

    final InstructionCommentNotificationContainer container =
        (InstructionCommentNotificationContainer) result;
    final INaviInstruction instruction = container.getInstruction();

    assertEquals(new CAddress(4608), instruction.getAddress());
    assertEquals(CommentOperation.DELETE, container.getOperation());
    assertNull(container.getCommentId());
    assertEquals(CommentScope.LOCAL, container.getScope());
  }

  @Test(expected = NullPointerException.class)
  public void testPostgreSQLNotificationParserArguments1() {
    parser.parse(null, provider);
  }

  @Test(expected = NullPointerException.class)
  public void testPostgreSQLNotificationParserArguments2() {
    parser.parse(notifications, null);
  }

  @Test
  public void testTextNodeCommentParsingAppendComment() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification =
        new MockPGNotification("comment_changes", "bn_text_nodes UPDATE 7777 3333");
    final CommentNotification result = PostgreSQLCommentNotificationParser
        .processTextNodeCommentNotification(notification, provider);

    assertNotNull(result);

    final TextNodeCommentNotificationContainer container =
        (TextNodeCommentNotificationContainer) result;
    final INaviTextNode textNode = container.getNode();

    assertEquals(7777, textNode.getId());
    assertEquals(CommentOperation.APPEND, container.getOperation());
    assertEquals(new Integer(3333), container.getCommentId());
  }

  @Test
  public void testTextNodeCommentParsingDeleteComment() {
    new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    MockPGNotification notification =
        new MockPGNotification("comment_changes", "bn_text_nodes UPDATE 7777 null");
    final CommentNotification result = PostgreSQLCommentNotificationParser
        .processTextNodeCommentNotification(notification, provider);

    assertNotNull(result);

    final TextNodeCommentNotificationContainer container =
        (TextNodeCommentNotificationContainer) result;
    final INaviTextNode textNode = container.getNode();

    assertEquals(7777, textNode.getId());
    assertEquals(CommentOperation.DELETE, container.getOperation());
    assertNull(container.getCommentId());
  }

  @Test
  public void testTypeInstanceCommentParsingAppendComment() throws CouldntSaveDataException,
      CouldntLoadDataException {
    final INaviModule module =
        new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    module.getContent().getSections()
        .createSection("A", new CAddress(0), new CAddress(1234), SectionPermission.READ, null);
    final TypeInstance fakeInstance = module.getContent().getTypeInstanceContainer().createInstance(
        "TYPE INSTANCE", null, module.getTypeManager().getTypes().get(0),
        module.getContent().getSections().getSection(0), 0);

    MockPGNotification notification = new MockPGNotification("comment_changes",
        "bn_type_instances UPDATE 1 " + fakeInstance.getId() + " 3333");

    final CommentNotification result = PostgreSQLCommentNotificationParser
        .processTypeInstanceCommentNotification(notification, provider);

    assertNotNull(result);

    final TypeInstanceCommentNotificationContainer container =
        (TypeInstanceCommentNotificationContainer) result;
    assertNotNull(container);

    final TypeInstance instance = container.getInstance();
    assertEquals(fakeInstance.getId(), instance.getId());
    assertEquals(CommentOperation.APPEND, container.getOperation());
    assertEquals(new Integer(3333), container.getCommentId());
  }

  @Test
  public void testTypeInstanceCommentParsingDeleteComment() throws CouldntSaveDataException,
      CouldntLoadDataException {
    final INaviModule module =
        new MockModule(provider, Lists.newArrayList(mockView), Lists.newArrayList(mockFunction));
    module.getContent().getSections()
        .createSection("A", new CAddress(0), new CAddress(1234), SectionPermission.READ, null);
    final TypeInstance fakeInstance = module.getContent().getTypeInstanceContainer().createInstance(
        "TYPE INSTANCE", null, module.getTypeManager().getTypes().get(0),
        module.getContent().getSections().getSection(0), 0);

    final PGNotification notification = new MockPGNotification("comment_changes",
        "bn_type_instances UPDATE 1 " + fakeInstance.getId() + " null");

    final CommentNotification result = PostgreSQLCommentNotificationParser
        .processTypeInstanceCommentNotification(notification, provider);

    assertNotNull(result);

    final TypeInstanceCommentNotificationContainer container =
        (TypeInstanceCommentNotificationContainer) result;
    assertNotNull(container);

    final TypeInstance instance = container.getInstance();
    assertEquals(fakeInstance.getId(), instance.getId());
    assertEquals(CommentOperation.DELETE, container.getOperation());
    assertNull(container.getCommentId());
  }
}
