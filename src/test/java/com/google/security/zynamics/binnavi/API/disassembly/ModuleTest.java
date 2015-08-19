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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.API.reil.InternalTranslationException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.disassembly.CFunction;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.FunctionType;
import com.google.security.zynamics.zylib.general.Convert;
import com.google.security.zynamics.zylib.types.trees.Tree;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(JUnit4.class)
public final class ModuleTest {
  private Module m_module;

  private static ExpressionType getType(final String value) {
    return Convert.isDecString(value) ? ExpressionType.ImmediateInteger : ExpressionType.Register;
  }

  public Instruction createInstruction(final Module module, final long lAddress,
      final String mnemonic, final String firstOperand) {
    final Address address = new Address(lAddress);
    final byte[] data = {1, 2, 3, 4};

    final List<Operand> operands = new ArrayList<Operand>();

    final OperandExpression ex2 =
        OperandExpression.create(module, firstOperand, getType(firstOperand));

    operands.add(Operand.create(module, ex2));

    return Instruction.create(module, address, mnemonic, operands, data, "x86-32");
  }

  public Instruction createInstruction(final Module module, final long lAddress,
      final String mnemonic, final String firstOperand, final String secondOperand) {
    final Address address = new Address(lAddress);
    final byte[] data = {1, 2, 3, 4};

    final List<Operand> operands = new ArrayList<Operand>();

    final OperandExpression ex2 =
        OperandExpression.create(module, firstOperand, getType(firstOperand));

    final OperandExpression ex4 =
        OperandExpression.create(module, secondOperand, getType(secondOperand));

    operands.add(Operand.create(module, ex2));
    operands.add(Operand.create(module, ex4));

    return Instruction.create(module, address, mnemonic, operands, data, "x86-32");
  }

  @Before
  public void setUp() throws FileReadException, CouldntLoadDataException, LoadCancelledException {
    ConfigManager.instance().read();

    final MockSqlProvider provider = new MockSqlProvider();

    final Date creationDate = new Date();
    final Date modificationDate = new Date();

    final CModule internalModule =
        new CModule(123, "Name", "Comment", creationDate, modificationDate,
            "12345678123456781234567812345678", "1234567812345678123456781234567812345678", 55, 66,
            new CAddress(0x555), new CAddress(0x666), new com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate(1, "Mock Debugger",
                "localhaus", 88, provider), null, Integer.MAX_VALUE, false, provider);
    internalModule.load();

    final TagManager nodeTagManager =
        new TagManager(new CTagManager(new Tree<CTag>(new TreeNode<CTag>(new CTag(0, "", "",
            TagType.NODE_TAG, provider))), TagType.NODE_TAG, provider));
    final TagManager viewTagManager =
        new TagManager(new CTagManager(new Tree<CTag>(new TreeNode<CTag>(new CTag(0, "", "",
            TagType.VIEW_TAG, provider))), TagType.VIEW_TAG, provider));

    final Database db = new Database(new MockDatabase());

    m_module = new Module(db, internalModule, nodeTagManager, viewTagManager);
  }

  @Test
  public void testConstructor() {
    final MockSqlProvider provider = new MockSqlProvider();

    final Date creationDate = new Date();
    final Date modificationDate = new Date();

    final CModule internalModule =
        new CModule(123, "Name", "Comment", creationDate, modificationDate,
            "12345678123456781234567812345678", "1234567812345678123456781234567812345678", 55, 66,
            new CAddress(0x555), new CAddress(0x666), new com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate(1, "Mock Debugger",
                "localhaus", 88, provider), null, Integer.MAX_VALUE, false, provider);

    final TagManager nodeTagManager =
        new TagManager(new CTagManager(new Tree<CTag>(new TreeNode<CTag>(new CTag(0, "", "",
            TagType.NODE_TAG, provider))), TagType.NODE_TAG, provider));
    final TagManager viewTagManager =
        new TagManager(new CTagManager(new Tree<CTag>(new TreeNode<CTag>(new CTag(0, "", "",
            TagType.VIEW_TAG, provider))), TagType.VIEW_TAG, provider));

    final Database db = new Database(new MockDatabase());

    final Module module = new Module(db, internalModule, nodeTagManager, viewTagManager);

    assertEquals("Name", module.getName());
    assertEquals("Comment", module.getDescription());
    assertNotSame(creationDate, module.getCreationDate());
    assertNotSame(modificationDate, module.getModificationDate());
    assertTrue(creationDate.equals(module.getCreationDate()));
    assertTrue(modificationDate.equals(module.getModificationDate()));
    assertEquals(db, module.getDatabase());
    assertNotNull(module.getDebugger());
    assertEquals(0x555, module.getFilebase().toLong());
    assertEquals(0x666, module.getImagebase().toLong());
    assertEquals("12345678123456781234567812345678", module.getMD5());
    assertEquals("1234567812345678123456781234567812345678", module.getSHA1());
    assertEquals("Module 'Name'", module.toString());
  }

