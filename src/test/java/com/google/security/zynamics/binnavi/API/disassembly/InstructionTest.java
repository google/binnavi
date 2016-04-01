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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.API.reil.InternalTranslationException;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.MockTagManager;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.CInstruction;
import com.google.security.zynamics.binnavi.disassembly.COperandTree;
import com.google.security.zynamics.binnavi.disassembly.COperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.reil.Architecture;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;
import com.google.security.zynamics.zylib.disassembly.IReference;
import com.google.security.zynamics.zylib.types.trees.Tree;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(JUnit4.class)
public final class InstructionTest {
  @Test
  public void testCommentInitialization() throws CouldntLoadDataException, LoadCancelledException {
    final SQLProvider provider = new MockSqlProvider();

    final CModule internalModule =
        new CModule(123, "Name", "Comment", new Date(), new Date(),
            "12345678123456781234567812345678", "1234567812345678123456781234567812345678", 55, 66,
            new CAddress(0x555), new CAddress(0x666), new DebuggerTemplate(1, "Mock Debugger",
                "localhaus", 88, provider), null, Integer.MAX_VALUE, false, provider);
    internalModule.load();

    final COperandTreeNode rootNode1 =
        new COperandTreeNode(1, IOperandTree.NODE_TYPE_REGISTER_ID, "eax", null,
            new ArrayList<IReference>(), provider, internalModule.getTypeManager(), internalModule
                .getContent().getTypeInstanceContainer());
    final COperandTreeNode rootNode2 =
        new COperandTreeNode(1, IOperandTree.NODE_TYPE_REGISTER_ID, "ebx", null,
            new ArrayList<IReference>(), provider, internalModule.getTypeManager(), internalModule
                .getContent().getTypeInstanceContainer());

    final COperandTree operand1 =
        new COperandTree(rootNode1, provider, internalModule.getTypeManager(), internalModule
            .getContent().getTypeInstanceContainer());
    final COperandTree operand2 =
        new COperandTree(rootNode2, provider, internalModule.getTypeManager(), internalModule
            .getContent().getTypeInstanceContainer());
    final List<COperandTree> operands = Lists.newArrayList(operand1, operand2);
    final CInstruction internalInstruction =
        new CInstruction(false, internalModule, new CAddress(0x123), "mov", operands, new byte[] {
            1, 2, 3}, Architecture.x86, provider);

    final Instruction instruction = new Instruction(internalInstruction);

    final MockInstructionListener listener = new MockInstructionListener();

    instruction.addListener(listener);

    final ArrayList<IComment> comment =
        Lists.<IComment>newArrayList(new CComment(null, CommonTestObjects.TEST_USER_1, null,
            "Hannes"));

    instruction.initializeComment(comment);

    assertEquals(comment, internalInstruction.getGlobalComment());
    assertEquals(comment, instruction.getComment());
    // TODO (timkornau): check if double messages are what we want here of rather not.
    // assertEquals("InitializedComment;", listener.events);

    instruction.removeListener(listener);
  }

  @Test
  public void testConstructor() throws CouldntLoadDataException, LoadCancelledException {
    final SQLProvider provider = new MockSqlProvider();

    final CModule internalModule =
        new CModule(123, "Name", "Comment", new Date(), new Date(),
            "12345678123456781234567812345678", "1234567812345678123456781234567812345678", 55, 66,
            new CAddress(0x555), new CAddress(0x666), new DebuggerTemplate(1, "Mock Debugger",
                "localhaus", 88, provider), null, Integer.MAX_VALUE, false, provider);
    internalModule.load();

    final COperandTreeNode rootNode1 =
        new COperandTreeNode(1, IOperandTree.NODE_TYPE_REGISTER_ID, "eax", null,
            new ArrayList<IReference>(), provider, internalModule.getTypeManager(), internalModule
                .getContent().getTypeInstanceContainer());
    final COperandTreeNode rootNode2 =
        new COperandTreeNode(1, IOperandTree.NODE_TYPE_REGISTER_ID, "ebx", null,
            new ArrayList<IReference>(), provider, internalModule.getTypeManager(), internalModule
                .getContent().getTypeInstanceContainer());

    final COperandTree operand1 =
        new COperandTree(rootNode1, provider, internalModule.getTypeManager(), internalModule
            .getContent().getTypeInstanceContainer());
    final COperandTree operand2 =
        new COperandTree(rootNode2, provider, internalModule.getTypeManager(), internalModule
            .getContent().getTypeInstanceContainer());
    final List<COperandTree> operands = Lists.newArrayList(operand1, operand2);
    final CInstruction internalInstruction =
        new CInstruction(false, internalModule, new CAddress(0x123), "mov", operands, new byte[] {
            1, 2, 3}, Architecture.x86, provider);

    final Instruction instruction = new Instruction(internalInstruction);

    assertEquals(0x123, instruction.getAddress().toLong());
    assertEquals(null, instruction.getComment());
    assertArrayEquals(new byte[] {1, 2, 3}, instruction.getData());
    assertEquals("mov", instruction.getMnemonic());
    assertEquals(2, instruction.getOperands().size());
    assertEquals("eax", instruction.getOperands().get(0).getRootNode().getChildren().get(0)
        .getValue());
    assertEquals("ebx", instruction.getOperands().get(1).getRootNode().getChildren().get(0)
        .getValue());
    assertEquals("123  mov eax, ebx", instruction.toString());
    assertEquals(Architecture.x86, instruction.getArchitecture());
  }

