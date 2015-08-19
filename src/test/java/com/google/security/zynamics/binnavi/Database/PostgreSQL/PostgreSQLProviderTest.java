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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Creators.PostgreSQLProjectCreator;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLDataFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLFunctionFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLInstructionFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLModuleFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLNodeFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLProjectFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLSettingsFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLTagFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLTraceFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLViewFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Loaders.PostgreSQLAddressSpaceLoader;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Loaders.PostgreSQLViewLoader;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Savers.PostgreSQLViewSaver;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceEvent;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceEventType;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceRegister;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceEvent;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.MockAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.CFunction;
import com.google.security.zynamics.binnavi.disassembly.CFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.CNaviViewEdge;
import com.google.security.zynamics.binnavi.disassembly.COperandTree;
import com.google.security.zynamics.binnavi.disassembly.COperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.IBlockNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.MockFunction;
import com.google.security.zynamics.binnavi.disassembly.MockOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.MockProject;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.types.BaseTypeCategory;
import com.google.security.zynamics.binnavi.disassembly.types.ExpensiveBaseTest;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.ReilTranslator;
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;
import com.google.security.zynamics.zylib.disassembly.MockAddress;
import com.google.security.zynamics.zylib.disassembly.ReferenceType;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test class for all functions in the PostgreSQL getProvider().
 */
@RunWith(JUnit4.class)
public class PostgreSQLProviderTest extends ExpensiveBaseTest {
  @Test
  public void testAddDebugger() throws CouldntSaveDataException, CouldntLoadDataException {
    final INaviProject project = getProvider().loadProjects().get(0);
    getProvider().addDebugger(project, getProvider().loadDebuggers().getDebugger(0));
  }

  @Test(expected = NullPointerException.class)
  public void testAddDebuggerConstructor1() throws CouldntSaveDataException,
      CouldntLoadDataException {
    getProvider().addDebugger(null, getProvider().loadDebuggers().getDebugger(0));
  }

  @Test(expected = NullPointerException.class)
  public void testAddDebuggerConstructor2() throws CouldntSaveDataException,
      CouldntLoadDataException {
    final INaviProject project = getProvider().loadProjects().get(0);
    getProvider().addDebugger(project, null);
  }

  @Test
  public void testAddModule1() throws CouldntSaveDataException, CouldntLoadDataException,
      CouldntDeleteException {
    final INaviModule module = getProvider().loadModules().get(0);
    final CProject project = getProvider().createProject("FOOBAR_PROJECT");
    getProvider().createAddressSpace(project, "FOOBAR_ADDRESS_SPACE");
    final CAddressSpace as = getProvider().loadAddressSpaces(project).get(0);

    getProvider().addModule(as, module);

    try {
      getProvider().addModule(as, module);
      fail();
    } catch (final CouldntSaveDataException exception) {
      getProvider().removeModule(as, module);
    }
  }

  @Test(expected = NullPointerException.class)
  public void testAddModuleConstructor1() throws CouldntLoadDataException,
      CouldntSaveDataException {
    final INaviModule module = getProvider().loadModules().get(0);
    getProvider().addModule(null, module);
  }

  @Test(expected = NullPointerException.class)
  public void testAddModuleConstructor2() throws CouldntLoadDataException,
      CouldntSaveDataException {
    final CProject project = getProvider().createProject("TEST ADD MOUDLE CONSTRUCTOR 2");
    getProvider().createAddressSpace(project, "FOOBAR_ADDRESS_SPACE");
    getProvider().loadAddressSpaces(project).get(0);
    getProvider().addModule(getProvider().loadAddressSpaces(project).get(0), null);
  }

  @Test
  public void testAssignDebuggerAddressSpace1() throws CouldntSaveDataException,
      CouldntLoadDataException {
    final CProject project = getProvider().createProject("ADDRESS_SPACE_PROJECT_123");
    getProvider().createAddressSpace(project, "ADDRESS_SPACE_123");
    final CAddressSpace addressSpace = getProvider().loadAddressSpaces(project).get(0);

    getProvider().assignDebugger(addressSpace, getProvider().loadDebuggers().getDebugger(0));
    getProvider().assignDebugger(addressSpace, null);
  }

  @Test(expected = NullPointerException.class)
  public void testAssignDebuggerAddressSpace2() throws CouldntSaveDataException,
      CouldntLoadDataException {
    final DebuggerTemplate debuggerTemplate = getProvider().loadDebuggers().getDebugger(0);
    getProvider().assignDebugger((CAddressSpace) null, debuggerTemplate);
  }

  @Test
  public void testAssignDebuggerModule1() throws CouldntSaveDataException,
      CouldntLoadDataException {
    final INaviModule module = getProvider().loadModules().get(0);
    final DebuggerTemplate debuggerTemplate = getProvider().loadDebuggers().getDebugger(0);
    getProvider().assignDebugger(module, debuggerTemplate);
    getProvider().assignDebugger(module, null);
  }

  @Test(expected = NullPointerException.class)
  public void testAssignDebuggerModule2() throws CouldntLoadDataException,
      CouldntSaveDataException {
    final DebuggerTemplate debuggerTemplate = getProvider().loadDebuggers().getDebugger(0);
    getProvider().assignDebugger((CModule) null, debuggerTemplate);
  }

  @Test
  public void testCAddressSpaceLoaderLoadAddressSpaces1() throws CouldntLoadDataException,
      LoadCancelledException, CouldntSaveDataException {
    final DebuggerTemplateManager debuggerManager = new DebuggerTemplateManager(getProvider());
    final CProject project = getProvider().createProject("PROJECT_234");
    getProvider().createAddressSpace(project, "ADDRESS_SPACE_234");

    final List<INaviModule> modules = getProvider().loadModules();
    final List<INaviModule> convertedModules = new ArrayList<INaviModule>();
    convertedModules.add(modules.get(0));
    final List<CAddressSpace> addressSpaces = PostgreSQLAddressSpaceLoader.loadAddressSpaces(
        (AbstractSQLProvider) getProvider(), project, debuggerManager, convertedModules);

    final CAddressSpace addressSpace = addressSpaces.get(0);

    addressSpace.load();
    addressSpace.getContent().addModule(modules.get(0));
    addressSpace.getContent().setImageBase(modules.get(0), new CAddress(0x1234));

    @SuppressWarnings("unused")
    final List<CAddressSpace> addressSpaces2 = PostgreSQLAddressSpaceLoader.loadAddressSpaces(
        (AbstractSQLProvider) getProvider(), project, debuggerManager, convertedModules);

  }

