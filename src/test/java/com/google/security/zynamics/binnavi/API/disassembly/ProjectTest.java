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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.disassembly.CFunction;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.FunctionType;
import com.google.security.zynamics.zylib.types.trees.Tree;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Date;

@RunWith(JUnit4.class)
public final class ProjectTest {
  private Project m_project;
  private CProject m_internalProject;

  private final Date creationDate = new Date();
  private final Date modificationDate = new Date();

  private final MockSqlProvider provider = new MockSqlProvider();

  private final MockDatabase internalDatabase = new MockDatabase(provider);

  private final Database database = new Database(internalDatabase);

  @SuppressWarnings("deprecation")
  @Before
  public void setUp() {
    modificationDate.setYear(creationDate.getYear() + 1);

    m_internalProject = new CProject(1,
        "Project Name",
        "Project Description",
        creationDate,
        modificationDate,
        1,
        new ArrayList<com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate>(),
        provider);

    final TagManager nodeTagManager = new TagManager(new CTagManager(
        new Tree<CTag>(new TreeNode<CTag>(new CTag(1, "", "", TagType.NODE_TAG, provider))),
        TagType.NODE_TAG, provider));
    final TagManager viewTagManager = new TagManager(new CTagManager(
        new Tree<CTag>(new TreeNode<CTag>(new CTag(1, "", "", TagType.VIEW_TAG, provider))),
        TagType.VIEW_TAG, provider));

    m_project = new Project(database, m_internalProject, nodeTagManager, viewTagManager);
  }

  @Test
  public void testConstructor() {
    assertEquals(database, m_project.getDatabase());
    assertEquals("Project Name", m_project.getName());
    assertEquals("Project Description", m_project.getDescription());
    assertEquals(creationDate, m_project.getCreationDate());
    assertEquals(modificationDate, m_project.getModificationDate());
    assertFalse(m_project.isLoaded());
    assertEquals("Project 'Project Name' [unloaded, 1 address spaces]", m_project.toString());
  }

  @Test
  public void testCreateAddressSpace() throws CouldntLoadDataException, CouldntSaveDataException {
    final MockProjectListener listener = new MockProjectListener();

    m_project.load();

    m_project.addListener(listener);

    final AddressSpace space = m_project.createAddressSpace("Hannes Space");
    space.load();

    final INaviModule nativeModule = new MockModule(provider);
    internalDatabase.getContent().addModule(nativeModule);

    final Module module = ModuleFactory.get(nativeModule, provider);
    module.load();

    space.addModule(module);

    assertEquals(space, m_project.getAddressSpaces().get(1));
    assertEquals("Hannes Space",
        m_internalProject.getContent().getAddressSpaces().get(1).getConfiguration().getName());
    assertEquals("addedAddressSpace;changedModificationDate;", listener.events);

    assertEquals(0, m_project.getFunctions().size());

    m_project.removeListener(listener);
  }

  @Test
  public void testCreateView() throws CouldntLoadDataException, CouldntSaveDataException {
    final MockProjectListener listener = new MockProjectListener();

    m_project.load();

    m_project.addListener(listener);

    final View view = m_project.createView("N A", "D A");

    assertEquals(view, m_project.getViews().get(0));
    assertEquals("N A", m_internalProject.getContent().getViews().get(0).getName());
    assertEquals("addedView;", listener.events);

    final View view2 = m_project.createView(view, "N B", "D B");

    assertEquals(view2, m_project.getViews().get(1));
    assertEquals("N B", m_internalProject.getContent().getViews().get(1).getName());
    assertEquals("addedView;addedView;", listener.events);

    m_project.removeListener(listener);
  }

  @Test
  public void testDebuggerTemplates() throws CouldntLoadDataException, CouldntSaveDataException,
      CouldntDeleteException {
    final MockProjectListener listener = new MockProjectListener();

    m_project.load();

    final DebuggerTemplate template =
        database.getDebuggerTemplateManager().createDebuggerTemplate("Foo", "Bar", 123);

    m_project.addListener(listener);

    m_project.addDebuggerTemplate(template);

    assertEquals(1, m_project.getDebuggerTemplates().size());
    assertEquals(template, m_project.getDebuggerTemplates().get(0));
    assertEquals("addedDebuggerTemplate;changedModificationDate;", listener.events);

    m_project.removeDebuggerTemplate(template);

    assertEquals(0, m_project.getDebuggerTemplates().size());
    assertEquals(
        "addedDebuggerTemplate;changedModificationDate;removedDebuggerTemplate;changedModificationDate;",
        listener.events);

    m_project.removeListener(listener);
  }

