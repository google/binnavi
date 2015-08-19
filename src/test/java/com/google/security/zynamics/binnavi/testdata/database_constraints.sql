
--
-- Name: bn_address_references_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_address_references
    ADD CONSTRAINT bn_address_references_pkey PRIMARY KEY (module_id, address, "position", expression_id, type, target);


--
-- Name: bn_address_spaces_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_address_spaces
    ADD CONSTRAINT bn_address_spaces_pkey PRIMARY KEY (id);


--
-- Name: bn_base_types_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_base_types
    ADD CONSTRAINT bn_base_types_pkey PRIMARY KEY (module_id, id);


--
-- Name: bn_code_nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_code_nodes
    ADD CONSTRAINT bn_code_nodes_pkey PRIMARY KEY (node_id);


--
-- Name: bn_codenode_instructions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_codenode_instructions
    ADD CONSTRAINT bn_codenode_instructions_pkey PRIMARY KEY (node_id, "position");


--
-- Name: bn_comments_audit_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_comments_audit
    ADD CONSTRAINT bn_comments_audit_pkey PRIMARY KEY (operation, time_stamp, id);


--
-- Name: bn_comments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_comments
    ADD CONSTRAINT bn_comments_pkey PRIMARY KEY (id);


--
-- Name: bn_data_parts_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_data_parts
    ADD CONSTRAINT bn_data_parts_pkey PRIMARY KEY (module_id, part_id);


--
-- Name: bn_debuggers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_debuggers
    ADD CONSTRAINT bn_debuggers_pkey PRIMARY KEY (id);


--
-- Name: bn_edge_paths_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_edge_paths
    ADD CONSTRAINT bn_edge_paths_pkey PRIMARY KEY (edge_id, "position");


--
-- Name: bn_edges_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_edges
    ADD CONSTRAINT bn_edges_pkey PRIMARY KEY (id);


--
-- Name: bn_expression_substitutions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_expression_substitutions
    ADD CONSTRAINT bn_expression_substitutions_pkey PRIMARY KEY (module_id, address, "position", expression_id);


--
-- Name: bn_expression_tree_ids_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_expression_tree_ids
    ADD CONSTRAINT bn_expression_tree_ids_pkey PRIMARY KEY (module_id, id);


--
-- Name: bn_expression_tree_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_expression_tree_mapping
    ADD CONSTRAINT bn_expression_tree_mapping_pkey PRIMARY KEY (module_id, tree_id, tree_node_id);


--
-- Name: bn_expression_tree_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_expression_tree
    ADD CONSTRAINT bn_expression_tree_pkey PRIMARY KEY (module_id, id);


--
-- Name: bn_expression_type_instances_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_expression_type_instances
    ADD CONSTRAINT bn_expression_type_instances_pkey PRIMARY KEY (module_id, address, "position", expression_id);


--
-- Name: bn_expression_types_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_expression_types
    ADD CONSTRAINT bn_expression_types_pkey PRIMARY KEY (module_id, address, "position", expression_id);


--
-- Name: bn_function_nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_function_nodes
    ADD CONSTRAINT bn_function_nodes_pkey PRIMARY KEY (node_id);


--
-- Name: bn_function_views_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_function_views
    ADD CONSTRAINT bn_function_views_pkey PRIMARY KEY (view_id);


--
-- Name: bn_functions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_functions
    ADD CONSTRAINT bn_functions_pkey PRIMARY KEY (module_id, address);


--
-- Name: bn_global_edge_comments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_global_edge_comments
    ADD CONSTRAINT bn_global_edge_comments_pkey PRIMARY KEY (src_module_id, dst_module_id, src_address, dst_address);


--
-- Name: bn_global_node_comments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_global_node_comments
    ADD CONSTRAINT bn_global_node_comments_pkey PRIMARY KEY (module_id, address);


--
-- Name: bn_group_nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_group_nodes
    ADD CONSTRAINT bn_group_nodes_pkey PRIMARY KEY (node_id);


--
-- Name: bn_instructions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_instructions
    ADD CONSTRAINT bn_instructions_pkey PRIMARY KEY (module_id, address);


--
-- Name: bn_module_settings_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_module_settings
    ADD CONSTRAINT bn_module_settings_pkey PRIMARY KEY (module_id, name);


--
-- Name: bn_module_traces_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_module_traces
    ADD CONSTRAINT bn_module_traces_pkey PRIMARY KEY (module_id, trace_id);


--
-- Name: bn_module_views_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_module_views
    ADD CONSTRAINT bn_module_views_pkey PRIMARY KEY (view_id);


--
-- Name: bn_modules_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_modules
    ADD CONSTRAINT bn_modules_pkey PRIMARY KEY (id);


--
-- Name: bn_nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_nodes
    ADD CONSTRAINT bn_nodes_pkey PRIMARY KEY (id);


--
-- Name: bn_nodes_spacemodules_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_nodes_spacemodules
    ADD CONSTRAINT bn_nodes_spacemodules_pkey PRIMARY KEY (node);


--
-- Name: bn_operands_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_operands
    ADD CONSTRAINT bn_operands_pkey PRIMARY KEY (module_id, address, "position");


--
-- Name: bn_project_settings_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_project_settings
    ADD CONSTRAINT bn_project_settings_pkey PRIMARY KEY (project_id, name);


--
-- Name: bn_project_traces_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_project_traces
    ADD CONSTRAINT bn_project_traces_pkey PRIMARY KEY (project_id, trace_id);


--
-- Name: bn_project_views_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_project_views
    ADD CONSTRAINT bn_project_views_pkey PRIMARY KEY (view_id);


--
-- Name: bn_projects_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_projects
    ADD CONSTRAINT bn_projects_pkey PRIMARY KEY (id);


--
-- Name: bn_sections_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_sections
    ADD CONSTRAINT bn_sections_pkey PRIMARY KEY (module_id, id);


--
-- Name: bn_space_modules_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_space_modules
    ADD CONSTRAINT bn_space_modules_pkey PRIMARY KEY (address_space_id, module_id);


--
-- Name: bn_tagged_nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_tagged_nodes
    ADD CONSTRAINT bn_tagged_nodes_pkey PRIMARY KEY (node_id, tag_id);


--
-- Name: bn_tagged_views_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_tagged_views
    ADD CONSTRAINT bn_tagged_views_pkey PRIMARY KEY (view_id, tag_id);