  @Test(expected = NullPointerException.class)
  public void testCAddressSpaceLoaderLoadAddressSpaces2() throws CouldntLoadDataException {
    PostgreSQLAddressSpaceLoader.loadAddressSpaces(null, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCAddressSpaceLoaderLoadAddressSpaces3() throws CouldntLoadDataException {
    PostgreSQLAddressSpaceLoader.loadAddressSpaces((AbstractSQLProvider) getProvider(), null, null,
        null);
  }

  @Test(expected = NullPointerException.class)
  public void testCAddressSpaceLoaderLoadAddressSpaces4() throws CouldntLoadDataException {
    final CProject project = (CProject) getProvider().loadProjects().get(0);
    PostgreSQLAddressSpaceLoader.loadAddressSpaces((AbstractSQLProvider) getProvider(), project,
        null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCAddressSpaceLoaderLoadAddressSpaces5() throws CouldntLoadDataException {
    final CProject project = (CProject) getProvider().loadProjects().get(0);
    final DebuggerTemplateManager debuggerManager = new DebuggerTemplateManager(getProvider());
    PostgreSQLAddressSpaceLoader.loadAddressSpaces((AbstractSQLProvider) getProvider(), project,
        debuggerManager, null);
  }

  @Test
  public void testCDataFunctions1() throws CouldntLoadDataException, LoadCancelledException {
    final INaviModule module = getProvider().loadModules().get(0);
    module.load();
    PostgreSQLDataFunctions.loadData((AbstractSQLProvider) getProvider(), (CModule) module);
  }

  @Test(expected = NullPointerException.class)
  public void testCDataFunctions2() throws CouldntLoadDataException {
    PostgreSQLDataFunctions.loadData(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCDataFunctions3() throws CouldntLoadDataException {
    PostgreSQLDataFunctions.loadData((AbstractSQLProvider) getProvider(), null);
  }

  @Test
  public void testCDataFunctions4() throws CouldntLoadDataException, CouldntSaveDataException {
    final INaviModule module = getProvider().loadModules().get(0);
    final byte[] data =
        PostgreSQLDataFunctions.loadData((AbstractSQLProvider) getProvider(), (CModule) module);
    PostgreSQLDataFunctions.saveData((AbstractSQLProvider) getProvider(), (CModule) module, data);
  }

  @Test(expected = NullPointerException.class)
  public void testCDataFunctions5() throws CouldntSaveDataException {
    PostgreSQLDataFunctions.saveData(null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCDataFunctions6() throws CouldntSaveDataException {
    PostgreSQLDataFunctions.saveData((AbstractSQLProvider) getProvider(), null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCDataFunctions7() throws CouldntLoadDataException, CouldntSaveDataException {
    final INaviModule module = getProvider().loadModules().get(0);
    PostgreSQLDataFunctions.saveData((AbstractSQLProvider) getProvider(), (CModule) module, null);
  }

  @Test
  public void testCGenericSQLHelpersDeleteByID1() throws CouldntDeleteException {
    final AbstractSQLProvider connectionProvider = (AbstractSQLProvider) getProvider();
    final CConnection connection = connectionProvider.getConnection();
    final String tableName = CTableNames.ADDRESS_SPACES_TABLE;

    PostgreSQLHelpers.deleteById(connection, tableName, 1);
  }

  @Test(expected = NullPointerException.class)
  public void testCGenericSQLHelpersDeleteByID2() throws CouldntDeleteException {
    PostgreSQLHelpers.deleteById(null, null, 0);
  }

  @Test(expected = NullPointerException.class)
  public void testCGenericSQLHelpersDeleteByID3() throws CouldntDeleteException {
    final AbstractSQLProvider connectionProvider = (AbstractSQLProvider) getProvider();
    final CConnection connection = connectionProvider.getConnection();
    PostgreSQLHelpers.deleteById(connection, null, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCGenericSQLHelpersDeleteByID4() throws CouldntDeleteException {
    final AbstractSQLProvider connectionProvider = (AbstractSQLProvider) getProvider();
    final CConnection connection = connectionProvider.getConnection();
    final String tableName = CTableNames.ADDRESS_SPACES_TABLE;
    PostgreSQLHelpers.deleteById(connection, tableName, 0);
  }

  @Test
  public void testCGenericSQLHelpersRollBack1() throws SQLException {
    final AbstractSQLProvider connectionProvider = (AbstractSQLProvider) getProvider();
    final CConnection connection = connectionProvider.getConnection();
    PostgreSQLHelpers.beginTransaction(connection);
    PostgreSQLHelpers.rollback(connection);
  }

  @Test(expected = NullPointerException.class)
  public void testCGenericSQLHelpersRollBack2() throws SQLException {
    PostgreSQLHelpers.rollback(null);
  }

  @Test
  public void testCModuleFunctionsgetViewsWithAddresses1() throws CouldntLoadDataException,
      LoadCancelledException {
    final INaviModule module = getProvider().loadModules().get(0);
    module.load();

    final IFilledList<UnrelocatedAddress> addresses = new FilledList<UnrelocatedAddress>();
    List<INaviView> views = PostgreSQLModuleFunctions.getViewsWithAddresses(
        (AbstractSQLProvider) getProvider(), module, addresses, true);

    assertEquals(0, views.size());

    addresses.add(new UnrelocatedAddress(new CAddress(0x10033DCL)));

    views = PostgreSQLModuleFunctions.getViewsWithAddresses((AbstractSQLProvider) getProvider(),
        module, addresses, true);

    assertEquals(1, views.size());

    addresses.add(new UnrelocatedAddress(new CAddress(0x1003429)));

    views = PostgreSQLModuleFunctions.getViewsWithAddresses((AbstractSQLProvider) getProvider(),
        module, addresses, true);

    assertEquals(0, views.size());

    views = PostgreSQLModuleFunctions.getViewsWithAddresses((AbstractSQLProvider) getProvider(),
        module, addresses, false);

    assertEquals(2, views.size());

  }

  @Test(expected = NullPointerException.class)
  public void testCModuleFunctionsgetViewsWithAddresses2() throws CouldntLoadDataException {
    PostgreSQLModuleFunctions.getViewsWithAddresses(null, null, null, true);
  }

  @Test(expected = NullPointerException.class)
  public void testCModuleFunctionsgetViewsWithAddresses3() throws CouldntLoadDataException {
    PostgreSQLModuleFunctions.getViewsWithAddresses((AbstractSQLProvider) getProvider(), null, null,
        true);
  }

  @Test(expected = NullPointerException.class)
  public void testCModuleFunctionsgetViewsWithAddresses4() throws CouldntLoadDataException {
    final INaviModule module = getProvider().loadModules().get(0);
    PostgreSQLModuleFunctions.getViewsWithAddresses((AbstractSQLProvider) getProvider(), module,
        null, true);
  }

  @Test
  public void testCNodeFunctionsUnTagNode1() throws CouldntSaveDataException,
      CouldntLoadDataException, LoadCancelledException, CPartialLoadException {
    final INaviModule module = getProvider().loadModules().get(0);
    module.load();

    final INaviView view = module.getContent().getViewContainer().getViews().get(0);
    view.load();
    PostgreSQLNodeFunctions.untagNode(getProvider(), view.getGraph().getNodes().get(0), 1);

  }


  @Test(expected = NullPointerException.class)
  public void testCNodeFunctionsUnTagNode2() throws CouldntSaveDataException {
    PostgreSQLNodeFunctions.untagNode(null, null, 1);
  }

  @Test(expected = NullPointerException.class)
  public void testCNodeFunctionsUnTagNode3() throws CouldntSaveDataException {
    PostgreSQLNodeFunctions.untagNode(getProvider(), null, 1);
  }

  @Test
  public void testCProjectCreatorCreateProject1() throws CouldntSaveDataException,
      CouldntLoadDataException, LoadCancelledException {
    final INaviModule module = getProvider().loadModules().get(0);
    module.load();
    final CProject project = PostgreSQLProjectCreator.createProject(
        (AbstractSQLProvider) getProvider(), "new project 010");
    project.load();
    assertNotNull(project.getConfiguration().getId());

    module.close();
    project.close();

  }

  @Test(expected = NullPointerException.class)
  public void testCProjectCreatorCreateProject2() throws CouldntSaveDataException {
    PostgreSQLProjectCreator.createProject(null, null);
  }


  @Test(expected = NullPointerException.class)
  public void testCProjectCreatorCreateProject3() throws CouldntSaveDataException {
    PostgreSQLProjectCreator.createProject((AbstractSQLProvider) getProvider(), null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCProjectCreatorCreateProject4() throws CouldntSaveDataException {
    PostgreSQLProjectCreator.createProject((AbstractSQLProvider) getProvider(), "");
  }

  @Test
  public void testCProjectFunctionsgetViewsWithAddresses1() throws CouldntLoadDataException,
      LoadCancelledException {
    final INaviProject project = getProvider().loadProjects().get(0);
    project.load();

    final IFilledList<UnrelocatedAddress> addresses = new FilledList<UnrelocatedAddress>();
    List<INaviView> views = PostgreSQLProjectFunctions.getViewsWithAddresses(
        (AbstractSQLProvider) getProvider(), project, addresses, true);

    assertEquals(0, views.size());

    addresses.add(new UnrelocatedAddress(new CAddress(0x10033DCL)));

    views = PostgreSQLProjectFunctions.getViewsWithAddresses((AbstractSQLProvider) getProvider(),
        project, addresses, true);

    assertEquals(0, views.size());

    addresses.add(new UnrelocatedAddress(new CAddress(0x1003429)));

    views = PostgreSQLProjectFunctions.getViewsWithAddresses((AbstractSQLProvider) getProvider(),
        project, addresses, true);

    assertEquals(0, views.size());

    views = PostgreSQLProjectFunctions.getViewsWithAddresses((AbstractSQLProvider) getProvider(),
        project, addresses, false);

    assertEquals(0, views.size());
  }

  @Test(expected = NullPointerException.class)
  public void testCProjectFunctionsgetViewsWithAddresses2() throws CouldntLoadDataException {
    PostgreSQLProjectFunctions.getViewsWithAddresses(null, null, null, true);
  }

  @Test(expected = NullPointerException.class)
  public void testCProjectFunctionsgetViewsWithAddresses3() throws CouldntLoadDataException {
    PostgreSQLProjectFunctions.getViewsWithAddresses((AbstractSQLProvider) getProvider(), null,
        null, true);
  }

  @Test(expected = NullPointerException.class)
  public void testCProjectFunctionsgetViewsWithAddresses4() throws CouldntLoadDataException {
    final INaviProject project = getProvider().loadProjects().get(0);
    PostgreSQLProjectFunctions.getViewsWithAddresses((AbstractSQLProvider) getProvider(), project,
        null, true);
  }

  @Test
  public void testCProjectFunctionsRemoveDebugger1() throws CouldntLoadDataException,
      LoadCancelledException, CouldntSaveDataException {
    final INaviProject project = getProvider().loadProjects().get(0);
    project.load();

    final DebuggerTemplate debugger = new DebuggerTemplate(1, "foo", "host", 1234, getProvider());

    PostgreSQLProjectFunctions.removeDebugger((AbstractSQLProvider) getProvider(), project,
        debugger);
  }

  @Test(expected = NullPointerException.class)
  public void testCProjectFunctionsRemoveDebugger2() throws CouldntSaveDataException {
    PostgreSQLProjectFunctions.removeDebugger(null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCProjectFunctionsRemoveDebugger3() throws CouldntSaveDataException {
    PostgreSQLProjectFunctions.removeDebugger((AbstractSQLProvider) getProvider(), null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCProjectFunctionsRemoveDebugger4() throws CouldntLoadDataException,
      CouldntSaveDataException {
    final INaviProject project = getProvider().loadProjects().get(0);
    PostgreSQLProjectFunctions.removeDebugger((AbstractSQLProvider) getProvider(), project, null);
  }

  @Test
  public void testCProjectFunctionsSetName1() throws CouldntLoadDataException,
      CouldntSaveDataException {
    final INaviProject project = getProvider().loadProjects().get(0);
    PostgreSQLProjectFunctions.setName((AbstractSQLProvider) getProvider(), project, "foobar");
  }

  @Test(expected = NullPointerException.class)
  public void testCProjectFunctionsSetName2() throws CouldntSaveDataException {
    PostgreSQLProjectFunctions.setName(null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCProjectFunctionsSetName3() throws CouldntSaveDataException {
    PostgreSQLProjectFunctions.setName((AbstractSQLProvider) getProvider(), null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCProjectFunctionsSetName4() throws CouldntLoadDataException,
      CouldntSaveDataException {
    final INaviProject project = getProvider().loadProjects().get(0);
    PostgreSQLProjectFunctions.setName((AbstractSQLProvider) getProvider(), project, null);
  }

  @Test
  public void testCSettingsFunctionsReadSettings1() throws CouldntLoadDataException {
    final INaviModule module = getProvider().loadModules().get(0);
    PostgreSQLSettingsFunctions.readSetting((AbstractSQLProvider) getProvider(), (CModule) module,
        "key");
  }

  @Test(expected = NullPointerException.class)
  public void testCSettingsFunctionsReadSettings12() throws CouldntLoadDataException {
    PostgreSQLSettingsFunctions.readSetting(null, (CModule) null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCSettingsFunctionsReadSettings13() throws CouldntLoadDataException {
    PostgreSQLSettingsFunctions.readSetting((AbstractSQLProvider) getProvider(), (CModule) null,
        null);
  }

  @Test(expected = NullPointerException.class)
  public void testCSettingsFunctionsReadSettings14() throws CouldntLoadDataException {
    final INaviModule module = getProvider().loadModules().get(0);
    PostgreSQLSettingsFunctions.readSetting((AbstractSQLProvider) getProvider(), (CModule) module,
        null);
  }

  @Test
  public void testCSettingsFunctionsReadSettings2() throws CouldntLoadDataException {
    final INaviProject project = getProvider().loadProjects().get(0);
    PostgreSQLSettingsFunctions.readSetting((AbstractSQLProvider) getProvider(), project, "key");
  }

  @Test(expected = NullPointerException.class)
  public void testCSettingsFunctionsReadSettings21() throws CouldntLoadDataException {
    PostgreSQLSettingsFunctions.readSetting(null, (CProject) null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCSettingsFunctionsReadSettings22() throws CouldntLoadDataException {
    PostgreSQLSettingsFunctions.readSetting((AbstractSQLProvider) getProvider(), (CProject) null,
        null);
  }

  @Test(expected = NullPointerException.class)
  public void testCSettingsFunctionsReadSettings23() throws CouldntLoadDataException {
    final INaviProject project = getProvider().loadProjects().get(0);

    PostgreSQLSettingsFunctions.readSetting((AbstractSQLProvider) getProvider(), project, null);
  }

  @Test
  public void testCSettingsFunctionsWriteSettings1() throws CouldntLoadDataException,
      LoadCancelledException, CouldntSaveDataException {
    final INaviModule module = getProvider().loadModules().get(0);
    module.load();

    PostgreSQLSettingsFunctions.writeSetting((AbstractSQLProvider) getProvider(), (CModule) module,
        "key", String.valueOf(1));
  }

  @Test(expected = NullPointerException.class)
  public void testCSettingsFunctionsWriteSettings11() throws CouldntSaveDataException {
    PostgreSQLSettingsFunctions.writeSetting(null, (CModule) null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCSettingsFunctionsWriteSettings12() throws CouldntSaveDataException {
    PostgreSQLSettingsFunctions.writeSetting((AbstractSQLProvider) getProvider(), (CModule) null,
        null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCSettingsFunctionsWriteSettings13() throws CouldntLoadDataException,
      CouldntSaveDataException {
    final INaviModule module = getProvider().loadModules().get(0);
    PostgreSQLSettingsFunctions.writeSetting((AbstractSQLProvider) getProvider(), (CModule) module,
        null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCSettingsFunctionsWriteSettings14() throws CouldntLoadDataException,
      CouldntSaveDataException {
    final INaviModule module = getProvider().loadModules().get(0);
    PostgreSQLSettingsFunctions.writeSetting((AbstractSQLProvider) getProvider(), (CModule) module,
        "key", null);
  }


  @Test
  public void testCSettingsFunctionsWriteSettings2() throws CouldntLoadDataException,
      LoadCancelledException, CouldntSaveDataException {
    final INaviProject project = getProvider().loadProjects().get(0);
    project.load();
    PostgreSQLSettingsFunctions.writeSetting((AbstractSQLProvider) getProvider(), project, "key",
        String.valueOf(1));
  }

  @Test(expected = NullPointerException.class)
  public void testCSettingsFunctionsWriteSettings21() throws CouldntSaveDataException {
    PostgreSQLSettingsFunctions.writeSetting(null, (CProject) null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCSettingsFunctionsWriteSettings22() throws CouldntSaveDataException {
    PostgreSQLSettingsFunctions.writeSetting((AbstractSQLProvider) getProvider(), (CProject) null,
        null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCSettingsFunctionsWriteSettings23() throws CouldntLoadDataException,
      CouldntSaveDataException {
    final INaviProject project = getProvider().loadProjects().get(0);
    PostgreSQLSettingsFunctions.writeSetting((AbstractSQLProvider) getProvider(), project, null,
        null);
  }

  @Test(expected = NullPointerException.class)
  public void testCSettingsFunctionsWriteSettings24() throws CouldntLoadDataException,
      CouldntSaveDataException {
    final INaviProject project = getProvider().loadProjects().get(0);
    PostgreSQLSettingsFunctions.writeSetting((AbstractSQLProvider) getProvider(), project, "key",
        null);
  }

  @Test
  public void testCTagFunctionsMoveTag() throws CouldntSaveDataException, CouldntLoadDataException,
      LoadCancelledException {
    final CTagManager tagManager = getProvider().loadTagManager(TagType.VIEW_TAG);

    tagManager.addTag(tagManager.getRootTag(), "Node Tag I");
    tagManager.addTag(tagManager.getRootTag(), "Node Tag II");
    tagManager.addTag(tagManager.getRootTag().getChildren().get(0), "Node Tag III");

    final ITreeNode<CTag> tag1 = tagManager.getRootTag().getChildren().get(0);
    final ITreeNode<CTag> tag2 = tagManager.getRootTag().getChildren().get(1);

    final INaviModule module = getProvider().loadModules().get(0);

    module.load();

    tagManager.moveTag(tag1, tag2);
    tagManager.moveTag(tag2, tag1);

    // m_sql.moveTag(tag1, tag2, TagType.VIEW_TAG);
    // m_sql.moveTag(tag2, tag1, TagType.VIEW_TAG);

    // PostgreSQLTagFunctions.moveTag((AbstractSQLProvider) m_sql, tag1, tag2);
    // PostgreSQLTagFunctions.moveTag((AbstractSQLProvider) m_sql, tag2, tag1);
  }

  @Test(expected = NullPointerException.class)
  public void testCTagFunctionsMoveTag1() throws CouldntSaveDataException {
    PostgreSQLTagFunctions.moveTag(null, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCTagFunctionsMoveTag2() throws CouldntSaveDataException {
    PostgreSQLTagFunctions.moveTag((AbstractSQLProvider) getProvider(), null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCTagFunctionsMoveTag3() throws CouldntSaveDataException,
      CouldntLoadDataException {
    final CTagManager tagManager = getProvider().loadTagManager(TagType.VIEW_TAG);
    tagManager.addTag(tagManager.getRootTag(), "Node Tag I");
    final ITreeNode<CTag> tag1 = tagManager.getRootTag().getChildren().get(0);
    PostgreSQLTagFunctions.moveTag((AbstractSQLProvider) getProvider(), tag1, null, null);
  }

  @Test
  public void testCTagFunctionsSetName() throws CouldntLoadDataException, CouldntSaveDataException,
      LoadCancelledException {
    final CTagManager tagManager = getProvider().loadTagManager(TagType.VIEW_TAG);

    tagManager.addTag(tagManager.getRootTag(), "Node Tag I");
    tagManager.addTag(tagManager.getRootTag(), "Node Tag II");
    tagManager.addTag(tagManager.getRootTag().getChildren().get(0), "Node Tag III");

    final ITreeNode<CTag> tag1 = tagManager.getRootTag().getChildren().get(0);

    final INaviModule module = getProvider().loadModules().get(0);

    module.load();

    PostgreSQLTagFunctions.setName((AbstractSQLProvider) getProvider(), tag1.getObject(), "foobar");

    module.close();

    final INaviModule module2 = getProvider().loadModules().get(0);

    module2.load();

    final CTagManager tagManager1 = getProvider().loadTagManager(TagType.VIEW_TAG);

    assertEquals("foobar", tagManager1.getRootTag().getChildren().get(0).getObject().getName());

  }

  @Test
  public void testCViewFunctionsGetDerivedViews1() throws CouldntLoadDataException,
      LoadCancelledException, CPartialLoadException {
    final INaviModule module = getProvider().loadModules().get(0);

    module.load();

    final List<INaviView> views = module.getContent().getViewContainer().getViews();
    final INaviView view = views.get(264);
    view.load();

    PostgreSQLViewFunctions.getDerivedViews((AbstractSQLProvider) getProvider(), view);
  }

  @Test(expected = NullPointerException.class)
  public void testCViewFunctionsGetDerivedViews2() throws CouldntLoadDataException {
    PostgreSQLViewFunctions.getDerivedViews(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCViewFunctionsGetDerivedViews3() throws CouldntLoadDataException {
    PostgreSQLViewFunctions.getDerivedViews((AbstractSQLProvider) getProvider(), null);
  }

  @Test
  public void testCViewFunctionsLoadSettings() throws CouldntLoadDataException,
      LoadCancelledException, CPartialLoadException {
    final INaviModule module = getProvider().loadModules().get(0);

    module.load();

    final List<INaviView> views = module.getContent().getViewContainer().getViews();
    final INaviView view = views.get(264);
    view.load();

    final Map<String, String> settings =
        PostgreSQLViewFunctions.loadSettings((AbstractSQLProvider) getProvider(), (CView) view);

    assertNotNull(settings);
  }

  @Test
  public void testCViewFunctionsSaveSettings() throws CouldntLoadDataException,
      LoadCancelledException, CPartialLoadException, CouldntSaveDataException {
    final INaviModule module = getProvider().loadModules().get(0);

    module.load();

    final List<INaviView> views = module.getContent().getViewContainer().getViews();
    final INaviView view = views.get(264);
    view.load();

    final Map<String, String> settings = new HashMap<String, String>();
    settings.put("foo", "bar");

    PostgreSQLViewFunctions.saveSettings((AbstractSQLProvider) getProvider(), (CView) view,
        settings);

    view.close();
    view.load();

    final Map<String, String> settings2 =
        PostgreSQLViewFunctions.loadSettings((AbstractSQLProvider) getProvider(), (CView) view);

    assertTrue(settings2.containsKey("foo"));
  }

  @Test
  public void testCViewFunctionsSetName1() throws CouldntLoadDataException, LoadCancelledException,
      CPartialLoadException, CouldntSaveDataException {
    final INaviModule module = getProvider().loadModules().get(0);

    module.load();

    final List<INaviView> views = module.getContent().getViewContainer().getViews();
    final INaviView view = views.get(264);
    view.load();

    PostgreSQLViewFunctions.setName((AbstractSQLProvider) getProvider(), (CView) view, "furzel");

    view.close();
    module.close();
    final INaviModule module2 = getProvider().loadModules().get(0);

    module2.load();
    final List<INaviView> views2 = module2.getContent().getViewContainer().getViews();
    final INaviView view2 = views2.get(264);
    view2.load();

    assertEquals("furzel", view2.getName());
  }

  @Test(expected = NullPointerException.class)
  public void testCViewFunctionsSetName2() throws CouldntSaveDataException {
    PostgreSQLViewFunctions.setName((AbstractSQLProvider) getProvider(),
        new MockView(getProvider()), null);
  }

  @Test
  public void testCViewFunctionsStarView() throws CouldntLoadDataException, CPartialLoadException,
      LoadCancelledException, CouldntSaveDataException {
    final INaviModule module = getProvider().loadModules().get(0);

    module.load();

    final List<INaviView> views = module.getContent().getViewContainer().getViews();
    final INaviView view = views.get(264);
    view.load();

    PostgreSQLViewFunctions.starView((AbstractSQLProvider) getProvider(), (CView) view, true);

    view.close();
    module.close();

    final INaviModule module2 = getProvider().loadModules().get(0);

    module2.load();

    final List<INaviView> views2 = module2.getContent().getViewContainer().getViews();
    final INaviView view2 = views2.get(264);
    view2.load();

    assertTrue(view2.isStared());

    PostgreSQLViewFunctions.starView((AbstractSQLProvider) getProvider(), (CView) view2, false);

    view2.close();
    module2.close();

    final INaviModule module3 = getProvider().loadModules().get(0);

    module3.load();

    final List<INaviView> views3 = module3.getContent().getViewContainer().getViews();
    final INaviView view3 = views3.get(264);
    view3.load();

    assertFalse(view3.isStared());
  }

  @Test(expected = NullPointerException.class)
  public void testCViewFunctionsUntagView1() throws CouldntSaveDataException {
    getProvider().removeTag(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCViewFunctionsUntagView2() throws CouldntSaveDataException,
      CouldntLoadDataException {
    final INaviModule module = getProvider().loadModules().get(0);
    final CView view = (CView) module.getContent().getViewContainer().getViews().get(224);
    getProvider().removeTag(view, null);
  }

  @Test
  public void testCviewFunctionsUntagView3() throws CouldntLoadDataException,
      LoadCancelledException, CPartialLoadException, CouldntSaveDataException {
    final INaviModule module = getProvider().loadModules().get(0);
    module.load();
    final CView view = (CView) module.getContent().getViewContainer().getViews().get(224);
    view.load();
    final Set<CTag> viewTags = view.getConfiguration().getViewTags();
    if (viewTags.isEmpty()) {
      final CTagManager tagManager = getProvider().loadTagManager(TagType.VIEW_TAG);
      final CTag tag = tagManager.getRootTag().getChildren().get(0).getObject();
      getProvider().tagView(view, tag);
    } else {
      getProvider().removeTag(view, viewTags.iterator().next());
    }
  }


  @Test(expected = NullPointerException.class)
  public void testCViewLoaderLoadView1() throws CouldntLoadDataException, CPartialLoadException {
    PostgreSQLViewLoader.loadView(null, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCViewLoaderLoadView2() throws CouldntLoadDataException, CPartialLoadException {
    PostgreSQLViewLoader.loadView((AbstractSQLProvider) getProvider(), null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCViewLoaderLoadView3() throws CouldntLoadDataException, LoadCancelledException,
      CPartialLoadException {
    final INaviModule module = getProvider().loadModules().get(0);
    module.load();
    final CView view = (CView) module.getContent().getViewContainer().getViews().get(224);
    PostgreSQLViewLoader.loadView((AbstractSQLProvider) getProvider(), view, null, null);
  }


  @Test(expected = NullPointerException.class)
  public void testCViewLoaderLoadView4() throws CouldntLoadDataException, LoadCancelledException,
      CPartialLoadException {
    final INaviModule module = getProvider().loadModules().get(0);
    final List<INaviModule> modules = new ArrayList<INaviModule>();
    module.load();
    final CView view = (CView) module.getContent().getViewContainer().getViews().get(224);
    PostgreSQLViewLoader.loadView((AbstractSQLProvider) getProvider(), view, modules, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCViewLoaderLoadView5() throws CouldntLoadDataException, LoadCancelledException,
      CPartialLoadException {
    final INaviModule module = getProvider().loadModules().get(0);
    final List<INaviModule> modules = new ArrayList<INaviModule>();
    module.load();
    final CView view = (CView) module.getContent().getViewContainer().getViews().get(224);
    modules.add(module);
    PostgreSQLViewLoader.loadView((AbstractSQLProvider) getProvider(), view, modules, null);
  }

  @Test
  public void testCViewLoaderLoadView6() throws CouldntLoadDataException, LoadCancelledException,
      CPartialLoadException {
    final INaviModule module = getProvider().loadModules().get(0);
    module.load();
    final CView view = (CView) module.getContent().getViewContainer().getViews().get(224);
    view.load();
    final CTagManager tagManager = getProvider().loadTagManager(TagType.NODE_TAG);
    PostgreSQLViewLoader.loadView((AbstractSQLProvider) getProvider(), view,
        Lists.newArrayList(module), tagManager);
  }

  @Test
  public void testCviewSaverSave1() throws CouldntLoadDataException, LoadCancelledException,
      CouldntSaveDataException, CPartialLoadException {
    final INaviModule module = getProvider().loadModules().get(0);
    module.load();
    final INaviView view = module.getContent().getViewContainer().getViews().get(0);
    PostgreSQLViewFunctions.setName((AbstractSQLProvider) getProvider(), view, "furzel");
    view.load();
    PostgreSQLViewSaver.save((AbstractSQLProvider) getProvider(), (CView) view);
    module.close();
    final INaviModule module2 = getProvider().loadModules().get(0);
    module2.load();
    final INaviView view2 = module2.getContent().getViewContainer().getViews().get(0);
    assertEquals("furzel", view2.getName());
  }

  @Test(expected = NullPointerException.class)
  public void testCviewSaverSave2() throws CouldntSaveDataException {
    PostgreSQLViewSaver.save(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testCviewSaverSave3() throws CouldntSaveDataException {
    PostgreSQLViewSaver.save((AbstractSQLProvider) getProvider(), null);
  }

  @Test
  public void testData() throws CouldntLoadDataException, CouldntSaveDataException {
    final INaviModule module = getProvider().loadModules().get(0);

    module.setData(new byte[1024 * 1024]);
    module.saveData();

    module.setData(new byte[0]);

    module.loadData();
    final byte[] data = module.getData();

    assertEquals(1024 * 1024, data.length);
  }

  @Test(expected = NullPointerException.class)
  public void testFunctionFunctionSetNameConstructor1() throws CouldntSaveDataException {
    getProvider().setName((INaviFunction) null, null);
  }

  @Test
  public void testFunctionFunctionsResolveFunction1() throws CouldntSaveDataException,
      CouldntLoadDataException, LoadCancelledException {

    final INaviModule module = getProvider().loadModules().get(0);
    module.load();
    final INaviFunction function1 =
        module.getContent().getFunctionContainer().getFunctions().get(0);
    final INaviFunction function2 =
        module.getContent().getFunctionContainer().getFunctions().get(1);

    PostgreSQLFunctionFunctions.resolveFunction((AbstractSQLProvider) getProvider(), function1,
        function2);
    PostgreSQLFunctionFunctions.resolveFunction((AbstractSQLProvider) getProvider(), function1,
        null);
  }

  @Test(expected = NullPointerException.class)
  public void testFunctionFunctionsResolveFunction2() throws CouldntSaveDataException {
    PostgreSQLFunctionFunctions.resolveFunction(null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testFunctionFunctionsResolveFunction3() throws CouldntSaveDataException {
    PostgreSQLFunctionFunctions.resolveFunction((AbstractSQLProvider) getProvider(), null, null);
  }

  @Test
  public void testFunctionFunctionsSetDescription() throws CouldntLoadDataException,
      LoadCancelledException, CouldntSaveDataException {
    final INaviModule module1 = getProvider().loadModules().get(0);
    module1.load();
    final INaviFunction function1 =
        module1.getContent().getFunctionContainer().getFunctions().get(25);

    getProvider().setDescription(function1, "description");
    module1.close();

    final INaviModule module2 = getProvider().loadModules().get(0);
    module2.load();
    final INaviFunction function2 =
        module2.getContent().getFunctionContainer().getFunctions().get(25);
    assertEquals("description", function2.getDescription());
    module2.close();
  }

  @Test(expected = NullPointerException.class)
  public void testFunctionFunctionsSetDescriptionConstructor1() throws CouldntSaveDataException {
    getProvider().setDescription((INaviFunction) null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testFunctionFunctionsSetDescriptionConstructor2() throws CouldntSaveDataException,
      CouldntLoadDataException, LoadCancelledException {
    final INaviModule module1 = getProvider().loadModules().get(0);
    module1.load();
    final INaviFunction function1 =
        module1.getContent().getFunctionContainer().getFunctions().get(25);

    getProvider().setDescription(function1, null);
  }

  @Test
  public void testFunctionFunctionsSetName() throws CouldntSaveDataException,
      CouldntLoadDataException, LoadCancelledException {
    INaviModule module1 = getProvider().loadModules().get(0);
    module1.load();
    INaviFunction function1 = module1.getContent().getFunctionContainer().getFunctions().get(25);

    getProvider().setName(function1, "name");
    module1 = getProvider().loadModules().get(0);
    module1.load();
    function1 = module1.getContent().getFunctionContainer().getFunctions().get(25);
    assertEquals("name", function1.getName());
  }

  @Test(expected = NullPointerException.class)
  public void testFunctionFunctionsSetNameConstructor2() throws CouldntLoadDataException,
      LoadCancelledException, CouldntSaveDataException {
    final INaviModule module = getProvider().loadModules().get(0);
    module.load();
    final INaviFunction function1 =
        module.getContent().getFunctionContainer().getFunctions().get(0);

    getProvider().setName(function1, null);
  }

  @Test
  public void testGetModificationDate1() throws CouldntLoadDataException, LoadCancelledException,
      CouldntSaveDataException {
    final CProject project = getProvider().createProject("TEST_PROJECT");
    getProvider().createAddressSpace(project, "SOME_ADDRESS_SPACE_12455");
    assertNotNull(
        getProvider().getModificationDate(getProvider().loadAddressSpaces(project).get(0)));
    assertNotNull(getProvider().getModificationDate(getProvider().loadModules().get(0)));
    assertNotNull(getProvider().getModificationDate(getProvider().loadProjects().get(0)));
    final INaviModule module = getProvider().loadModules().get(0);
    module.load();
    assertNotNull(getProvider().getModificationDate(
        module.getContent().getViewContainer().getViews().get(0)));
  }

  @Test(expected = NullPointerException.class)
  public void testGetModificationDate2() throws CouldntLoadDataException {
    getProvider().getModificationDate((CAddressSpace) null);
  }

  @Test(expected = NullPointerException.class)
  public void testGetModificationDate3() throws CouldntLoadDataException {
    getProvider().getModificationDate((CModule) null);
  }

  @Test(expected = NullPointerException.class)
  public void testGetModificationDate4() throws CouldntLoadDataException {
    getProvider().getModificationDate((CProject) null);
  }

  @Test(expected = NullPointerException.class)
  public void testGetModificationDate5() throws CouldntLoadDataException {
    getProvider().getModificationDate((CView) null);
  }

  @Test
  public void testGetViewsWithAddress() throws CouldntLoadDataException, LoadCancelledException {
    final INaviModule module = getProvider().loadModules().get(0);
    module.load();

    assertNotNull(getProvider().getViewsWithAddresses(module,
        Lists.newArrayList(new UnrelocatedAddress(new CAddress(0x01002B69))), true).size());
  }


  @Test
  public void testGetViewsWithAddress_Project1() throws CouldntLoadDataException,
      LoadCancelledException {
    final INaviProject project = getProvider().loadProjects().get(0);
    project.load();
    assertEquals(0, getProvider().getViewsWithAddress(project,
        Lists.newArrayList(new UnrelocatedAddress(new CAddress(0x01002B69))), true).size());
  }


  @Test(expected = NullPointerException.class)
  public void testGetViewsWithAddress_Project2() throws CouldntLoadDataException {
    getProvider().getViewsWithAddress((CProject) null,
        Lists.newArrayList(new UnrelocatedAddress(new CAddress(0))), true);
  }


  @Test(expected = NullPointerException.class)
  public void testGetViewsWithAddress_Project3() throws CouldntLoadDataException {
    final INaviProject project = getProvider().loadProjects().get(0);
    getProvider().getViewsWithAddress(project, null, true);
  }

  @Test(expected = NullPointerException.class)
  public void testGetViewsWithAddressFail1() throws CouldntLoadDataException {
    getProvider().getViewsWithAddresses((CModule) null,
        Lists.newArrayList(new UnrelocatedAddress(new CAddress(0))), true);
  }

  @Test(expected = NullPointerException.class)
  public void testGetViewsWithAddressFail2() throws CouldntLoadDataException {
    final INaviModule module = getProvider().loadModules().get(0);
    getProvider().getViewsWithAddresses(module, null, true);
  }

  @Test
  public void testInsertTag() throws CouldntLoadDataException, CouldntSaveDataException {

    final CTagManager tagManager = getProvider().loadTagManager(TagType.VIEW_TAG);

    final CTag newTag = getProvider().insertTag(tagManager.getRootTag(), "Tag Name",
        "Tag Description", TagType.VIEW_TAG);

    assertEquals("Tag Name", newTag.getName());
    assertEquals("Tag Description", newTag.getDescription());
    assertEquals(TagType.VIEW_TAG, newTag.getType());

    getProvider().createTag(newTag, "Tag Name", "Tag Description", TagType.VIEW_TAG);

    // Create more tags for the delete test later
    getProvider().createTag(tagManager.getRootTag().getObject(), "Tag Name", "Tag Description",
        TagType.VIEW_TAG);
    final CTag tag4 = getProvider().createTag(tagManager.getRootTag().getObject(), "Tag Name",
        "Tag Description", TagType.VIEW_TAG);
    getProvider().createTag(tag4, "Tag Name", "Tag Description", TagType.VIEW_TAG);
  }

  @Test(expected = NullPointerException.class)
  public void testInsertTagFail1() throws CouldntSaveDataException {
    getProvider().insertTag(null, "Tag Name", "Tag Description", TagType.VIEW_TAG);
  }

  @Test(expected = NullPointerException.class)
  public void testinsertTagFail2() throws CouldntSaveDataException, CouldntLoadDataException {
    final CTagManager tagManager = getProvider().loadTagManager(TagType.VIEW_TAG);
    getProvider().insertTag(tagManager.getRootTag(), null, "Tag Description", TagType.VIEW_TAG);
  }

  @Test(expected = NullPointerException.class)
  public void testInsertTagFail3() throws CouldntLoadDataException, CouldntSaveDataException {
    final CTagManager tagManager = getProvider().loadTagManager(TagType.VIEW_TAG);
    getProvider().insertTag(tagManager.getRootTag(), "Tag Name", null, TagType.VIEW_TAG);
  }

  @Test(expected = NullPointerException.class)
  public void testInsertTagFail4() throws CouldntLoadDataException, CouldntSaveDataException {
    final CTagManager tagManager = getProvider().loadTagManager(TagType.VIEW_TAG);
    getProvider().insertTag(tagManager.getRootTag(), "Tag Name", "Tag Description", null);
  }

  @Test
  public void testInstructionFunctionsAddReference1() throws CouldntSaveDataException,
      CouldntLoadDataException, LoadCancelledException {

    final INaviModule module = getProvider().loadModules().get(1);
    module.load();
    final INaviFunction function =
        module.getContent().getFunctionContainer().getFunctions().get(1800);
    function.load();
    final IBlockNode basicBlock = function.getBasicBlocks().get(0);
    final INaviInstruction instruction = Iterables.get(basicBlock.getInstructions(), 1);
    final COperandTree tree = instruction.getOperands().get(0);
    final INaviOperandTreeNode node = tree.getRootNode();
    final IAddress address = instruction.getAddress();
    final ReferenceType type = ReferenceType.DATA;

    final int references = node.getReferences().size();

    PostgreSQLInstructionFunctions.addReference(getProvider(), node, address, type);

    final INaviModule module2 = getProvider().loadModules().get(1);
    module2.load();
    final INaviFunction function2 =
        module2.getContent().getFunctionContainer().getFunctions().get(1800);
    function2.load();
    final IBlockNode basicBlock2 = function2.getBasicBlocks().get(0);
    final INaviInstruction instruction2 = Iterables.get(basicBlock2.getInstructions(), 1);
    final COperandTree tree2 = instruction2.getOperands().get(0);
    final INaviOperandTreeNode node2 = tree2.getRootNode();

    assertEquals(references + 1, node2.getReferences().size());
  }

  @Test(expected = NullPointerException.class)
  public void testInstructionFunctionsAddReference2() throws CouldntSaveDataException {
    PostgreSQLInstructionFunctions.addReference(null, null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testInstructionFunctionsAddReference3() throws CouldntSaveDataException {
    PostgreSQLInstructionFunctions.addReference(getProvider(), null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testInstructionFunctionsAddReference4() throws CouldntSaveDataException,
      CouldntLoadDataException, LoadCancelledException {
    final INaviModule module = getProvider().loadModules().get(1);
    module.load();
    final INaviFunction function =
        module.getContent().getFunctionContainer().getFunctions().get(1800);
    function.load();
    final IBlockNode basicBlock = function.getBasicBlocks().get(0);
    final INaviInstruction instruction = Iterables.get(basicBlock.getInstructions(), 1);
    final COperandTree tree = instruction.getOperands().get(0);
    final INaviOperandTreeNode node = tree.getRootNode();
    PostgreSQLInstructionFunctions.addReference(getProvider(), node, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testInstructionFunctionsAddReference5() throws CouldntSaveDataException,
      CouldntLoadDataException, LoadCancelledException {
    final INaviModule module = getProvider().loadModules().get(1);
    module.load();
    final INaviFunction function =
        module.getContent().getFunctionContainer().getFunctions().get(1800);
    function.load();
    final IBlockNode basicBlock = function.getBasicBlocks().get(0);
    final INaviInstruction instruction = Iterables.get(basicBlock.getInstructions(), 1);
    final COperandTree tree = instruction.getOperands().get(0);
    final INaviOperandTreeNode node = tree.getRootNode();
    final IAddress address = instruction.getAddress();
    PostgreSQLInstructionFunctions.addReference(getProvider(), node, address, null);
  }

  @Test(expected = CouldntSaveDataException.class)
  public void testInstructionFunctionsAddReference6() throws CouldntSaveDataException,
      CouldntLoadDataException, LoadCancelledException {
    final INaviModule module = getProvider().loadModules().get(1);
    module.load();
    final INaviFunction function =
        module.getContent().getFunctionContainer().getFunctions().get(1800);
    function.load();
    final IBlockNode basicBlock = function.getBasicBlocks().get(0);
    final INaviInstruction instruction = Iterables.get(basicBlock.getInstructions(), 1);
    final COperandTree tree = instruction.getOperands().get(0);
    final INaviOperandTreeNode node = tree.getRootNode();
    final IAddress address = instruction.getAddress();
    final ReferenceType type = ReferenceType.DATA;
    PostgreSQLInstructionFunctions.addReference(getProvider(), node, address, type);
  }

  @Test
  public void testInstructionFunctionsCreateInstruction1() throws SQLException,
      CouldntLoadDataException, LoadCancelledException {

    final INaviModule module = getProvider().loadModules().get(0);
    module.load();

    final String mnemonic = "burzel";
    final IAddress iAddress = new CAddress(0x1234);
    final COperandTreeNode rootNode =
        module.createOperandExpression("eax", ExpressionType.REGISTER);
    final COperandTree tree = new COperandTree(rootNode, getProvider(), module.getTypeManager(),
        module.getContent().getTypeInstanceContainer());
    final List<COperandTree> operands = new ArrayList<COperandTree>();
    operands.add(tree);
    final byte[] data = {0xF};
    final String architecture = "x86-32";
    final INaviInstruction instruction =
        module.createInstruction(iAddress, mnemonic, operands, data, architecture);
    PostgreSQLInstructionFunctions.createInstructions(getProvider(),
        Lists.newArrayList(instruction));
  }

  @Test(expected = NullPointerException.class)
  public void testInstructionFunctionsCreateInstruction2() throws SQLException {
    PostgreSQLInstructionFunctions.createInstructions(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testInstructionFunctionsCreateInstruction3() throws SQLException {
    PostgreSQLInstructionFunctions.createInstructions(getProvider(), null);
  }

  @Test(expected = NullPointerException.class)
  public void testInstructionFunctionsDeleteReference0() throws CouldntDeleteException {
    PostgreSQLInstructionFunctions.deleteReference(getProvider(), new MockOperandTreeNode(),
        new MockAddress(), null);
  }

  @Test(expected = NullPointerException.class)
  public void testInstructionFunctionsDeleteReference1() throws CouldntDeleteException {
    PostgreSQLInstructionFunctions.deleteReference(getProvider(), new MockOperandTreeNode(), null,
        null);
  }

  @Test(expected = NullPointerException.class)
  public void testInstructionFunctionsDeleteReference2() throws CouldntDeleteException {
    PostgreSQLInstructionFunctions.deleteReference(getProvider(), null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testInstructionFunctionsDeleteReference3() throws CouldntDeleteException {
    PostgreSQLInstructionFunctions.deleteReference(null, null, null, null);
  }

  @Test
  public void testInstructionFunctionsDeleteReference4()
      throws CouldntLoadDataException,
      LoadCancelledException,
      CouldntSaveDataException,
      CouldntDeleteException,
      MaybeNullException {
    final INaviModule module = getProvider().loadModules().get(1);
    module.load();
    final INaviFunction function =
        module.getContent().getFunctionContainer().getFunction("DelayLoadFailureHook");
    function.load();
    final IBlockNode basicBlock = function.getBasicBlocks().get(0);
    final INaviInstruction instruction = Iterables.get(basicBlock.getInstructions(), 1);
    final COperandTree tree = instruction.getOperands().get(0);
    final INaviOperandTreeNode node = tree.getRootNode();
    final IAddress address = instruction.getAddress();
    final ReferenceType type = ReferenceType.DATA;

    final int references = node.getReferences().size();

    PostgreSQLInstructionFunctions.addReference(getProvider(), node, address, type);

    final INaviModule module2 = getProvider().loadModules().get(1);
    module2.load();
    final INaviFunction function2 =
        module2.getContent().getFunctionContainer().getFunction("DelayLoadFailureHook");
    function2.load();
    final IBlockNode basicBlock2 = function2.getBasicBlocks().get(0);
    final INaviInstruction instruction2 = Iterables.get(basicBlock2.getInstructions(), 1);
    final COperandTree tree2 = instruction2.getOperands().get(0);
    final INaviOperandTreeNode node2 = tree2.getRootNode();

    assertEquals(references + 1, node2.getReferences().size());

    PostgreSQLInstructionFunctions.deleteReference(getProvider(), node2, address, type);

    final INaviModule module3 = getProvider().loadModules().get(1);
    module3.load();
    final INaviFunction function3 =
        module3.getContent().getFunctionContainer().getFunction("DelayLoadFailureHook");
    function3.load();
    final IBlockNode basicBlock3 = function3.getBasicBlocks().get(0);
    final INaviInstruction instruction3 = Iterables.get(basicBlock3.getInstructions(), 1);
    final COperandTree tree3 = instruction3.getOperands().get(0);
    final INaviOperandTreeNode node3 = tree3.getRootNode();

    assertEquals(references, node3.getReferences().size());

  }

  @Test
  public void testInstructionFunctionSetGlobalReplacement1()
      throws CouldntSaveDataException,
      CouldntLoadDataException,
      LoadCancelledException,
      CPartialLoadException,
      MaybeNullException {
    final INaviModule module = getProvider().loadModules().get(0);
    module.load();
    final INaviFunction function =
        module.getContent().getFunctionContainer().getFunction("sub_1004565");
    final INaviView view = module.getContent().getViewContainer().getView(function);
    view.load();
    final IInstruction instruction =
        Iterables.get(view.getContent().getBasicBlocks().get(0).getInstructions(), 4);

    final INaviOperandTreeNode node = (INaviOperandTreeNode) instruction
        .getOperands()
        .get(1)
        .getRootNode()
        .getChildren()
        .get(0)
        .getChildren()
        .get(0)
        .getChildren()
        .get(0);

    getProvider().setGlobalReplacement(node, "replacement");

    view.close();

    final INaviModule module2 = getProvider().loadModules().get(0);
    module2.load();
    final INaviFunction function2 =
        module2.getContent().getFunctionContainer().getFunction("sub_1004565");
    final INaviView view2 = module2.getContent().getViewContainer().getView(function2);
    view2.load();
    final IInstruction instruction2 =
        Iterables.get(view2.getContent().getBasicBlocks().get(0).getInstructions(), 4);

    instruction2
        .getOperands()
        .get(1)
        .getRootNode()
        .getChildren()
        .get(0)
        .getChildren()
        .get(0)
        .getChildren()
        .get(0);
  }

  @Test(expected = NullPointerException.class)
  public void testInstructionFunctionSetGlobalReplacement2() throws CouldntSaveDataException {
    getProvider().setGlobalReplacement(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testInstructionFunctionSetGlobalReplacement3() throws CouldntSaveDataException {
    getProvider().setGlobalReplacement(new MockOperandTreeNode(), null);
  }

  @Test
  public void testInstructionFunctionsSetReplacement1()
      throws CouldntLoadDataException,
      CPartialLoadException,
      LoadCancelledException,
      MaybeNullException,
      CouldntSaveDataException {
    final INaviModule module = getProvider().loadModules().get(0);
    module.load();
    final INaviFunction function =
        module.getContent().getFunctionContainer().getFunction("sub_1004565");
    final INaviView view = module.getContent().getViewContainer().getView(function);
    view.load();
    final IInstruction instruction =
        Iterables.get(view.getContent().getBasicBlocks().get(0).getInstructions(), 7);

    final IOperandTreeNode node = instruction
        .getOperands()
        .get(1)
        .getRootNode()
        .getChildren()
        .get(0)
        .getChildren()
        .get(0)
        .getChildren()
        .get(0)
        .getChildren()
        .get(1);

    PostgreSQLInstructionFunctions.setReplacement((AbstractSQLProvider) getProvider(),
        (COperandTreeNode) node, "replacement4");

    view.close();

    final INaviModule module2 = getProvider().loadModules().get(0);
    module2.load();
    final INaviFunction function2 =
        module2.getContent().getFunctionContainer().getFunction("sub_1004565");
    final INaviView view2 = module2.getContent().getViewContainer().getView(function2);
    view2.load();
    final IInstruction instruction2 =
        Iterables.get(view2.getContent().getBasicBlocks().get(0).getInstructions(), 7);

    instruction2
        .getOperands()
        .get(1)
        .getRootNode()
        .getChildren()
        .get(0)
        .getChildren()
        .get(0)
        .getChildren()
        .get(0)
        .getChildren()
        .get(1);
  }

  @Test(expected = NullPointerException.class)
  public void testInstructionFunctionsSetReplacement2() throws CouldntSaveDataException {
    PostgreSQLInstructionFunctions.setReplacement(null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testInstructionFunctionsSetReplacement3() throws CouldntSaveDataException {
    PostgreSQLInstructionFunctions.setReplacement(getProvider(), null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testInstructionFunctionsSetReplacement4() throws CouldntSaveDataException {
    PostgreSQLInstructionFunctions.setReplacement(getProvider(), new MockOperandTreeNode(), null);
  }

  @Test
  public void testLoadView() throws CouldntLoadDataException, CPartialLoadException,
      LoadCancelledException {
    // TEST: Case 450 - Read Edge Comments fails if graph has only one node

    final INaviModule module = getProvider().loadModules().get(0);

    module.load();

    for (final INaviView view : module.getContent().getViewContainer().getViews()) {
      if (view.getNodeCount() == 1) {
        view.load();
        view.close();
      }
    }
  }

  @Test
  public void testMoveMembers() throws CouldntSaveDataException, CouldntLoadDataException,
      LoadCancelledException {
    final INaviModule module = getProvider().loadModules().get(0);
    module.load();
    final int baseTypeId = getProvider().createType(module.getConfiguration().getId(),
        "int_type",
        32,
        null,
        false,
        BaseTypeCategory.ATOMIC);
    final int containingTypeId = getProvider().createType(module.getConfiguration().getId(),
        "struct_type",
        64,
        null,
        false,
        BaseTypeCategory.STRUCT);
    final int member0 = getProvider().createTypeMember(module,
        containingTypeId,
        baseTypeId,
        "member",
        Optional.of(0),
        Optional.<Integer>absent(),
        Optional.<Integer>absent());
    final int member1 = getProvider().createTypeMember(module,
        containingTypeId,
        baseTypeId,
        "member",
        Optional.of(32),
        Optional.<Integer>absent(),
        Optional.<Integer>absent());
    getProvider().updateMemberOffsets(Lists.newArrayList(member0), 32, Lists.newArrayList(member1),
        -32, module);
  }

  @Test(expected = NullPointerException.class)
  public void testPostgreSQLTagFunctionsSetName1() throws CouldntSaveDataException {
    PostgreSQLTagFunctions.setName(null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testPostgreSQLTagFunctionsSetName2() throws CouldntSaveDataException {
    PostgreSQLTagFunctions.setName((AbstractSQLProvider) getProvider(), null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testPostgreSQLTagFunctionsSetName3() throws CouldntSaveDataException,
      CouldntLoadDataException {
    final CTagManager tagManager = getProvider().loadTagManager(TagType.VIEW_TAG);
    tagManager.addTag(tagManager.getRootTag(), "Node Tag I");
    tagManager.addTag(tagManager.getRootTag(), "Node Tag II");
    tagManager.addTag(tagManager.getRootTag().getChildren().get(0), "Node Tag III");
    final ITreeNode<CTag> tag1 = tagManager.getRootTag().getChildren().get(0);
    PostgreSQLTagFunctions.setName((AbstractSQLProvider) getProvider(), tag1.getObject(), null);
  }

  @Test
  public void testSave()
      throws CouldntSaveDataException,
      CouldntLoadDataException,
      CouldntDeleteException,
      CPartialLoadException,
      InternalTranslationException,
      LoadCancelledException,
      MaybeNullException {
    final CTagManager tagManager = getProvider().loadTagManager(TagType.NODE_TAG);

    tagManager.addTag(tagManager.getRootTag(), "Node Tag I");
    tagManager.addTag(tagManager.getRootTag(), "Node Tag II");

    final ITreeNode<CTag> tag1 = tagManager.getRootTag().getChildren().get(0);
    final ITreeNode<CTag> tag2 = tagManager.getRootTag().getChildren().get(1);

    final INaviModule module = getProvider().loadModules().get(0);

    module.load();

    final CView view =
        module.getContent().getViewContainer().createView("Save View", "Save View Description");

    final INaviFunction function =
        module.getContent().getFunctionContainer().getFunction("sub_1002B87");

    function.load();

    final List<COperandTree> operands = new ArrayList<COperandTree>();

    final COperandTreeNode root1 =
        module.createOperandExpression("dword", ExpressionType.SIZE_PREFIX);
    final COperandTreeNode child1 = module.createOperandExpression("eax", ExpressionType.REGISTER);

    COperandTreeNode.link(root1, child1);

    final COperandTreeNode root2 =
        module.createOperandExpression("dword", ExpressionType.SIZE_PREFIX);
    final COperandTreeNode child2 =
        module.createOperandExpression("16", ExpressionType.IMMEDIATE_INTEGER);

    COperandTreeNode.link(root2, child2);

    final COperandTree operand1 = module.createOperand(root1);
    final COperandTree operand2 = module.createOperand(root2);

    operands.add(operand1);
    operands.add(operand2);

    final Iterable<INaviInstruction> instructions =
        function.getBasicBlocks().get(0).getInstructions();
    final Iterable<INaviInstruction> instructions2 =
        function.getBasicBlocks().get(1).getInstructions();

    final CCodeNode codeNode =
        view.getContent().createCodeNode(function, Lists.newArrayList(instructions));

    codeNode.tagNode(tag1.getObject());

    codeNode.getComments().appendLocalCodeNodeComment("XXX");
    codeNode.getComments().appendLocalInstructionComment(
        Iterables.getLast(codeNode.getInstructions()), "YYY");

    Iterables.getLast(codeNode.getInstructions()).appendGlobalComment(
        " GLOBAL INSTRUCTION COMMENT ");

    @SuppressWarnings("unused")
    final CCodeNode codeNode2 =
        view.getContent().createCodeNode(null, Lists.newArrayList(instructions2));

    final CFunctionNode functionNode = view.getContent().createFunctionNode(function);

    functionNode.tagNode(tag2.getObject());

    functionNode.appendLocalFunctionComment("ZZZ");

    @SuppressWarnings("unused")
    final CNaviViewEdge edge =
        view.getContent().createEdge(codeNode, functionNode, EdgeType.JUMP_UNCONDITIONAL);

    view.save();
    view.close();
    view.load();

    assertEquals(3, view.getGraph().getNodes().size());
    assertEquals(1, view.getGraph().getEdges().size());
    assertTrue(view.getGraph().getNodes().get(0).isTagged(tag1.getObject()));
    assertTrue(view.getGraph().getNodes().get(2).isTagged(tag2.getObject()));

    final CCodeNode loadedCodeNode = (CCodeNode) view.getGraph().getNodes().get(0);
    final CCodeNode loadedCodeNode2 = (CCodeNode) view.getGraph().getNodes().get(1);

    assertEquals("XXX", loadedCodeNode.getComments().getLocalCodeNodeComment().get(0).getComment());

    final INaviInstruction customInstruction = Iterables.getLast(loadedCodeNode.getInstructions());

    assertEquals(" GLOBAL INSTRUCTION COMMENT ",
        customInstruction.getGlobalComment().get(0).getComment());
    assertEquals("YYY", loadedCodeNode.getComments().getLocalInstructionComment(customInstruction)
        .get(0).getComment());

    final ReilTranslator<INaviInstruction> translator = new ReilTranslator<INaviInstruction>();

    translator.translate(new StandardEnvironment(), loadedCodeNode);
    translator.translate(new StandardEnvironment(), loadedCodeNode2);

    final CFunctionNode loadedFunctionNode = (CFunctionNode) view.getGraph().getNodes().get(2);

    assertEquals("ZZZ", loadedFunctionNode.getLocalFunctionComment().get(0).getComment());

    tagManager.deleteTag(tag1);
    tagManager.deleteTag(tag2);
  }

  @Test
  public void testSetDescription1() throws CouldntSaveDataException, CouldntLoadDataException,
      LoadCancelledException {
    final INaviProject project = getProvider().loadProjects().get(0);
    final CAddressSpace addressSpace =
        getProvider().createAddressSpace(project, "SOME_OTHER_ADDRESS_SPACE");
    final INaviModule module = getProvider().loadModules().get(0);
    module.load();
    final CFunction function =
        (CFunction) module.getContent().getFunctionContainer().getFunctions().get(0);
    final CView view = (CView) module.getContent().getViewContainer().getViews().get(0);
    final CTagManager tagManager = getProvider().loadTagManager(TagType.VIEW_TAG);
    final ITreeNode<CTag> tag = tagManager.getRootTag().getChildren().get(0);
    getProvider().setDescription(addressSpace, "New Description");
    getProvider().setDescription(function, "New Description");
    getProvider().setDescription(module, "New Description");
    getProvider().setDescription(project, "New Description");
    getProvider().setDescription(tag.getObject(), "New Description");
    getProvider().setDescription(module.getContent().getTraceContainer().getTraces().get(0),
        "New Description");
    getProvider().setDescription(view, "New Description");
  }

  @Test(expected = NullPointerException.class)
  public void testSetDescription10() throws CouldntSaveDataException {
    getProvider().setDescription((CTag) null, "New Description");
  }

  @Test(expected = NullPointerException.class)
  public void testSetDescription11() throws CouldntLoadDataException, CouldntSaveDataException {
    final CTagManager tagManager = getProvider().loadTagManager(TagType.VIEW_TAG);
    final ITreeNode<CTag> tag = tagManager.getRootTag().getChildren().get(0);
    getProvider().setDescription(tag.getObject(), null);
  }

  @Test(expected = NullPointerException.class)
  public void testSetDescription12() throws CouldntSaveDataException {
    getProvider().setDescription((TraceList) null, "New Description");
  }

  @Test(expected = NullPointerException.class)
  public void testSetDescription13() throws CouldntSaveDataException, CouldntLoadDataException,
      LoadCancelledException {
    final INaviModule module = getProvider().loadModules().get(0);
    module.load();
    getProvider().setDescription(module.getContent().getTraceContainer().getTraces().get(0), null);
  }

  @Test(expected = NullPointerException.class)
  public void testSetDescription14() throws CouldntSaveDataException {
    getProvider().setDescription((CView) null, "New Description");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetDescription15() throws CouldntSaveDataException {
    getProvider().setDescription(new MockView(), null);
  }

  @Test(expected = NullPointerException.class)
  public void testSetDescription2() throws CouldntSaveDataException {
    getProvider().setDescription((CAddressSpace) null, "New Description");
  }

  @Test(expected = NullPointerException.class)
  public void testSetDescription3() throws CouldntSaveDataException, CouldntLoadDataException {
    final INaviProject project = getProvider().loadProjects().get(0);
    final CAddressSpace addressSpace =
        getProvider().createAddressSpace(project, "SOME_OTHER_ADDRESS_SPACE");
    getProvider().setDescription(addressSpace, null);
  }

  @Test(expected = NullPointerException.class)
  public void testSetDescription4() throws CouldntSaveDataException {
    getProvider().setDescription((CFunction) null, "New Description");
  }

  @Test(expected = NullPointerException.class)
  public void testSetDescription5() throws CouldntSaveDataException {
    getProvider().setDescription(new MockFunction(), null);
  }

  @Test(expected = NullPointerException.class)
  public void testSetDescription6() throws CouldntSaveDataException {
    getProvider().setDescription((CModule) null, "New Description");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetDescription7() throws CouldntSaveDataException {
    getProvider().setDescription(new MockModule(), null);
  }

  @Test(expected = NullPointerException.class)
  public void testSetDescription8() throws CouldntSaveDataException {
    getProvider().setDescription((CProject) null, "New Description");
  }

  @Test(expected = IllegalStateException.class)
  public void testSetDescription9() throws CouldntSaveDataException {
    getProvider().setDescription(new MockProject(), null);
  }

  @Test
  public void testSetFilebase() throws CouldntSaveDataException, CouldntLoadDataException {
    final INaviModule module = getProvider().loadModules().get(0);
    getProvider().setFileBase(module, new CAddress(BigInteger.valueOf(0x100)));
  }

  @Test(expected = NullPointerException.class)
  public void testSetFilebaseFail1() throws CouldntSaveDataException {
    getProvider().setFileBase(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testSetFilebaseFail2() throws CouldntSaveDataException {
    getProvider().setFileBase(null, new CAddress(BigInteger.valueOf(0x100)));
  }

  @Test(expected = NullPointerException.class)
  public void testSetFilebaseFail3() throws CouldntSaveDataException, CouldntLoadDataException {
    final INaviModule module = getProvider().loadModules().get(0);
    getProvider().setFileBase(module, null);
  }

  @Test
  public void testSetHost1() throws CouldntSaveDataException, CouldntLoadDataException {
    final DebuggerTemplate debuggerTemplate = getProvider().loadDebuggers().getDebugger(0);
    getProvider().setHost(debuggerTemplate, "localhost");
  }

  @Test(expected = NullPointerException.class)
  public void testSetHost2() throws CouldntSaveDataException {
    getProvider().setHost(null, "localhost");
  }

  @Test(expected = NullPointerException.class)
  public void testSetHost3() throws CouldntSaveDataException, CouldntLoadDataException {
    final DebuggerTemplate debuggerTemplate = getProvider().loadDebuggers().getDebugger(0);
    getProvider().setHost(debuggerTemplate, null);
  }

  @Test
  public void testSetImageBase1() throws CouldntSaveDataException, CouldntLoadDataException {
    final CProject project = getProvider().createProject("SOME_TEST_PROJECT");
    getProvider().createAddressSpace(project, "SOME_ADDRESS_SPACE");

    final CAddressSpace addressSpace = getProvider().loadAddressSpaces(project).get(0);
    final INaviModule module = getProvider().loadModules().get(0);

    getProvider().setImageBase(addressSpace, module, new CAddress(BigInteger.valueOf(0)));
  }

  @Test(expected = NullPointerException.class)
  public void testSetImageBase2() throws CouldntSaveDataException {
    getProvider().setImageBase(null, new MockModule(), new CAddress(BigInteger.valueOf(0)));
  }

  @Test(expected = IllegalStateException.class)
  public void testSetImageBase3() throws CouldntSaveDataException {
    getProvider().setImageBase(new MockAddressSpace(), null, new CAddress(BigInteger.valueOf(0)));
  }

  @Test(expected = IllegalStateException.class)
  public void testSetImageBase4() throws CouldntSaveDataException {
    getProvider().setImageBase(new MockAddressSpace(), new MockModule(), null);
  }

  @Test
  public void testSetImageBaseModule1() throws CouldntSaveDataException, CouldntLoadDataException {
    final INaviModule module = getProvider().loadModules().get(0);
    getProvider().setImageBase(module, new CAddress(BigInteger.valueOf(0)));
  }

  @Test(expected = NullPointerException.class)
  public void testSetImageBaseModule2() throws CouldntSaveDataException {
    getProvider().setImageBase(null, new CAddress(BigInteger.valueOf(0)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetImageBaseModule3() throws CouldntSaveDataException {
    getProvider().setImageBase(new MockModule(), null);
  }

  @Test
  public void testSetNameAddressSpace1() throws CouldntSaveDataException, CouldntLoadDataException {
    final INaviProject project = getProvider().loadProjects().get(0);
    getProvider().createAddressSpace(project, "SEPPEL");
    final CAddressSpace addressSpace =
        getProvider().loadAddressSpaces(getProvider().loadProjects().get(0)).get(0);
    getProvider().setName(addressSpace, "New Name");
  }

  @Test(expected = NullPointerException.class)
  public void testSetNameAddressSpace2() throws CouldntSaveDataException {
    getProvider().setName((CAddressSpace) null, "New Name");
  }

  @Test(expected = NullPointerException.class)
  public void testSetNameAddressSpace3() throws CouldntSaveDataException, CouldntLoadDataException {
    final INaviProject project = getProvider().loadProjects().get(0);
    getProvider().setName(getProvider().loadAddressSpaces(project).get(0), null);
  }

  @Test(expected = NullPointerException.class)
  public void testSetNameDebugge3() throws CouldntSaveDataException, CouldntLoadDataException {
    final DebuggerTemplate debuggerTemplate = getProvider().loadDebuggers().getDebugger(0);
    getProvider().setName(debuggerTemplate, null);
  }

  @Test
  public void testSetNameDebugger1() throws CouldntSaveDataException, CouldntLoadDataException {
    final DebuggerTemplate debuggerTemplate = getProvider().loadDebuggers().getDebugger(0);
    getProvider().setName(debuggerTemplate, "localhost");
  }

  @Test(expected = NullPointerException.class)
  public void testSetNameDebugger2() throws CouldntSaveDataException {
    getProvider().setName((DebuggerTemplate) null, "localhost");
  }

  @Test
  public void testSetPort1() throws CouldntSaveDataException, CouldntLoadDataException {
    final DebuggerTemplate debuggerTemplate = getProvider().loadDebuggers().getDebugger(0);
    getProvider().setPort(debuggerTemplate, 123);
  }

  @Test(expected = NullPointerException.class)
  public void testSetPort2() throws CouldntSaveDataException {
    getProvider().setPort(null, 123);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetPort3() throws CouldntSaveDataException, CouldntLoadDataException {
    final DebuggerTemplate debuggerTemplate = getProvider().loadDebuggers().getDebugger(0);
    getProvider().setPort(debuggerTemplate, -1);
  }

  @Test
  public void testTagFuckUp() throws CouldntLoadDataException, LoadCancelledException {
    final INaviModule module = getProvider().loadModules().get(0);
    module.load();
  }

  @Test
  public void testTagView1() throws CouldntSaveDataException, CouldntLoadDataException,
      CPartialLoadException, LoadCancelledException {
    final INaviModule module = getProvider().loadModules().get(0);
    module.load();
    final CView view = (CView) module.getContent().getViewContainer().getViews().get(224);
    view.load();
    final CTagManager tagManager = getProvider().loadTagManager(TagType.VIEW_TAG);
    final CTag tag = tagManager.getRootTag().getChildren().get(0).getObject();
    getProvider().tagView(view, tag);
  }

  @Test(expected = NullPointerException.class)
  public void testTagView2() throws CouldntLoadDataException, CouldntSaveDataException {
    final CTagManager tagManager = getProvider().loadTagManager(TagType.VIEW_TAG);
    final CTag tag = tagManager.getRootTag().getChildren().get(0).getObject();
    getProvider().tagView(null, tag);
  }

  @Test(expected = NullPointerException.class)
  public void testTagView3() throws CouldntLoadDataException, CouldntSaveDataException,
      LoadCancelledException {
    final INaviModule module = getProvider().loadModules().get(0);
    module.load();
    final CView view = (CView) module.getContent().getViewContainer().getViews().get(224);
    getProvider().tagView(view, null);
  }

  @Test
  public void testTraceFunctionsCreateTrace1() throws CouldntLoadDataException,
      CouldntSaveDataException, LoadCancelledException {
    final INaviModule module = getProvider().loadModules().get(0);
    module.load();
    final String name = "Very new trace";
    final String description = "Very new trace description";
    PostgreSQLTraceFunctions.createTrace((AbstractSQLProvider) getProvider(), module, name,
        description);

    module.close();

    final INaviModule module2 = getProvider().loadModules().get(0);
    module2.load();

    assertEquals("Very new trace description", module2
        .getContent()
        .getTraceContainer()
        .getTraces()
        .get(module2.getContent().getTraceContainer().getTraceCount() - 1)
        .getDescription());

  }

  @Test(expected = NullPointerException.class)
  public void testTraceFunctionsCreateTrace11() throws CouldntSaveDataException {
    PostgreSQLTraceFunctions.createTrace(null, (INaviModule) null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testTraceFunctionsCreateTrace12() throws CouldntSaveDataException {
    PostgreSQLTraceFunctions.createTrace((AbstractSQLProvider) getProvider(), (INaviModule) null,
        null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testTraceFunctionsCreateTrace13() throws CouldntSaveDataException {
    PostgreSQLTraceFunctions.createTrace((AbstractSQLProvider) getProvider(),
        new MockModule(new MockSqlProvider()), null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testTraceFunctionsCreateTrace14() throws CouldntSaveDataException {
    PostgreSQLTraceFunctions.createTrace((AbstractSQLProvider) getProvider(),
        new MockModule(new MockSqlProvider()), "", null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTraceFunctionsCreateTrace15() throws CouldntSaveDataException {
    PostgreSQLTraceFunctions.createTrace((AbstractSQLProvider) getProvider(),
        new MockModule(new MockSqlProvider()), "", "");
  }

  @Test
  public void testTraceFunctionsCreateTrace2() throws CouldntSaveDataException,
      CouldntLoadDataException, LoadCancelledException {
    final CProject project = getProvider().createProject("SOME_NEW_PROJECT");
    getProvider().createTrace(project, "SOME_TRACE_NAME", "SOME_TRACE_DESCRIPTION");
    project.load();

    final String name = "Very new name";
    final String description = "Super cool description";

    final int projectTraceCount = project.getContent().getTraceCount();
    PostgreSQLTraceFunctions.createTrace((AbstractSQLProvider) getProvider(), project, name,
        description);
    project.close();

    final List<INaviProject> projects = getProvider().loadProjects();
    INaviProject project2 = null;
    for (final INaviProject cProject : projects) {
      if (project.getConfiguration().getId() == cProject.getConfiguration().getId()) {
        project2 = cProject;
      }
    }

    project2.load();
    assertEquals(projectTraceCount + 1, project2.getContent().getTraceCount());
    assertEquals(description, project2.getContent().getTraces()
        .get(project2.getContent().getTraces().size() - 1).getDescription());
  }

  @Test(expected = NullPointerException.class)
  public void testTraceFunctionsCreateTrace21() throws CouldntSaveDataException {
    PostgreSQLTraceFunctions.createTrace(null, (INaviProject) null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testTraceFunctionsCreateTrace22() throws CouldntSaveDataException {
    PostgreSQLTraceFunctions.createTrace((AbstractSQLProvider) getProvider(), (INaviProject) null,
        null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testTraceFunctionsCreateTrace23() throws CouldntSaveDataException {
    PostgreSQLTraceFunctions.createTrace((AbstractSQLProvider) getProvider(),
        new MockProject(getProvider()), null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testTraceFunctionsCreateTrace24() throws CouldntSaveDataException {
    PostgreSQLTraceFunctions.createTrace((AbstractSQLProvider) getProvider(),
        new MockProject(getProvider()), "", null);
  }

  @Test
  public void testTraceFunctionsDeleteTrace1() throws CouldntLoadDataException,
      LoadCancelledException, CouldntDeleteException, CouldntSaveDataException {
    final CProject project = getProvider().createProject("SOME_NEW_PROJECT");
    getProvider().createTrace(project, "SOME_TRACE_NAME_ONE", "SOME_TRACE_DESCRIPTION_ONE");
    getProvider().createTrace(project, "SOME_TRACE_NAME_TWO", "SOME_TRACE_DESCRIPTION_TWO");
    project.load();

    final int projectTraceCount = project.getContent().getTraceCount();
    assertEquals(2, projectTraceCount);

    PostgreSQLTraceFunctions.deleteTrace((AbstractSQLProvider) getProvider(),
        project.getContent().getTraces().get(0));
    project.close();

    final List<INaviProject> projects = getProvider().loadProjects();
    INaviProject project2 = null;
    for (final INaviProject cProject : projects) {
      if (cProject.getConfiguration().getId() == project.getConfiguration().getId()) {
        project2 = cProject;
      }
    }
    project2.load();
    assertEquals(projectTraceCount - 1, project2.getContent().getTraceCount());
  }

  @Test(expected = NullPointerException.class)
  public void testTraceFunctionsDeleteTrace2() throws CouldntDeleteException {
    PostgreSQLTraceFunctions.deleteTrace(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testTraceFunctionsDeleteTrace3() throws CouldntDeleteException {
    PostgreSQLTraceFunctions.deleteTrace((AbstractSQLProvider) getProvider(), null);
  }

  @Test
  public void testTraceFunctionsSave1() throws CouldntLoadDataException, LoadCancelledException,
      CouldntSaveDataException {
    final INaviProject project = getProvider().createProject("SOME_PROJECT");
    getProvider().createTrace(project, "SOME_TRACE", "SOME_TRACE_DESCRIPTION");
    project.load();

    final TraceList trace = project.getContent().getTraces().get(0);

    final INaviModule module = getProvider().loadModules().get(0);
    module.load();

    final long tid = 0x1L;
    final UnrelocatedAddress address2 = new UnrelocatedAddress(new CAddress(0x1234));
    final BreakpointAddress address = new BreakpointAddress(module, address2);
    final TraceEventType type = TraceEventType.ECHO_BREAKPOINT;
    final List<TraceRegister> values = new ArrayList<>();
    final ITraceEvent event = new TraceEvent(tid, address, type, values);
    trace.addEvent(event);

    trace.setDescription("burzelbarf");

    PostgreSQLTraceFunctions.save((AbstractSQLProvider) getProvider(), trace);
    project.close();

    INaviProject project2 = null;

    for (final INaviProject cProject : getProvider().loadProjects()) {
      if (cProject.getConfiguration().getId() == project.getConfiguration().getId()) {
        project2 = cProject;
      }
    }

    getProvider().createTrace(project2, "SOME_TRACE_2", "SOME_TRACE_DESCRIPTION_2");
    project2.load();

    final TraceList trace2 = project2.getContent().getTraces().get(0);
    assertEquals("burzelbarf", trace2.getDescription());
  }

  @Test(expected = NullPointerException.class)
  public void testTraceFunctionsSave2() throws CouldntSaveDataException {
    PostgreSQLTraceFunctions.save(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testTraceFunctionsSave3() throws CouldntSaveDataException {
    PostgreSQLTraceFunctions.save((AbstractSQLProvider) getProvider(), null);
  }

  @Test
  public void testTraceFunctionsSetDescription1() throws CouldntLoadDataException,
      LoadCancelledException, CouldntSaveDataException {
    final INaviProject project = getProvider().createProject("SOME_PROJECT");
    getProvider().createTrace(project, "SOME_TRACE", "SOME_TRACE_DESCRIPTION");
    project.load();
    final TraceList trace = project.getContent().getTraces().get(0);

    assertEquals("SOME_TRACE_DESCRIPTION",
        project.getContent().getTraces().get(0).getDescription());

    final String description = "boing boing";
    PostgreSQLTraceFunctions.setDescription((AbstractSQLProvider) getProvider(), trace,
        description);

    project.close();
    INaviProject project2 = null;
    for (final INaviProject cProject : getProvider().loadProjects()) {
      if (cProject.getConfiguration().getId() == project.getConfiguration().getId()) {
        project2 = cProject;
      }
    }

    getProvider().createTrace(project2, "SOME_TRACE_2", "SOME_TRACE_DESCRIPTION_2");
    project2.load();
    final TraceList trace2 = project2.getContent().getTraces().get(0);
    assertEquals(description, trace2.getDescription());
  }

  @Test(expected = NullPointerException.class)
  public void testTraceFunctionsSetDescription2() throws CouldntSaveDataException {
    PostgreSQLTraceFunctions.setDescription(null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testTraceFunctionsSetDescription3() throws CouldntSaveDataException {
    PostgreSQLTraceFunctions.setDescription((AbstractSQLProvider) getProvider(), null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testTraceFunctionsSetDescription4() throws CouldntSaveDataException {
    final INaviProject project = getProvider().createProject("SOME_PROJECT");
    getProvider().createTrace(project, "SOME_TRACE", "SOME_TRACE_DESCRIPTION");
    final TraceList trace = project.getContent().getTraces().get(0);
    PostgreSQLTraceFunctions.setDescription((AbstractSQLProvider) getProvider(), trace, null);
  }

  @Test
  public void testTraceFuntionsSetName1() throws CouldntLoadDataException, LoadCancelledException,
      CouldntSaveDataException {
    final INaviProject project = getProvider().createProject("SOME_PROJECT");
    getProvider().createTrace(project, "SOME_TRACE_2344", "SOME_TRACE_DESCRIPTION_1235");
    project.load();
    final TraceList trace = project.getContent().getTraces().get(0);

    assertEquals("SOME_TRACE_DESCRIPTION_1235",
        project.getContent().getTraces().get(0).getDescription());

    final String name = "boing boing";
    PostgreSQLTraceFunctions.setName((AbstractSQLProvider) getProvider(), trace, name);

    project.close();
    INaviProject project2 = null;

    for (final INaviProject cProject : getProvider().loadProjects()) {
      if (cProject.getConfiguration().getId() == project.getConfiguration().getId()) {
        project2 = cProject;
      }
    }

    getProvider().createTrace(project2, "SOME_TRACE_2", "SOME_TRACE_DESCRIPTION_2");
    project2.load();
    final TraceList trace2 = project2.getContent().getTraces().get(0);

    assertEquals(name, trace2.getName());
  }

  @Test(expected = NullPointerException.class)
  public void testTraceFuntionsSetName2() throws CouldntSaveDataException {
    PostgreSQLTraceFunctions.setName(null, null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testTraceFuntionsSetName3() throws CouldntSaveDataException {
    PostgreSQLTraceFunctions.setName((AbstractSQLProvider) getProvider(), null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testTraceFuntionsSetName4() throws CouldntLoadDataException, LoadCancelledException,
      CouldntSaveDataException {
    final INaviProject project = getProvider().createProject("SOME_PROJECT");
    getProvider().createTrace(project, "SOME_TRACE_2344", "SOME_TRACE_DESCRIPTION_1235");
    project.load();
    final TraceList trace = project.getContent().getTraces().get(0);
    PostgreSQLTraceFunctions.setName((AbstractSQLProvider) getProvider(), trace, null);
  }
}
