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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.TypeInstancesNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.parsers.PostgreSQLTypeInstancesNotificationParser;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.MockFunction;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.MockViewGenerator;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.Section;
import com.google.security.zynamics.binnavi.disassembly.types.SectionPermission;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstance;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainer;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceReference;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.postgresql.PGNotification;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

@RunWith(JUnit4.class)
public class PostgreSQLTypeInstancesNotificationParserTest {
  // TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.module_id || ' ' || NEW.address || ' ' ||
  // NEW."position" || ' ' || NEW.expression_id || ' ' || NEW.type_instance_id);

  private final SQLProvider provider = new MockSqlProvider();
  private final MockModule module = new MockModule(provider);
  private final INaviFunction function =
      new MockFunction(provider, new CAddress("01002B87", 16), module);

  private final Collection<PGNotification> notifications = new ArrayList<PGNotification>();
  private MockView view;
  private TypeInstance instance;
  private TypeInstanceReference reference;
  private BaseType baseType;

  private void testParser(final String table, final String databaseOperation, final String moduleId,
      final String address, final String position, final String expressionId,
      final String typeInstanceId) {

    final StringBuilder builder = new StringBuilder();
    builder.append(table);
    builder.append(' ');
    builder.append(databaseOperation);
    builder.append(' ');
    builder.append(moduleId);
    builder.append(' ');
    if (address != null) {
      builder.append(address);
      builder.append(' ');
    }
    if (position != null) {
      builder.append(position);
      builder.append(' ');
    }
    if (expressionId != null) {
      builder.append(expressionId);
      builder.append(' ');
    }
    builder.append(typeInstanceId);
    final String notification = builder.toString();
    notifications.add(new MockPGNotification("type_instances_changes", notification));

    final PostgreSQLTypeInstancesNotificationParser parser =
        new PostgreSQLTypeInstancesNotificationParser();
    final Collection<TypeInstancesNotificationContainer> result =
        parser.parse(notifications, provider);

    assertNotNull(result);
    assertTrue(!result.isEmpty());
    assertTrue(result.size() == 1);

    final TypeInstancesNotificationContainer container = Iterables.getFirst(result, null);
    assertNotNull(container);
    assertEquals(databaseOperation, container.getDatabaseOperation());

  }

  @Before
  public void setUp()
      throws NumberFormatException, CouldntSaveDataException, CouldntLoadDataException {

    view = MockViewGenerator.generate(provider, module, function);
    baseType = module.getTypeManager().getTypes().get(0);
    final Section section = module.getContent().getSections().createSection("TEST_SECTION",
        new CAddress("01000000", 16), new CAddress("0100FFFF", 16),
        SectionPermission.READ_WRITE_EXECUTE, null);
    final TypeInstanceContainer container = module.getContent().getTypeInstanceContainer();
    instance =
        container.createInstance("TEST_INSTANCE", null, baseType, section, new Long("11143"));
    final CCodeNode node = view.getBasicBlocks().get(0);
    final INaviInstruction instruction = node.getInstructions().iterator().next();
    final INaviOperandTreeNode operandNode = instruction.getOperands().get(0).getNodes().get(0);

    reference = container.createReference(instruction.getAddress(), 0, operandNode, instance, view);
    assertNotNull(reference);
  }

  @Test
  public void testTypeInstanceInform1() throws CouldntLoadDataException {
    final TypeInstancesNotificationContainer container = new TypeInstancesNotificationContainer(
        "INSERT", module.getConfiguration().getId(), instance.getId(),
        Optional.<BigInteger> absent(), Optional.<Integer> absent(), Optional.<Integer> absent());

    final PostgreSQLTypeInstancesNotificationParser parser =
        new PostgreSQLTypeInstancesNotificationParser();
    parser.inform(Lists.newArrayList(container), provider);
  }

  @Test
  public void testTypeInstanceParsing1() {
    testParser(CTableNames.TYPE_INSTANCE_TABLE, "INSERT",
        String.valueOf(module.getConfiguration().getId()), null, null, null,
        String.valueOf(instance.getId()));
  }

  @Test
  public void testTypeInstanceParsing2() {
    testParser(CTableNames.TYPE_INSTANCE_TABLE, "UPDATE",
        String.valueOf(module.getConfiguration().getId()), null, null, null,
        String.valueOf(instance.getId()));
  }

  @Test
  public void testTypeInstanceParsing3() {
    testParser(CTableNames.TYPE_INSTANCE_TABLE, "DELETE",
        String.valueOf(module.getConfiguration().getId()), null, null, null,
        String.valueOf(instance.getId()));
  }

  @Test(expected = IllegalStateException.class)
  public void testTypeInstanceParsing4() {
    testParser(CTableNames.TYPE_INSTANCE_TABLE, "XXXXXXX",
        String.valueOf(module.getConfiguration().getId()), null, null, null,
        String.valueOf(instance.getId()));
  }

  @Test(expected = IllegalStateException.class)
  public void testTypeInstanceParsing5() {
    testParser(CTableNames.TYPE_INSTANCE_TABLE, "INSERT", "XXXXXXX", null, null, null,
        String.valueOf(instance.getId()));
  }

  @Test(expected = IllegalStateException.class)
  public void testTypeInstanceParsing6() {
    testParser(CTableNames.TYPE_INSTANCE_TABLE, "INSERT",
        String.valueOf(module.getConfiguration().getId()), null, null, null, "XXXXXXX");
  }

  @Test(expected = IllegalStateException.class)
  public void testTypeInstanceParsing7() {
    testParser(CTableNames.TYPE_INSTANCE_TABLE, "INSERT",
        String.valueOf(module.getConfiguration().getId()), "XXXXXX", "XXXXXX", "XXXX", "XXXXXXX");
  }
}
