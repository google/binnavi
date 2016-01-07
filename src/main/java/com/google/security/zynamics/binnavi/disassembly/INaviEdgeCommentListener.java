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
package com.google.security.zynamics.binnavi.disassembly;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdgeListener;

import java.util.List;



/**
 * Interface for objects that want to be notified about changes in edges.
 */
public interface INaviEdgeCommentListener extends IViewEdgeListener {
  /**
   * Invoked after a global edge comment has been appended to an edge.
   * 
   * @param edge The edge where the comment has been appended.
   * @param comment The comment which has been appended.
   */
  void appendedGlobalEdgeComment(INaviEdge edge, IComment comment);

  /**
   * Invoked after a local edge comment has been appended to an edge.
   * 
   * @param edge The edge where the comment has been appended.
   * @param comment The comment which has been appended.
   */
  void appendedLocalEdgeComment(INaviEdge edge, IComment comment);

  /**
   * Invoked after a global edge comment has been deleted from an edge.
   * 
   * @param edge The edge where the global comment has been deleted.
   * @param comment The comment which has been deleted.
   */
  void deletedGlobalEdgeComment(INaviEdge edge, IComment comment);

  /**
   * Invoked after a local edge comment has been deleted from an edge.
   * 
   * @param edge The edge where the local comment has been deleted.
   * @param comment The comment which has been deleted.
   */
  void deletedLocalEdgeComment(INaviEdge edge, IComment comment);

  /**
   * Invoked after a global edge comment has been edited in an edge.
   * 
   * @param edge The edge where the comment has been edited.
   * @param comment The comment which was edited.
   */
  void editedGlobalEdgeComment(INaviEdge edge, IComment comment);

  /**
   * Invoked after a local edge comment has been edited in an edge.
   * 
   * @param edge The edge where the comment has been edited.
   * @param comment The comment which has been edited.
   */
  void editedLocalEdgeComment(INaviEdge edge, IComment comment);

  /**
   * Invoked after a global edge comment has been initialized.
   * 
   * @param edge The edge which where the comments have been initialized.
   * @param comments The global comments which have been initialized.
   */
  void initializedGlobalEdgeComment(INaviEdge edge, List<IComment> comments);

  /**
   * Invoked after a local edge comment has been initialized.
   * 
   * @param edge The edge which where the comments have been initialized.
   * @param comments The local comments which have been initialized.
   */
  void initializedLocalEdgeComment(INaviEdge edge, List<IComment> comments);

}
