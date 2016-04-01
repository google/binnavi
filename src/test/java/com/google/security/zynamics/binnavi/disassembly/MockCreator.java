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

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Database.CModuleViewGenerator;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.reil.Architecture;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.FunctionType;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;
import com.google.security.zynamics.zylib.disassembly.IReference;
import com.google.security.zynamics.zylib.disassembly.ViewType;

public final class MockCreator {
  final static SecureRandom random = new SecureRandom();

  public static CAddressSpace createAddressSpace(final SQLProvider sql) {
    return new CAddressSpace(1, "AS Name", "AS Description", new Date(), new Date(),
        new HashMap<INaviModule, IAddress>(), null, sql, new MockProject());
  }

  public static DebuggerTemplate createDebuggerTemplate(final SQLProvider sql) {
    return new DebuggerTemplate(1, "Mock Debugger Template", "localhost", 1234, sql);
  }

  public static CFunction createFunction(final CModule module, final SQLProvider sql) {
    return new CFunction(module, new MockView(), new CAddress(0x1200), "Mock Function",
        "Mock Function", "Mock Function Description", 5, 6, 7, 8, FunctionType.NORMAL, null, 0,
        null, null, null, sql);
  }

  public static CFunction createFunction(final CModule module, final SQLProvider sql,
      final INaviView view) {
    return new CFunction(module, view, new CAddress(0x1200), "Mock Function", "Mock Function",
        "Mock Function Description", 5, 6, 7, 8, FunctionType.NORMAL, null, 0, null, null, null, sql);
  }

  public static CInstruction createInstruction(final CModule module, final SQLProvider sql) {
    return new CInstruction(false, module, new CAddress(0x123), "nop",
        Lists.newArrayList(new COperandTree(new COperandTreeNode(1,
            IOperandTree.NODE_TYPE_IMMEDIATE_INT_ID, "123", null, new ArrayList<IReference>(), sql,
            module.getTypeManager(), module.getContent().getTypeInstanceContainer()), sql, module
            .getTypeManager(), module.getContent().getTypeInstanceContainer())),
        new byte[] {(byte) 0x41}, Architecture.x86, sql);
  }

  public static CInstruction createInstructionWithOperand(final BigInteger address,
      final INaviModule module, final SQLProvider sql) {
    return new CInstruction(false, module, new CAddress(address), "inc",
        Lists.newArrayList(new COperandTree(new COperandTreeNode(1,
            IOperandTree.NODE_TYPE_REGISTER_ID, "eax", null, new ArrayList<IReference>(), sql,
            module.getTypeManager(), module.getContent().getTypeInstanceContainer()), sql, module
            .getTypeManager(), module.getContent().getTypeInstanceContainer())),
        new byte[] {(byte) 0x90}, Architecture.x86, sql);
  }

  public static CModule createModule() {
    return createModule(new MockSqlProvider());
  }

  public static CModule createModule(final SQLProvider sql) {
    return new CModule(123, "Mock Name", "Mock Comment", new Date(), new Date(),
        CommonTestObjects.MD5, CommonTestObjects.SHA1, 55, 66, new CAddress(0x555), new CAddress(
            0x666), null, null, Integer.MAX_VALUE, false, sql);
  }

  public static CView createNativeView(final SQLProvider sql, final CModule module) {
    final CModuleViewGenerator generator = new CModuleViewGenerator(sql, module);
    return generator.generate(1, "Mock View", "Mock View Description", ViewType.Native,
        GraphType.MIXED_GRAPH, new Date(), new Date(), 1, 1, new HashSet<CTag>(),
        new HashSet<CTag>(), false);
  }

  public static CProject createProject(final SQLProvider sql) {
    return new CProject(1, "Mock Project", "Mock Project Description", new Date(), new Date(), 0,
        new ArrayList<DebuggerTemplate>(), sql);
  }

  public static CView createView(final int id, final SQLProvider sql, final CModule module) {
    final CModuleViewGenerator generator = new CModuleViewGenerator(sql, module);
    return generator.generate(id, "Mock View", "Mock View Description", ViewType.NonNative,
        GraphType.MIXED_GRAPH, new Date(), new Date(), 1, 1, new HashSet<CTag>(),
        new HashSet<CTag>(), false);
  }

  public static CView createView(final SQLProvider sql, final CModule module) {
    final CModuleViewGenerator generator = new CModuleViewGenerator(sql, module);
    return generator.generate(new BigInteger(31, random).intValue(), "Mock View",
        "Mock View Description", ViewType.NonNative, GraphType.MIXED_GRAPH, new Date(), new Date(),
        1, 1, new HashSet<CTag>(), new HashSet<CTag>(), false);
  }


  public static CView createView(final SQLProvider sql, final CModule module, final ViewType type) {
    final CModuleViewGenerator generator = new CModuleViewGenerator(sql, module);
    return generator.generate(new BigInteger(31, random).intValue(), "Mock View",
        "Mock View Description", type, GraphType.MIXED_GRAPH, new Date(), new Date(), 1, 1,
        new HashSet<CTag>(), new HashSet<CTag>(), false);
  }

  public static CTag createViewTag(final SQLProvider sql) {
    return new CTag(1, "Mock Tag", "Mock Tag Description", TagType.VIEW_TAG, sql);
  }
}
