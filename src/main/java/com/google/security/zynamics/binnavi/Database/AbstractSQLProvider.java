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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseException;
import com.google.security.zynamics.binnavi.Database.Interfaces.ModuleConverter;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLAddressSpaceFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLDataFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLEdgeFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLFunctionFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLInstructionFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLModuleFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLNodeFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLProjectFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLRawModuleFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLSectionFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLSettingsFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLTagFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLTraceFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLTypeFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLViewFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgresSQLDebuggerFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Loaders.PostgreSQLAddressSpaceLoader;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Loaders.PostgreSQLCallgraphLoader;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Loaders.PostgreSQLFunctionsLoader;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Loaders.PostgreSQLModuleCallgraphsLoader;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Loaders.PostgreSQLModuleFlowgraphsLoader;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Loaders.PostgreSQLModuleMixedGraphsLoader;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Loaders.PostgreSQLProjectCallgraphLoader;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Loaders.PostgreSQLProjectFlowgraphsLoader;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Loaders.PostgreSQLProjectMixedGraphsLoader;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Loaders.PostgreSQLTracesLoader;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Loaders.PostgreSQLViewLoader;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Loaders.PostgreSQLViewsLoader;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.disassembly.CCallgraph;
import com.google.security.zynamics.binnavi.disassembly.COperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphView;
import com.google.security.zynamics.binnavi.disassembly.IFlowgraphView;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.INaviRawModule;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.BaseTypeCategory;
import com.google.security.zynamics.binnavi.disassembly.types.RawBaseType;
import com.google.security.zynamics.binnavi.disassembly.types.RawTypeInstance;
import com.google.security.zynamics.binnavi.disassembly.types.RawTypeMember;
import com.google.security.zynamics.binnavi.disassembly.types.RawTypeSubstitution;
import com.google.security.zynamics.binnavi.disassembly.types.Section;
import com.google.security.zynamics.binnavi.disassembly.types.SectionPermission;
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;
import com.google.security.zynamics.binnavi.disassembly.types.TypeSubstitution;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.ImmutableNaviViewConfiguration;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.ReferenceType;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.types.graphs.MutableDirectedGraph;
import com.google.security.zynamics.zylib.types.lists.IFilledList;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * The abstract SQL provider should be implemented by all real SQL providers. This class contains
 * the general strategy for all complex operations on the database. Only the actual queries are
 * delegated towards the implementing classes.
 */
public abstract class AbstractSQLProvider implements SQLProvider {
  /**
   * The tables array contains the names of all tables that exist in a proper BinNavi database
   * environment. Concrete SQL providers can use this array to check whether the database is in a
   * proper state. This array should contain all tables listed above except of RAW_MODULES_TABLE
   * which is an exporter table (modules).
   */
  private static final String[] TABLES = {CTableNames.PROJECTS_TABLE,
      CTableNames.MODULES_TABLE,
      CTableNames.ADDRESS_SPACES_TABLE,
      CTableNames.SPACE_MODULES_TABLE,
      CTableNames.FUNCTIONS_TABLE,
      CTableNames.FUNCTION_VIEWS_TABLE,
      CTableNames.INSTRUCTIONS_TABLE,
      CTableNames.OPERANDS_TABLE,
      CTableNames.EXPRESSION_TREE_TABLE,
      CTableNames.EXPRESSION_TREE_IDS_TABLE,
      CTableNames.EXPRESSION_TREE_MAPPING_TABLE,
      CTableNames.CODE_NODES_TABLE,
      CTableNames.CODENODE_INSTRUCTIONS_TABLE,
      CTableNames.EDGES_TABLE,
      CTableNames.EDGE_PATHS_TABLE,
      CTableNames.FUNCTION_NODES_TABLE,
      CTableNames.GROUP_NODES_TABLE,
      CTableNames.NODES_TABLE,
      CTableNames.PROJECT_SETTINGS_TABLE,
      CTableNames.MODULE_SETTINGS_TABLE,
      CTableNames.TRACES_TABLE,
      CTableNames.TRACE_EVENT_TABLE,
      CTableNames.TRACE_EVENT_VALUES_TABLE,
      CTableNames.VIEWS_TABLE,
      CTableNames.MODULE_VIEWS_TABLE,
      CTableNames.PROJECT_VIEWS_TABLE,
      CTableNames.VIEW_SETTINGS_TABLE,
      CTableNames.GLOBAL_EDGE_COMMENTS_TABLE,
      CTableNames.GLOBAL_NODE_COMMENTS_TABLE,
      CTableNames.PROJECT_DEBUGGERS_TABLE,
      CTableNames.DEBUGGERS_TABLE,
      CTableNames.TAGS_TABLE,
      CTableNames.TAGGED_VIEWS_TABLE,
      CTableNames.TAGGED_NODES_TABLE,
      CTableNames.EXPRESSION_SUBSTITUTIONS_TABLE,
      CTableNames.COMMENTS_TABLE,
      CTableNames.COMMENTS_AUDIT_TABLE,
      CTableNames.TYPE_MEMBERS_TABLE,
      CTableNames.BASE_TYPES_TABLE,
      CTableNames.USER_TABLE,
      CTableNames.EXPRESSION_TYPES_TABLE};

