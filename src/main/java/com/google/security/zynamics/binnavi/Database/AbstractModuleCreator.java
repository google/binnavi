/*
Copyright 2011-2016 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Database;

import java.sql.CallableStatement;
import java.sql.SQLException;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.ModuleConverter;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLBorderColorizer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLDataImporter;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Creators.PostgreSQLModuleCreator;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Creators.PostgreSQLNativeViewCreator;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLModuleFunctions;
import com.google.security.zynamics.binnavi.Gui.Users.CUserManager;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviRawModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleInitializeReporter;
import com.google.security.zynamics.binnavi.disassembly.Modules.ModuleInitializeEvents;


/**
 * Abstract module converter for converting raw modules into BinNavi modules.
 */
public abstract class AbstractModuleCreator implements ModuleConverter {

  /**
   * Changes the border color of native basic blocks according to their type.
   * 
   * @param moduleId The ID of the converted module.
   * 
   * @throws SQLException Thrown if the border colors could not be updated.
   */
  protected void colorizeNativeCodeNodeBorderColors(final int moduleId) throws SQLException {
    PostgreSQLBorderColorizer.setInitialBorderColors(getProvider().getConnection(), moduleId);
  }

  /**
   * Connects operand expression to their trees.
   * 
   * @param rawModuleId ID of the raw module.
   * 
   * @throws SQLException Thrown if the connection failed.
   */
  protected void connectExpressionTrees(final int moduleId, final int rawModuleId)
      throws SQLException {
    PostgreSQLDataImporter.connectExpressionTrees(getProvider().getConnection(), moduleId,
        rawModuleId);
  }

  /**
   * Connects the instructions of the converted module to their code nodes.
   * 
   * @param rawModuleId The ID of the raw module that provides the input data.
   * @param moduleId The ID of the converted module.
   * 
   * @throws SQLException Thrown if the instructions could not be connected to their code nodes.
   */
  protected void connectInstructionsToCodeNodes(final int rawModuleId, final int moduleId)
      throws SQLException {
    PostgreSQLModuleCreator.connectInstructionsToCodeNodes(getProvider().getConnection(),
        rawModuleId, moduleId);
  }

  protected abstract void connectViewsFunctions(final int moduleId, final int firstViewId)
      throws SQLException;

  /**
   * Creates the edges of the native call graph.
   * 
   * @param rawModuleId The ID of the raw module.
   * @param moduleId The ID of the converted module.
   * 
   * @throws SQLException Thrown if the call graph edges could not be created.
   */
  protected void createNativeCallgraphEdges(final int rawModuleId, final int moduleId)
      throws SQLException {
    PostgreSQLNativeViewCreator.createNativeCallgraphEdges(getProvider().getConnection(),
        rawModuleId, moduleId);
  }

  /**
   * Creates the nodes of the native call graph.
   * 
   * @param moduleId The ID of the converted module.
   * @param viewId The ID of the call graph view.
   * 
   * @throws SQLException Thrown if the call graph nodes could not be created.
   */
  protected abstract void createNativeCallgraphNodes(int moduleId, int viewId) throws SQLException;

  /**
   * Creates the call graph view of the converted module.
   * 
   * @param moduleId The ID of the converted module.
   * 
   * @return The ID of the call graph view.
   * 
   * @throws SQLException Thrown if the call graph view could not be created.
   */
  protected abstract int createNativeCallgraphView(int moduleId) throws SQLException;

  /**
   * Creates the code nodes of the native views.
   * 
   * @param rawModuleId The ID of the raw module.
   * @param moduleId The ID of the converted module.
   * 
   * @throws SQLException Thrown if the code nodes could not be created.
   */
  protected abstract void createNativeCodeNodes(int rawModuleId, int moduleId) throws SQLException;

  /**
   * Creates the edges of the native flow graph views.
   * 
   * @param rawModuleId The ID of the raw module.
   * @param moduleId The ID of the converted module.
   * 
   * @throws SQLException Thrown if the native flow graph edges could not be created.
   */
  protected abstract void createNativeFlowgraphEdges(final int rawModuleId, final int moduleId)
      throws SQLException;

  /**
   * Creates the native flow graph views.
   * 
   * @param moduleId The ID of the converted module.
   * 
   * @return The ID of the first created flow graph view.
   * 
   * @throws SQLException Thrown if the flow graph views could not be created.
   */
  protected abstract int createNativeFlowgraphViews(int moduleId) throws SQLException;

  /**
   * Creates a new module.
   * 
   * @return The ID of the created module.
   * 
   * @throws SQLException Thrown if the module could not be created.
   */
  protected abstract int createNewModule(INaviRawModule rawModule) throws SQLException;

  /**
   * Returns the provider to the database.
   * 
   * @return The provider to the database.
   */
  protected abstract SQLProvider getProvider();

  protected abstract void importAddressReferences(int id, int moduleId) throws SQLException;

  /**
   * Imports the base types into the corresponding BinNavi table.
   * 
   * @param rawModuleId The Id of the raw module to import from.
   * @param moduleId The Id of the converted module.
   * @throws SQLException Thrown if the types could not be imported.
   */
  protected void importBaseTypes(final int rawModuleId, final int moduleId) throws SQLException {
    PostgreSQLDataImporter.importBaseTypes(getProvider().getConnection(), rawModuleId, moduleId);
  }

