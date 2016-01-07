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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntInitializeDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntUpdateDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseException;
import com.google.security.zynamics.binnavi.Database.Interfaces.ModuleConverter;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProviderListener;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Creators.PostgreSQLProjectCreator;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Creators.PostgreSQLViewCreator;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLCommentFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLDataFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLDatabaseFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLEdgeFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLFunctionFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLInstructionFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLModuleFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLNodeFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLProjectFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLRawModuleFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLSectionFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLTagFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLTagManagerFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLTraceFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLTypeFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Loaders.PostgreSQLFunctionsLoader;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLModuleConverter;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Savers.PostgreSQLViewSaver;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.Users.Interfaces.IUser;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.INaviRawModule;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleInitializeReporter;
import com.google.security.zynamics.binnavi.disassembly.types.RawBaseType;
import com.google.security.zynamics.binnavi.disassembly.types.RawTypeInstance;
import com.google.security.zynamics.binnavi.disassembly.types.RawTypeInstanceReference;
import com.google.security.zynamics.binnavi.disassembly.types.RawTypeMember;
import com.google.security.zynamics.binnavi.disassembly.types.RawTypeSubstitution;
import com.google.security.zynamics.binnavi.disassembly.types.Section;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * This class provides concrete implementations of all necessary queries to use BinNavi with
 * PostgresqlSQL databases.
 */

public class PostgreSQLProvider extends AbstractSQLProvider {
  /**
   * Used to convert raw modules into BinNavi modules.
   */
  protected final PostgreSQLModuleConverter moduleConverter;

  public PostgreSQLProvider(final CConnection connection) {
    super(connection);

    Preconditions.checkNotNull(connection, "IE01210: Database connection can't be null");

    moduleConverter = new PostgreSQLModuleConverter(this);
  }

  /**
   * {@link ListenerProvider Listener provider} to keep track of {@link SQLProviderListener
   * listeners} which want to get informed about changes in the provider.
   */
  private final ListenerProvider<SQLProviderListener> listeners =
      new ListenerProvider<SQLProviderListener>();

  /**
   * Determines whether a column with a given name exists in a given table.
   *
   * @param tableName Name of the table.
   * @param columnname Name of the column.
   *
   * @return True, if the column exists. False, otherwise.
   *
   * @throws CouldntLoadDataException Thrown if the existence of the column could not be determined.
   */
  private boolean hasColumn(final String tableName, final String columnname)
      throws CouldntLoadDataException {
    return PostgreSQLHelpers.hasColumn(getConnection(), tableName, columnname);
  }

  @Override
  protected ModuleConverter getModuleConverter() {
    return moduleConverter;
  }

  @Override
  protected String getTablesFile() {
    return "postgresql_tables.sql";
  }

  protected int hasAllTables(final List<String> tableNames) throws CouldntLoadDataException {
    return PostgreSQLHelpers.getTableCount(getConnection(), tableNames);
  }

  @Override
  protected boolean hasTable(final String tableName) throws CouldntLoadDataException {
    return PostgreSQLHelpers.hasTable(getConnection(), tableName);
  }

  @Override
  public IUser addUser(final String userName) throws CouldntSaveDataException {
    return CGenericSQLUserFunctions.addUser(this, userName);
  }

  @Override
  public Integer appendFunctionComment(
      final INaviFunction function, final String commentText, final Integer userId)
      throws CouldntSaveDataException {
    return PostgreSQLFunctionFunctions.appendGlobalFunctionComment(
        this, function, commentText, userId);
  }

  @Override
  public Integer appendGroupNodeComment(
      final INaviGroupNode groupNode, final String commentText, final Integer userId)
      throws CouldntSaveDataException {
    return PostgreSQLNodeFunctions.appendGroupNodeComment(this, groupNode, commentText, userId);
  }

  @Override
  public Integer appendTextNodeComment(
      final INaviTextNode textNode, final String commentText, final Integer userId)
      throws CouldntSaveDataException {
    return PostgreSQLNodeFunctions.appendTextNodeComment(this, textNode, commentText, userId);
  }

  @Override
  public Integer appendTypeInstanceComment(
      final int moduleId, final int instanceId, final String commentText, final Integer userId)
      throws CouldntSaveDataException {
    return PostgreSQLTypeFunctions.appendTypeInstanceComment(
        this, moduleId, instanceId, commentText, userId);
  }

