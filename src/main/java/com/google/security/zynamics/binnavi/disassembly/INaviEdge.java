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

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdge;

import java.util.List;



/**
 * Interface that represents edges in graphs.
 */
public interface INaviEdge extends IViewEdge<INaviViewNode>, IDatabaseObject {
  /**
   * Appends a new global comment to the list of global comments associated to this edge.
   * 
   * @param commentText The comment text to be added.
   * @return The appended comment.
   * 
   * @throws CouldntSaveDataException Thrown if the comment could not be saved to the database.
   * @throws CouldntLoadDataException
   */
  List<IComment> appendGlobalComment(final String commentText) throws CouldntSaveDataException,
      CouldntLoadDataException;

  /**
   * Appends a new local comment to the list of local comments associated to this edge.
   * 
   * @param commentText The comment text to be added.
   * @return The appended comment.
   * 
   * @throws CouldntSaveDataException Thrown if the comment could not be saved to the database.
   * @throws CouldntLoadDataException
   */
  List<IComment> appendLocalComment(final String commentText) throws CouldntSaveDataException,
      CouldntLoadDataException;

  /**
   * Deletes a global comment from the list of global comments currently associated to this edge.
   * 
   * @param comment The global comment to be deleted.
   * 
   * @throws CouldntDeleteException Thrown if the comment could not be deleted from the database.
   */
  void deleteGlobalComment(final IComment comment) throws CouldntDeleteException;

  /**
   * Deletes a local comment from the list of local comments currently associated to this edge.
   * 
   * @param comment The local comment to be deleted.
   * 
   * @throws CouldntDeleteException Thrown if the comment could not be deleted from the database.
   */
  void deleteLocalComment(final IComment comment) throws CouldntDeleteException;

  /**
   * Frees the allocated resources of the edge.
   */
  void dispose();

  /**
   * Changes the comment text of a global comment currently associated to this edge.
   * 
   * @param comment The global comment which has been edited.
   * 
   * @throws CouldntSaveDataException Thrown if the changes to the comment could not be saved to the
   *         database.
   */
  IComment editGlobalComment(final IComment comment, final String commentText)
      throws CouldntSaveDataException;

  /**
   * Changes the comment text of a local comment currently associated to this edge.
   * 
   * @param comment The local comment which has been edited.
   * 
   * @throws CouldntSaveDataException Thrown if the changes to the comment could not be saved to the
   *         database.
   */
  IComment editLocalComment(final IComment comment, final String commentText)
      throws CouldntSaveDataException;

  /**
   * Returns the global comment of the edge.
   * 
   * @return The global comment of the edge.
   */
  List<IComment> getGlobalComment();

  /**
   * Returns the local comment of the edge.
   * 
   * @return The local comment of the edge.
   */
  List<IComment> getLocalComment();

  /**
   * Initializes the global comment of the edge.
   * 
   * @param globalComments The new global edge comment.
   */
  void initializeGlobalComment(List<IComment> globalComments);

  /**
   * Initializes the local comment of the edge.
   * 
   * @param localComments The new local comment of the edge.
   */
  void initializeLocalComment(List<IComment> localComments);

  /**
   * Checks if the current active user is the owner of the comment in question.
   * 
   * @param comment The comment to check.
   * 
   * @return True if the current active user is the owner of the comment.
   */
  boolean isOwner(IComment comment);

  boolean isStored();
}