  /**
   * The connection to the database.
   */
  protected final CConnection connection;

  /**
   * List of projects stored in the database.
   */
  protected List<INaviProject> projects;

  /**
   * List of modules stored in the database.
   */
  private List<INaviModule> modules;
  /**
   * List of raw modules stored in the database.
   */
  private List<INaviRawModule> rawModules;
  /**
   * View tag manager of the database.
   */
  private CTagManager viewTagManager;

  /**
   * Node tag manager of the database.
   */
  protected CTagManager nodeTagManager;

  /**
   * The debugger template manager of this database.
   */
  private final DebuggerTemplateManager debuggerManager = new DebuggerTemplateManager(this);

  public AbstractSQLProvider(final CConnection connection) {
    this.connection =
        Preconditions.checkNotNull(connection, "IE00042: Connection argument can not be null");
  }

  /**
   * Parses the contents of the {@link BufferedReader input} as SQL file and returns the result as
   * string.
   *
   * @param input The {@link BufferedReader} to parse.
   * @return A String containing the contents of the {@link BufferedReader input}.
   * @throws IOException If the parsing was unsuccessful.
   */
  private static String parseSQLFile(final BufferedReader input) throws IOException {
    final StringBuffer contents = new StringBuffer();
    String line = null;

    while ((line = input.readLine()) != null) {
      if (line.length() > 0 && line.charAt(0) == '#') {
        continue;
      }
      contents.append(line);
      contents.append('\n');
    }
    input.close();
    return contents.toString();
  }

  /**
   * Parses a resource as SQL file and returns the contents in a string.
   *
   * @param resource an {@link InputStream stream} with the class relative resource.
   * @return A String containing the contents of the resource.
   * @throws IOException If the resource could not be parsed.
   */
  public static String parseResourceAsSQLFile(final InputStream resource) throws IOException {
    return parseSQLFile(new BufferedReader(new InputStreamReader(resource)));
  }

