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
package com.google.security.zynamics.binnavi.Database.NodeParser;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockCodeNodeData;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockCodeNodeProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Database.NodeParser.CCodeNodeParser;
import com.google.security.zynamics.binnavi.Database.NodeParser.ParserException;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.CFunctionContainerHelper;
import com.google.security.zynamics.binnavi.disassembly.CReference;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.MockFunction;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.ReilTranslator;
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.ReferenceType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;
import java.util.List;

@RunWith(JUnit4.class)
public final class CCodeNodeParserTest {
  @Before
  public void setUp() {
    System.out.println("Available Memory: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024)
        + " MB");
  }

  @Test
  public void testAddZero() throws ParserException, CPartialLoadException,
      IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
    // 00000000: mov eax, [esp+0]

    final MockCodeNodeProvider cnProvider = new MockCodeNodeProvider();

    final MockCodeNodeData instruction2 = new MockCodeNodeData();

    instruction2.nodeId = 5193;
    instruction2.address = new CAddress(0x4180dd);
    instruction2.mnemonic = "mov";
    instruction2.operandPosition = 0;
    instruction2.expressionId = 1;
    instruction2.expressionType = 6;
    instruction2.symbol = "b4";
    instruction2.immediate = null;
    instruction2.parentId = 0;
    instruction2.replacement = null;

    final MockCodeNodeData instruction3 = new MockCodeNodeData();

    instruction3.nodeId = 5193;
    instruction3.address = new CAddress(0x4180dd);
    instruction3.mnemonic = "mov";
    instruction3.operandPosition = 0;
    instruction3.expressionId = 29;
    instruction3.expressionType = 4;
    instruction3.symbol = "ss:";
    instruction3.immediate = null;
    instruction3.parentId = 1;
    instruction3.replacement = null;

    final MockCodeNodeData instruction4 = new MockCodeNodeData();

    instruction4.nodeId = 5193;
    instruction4.address = new CAddress(0x4180dd);
    instruction4.mnemonic = "mov";
    instruction4.operandPosition = 0;
    instruction4.expressionId = 30;
    instruction4.expressionType = 7;
    instruction4.symbol = "[";
    instruction4.immediate = null;
    instruction4.parentId = 29;
    instruction4.replacement = null;

    final MockCodeNodeData instruction5 = new MockCodeNodeData();

    instruction5.nodeId = 5193;
    instruction5.address = new CAddress(0x4180dd);
    instruction5.mnemonic = "mov";
    instruction5.operandPosition = 0;
    instruction5.expressionId = 31;
    instruction5.expressionType = 4;
    instruction5.symbol = "+";
    instruction5.immediate = null;
    instruction5.parentId = 30;
    instruction5.replacement = null;

    final MockCodeNodeData instruction6 = new MockCodeNodeData();

    instruction6.nodeId = 5193;
    instruction6.address = new CAddress(0x4180dd);
    instruction6.mnemonic = "mov";
    instruction6.operandPosition = 0;
    instruction6.expressionId = 32;
    instruction6.expressionType = 5;
    instruction6.symbol = "esp";
    instruction6.immediate = null;
    instruction6.parentId = 31;
    instruction6.replacement = null;

    final MockCodeNodeData instruction7 = new MockCodeNodeData();

    instruction7.nodeId = 5193;
    instruction7.address = new CAddress(0x4180dd);
    instruction7.mnemonic = "mov";
    instruction7.operandPosition = 0;
    instruction7.expressionId = 498;
    instruction7.expressionType = 2;
    instruction7.symbol = null;
    instruction7.immediate = "0";
    instruction7.parentId = 31;
    instruction7.replacement = "134h+var_134";

    final MockCodeNodeData instruction1 = new MockCodeNodeData();

    instruction1.nodeId = 5193;
    instruction1.address = new CAddress(0x4180dd);
    instruction1.mnemonic = "mov";
    instruction1.operandPosition = 1;
    instruction1.expressionId = 1594;
    instruction1.expressionType = 2;
    instruction1.symbol = null;
    instruction1.immediate = "4561216";
    instruction1.parentId = 0;
    instruction1.replacement = null;

    cnProvider.data.add(instruction2);
    cnProvider.data.add(instruction3);
    cnProvider.data.add(instruction4);
    cnProvider.data.add(instruction5);
    cnProvider.data.add(instruction6);
    cnProvider.data.add(instruction7);
    cnProvider.data.add(instruction1);

    final MockSqlProvider provider = new MockSqlProvider();

    final MockModule module = new MockModule();
    CFunctionContainerHelper.addFunction(module.getContent().getFunctionContainer(),
        new MockFunction(0));

    final CCodeNodeParser p = new CCodeNodeParser(cnProvider, Lists.newArrayList(module), provider);

    final List<CCodeNode> result = p.parse();

    assertEquals(1, result.size());
    assertEquals(1, Iterables.size(result.get(0).getInstructions()));

    final ReilTranslator<INaviInstruction> translator = new ReilTranslator<INaviInstruction>();