  /**
   * Imports the expression substitutions from a raw module into a BinNavi module.
   * 
   * @param rawModuleId The ID of the raw module.
   * @param moduleId The ID of the converted module.
   * @throws SQLException Thrown if the expression substitutions could not be imported.
   */
  protected void importExpressionSubstitutions(final int rawModuleId, final int moduleId)
      throws SQLException {
    PostgreSQLDataImporter.importExpressionSubstitutions(getProvider().getConnection(),
        rawModuleId, moduleId);
  }

  /**
   * Imports the expression tree of a raw module into a BinNavi module.
   * 
   * @param moduleId The ID of the converted module.
   * 
   * @throws SQLException Thrown if the expression tree could not be imported.
   */
  protected void importExpressionTree(final int moduleId, final int rawModuleId)
      throws SQLException {
    PostgreSQLDataImporter.importExpressionTree(getProvider().getConnection(), moduleId,
        rawModuleId);
  }

  /**
   * Imports the expression trees of a raw module into a BinNavi module.
   * 
   * @param moduleId The ID of the converted module.
   * 
   * @throws SQLException Thrown if the expression tree could not be imported.
   */
  protected void importExpressionTrees(final int moduleId, final int rawModuleId)
      throws SQLException {
    PostgreSQLDataImporter.importExpressionTrees(getProvider().getConnection(), moduleId,
        rawModuleId);
  }

  protected abstract void importFunctions(final int rawModuleId, final int moduleId)
      throws SQLException;

  /**
   * Imports the instructions of a raw module into a BinNavi module.
   * 
   * @param rawModuleId The ID of the raw module.
   * @param moduleId The ID of the converted module.
   * 
   * @throws SQLException Thrown if the instructions could not be imported.
   * @throws CouldntLoadDataException
   */
  protected void importInstructions(final int rawModuleId, final int moduleId) throws SQLException,
      CouldntLoadDataException {
    PostgreSQLDataImporter.importInstructions(getProvider(), rawModuleId, moduleId);
  }

  /**
   * Imports the operands of a raw module into a converted module.
   * 
   * @param rawModuleId The ID of the raw module.
   * @param moduleId The ID of the converted module.
   * @throws SQLException Thrown if the operands could not be imported.
   */
  protected void importOperands(final int rawModuleId, final int moduleId) throws SQLException {
    PostgreSQLDataImporter.importOperands(getProvider().getConnection(), rawModuleId, moduleId);
  }

  /**
   * Imports the member types into the corresponding BinNavi table.
   * 
   * @param rawModuleId The Id of the raw module to import from.
   * @param moduleId The Id of the converted module.
   * @throws SQLException Thrown if the types could not be imported.
   */
  protected void importTypes(final int rawModuleId, final int moduleId) throws SQLException {
    PostgreSQLDataImporter.importTypes(getProvider().getConnection(), rawModuleId, moduleId);
  }

  /**
   * Imports the expression type substitutions into a BinNavi module.
   * 
   * @param rawModuleId The Id of the raw module.
   * @param moduleId The Id of the converted module.
   * @throws SQLException Thrown if the expression types could not be imported.
   */
  protected void importTypeSubstitutions(final int rawModuleId, final int moduleId)
      throws SQLException {
    PostgreSQLDataImporter.importExpressionTypes(getProvider().getConnection(), rawModuleId,
        moduleId);
  }

  /**
   * Loads a module from the database.
   * 
   * @param moduleId The ID of the module to load.
   * @param rawModule
   * @param provider The connection to the database.
   * 
   * @return The loaded module.
   * 
   * @throws CouldntLoadDataException Thrown if the module could not be loaded.
   */
  protected CModule readModule(final int moduleId, final INaviRawModule rawModule,
      final SQLProvider provider) throws CouldntLoadDataException {
    return PostgreSQLModuleFunctions.readModule(getProvider().getConnection(), moduleId, rawModule,
        provider);
  }

  @Override
  public CModule createModule(final SQLProvider provider, final INaviRawModule rawModule)
      throws CouldntLoadDataException, CouldntSaveDataException {
    NaviLogger.info("Starting to convert raw module %s", rawModule.getName());

    int moduleId = -1;

    try {
      // 1. Create the module in the database
      NaviLogger.info("Creating a new module for raw module %s", rawModule.getName());
      moduleId = createNewModule(rawModule);

    } catch (final SQLException exception) {
      throw new CouldntSaveDataException(exception);
    }

    // If the import process was successful we can load the module.
    NaviLogger.info("Loading module %d", moduleId);

    return readModule(moduleId, rawModule, provider);
  }

  @Override
  public void initializeModule(final SQLProvider sqlProvider, final INaviModule module,
      final CModuleInitializeReporter reporter) throws CouldntSaveDataException {
    final int moduleId = module.getConfiguration().getId();
    final INaviRawModule rawModule = module.getConfiguration().getRawModule();

    if (!rawModule.isComplete()) {
      throw new CouldntSaveDataException("E00008: Raw module is incomplete");
    }

    try {
      reporter.report(ModuleInitializeEvents.Starting);

      final String query = " { call import(?,?,?) } ";
      final CallableStatement call =
          getProvider().getConnection().getConnection().prepareCall(query);
      call.setInt(1, rawModule.getId());
      call.setInt(2, moduleId);
      call.setInt(3, CUserManager.get(getProvider()).getCurrentActiveUser().getUserId());
      call.execute();

      module.setInitialized();
    } catch (final SQLException exception) {
      throw new CouldntSaveDataException(exception);
    } finally {
      reporter.report(ModuleInitializeEvents.Finished);
    }
  }
}
