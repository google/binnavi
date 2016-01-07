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
package com.google.security.zynamics.binnavi.disassembly.views;

import java.util.ArrayList;
import java.util.List;


import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.general.Pair;

/**
 * Contains code for filtering view lists.
 */
public final class CViewFilter {
  /**
   * You are not supposed to instantiate this class.
   */
  private CViewFilter() {
  }

  /**
   * Returns the number of views which match the given type.
   * 
   * @param views The list of available views.
   * @param type The type of view to match.
   * @return The number of views matching the given type.
   */
  private static Iterable<INaviView> getViewsByType(final List<INaviView> views,
      final GraphType type) {
    return Collections2.filter(views, new Predicate<INaviView>() {
      @Override
      public boolean apply(final INaviView view) {
        return view.getGraphType() == type;
      }
    });
  }

  /**
   * Returns the number of views of type CALLGRAPH.
   * 
   * @param views The list of views.
   * @return The number of views of type CALLGRAPH:
   */
  public static int getCallgraphViewCount(final List<INaviView> views) {
    return Iterables.size(getViewsByType(views, GraphType.CALLGRAPH));
  }

  /**
   * Returns an iterable for all views of type CALLGRAPH.
   * 
   * @param views The list of views.
   * @return The iterable for all views of type CALLGRAPH:
   */
  public static Iterable<INaviView> getCallgraphViews(final List<INaviView> views) {
    return getViewsByType(views, GraphType.CALLGRAPH);
  }

  /**
   * Returns the number of views of type FLOWGRAPH.
   * 
   * @param views The list of views.
   * @return The number of views of type FLOWGRAPH:
   */
  public static int getFlowgraphViewCount(final List<INaviView> views) {
    return Iterables.size(getViewsByType(views, GraphType.FLOWGRAPH));
  }

  /**
   * Returns an iterable for all views of type FLOWGRAPH.
   * 
   * @param views The list of views.
   * @return The iterable for all views of type FLOWGRAPH:
   */
  public static Iterable<INaviView> getFlowgraphViews(final List<INaviView> views) {
    return getViewsByType(views, GraphType.FLOWGRAPH);
  }

  /**
   * Returns the number of views of type MIXED_GRAPH.
   * 
   * @param views The list of views.
   * @return The number of views of type MIXED_GRAPH:
   */
  public static int getMixedgraphViewCount(final List<INaviView> views) {
    return Iterables.size(getViewsByType(views, GraphType.MIXED_GRAPH));
  }

  /**
   * Returns a list of all tagged views and the tags they are tagged with.
   * 
   * @return A list of tagged views.
   */
  public static List<Pair<INaviView, CTag>> getTaggedViews(final List<INaviView> views) {
    final List<Pair<INaviView, CTag>> taggedViews = new ArrayList<Pair<INaviView, CTag>>();

    for (final INaviView view : views) {
      for (final CTag tag : view.getConfiguration().getViewTags()) {
        taggedViews.add(new Pair<INaviView, CTag>(view, tag));
      }
    }

    return taggedViews;
  }

  /**
   * Returns a list of views tagged with a given tag.
   * 
   * @param tag The tag in question.
   * 
   * @return List of views tagged with the given tag.
   */
  public static List<INaviView> getTaggedViews(final List<INaviView> views, final CTag tag) {
    final List<INaviView> taggedViews = new ArrayList<INaviView>();

    for (final INaviView view : views) {
      if (view.getConfiguration().isTagged(tag)) {
        taggedViews.add(view);
      }
    }

    return taggedViews;
  }
}
