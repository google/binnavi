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

import java.util.ArrayList;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;


// / Used to listen on function nodes.
/**
 * Interface that can be implemented by objects that want to be notified about changes in
 * FunctionNode objects.
 */
public interface IFunctionNodeListener {
  // ! Signals a new function node comment.
  /**
   * Invoked after the local comment of the function node changed.
   *
   * @param node The function node whose comment changed.
   * @param comment The new comment of the function node.
   */
  void appendedComment(FunctionNode node, IComment comment);

  // ! Signals a deleted function node comment.
  /**
   * invoked after a function node comment has been deleted.
   *
   * @param node The node where the function node comment has been deleted.
   * @param comment The comment which has been deleted.
   */
  void deletedComment(FunctionNode node, IComment comment);

  // ! Signals an edited function node comment.
  /**
   * Invoked after a function node comment has been edited.
   *
   * @param node The function node where the comment has been edited.
   * @param comment The comment which has been edited.
   */
  void editedComment(FunctionNode node, IComment comment);

  // ! Signals an initialized function node comment.
  /**
   * Invoked after a function node comment has been initialized.
   *
   * @param node The function node where the comment has been initialized.
   * @param comment The comments with which the node has been initialized.
   */
  void initializedComment(FunctionNode node, ArrayList<IComment> comment);
}