  @Override
  public Integer appendSectionComment(
      final int moduleId, final int sectionId, final String commentText, final Integer userId)
      throws CouldntSaveDataException {
    return PostgreSQLSectionFunctions.appendSectionComment(
        this, moduleId, sectionId, commentText, userId);
  }

  @Override
  public CAddressSpace createAddressSpace(final INaviProject project, final String name)
      throws CouldntSaveDataException {
    return PostgreSQLProjectFunctions.createAddressSpace(this, project, name);
  }

  @Override
  public CModule createModule(final INaviRawModule rawModule)
      throws CouldntLoadDataException, CouldntSaveDataException {
    final CModule newModule = getModuleConverter().createModule(this, rawModule);
    getModules().add(newModule);
    return newModule;
  }

  /**
   * Creates the raw modules table.
   *
   * @throws SQLException Thrown if the raw modules table could not be created.
   */
  @Override
  public void createModulesTable() throws SQLException {
    executeUpdate("CREATE TABLE modules (" + " id serial, "
        + " name text NOT NULL, "
        + " architecture varchar( 32 ) NOT NULL, "
        + " base_address bigint NOT NULL, "
        + " exporter varchar( 256 ) NOT NULL, "
        + " version int NOT NULL, "
        + " md5 char( 32 ) NOT NULL, "
        + " sha1 char( 40 ) NOT NULL, "
        + " comment TEXT, "
        + " import_time timestamp NOT NULL DEFAULT current_timestamp, "
        + " PRIMARY KEY (id));"
    );
  }

  @Override
  public CProject createProject(final String name) throws CouldntSaveDataException {
    final CProject project = PostgreSQLProjectCreator.createProject(this, name);
    projects.add(project);
    return project;
  }

  @Override
  public CTag createTag(
      final CTag parent, final String name, final String description, final TagType type)
      throws CouldntSaveDataException {
    return PostgreSQLTagFunctions.createTag(this, parent, name, description, type);
  }

  @Override
  public TraceList createTrace(
      final INaviModule module, final String name, final String description)
      throws CouldntSaveDataException {
    return PostgreSQLTraceFunctions.createTrace(this, module, name, description);
  }

  @Override
  public TraceList createTrace(
      final INaviProject project, final String name, final String description)
      throws CouldntSaveDataException {
    return PostgreSQLTraceFunctions.createTrace(this, project, name, description);
  }

  @Override
  public CView createView(
      final INaviModule module, final INaviView view, final String name, final String description)
      throws CouldntSaveDataException {
    return PostgreSQLViewCreator.createView(this, module, view, name, description);
  }

  @Override
  public CView createView(
      final INaviProject project, final INaviView view, final String name, final String description)
      throws CouldntSaveDataException {
    return PostgreSQLViewCreator.createView(this, project, view, name, description);
  }

  @Override
  public void deleteFunctionComment(
      final INaviFunction function, final Integer commentId, final Integer userId)
      throws CouldntDeleteException {
    PostgreSQLFunctionFunctions.deleteGlobalFunctionComment(this, function, commentId, userId);
  }

  @Override
  public void deleteFunctionNodeComment(
      final INaviFunctionNode functionNode, final Integer commentId, final Integer userId)
      throws CouldntDeleteException {
    PostgreSQLNodeFunctions.deleteLocalFunctionNodeComment(this, functionNode, commentId, userId);
  }

  @Override
  public void deleteGlobalCodeNodeComment(
      final INaviCodeNode codeNode, final Integer commentId, final Integer userId)
      throws CouldntDeleteException {
    PostgreSQLNodeFunctions.deleteGlobalCodeNodeComment(this, codeNode, commentId, userId);
  }

  @Override
  public void deleteGlobalEdgeComment(
      final INaviEdge edge, final Integer commentId, final Integer userId)
      throws CouldntDeleteException {
    PostgreSQLEdgeFunctions.deleteGlobalEdgeComment(this, edge, commentId, userId);
  }

  @Override
  public void deleteGlobalInstructionComment(
      final INaviInstruction instruction, final Integer commentId, final Integer userId)
      throws CouldntDeleteException {
    PostgreSQLInstructionFunctions.deleteGlobalInstructionComment(
        this, instruction, commentId, userId);
  }

  @Override
  public void deleteGroupNodeComment(
      final INaviGroupNode groupNode, final Integer commentId, final Integer userId)
      throws CouldntDeleteException {
    PostgreSQLNodeFunctions.deleteGroupNodeComment(this, groupNode, commentId, userId);
  }

