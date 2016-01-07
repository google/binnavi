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

/**
 * Contains the names of the BinNavi tables of a BinNavi database.
 */
public final class CTableNames {
  /**
   * Sections table
   */
  public static final String SECTIONS_TABLE = "bn_sections";

  /**
   * Table that holds expression tree to type instance connection information.
   */
  public static final String EXPRESSION_TYPE_INSTANCES_TABLE = "bn_expression_type_instances";

  /**
   * Table which holds type instance information.
   */
  public static final String TYPE_INSTANCE_TABLE = "bn_type_instances";

  /**
   * User table.
   */
  public static final String USER_TABLE = "bn_users";

  /**
   * Comment table.
   */
  public static final String COMMENTS_TABLE = "bn_comments";

  /**
   * Audit table for comments.
   */
  public static final String COMMENTS_AUDIT_TABLE = "bn_comments_audit";

  /**
   * Types table.
   */
  public static final String TYPE_MEMBERS_TABLE = "bn_types";

  /**
   * Base types table.
   */
  public static final String BASE_TYPES_TABLE = "bn_base_types";

  /**
   * Expression types table.
   */
  public static final String EXPRESSION_TYPES_TABLE = "bn_expression_types";

  /**
   * Data parts table.
   */
  public static final String DATA_PARTS_TABLE = "bn_data_parts";

  /**
   * Project traces table.
   */
  public static final String PROJECT_TRACES_TABLE = "bn_project_traces";

  /**
   * Module traces table.
   */
  public static final String MODULE_TRACES_TABLE = "bn_module_traces";

  /**
   * Text Nodes.
   */
  public static final String TEXT_NODES_TABLE = "bn_text_nodes";

  /**
   * Project information is stored here.
   */
  public static final String PROJECTS_TABLE = "bn_projects";

  /**
   * Module information is stored here.
   */
  public static final String MODULES_TABLE = "bn_modules";

  /**
   * Address space information is stored here.
   */
  public static final String ADDRESS_SPACES_TABLE = "bn_address_spaces";

  /**
   * Membership of modules in address spaces is stored here.
   */
  public static final String SPACE_MODULES_TABLE = "bn_space_modules";

  /**
   * Function information is stored here.
   */
  public static final String FUNCTIONS_TABLE = "bn_functions";

  /**
   * Maps between native views and their backing functions.
   */
  public static final String FUNCTION_VIEWS_TABLE = "bn_function_views";

  /**
   * Information about instructions is stored here.
   */
  public static final String INSTRUCTIONS_TABLE = "bn_instructions";

  /**
   * Information about operands is stored here.
   */
  public static final String OPERANDS_TABLE = "bn_operands";

  /**
   * Information about expression trees is stored here.
   */
  public static final String EXPRESSION_TREE_TABLE = "bn_expression_tree";

  /**
   * Contains the IDs of known expression trees.
   */
  public static final String EXPRESSION_TREE_IDS_TABLE = "bn_expression_tree_ids";

  /**
   * Maps between operands and expression trees.
   */
  public static final String EXPRESSION_TREE_MAPPING_TABLE = "bn_expression_tree_mapping";

  /**
   * Information about code nodes is stored here.
   */
  public static final String CODE_NODES_TABLE = "bn_code_nodes";

  /**
   * Maps instructions to the code nodes they belong to.
   */
  public static final String CODENODE_INSTRUCTIONS_TABLE = "bn_codenode_instructions";

  /**
   * Information about edges is stored here.
   */
  public static final String EDGES_TABLE = "bn_edges";

  /**
   * Information about edge paths is stored here.
   */
  public static final String EDGE_PATHS_TABLE = "bn_edge_paths";

  /**
   * Information about function nodes is stored here.
   */
  public static final String FUNCTION_NODES_TABLE = "bn_function_nodes";

  /**
   * Information about group nodes is stored here.
   */
  public static final String GROUP_NODES_TABLE = "bn_group_nodes";

  /**
   * Common node information is stored here.
   */
  public static final String NODES_TABLE = "bn_nodes";

  /**
   * Project settings are stored here.
   */
  public static final String PROJECT_SETTINGS_TABLE = "bn_project_settings";

  /**
   * Module settings are stored here.
   */
  public static final String MODULE_SETTINGS_TABLE = "bn_module_settings";

  /**
   * Recorded debug events are stored here.
   */
  public static final String TRACES_TABLE = "bn_traces";

  /**
   * Debug events of recorded debug traces are stored here.
   */
  public static final String TRACE_EVENT_TABLE = "bn_trace_events";

  /**
   * Register values of debug events of recorded debug traces are stored here.
   */
  public static final String TRACE_EVENT_VALUES_TABLE = "bn_trace_event_values";

  /**
   * Information about views is stored here.
   */
  public static final String VIEWS_TABLE = "bn_views";

  /**
   * Maps views to the modules they belong to.
   */
  public static final String MODULE_VIEWS_TABLE = "bn_module_views";

