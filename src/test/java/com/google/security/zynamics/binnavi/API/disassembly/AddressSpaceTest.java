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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.MockTagManager;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.MockProject;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.types.trees.Tree;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;
import java.util.LinkedHashMap;

@RunWith(JUnit4.class)
public final class AddressSpaceTest {
  private AddressSpace addressSpace;
  private CAddressSpace internalAddressSpace;
  private Database database;

  @SuppressWarnings("unused")
  private final TagManager nodeManager = new TagManager(
      new MockTagManager(com.google.security.zynamics.binnavi.Tagging.TagType.NODE_TAG));
  @SuppressWarnings("unused")
  private final TagManager viewManager = new TagManager(
      new MockTagManager(com.google.security.zynamics.binnavi.Tagging.TagType.VIEW_TAG));

  private Date creationDate;
  private Date modificationDate;
  private Module module;

  @Before
  public void setUp() {
    final MockSqlProvider provider = new MockSqlProvider();

    creationDate = new Date();
    modificationDate = new Date();

    final MockDatabase mockDb = new MockDatabase();
    database = new Database(mockDb);

    internalAddressSpace = new CAddressSpace(1,
        "Mock Space",
        "Mock Space Description",
        creationDate,
        modificationDate,
        new LinkedHashMap<INaviModule, IAddress>(),
        null,
        provider,
        new MockProject());

    addressSpace = new AddressSpace(database, null, internalAddressSpace);

    final Date creationDate = new Date();
    final Date modificationDate = new Date();

    final CModule internalModule = new CModule(123,
        "Name",
        "Comment",
        creationDate,
        modificationDate,
        "12345678123456781234567812345678",
        "1234567812345678123456781234567812345678",
        55,
        66,
        new CAddress(0x555),
        new CAddress(0x666),
        new com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate(1, "Mock Debugger",
            "localhaus", 88, provider),
        null,
        Integer.MAX_VALUE,
        false,
        provider);
    mockDb.getContent().addModule(internalModule);

    final TagManager nodeTagManager = new TagManager(new CTagManager(
        new Tree<CTag>(new TreeNode<CTag>(new CTag(0, "", "", TagType.NODE_TAG, provider))),
        TagType.NODE_TAG, provider));
    final TagManager viewTagManager = new TagManager(new CTagManager(
        new Tree<CTag>(new TreeNode<CTag>(new CTag(0, "", "", TagType.VIEW_TAG, provider))),
        TagType.VIEW_TAG, provider));

    module = new Module(database, internalModule, nodeTagManager, viewTagManager);
  }

  @Test
  public void testConstructor() {
    assertEquals("Mock Space", addressSpace.getName());
    assertEquals("Mock Space Description", addressSpace.getDescription());
    assertEquals(creationDate, addressSpace.getCreationDate());
    assertEquals(modificationDate, addressSpace.getModificationDate());

    assertEquals("Address space Mock Space [unloaded, 0 modules]", addressSpace.toString());
  }

  @Test
  public void testConstructorAlternative()
      throws com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException,
      com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException,
      LoadCancelledException {
    final MockSqlProvider provider = new MockSqlProvider();

    final MockDatabase mockDb = new MockDatabase(provider);
    final Database database = new Database(mockDb);

    final com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate template =
        new com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate(1, "", "", 0,
            provider);
    mockDb.getContent().getDebuggerTemplateManager().addDebugger(template);

    final CModule internalModule = new CModule(123,
        "Name",
        "Comment",
        new Date(),
        new Date(),
        "12345678123456781234567812345678",
        "1234567812345678123456781234567812345678",
        55,
        66,
        new CAddress(0x555),
        new CAddress(0x666),
        new com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate(1, "Mock Debugger",
            "localhaus", 88, provider),
        null,
        Integer.MAX_VALUE,
        false,
        provider);
    mockDb.getContent().addModule(internalModule);

    final CAddressSpace internalAddressSpace = new CAddressSpace(1,
        "Mock Space",
        "Mock Space Description",
        new Date(),
        new Date(),
        new LinkedHashMap<INaviModule, IAddress>(),
        null,
        provider,
        new MockProject());
    internalAddressSpace.load();
    internalAddressSpace.getConfiguration().setDebuggerTemplate(template);
    internalAddressSpace.getContent().addModule(internalModule);

    final Project project = ProjectFactory.get();
    final AddressSpace addressSpace = new AddressSpace(database, project, internalAddressSpace);

    assertEquals(1, addressSpace.getModules().size());
    assertNotNull(addressSpace.getDebuggerTemplate());
    assertNotNull(addressSpace.getDebugger());
  }

