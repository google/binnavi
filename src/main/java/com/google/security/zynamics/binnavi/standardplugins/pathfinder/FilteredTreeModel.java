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
package com.google.security.zynamics.binnavi.standardplugins.pathfinder;

import javax.swing.JDialog;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import com.google.security.zynamics.binnavi.API.disassembly.Module;


/**
 * Class to implement a tree model which can be filtered by supplying a filter object
 */
public class FilteredTreeModel extends DefaultTreeModel {

  public FilteredTreeModel(final JDialog parent, final Module module) {
    super(new FunctionTreeRootNode(parent, module));
  }

  /**
   * Apply given filter to root node in the tree The filter is then recursively propagated to all
   * children
   * 
   * @param filter
   */
  public void setFilter(final TextPatternFilter filter) {
    ((FunctionTreeRootNode) getRoot()).setFilter(filter);
    nodeStructureChanged((TreeNode) getRoot());
  }
}