--
-- Name: bn_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_tags
    ADD CONSTRAINT bn_tags_pkey PRIMARY KEY (id);


--
-- Name: bn_text_nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_text_nodes
    ADD CONSTRAINT bn_text_nodes_pkey PRIMARY KEY (node_id);


--
-- Name: bn_trace_event_values_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_trace_event_values
    ADD CONSTRAINT bn_trace_event_values_pkey PRIMARY KEY (trace_id, "position", register_name);


--
-- Name: bn_trace_events_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_trace_events
    ADD CONSTRAINT bn_trace_events_pkey PRIMARY KEY (trace_id, "position");


--
-- Name: bn_traces_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_traces
    ADD CONSTRAINT bn_traces_pkey PRIMARY KEY (id);


--
-- Name: bn_type_instances_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_type_instances
    ADD CONSTRAINT bn_type_instances_pkey PRIMARY KEY (module_id, id);


--
-- Name: bn_types_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_types
    ADD CONSTRAINT bn_types_pkey PRIMARY KEY (module_id, id);


--
-- Name: bn_users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_users
    ADD CONSTRAINT bn_users_pkey PRIMARY KEY (user_id);


--
-- Name: bn_view_settings_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_view_settings
    ADD CONSTRAINT bn_view_settings_pkey PRIMARY KEY (view_id, name);


--
-- Name: bn_views_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY bn_views
    ADD CONSTRAINT bn_views_pkey PRIMARY KEY (id);


--
-- Name: ex_1_base_types_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_1_base_types
    ADD CONSTRAINT ex_1_base_types_pkey PRIMARY KEY (id);


--
-- Name: ex_1_basic_blocks_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_1_basic_blocks
    ADD CONSTRAINT ex_1_basic_blocks_pkey PRIMARY KEY (id);


--
-- Name: ex_1_callgraph_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_1_callgraph
    ADD CONSTRAINT ex_1_callgraph_pkey PRIMARY KEY (id);


--
-- Name: ex_1_control_flow_graphs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_1_control_flow_graphs
    ADD CONSTRAINT ex_1_control_flow_graphs_pkey PRIMARY KEY (id);


--
-- Name: ex_1_expression_nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_1_expression_nodes
    ADD CONSTRAINT ex_1_expression_nodes_pkey PRIMARY KEY (id);


--
-- Name: ex_1_expression_trees_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_1_expression_trees
    ADD CONSTRAINT ex_1_expression_trees_pkey PRIMARY KEY (id);


--
-- Name: ex_1_expression_type_instances_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_1_expression_type_instances
    ADD CONSTRAINT ex_1_expression_type_instances_pkey PRIMARY KEY (address, "position", expression_node_id);


--
-- Name: ex_1_expression_types_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_1_expression_types
    ADD CONSTRAINT ex_1_expression_types_pkey PRIMARY KEY (address, "position", expression_id);


--
-- Name: ex_1_functions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_1_functions
    ADD CONSTRAINT ex_1_functions_pkey PRIMARY KEY (address);


--
-- Name: ex_1_instructions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_1_instructions
    ADD CONSTRAINT ex_1_instructions_pkey PRIMARY KEY (address);


--
-- Name: ex_1_operands_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_1_operands
    ADD CONSTRAINT ex_1_operands_pkey PRIMARY KEY (address, "position");


--
-- Name: ex_1_sections_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_1_sections
    ADD CONSTRAINT ex_1_sections_pkey PRIMARY KEY (id);


--
-- Name: ex_1_type_instances_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_1_type_instances
    ADD CONSTRAINT ex_1_type_instances_pkey PRIMARY KEY (id);


--
-- Name: ex_1_types_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_1_types
    ADD CONSTRAINT ex_1_types_pkey PRIMARY KEY (id);


--
-- Name: ex_2_base_types_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_2_base_types
    ADD CONSTRAINT ex_2_base_types_pkey PRIMARY KEY (id);


--
-- Name: ex_2_basic_blocks_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_2_basic_blocks
    ADD CONSTRAINT ex_2_basic_blocks_pkey PRIMARY KEY (id);


--
-- Name: ex_2_callgraph_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_2_callgraph
    ADD CONSTRAINT ex_2_callgraph_pkey PRIMARY KEY (id);


--
-- Name: ex_2_control_flow_graphs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_2_control_flow_graphs
    ADD CONSTRAINT ex_2_control_flow_graphs_pkey PRIMARY KEY (id);


--
-- Name: ex_2_expression_nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_2_expression_nodes
    ADD CONSTRAINT ex_2_expression_nodes_pkey PRIMARY KEY (id);


--
-- Name: ex_2_expression_trees_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_2_expression_trees
    ADD CONSTRAINT ex_2_expression_trees_pkey PRIMARY KEY (id);


--
-- Name: ex_2_expression_type_instances_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_2_expression_type_instances
    ADD CONSTRAINT ex_2_expression_type_instances_pkey PRIMARY KEY (address, "position", expression_node_id);


--
-- Name: ex_2_expression_types_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_2_expression_types
    ADD CONSTRAINT ex_2_expression_types_pkey PRIMARY KEY (address, "position", expression_id);


--
-- Name: ex_2_functions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_2_functions
    ADD CONSTRAINT ex_2_functions_pkey PRIMARY KEY (address);


--
-- Name: ex_2_instructions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_2_instructions
    ADD CONSTRAINT ex_2_instructions_pkey PRIMARY KEY (address);


--
-- Name: ex_2_operands_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_2_operands
    ADD CONSTRAINT ex_2_operands_pkey PRIMARY KEY (address, "position");


--
-- Name: ex_2_sections_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_2_sections
    ADD CONSTRAINT ex_2_sections_pkey PRIMARY KEY (id);


--
-- Name: ex_2_type_instances_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_2_type_instances
    ADD CONSTRAINT ex_2_type_instances_pkey PRIMARY KEY (id);


--
-- Name: ex_2_types_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ex_2_types
    ADD CONSTRAINT ex_2_types_pkey PRIMARY KEY (id);


--
-- Name: modules_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY modules
    ADD CONSTRAINT modules_pkey PRIMARY KEY (id);


--
-- Name: bn_address_references_module_id_address_position_expression_id_; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_address_references_module_id_address_position_expression_id_ ON bn_address_references USING btree (module_id, address, "position", expression_id);


--
-- Name: bn_address_references_module_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_address_references_module_id_idx ON bn_address_references USING btree (module_id);


