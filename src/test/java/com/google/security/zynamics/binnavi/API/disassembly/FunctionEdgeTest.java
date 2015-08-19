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

import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.disassembly.CFunction;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.FunctionType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;

@RunWith(JUnit4.class)
public final class FunctionEdgeTest {
  @Test
  public void testConstructor() {
    final MockSqlProvider provider = new MockSqlProvider();
    final CModule internalModule =
        new CModule(1, "", "", new Date(), new Date(), "00000000000000000000000000000000",
            "0000000000000000000000000000000000000000", 0, 0, new CAddress(0), new CAddress(0),
            null, null, Integer.MAX_VALUE, false, provider);
    final CFunction parentFunction = new CFunction(
        internalModule, new MockView(), new CAddress(0x123), "Mock Function", "Mock Function",
        "Mock Description", 0, 0, 0, 0, FunctionType.NORMAL, "", 0, null, null,
        null, provider);

    final Function function = new Function(ModuleFactory.get(), parentFunction);

    final FunctionBlock block = new FunctionBlock(function);
    final FunctionBlock childBlock = new FunctionBlock(function);

    final FunctionEdge edge = new FunctionEdge(block, childBlock);

    assertEquals("Function Edge [Mock Function -> Mock Function]", edge.toString());
    assertEquals(block, edge.getSource());
    assertEquals(childBlock, edge.getTarget());

    assertEquals(0, block.getParents().size());
    assertEquals(1, block.getChildren().size());
    assertEquals(childBlock, block.getChildren().get(0));

    assertEquals(0, childBlock.getChildren().size());
    assertEquals(1, childBlock.getParents().size());
    assertEquals(block, childBlock.getParents().get(0));
  }
}