  @Override
  public void deleteLocalCodeNodeComment(
      final INaviCodeNode codeNode, final Integer commentId, final Integer userId)
      throws CouldntDeleteException {
    PostgreSQLNodeFunctions.deleteLocalCodeNodeComment(this, codeNode, commentId, userId);
  }

  @Override
  public void deleteLocalEdgeComment(
      final INaviEdge edge, final Integer commentId, final Integer userId)
      throws CouldntDeleteException {
    PostgreSQLEdgeFunctions.deleteLocalEdgeComment(this, edge, commentId, userId);
  }

  @Override
  public void deleteLocalInstructionComment(final INaviCodeNode codeNode,
      final INaviInstruction instruction, final Integer commentId, final Integer userId)
      throws CouldntDeleteException {
    PostgreSQLInstructionFunctions.deleteLocalInstructionComment(
        this, codeNode, instruction, commentId, userId);
  }

  @Override
  public void deleteTypeInstanceComment(
      final int moduleId, final int instanceId, final Integer commentId, final Integer userId)
      throws CouldntDeleteException {
    PostgreSQLTypeFunctions.deleteTypeInstanceComment(
        this, moduleId, instanceId, commentId, userId);
  }

  @Override
  public void deleteSectionComment(
      final int moduleId, final int sectionId, final Integer commentId, final Integer userId)
      throws CouldntDeleteException {
    PostgreSQLSectionFunctions.deleteSectionComment(this, moduleId, sectionId, commentId, userId);
  }

  @Override
  public void deleteModule(final INaviModule module) throws CouldntDeleteException {
    PostgreSQLModuleFunctions.deleteModule(this, module);
    PostgreSQLRawModuleFunctions.deleteRawModule(this, module.getConfiguration().getRawModule());
  }

  @Override
  public void deleteTextNodeComment(
      final INaviTextNode textNode, final Integer commentId, final Integer userId)
      throws CouldntDeleteException {
    PostgreSQLNodeFunctions.deleteTextNodeComment(this, textNode, commentId, userId);
  }

  @Override
  public void deleteUser(final IUser user) throws CouldntDeleteException {
    CGenericSQLUserFunctions.deleteUser(this, user);
  }

  @Override
  public void editTypeInstanceComment(
      final int moduleId, final Integer commentId, final Integer userId, final String commentText)
      throws CouldntSaveDataException {
    PostgreSQLTypeFunctions.editTypeInstanceComment(this, moduleId, commentId, userId, commentText);
  }

  @Override
  public void editSectionComment(final int moduleId, final int sectionId, final Integer commentId,
      final Integer userId, final String commentText) throws CouldntSaveDataException {
    PostgreSQLSectionFunctions.editSectionComment(this, moduleId, commentId, userId, commentText);
  }

  @Override
  public void editFunctionComment(final INaviFunction function, final Integer commentId,
      final Integer userId, final String newCommentText) throws CouldntSaveDataException {
    PostgreSQLFunctionFunctions.editGlobalFunctionComment(
        this, function, commentId, userId, newCommentText);
  }

  @Override
  public void editFunctionNodeComment(final INaviFunctionNode functionNode, final Integer commentId,
      final Integer userId, final String newCommentText) throws CouldntSaveDataException {
    PostgreSQLNodeFunctions.editLocalFunctionNodeComment(
        this, functionNode, commentId, userId, newCommentText);
  }

  @Override
  public void editGlobalCodeNodeComment(final INaviCodeNode codeNode, final Integer commentId,
      final Integer userId, final String newCommentText) throws CouldntSaveDataException {
    PostgreSQLNodeFunctions.editGlobalCodeNodeComment(
        this, codeNode, commentId, userId, newCommentText);
  }

  @Override
  public void editGlobalEdgeComment(final INaviEdge edge, final Integer commentId,
      final Integer userId, final String newCommentText) throws CouldntSaveDataException {
    PostgreSQLEdgeFunctions.editGlobalEdgeComment(this, edge, commentId, userId, newCommentText);
  }

  @Override
  public void editGlobalInstructionComment(final INaviInstruction instruction,
      final Integer commentId, final Integer userId, final String newCommentText)
      throws CouldntSaveDataException {
    PostgreSQLInstructionFunctions.editGlobalInstructionComment(
        this, commentId, userId, newCommentText);
  }