    try {
      translator.translate(new StandardEnvironment(),
          Iterables.get(result.get(0).getInstructions(), 0));
    } catch (final InternalTranslationException exception) {
      CUtilityFunctions.logException(exception);
    }
  }

  @Test
  public void testEmpty() throws ParserException, CPartialLoadException {
    final MockCodeNodeProvider cnProvider = new MockCodeNodeProvider();

    final MockSqlProvider provider = new MockSqlProvider();

    final CCodeNodeParser p =
        new CCodeNodeParser(cnProvider, Lists.newArrayList(new MockModule()), provider);

    final List<CCodeNode> result = p.parse();

    assertEquals(0, result.size());
  }

  @Test
  public void testMultipleOperands() throws ParserException, CPartialLoadException,
      IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
    // 00000000: mov eax, ebx

    final MockCodeNodeProvider cnProvider = new MockCodeNodeProvider();

    final MockCodeNodeData instruction1a = new MockCodeNodeData();

    instruction1a.mnemonic = "mov";
    instruction1a.nodeId = 0;
    instruction1a.expressionId = 0;
    instruction1a.expressionType = 5;
    instruction1a.operandPosition = 0;
    instruction1a.symbol = "eax";

    final MockCodeNodeData instruction1b = new MockCodeNodeData();

    instruction1b.mnemonic = "mov";
    instruction1b.nodeId = 0;
    instruction1b.expressionId = 1;
    instruction1b.expressionType = 5;
    instruction1b.operandPosition = 1;
    instruction1b.symbol = "ebx";

    cnProvider.data.add(instruction1a);
    cnProvider.data.add(instruction1b);

    final MockSqlProvider provider = new MockSqlProvider();

    final MockModule module = new MockModule();
    CFunctionContainerHelper.addFunction(module.getContent().getFunctionContainer(),
        new MockFunction(0));

    final CCodeNodeParser p = new CCodeNodeParser(cnProvider, Lists.newArrayList(module), provider);

    final List<CCodeNode> result = p.parse();

    assertEquals(1, result.size());
    assertEquals(1, Iterables.size(result.get(0).getInstructions()));

    final INaviInstruction instruction = Iterables.getFirst(result.get(0).getInstructions(), null);

    assertEquals("mov", instruction.getMnemonic());

    assertEquals("dword", instruction.getOperands().get(0).getRootNode().getValue());
    assertEquals("eax", instruction.getOperands().get(0).getRootNode().getChildren().get(0)
        .getValue());

    assertEquals("dword", instruction.getOperands().get(1).getRootNode().getValue());
    assertEquals("ebx", instruction.getOperands().get(1).getRootNode().getChildren().get(0)
        .getValue());
  }

  @Test
  public void testReference() throws ParserException, CPartialLoadException,
      IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
    // 00000000: jz 123

    final MockCodeNodeProvider cnProvider = new MockCodeNodeProvider();

    final MockCodeNodeData instruction1 = new MockCodeNodeData();

    instruction1.reference =
        new CReference(new CAddress(BigInteger.valueOf(123)), ReferenceType.CONDITIONAL_TRUE);
    instruction1.nodeId = 0;
    instruction1.expressionId = 0;
    instruction1.expressionType = 2;
    instruction1.operandPosition = 0;
    instruction1.immediate = "123";

    cnProvider.data.add(instruction1);

    final MockSqlProvider provider = new MockSqlProvider();

    final MockModule module = new MockModule();
    CFunctionContainerHelper.addFunction(module.getContent().getFunctionContainer(),
        new MockFunction(0));

    final CCodeNodeParser p = new CCodeNodeParser(cnProvider, Lists.newArrayList(module), provider);

    final List<CCodeNode> result = p.parse();

    assertEquals(1, result.size());
    assertEquals(1, Iterables.size(result.get(0).getInstructions()));
  }

  @Test
  public void testSameOperandTwice() throws ParserException, CPartialLoadException,
      IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
    // 00000000: jz eax
    // 00000001: jg eax

    final MockCodeNodeProvider cnProvider = new MockCodeNodeProvider();

    final MockCodeNodeData instruction1 = new MockCodeNodeData();

    instruction1.nodeId = 0;
    instruction1.expressionId = 0;
    instruction1.expressionType = 5;
    instruction1.operandPosition = 0;
    instruction1.symbol = "eax";

    final MockCodeNodeData instruction2 = new MockCodeNodeData();

    instruction2.address = new CAddress(BigInteger.ONE);
    instruction2.nodeId = 1;
    instruction2.expressionId = 0;
    instruction2.expressionType = 5;
    instruction2.operandPosition = 0;
    instruction2.symbol = "eax";

    cnProvider.data.add(instruction1);
    cnProvider.data.add(instruction2);

    final MockSqlProvider provider = new MockSqlProvider();

    final MockModule module = new MockModule();
    CFunctionContainerHelper.addFunction(module.getContent().getFunctionContainer(),
        new MockFunction(0));

    final CCodeNodeParser p = new CCodeNodeParser(cnProvider, Lists.newArrayList(module), provider);

    final List<CCodeNode> result = p.parse();

    assertEquals(2, result.size());
    assertEquals(1, Iterables.size(result.get(0).getInstructions()));
    assertEquals(1, Iterables.size(result.get(1).getInstructions()));
  }

  @Test
  public void testSingleInstruction() throws ParserException, CPartialLoadException,
      IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
    // nop

    final MockCodeNodeProvider cnProvider = new MockCodeNodeProvider();

    cnProvider.data.add(new MockCodeNodeData());

    final MockSqlProvider provider = new MockSqlProvider();

    final MockModule module = new MockModule();
    CFunctionContainerHelper.addFunction(module.getContent().getFunctionContainer(),
        new MockFunction(0));
    final CCodeNodeParser p = new CCodeNodeParser(cnProvider, Lists.newArrayList(module), provider);

    final List<CCodeNode> result = p.parse();

    assertEquals(1, result.size());
  }
}