  /**
   * Maps views to the projects they belong to.
   */
  public static final String PROJECT_VIEWS_TABLE = "bn_project_views";

  /**
   * View settings are stored here.
   */
  public static final String VIEW_SETTINGS_TABLE = "bn_view_settings";

  /**
   * Global edge comments are stored here.
   */
  public static final String GLOBAL_EDGE_COMMENTS_TABLE = "bn_global_edge_comments";

  /**
   * Global node comments are stored here.
   */
  public static final String GLOBAL_NODE_COMMENTS_TABLE = "bn_global_node_comments";

  /**
   * Debuggers assigned to a project are stored here.
   */
  public static final String PROJECT_DEBUGGERS_TABLE = "bn_project_debuggers";

  /**
   * Information about debuggers is stored here.
   */
  public static final String DEBUGGERS_TABLE = "bn_debuggers";

  /**
   * Information about tags is stored here.
   */
  public static final String TAGS_TABLE = "bn_tags";

  /**
   * Information about tagged views is stored here.
   */
  public static final String TAGGED_VIEWS_TABLE = "bn_tagged_views";

  /**
   * Information about tagged nodes is stored here.
   */
  public static final String TAGGED_NODES_TABLE = "bn_tagged_nodes";

  /**
   * Expressions substitutions for operands is stored here.
   */
  public static final String EXPRESSION_SUBSTITUTIONS_TABLE = "bn_expression_substitutions";

  /**
   * Information about address references is stored here.
   */
  public static final String ADDRESS_REFERENCES_TABLE = "bn_address_references";

  /**
   * Information about raw modules is stored here.
   */
  public static final String RAW_MODULES_TABLE = "modules";

  /**
   * Raw exporter information about address comments is stored here.
   */
  public static final String RAW_ADDRESS_COMMENTS_TABLE = "ex_%d_address_comments";

  /**
   * Raw exporter information about address references is stored here.
   */
  public static final String RAW_ADDRESS_REFERENCES_TABLE = "ex_%d_address_references";

  /**
   * Raw exporter information about basic blocks is stored here.
   */
  public static final String RAW_BASIC_BLOCKS_TABLE = "ex_%d_basic_blocks";

  /**
   * Raw exporter information about basic block instructions is stored here.
   */
  public static final String RAW_BASIC_BLOCK_INSTRUCTIONS_TABLE = "ex_%d_basic_block_instructions";

  /**
   * Raw exporter information about the Call graph is stored here.
   */
  public static final String RAW_CALLGRAPH_TABLE = "ex_%d_callgraph";

  /**
   * Raw exporter information about control flow graphs is stored here.
   */
  public static final String RAW_CONTROL_FLOW_GRAPHS_TABLE = "ex_%d_control_flow_graphs";

  /**
   * Raw exporter information about expression nodes is stored here.
   */
  public static final String RAW_EXPRESSION_NODES_TABLE = "ex_%d_expression_nodes";

  /**
   * Raw exporter information about expression substitutions are stored here.
   */
  public static final String RAW_EXPRESSION_SUBSTITUTIONS_TABLE = "ex_%d_expression_substitutions";

  /**
   * Raw exporter information about expression trees is stored here.
   */
  public static final String RAW_EXPRESSION_TREES_TABLE = "ex_%d_expression_trees";

  /**
   * Raw exporter information about expression tree nodes is stored here.
   */
  public static final String RAW_EXPRESSION_TREE_NODES_TABLE = "ex_%d_expression_tree_nodes";

  /**
   * Raw exporter information about functions is stored here.
   */
  public static final String RAW_FUNCTIONS_TABLE = "ex_%d_functions";

  /**
   * Raw exporter information about instructions is stored here.
   */
  public static final String RAW_INSTRUCTIONS_TABLE = "ex_%d_instructions";

  /**
   * Raw exporter information about operands is stored here.
   */
  public static final String RAW_OPERANDS_TABLE = "ex_%d_operands";

  /**
   * Raw exporter information about sections are stored here.
   */
  public static final String RAW_SECTIONS = "ex_%d_sections";

  /**
   * Raw exporter information about expression tree type instance connections is stored here.
   */
  public static final String RAW_EXPRESSION_TYPE_INSTANCES = "ex_%d_expression_type_instances";

  /**
   * Raw exporter type instance information is stored here.
   */
  public static final String RAW_TYPE_INSTACES = "ex_%d_type_instances";

  /**
   * Raw exporter information about types is stored here.
   */
  public static final String RAW_TYPES = "ex_%d_types";

  /**
   * Raw exporter information about all base types.
   */
  public static final String RAW_BASE_TYPES = "ex_%d_base_types";

  /**
   * Raw exporter information about the mapping of expression ids to types.
   */
  public static final String RAW_EXPRESSION_TYPES_TABLE = "ex_%d_expression_types";

  /**
   * You are not supposed to instantiate this class.
   */
  private CTableNames() {
  }
}
