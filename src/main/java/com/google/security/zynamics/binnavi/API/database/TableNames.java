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
package com.google.security.zynamics.binnavi.API.database;

import com.google.security.zynamics.binnavi.Database.CTableNames;

// / Holds the names for all available BinNavi tables.
/**
 * Contains the names of the BinNavi tables of a BinNavi database.
 */
public final class TableNames {
  // ! Data parts table
  /**
   * Data parts table
   */
  public static final String DATA_PARTS_TABLE = CTableNames.DATA_PARTS_TABLE;

  /**
   * Project traces table
   */
  public static final String PROJECT_TRACES_TABLE = CTableNames.PROJECT_TRACES_TABLE;

  /**
   * Module traces table
   */
  public static final String MODULE_TRACES_TABLE = CTableNames.MODULE_TRACES_TABLE;

  /**
   * Text Nodes
   */
  public static final String TEXT_NODES_TABLE = CTableNames.TEXT_NODES_TABLE;

  /**
   * Project information is stored here.
   */
  public static final String PROJECTS_TABLE = CTableNames.PROJECTS_TABLE;

  /**
   * Module information is stored here.
   */
  public static final String MODULES_TABLE = CTableNames.MODULES_TABLE;

  /**
   * Address space information is stored here.
   */
  public static final String ADDRESS_SPACES_TABLE = CTableNames.ADDRESS_SPACES_TABLE;

  /**
   * Membership of modules in address spaces is stored here.
   */
  public static final String SPACE_MODULES_TABLE = CTableNames.SPACE_MODULES_TABLE;

  /**
   * Function information is stored here.
   */
  public static final String FUNCTIONS_TABLE = CTableNames.FUNCTIONS_TABLE;

  /**
   * Maps between native views and their backing functions.
   */
  public static final String FUNCTION_VIEWS_TABLE = CTableNames.FUNCTION_VIEWS_TABLE;

  /**
   * Information about instructions is stored here.
   */
  public static final String INSTRUCTIONS_TABLE = CTableNames.INSTRUCTIONS_TABLE;

  /**
   * Information about operands is stored here.
   */
  public static final String OPERANDS_TABLE = CTableNames.OPERANDS_TABLE;

  /**
   * Information about expression trees is stored here.
   */
  public static final String EXPRESSION_TREE_TABLE = CTableNames.EXPRESSION_TREE_TABLE;

  /**
   * Contains the IDs of known expression trees.
   */
  public static final String EXPRESSION_TREE_IDS_TABLE = CTableNames.EXPRESSION_TREE_IDS_TABLE;

  /**
   * Maps between operands and expression trees.
   */
  public static final String EXPRESSION_TREE_MAPPING_TABLE =
      CTableNames.EXPRESSION_TREE_MAPPING_TABLE;

  /**
   * Information about code nodes is stored here.
   */
  public static final String CODE_NODES_TABLE = CTableNames.CODE_NODES_TABLE;

  /**
   * Maps instructions to the code nodes they belong to.
   */
  public static final String CODENODE_INSTRUCTIONS_TABLE = CTableNames.CODENODE_INSTRUCTIONS_TABLE;

  /**
   * Information about edges is stored here.
   */
  public static final String EDGES_TABLE = CTableNames.EDGES_TABLE;

  /**
   * Information about edge paths is stored here.
   */
  public static final String EDGE_PATHS_TABLE = CTableNames.EDGE_PATHS_TABLE;

  /**
   * Information about function nodes is stored here.
   */
  public static final String FUNCTION_NODES_TABLE = CTableNames.FUNCTION_NODES_TABLE;

  /**
   * Information about group nodes is stored here.
   */
  public static final String GROUP_NODES_TABLE = CTableNames.GROUP_NODES_TABLE;

  /**
   * Common node information is stored here.
   */
  public static final String NODES_TABLE = CTableNames.NODES_TABLE;

  /**
   * Project settings are stored here.
   */
  public static final String PROJECT_SETTINGS_TABLE = CTableNames.PROJECT_SETTINGS_TABLE;

  /**
   * Module settings are stored here.
   */
  public static final String MODULE_SETTINGS_TABLE = CTableNames.MODULE_SETTINGS_TABLE;

  /**
   * Recorded debug events are stored here.
   */
  public static final String TRACES_TABLE = CTableNames.TRACES_TABLE;

  /**
   * Debug events of recorded debug traces are stored here.
   */
  public static final String TRACE_EVENT_TABLE = CTableNames.TRACE_EVENT_TABLE;

  /**
   * Register values of debug events of recorded debug traces are stored here.
   */
  public static final String TRACE_EVENT_VALUES_TABLE = CTableNames.TRACE_EVENT_VALUES_TABLE;

  /**
   * Information about views is stored here.
   */
  public static final String VIEWS_TABLE = CTableNames.VIEWS_TABLE;

  /**
   * Maps views to the modules they belong to.
   */
  public static final String MODULE_VIEWS_TABLE = CTableNames.MODULE_VIEWS_TABLE;

  /**
   * Maps views to the projects they belong to.
   */
  public static final String PROJECT_VIEWS_TABLE = CTableNames.PROJECT_VIEWS_TABLE;

  /**
   * View settings are stored here.
   */
  public static final String VIEW_SETTINGS_TABLE = CTableNames.VIEW_SETTINGS_TABLE;