--
-- Name: bn_address_references_target_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_address_references_target_idx ON bn_address_references USING btree (target);


--
-- Name: bn_address_references_type_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_address_references_type_idx ON bn_address_references USING btree (type);


--
-- Name: bn_code_nodes_comment_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_code_nodes_comment_id_idx ON bn_code_nodes USING btree (comment_id);


--
-- Name: bn_code_nodes_module_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_code_nodes_module_id_idx ON bn_code_nodes USING btree (module_id);


--
-- Name: bn_codenode_instructions_address_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_codenode_instructions_address_idx ON bn_codenode_instructions USING btree (address);


--
-- Name: bn_codenode_instructions_comment_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_codenode_instructions_comment_id_idx ON bn_codenode_instructions USING btree (comment_id);


--
-- Name: bn_codenode_instructions_module_id_address_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_codenode_instructions_module_id_address_idx ON bn_codenode_instructions USING btree (module_id, address);


--
-- Name: bn_comments_parent_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_comments_parent_id_idx ON bn_comments USING btree (parent_id);


--
-- Name: bn_comments_user_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_comments_user_id_idx ON bn_comments USING btree (user_id);


--
-- Name: bn_edges_comment_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_edges_comment_id_idx ON bn_edges USING btree (comment_id);


--
-- Name: bn_edges_source_node_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_edges_source_node_id_idx ON bn_edges USING btree (source_node_id);


--
-- Name: bn_edges_target_node_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_edges_target_node_id_idx ON bn_edges USING btree (target_node_id);


--
-- Name: bn_expression_substitutions_module_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_expression_substitutions_module_id_idx ON bn_expression_substitutions USING btree (module_id);


--
-- Name: bn_expression_tree_ids_module_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_expression_tree_ids_module_id_idx ON bn_expression_tree_ids USING btree (module_id);


--
-- Name: bn_expression_tree_mapping_module_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_expression_tree_mapping_module_id_idx ON bn_expression_tree_mapping USING btree (module_id);


--
-- Name: bn_function_nodes_comment_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_function_nodes_comment_id_idx ON bn_function_nodes USING btree (comment_id);


--
-- Name: bn_function_nodes_function_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_function_nodes_function_idx ON bn_function_nodes USING btree (function);


--
-- Name: bn_function_nodes_module_id_function_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_function_nodes_module_id_function_idx ON bn_function_nodes USING btree (module_id, function);


--
-- Name: bn_function_nodes_module_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_function_nodes_module_id_idx ON bn_function_nodes USING btree (module_id);


--
-- Name: bn_function_views_module_id_function_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_function_views_module_id_function_idx ON bn_function_views USING btree (module_id, function);


--
-- Name: bn_function_views_module_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_function_views_module_id_idx ON bn_function_views USING btree (module_id);


--
-- Name: bn_functions_address_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_functions_address_idx ON bn_functions USING btree (address);


--
-- Name: bn_functions_comment_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_functions_comment_id_idx ON bn_functions USING btree (comment_id);


--
-- Name: bn_functions_module_id_address_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_functions_module_id_address_idx ON bn_functions USING btree (module_id, address);


--
-- Name: bn_functions_module_id_address_type_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_functions_module_id_address_type_idx ON bn_functions USING btree (module_id, address, type);


--
-- Name: bn_functions_module_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_functions_module_id_idx ON bn_functions USING btree (module_id);


--
-- Name: bn_functions_parent_module_id_parent_module_function_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_functions_parent_module_id_parent_module_function_idx ON bn_functions USING btree (parent_module_id, parent_module_function);


--
-- Name: bn_global_edge_comments_comment_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_global_edge_comments_comment_id_idx ON bn_global_edge_comments USING btree (comment_id);


--
-- Name: bn_global_edge_comments_dst_module_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_global_edge_comments_dst_module_id_idx ON bn_global_edge_comments USING btree (dst_module_id);


--
-- Name: bn_global_edge_comments_src_module_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_global_edge_comments_src_module_id_idx ON bn_global_edge_comments USING btree (src_module_id);


--
-- Name: bn_global_node_comments_comment_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_global_node_comments_comment_id_idx ON bn_global_node_comments USING btree (comment_id);


--
-- Name: bn_group_nodes_comments_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_group_nodes_comments_id_idx ON bn_group_nodes USING btree (comment_id);


--
-- Name: bn_instructions_comment_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_instructions_comment_id_idx ON bn_instructions USING btree (comment_id);


--
-- Name: bn_module_views_module_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_module_views_module_id_idx ON bn_module_views USING btree (module_id);


--
-- Name: bn_module_views_module_id_view_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_module_views_module_id_view_id_idx ON bn_module_views USING btree (module_id, view_id);


--
-- Name: bn_modules_raw_module_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_modules_raw_module_id_idx ON bn_modules USING btree (raw_module_id);


--
-- Name: bn_nodes_spacemodules_address_space_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_nodes_spacemodules_address_space_idx ON bn_nodes_spacemodules USING btree (address_space);


--
-- Name: bn_nodes_type; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_nodes_type ON bn_nodes USING btree (type);


--
-- Name: bn_nodes_view_id; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_nodes_view_id ON bn_nodes USING btree (view_id);


--
-- Name: bn_nodes_view_id_type_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_nodes_view_id_type_idx ON bn_nodes USING btree (view_id, type);


--
-- Name: bn_operands_module_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_operands_module_id_idx ON bn_operands USING btree (module_id);


--
-- Name: bn_project_debuggers_debugger_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_project_debuggers_debugger_id_idx ON bn_project_debuggers USING btree (debugger_id);


--
-- Name: bn_project_traces_trace_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_project_traces_trace_id_idx ON bn_project_traces USING btree (trace_id);


--
-- Name: bn_project_views_project_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_project_views_project_id_idx ON bn_project_views USING btree (project_id);


--
-- Name: bn_sections_comment_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_sections_comment_id_idx ON bn_sections USING btree (comment_id);


--
-- Name: bn_tagged_views_tag_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_tagged_views_tag_id_idx ON bn_tagged_views USING btree (tag_id);


--
-- Name: bn_tags_parent_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_tags_parent_id_idx ON bn_tags USING btree (parent_id);


--
-- Name: bn_text_nodes_comment_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_text_nodes_comment_id_idx ON bn_text_nodes USING btree (comment_id);


--
-- Name: bn_trace_event_values_trace_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_trace_event_values_trace_id_idx ON bn_trace_event_values USING btree (trace_id);