  @Test
  public void testGetImageBase() throws CouldntSaveDataException, CouldntLoadDataException {
    addressSpace.load();

    final MockAddressSpaceListener listener = new MockAddressSpaceListener();

    addressSpace.addModule(module);

    addressSpace.addListener(listener);

    addressSpace.setImageBase(module, new Address(0x1234));

    assertEquals(0x1234, addressSpace.getImagebase(module).toLong());
    assertEquals("changedImageBase;changedModificationDate;", listener.events);

    addressSpace.removeListener(listener);
  }

  @Test
  public void testLoad() throws CouldntSaveDataException, CouldntLoadDataException {
    final MockAddressSpaceListener listener = new MockAddressSpaceListener();

    addressSpace.addListener(listener);

    addressSpace.load();

    addressSpace.addModule(module);

    assertEquals("loaded;addedModule;changedImageBase;changedModificationDate;", listener.events);

    assertEquals(1, addressSpace.getModules().size());

    assertNotNull(addressSpace.toString());

    addressSpace.close();

    assertEquals("loaded;addedModule;changedImageBase;changedModificationDate;closing;closed;",
        listener.events);
  }

  @Test
  public void testModules()
      throws CouldntSaveDataException,
      com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException,
      CouldntLoadDataException,
      CouldntDeleteException,
      CouldntDeleteException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException {
    addressSpace.load();

    final MockAddressSpaceListener listener = new MockAddressSpaceListener();

    addressSpace.addListener(listener);

    addressSpace.addModule(module);

    assertEquals("addedModule;changedImageBase;changedModificationDate;", listener.events);
    assertEquals(1, addressSpace.getModules().size());

    assertTrue(database.getModules().contains(addressSpace.getModules().get(0)));

    addressSpace.removeModule(module);

    assertEquals(0, addressSpace.getModules().size());
    assertEquals(
        "addedModule;changedImageBase;changedModificationDate;removedModule;changedModificationDate;",
        listener.events);

    internalAddressSpace.getContent().addModule(module.getNative());

    assertEquals(1, addressSpace.getModules().size());
    assertEquals(
        "addedModule;changedImageBase;changedModificationDate;removedModule;changedModificationDate;addedModule;",
        listener.events);

    internalAddressSpace.getContent().removeModule(module.getNative());

    assertEquals(0, addressSpace.getModules().size());
    assertEquals(
        "addedModule;changedImageBase;changedModificationDate;removedModule;changedModificationDate;addedModule;removedModule;changedModificationDate;",
        listener.events);

    addressSpace.removeListener(listener);
  }

  @Test
  public void testSetDebuggerTemplate() throws CouldntSaveDataException, CouldntLoadDataException {
    addressSpace.load();

    final MockAddressSpaceListener listener = new MockAddressSpaceListener();

    addressSpace.addListener(listener);

    final DebuggerTemplate template =
        database.getDebuggerTemplateManager().createDebuggerTemplate("foo", "", 0);

    addressSpace.setDebuggerTemplate(template);

    assertEquals("changedDebugger;changedModificationDate;", listener.events);
    assertNotNull(addressSpace.getDebugger());
    assertEquals(template, addressSpace.getDebuggerTemplate());

    addressSpace.setDebuggerTemplate(null);

    assertEquals("changedDebugger;changedModificationDate;changedDebugger;changedModificationDate;",
        listener.events);
    assertNull(addressSpace.getDebugger());
    assertNull(addressSpace.getDebuggerTemplate());

    addressSpace.removeListener(listener);
  }

  @Test
  public void testSetDescription() throws CouldntSaveDataException {
    final MockAddressSpaceListener listener = new MockAddressSpaceListener();

    addressSpace.addListener(listener);

    addressSpace.setDescription("Foo");

    assertEquals("changedDescription;changedModificationDate;", listener.events);

    addressSpace.removeListener(listener);
  }

  @Test
  public void testSetName() throws CouldntSaveDataException {
    final MockAddressSpaceListener listener = new MockAddressSpaceListener();

    addressSpace.addListener(listener);

    addressSpace.setName("Foo");

    assertEquals("changedName;changedModificationDate;", listener.events);

    addressSpace.removeListener(listener);
  }
}
