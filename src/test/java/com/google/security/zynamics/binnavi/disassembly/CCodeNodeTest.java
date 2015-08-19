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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.Users.CUserManager;
import com.google.security.zynamics.binnavi.Gui.Users.Interfaces.IUser;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.FunctionType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

@RunWith(JUnit4.class)
public final class CCodeNodeTest {
  final ArrayList<IComment> m_globalComment = Lists.<IComment>newArrayList(new CComment(null,
      CommonTestObjects.TEST_USER_1, null, "Global Comment\nTest Case\nCCodeNodeTest"));

  @Test
  public void testInstructionToLine() {
    final MockSqlProvider provider = new MockSqlProvider();
    final CModule internalModule =
        new CModule(1, "", "", new Date(), new Date(), CommonTestObjects.MD5,
            CommonTestObjects.SHA1, 0, 0, new CAddress(0), new CAddress(0), null, null,
            Integer.MAX_VALUE, false, provider);
    final CFunction parentFunction = new CFunction(
        internalModule, new MockView(), new CAddress(0x123), "Mock Function", "Mock Function",
        "Mock Description", 0, 0, 0, 0, FunctionType.NORMAL, "", 0, null, null, null, provider);

    final CCodeNode node =
        new CCodeNode(1, 0, 0, 0, 0, Color.BLACK, Color.BLACK, false, true, null, parentFunction,
            new LinkedHashSet<CTag>(), new MockSqlProvider());

    final MockInstruction i1 =
        new MockInstruction(new CAddress(0x123), "nop", new ArrayList<COperandTree>(), null);
    final MockInstruction i2 =
        new MockInstruction(new CAddress(0x124), "nop", new ArrayList<COperandTree>(), null);
    final MockInstruction i3 =
        new MockInstruction(new CAddress(0x125), "nop", new ArrayList<COperandTree>(), null);
    final MockInstruction i4 =
        new MockInstruction(new CAddress(0x126), "nop", new ArrayList<COperandTree>(),
            m_globalComment);
    final MockInstruction i5 =
        new MockInstruction(new CAddress(0x127), "nop", new ArrayList<COperandTree>(), null);

    node.addInstruction(i1, Lists.<IComment>newArrayList(new CComment(null,
        CommonTestObjects.TEST_USER_1, null, "Foo\nBar")));
    node.addInstruction(i2,
        Lists.<IComment>newArrayList(new CComment(null, CommonTestObjects.TEST_USER_1, null, "\n")));
    node.addInstruction(i3, null);
    node.addInstruction(i4, null);
    node.addInstruction(i5, null);

    // The first instruction should (naturally) start in the first line.
    assertEquals(1, CCodeNodeHelpers.instructionToLine(node, i1));
    // The second instruction should start in line 3 - because the first instruction, with the two-
    // line comment, eats up lines 1 and 2.
    assertEquals(3, CCodeNodeHelpers.instructionToLine(node, i2));
    // The third instruction needs to start in line 4 - since the second instruction is a single
    // line with an empty newline comment (??). Why is this not 2 lines then?
    assertEquals(5, CCodeNodeHelpers.instructionToLine(node, i3));
    assertEquals(6, CCodeNodeHelpers.instructionToLine(node, i4));
    assertEquals(9, CCodeNodeHelpers.instructionToLine(node, i5));

    try {
      new CCodeNode(1, 0, 0, 0, 0, Color.BLUE, Color.BLUE, false, true, null, parentFunction,
          new LinkedHashSet<CTag>(), null);
      fail();
    } catch (final NullPointerException e) {
    }

  }

  @Test
  public void testLineToInstruction() {
    final MockSqlProvider provider = new MockSqlProvider();
    final CModule internalModule =
        new CModule(1, "", "", new Date(), new Date(), CommonTestObjects.MD5,
            CommonTestObjects.SHA1, 0, 0, new CAddress(0), new CAddress(0), null, null,
            Integer.MAX_VALUE, false, provider);
    final CFunction parentFunction = new CFunction(
        internalModule, new MockView(), new CAddress(0x123), "Mock Function", "Mock Function",
        "Mock Description", 0, 0, 0, 0, FunctionType.NORMAL, "", 0, null, null, null, provider);

    final CCodeNode node =
        new CCodeNode(1, 0, 0, 0, 0, Color.BLACK, Color.BLACK, false, true, null, parentFunction,
            new LinkedHashSet<CTag>(), new MockSqlProvider());

    final MockInstruction i1 =
        new MockInstruction(new CAddress(0x123), "nop", new ArrayList<COperandTree>(), null);
    final MockInstruction i2 =
        new MockInstruction(new CAddress(0x124), "nop", new ArrayList<COperandTree>(), null);
    final MockInstruction i3 =
        new MockInstruction(new CAddress(0x125), "nop", new ArrayList<COperandTree>(), null);
    final MockInstruction i4 =
        new MockInstruction(new CAddress(0x126), "nop", new ArrayList<COperandTree>(),
            m_globalComment);
    final MockInstruction i5 =
        new MockInstruction(new CAddress(0x127), "nop", new ArrayList<COperandTree>(), null);

    node.addInstruction(i1, Lists.<IComment>newArrayList(new CComment(null,
        CommonTestObjects.TEST_USER_1, null, "Foo\nBar")));
    node.addInstruction(i2,
        Lists.<IComment>newArrayList(new CComment(null, CommonTestObjects.TEST_USER_1, null, "\n")));
    node.addInstruction(i3, null);
    node.addInstruction(i4, null);
    node.addInstruction(i5, null);

    try {
      node.addInstruction(null, null);
      fail();
    } catch (final NullPointerException e) {
    }

    assertEquals(null, CCodeNodeHelpers.lineToInstruction(node, 0));
    assertEquals(i1, CCodeNodeHelpers.lineToInstruction(node, 1));
    assertEquals(i1, CCodeNodeHelpers.lineToInstruction(node, 2));
    assertEquals(i2, CCodeNodeHelpers.lineToInstruction(node, 3));
    assertEquals(i2, CCodeNodeHelpers.lineToInstruction(node, 4));
    assertEquals(i3, CCodeNodeHelpers.lineToInstruction(node, 5));
    assertEquals(i5, CCodeNodeHelpers.lineToInstruction(node, 9));
    assertEquals(null, CCodeNodeHelpers.lineToInstruction(node, 10));
  }

