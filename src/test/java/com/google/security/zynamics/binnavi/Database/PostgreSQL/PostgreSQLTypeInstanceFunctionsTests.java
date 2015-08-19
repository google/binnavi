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
package com.google.security.zynamics.binnavi.Database.PostgreSQL;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.CConfigLoader;
import com.google.security.zynamics.binnavi.Database.CDatabase;
import com.google.security.zynamics.binnavi.Database.CJdbcDriverNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntConnectException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntInitializeDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDriverException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseVersionException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidExporterDatabaseFormatException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.RawTypeInstance;
import com.google.security.zynamics.binnavi.disassembly.types.RawTypeInstanceReference;
import com.google.security.zynamics.binnavi.disassembly.types.Section;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstance;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceAddress;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainer;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceReference;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.List;

@RunWith(JUnit4.class)
public class PostgreSQLTypeInstanceFunctionsTests {
  private CDatabase database;
  private INaviModule module;
  private SQLProvider provider;

  @Test(expected = IllegalArgumentException.class)
  public void createTypeInstance1() throws CouldntSaveDataException {
    provider.createTypeInstance(0, null, null, 0, 0, 0);
  }

  @Test(expected = NullPointerException.class)
  public void createTypeInstance2() throws CouldntSaveDataException {
    provider.createTypeInstance(1, null, null, 0, 0, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createTypeInstance3() throws CouldntSaveDataException {
    provider.createTypeInstance(1, " TYPE INSTANCE NAME ", null, -1, -1, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createTypeInstance4() throws CouldntSaveDataException {
    provider.createTypeInstance(1, " TYPE INSTANCE NAME ", null, 1, -1, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createTypeInstance5() throws CouldntSaveDataException {
    provider.createTypeInstance(1, " TYPE INSTANCE NAME ", null, 1, 1, -1);
  }

  @Test
  public void createTypeInstance6()
      throws CouldntSaveDataException, CouldntLoadDataException, LoadCancelledException {

    final String typeInstanceName = " TYPE INSTANCE NAME ";

    module.load();
    final Section section = module.getContent().getSections().getSections().get(0);
    final BaseType type = module.getTypeManager().getTypes().get(0);

    final int typeInstanceId = provider.createTypeInstance(
        module.getConfiguration().getId(), typeInstanceName, null, type.getId(), section.getId(),
        0);

    module.close();
    module.load();

    final TypeInstance typeInstance =
        module.getContent().getTypeInstanceContainer().getTypeInstanceById(typeInstanceId);
    Assert.assertEquals(typeInstanceId, typeInstance.getId());
    Assert.assertEquals(typeInstanceName, typeInstance.getName());
    Assert.assertEquals(module, typeInstance.getModule());
    Assert.assertEquals(section.getId(), typeInstance.getSection().getId());
  }

  @Test(expected = IllegalArgumentException.class)
  public void createTypeInstanceReference1() throws CouldntSaveDataException {
    provider.createTypeInstanceReference(0, 0, 0, 0, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createTypeInstanceReference2() throws CouldntSaveDataException {
    provider.createTypeInstanceReference(1, 0, 0, 0, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createTypeInstanceReference3() throws CouldntSaveDataException {
    provider.createTypeInstanceReference(1, 1, 0, 0, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createTypeInstanceReference4() throws CouldntSaveDataException {
    provider.createTypeInstanceReference(1, 1, 0, 1, -1);
  }

  @Test
  public void createTypeInstanceReference5() throws CouldntSaveDataException,
  CouldntLoadDataException, LoadCancelledException, CPartialLoadException {

    module.load();
    final TypeInstance typeInstance =
        module.getContent().getTypeInstanceContainer().getTypeInstances().get(0);
    final TypeInstanceAddress address1 = typeInstance.getAddress();
    final INaviFunction function =
        module.getContent().getFunctionContainer().getFunction(new CAddress("1001929", 16));
    final INaviView view = module.getContent().getViewContainer().getView(function);
    view.load();
    final INaviInstruction instruction =
        view.getBasicBlocks().get(1).getInstructions().iterator().next();
    Assert.assertNotNull(typeInstance);

    provider.createTypeInstanceReference(typeInstance.getModule().getConfiguration().getId(),
        instruction.getAddress().toLong(),
        instruction.getOperandPosition(instruction.getOperands().get(0)),
        instruction.getOperands().get(0).getNodes().get(0).getId(), typeInstance.getId());

    view.close();
    module.close();
    module.load();
    view.load();
    final TypeInstance typeInstance2 =
        module.getContent().getTypeInstanceContainer().getTypeInstance(typeInstance.getAddress());
    Assert.assertEquals(address1, typeInstance2.getAddress());
    final List<TypeInstanceReference> references =
        module.getContent().getTypeInstanceContainer().getReferences(typeInstance2);
    Assert.assertTrue(!references.isEmpty());

    Assert.assertEquals(instruction.getAddress(), references.get(0).getAddress());
    Assert.assertEquals(instruction.getOperandPosition(instruction.getOperands().get(0)),
        references.get(0).getPosition());
    Assert.assertEquals(instruction.getOperands().get(0).getNodes().get(0).getId(),
        references.get(0).getTreeNode().get().getId());
    Assert.assertEquals(typeInstance.getId(), references.get(0).getTypeInstance().getId());
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteTypeInstanceReferenceTest1() throws CouldntDeleteException {
    provider.deleteTypeInstanceReference(-1, null, -1, -1);
  }

  @Test(expected = NullPointerException.class)
  public void deleteTypeInstanceReferenceTest2() throws CouldntDeleteException {
    provider.deleteTypeInstanceReference(module.getConfiguration().getId(), null, -1, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteTypeInstanceReferenceTest3() throws CouldntDeleteException {
    provider.deleteTypeInstanceReference(
        module.getConfiguration().getId(), new BigInteger("200", 16), -1, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteTypeInstanceReferenceTest4() throws CouldntDeleteException {
    provider.deleteTypeInstanceReference(
        module.getConfiguration().getId(), new BigInteger("200", 16), 1, -1);
  }

  @Test
  public void deleteTypeInstanceReferenceTest5() throws CouldntDeleteException,
  CouldntLoadDataException, LoadCancelledException, CPartialLoadException,
  CouldntSaveDataException {
    module.load();
    final TypeInstance typeInstance =
        module.getContent().getTypeInstanceContainer().getTypeInstances().get(0);
    final TypeInstanceAddress address1 = typeInstance.getAddress();
    final INaviFunction function =
        module.getContent().getFunctionContainer().getFunction(new CAddress("1001929", 16));
    final INaviView view = module.getContent().getViewContainer().getView(function);
    view.load();
    final INaviInstruction instruction =
        view.getBasicBlocks().get(2).getInstructions().iterator().next();
    Assert.assertNotNull(typeInstance);

    provider.createTypeInstanceReference(typeInstance.getModule().getConfiguration().getId(),
        instruction.getAddress().toLong(),
        instruction.getOperandPosition(instruction.getOperands().get(1)),
        instruction.getOperands().get(0).getNodes().get(0).getId(), typeInstance.getId());

    view.close();
    module.close();
    module.load();
    view.load();
    final TypeInstance typeInstance2 =
        module.getContent().getTypeInstanceContainer().getTypeInstance(typeInstance.getAddress());
    Assert.assertEquals(address1, typeInstance2.getAddress());
    final List<TypeInstanceReference> references =
        module.getContent().getTypeInstanceContainer().getReferences(typeInstance2);
    Assert.assertTrue(!references.isEmpty());
    final TypeInstanceReference reference =
        Iterables.find(references, new Predicate<TypeInstanceReference>() {
          @Override
          public boolean apply(final TypeInstanceReference reference) {
            return reference.getAddress().equals(instruction.getAddress());
          }
        });
    Assert.assertNotNull(reference);

    provider.deleteTypeInstanceReference(module.getConfiguration().getId(),
        reference.getAddress().toBigInteger(), reference.getPosition(),
        reference.getTreeNode().get().getId());
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteTypeInstanceTest1() throws CouldntDeleteException {
    provider.deleteTypeInstance(-1, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteTypeInstanceTest2() throws CouldntDeleteException {
    provider.deleteTypeInstance(module.getConfiguration().getId(), -1);
  }

  @Test
  public void deleteTypeInstanceTest3()
      throws CouldntDeleteException, CouldntLoadDataException, LoadCancelledException {

    module.load();
    final TypeInstance typeInstance =
        module.getContent().getTypeInstanceContainer().getTypeInstances().get(0);

    provider.deleteTypeInstance(module.getConfiguration().getId(), typeInstance.getId());
    module.close();
    module.load();
    Assert.assertFalse(
        module.getContent().getTypeInstanceContainer().getTypeInstances().contains(typeInstance));
  }

  @Test(expected = NullPointerException.class)
  public void loadSingleTypeInstanceReference1() throws CouldntLoadDataException {
    provider.loadTypeInstanceReference(null, null, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void loadSingleTypeInstanceReference2() throws CouldntLoadDataException {
    provider.loadTypeInstanceReference(module, null, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void loadSingleTypeInstanceReference3() throws CouldntLoadDataException {
    provider.loadTypeInstanceReference(module, 1, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void loadSingleTypeInstanceReference4() throws CouldntLoadDataException {
    provider.loadTypeInstanceReference(module, 1, new BigInteger("123"), null, null);
  }

  @Test(expected = NullPointerException.class)
  public void loadSingleTypeInstanceReference5() throws CouldntLoadDataException {
    provider.loadTypeInstanceReference(module, 1, new BigInteger("123"), 1, null);
  }

  @Test
  public void loadSingleTypeInstanceReference6()
      throws CouldntLoadDataException, LoadCancelledException, CPartialLoadException {
    module.load();
    for (final INaviView view : module.getContent().getViewContainer().getViews().subList(10, 20)) {
      view.load();
      final TypeInstanceContainer container = module.getContent().getTypeInstanceContainer();
      for (final TypeInstance typeInstance : container.getTypeInstances()) {
        for (final TypeInstanceReference reference : container.getReferences(typeInstance)) {
          if (reference.getTreeNode().isPresent()) {
            final RawTypeInstanceReference rawReference = provider.loadTypeInstanceReference(
                module, typeInstance.getId(), reference.getAddress().toBigInteger(),
                reference.getPosition(), reference.getTreeNode().get().getId());
            Assert.assertEquals(reference.getAddress(), rawReference.getAddress());
          }
        }
      }
      view.close();
    }
  }

  @Test(expected = NullPointerException.class)
  public void loadSingleTypeInstanceTest1() throws CouldntLoadDataException {
    provider.loadTypeInstance(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void loadSingleTypeInstanceTest2() throws CouldntLoadDataException {
    provider.loadTypeInstance(module, null);
  }

  @Test
  public void loadSingleTypeInstanceTest3()
      throws CouldntLoadDataException, LoadCancelledException {
    module.load();
    final TypeInstance typeInstance =
        module.getContent().getTypeInstanceContainer().getTypeInstances().get(0);
    final RawTypeInstance rawTypeInstance = provider.loadTypeInstance(module, typeInstance.getId());
    Assert.assertEquals(typeInstance.getId(), rawTypeInstance.getId());
    Assert.assertEquals(typeInstance.getBaseType().getId(), rawTypeInstance.getTypeId());
  }

  @Test(expected = IllegalArgumentException.class)
  public void setTypeInstanceNameTest1() throws CouldntSaveDataException {
    provider.setTypeInstanceName(-1, -1, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void setTypeInstanceNameTest2() throws CouldntSaveDataException {
    provider.setTypeInstanceName(module.getConfiguration().getId(), -1, null);
  }

  @Test(expected = NullPointerException.class)
  public void setTypeInstanceNameTest3() throws CouldntSaveDataException {
    provider.setTypeInstanceName(module.getConfiguration().getId(), 0, null);
  }

  @Test
  public void setTypeInstanceNameTest4()
      throws CouldntSaveDataException, CouldntLoadDataException, LoadCancelledException {
    module.load();
    final TypeInstance typeInstance =
        module.getContent().getTypeInstanceContainer().getTypeInstances().get(0);
    final TypeInstanceAddress address = typeInstance.getAddress();
    provider.setTypeInstanceName(
        module.getConfiguration().getId(), typeInstance.getId(), " SUPER NEW NAME ");
    module.close();
    module.load();
    final TypeInstance loadedInstance =
        module.getContent().getTypeInstanceContainer().getTypeInstance(address);
    Assert.assertEquals(" SUPER NEW NAME ", loadedInstance.getName());

  }

  @Before
  public void setUp() throws IOException, CouldntLoadDriverException, CouldntConnectException,
  IllegalStateException, CouldntLoadDataException, InvalidDatabaseException,
  CouldntInitializeDatabaseException, CouldntSaveDataException,
  InvalidExporterDatabaseFormatException, InvalidDatabaseVersionException,
  LoadCancelledException, FileReadException {
    final String[] parts = CConfigLoader.loadPostgreSQL();

    database = new CDatabase(
        "None", CJdbcDriverNames.jdbcPostgreSQLDriverName, parts[0], "test_disassembly", parts[1],
        parts[2], parts[3], false, false);

    database.connect();
    database.load();

    try {
      final Field privateProviderField = CDatabase.class.getDeclaredField("provider");
      privateProviderField.setAccessible(true);
      provider = (SQLProvider) privateProviderField.get(database);
    } catch (final Exception exception) {
      throw new RuntimeException(exception);
    }

    provider.createDebuggerTemplate("Test Debugger", "localhost", 2222);
    final CProject project = provider.createProject("Test Project");
    provider.createAddressSpace(project, "Test Address Space");

    ConfigManager.instance().read();

    module = database.getContent().getModules().get(0);
  }

  @After
  public void tearDown() {
    database.close();
  }
}
