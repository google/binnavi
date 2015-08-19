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

import com.google.security.zynamics.binnavi.API.disassembly.Database;
import com.google.security.zynamics.binnavi.API.disassembly.DatabaseManager;
import com.google.security.zynamics.binnavi.Database.CDatabaseManager;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.disassembly.MockDatabaseManagerListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public final class DatabaseManagerTest {
  @Test
  public void testLifecycle() {
    final MockDatabaseManagerListener listener = new MockDatabaseManagerListener();

    final DatabaseManager manager = new DatabaseManager(CDatabaseManager.instance());

    manager.addListener(listener);

    manager.addDatabase("Description", "Driver", "Host", "Name", "User", "Password", "identity",
        true, false);

    final IDatabase internalDatabase = CDatabaseManager.instance().iterator().next();

    assertEquals("Description", internalDatabase.getConfiguration().getDescription());
    assertEquals("Driver", internalDatabase.getConfiguration().getDriver());
    assertEquals("User", internalDatabase.getConfiguration().getUser());
    assertEquals("Password", internalDatabase.getConfiguration().getPassword());
    assertTrue(internalDatabase.getConfiguration().isSavePassword());
    assertFalse(internalDatabase.getConfiguration().isAutoConnect());

    assertEquals(1, manager.getDatabases().size());
    assertEquals("addedDatabase;", listener.events);

    assertEquals("Database Manager ['Description']", manager.toString());

    final Database database = manager.getDatabases().get(0);

    manager.removeDatabase(database);

    assertEquals(0, manager.getDatabases().size());
    assertEquals("addedDatabase;removedDatabase;", listener.events);

    manager.removeListener(listener);
  }
}