  @Test
  public void testMiscFunctions() throws MaybeNullException, CouldntSaveDataException,
  CouldntLoadDataException {
    final MockSqlProvider provider = new MockSqlProvider();

    final CUserManager userManager = CUserManager.get(provider);
    final IUser currentUser = userManager.addUser("TEST USER 1");
    userManager.setCurrentActiveUser(currentUser);

    final CModule internalModule =
        new CModule(1, "", "", new Date(), new Date(), CommonTestObjects.MD5,
            CommonTestObjects.SHA1, 0, 0, new CAddress(0), new CAddress(0), null, null,
            Integer.MAX_VALUE, false, provider);
    final CFunction parentFunction = new CFunction(
        internalModule, new MockView(), new CAddress(0x123), "Mock Function", "Mock Function",
        "Mock Description", 0, 0, 0, 0, FunctionType.NORMAL, "", 0, null, null, null, provider);

    final CCodeNode node =
        new CCodeNode(1, 0, 0, 0, 0, Color.BLACK, Color.BLACK, false, true,
            Lists.<IComment>newArrayList(new CComment(null, CommonTestObjects.TEST_USER_1, null,
                "foobar")), parentFunction, new LinkedHashSet<CTag>(), provider);

    final MockInstruction i1 =
        new MockInstruction(new CAddress(0x123), "nop", new ArrayList<COperandTree>(), null);
    final MockInstruction i2 =
        new MockInstruction(new CAddress(0x124), "nop", new ArrayList<COperandTree>(), null);
    final MockInstruction i3 =
        new MockInstruction(new CAddress(0x125), "nop", new ArrayList<COperandTree>(), null);
    final MockInstruction i4 =
        new MockInstruction(new CAddress(0x126), "nop", new ArrayList<COperandTree>(),
            m_globalComment);
    final MockInstruction i5 =
        new MockInstruction(new CAddress(0x127), "nop", new ArrayList<COperandTree>(), null);

    node.addInstruction(i1,
        Lists.<IComment>newArrayList(new CComment(null, currentUser, null, "Foo\nBar")));
    node.addInstruction(i2,
        Lists.<IComment>newArrayList(new CComment(null, currentUser, null, "\n")));
    node.addInstruction(i3, null);
    node.addInstruction(i4, null);

    final INaviCodeNodeListener listener = new CNaviCodeNodeListenerAdapter();

    node.addListener(listener);

    node.addInstruction(i5, null);

    assertNotNull(node.cloneNode());
    assertFalse(CCodeNodeHelpers.containsAddress(node, new CAddress(0xFFFFFFFEL)));
    assertEquals(null, node.getComments().getGlobalCodeNodeComment());
    assertEquals(new CAddress(0x123L), node.getAddress());
    assertEquals(0, CCodeNodeHelpers.getInstruction(node, new CAddress(0x123L)));
    assertEquals(-1, CCodeNodeHelpers.getInstruction(node, new CAddress(0x129L)));
    assertEquals(i1, Iterables.getFirst(node.getInstructions(), null));
    assertEquals(5, Iterables.size(node.getInstructions()));
    assertEquals(5, node.instructionCount());
    assertNotNull(node.getParentFunction());
    node.removeInstruction(i5);
    assertEquals(4, node.instructionCount());

    try {
      node.removeInstruction(null);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      node.removeInstruction(i5);
      fail();
    } catch (final IllegalArgumentException e) {
    }

    final List<IComment> comments = node.getComments().appendGlobalCodeNodeComment("barfoos");

    assertEquals(comments, node.getComments().getGlobalCodeNodeComment());

    try {
      node.getComments().appendGlobalCodeNodeComment(null);
      fail();
    } catch (final Exception e) {
    }

    node.setInstructionColor(i4, 0, Color.YELLOW);

    try {
      node.setInstructionColor(null, 0, null);
      fail();
    } catch (final Exception e) {
    }

    try {
      node.setInstructionColor(i5, 0, null);
      fail();
    } catch (final Exception e) {
    }

    node.setBorderColor(Color.GRAY);

    assertEquals(Color.GRAY, node.getBorderColor());

    final List<IComment> comments2 = node.getComments().appendLocalCodeNodeComment("barfoos2");

    assertEquals(comments2, node.getComments().getLocalCodeNodeComment());

    final List<IComment> appendedComments =
        node.getComments().appendLocalInstructionComment(i4, "foo");

    assertEquals(appendedComments, node.getComments().getLocalInstructionComment(i4));

    try {
      node.getComments().appendLocalInstructionComment(i5, " INSTRUCTION i5 COMMENT ");
      fail();
    } catch (final IllegalArgumentException e) {
    }

    node.getComments().appendLocalCodeNodeComment("foo");

    node.toString();

    node.removeListener(listener);

    node.close();
  }
}