  @Override
  public void editGroupNodeComment(final INaviGroupNode groupNode, final Integer commentId,
      final Integer userId, final String newComment) throws CouldntSaveDataException {
    PostgreSQLNodeFunctions.editGroupNodeComment(this, groupNode, commentId, userId, newComment);
  }

  @Override
  public void editLocalCodeNodeComment(final INaviCodeNode codeNode, final Integer commentId,
      final Integer userId, final String newCommentText) throws CouldntSaveDataException {
    PostgreSQLNodeFunctions.editLocalCodeNodeComment(
        this, codeNode, commentId, userId, newCommentText);
  }

  @Override
  public void editLocalEdgeComment(final INaviEdge edge, final Integer commentId,
      final Integer userId, final String newCommentText) throws CouldntSaveDataException {
    PostgreSQLEdgeFunctions.editLocalEdgeComment(this, edge, commentId, userId, newCommentText);
  }

  @Override
  public void editLocalInstructionComment(final INaviCodeNode codeNode,
      final INaviInstruction instruction, final Integer commentId, final Integer userId,
      final String newCommentText) throws CouldntSaveDataException {
    PostgreSQLInstructionFunctions.editLocalInstructionComment(
        this, commentId, userId, newCommentText);
  }

  @Override
  public void editTextNodeComment(final INaviTextNode textNode, final Integer commentId,
      final Integer userId, final String newComment) throws CouldntSaveDataException {
    PostgreSQLNodeFunctions.editTextNodeComment(this, textNode, commentId, userId, newComment);
  }

  @Override
  public IUser editUserName(final IUser user, final String userName)
      throws CouldntSaveDataException {
    return CGenericSQLUserFunctions.editUserName(this, user, userName);
  }

  @Override
  public DatabaseVersion getDatabaseVersion() throws CouldntLoadDataException {
    try {
      return PostgreSQLDatabaseFunctions.getDatabaseVersion(connection);
    } catch (final SQLException exception) {
      throw new CouldntLoadDataException(exception);
    }
  }

  @Override
  public void initializeDatabase()
      throws CouldntInitializeDatabaseException, CouldntLoadDataException {
    try {
      createEmptyTables();
    } catch (final IOException exception) {
      throw new CouldntInitializeDatabaseException(exception);
    }
  }

  @Override
  public void initializeModule(final CModule module, final CModuleInitializeReporter reporter)
      throws CouldntSaveDataException {
    getModuleConverter().initializeModule(this, module, reporter);
  }

  @Override
  public CTag insertTag(
      final ITreeNode<CTag> parent, final String name, final String description, final TagType type)
      throws CouldntSaveDataException {
    return PostgreSQLTagFunctions.insertTag(this, parent, name, description, type);
  }

  @Override
  public boolean isExporterDatabaseFormatValid() throws CouldntLoadDataException {
    return !hasTable("modules") || hasColumn("modules", "version");
  }

  @Override
  public boolean isInitialized() throws CouldntLoadDataException, InvalidDatabaseException {
    return hasAllTables();
  }

  @Override
  public ArrayList<IComment> loadCommentById(final Integer commentId)
      throws CouldntLoadDataException {
    return PostgreSQLCommentFunctions.loadCommentByCommentId(this, commentId);
  }

  @Override
  public INaviFunction loadFunction(final INaviModule module, final IAddress functionAddress)
      throws CouldntLoadDataException {
    return PostgreSQLFunctionsLoader.loadFunction(this, module, functionAddress);
  }

  @Override
  public List<INaviModule> loadModules() throws CouldntLoadDataException {
    if (getDebuggerManager() == null) {
      throw new CouldntLoadDataException("Error: Debugger manager must be loaded first");
    }

    setModules(
        PostgreSQLDatabaseFunctions.loadModules(this, getRawModules(), getDebuggerManager()));
    return new ArrayList<INaviModule>(getModules());
  }

  @Override
  public HashMap<Integer, ArrayList<IComment>> loadMultipleCommentsById(
      final Collection<Integer> commentIds) throws CouldntLoadDataException {
    return PostgreSQLCommentFunctions.loadMultipleCommentsById(this, commentIds);
  }

  @Override
  public List<INaviProject> loadProjects() throws CouldntLoadDataException {
    projects = PostgreSQLDatabaseFunctions.loadProjects(this, getDebuggerManager());
    return new ArrayList<INaviProject>(projects);
  }

