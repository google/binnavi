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
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.binnavi.API.reil.InternalTranslationException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.disassembly.CBasicBlock;
import com.google.security.zynamics.binnavi.disassembly.CBlockNode;
import com.google.security.zynamics.binnavi.disassembly.CFunction;
import com.google.security.zynamics.binnavi.disassembly.IBlockNode;
import com.google.security.zynamics.binnavi.disassembly.INaviBasicBlock;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.MockInstruction;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.FunctionType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(JUnit4.class)
public final class BasicBlockTest {
  @Test
  public void testConstructor() throws InternalTranslationException {
    final MockSqlProvider provider = new MockSqlProvider();
    final CModule internalModule =
        new CModule(1, "", "", new Date(), new Date(), "00000000000000000000000000000000",
            "0000000000000000000000000000000000000000", 0, 0, new CAddress(0), new CAddress(0),
            null, null, Integer.MAX_VALUE, false, provider);
    final CFunction internalFunction = new CFunction(
        internalModule, new MockView(), new CAddress(0x123), "Mock Function", "Mock Function",
        "Mock Description", 0, 0, 0, 0, FunctionType.NORMAL, "", 0, null, null, null, provider);

    final List<INaviInstruction> instructions = new ArrayList<INaviInstruction>();
    instructions.add(new MockInstruction(0x123));

    final INaviBasicBlock bblock = new CBasicBlock(1, "Hannes", instructions);

    final IBlockNode node = new CBlockNode(bblock);

    final Function pFunction = new Function(ModuleFactory.get(), internalFunction);

    final BasicBlock block = new BasicBlock(node, pFunction);

    assertEquals(0x123, block.getAddress().toLong());
    assertEquals(pFunction, block.getParentFunction());
    assertEquals("Hannes", block.getComment());
    assertEquals(0x123, block.getInstructions().get(0).getAddress().toLong());
    assertNotNull(block.getReilCode());
    assertTrue(block.getChildren().isEmpty());
    assertTrue(block.getParents().isEmpty());
    assertEquals("123  nop \n", block.toString());
  }
}