  /**
   * Parses a system resource as SQL file and returns the contents in a string.
   *
   * @param file The file to use as a resource. It needs to include the complete path.
   * @return A String containing the contents of the resource.
   * @throws IOException If the resource could not be parsed.
   */
  public static String parseSystemResourceAsSQLFile(final String file) throws IOException {
    return parseSQLFile(
        new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(file))));
  }

  protected abstract void createModulesTable() throws SQLException;

  /**
   * Executes an updating statement on the database.
   *
   * @param statement The updating statement.
   *
   * @throws SQLException Thrown if the statement could not be executed.
   */
  protected void executeUpdate(final String statement) throws SQLException {
    connection.executeUpdate(statement, true);
  }

  protected DebuggerTemplateManager getDebuggerManager() {
    return debuggerManager;
  }

  /**
   * Returns the module converter object.
   *
   * @return The module converter object.
   */
  protected abstract ModuleConverter getModuleConverter();

  /**
   * Returns the name of the file that contains the SQL table definitions.
   *
   * @return The name of the file that contains the SQL table definitions.
   */
  protected abstract String getTablesFile();

  /**
   * Makes sure that all required BinNavi database tables exist. This method tries to find out
   * whether the database is empty or already initialized (or inconsistent).
   *
   * @return True, if all required BinNavi tables exist in the database. False, if the database is
   *         empty.
   *
   * @throws CouldntLoadDataException Thrown if access to the database failed.
   * @throws InvalidDatabaseException Thrown if the database is in an inconsistent state. This means
   *         some but not all tables exist.
   */
  protected boolean hasAllTables() throws CouldntLoadDataException, InvalidDatabaseException {
    NaviLogger.info("Checking the existence of the BinNavi database tables");

    final int counter =
        PostgreSQLHelpers.getTableCount(getConnection(), Lists.newArrayList(TABLES));

    // Databases come in three states:
    //
    // 1. Empty
    // 2. Properly Initialized
    // 3. Missing some but not all tables, this is a problem

    if (counter == 0) {
      return false;
    } else if (counter == TABLES.length) {
      return true;
    } else {
      throw new InvalidDatabaseException(
          String.format("Invalid database state (%d of %d tables found)", counter, TABLES.length));
    }
  }

  /**
   * Determines whether the database has a table with the given name.
   *
   * @param tableName The name of the table to check for.
   *
   * @return True, if a table with the given name exists in the database. False, otherwise.
   *
   * @throws CouldntLoadDataException Thrown if the existence of the table could not be determined.
   */
  protected abstract boolean hasTable(String tableName) throws CouldntLoadDataException;

  protected void setRawModules(final List<INaviRawModule> rawModules) {
    this.rawModules = rawModules;
  }

  @Override
  public void addDebugger(final INaviProject project, final DebuggerTemplate debugger)
      throws CouldntSaveDataException {
    PostgreSQLProjectFunctions.addDebugger(this, project, debugger);
  }

  @Override
  public void addModule(final INaviAddressSpace addressSpace, final INaviModule module)
      throws CouldntSaveDataException {
    PostgreSQLAddressSpaceFunctions.addModule(this, addressSpace, module);
  }

  @Override
  public void addReference(final INaviOperandTreeNode node, final IAddress address,
      final ReferenceType type) throws CouldntSaveDataException {
    PostgreSQLInstructionFunctions.addReference(this, node, address, type);
  }

  @Override
  public Integer appendFunctionNodeComment(final INaviFunctionNode functionNode,
      final String commentText, final Integer userId) throws CouldntSaveDataException {
    return PostgreSQLNodeFunctions.appendLocalFunctionNodeComment(this, functionNode, commentText,
        userId);
  }

  @Override
  public Integer appendGlobalCodeNodeComment(final INaviCodeNode codeNode, final String commentText,
      final Integer userId) throws CouldntSaveDataException {
    return PostgreSQLNodeFunctions.appendGlobalCodeNodeComment(this, codeNode, commentText, userId);
  }

  @Override
  public Integer appendGlobalEdgeComment(final INaviEdge edge, final String commentText,
      final Integer userId) throws CouldntSaveDataException {
    return PostgreSQLEdgeFunctions.appendGlobalEdgeComment(this, edge, commentText, userId);
  }

  @Override
  public Integer appendGlobalInstructionComment(final INaviInstruction instruction,
      final String commentText, final Integer userId) throws CouldntSaveDataException {
    return PostgreSQLInstructionFunctions.appendGlobalInstructionComment(this, instruction,
        commentText, userId);
  }

  @Override
  public Integer appendLocalCodeNodeComment(final INaviCodeNode codeNode, final String commentText,
      final Integer userId) throws CouldntSaveDataException {
    return PostgreSQLNodeFunctions.appendLocalCodeNodeComment(this, codeNode, commentText, userId);
  }

  @Override
  public Integer appendLocalEdgeComment(final INaviEdge edge, final String commentText,
      final Integer userId) throws CouldntSaveDataException {
    return PostgreSQLEdgeFunctions.appendLocalEdgeComment(this, edge, commentText, userId);
  }

  @Override
  public Integer appendLocalInstructionComment(final INaviCodeNode codeNode,
      final INaviInstruction instruction, final String commentText, final Integer userId)
      throws CouldntSaveDataException {
    return PostgreSQLInstructionFunctions.appendLocalInstructionComment(this, codeNode, instruction,
        commentText, userId);
  }

  @Override
  public void assignDebugger(final CAddressSpace addressSpace, final DebuggerTemplate debugger)
      throws CouldntSaveDataException {
    PostgreSQLAddressSpaceFunctions.assignDebugger(this, addressSpace, debugger);
  }

  @Override
  public void assignDebugger(final INaviModule module, final DebuggerTemplate debugger)
      throws CouldntSaveDataException {
    PostgreSQLModuleFunctions.assignDebugger(this, module, debugger);
  }

  @Override
  public DebuggerTemplate createDebuggerTemplate(final String name, final String host,
      final int port) throws CouldntSaveDataException {
    return PostgresSQLDebuggerFunctions.createDebuggerTemplate(this, name, host, port);
  }

  /**
   * Creates the BinNavi specific tables from the tables definition file.
   *
   * @throws IOException Thrown when the database schema file could not be read.
   * @throws CouldntLoadDataException Thrown
   */
  public void createEmptyTables() throws IOException, CouldntLoadDataException {
    final String tablesFile = getTablesFile();
    Preconditions.checkNotNull(tablesFile, "IE01225: E00037: Tables file can not be null");
    NaviLogger.info("Reading BinNavi tables definition file %s", tablesFile);
    final String sqlStatements =
        parseSystemResourceAsSQLFile("com/google/security/zynamics/binnavi/data/" + tablesFile);
    final boolean hasModulesTable = hasTable("modules");

    try {
      if (!hasModulesTable) {
        createModulesTable();
      }
      executeUpdate(sqlStatements);
    } catch (final SQLException e) {
      CUtilityFunctions.logException(e);
    }
  }

  @Override
  public int createSection(final int moduleId,
      final String name,
      final Integer commentId,
      final BigInteger startAddress,
      final BigInteger endAddress,
      final SectionPermission permission,
      final byte[] data) throws CouldntSaveDataException {
    return PostgreSQLSectionFunctions.createSection(getConnection().getConnection(),
        moduleId,
        name,
        commentId,
        startAddress,
        endAddress,
        permission,
        data);
  }

  @Override
  public int createType(final int moduleId,
      final String name,
      final int size,
      final Integer childPointerTypeId,
      final boolean signed,
      final BaseTypeCategory category) throws CouldntSaveDataException {
    return PostgreSQLTypeFunctions.createType(getConnection().getConnection(),
        moduleId,
        name,
        size,
        childPointerTypeId,
        signed,
        category);
  }

  @Override
  public int createTypeInstance(final int moduleId,
      final String name,
      final Integer commentId,
      final int typeId,
      final int sectionId,
      final long sectionOffset) throws CouldntSaveDataException {
    return PostgreSQLTypeFunctions.createTypeInstance(getConnection().getConnection(),
        moduleId,
        name,
        commentId,
        typeId,
        sectionId,
        sectionOffset);
  }

  @Override
  public void createTypeInstanceReference(final int moduleId, final long address,
      final int position, final int expressionId, final int typeInstanceId)
      throws CouldntSaveDataException {
    PostgreSQLTypeFunctions.createTypeInstanceReference(getConnection().getConnection(),
        moduleId,
        address,
        position,
        expressionId,
        typeInstanceId);
  }

  @Override
  public int createTypeMember(final INaviModule module,
      final int containingTypeId,
      final int baseTypeId,
      final String name,
      final Optional<Integer> offset,
      final Optional<Integer> numberOfElements, Optional<Integer> argumentIndex) throws CouldntSaveDataException {
    return PostgreSQLTypeFunctions.createTypeMember(getConnection().getConnection(),
        containingTypeId,
        offset,
        name,
        baseTypeId,
        numberOfElements,
        module);
  }

  @Override
  public void createTypeSubstitution(final int treeNodeId,
      final int baseTypeId,
      final List<Integer> memberPath,
      final int position,
      final int offset,
      final IAddress address,
      final INaviModule module) throws CouldntSaveDataException {
    PostgreSQLTypeFunctions.createTypeSubstitution(getConnection().getConnection(),
        treeNodeId,
        baseTypeId,
        memberPath,
        position,
        offset,
        address,
        module);
  }

  @Override
  public void deleteAddressSpace(final INaviAddressSpace addressSpace)
      throws CouldntDeleteException {
    PostgreSQLAddressSpaceFunctions.deleteAddressSpace(this, addressSpace);
  }

  @Override
  public void deleteDebugger(final DebuggerTemplate debugger) throws CouldntDeleteException {
    PostgresSQLDebuggerFunctions.deleteDebugger(this, debugger);
  }

  @Override
  public void deleteMember(final TypeMember member, final INaviModule module)
      throws CouldntDeleteException {
    PostgreSQLTypeFunctions.deleteMember(getConnection().getConnection(), member, module);
  }

  @Override
  public void deleteProject(final INaviProject project) throws CouldntDeleteException {
    PostgreSQLProjectFunctions.deleteProject(this, project);
  }

  @Override
  public void deleteRawModule(final INaviRawModule module) throws CouldntDeleteException {
    PostgreSQLRawModuleFunctions.deleteRawModule(this, module);
  }

  @Override
  public void deleteReference(final COperandTreeNode operandTreeNode, final IAddress target,
      final ReferenceType type) throws CouldntDeleteException {
    PostgreSQLInstructionFunctions.deleteReference(this, operandTreeNode, target, type);
  }

  @Override
  public void deleteTag(final ITreeNode<CTag> tag) throws CouldntDeleteException {
    PostgreSQLTagFunctions.deleteTag(this, tag);
  }

  @Override
  public void deleteTagSubtree(final ITreeNode<CTag> tag) throws CouldntDeleteException {
    PostgreSQLTagFunctions.deleteTagSubtree(this, tag);
  }

  @Override
  public void deleteTrace(final TraceList trace) throws CouldntDeleteException {
    PostgreSQLTraceFunctions.deleteTrace(this, trace);
  }

  @Override
  public void deleteType(final BaseType baseType, final INaviModule module)
      throws CouldntDeleteException {
    PostgreSQLTypeFunctions.deleteType(getConnection().getConnection(), baseType, module);
  }

  @Override
  public void deleteTypeSubstitution(final INaviModule module,
      final TypeSubstitution typeSubstitution) throws CouldntDeleteException {
    PostgreSQLTypeFunctions.deleteTypeSubstitution(getConnection().getConnection(), module,
        typeSubstitution);
  }

  @Override
  public void deleteView(final INaviView view) throws CouldntDeleteException {
    PostgreSQLViewFunctions.deleteView(this, view);
  }

  @Override
  public ResultSet executeQuery(final String query) throws SQLException {
    return connection.executeQuery(query, true);
  }

  /**
   * Returns the module with the given ID.
   *
   * @param moduleId The ID to search for.
   *
   * @return The module with the given ID.
   */
  @Override
  public INaviModule findModule(final int moduleId) {
    return Iterables.find(modules, new Predicate<INaviModule>() {
      @Override
      public boolean apply(final INaviModule module) {
        return module.getConfiguration().getId() == moduleId;
      }
    });
  }

  /**
   * Returns the project with the given ID.
   *
   * @param projectId The ID of the project to search for.
   *
   * @return The project with the given ID.
   */
  @Override
  public INaviProject findProject(final int projectId) {
    return Iterables.find(projects, new Predicate<INaviProject>() {
      @Override
      public boolean apply(final INaviProject project) {
        return project.getConfiguration().getId() == projectId;
      }
    });
  }

  /**
   * Returns the connection to the database.
   *
   * @return The connection to the database.
   */
  @Override
  public CConnection getConnection() {
    return connection;
  }

  @Override
  public List<INaviView> getDerivedViews(final INaviView view) throws CouldntLoadDataException {
    return PostgreSQLViewFunctions.getDerivedViews(this, view);
  }

  @Override
  public Date getModificationDate(final CAddressSpace addressSpace)
      throws CouldntLoadDataException {
    return PostgreSQLAddressSpaceFunctions.getModificationDate(this, addressSpace);
  }

  @Override
  public Date getModificationDate(final INaviModule module) throws CouldntLoadDataException {
    return PostgreSQLModuleFunctions.getModificationDate(this, module);
  }

  @Override
  public Date getModificationDate(final INaviProject project) throws CouldntLoadDataException {
    return PostgreSQLProjectFunctions.getModificationDate(this, project);
  }

  @Override
  public Date getModificationDate(final INaviView view) throws CouldntLoadDataException {
    return PostgreSQLViewFunctions.getModificationDate(this, view);
  }

  @Override
  public List<INaviModule> getModules() {
    return modules;
  }

  @Override
  public CTagManager getNodeTagManager() {
    return nodeTagManager;
  }

  @Override
  public List<INaviProject> getProjects() {
    return projects;
  }

  public List<INaviRawModule> getRawModules() {
    return rawModules;
  }

  @Override
  public List<INaviView> getViewsWithAddress(final INaviProject project,
      final List<UnrelocatedAddress> addresses, final boolean all)
      throws CouldntLoadDataException {
    return PostgreSQLProjectFunctions.getViewsWithAddresses(this, project, addresses, all);
  }

  @Override
  public List<INaviView> getViewsWithAddresses(final INaviModule module,
      final List<UnrelocatedAddress> addresses, final boolean all)
      throws CouldntLoadDataException {
    return PostgreSQLModuleFunctions.getViewsWithAddresses(this, module, addresses, all);
  }

  @Override
  public CTagManager getViewTagManager() {
    return viewTagManager;
  }

  @Override
  public List<CAddressSpace> loadAddressSpaces(final INaviProject project)
      throws CouldntLoadDataException {
    return PostgreSQLAddressSpaceLoader.loadAddressSpaces(this, project, getDebuggerManager(),
        getModules());
  }

  @Override
  public CCallgraph loadCallgraph(final CModule module, final int callgraphId,
      final List<INaviFunction> functions) throws CouldntLoadDataException {
    return PostgreSQLCallgraphLoader.loadCallgraph(this, module, callgraphId, functions);
  }

  @Override
  public IFilledList<ICallgraphView> loadCallgraphViews(final CModule module)
      throws CouldntLoadDataException {
    return PostgreSQLModuleCallgraphsLoader.loadCallgraphViews(this, module, getViewTagManager(),
        nodeTagManager);
  }

  @Override
  public List<ICallgraphView> loadCallgraphViews(final CProject project)
      throws CouldntLoadDataException {
    return PostgreSQLProjectCallgraphLoader.loadCallgraphViews(this, project, getViewTagManager(),
        nodeTagManager);
  }

  @Override
  public byte[] loadData(final CModule module) throws CouldntLoadDataException {
    return PostgreSQLDataFunctions.loadData(this, module);
  }

  @Override
  public DebuggerTemplateManager loadDebuggers() throws CouldntLoadDataException {
    PostgresSQLDebuggerFunctions.loadDebuggers(this, getDebuggerManager());
    return getDebuggerManager();
  }

  @Override
  public ImmutableNaviViewConfiguration loadFlowGraphInformation(final INaviModule module,
      final Integer viewId) throws CouldntLoadDataException {
    return PostgreSQLModuleFlowgraphsLoader.loadFlowGraphInformation(this, module, viewId);
  }

  @Override
  public ImmutableNaviViewConfiguration loadFlowGraphInformation(final INaviProject project,
      final Integer viewId) throws CouldntLoadDataException {
    return PostgreSQLProjectFlowgraphsLoader.loadFlowGraphInformation(this, project, viewId);
  }

  @Override
  public ImmutableList<IFlowgraphView> loadFlowgraphs(final CModule module)
      throws CouldntLoadDataException {
    return PostgreSQLModuleFlowgraphsLoader.loadFlowgraphs(this, module, getViewTagManager(),
        nodeTagManager);
  }

  @Override
  public List<IFlowgraphView> loadFlowgraphs(final CProject project)
      throws CouldntLoadDataException {
    return PostgreSQLProjectFlowgraphsLoader.loadFlowgraphs(this, project, getViewTagManager(),
        nodeTagManager);
  }

  @Override
  public List<INaviFunction> loadFunctions(final INaviModule module,
      final List<IFlowgraphView> views) throws CouldntLoadDataException {
    return PostgreSQLFunctionsLoader.loadFunctions(this, module, views);
  }

  @Override
  public IFilledList<INaviView> loadMixedgraphs(final CModule module)
      throws CouldntLoadDataException {
    return PostgreSQLModuleMixedGraphsLoader.loadMixedgraphs(this, module, getViewTagManager(),
        nodeTagManager);
  }

  @Override
  public List<INaviView> loadMixedgraphs(final CProject project) throws CouldntLoadDataException {
    return PostgreSQLProjectMixedGraphsLoader.loadMixedgraphs(this, project, getViewTagManager(),
        nodeTagManager);
  }

  @Override
  public List<Pair<IAddress, INaviModule>> loadModules(final CAddressSpace addressSpace)
      throws CouldntLoadDataException {
    return PostgreSQLAddressSpaceFunctions.loadModules(this, addressSpace);
  }

  @Override
  public ICallgraphView loadNativeCallgraph(final CModule module) throws CouldntLoadDataException {
    return PostgreSQLModuleCallgraphsLoader.loadNativeCallgraph(this, module, getViewTagManager(),
        nodeTagManager);
  }

  @Override
  public ImmutableList<IFlowgraphView> loadNativeFlowgraphs(final CModule module)
      throws CouldntLoadDataException {
    return PostgreSQLModuleFlowgraphsLoader.loadNativeFlowgraphs(this, module, getViewTagManager(),
        nodeTagManager);
  }

  @Override
  public Map<Section, Integer> loadSections(final INaviModule module)
      throws CouldntLoadDataException {
    return PostgreSQLSectionFunctions.loadSections(this, module);
  }

  @Override
  public Map<String, String> loadSettings(final CView view) throws CouldntLoadDataException {
    return PostgreSQLViewFunctions.loadSettings(this, view);
  }

  @Override
  public IFilledList<TraceList> loadTraces(final CModule module) throws CouldntLoadDataException {
    return PostgreSQLTracesLoader.loadTraces(this, CTableNames.MODULE_TRACES_TABLE, "module_id",
        module.getConfiguration().getId(), getModules());
  }

  @Override
  public IFilledList<TraceList> loadTraces(final CProject project)
      throws CouldntLoadDataException {
    return PostgreSQLTracesLoader.loadTraces(this, CTableNames.PROJECT_TRACES_TABLE, "project_id",
        project.getConfiguration().getId(), getModules());
  }

  @Override
  public List<RawTypeInstance> loadTypeInstances(final INaviModule module)
      throws CouldntLoadDataException {
    return PostgreSQLTypeFunctions.loadRawTypeInstances(getConnection().getConnection(), module);
  }

  @Override
  public List<RawTypeMember> loadTypeMembers(final INaviModule module)
      throws CouldntLoadDataException {
    return PostgreSQLTypeFunctions.loadRawTypeMembers(getConnection().getConnection(), module);
  }

  @Override
  public List<RawBaseType> loadTypes(final INaviModule module) throws CouldntLoadDataException {
    return PostgreSQLTypeFunctions.loadRawTypes(getConnection().getConnection(), module);
  }

  @Override
  public List<RawTypeSubstitution> loadTypeSubstitutions(final INaviModule module)
      throws CouldntLoadDataException {
    return PostgreSQLTypeFunctions.loadRawTypeSubstitutions(getConnection().getConnection(),
        module);
  }

  @Override
  public MutableDirectedGraph<INaviViewNode, INaviEdge> loadView(final INaviView view)
      throws CouldntLoadDataException, CPartialLoadException {
    return PostgreSQLViewLoader.loadView(this, view, getModules(), nodeTagManager);
  }

  @Override
  public ImmutableBiMap<INaviView, INaviFunction> loadViewFunctionMapping(
      final List<IFlowgraphView> flowgraphs, final List<INaviFunction> functions,
      final CModule module) throws CouldntLoadDataException {
    return PostgreSQLViewsLoader.loadViewFunctionMapping(this, flowgraphs, functions, module);
  }

  @Override
  public void moveTag(final ITreeNode<CTag> parent, final ITreeNode<CTag> child, final TagType type)
      throws CouldntSaveDataException {
    PostgreSQLTagFunctions.moveTag(this, parent, child, type);
  }

  @Override
  public String readSetting(final CModule module, final String key)
      throws CouldntLoadDataException {
    return PostgreSQLSettingsFunctions.readSetting(this, module, key);
  }

  @Override
  public String readSetting(final CProject project, final String key)
      throws CouldntLoadDataException {
    return PostgreSQLSettingsFunctions.readSetting(this, project, key);
  }

  @Override
  public void removeDebugger(final INaviProject project, final DebuggerTemplate debugger)
      throws CouldntSaveDataException {
    PostgreSQLProjectFunctions.removeDebugger(this, project, debugger);
  }

  @Override
  public void removeModule(final INaviAddressSpace addressSpace, final INaviModule module)
      throws CouldntDeleteException, CouldntSaveDataException {
    PostgreSQLAddressSpaceFunctions.removeModule(this, addressSpace, module);
  }

  @Override
  public void removeTag(final INaviView view, final CTag tag) throws CouldntSaveDataException {
    PostgreSQLViewFunctions.untagView(this, view, tag);
  }

  @Override
  public void removeTagFromNode(final INaviViewNode node, final int tagId)
      throws CouldntSaveDataException {
    PostgreSQLNodeFunctions.untagNode(this, node, tagId);
  }

  @Override
  public void forwardFunction(final INaviFunction source, final INaviFunction target)
      throws CouldntSaveDataException {
    PostgreSQLFunctionFunctions.resolveFunction(this, source, target);
  }

  @Override
  public void saveSettings(final CView view, final Map<String, String> settings)
      throws CouldntSaveDataException {
    PostgreSQLViewFunctions.saveSettings(this, view, settings);
  }

  @Override
  public void saveTagToNode(final INaviViewNode node, final int tagId)
      throws CouldntSaveDataException {
    PostgreSQLNodeFunctions.tagNode(this, node, tagId);
  }

  @Override
  public void setDescription(final CAddressSpace addressSpace, final String description)
      throws CouldntSaveDataException {
    PostgreSQLAddressSpaceFunctions.setDescription(this, addressSpace, description);
  }

  @Override
  public void setDescription(final CTag tag, final String description)
      throws CouldntSaveDataException {
    PostgreSQLTagFunctions.setDescription(this, tag, description);
  }

  @Override
  public void setDescription(final TraceList traceList, final String description)
      throws CouldntSaveDataException {
    PostgreSQLTraceFunctions.setDescription(this, traceList, description);
  }

  @Override
  public void setDescription(final INaviFunction function, final String comment)
      throws CouldntSaveDataException {
    PostgreSQLFunctionFunctions.setDescription(this, function, comment);
  }

  @Override
  public void setDescription(final INaviModule module, final String description)
      throws CouldntSaveDataException {
    PostgreSQLModuleFunctions.setDescription(this, module, description);
  }

  @Override
  public void setDescription(final INaviProject project, final String description)
      throws CouldntSaveDataException {
    PostgreSQLProjectFunctions.setDescription(this, project, description);
  }

  @Override
  public void setDescription(final INaviView view, final String description)
      throws CouldntSaveDataException {
    PostgreSQLViewFunctions.setDescription(this, view, description);
  }

  @Override
  public void setFileBase(final INaviModule module, final IAddress addr)
      throws CouldntSaveDataException {
    PostgreSQLModuleFunctions.setFileBase(this, module, addr);
  }

  @Override
  public void setGlobalReplacement(final INaviOperandTreeNode operandTreeNode,
      final String replacement) throws CouldntSaveDataException {
    PostgreSQLInstructionFunctions.setGlobalReplacement(this, operandTreeNode, replacement);
  }

  @Override
  public void setHost(final DebuggerTemplate debugger, final String host)
      throws CouldntSaveDataException {
    PostgresSQLDebuggerFunctions.setHost(this, debugger, host);
  }

  @Override
  public void setImageBase(final INaviAddressSpace addressSpace, final INaviModule module,
      final IAddress addr) throws CouldntSaveDataException {
    PostgreSQLAddressSpaceFunctions.setImageBase(this, addressSpace, module, addr);
  }

  @Override
  public void setImageBase(final INaviModule module, final IAddress addr)
      throws CouldntSaveDataException {
    PostgreSQLModuleFunctions.setImageBase(this, module, addr);
  }

  @Override
  public void setModules(final List<INaviModule> modules) {
    this.modules = modules;
  }

  @Override
  public void setName(final CAddressSpace addressSpace, final String name)
      throws CouldntSaveDataException {
    PostgreSQLAddressSpaceFunctions.setName(this, addressSpace, name);
  }

  @Override
  public void setName(final DebuggerTemplate debugger, final String name)
      throws CouldntSaveDataException {
    PostgresSQLDebuggerFunctions.setName(this, debugger, name);
  }

  @Override
  public void setName(final CTag tag, final String name) throws CouldntSaveDataException {
    PostgreSQLTagFunctions.setName(this, tag, name);
  }

  @Override
  public void setName(final TraceList traceList, final String name)
      throws CouldntSaveDataException {
    PostgreSQLTraceFunctions.setName(this, traceList, name);
  }

  @Override
  public void setName(final INaviFunction function, final String name)
      throws CouldntSaveDataException {
    PostgreSQLFunctionFunctions.setName(this, function, name);
  }

  @Override
  public void setName(final INaviModule module, final String name) throws CouldntSaveDataException {
    PostgreSQLModuleFunctions.setName(this, module, name);
  }

  @Override
  public void setName(final INaviProject project, final String name)
      throws CouldntSaveDataException {
    PostgreSQLProjectFunctions.setName(this, project, name);
  }

  @Override
  public void setName(final INaviView view, final String name) throws CouldntSaveDataException {
    PostgreSQLViewFunctions.setName(this, view, name);
  }

  @Override
  public void setPort(final DebuggerTemplate debugger, final int port)
      throws CouldntSaveDataException {
    PostgresSQLDebuggerFunctions.setPort(this, debugger, port);
  }

  @Override
  public void setReplacement(final COperandTreeNode operandTreeNode, final String replacement)
      throws CouldntSaveDataException {
    PostgreSQLInstructionFunctions.setReplacement(this, operandTreeNode, replacement);
  }

  @Override
  public void setSectionName(final int moduleId, final int sectionId, final String name)
      throws CouldntSaveDataException {
    PostgreSQLSectionFunctions.setSectionName(this.getConnection().getConnection(), moduleId,
        sectionId, name);
  }

  @Override
  public void setStared(final INaviModule module, final boolean isStared)
      throws CouldntSaveDataException {
    PostgreSQLModuleFunctions.starModule(this, module, isStared);
  }

  @Override
  public void setStared(final INaviView view, final boolean isStared)
      throws CouldntSaveDataException {
    PostgreSQLViewFunctions.starView(this, view, isStared);
  }

  public void setViewTagManager(final CTagManager viewTagManager) {
    this.viewTagManager = viewTagManager;
  }

  public void setViewTagManaget(final CTagManager viewTagManager) {
    setViewTagManager(viewTagManager);
  }

  @Override
  public void tagView(final INaviView view, final CTag tag) throws CouldntSaveDataException {
    PostgreSQLViewFunctions.tagView(this, view, tag);
  }

  @Override
  public void updateMember(final TypeMember member,
      final String newName,
      final BaseType newBaseType,
      final Optional<Integer> newoffset,
      final Optional<Integer> newNumberOfElements,
      final Optional<Integer> newArgumentIndex,
      final INaviModule module) throws CouldntSaveDataException {
    PostgreSQLTypeFunctions.updateTypeMember(getConnection().getConnection(),
        member,
        newName,
        newBaseType,
        newoffset,
        newNumberOfElements,
        newArgumentIndex,
        module);
  }

  @Override
  public void updateMemberOffsets(final List<Integer> updatedMembers, final int delta,
      final List<Integer> implicitlyUpdatedMembers, final int implicitDelta,
      final INaviModule module) throws CouldntSaveDataException {
    PostgreSQLTypeFunctions.updateMemberOffsets(getConnection().getConnection(),
        updatedMembers,
        delta,
        implicitlyUpdatedMembers,
        implicitDelta,
        module);
  }

  @Override
  public void updateType(final BaseType baseType, final String name, final int size,
      final boolean isSigned, final INaviModule module) throws CouldntSaveDataException {
    PostgreSQLTypeFunctions.updateType(getConnection().getConnection(),
        baseType,
        name,
        size,
        isSigned,
        module);
  }

  @Override
  public void updateTypeSubstitution(final TypeSubstitution substitution, final BaseType baseType,
      final List<Integer> memberPath, final int offset, final INaviModule module)
      throws CouldntSaveDataException {
    PostgreSQLTypeFunctions.updateTypeSubstitution(getConnection().getConnection(),
        substitution,
        baseType,
        memberPath,
        substitution.getPosition(),
        offset,
        module);
  }

  @Override
  public void writeSetting(final CModule module, final String key, final String value)
      throws CouldntSaveDataException {
    PostgreSQLSettingsFunctions.writeSetting(this, module, key, value);
  }

  @Override
  public void writeSetting(final CProject project, final String key, final String value)
      throws CouldntSaveDataException {
    PostgreSQLSettingsFunctions.writeSetting(this, project, key, value);
  }
}
