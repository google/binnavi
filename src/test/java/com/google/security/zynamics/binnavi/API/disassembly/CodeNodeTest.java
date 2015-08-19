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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.API.reil.InternalTranslationException;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.MockTagManager;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.CFunction;
import com.google.security.zynamics.binnavi.disassembly.CInstruction;
import com.google.security.zynamics.binnavi.disassembly.COperandTree;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.MockInstruction;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.FunctionType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

@RunWith(JUnit4.class)
public final class CodeNodeTest {
  private CodeNode m_node;

  @Before
  public void setUp() {
    final Database database = new Database(new MockDatabase());

    final MockModule mockModule = new MockModule();

    final TagManager nodeTagManager =
        new TagManager(new MockTagManager(
            com.google.security.zynamics.binnavi.Tagging.TagType.NODE_TAG));
    final TagManager viewTagManager =
        new TagManager(new MockTagManager(
            com.google.security.zynamics.binnavi.Tagging.TagType.VIEW_TAG));

    final Module module = new Module(database, mockModule, nodeTagManager, viewTagManager);

    final MockView mockView = new MockView();

    final View view = new View(module, mockView, nodeTagManager, viewTagManager);

    final MockSqlProvider provider = new MockSqlProvider();

    final CModule internalModule =
        new CModule(123, "Name", "Comment", new Date(), new Date(),
            "12345678123456781234567812345678", "1234567812345678123456781234567812345678", 55, 66,
            new CAddress(0x555), new CAddress(0x666), new DebuggerTemplate(1, "Mock Debugger",
                "localhaus", 88, provider), null, Integer.MAX_VALUE, false, provider);

    final CFunction internalFunction = new CFunction(
        internalModule, new MockView(), new CAddress(0x123), "Mock Function", "Mock Function",
        "Mock Description", 0, 0, 0, 0, FunctionType.NORMAL, "", 0, null, null, null, provider);

    final CComment m_globalComment =
        new CComment(null, CommonTestObjects.TEST_USER_1, null, "Global Comment");

    final INaviCodeNode codeNode =
        new CCodeNode(0, 0, 0, 0, 0, Color.RED, Color.RED, false, false,
            Lists.<IComment>newArrayList(m_globalComment), internalFunction, new HashSet<CTag>(),
            new MockSqlProvider());
    codeNode.addInstruction(new CInstruction(true, internalModule, new CAddress(0x123), "nop",
        new ArrayList<COperandTree>(), new byte[] {(byte) 0x90}, "x86-32", provider), null);

    m_node = new CodeNode(view, codeNode, nodeTagManager);
  }

  @Test
  public void testAddInstruction() {
    final MockCodeNodeListener listener = new MockCodeNodeListener();

    m_node.addListener(listener);

    final Instruction instruction = new Instruction(new MockInstruction());

    final Instruction clonedInstruction = m_node.addInstruction(instruction);

    m_node.setInstructionColor(instruction, 10000, Color.RED);

    assertEquals("addedInstruction;", listener.events);

    m_node.removeInstruction(clonedInstruction);

    try {
      m_node.setInstructionColor(clonedInstruction, 10000, Color.RED);
      fail();
    } catch (final IllegalArgumentException e) {
    }

    assertEquals("addedInstruction;removedInstruction;", listener.events);

    m_node.removeListener(listener);
  }

  @Test
  public void testConstructor() throws InternalTranslationException {
    assertEquals(0x123, m_node.getAddress().toLong());
    assertNotNull(m_node.getLocalComments());
    assertEquals(1, m_node.getLocalComments().size());
    assertEquals(new CComment(null, CommonTestObjects.TEST_USER_1, null, "Global Comment"), m_node
        .getLocalComments().get(0));
    assertEquals(1, m_node.getInstructions().size());
    assertEquals("123  nop \n", m_node.toString());

    assertNotNull(m_node.getReilCode());
  }
}
