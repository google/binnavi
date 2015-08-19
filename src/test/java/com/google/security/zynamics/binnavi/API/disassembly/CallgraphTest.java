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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(JUnit4.class)
public final class CallgraphTest {
  @Test
  public void testConstructor() {
    final List<FunctionBlock> nodes = new ArrayList<FunctionBlock>();

    final MockSqlProvider provider = new MockSqlProvider();
    final CModule internalModule =
        new CModule(1, "", "", new Date(), new Date(), "00000000000000000000000000000000",
            "0000000000000000000000000000000000000000", 0, 0, new CAddress(0), new CAddress(0),
            null, null, Integer.MAX_VALUE, false, provider);
    final CFunction parentFunction = new CFunction(
        internalModule, new MockView(), new CAddress(0x123), "Mock Function", "Mock Function",
        "Mock Description", 0, 0, 0, 0, FunctionType.NORMAL, "", 0, null, null, null, provider);

    final FunctionBlock b1 = new FunctionBlock(new Function(ModuleFactory.get(), parentFunction));
    final FunctionBlock b2 = new FunctionBlock(new Function(ModuleFactory.get(), parentFunction));

    nodes.add(b1);
    nodes.add(b2);

    final List<FunctionEdge> edges = new ArrayList<FunctionEdge>();

    final FunctionEdge e1 = new FunctionEdge(b1, b2);

    edges.add(e1);

    final Callgraph cg = new Callgraph(nodes, edges);

    assertEquals(2, cg.getNodes().size());
    assertEquals(b1, cg.getNodes().get(0));
    assertEquals(b2, cg.getNodes().get(1));

    assertEquals(1, cg.getEdges().size());
    assertEquals(e1, cg.getEdges().get(0));

    assertEquals("Callgraph [2 function nodes, 1 edges]", cg.toString());
  }
}