  @Override
  public List<INaviRawModule> loadRawModules() throws CouldntLoadDataException {
    setRawModules(PostgreSQLDatabaseFunctions.loadRawModules(this));
    return getRawModules();
  }

  @Override
  public CTagManager loadTagManager(final TagType type) throws CouldntLoadDataException {
    final CTagManager manager = PostgreSQLTagManagerFunctions.loadTagManager(this, type);
    if (type == TagType.NODE_TAG) {
      nodeTagManager = manager;
    } else {
      setViewTagManager(manager);
    }
    return manager;
  }

  @Override
  public List<IUser> loadUsers() throws CouldntLoadDataException {
    return CGenericSQLUserFunctions.loadUsers(this);
  }

  @Override
  public void save(final TraceList traces) throws CouldntSaveDataException {
    PostgreSQLTraceFunctions.save(this, traces);
  }

  @Override
  public void save(final CView view) throws CouldntSaveDataException {
    PostgreSQLViewSaver.save(this, view);
  }

  @Override
  public void saveData(final INaviModule module, final byte[] data)
      throws CouldntSaveDataException {
    PostgreSQLDataFunctions.saveData(this, module, data);
  }

  @Override
  public void updateDatabase() throws CouldntUpdateDatabaseException {
    PostgreSQLDatabaseFunctions.updateDatabase(this);
  }

  @Override
  public void deleteSection(final Section section) throws CouldntLoadDataException {
    PostgreSQLSectionFunctions.deleteSection(this, section);
  }

  @Override
  public void deleteTypeInstance(final int moduleId, final int typeInstanceId)
      throws CouldntDeleteException {
    PostgreSQLTypeFunctions.deleteTypeInstance(this, moduleId, typeInstanceId);
  }

  @Override
  public void deleteTypeInstanceReference(
      final int moduleId, final BigInteger address, final int position, final int expressionId)
      throws CouldntDeleteException {
    PostgreSQLTypeFunctions.deleteTypeInstanceReference(
        this, moduleId, address, position, expressionId);
  }

  @Override
  public void setTypeInstanceName(final int moduleId, final int id, final String name)
      throws CouldntSaveDataException {
    PostgreSQLTypeFunctions.setTypeInstanceName(this, moduleId, id, name);
  }

  @Override
  public List<RawTypeInstanceReference> loadTypeInstanceReferences(final INaviModule module)
      throws CouldntLoadDataException {
    return PostgreSQLTypeFunctions.loadRawTypeInstanceReferences(
        this.getConnection().getConnection(), module);
  }

  @Override
  public RawTypeInstance loadTypeInstance(final INaviModule module, final Integer typeInstanceId)
      throws CouldntLoadDataException {
    return PostgreSQLTypeFunctions.loadRawTypeInstance(this, module, typeInstanceId);
  }

  @Override
  public RawTypeInstanceReference loadTypeInstanceReference(final INaviModule module,
      final Integer typeInstanceId, final BigInteger address, final Integer position,
      final Integer expressionId) throws CouldntLoadDataException {
    return PostgreSQLTypeFunctions.loadRawTypeInstanceReference(this,
        module,
        typeInstanceId,
        address,
        position,
        expressionId);
  }

  @Override
  public RawTypeMember loadTypeMember(final INaviModule module, final int typeMemberId)
      throws CouldntLoadDataException {
    return PostgreSQLTypeFunctions.loadRawTypeMember(this, module, typeMemberId);
  }

  @Override
  public RawBaseType loadType(final INaviModule module, final int baseTypeId)
      throws CouldntLoadDataException {
    return PostgreSQLTypeFunctions.loadRawBaseType(this, module, baseTypeId);
  }

  @Override
  public RawTypeSubstitution loadTypeSubstitution(final INaviModule module,
      final BigInteger address, final int position, final int expressionId)
      throws CouldntLoadDataException {
    return PostgreSQLTypeFunctions.loadRawTypeSubstitution(
        this, module, address, position, expressionId);
  }

  @Override
  public void close() {
    for (SQLProviderListener listener : listeners) {
      listener.providerClosing(this);
    }

    this.connection.closeConnection();
  }

  @Override
  public void addListener(SQLProviderListener listener) {
    listeners.addListener(listener);
  }

  @Override
  public void removeListener(SQLProviderListener listener) {
    listeners.removeListener(listener);
  }
}
