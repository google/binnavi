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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.binnavi.API.disassembly.CouldntConnectException;
import com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException;
import com.google.security.zynamics.binnavi.API.disassembly.CouldntInitializeDatabaseException;
import com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDriverException;
import com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.API.disassembly.Database;
import com.google.security.zynamics.binnavi.API.disassembly.DebuggerTemplateManager;
import com.google.security.zynamics.binnavi.API.disassembly.InvalidDatabaseException;
import com.google.security.zynamics.binnavi.API.disassembly.InvalidDatabaseFormatException;
import com.google.security.zynamics.binnavi.API.disassembly.InvalidDatabaseVersionException;
import com.google.security.zynamics.binnavi.API.disassembly.Project;
import com.google.security.zynamics.binnavi.API.disassembly.TagManager;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.disassembly.CProjectFactory;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public final class DatabaseTest {
  @Test
  public void testConnect() throws CouldntLoadDriverException, CouldntConnectException,
      InvalidDatabaseException, CouldntInitializeDatabaseException, InvalidDatabaseFormatException,
      CouldntLoadDataException, InvalidDatabaseVersionException {
    final MockDatabaseListener listener = new MockDatabaseListener();

    final MockDatabase mockDatabase = new MockDatabase();
    mockDatabase.getContent().m_modules.add(CModuleFactory.get());
    mockDatabase.getContent().m_projects.add(CProjectFactory.get());

    final Database database = new Database(mockDatabase);

    database.addListener(listener);

    database.connect();

    assertTrue(database.isConnected());

    database.load();

    listener.m_allowClosing = false;

    database.close();

    listener.m_allowClosing = true;

    database.close();

    assertEquals("openedDatabase;loadedDatabase;closingDatabase;closingDatabase;closedDatabase;",
        listener.events);

    assertEquals("Database 'Mock Database' [Unloaded]", database.toString());

    database.dispose();
  }

  @Test
  public void testCreateProject() throws CouldntSaveDataException, CouldntDeleteException {
    final MockDatabaseListener listener = new MockDatabaseListener();

    final MockDatabase mockDatabase = new MockDatabase();

    final Database database = new Database(mockDatabase);

    database.addListener(listener);

    final Project newProject = database.createProject("Hannes");

    mockDatabase.getContent().addProject("Fork");

    assertEquals("addedProject;addedProject;", listener.events);
    assertEquals(2, database.getProjects().size());

    database.deleteProject(newProject);

    assertEquals("addedProject;addedProject;deletedProject;", listener.events);
    assertEquals(1, database.getProjects().size());

    database.removeListener(listener);
  }

  @Test
  public void testDeleteModule() throws CouldntLoadDriverException, CouldntConnectException,
      InvalidDatabaseException, CouldntInitializeDatabaseException, InvalidDatabaseFormatException,
      CouldntLoadDataException, InvalidDatabaseVersionException, CouldntDeleteException {
    final MockDatabaseListener listener = new MockDatabaseListener();

    final MockDatabase mockDatabase = new MockDatabase();
    mockDatabase.getContent().m_modules.add(CModuleFactory.get());

    final Database database = new Database(mockDatabase);

    database.addListener(listener);

    database.connect();
    database.load();

    database.deleteModule(database.getModules().get(0));

    assertEquals("openedDatabase;loadedDatabase;deletedModule;", listener.events);
    assertTrue(database.getModules().isEmpty());
  }

  @Test
  public void testGetDebuggerTemplateManager() {
    final MockDatabase mockDatabase = new MockDatabase();

    final Database database = new Database(mockDatabase);

    final DebuggerTemplateManager manager1 = database.getDebuggerTemplateManager();
    final DebuggerTemplateManager manager2 = database.getDebuggerTemplateManager();

    assertNotNull(manager1);
    assertEquals(manager1, manager2);
  }

  @Test
  public void testNodeTagManager() {
    final MockDatabase mockDatabase = new MockDatabase();

    final Database database = new Database(mockDatabase);

    final TagManager manager1 = database.getNodeTagManager();
    final TagManager manager2 = database.getNodeTagManager();

    assertNotNull(manager1);
    assertEquals(manager1, manager2);
  }

  @Test
  public void testSetAutoConnect() {
    final MockDatabaseListener listener = new MockDatabaseListener();

    final MockDatabase mockDatabase = new MockDatabase();

    final Database database = new Database(mockDatabase);

    database.addListener(listener);

    database.setAutoConnect(false);

    assertEquals("changedAutoConnect;", listener.events);
    assertFalse(database.isAutoConnect());
    assertFalse(mockDatabase.getConfiguration().isAutoConnect());

    mockDatabase.getConfiguration().setAutoConnect(true);

    assertEquals("changedAutoConnect;changedAutoConnect;", listener.events);
    assertTrue(database.isAutoConnect());
    assertTrue(mockDatabase.getConfiguration().isAutoConnect());

    database.removeListener(listener);
  }

  @Test
  public void testSetDescription() {
    final MockDatabaseListener listener = new MockDatabaseListener();

    final MockDatabase mockDatabase = new MockDatabase();

    final Database database = new Database(mockDatabase);

    database.addListener(listener);

    database.setDescription("Fark 1");

    assertEquals("changedDescription;", listener.events);
    assertEquals("Fark 1", database.getDescription());
    assertEquals("Fark 1", mockDatabase.getConfiguration().getDescription());

    mockDatabase.getConfiguration().setDescription("Fark 2");

    assertEquals("changedDescription;changedDescription;", listener.events);
    assertEquals("Fark 2", database.getDescription());
    assertEquals("Fark 2", mockDatabase.getConfiguration().getDescription());

    database.removeListener(listener);
  }

  @Test
  public void testSetDriver() {
    final MockDatabaseListener listener = new MockDatabaseListener();

    final MockDatabase mockDatabase = new MockDatabase();

    final Database database = new Database(mockDatabase);

    database.addListener(listener);

    database.setDriver("Fark 1");

    assertEquals("changedDriver;", listener.events);
    assertEquals("Fark 1", database.getDriver());
    assertEquals("Fark 1", mockDatabase.getConfiguration().getDriver());

    mockDatabase.getConfiguration().setDriver("Fark 2");

    assertEquals("changedDriver;changedDriver;", listener.events);
    assertEquals("Fark 2", database.getDriver());
    assertEquals("Fark 2", mockDatabase.getConfiguration().getDriver());

    database.removeListener(listener);
  }

  @Test
  public void testSetHost() {
    final MockDatabaseListener listener = new MockDatabaseListener();

    final MockDatabase mockDatabase = new MockDatabase();

    final Database database = new Database(mockDatabase);

    database.addListener(listener);

    database.setHost("Fark 1");

    assertEquals("changedHost;", listener.events);
    assertEquals("Fark 1", database.getHost());
    assertEquals("Fark 1", mockDatabase.getConfiguration().getHost());

    mockDatabase.getConfiguration().setHost("Fark 2");

    assertEquals("changedHost;changedHost;", listener.events);
    assertEquals("Fark 2", database.getHost());
    assertEquals("Fark 2", mockDatabase.getConfiguration().getHost());

    database.removeListener(listener);
  }

  @Test
  public void testSetName() {
    final MockDatabaseListener listener = new MockDatabaseListener();

    final MockDatabase mockDatabase = new MockDatabase();

    final Database database = new Database(mockDatabase);

    database.addListener(listener);

    database.setName("Fark 1");

    assertEquals("changedName;", listener.events);
    assertEquals("Fark 1", database.getName());
    assertEquals("Fark 1", mockDatabase.getConfiguration().getName());

    mockDatabase.getConfiguration().setName("Fark 2");

    assertEquals("changedName;changedName;", listener.events);
    assertEquals("Fark 2", database.getName());
    assertEquals("Fark 2", mockDatabase.getConfiguration().getName());

    database.removeListener(listener);
  }

  @Test
  public void testSetPassword() {
    final MockDatabaseListener listener = new MockDatabaseListener();

    final MockDatabase mockDatabase = new MockDatabase();

    final Database database = new Database(mockDatabase);

    database.addListener(listener);

    database.setPassword("Fark 1");

    assertEquals("changedPassword;", listener.events);
    assertEquals("Fark 1", database.getPassword());
    assertEquals("Fark 1", mockDatabase.getConfiguration().getPassword());

    mockDatabase.getConfiguration().setPassword("Fark 2");

    assertEquals("changedPassword;changedPassword;", listener.events);
    assertEquals("Fark 2", database.getPassword());
    assertEquals("Fark 2", mockDatabase.getConfiguration().getPassword());

    database.removeListener(listener);
  }

  @Test
  public void testSetSavePassword() {
    final MockDatabaseListener listener = new MockDatabaseListener();

    final MockDatabase mockDatabase = new MockDatabase();

    final Database database = new Database(mockDatabase);

    database.addListener(listener);

    database.setSavePassword(false);

    assertEquals("changedSavePassword;", listener.events);
    assertFalse(database.isSavePassword());
    assertFalse(mockDatabase.getConfiguration().isSavePassword());

    mockDatabase.getConfiguration().setSavePassword(true);

    assertEquals("changedSavePassword;changedSavePassword;", listener.events);
    assertTrue(database.isSavePassword());
    assertTrue(mockDatabase.getConfiguration().isSavePassword());

    database.removeListener(listener);
  }

  @Test
  public void testSetUser() {
    final MockDatabaseListener listener = new MockDatabaseListener();

    final MockDatabase mockDatabase = new MockDatabase();

    final Database database = new Database(mockDatabase);

    database.addListener(listener);

    database.setUser("Fark 1");

    assertEquals("changedUser;", listener.events);
    assertEquals("Fark 1", database.getUser());
    assertEquals("Fark 1", mockDatabase.getConfiguration().getUser());

    mockDatabase.getConfiguration().setUser("Fark 2");

    assertEquals("changedUser;changedUser;", listener.events);
    assertEquals("Fark 2", database.getUser());
    assertEquals("Fark 2", mockDatabase.getConfiguration().getUser());

    database.removeListener(listener);
  }

  @Test
  public void testViewTagManager() {
    final MockDatabase mockDatabase = new MockDatabase();

    final Database database = new Database(mockDatabase);

    final TagManager manager1 = database.getViewTagManager();
    final TagManager manager2 = database.getViewTagManager();

    assertNotNull(manager1);
    assertEquals(manager1, manager2);
  }
}