  @Test
  public void testCreate() throws CouldntLoadDataException, LoadCancelledException {
    final SQLProvider provider = new MockSqlProvider();

    final CModule internalModule =
        new CModule(123, "Name", "Comment", new Date(), new Date(),
            "12345678123456781234567812345678", "1234567812345678123456781234567812345678", 55, 66,
            new CAddress(0x555), new CAddress(0x666), new DebuggerTemplate(1, "Mock Debugger",
                "localhaus", 88, provider), null, Integer.MAX_VALUE, false, provider);
    internalModule.load();

    final Database database = new Database(new MockDatabase());

    final CTagManager mockTagManager =
        new CTagManager(new Tree<CTag>(new TreeNode<CTag>(new CTag(1, "Root", "", TagType.NODE_TAG,
            provider))), TagType.NODE_TAG, provider);

    final TagManager nodeTagManager = new TagManager(mockTagManager);
    final TagManager viewTagManager =
        new TagManager(new MockTagManager(
            com.google.security.zynamics.binnavi.Tagging.TagType.VIEW_TAG));

    final Module module = new Module(database, internalModule, nodeTagManager, viewTagManager);
    final List<Operand> operands = new ArrayList<Operand>();
    final OperandExpression ex2 = OperandExpression.create(module, "eax", ExpressionType.Register);
    final OperandExpression ex4 = OperandExpression.create(module, "ebx", ExpressionType.Register);
    operands.add(Operand.create(module, ex2));
    operands.add(Operand.create(module, ex4));

    final Instruction instruction =
        Instruction.create(module, new Address(0x123), "mov", operands, new byte[] {1, 2, 3},
            "x86-32");

    assertEquals(0x123, instruction.getAddress().toLong());
    assertEquals(null, instruction.getComment());
    assertArrayEquals(new byte[] {1, 2, 3}, instruction.getData());
    assertEquals("mov", instruction.getMnemonic());
    assertEquals(2, instruction.getOperands().size());
    assertEquals("eax", instruction.getOperands().get(0).getRootNode().getChildren().get(0)
        .getValue());
    assertEquals("ebx", instruction.getOperands().get(1).getRootNode().getChildren().get(0)
        .getValue());
    assertEquals("123  mov eax, ebx", instruction.toString());
  }

  @Test
  public void testReil() throws InternalTranslationException, CouldntLoadDataException,
      LoadCancelledException {
    final SQLProvider provider = new MockSqlProvider();

    final CModule internalModule =
        new CModule(123, "Name", "Comment", new Date(), new Date(),
            "12345678123456781234567812345678", "1234567812345678123456781234567812345678", 55, 66,
            new CAddress(0x555), new CAddress(0x666), new DebuggerTemplate(1, "Mock Debugger",
                "localhaus", 88, provider), null, Integer.MAX_VALUE, false, provider);
    internalModule.load();

    final COperandTreeNode rootNode1 =
        new COperandTreeNode(1, IOperandTree.NODE_TYPE_REGISTER_ID, "eax", null,
            new ArrayList<IReference>(), provider, internalModule.getTypeManager(), internalModule
                .getContent().getTypeInstanceContainer());
    final COperandTreeNode rootNode2 =
        new COperandTreeNode(1, IOperandTree.NODE_TYPE_REGISTER_ID, "ebx", null,
            new ArrayList<IReference>(), provider, internalModule.getTypeManager(), internalModule
                .getContent().getTypeInstanceContainer());

    final COperandTree operand1 =
        new COperandTree(rootNode1, provider, internalModule.getTypeManager(), internalModule
            .getContent().getTypeInstanceContainer());
    final COperandTree operand2 =
        new COperandTree(rootNode2, provider, internalModule.getTypeManager(), internalModule
            .getContent().getTypeInstanceContainer());
    final List<COperandTree> operands = Lists.newArrayList(operand1, operand2);
    final CInstruction internalInstruction =
        new CInstruction(false, internalModule, new CAddress(0x123), "mov", operands, new byte[] {
            1, 2, 3}, Architecture.x86, provider);

    final Instruction instruction = new Instruction(internalInstruction);

    instruction.getReilCode();
  }
}