  @Test
  public void testDeleteAddressSpace() throws CouldntLoadDataException, CouldntDeleteException {
    final MockProjectListener listener = new MockProjectListener();

    m_project.load();

    m_project.addListener(listener);

    try {
      m_project.deleteAddressSpace(m_project.getAddressSpaces().get(0));
      fail();
    } catch (final IllegalStateException exception) {
    }

    m_project.getAddressSpaces().get(0).close();
    m_project.deleteAddressSpace(m_project.getAddressSpaces().get(0));

    assertTrue(m_project.getAddressSpaces().isEmpty());
    assertEquals("deletedAddressSpace;changedModificationDate;", listener.events);

    m_project.removeListener(listener);
  }

  @Test
  public void testDeleteView() throws CouldntLoadDataException, CouldntSaveDataException,
      CouldntDeleteException {
    final MockProjectListener listener = new MockProjectListener();

    m_project.load();

    final View view = m_project.createView("N A", "D A");
    final View view2 = m_project.createView(view, "N B", "D B");

    m_project.addListener(listener);

    m_project.deleteView(view);

    assertEquals(1, m_project.getViews().size());
    assertEquals(view2, m_project.getViews().get(0));
    assertEquals("deletedView;changedModificationDate;", listener.events);

    m_project.deleteView(view2);

    assertEquals(0, m_project.getViews().size());
    assertEquals("deletedView;changedModificationDate;deletedView;changedModificationDate;",
        listener.events);

    m_project.removeListener(listener);
  }

  @Test
  public void testLifecycle() throws CouldntLoadDataException {
    final MockProjectListener listener = new MockProjectListener();

    m_project.addListener(listener);

    m_project.load();

    assertTrue(m_project.isLoaded());
    assertTrue(m_internalProject.isLoaded());
    assertEquals("loadedProject;", listener.events);
    assertEquals("Project 'Project Name' ['']", m_project.toString());

    assertTrue(m_project.close());

    assertFalse(m_project.isLoaded());
    assertFalse(m_internalProject.isLoaded());
    assertEquals("loadedProject;closingProject;closedProject;", listener.events);

    m_project.removeListener(listener);
  }

  @Test
  public void testSetDescription() throws CouldntSaveDataException {
    final MockProjectListener listener = new MockProjectListener();

    m_project.addListener(listener);

    m_project.setDescription("New Description");

    assertEquals("New Description", m_project.getDescription());
    assertEquals("changedDescription;changedModificationDate;", listener.events);

    m_project.removeListener(listener);
  }

  @Test
  public void testSetName() throws CouldntSaveDataException {
    final MockProjectListener listener = new MockProjectListener();

    m_project.addListener(listener);

    m_project.setName("New Name");

    assertEquals("New Name", m_project.getName());
    assertEquals("changedName;changedModificationDate;", listener.events);

    m_project.removeListener(listener);
  }

  @Test
  public void testTraces() throws CouldntLoadDataException,
      com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException,
      com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException {
    final MockProjectListener listener = new MockProjectListener();

    m_project.load();

    m_project.addListener(listener);

    m_project.getNative().getContent().createTrace("foo", "bar");

    assertEquals("addedTrace;changedModificationDate;", listener.events);
    assertEquals(1, m_project.getTraces().size());
    assertEquals("foo", m_project.getTraces().get(0).getName());
    assertEquals("bar", m_project.getTraces().get(0).getDescription());

    m_project.getNative().getContent()
        .removeTrace(m_project.getNative().getContent().getTraces().get(0));

    assertEquals("addedTrace;changedModificationDate;deletedTrace;changedModificationDate;",
        listener.events);
    assertTrue(m_project.getTraces().isEmpty());
  }

  @Test
  public void testUnloaded() throws CouldntSaveDataException, CouldntLoadDataException {
    try {
      m_project.createAddressSpace("Hannes");
      fail();
    } catch (final IllegalStateException exception) {
    }

    try {
      m_project.createView("", "");
      fail();
    } catch (final IllegalStateException exception) {
    }

    try {
      final MockSqlProvider provider = new MockSqlProvider();

      final TagManager nodeTagManager = new TagManager(new CTagManager(
          new Tree<CTag>(new TreeNode<CTag>(new CTag(1, "", "", TagType.NODE_TAG, provider))),
          TagType.NODE_TAG, provider));
      final TagManager viewTagManager = new TagManager(new CTagManager(
          new Tree<CTag>(new TreeNode<CTag>(new CTag(1, "", "", TagType.VIEW_TAG, provider))),
          TagType.VIEW_TAG, provider));

      final View view = new View(m_project, new MockView(), nodeTagManager, viewTagManager);
      m_project.createView(view, "", "");
      fail();
    } catch (final IllegalStateException exception) {
    }

    try {
      m_project.getAddressSpaces();
      fail();
    } catch (final IllegalStateException exception) {
    }

    try {
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
          null,
          provider);

      m_project.getFunction(parentFunction);
      fail();
    } catch (final IllegalStateException exception) {
    }

    try {
      m_project.getTraces();
      fail();
    } catch (final IllegalStateException exception) {
    }

    try {
      m_project.getViews();
      fail();
    } catch (final IllegalStateException exception) {
    }
  }
}