  /**
   * Global edge comments are stored here.
   */
  public static final String GLOBAL_EDGE_COMMENTS_TABLE = CTableNames.GLOBAL_EDGE_COMMENTS_TABLE;

  /**
   * Global node comments are stored here.
   */
  public static final String GLOBAL_NODE_COMMENTS_TABLE = CTableNames.GLOBAL_NODE_COMMENTS_TABLE;

  /**
   * Debuggers assigned to a project are stored here.
   */
  public static final String PROJECT_DEBUGGERS_TABLE = CTableNames.PROJECT_DEBUGGERS_TABLE;

  /**
   * Information about debuggers is stored here.
   */
  public static final String DEBUGGERS_TABLE = CTableNames.DEBUGGERS_TABLE;

  /**
   * Information about tags is stored here.
   */
  public static final String TAGS_TABLE = CTableNames.TAGS_TABLE;

  /**
   * Information about tagged views is stored here.
   */
  public static final String TAGGED_VIEWS_TABLE = CTableNames.TAGGED_VIEWS_TABLE;

  /**
   * Information about tagged nodes is stored here.
   */
  public static final String TAGGED_NODES_TABLE = CTableNames.TAGGED_NODES_TABLE;

  /**
   * Expressions substitutions for operands is stored here.
   */
  public static final String EXPRESSION_SUBSTITUTIONS_TABLE =
      CTableNames.EXPRESSION_SUBSTITUTIONS_TABLE;

  /**
   * Information about raw modules is stored here.
   */
  public static final String RAW_MODULES_TABLE = CTableNames.RAW_MODULES_TABLE;

  /**
   * Information about address references is stored here.
   */
  public static final String ADDRESS_REFERENCES_TABLE = CTableNames.ADDRESS_REFERENCES_TABLE;

  /**
   * Raw exporter information about address comments is stored here.
   */
  public static final String RAW_ADDRESS_COMMENTS_TABLE = CTableNames.RAW_ADDRESS_COMMENTS_TABLE;

  /**
   * Raw exporter information about address references is stored here.
   */
  public static final String RAW_ADDRESS_REFERENCES_TABLE =
      CTableNames.RAW_ADDRESS_REFERENCES_TABLE;

  /**
   * Raw exporter information about basic blocks is stored here.
   */
  public static final String RAW_BASIC_BLOCKS_TABLE = CTableNames.RAW_BASIC_BLOCKS_TABLE;

  /**
   * Raw exporter information about basic block instructions is stored here.
   */
  public static final String RAW_BASIC_BLOCK_INSTRUCTIONS_TABLE =
      CTableNames.RAW_BASIC_BLOCK_INSTRUCTIONS_TABLE;

  /**
   * Raw exporter information about the Call graph is stored here.
   */
  public static final String RAW_CALLGRAPH_TABLE = CTableNames.RAW_CALLGRAPH_TABLE;

  /**
   * Raw exporter information about control flow graphs is stored here.
   */
  public static final String RAW_CONTROL_FLOW_GRAPHS_TABLE =
      CTableNames.RAW_CONTROL_FLOW_GRAPHS_TABLE;

  /**
   * Raw exporter information about expression nodes is stored here.
   */
  public static final String RAW_EXPRESSION_NODES_TABLE = CTableNames.RAW_EXPRESSION_NODES_TABLE;

  /**
   * Raw exporter information about expression substitutions are stored here.
   */
  public static final String RAW_EXPRESSION_SUBSTITUTIONS_TABLE =
      CTableNames.RAW_EXPRESSION_SUBSTITUTIONS_TABLE;

  /**
   * Raw exporter information about expression trees is stored here.
   */
  public static final String RAW_EXPRESSION_TREES_TABLE = CTableNames.RAW_EXPRESSION_TREES_TABLE;

  /**
   * Raw exporter information about expression tree nodes is stored here.
   */
  public static final String RAW_EXPRESSION_TREE_NODES_TABLE =
      CTableNames.RAW_EXPRESSION_TREE_NODES_TABLE;

  /**
   * Raw exporter information about functions is stored here.
   */
  public static final String RAW_FUNCTIONS_TABLE = CTableNames.RAW_FUNCTIONS_TABLE;

  /**
   * Raw exporter information about instructions is stored here.
   */
  public static final String RAW_INSTRUCTIONS_TABLE = CTableNames.RAW_INSTRUCTIONS_TABLE;

  /**
   * Raw exporter information about operands is stored here.
   */
  public static final String RAW_OPERANDS_TABLE = CTableNames.RAW_OPERANDS_TABLE;

  /**
   * Raw exporter information about sections are stored here.
   */
  public static final String RAW_SECTIONS = CTableNames.RAW_SECTIONS;

  /**
   * Raw exporter information about type instances are stored here.
   */
  public static final String RAW_TYPE_RENDERERS = CTableNames.RAW_TYPE_INSTACES;

  public static final String RAW_EXPRESSION_TYPE_INSTANCES =
      CTableNames.RAW_EXPRESSION_TYPE_INSTANCES;

  /**
   * Raw exporter information about types is stored here.
   */
  public static final String RAW_TYPES = CTableNames.RAW_TYPES;

  public static final String RAW_BASE_TYPES = CTableNames.RAW_BASE_TYPES;

  public static final String RAW_EXPRESSION_TYPES_TABLE = CTableNames.RAW_EXPRESSION_TYPES_TABLE;


  /**
   * You are not supposed to instantiate this class.
   */
  private TableNames() {
  }
}