--
-- Name: bn_trace_events_module_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_trace_events_module_id_idx ON bn_trace_events USING btree (module_id);


--
-- Name: bn_traces_view_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_traces_view_id_idx ON bn_traces USING btree (view_id);


--
-- Name: bn_type_instances_comment_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_type_instances_comment_id_idx ON bn_type_instances USING btree (comment_id);


--
-- Name: bn_views_type_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_views_type_idx ON bn_views USING btree (type);


--
-- Name: bn_views_user_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bn_views_user_id_idx ON bn_views USING btree (user_id);


--
-- Name: ex_1_basic_blocks_address_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX ex_1_basic_blocks_address_idx ON ex_1_basic_blocks USING btree (address);


--
-- Name: ex_1_basic_blocks_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX ex_1_basic_blocks_id_idx ON ex_1_basic_blocks USING btree (id);


--
-- Name: ex_1_expression_nodes_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX ex_1_expression_nodes_id_idx ON ex_1_expression_nodes USING btree (id);


--
-- Name: ex_1_expression_trees_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX ex_1_expression_trees_id_idx ON ex_1_expression_trees USING btree (id);


--
-- Name: ex_1_functions_address_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX ex_1_functions_address_idx ON ex_1_functions USING btree (address);


--
-- Name: ex_1_instructions_address_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX ex_1_instructions_address_idx ON ex_1_instructions USING btree (address);


--
-- Name: ex_2_basic_blocks_address_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX ex_2_basic_blocks_address_idx ON ex_2_basic_blocks USING btree (address);


--
-- Name: ex_2_basic_blocks_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX ex_2_basic_blocks_id_idx ON ex_2_basic_blocks USING btree (id);


--
-- Name: ex_2_expression_nodes_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX ex_2_expression_nodes_id_idx ON ex_2_expression_nodes USING btree (id);


--
-- Name: ex_2_expression_trees_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX ex_2_expression_trees_id_idx ON ex_2_expression_trees USING btree (id);


--
-- Name: ex_2_functions_address_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX ex_2_functions_address_idx ON ex_2_functions USING btree (address);


--
-- Name: ex_2_instructions_address_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX ex_2_instructions_address_idx ON ex_2_instructions USING btree (address);


--
-- Name: bn_base_types_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER bn_base_types_trigger AFTER INSERT OR DELETE OR UPDATE ON bn_base_types FOR EACH ROW EXECUTE PROCEDURE bn_base_types_trigger();


--
-- Name: bn_code_node_comment_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER bn_code_node_comment_trigger AFTER UPDATE OF comment_id ON bn_code_nodes FOR EACH ROW EXECUTE PROCEDURE bn_code_node_comment_trigger();


--
-- Name: bn_codenode_instructions_comment_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER bn_codenode_instructions_comment_trigger AFTER UPDATE OF comment_id ON bn_codenode_instructions FOR EACH ROW EXECUTE PROCEDURE bn_codenode_instructions_comment_trigger();


--
-- Name: bn_comments_audit_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER bn_comments_audit_trigger AFTER INSERT OR DELETE OR UPDATE ON bn_comments FOR EACH ROW EXECUTE PROCEDURE bn_comments_audit_logger();


--
-- Name: bn_comments_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER bn_comments_trigger AFTER DELETE OR UPDATE ON bn_comments FOR EACH ROW EXECUTE PROCEDURE bn_comments_trigger();


--
-- Name: bn_edges_comment_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER bn_edges_comment_trigger AFTER UPDATE OF comment_id ON bn_edges FOR EACH ROW EXECUTE PROCEDURE bn_edges_comment_trigger();


--
-- Name: bn_expression_type_instances_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER bn_expression_type_instances_trigger AFTER INSERT OR DELETE OR UPDATE ON bn_expression_type_instances FOR EACH ROW EXECUTE PROCEDURE bn_expression_type_instances_trigger();


--
-- Name: bn_expression_types_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER bn_expression_types_trigger AFTER INSERT OR DELETE OR UPDATE ON bn_expression_types FOR EACH ROW EXECUTE PROCEDURE bn_expression_types_trigger();


--
-- Name: bn_function_nodes_comment_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER bn_function_nodes_comment_trigger AFTER UPDATE OF comment_id ON bn_function_nodes FOR EACH ROW EXECUTE PROCEDURE bn_function_nodes_comment_trigger();


--
-- Name: bn_functions_comment_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER bn_functions_comment_trigger AFTER UPDATE OF comment_id ON bn_functions FOR EACH ROW EXECUTE PROCEDURE bn_functions_comment_trigger();


--
-- Name: bn_global_edge_comments_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER bn_global_edge_comments_trigger AFTER INSERT OR DELETE OR UPDATE ON bn_global_edge_comments FOR EACH ROW EXECUTE PROCEDURE bn_global_edge_comments_trigger();


--
-- Name: bn_global_node_comments_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER bn_global_node_comments_trigger AFTER INSERT OR DELETE OR UPDATE ON bn_global_node_comments FOR EACH ROW EXECUTE PROCEDURE bn_global_node_comments_trigger();


--
-- Name: bn_group_nodes_comment_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER bn_group_nodes_comment_trigger AFTER INSERT OR DELETE OR UPDATE ON bn_group_nodes FOR EACH ROW EXECUTE PROCEDURE bn_group_nodes_comment_trigger();


--
-- Name: bn_instructions_comment_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER bn_instructions_comment_trigger AFTER UPDATE OF comment_id ON bn_instructions FOR EACH ROW EXECUTE PROCEDURE bn_instructions_comment_trigger();


--
-- Name: bn_module_views_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER bn_module_views_trigger AFTER INSERT OR DELETE OR UPDATE ON bn_module_views FOR EACH ROW EXECUTE PROCEDURE bn_module_views_trigger();


--
-- Name: bn_project_views_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER bn_project_views_trigger AFTER INSERT OR DELETE OR UPDATE ON bn_project_views FOR EACH ROW EXECUTE PROCEDURE bn_project_views_trigger();


--
-- Name: bn_sections_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER bn_sections_trigger AFTER INSERT OR DELETE OR UPDATE ON bn_sections FOR EACH ROW EXECUTE PROCEDURE bn_sections_trigger();


