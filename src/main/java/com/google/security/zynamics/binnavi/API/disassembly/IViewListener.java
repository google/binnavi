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
package com.google.security.zynamics.binnavi.API.disassembly;

import java.util.Date;

// / Used to listen on views.
/**
 * Interface that can be implemented by objects that want to be notified about changes in
 * {@link View} objects.
 */
public interface IViewListener {

  // ! Signals a new edge.
  /**
   * Invoked after an edge was added to the view.
   *
   * @param view The view where the edge was added.
   * @param edge The edge that was added to the view.
   */
  void addedEdge(View view, ViewEdge edge);

  // ! Signals a new node.
  /**
   * Invoked after a node was added to the view.
   *
   * @param view The view where the node was added.
   * @param node The node that was added to the view.
   */
  void addedNode(View view, ViewNode node);

  // ! Signals a change of the view description.
  /**
   * Invoked after the description of a view changed.
   *
   * @param view The view whose description changed.
   * @param description The new description of the view.
   */
  void changedDescription(View view, String description);

  // ! Signals a change of the graph type.
  /**
   * Invoked after the type of the view's graph changed. This event is generally invoked after nodes
   * were added to or removed from the view.
   *
   * @param view The view whose type changed.
   * @param type The new type of the graph,
   */
  void changedGraphType(View view, GraphType type);

  // ! Signals a change of the view modification date.
  /**
   * Invoked after the modification date of the view changed.
   *
   * @param view The view whose modification date changed.
   * @param date The new modification date of the view.
   */
  void changedModificationDate(View view, Date date);

  // ! Signals a change of the view name.
  /**
   * Invoked after the name of the view changed.
   *
   * @param view The view whose name changed.
   * @param name The new name of the view.
   */
  void changedName(View view, String name);

  // ! Signals that the view was closed.
  /**
   * Invoked after the view was closed.
   *
   *  After this function was invoked, using parts of the view which must be loaded before they can
   * be used (for example the graph of the view) leads to undefined behavior.
   *
   * @param view The view that was closed.
   */
  void closedView(View view);

  // ! Signals that the view is about to be closed.
  /**
   * Invoked right before a view is closed. The listening object has the opportunity to veto the
   * close process if it still needs to work with the loaded parts of the view.
   *
   * @param view The view that is about to be closed.
   *
   * @return True, to indicate that the view can be closed. False, to veto the close process.
   */
  boolean closingView(View view);

  // ! Signals the deletion of an edge.
  /**
   * Invoked after an edge was deleted from the view.
   *
   * @param view The view the edge was deleted from.
   * @param edge The edge that was deleted from the view.
   */
  void deletedEdge(View view, ViewEdge edge);

  // ! Signals the deletion of a node.
  /**
   * Invoked after an node was deleted from the view.
   *
   * @param view The view the node was deleted from.
   * @param node The node that was deleted from the view.
   */
  void deletedNode(View view, ViewNode node);

  // ! Signals that the view was tagged.
  /**
   * Invoked after a view was tagged with a tag.
   *
   * @param view The view that was tagged.
   * @param tag The tag that was used to tag the view.
   */
  void taggedView(View view, Tag tag);

  // ! Signals that the view was untagged.
  /**
   * Invoked after a tag was removed from a view.
   *
   * @param view The view that was untagged.
   * @param tag The tag that was removed from the view.
   */
  void untaggedView(View view, Tag tag);
}
