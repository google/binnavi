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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Implementations;

import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.ZyGraph.Filter.CGraphNodeTaggedAndVisibleFilter;
import com.google.security.zynamics.binnavi.ZyGraph.Filter.CGraphNodeTaggedFilter;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphHelpers;
import com.google.security.zynamics.zylib.types.trees.BreadthFirstSorter;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains methods for selecting and deselecting nodes based on tag traits.
 */
public final class CTagSelectionFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CTagSelectionFunctions() {
  }

  /**
   * Returns all tags of a sub-hierarchy.
   *
   * @param tag The root node of the sub-hierarchy.
   *
   * @return The tags of the sub-hierarchy.
   */
  private static Set<CTag> getSubtreeTags(final ITreeNode<CTag> tag) {
    final Set<CTag> tags = new HashSet<CTag>();

    tags.add(tag.getObject());

    for (final ITreeNode<CTag> t : BreadthFirstSorter.getSortedList(tag)) {
      tags.add(t.getObject());
    }

    return tags;
  }

  /**
   * Selects the nodes of a graph that are selected with the given tags.
   *
   * @param graph The graph whose nodes are selected.
   * @param tags Nodes tagged with these tags are selected.
   * @param mustBeVisible True, to select only visible nodes. False, to select all nodes.
   */
  private static void selectNodes(
      final ZyGraph graph, final Set<CTag> tags, final boolean mustBeVisible) {
    final Collection<NaviNode> nodes;

    if (mustBeVisible) {
      nodes = GraphHelpers.filter(graph, new CGraphNodeTaggedFilter(tags));
    } else {
      nodes = GraphHelpers.filter(graph, new CGraphNodeTaggedAndVisibleFilter(tags));
    }

    graph.selectNodes(nodes, true);
  }

  /**
   * Unselects the nodes of a graph that are selected with the given tags.
   *
   * @param graph The graph whose nodes are unselected.
   * @param tags Nodes tagged with these tags are unselected.
   * @param mustBeVisible True, to unselect only visible nodes. False, to unselect all nodes.
   */
  private static void unselectNodes(
      final ZyGraph graph, final Set<CTag> tags, final boolean mustBeVisible) {
    final Collection<NaviNode> nodes;

    if (mustBeVisible) {
      nodes = GraphHelpers.filter(graph, new CGraphNodeTaggedFilter(tags));
    } else {
      nodes = GraphHelpers.filter(graph, new CGraphNodeTaggedAndVisibleFilter(tags));
    }

    graph.selectNodes(nodes, false);
  }

  /**
   * Selects all nodes of a graph tagged with the given tag.
   *
   * @param graph The graph whose nodes are selected.
   * @param tag Nodes with this tag are selected.
   */
  public static void selectNodes(final ZyGraph graph, final CTag tag) {
    selectNodes(graph, Sets.newHashSet(tag), false);
  }

  /**
   * Selects all nodes of a graph that are tagged with a given tag or any of its children.
   *
   * @param graph The graph whose nodes are selected.
   * @param tag Nodes with this tag are selected.
   */
  public static void selectSubtreeNodes(final ZyGraph graph, final ITreeNode<CTag> tag) {
    selectNodes(graph, getSubtreeTags(tag), false);
  }

  /**
   * Selects all visible nodes of a graph tagged with the given tag.
   *
   * @param graph The graph whose nodes are selected.
   * @param tag Nodes with this tag are selected.
   */
  public static void selectVisibleNodes(final ZyGraph graph, final CTag tag) {
    selectNodes(graph, Sets.newHashSet(tag), true);
  }

  /**
   * Selects all visible nodes of a graph that are tagged with a given tag or any of its children.
   *
   * @param graph The graph whose nodes are selected.
   * @param tag Nodes with this tag are selected.
   */
  public static void selectVisibleSubtreeNodes(final ZyGraph graph, final ITreeNode<CTag> tag) {
    selectNodes(graph, getSubtreeTags(tag), true);
  }


  /**
   * Unselects all nodes of a given graph that are tagged with a given tag.
   *
   * @param graph The graph whose nodes are untagged.
   * @param tag Nodes with this tag are unselected.
   */
  public static void unselectNodes(final ZyGraph graph, final CTag tag) {
    unselectNodes(graph, Sets.newHashSet(tag), true);
  }

  /**
   * Unselects all nodes of a given graph that are tagged with a given tag or any of its child tags.
   *
   * @param graph The graph whose nodes are unselected.
   * @param tag The root tag of the child hierarchy to unselect.
   */
  public static void unselectSubtreeNodes(final ZyGraph graph, final ITreeNode<CTag> tag) {
    unselectNodes(graph, getSubtreeTags(tag), true);
  }
}