--
-- Name: bn_text_nodes_comment_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER bn_text_nodes_comment_trigger AFTER INSERT OR DELETE OR UPDATE ON bn_text_nodes FOR EACH ROW EXECUTE PROCEDURE bn_text_nodes_comment_trigger();


--
-- Name: bn_type_instances_comment_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER bn_type_instances_comment_trigger AFTER UPDATE OF comment_id ON bn_type_instances FOR EACH ROW EXECUTE PROCEDURE bn_type_instances_comment_trigger();


--
-- Name: bn_type_instances_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER bn_type_instances_trigger AFTER INSERT OR DELETE OR UPDATE ON bn_type_instances FOR EACH ROW EXECUTE PROCEDURE bn_type_instances_trigger();


--
-- Name: bn_types_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER bn_types_trigger AFTER INSERT OR DELETE OR UPDATE ON bn_types FOR EACH ROW EXECUTE PROCEDURE bn_types_trigger();


--
-- Name: bn_views_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER bn_views_trigger AFTER DELETE OR UPDATE ON bn_views FOR EACH ROW EXECUTE PROCEDURE bn_views_trigger();


--
-- Name: bn_address_references_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_address_references
    ADD CONSTRAINT bn_address_references_module_id_fkey FOREIGN KEY (module_id) REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_address_spaces_debugger_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_address_spaces
    ADD CONSTRAINT bn_address_spaces_debugger_id_fkey FOREIGN KEY (debugger_id) REFERENCES bn_debuggers(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_address_spaces_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_address_spaces
    ADD CONSTRAINT bn_address_spaces_project_id_fkey FOREIGN KEY (project_id) REFERENCES bn_projects(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_base_types_pointer_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_base_types
    ADD CONSTRAINT bn_base_types_pointer_fkey FOREIGN KEY (module_id, pointer) REFERENCES bn_base_types(module_id, id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_code_nodes_comment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_code_nodes
    ADD CONSTRAINT bn_code_nodes_comment_id_fkey FOREIGN KEY (comment_id) REFERENCES bn_comments(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_code_nodes_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_code_nodes
    ADD CONSTRAINT bn_code_nodes_module_id_fkey FOREIGN KEY (module_id) REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_code_nodes_node_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_code_nodes
    ADD CONSTRAINT bn_code_nodes_node_id_fkey FOREIGN KEY (node_id) REFERENCES bn_nodes(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_codenode_instructions_comment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_codenode_instructions
    ADD CONSTRAINT bn_codenode_instructions_comment_id_fkey FOREIGN KEY (comment_id) REFERENCES bn_comments(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_codenode_instructions_node_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_codenode_instructions
    ADD CONSTRAINT bn_codenode_instructions_node_id_fkey FOREIGN KEY (node_id) REFERENCES bn_nodes(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_comments_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_comments
    ADD CONSTRAINT bn_comments_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES bn_comments(id) ON DELETE SET NULL DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_comments_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_comments
    ADD CONSTRAINT bn_comments_user_id_fkey FOREIGN KEY (user_id) REFERENCES bn_users(user_id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_data_parts_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_data_parts
    ADD CONSTRAINT bn_data_parts_module_id_fkey FOREIGN KEY (module_id) REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_edge_paths_edge_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_edge_paths
    ADD CONSTRAINT bn_edge_paths_edge_id_fkey FOREIGN KEY (edge_id) REFERENCES bn_edges(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_edges_comment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_edges
    ADD CONSTRAINT bn_edges_comment_id_fkey FOREIGN KEY (comment_id) REFERENCES bn_comments(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_edges_source_node_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_edges
    ADD CONSTRAINT bn_edges_source_node_id_fkey FOREIGN KEY (source_node_id) REFERENCES bn_nodes(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_edges_target_node_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_edges
    ADD CONSTRAINT bn_edges_target_node_id_fkey FOREIGN KEY (target_node_id) REFERENCES bn_nodes(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_expression_substitutions_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_expression_substitutions
    ADD CONSTRAINT bn_expression_substitutions_module_id_fkey FOREIGN KEY (module_id) REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_expression_tree_ids_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_expression_tree_ids
    ADD CONSTRAINT bn_expression_tree_ids_module_id_fkey FOREIGN KEY (module_id) REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_expression_tree_mapping_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_expression_tree_mapping
    ADD CONSTRAINT bn_expression_tree_mapping_module_id_fkey FOREIGN KEY (module_id) REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_expression_tree_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_expression_tree
    ADD CONSTRAINT bn_expression_tree_module_id_fkey FOREIGN KEY (module_id) REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_expression_type_instances_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_expression_type_instances
    ADD CONSTRAINT bn_expression_type_instances_module_id_fkey FOREIGN KEY (module_id) REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_expression_type_instances_module_id_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_expression_type_instances
    ADD CONSTRAINT bn_expression_type_instances_module_id_type_id_fkey FOREIGN KEY (module_id, type_instance_id) REFERENCES bn_type_instances(module_id, id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_expression_types_module_id_member_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_expression_types
    ADD CONSTRAINT bn_expression_types_module_id_member_id_fkey FOREIGN KEY (module_id, base_type_id) REFERENCES bn_base_types(module_id, id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_function_nodes_comment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_function_nodes
    ADD CONSTRAINT bn_function_nodes_comment_id_fkey FOREIGN KEY (comment_id) REFERENCES bn_comments(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_function_nodes_node_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_function_nodes
    ADD CONSTRAINT bn_function_nodes_node_id_fkey FOREIGN KEY (node_id) REFERENCES bn_nodes(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_function_views_view_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_function_views
    ADD CONSTRAINT bn_function_views_view_id_fkey FOREIGN KEY (view_id) REFERENCES bn_views(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_functions_comment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_functions
    ADD CONSTRAINT bn_functions_comment_id_fkey FOREIGN KEY (comment_id) REFERENCES bn_comments(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_functions_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_functions
    ADD CONSTRAINT bn_functions_module_id_fkey FOREIGN KEY (module_id) REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_global_edge_comments_comment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_global_edge_comments
    ADD CONSTRAINT bn_global_edge_comments_comment_id_fkey FOREIGN KEY (comment_id) REFERENCES bn_comments(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_global_edge_comments_dst_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_global_edge_comments
    ADD CONSTRAINT bn_global_edge_comments_dst_module_id_fkey FOREIGN KEY (dst_module_id) REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_global_edge_comments_src_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_global_edge_comments
    ADD CONSTRAINT bn_global_edge_comments_src_module_id_fkey FOREIGN KEY (src_module_id) REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_global_node_comments_comment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_global_node_comments
    ADD CONSTRAINT bn_global_node_comments_comment_id_fkey FOREIGN KEY (comment_id) REFERENCES bn_comments(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_global_node_comments_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_global_node_comments
    ADD CONSTRAINT bn_global_node_comments_module_id_fkey FOREIGN KEY (module_id) REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_group_nodes_comment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_group_nodes
    ADD CONSTRAINT bn_group_nodes_comment_id_fkey FOREIGN KEY (comment_id) REFERENCES bn_comments(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_group_nodes_node_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_group_nodes
    ADD CONSTRAINT bn_group_nodes_node_id_fkey FOREIGN KEY (node_id) REFERENCES bn_nodes(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_instructions_comment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_instructions
    ADD CONSTRAINT bn_instructions_comment_id_fkey FOREIGN KEY (comment_id) REFERENCES bn_comments(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_instructions_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_instructions
    ADD CONSTRAINT bn_instructions_module_id_fkey FOREIGN KEY (module_id) REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_module_settings_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_module_settings
    ADD CONSTRAINT bn_module_settings_module_id_fkey FOREIGN KEY (module_id) REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_module_traces_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_module_traces
    ADD CONSTRAINT bn_module_traces_module_id_fkey FOREIGN KEY (module_id) REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_module_traces_trace_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_module_traces
    ADD CONSTRAINT bn_module_traces_trace_id_fkey FOREIGN KEY (trace_id) REFERENCES bn_traces(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_module_views_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_module_views
    ADD CONSTRAINT bn_module_views_module_id_fkey FOREIGN KEY (module_id) REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_module_views_view_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_module_views
    ADD CONSTRAINT bn_module_views_view_id_fkey FOREIGN KEY (view_id) REFERENCES bn_views(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_modules_debugger_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_modules
    ADD CONSTRAINT bn_modules_debugger_id_fkey FOREIGN KEY (debugger_id) REFERENCES bn_debuggers(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_nodes_spacemodules_address_space_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_nodes_spacemodules
    ADD CONSTRAINT bn_nodes_spacemodules_address_space_fkey FOREIGN KEY (address_space) REFERENCES bn_address_spaces(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_nodes_spacemodules_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_nodes_spacemodules
    ADD CONSTRAINT bn_nodes_spacemodules_module_id_fkey FOREIGN KEY (module_id) REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_nodes_spacemodules_node_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_nodes_spacemodules
    ADD CONSTRAINT bn_nodes_spacemodules_node_fkey FOREIGN KEY (node) REFERENCES bn_nodes(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_operands_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_operands
    ADD CONSTRAINT bn_operands_module_id_fkey FOREIGN KEY (module_id) REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_project_debuggers_debugger_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_project_debuggers
    ADD CONSTRAINT bn_project_debuggers_debugger_id_fkey FOREIGN KEY (debugger_id) REFERENCES bn_debuggers(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_project_debuggers_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_project_debuggers
    ADD CONSTRAINT bn_project_debuggers_project_id_fkey FOREIGN KEY (project_id) REFERENCES bn_projects(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_project_settings_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_project_settings
    ADD CONSTRAINT bn_project_settings_project_id_fkey FOREIGN KEY (project_id) REFERENCES bn_projects(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_project_traces_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_project_traces
    ADD CONSTRAINT bn_project_traces_project_id_fkey FOREIGN KEY (project_id) REFERENCES bn_projects(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_project_traces_trace_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_project_traces
    ADD CONSTRAINT bn_project_traces_trace_id_fkey FOREIGN KEY (trace_id) REFERENCES bn_traces(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_project_views_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_project_views
    ADD CONSTRAINT bn_project_views_project_id_fkey FOREIGN KEY (project_id) REFERENCES bn_projects(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_project_views_view_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_project_views
    ADD CONSTRAINT bn_project_views_view_id_fkey FOREIGN KEY (view_id) REFERENCES bn_views(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_sections_comment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_sections
    ADD CONSTRAINT bn_sections_comment_id_fkey FOREIGN KEY (comment_id) REFERENCES bn_comments(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_sections_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_sections
    ADD CONSTRAINT bn_sections_module_id_fkey FOREIGN KEY (module_id) REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_space_modules_address_space_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_space_modules
    ADD CONSTRAINT bn_space_modules_address_space_id_fkey FOREIGN KEY (address_space_id) REFERENCES bn_address_spaces(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_space_modules_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_space_modules
    ADD CONSTRAINT bn_space_modules_module_id_fkey FOREIGN KEY (module_id) REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_tagged_nodes_node_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_tagged_nodes
    ADD CONSTRAINT bn_tagged_nodes_node_id_fkey FOREIGN KEY (node_id) REFERENCES bn_nodes(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_tagged_nodes_tag_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_tagged_nodes
    ADD CONSTRAINT bn_tagged_nodes_tag_id_fkey FOREIGN KEY (tag_id) REFERENCES bn_tags(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_tagged_views_tag_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_tagged_views
    ADD CONSTRAINT bn_tagged_views_tag_id_fkey FOREIGN KEY (tag_id) REFERENCES bn_tags(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_tagged_views_view_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_tagged_views
    ADD CONSTRAINT bn_tagged_views_view_id_fkey FOREIGN KEY (view_id) REFERENCES bn_views(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_tags_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_tags
    ADD CONSTRAINT bn_tags_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES bn_tags(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_text_nodes_comment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_text_nodes
    ADD CONSTRAINT bn_text_nodes_comment_id_fkey FOREIGN KEY (comment_id) REFERENCES bn_comments(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_text_nodes_node_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_text_nodes
    ADD CONSTRAINT bn_text_nodes_node_id_fkey FOREIGN KEY (node_id) REFERENCES bn_nodes(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_trace_event_values_trace_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_trace_event_values
    ADD CONSTRAINT bn_trace_event_values_trace_id_fkey FOREIGN KEY (trace_id) REFERENCES bn_traces(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_trace_events_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_trace_events
    ADD CONSTRAINT bn_trace_events_module_id_fkey FOREIGN KEY (module_id) REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_trace_events_trace_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_trace_events
    ADD CONSTRAINT bn_trace_events_trace_id_fkey FOREIGN KEY (trace_id) REFERENCES bn_traces(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_type_instances_comment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_type_instances
    ADD CONSTRAINT bn_type_instances_comment_id_fkey FOREIGN KEY (comment_id) REFERENCES bn_comments(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_type_instances_module_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_type_instances
    ADD CONSTRAINT bn_type_instances_module_id_fkey FOREIGN KEY (module_id) REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_type_instances_module_id_section_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_type_instances
    ADD CONSTRAINT bn_type_instances_module_id_section_id_fkey FOREIGN KEY (module_id, section_id) REFERENCES bn_sections(module_id, id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_type_instances_module_id_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_type_instances
    ADD CONSTRAINT bn_type_instances_module_id_type_id_fkey FOREIGN KEY (module_id, type_id) REFERENCES bn_base_types(module_id, id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_types_base_type_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_types
    ADD CONSTRAINT bn_types_base_type_fkey FOREIGN KEY (module_id, base_type) REFERENCES bn_base_types(module_id, id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_types_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_types
    ADD CONSTRAINT bn_types_parent_id_fkey FOREIGN KEY (module_id, parent_id) REFERENCES bn_base_types(module_id, id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_view_settings_view_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_view_settings
    ADD CONSTRAINT bn_view_settings_view_id_fkey FOREIGN KEY (view_id) REFERENCES bn_views(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: bn_views_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_views
    ADD CONSTRAINT bn_views_user_id_fkey FOREIGN KEY (user_id) REFERENCES bn_users(user_id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: ex_1_address_references_address_position; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_address_references
    ADD CONSTRAINT ex_1_address_references_address_position FOREIGN KEY (address, "position") REFERENCES ex_1_operands(address, "position") ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_address_references_expression_node_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_address_references
    ADD CONSTRAINT ex_1_address_references_expression_node_id_fkey FOREIGN KEY (expression_node_id) REFERENCES ex_1_expression_nodes(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_base_types_pointer_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_base_types
    ADD CONSTRAINT ex_1_base_types_pointer_fkey FOREIGN KEY (pointer) REFERENCES ex_1_base_types(id) ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: ex_1_basic_block_instructions_bb_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_basic_block_instructions
    ADD CONSTRAINT ex_1_basic_block_instructions_bb_fkey FOREIGN KEY (basic_block_id) REFERENCES ex_1_basic_blocks(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_basic_block_instructions_ins_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_basic_block_instructions
    ADD CONSTRAINT ex_1_basic_block_instructions_ins_fkey FOREIGN KEY (instruction) REFERENCES ex_1_instructions(address) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_basic_blocks_parent_function_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_basic_blocks
    ADD CONSTRAINT ex_1_basic_blocks_parent_function_fkey FOREIGN KEY (parent_function) REFERENCES ex_1_functions(address) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_callgraph_destination_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_callgraph
    ADD CONSTRAINT ex_1_callgraph_destination_fkey FOREIGN KEY (destination) REFERENCES ex_1_functions(address) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_callgraph_source_address_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_callgraph
    ADD CONSTRAINT ex_1_callgraph_source_address_fkey FOREIGN KEY (source_address) REFERENCES ex_1_instructions(address) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_callgraph_source_basic_block_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_callgraph
    ADD CONSTRAINT ex_1_callgraph_source_basic_block_id_fkey FOREIGN KEY (source_basic_block_id) REFERENCES ex_1_basic_blocks(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_callgraph_source_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_callgraph
    ADD CONSTRAINT ex_1_callgraph_source_fkey FOREIGN KEY (source) REFERENCES ex_1_functions(address) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_control_flow_graphs_destination_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_control_flow_graphs
    ADD CONSTRAINT ex_1_control_flow_graphs_destination_fkey FOREIGN KEY (destination) REFERENCES ex_1_basic_blocks(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_control_flow_graphs_parent_function_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_control_flow_graphs
    ADD CONSTRAINT ex_1_control_flow_graphs_parent_function_fkey FOREIGN KEY (parent_function) REFERENCES ex_1_functions(address) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_control_flow_graphs_source_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_control_flow_graphs
    ADD CONSTRAINT ex_1_control_flow_graphs_source_fkey FOREIGN KEY (source) REFERENCES ex_1_basic_blocks(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_expression_nodes_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_expression_nodes
    ADD CONSTRAINT ex_1_expression_nodes_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES ex_1_expression_nodes(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_expression_substitutions_address_position_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_expression_substitutions
    ADD CONSTRAINT ex_1_expression_substitutions_address_position_fkey FOREIGN KEY (address, "position") REFERENCES ex_1_operands(address, "position") ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_expression_substitutions_expression_node_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_expression_substitutions
    ADD CONSTRAINT ex_1_expression_substitutions_expression_node_id_fkey FOREIGN KEY (expression_node_id) REFERENCES ex_1_expression_nodes(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_expression_tree_nodes_expression_node_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_expression_tree_nodes
    ADD CONSTRAINT ex_1_expression_tree_nodes_expression_node_id_fkey FOREIGN KEY (expression_node_id) REFERENCES ex_1_expression_nodes(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_expression_tree_nodes_expression_tree_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_expression_tree_nodes
    ADD CONSTRAINT ex_1_expression_tree_nodes_expression_tree_id_fkey FOREIGN KEY (expression_tree_id) REFERENCES ex_1_expression_trees(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_expression_type_instances_address_position_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_expression_type_instances
    ADD CONSTRAINT ex_1_expression_type_instances_address_position_fkey FOREIGN KEY (address, "position") REFERENCES ex_1_operands(address, "position") ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_expression_type_instances_expression_node_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_expression_type_instances
    ADD CONSTRAINT ex_1_expression_type_instances_expression_node_id_fkey FOREIGN KEY (expression_node_id) REFERENCES ex_1_expression_nodes(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_expression_type_instances_type_instance_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_expression_type_instances
    ADD CONSTRAINT ex_1_expression_type_instances_type_instance_id_fkey FOREIGN KEY (type_instance_id) REFERENCES ex_1_type_instances(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_expression_type_type_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_expression_types
    ADD CONSTRAINT ex_1_expression_type_type_fkey FOREIGN KEY (type) REFERENCES ex_1_base_types(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: ex_1_operands_address_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_operands
    ADD CONSTRAINT ex_1_operands_address_fkey FOREIGN KEY (address) REFERENCES ex_1_instructions(address) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_operands_expression_tree_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_operands
    ADD CONSTRAINT ex_1_operands_expression_tree_id_fkey FOREIGN KEY (expression_tree_id) REFERENCES ex_1_expression_trees(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_type_instances_section_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_type_instances
    ADD CONSTRAINT ex_1_type_instances_section_id_fkey FOREIGN KEY (section_id) REFERENCES ex_1_sections(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_type_instances_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_type_instances
    ADD CONSTRAINT ex_1_type_instances_type_id_fkey FOREIGN KEY (type_id) REFERENCES ex_1_base_types(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_types_base_type_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_types
    ADD CONSTRAINT ex_1_types_base_type_fkey FOREIGN KEY (base_type) REFERENCES ex_1_base_types(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_1_types_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_types
    ADD CONSTRAINT ex_1_types_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES ex_1_base_types(id) ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: ex_2_address_references_address_position; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_address_references
    ADD CONSTRAINT ex_2_address_references_address_position FOREIGN KEY (address, "position") REFERENCES ex_2_operands(address, "position") ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_address_references_expression_node_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_address_references
    ADD CONSTRAINT ex_2_address_references_expression_node_id_fkey FOREIGN KEY (expression_node_id) REFERENCES ex_2_expression_nodes(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_base_types_pointer_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_base_types
    ADD CONSTRAINT ex_2_base_types_pointer_fkey FOREIGN KEY (pointer) REFERENCES ex_2_base_types(id) ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: ex_2_basic_block_instructions_bb_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_basic_block_instructions
    ADD CONSTRAINT ex_2_basic_block_instructions_bb_fkey FOREIGN KEY (basic_block_id) REFERENCES ex_2_basic_blocks(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_basic_block_instructions_ins_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_basic_block_instructions
    ADD CONSTRAINT ex_2_basic_block_instructions_ins_fkey FOREIGN KEY (instruction) REFERENCES ex_2_instructions(address) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_basic_blocks_parent_function_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_basic_blocks
    ADD CONSTRAINT ex_2_basic_blocks_parent_function_fkey FOREIGN KEY (parent_function) REFERENCES ex_2_functions(address) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_callgraph_destination_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_callgraph
    ADD CONSTRAINT ex_2_callgraph_destination_fkey FOREIGN KEY (destination) REFERENCES ex_2_functions(address) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_callgraph_source_address_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_callgraph
    ADD CONSTRAINT ex_2_callgraph_source_address_fkey FOREIGN KEY (source_address) REFERENCES ex_2_instructions(address) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_callgraph_source_basic_block_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_callgraph
    ADD CONSTRAINT ex_2_callgraph_source_basic_block_id_fkey FOREIGN KEY (source_basic_block_id) REFERENCES ex_2_basic_blocks(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_callgraph_source_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_callgraph
    ADD CONSTRAINT ex_2_callgraph_source_fkey FOREIGN KEY (source) REFERENCES ex_2_functions(address) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_control_flow_graphs_destination_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_control_flow_graphs
    ADD CONSTRAINT ex_2_control_flow_graphs_destination_fkey FOREIGN KEY (destination) REFERENCES ex_2_basic_blocks(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_control_flow_graphs_parent_function_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_control_flow_graphs
    ADD CONSTRAINT ex_2_control_flow_graphs_parent_function_fkey FOREIGN KEY (parent_function) REFERENCES ex_2_functions(address) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_control_flow_graphs_source_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_control_flow_graphs
    ADD CONSTRAINT ex_2_control_flow_graphs_source_fkey FOREIGN KEY (source) REFERENCES ex_2_basic_blocks(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_expression_nodes_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_expression_nodes
    ADD CONSTRAINT ex_2_expression_nodes_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES ex_2_expression_nodes(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_expression_substitutions_address_position_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_expression_substitutions
    ADD CONSTRAINT ex_2_expression_substitutions_address_position_fkey FOREIGN KEY (address, "position") REFERENCES ex_2_operands(address, "position") ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_expression_substitutions_expression_node_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_expression_substitutions
    ADD CONSTRAINT ex_2_expression_substitutions_expression_node_id_fkey FOREIGN KEY (expression_node_id) REFERENCES ex_2_expression_nodes(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_expression_tree_nodes_expression_node_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_expression_tree_nodes
    ADD CONSTRAINT ex_2_expression_tree_nodes_expression_node_id_fkey FOREIGN KEY (expression_node_id) REFERENCES ex_2_expression_nodes(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_expression_tree_nodes_expression_tree_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_expression_tree_nodes
    ADD CONSTRAINT ex_2_expression_tree_nodes_expression_tree_id_fkey FOREIGN KEY (expression_tree_id) REFERENCES ex_2_expression_trees(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_expression_type_instances_address_position_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_expression_type_instances
    ADD CONSTRAINT ex_2_expression_type_instances_address_position_fkey FOREIGN KEY (address, "position") REFERENCES ex_2_operands(address, "position") ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_expression_type_instances_expression_node_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_expression_type_instances
    ADD CONSTRAINT ex_2_expression_type_instances_expression_node_id_fkey FOREIGN KEY (expression_node_id) REFERENCES ex_2_expression_nodes(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_expression_type_instances_type_instance_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_expression_type_instances
    ADD CONSTRAINT ex_2_expression_type_instances_type_instance_id_fkey FOREIGN KEY (type_instance_id) REFERENCES ex_2_type_instances(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_expression_type_type_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_expression_types
    ADD CONSTRAINT ex_2_expression_type_type_fkey FOREIGN KEY (type) REFERENCES ex_2_base_types(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: ex_2_operands_address_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_operands
    ADD CONSTRAINT ex_2_operands_address_fkey FOREIGN KEY (address) REFERENCES ex_2_instructions(address) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_operands_expression_tree_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_operands
    ADD CONSTRAINT ex_2_operands_expression_tree_id_fkey FOREIGN KEY (expression_tree_id) REFERENCES ex_2_expression_trees(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_type_instances_section_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_type_instances
    ADD CONSTRAINT ex_2_type_instances_section_id_fkey FOREIGN KEY (section_id) REFERENCES ex_2_sections(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_type_instances_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_type_instances
    ADD CONSTRAINT ex_2_type_instances_type_id_fkey FOREIGN KEY (type_id) REFERENCES ex_2_base_types(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_types_base_type_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_types
    ADD CONSTRAINT ex_2_types_base_type_fkey FOREIGN KEY (base_type) REFERENCES ex_2_base_types(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ex_2_types_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_types
    ADD CONSTRAINT ex_2_types_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES ex_2_base_types(id) ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--