  @Test
  public void testCreateInstruction() throws InternalTranslationException {
    final Address address = new Address(0x100);
    final String mnemonic = "mov";
    final byte[] data = {1, 2, 3, 4};

    final List<Operand> operands = new ArrayList<Operand>();

    final OperandExpression ex2 =
        OperandExpression.create(m_module, "eax", ExpressionType.Register);

    final OperandExpression ex4 =
        OperandExpression.create(m_module, "123", ExpressionType.ImmediateInteger);

    operands.add(Operand.create(m_module, ex2));
    operands.add(Operand.create(m_module, ex4));

    assertEquals("dword", operands.get(0).getRootNode().getValue());
    assertEquals(1, operands.get(0).getRootNode().getChildren().size());
    assertEquals("eax", operands.get(0).getRootNode().getChildren().get(0).getValue());

    assertEquals("dword", operands.get(1).getRootNode().getValue());
    assertEquals(1, operands.get(1).getRootNode().getChildren().size());
    assertEquals("123", operands.get(1).getRootNode().getChildren().get(0).getValue());

    final Instruction instruction =
        Instruction.create(m_module, address, mnemonic, operands, data, "x86-32");

    assertEquals("dword", instruction.getOperands().get(0).getRootNode().getValue());
    assertEquals(1, instruction.getOperands().get(0).getRootNode().getChildren().size());
    assertEquals("eax", instruction.getOperands().get(0).getRootNode().getChildren().get(0)
        .getValue());

    assertEquals("dword", instruction.getOperands().get(1).getRootNode().getValue());
    assertEquals(1, instruction.getOperands().get(1).getRootNode().getChildren().size());
    assertEquals("123", instruction.getOperands().get(1).getRootNode().getChildren().get(0)
        .getValue());

    instruction.getReilCode();
  }

  @Test
  public void testCreateView() throws InternalTranslationException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException,
      CouldntDeleteException, CouldntSaveDataException {
    final List<Instruction> instructions =
        Lists.newArrayList(createInstruction(m_module, 0x2000, "mov", "edi", "edi"),
            createInstruction(m_module, 0x2001, "push", "ebp"),
            createInstruction(m_module, 0x2002, "mov", "ebp", "esp"),
            createInstruction(m_module, 0x2003, "mov", "eax", "123"),
            createInstruction(m_module, 0x2004, "add", "eax", "ecx"));

    for (final Instruction instruction : instructions) {
      instruction.getReilCode();
    }

    m_module.load();

    assertNotNull(m_module.getFunction(m_module.getViews().get(1)));

    final View view = m_module.createView("Empty View", "");

    final Function function = m_module.getFunctions().get(0);

    view.createCodeNode(function, instructions);

    view.save();

    final MockModuleListener listener = new MockModuleListener();
    m_module.addListener(listener);

    assertEquals(3, m_module.getViews().size());

    m_module.deleteView(view);

    assertEquals("deletedView;", listener.events);
    assertEquals(2, m_module.getViews().size());
    assertFalse(m_module.getViews().contains(view));

    m_module.close();
  }

  @Test
  public void testSetDebuggerTemplate() throws CouldntSaveDataException {
    final MockModuleListener listener = new MockModuleListener();

    m_module.addListener(listener);

    final DebuggerTemplate template =
        m_module.getDatabase().getDebuggerTemplateManager()
            .createDebuggerTemplate("New Debugger", "localhaus", 88);
    m_module.setDebuggerTemplate(template);

    assertEquals(template, m_module.getDebuggerTemplate());
    assertEquals("changedDebuggerTemplate;changedDebugger;changedModificationDate;",
        listener.events);

    m_module.removeListener(listener);
  }

  @Test
  public void testSetDescription() throws CouldntSaveDataException {
    final MockModuleListener listener = new MockModuleListener();

    m_module.addListener(listener);

    m_module.setDescription("New Description");

    assertEquals("New Description", m_module.getDescription());
    assertEquals("changedDescription;changedModificationDate;", listener.events);

    m_module.removeListener(listener);
  }

  @Test
  public void testSetFilebase() throws CouldntSaveDataException {
    final MockModuleListener listener = new MockModuleListener();

    m_module.addListener(listener);

    m_module.setFilebase(new Address(0x100));

    assertEquals(0x100, m_module.getFilebase().toLong());
    assertEquals("changedFilebase;changedModificationDate;", listener.events);

    m_module.removeListener(listener);
  }

  @Test
  public void testSetImagebase() throws CouldntSaveDataException {
    final MockModuleListener listener = new MockModuleListener();

    m_module.addListener(listener);

    m_module.setImagebase(new Address(0x100));

    assertEquals(0x100, m_module.getImagebase().toLong());
    assertEquals("changedImagebase;changedModificationDate;", listener.events);

    m_module.removeListener(listener);
  }

  @Test
  public void testSetName() throws CouldntSaveDataException {
    final MockModuleListener listener = new MockModuleListener();

    m_module.addListener(listener);

    m_module.setName("New Name");

    assertEquals("New Name", m_module.getName());
    assertEquals("changedName;changedModificationDate;", listener.events);

    m_module.removeListener(listener);
  }

  @Test
  public void testUnloaded() {
    m_module.close();
    try {
      m_module.getCallgraph();
      fail();
    } catch (final IllegalStateException exception) {
    }

    try {
      final MockSqlProvider provider = new MockSqlProvider();
      final CModule internalModule =
          new CModule(1, "", "", new Date(), new Date(), "00000000000000000000000000000000",
              "0000000000000000000000000000000000000000", 0, 0, new CAddress(0), new CAddress(0),
              null, null, Integer.MAX_VALUE, false, provider);
      final CFunction parentFunction =
          new CFunction(internalModule, new MockView(), new CAddress(0x123), "Mock Function",
              "Mock Function", "Mock Description", 0, 0, 0, 0, FunctionType.NORMAL, "", 0, null,
              null, null, provider);

      m_module.getFunction(parentFunction);
      fail();
    } catch (final IllegalStateException exception) {
    }

    try {
      m_module.getFunctions();
      fail();
    } catch (final IllegalStateException exception) {
    }

    try {
      m_module.getViews();
      fail();
    } catch (final IllegalStateException exception) {
    }

    try {
      m_module.getTraces();
      fail();
    } catch (final IllegalStateException exception) {
    }
  }
}
