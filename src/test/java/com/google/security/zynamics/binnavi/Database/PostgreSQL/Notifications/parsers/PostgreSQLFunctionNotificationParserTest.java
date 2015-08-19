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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.parsers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.FunctionNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.parsers.PostgreSQLFunctionNotificationParser;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.MockFunction;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.postgresql.PGNotification;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Test class for all tests related to function synchronisation between multiple instances of
 * BinNavi.
 */
@RunWith(JUnit4.class)
public class PostgreSQLFunctionNotificationParserTest {
  // TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.module_id || ' ' || NEW.address );

  private final SQLProvider provider = new MockSqlProvider();
  private final MockModule module = new MockModule(provider);
  private final IAddress address = new CAddress("12345678", 10);
  private final INaviFunction function = new MockFunction(provider, address, module);
  private final Collection<PGNotification> notifications = new ArrayList<PGNotification>();

  private void testParser(final String table, final String databaseOperation, final String address,
      final String moduleId) {

    final String notification = table + " " + databaseOperation + " " + moduleId + " " + address;
    notifications.add(new MockPGNotification("function_changes", notification));

    final PostgreSQLFunctionNotificationParser parser = new PostgreSQLFunctionNotificationParser();
    final Collection<FunctionNotificationContainer> result = parser.parse(notifications, provider);

    assertNotNull(result);
    assertTrue(!result.isEmpty());
    assertTrue(result.size() == 1);

    final FunctionNotificationContainer container = Iterables.getFirst(result, null);
    assertNotNull(container);
    assertEquals(databaseOperation, container.getDatabaseOperation());
    assertEquals(new CAddress(address, 10), container.getFunctionAddress());
  }

  @Test
  public void testFunctionParsing1() {
    testParser(CTableNames.FUNCTIONS_TABLE, "INSERT",
        String.valueOf(function.getAddress().toLong()),
        String.valueOf(module.getConfiguration().getId()));
  }

  @Test
  public void testFunctionParsing2() {
    testParser(CTableNames.FUNCTIONS_TABLE, "UPDATE",
        String.valueOf(function.getAddress().toLong()),
        String.valueOf(module.getConfiguration().getId()));
  }

  @Test
  public void testFunctionParsing3() {
    testParser(CTableNames.FUNCTIONS_TABLE, "DELETE",
        String.valueOf(function.getAddress().toLong()),
        String.valueOf(module.getConfiguration().getId()));
  }

  @Test(expected = IllegalStateException.class)
  public void testFunctionParsing4() {
    testParser(CTableNames.FUNCTIONS_TABLE, "XXXXXX",
        String.valueOf(function.getAddress().toLong()),
        String.valueOf(module.getConfiguration().getId()));
  }

  @Test(expected = IllegalStateException.class)
  public void testFunctionParsing5() {
    testParser(CTableNames.FUNCTIONS_TABLE, "INSERT", "XXXXXX",
        String.valueOf(module.getConfiguration().getId()));
  }

  @Test(expected = IllegalStateException.class)
  public void testFunctionParsing6() {
    testParser(CTableNames.FUNCTIONS_TABLE, "INSERT",
        String.valueOf(function.getAddress().toLong()), "XXXXXX");
  }

  @Test(expected = IllegalStateException.class)
  public void testFunctionParsing7() {
    testParser(CTableNames.FUNCTION_NODES_TABLE, "INSERT",
        String.valueOf(function.getAddress().toLong()), "XXXXXX");
  }

  @Test
  public void testFunctionInform0() throws CouldntLoadDataException {
    final FunctionNotificationContainer container = new FunctionNotificationContainer(
        module.getConfiguration().getId(), module, function.getAddress(), "INSERT");
    final PostgreSQLFunctionNotificationParser parser = new PostgreSQLFunctionNotificationParser();
    parser.inform(Lists.newArrayList(container), provider);
  }

  @Test
  public void testFunctionInform1Name() throws CouldntLoadDataException {
    final String name = "FUNCTION NAME CHANGE TEST";
    function.setNameInternal(name);
    assertEquals(name, function.getName());

    final FunctionNotificationContainer container = new FunctionNotificationContainer(
        module.getConfiguration().getId(), module, function.getAddress(), "UPDATE");

    final PostgreSQLFunctionNotificationParser parser = new PostgreSQLFunctionNotificationParser();
    parser.inform(Lists.newArrayList(container), provider);

    assertEquals("Mock Function", function.getName());
  }

  @Test
  public void testFunctionInform2Description() throws CouldntLoadDataException {
    final String description = "FUNCTION DESCRIPTION CHANGE TEST";
    function.setDescriptionInternal(description);
    assertEquals(description, function.getDescription());

    final FunctionNotificationContainer container = new FunctionNotificationContainer(
        module.getConfiguration().getId(), module, function.getAddress(), "UPDATE");

    final PostgreSQLFunctionNotificationParser parser = new PostgreSQLFunctionNotificationParser();
    parser.inform(Lists.newArrayList(container), provider);

    assertEquals("Mock Description", function.getDescription());
  }

  @Test
  public void testFunctionInform3Resolve() throws CouldntLoadDataException {
    final INaviFunction forwardFunction = new MockFunction(provider);
    function.setForwardedFunctionInternal(forwardFunction);
    assertTrue(function.isForwarded());
    assertEquals(forwardFunction.getAddress(), function.getForwardedFunctionAddress());
    assertEquals(forwardFunction.getModule().getConfiguration().getId(),
        function.getForwardedFunctionModuleId());

    final FunctionNotificationContainer container = new FunctionNotificationContainer(
        module.getConfiguration().getId(), module, function.getAddress(), "UPDATE");

    final PostgreSQLFunctionNotificationParser parser = new PostgreSQLFunctionNotificationParser();
    parser.inform(Lists.newArrayList(container), provider);

    assertFalse(function.isForwarded());
    assertNull(function.getForwardedFunctionAddress());
    assertEquals(0, function.getForwardedFunctionModuleId());
  }
}
