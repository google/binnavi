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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Tagging.MockTagManager;
import com.google.security.zynamics.binnavi.disassembly.CFunction;
import com.google.security.zynamics.binnavi.disassembly.CFunctionContainerHelper;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.Modules.CFunctionContainer;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.FunctionType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;

@RunWith(JUnit4.class)
public final class ModuleHelpersTest {
  @Test
  public void testGetFunction_1() {
    final Database database = new Database(new MockDatabase());

    @SuppressWarnings("unused")
    final MockModule mockModule = new MockModule();

    final MockSqlProvider provider = new MockSqlProvider();
    final CModule internalModule = new CModule(1,
        "",
        "",
        new Date(),
        new Date(),
        "00000000000000000000000000000000",
        "0000000000000000000000000000000000000000",
        0,
        0,
        new CAddress(0),
        new CAddress(0),
        null,
        null,
        Integer.MAX_VALUE,
        false,
        provider);

    try {
      internalModule.load();
    } catch (final CouldntLoadDataException exception) {
      CUtilityFunctions.logException(exception);
    } catch (final LoadCancelledException exception) {
      CUtilityFunctions.logException(exception);
    }

    @SuppressWarnings("unused")
    final CFunction parentFunction = new CFunction(internalModule,
        new MockView(),
        new CAddress(0x123),
        "Mock Function",
        "Mock Function",
        "Mock Description",
        0,
        0,
        0,
        0,
        FunctionType.NORMAL,
        "",
        0,
        null,
        null,
        null, provider);

    final TagManager nodeTagManager = new TagManager(
        new MockTagManager(com.google.security.zynamics.binnavi.Tagging.TagType.NODE_TAG));
    final TagManager viewTagManager = new TagManager(
        new MockTagManager(com.google.security.zynamics.binnavi.Tagging.TagType.VIEW_TAG));

    final Module module = new Module(database, internalModule, nodeTagManager, viewTagManager);

    assertEquals(module.getFunctions().get(0), ModuleHelpers.getFunction(module, 0x123));
    assertNull(ModuleHelpers.getFunction(module, 0x1235));

    try {
      ModuleHelpers.getFunction(null, -1);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      ModuleHelpers.getFunction(module, -1);
      fail();
    } catch (final IllegalArgumentException e) {
    }
  }

  @Test
  public void testGetFunction_2() throws IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    final Database database = new Database(new MockDatabase());

    final MockModule mockModule = new MockModule();

    final MockSqlProvider provider = new MockSqlProvider();
    final CModule internalModule = new CModule(1,
        "",
        "",
        new Date(),
        new Date(),
        "00000000000000000000000000000000",
        "0000000000000000000000000000000000000000",
        0,
        0,
        new CAddress(0),
        new CAddress(0),
        null,
        null,
        Integer.MAX_VALUE,
        false,
        provider);
    final CFunction parentFunction = new CFunction(internalModule,
        new MockView(),
        new CAddress(0x123),
        "Mock Function",
        "Mock Function",
        "Mock Description",
        0,
        0,
        0,
        0,
        FunctionType.NORMAL,
        "",
        0,
        null,
        null,
        null, provider);

    CFunctionContainerHelper.addFunction(mockModule.getContent().getFunctionContainer(),
        parentFunction);

    final TagManager nodeTagManager = new TagManager(
        new MockTagManager(com.google.security.zynamics.binnavi.Tagging.TagType.NODE_TAG));
    final TagManager viewTagManager = new TagManager(
        new MockTagManager(com.google.security.zynamics.binnavi.Tagging.TagType.VIEW_TAG));

    final Module module = new Module(database, mockModule, nodeTagManager, viewTagManager);

    assertEquals(module.getFunctions().get(0),
        ModuleHelpers.getFunction(module, new Address(0x123)));
    assertNull(ModuleHelpers.getFunction(module, new Address(0x1235)));

    try {
      ModuleHelpers.getFunction(null, (Address) null);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      ModuleHelpers.getFunction(module, (Address) null);
      fail();
    } catch (final NullPointerException e) {
    }
  }

  @Test
  public void testGetFunction_3() throws IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    final Database database = new Database(new MockDatabase());

    final MockModule mockModule = new MockModule();

    final MockSqlProvider provider = new MockSqlProvider();
    final CModule internalModule = new CModule(1,
        "",
        "",
        new Date(),
        new Date(),
        "00000000000000000000000000000000",
        "0000000000000000000000000000000000000000",
        0,
        0,
        new CAddress(0),
        new CAddress(0),
        null,
        null,
        Integer.MAX_VALUE,
        false,
        provider);
    final CFunction parentFunction = new CFunction(internalModule,
        new MockView(),
        new CAddress(0x123),
        "Mock Function",
        "Mock Function",
        "Mock Description",
        0,
        0,
        0,
        0,
        FunctionType.NORMAL,
        "",
        0,
        null,
        null,
        null, provider);

    new CFunctionContainer(mockModule, Lists.<INaviFunction>newArrayList(parentFunction));
    CFunctionContainerHelper.addFunction(mockModule.getContent().getFunctionContainer(),
        parentFunction);
    final TagManager nodeTagManager = new TagManager(
        new MockTagManager(com.google.security.zynamics.binnavi.Tagging.TagType.NODE_TAG));
    final TagManager viewTagManager = new TagManager(
        new MockTagManager(com.google.security.zynamics.binnavi.Tagging.TagType.VIEW_TAG));

    final Module module = new Module(database, mockModule, nodeTagManager, viewTagManager);

    assertEquals(module.getFunctions().get(0), ModuleHelpers.getFunction(module, "Mock Function"));
    assertNull(ModuleHelpers.getFunction(module, ""));

    try {
      ModuleHelpers.getFunction(null, (String) null);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      ModuleHelpers.getFunction(module, (String) null);
      fail();
    } catch (final NullPointerException e) {
    }
  }
}
