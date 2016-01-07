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

import java.util.List;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;


// / Used to listen on group nodes.
/**
 * Interface that can be implemented by objects that want to be notified about changes in GroupNode
 * objects.
 */
public interface IGroupNodeListener {
  // ! Signals a new node in the group node.
  /**
   * Invoked after a new node was added to the group node.
   * 
   * @param groupNode The group node that was extended.
   * @param node The node that was added to the group node.
   */
  void addedNode(GroupNode groupNode, ViewNode node);

  // ! Signals an appended group node comment.
  /**
   * Invoked after a comment has been appended to the list of group node comments.
   * 
   * @param groupNode The group node where the comment was appended.
   * @param comment The comment which has been appended.
   */
  void appendedComment(GroupNode groupNode, IComment comment);

  // ! Signals changing collapse states.
  /**
   * Invoked after the collapse state of the group node changed.
   * 
   * @param groupNode The group node that was collapsed or uncollapsed.
   * @param collapsed True, if the node is now collapsed. False, if it is expanded.
   */
  void changedState(GroupNode groupNode, boolean collapsed);

  // ! Signals a deleted group node comment.
  /**
   * Invoked after a comment has been deleted from the list of group node comments.
   * 
   * @param groupNode The group node where the comment was deleted.
   * @param comment The comment which has been deleted.
   */
  void deletedComment(GroupNode groupNode, IComment comment);

  // ! Signals an edited group node comment.
  /**
   * Invoked after a comment has been edited in the list of group node comments.
   * 
   * @param groupNode The group node where the comment was edited.
   * @param comment The comment which has been edited.
   */
  void editedComment(GroupNode groupNode, IComment comment);

  // ! Signals the initialization of the group nodes comments.
  /**
   * Invoked after the group node comments have been initialized.
   * 
   * @param groupNode The group node where comments have been initialized.
   * @param comment The comments which have been associated with the group node.
   */
  void initializedComment(GroupNode groupNode, List<IComment> comment);

  // ! Signals that a node was removed from the group node.
  /**
   * Invoked after a node was removed from the group node.
   * 
   * @param groupNode The group node from where the node was removed.
   * @param node The node that was removed from the group node..
   */
  void removedNode(GroupNode groupNode, ViewNode node);
}
