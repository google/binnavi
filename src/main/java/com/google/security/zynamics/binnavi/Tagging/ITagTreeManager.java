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
package com.google.security.zynamics.binnavi.Tagging;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;



/**
 * Interface to be implemented by all objects that want to serve as tag managers.
 */
public interface ITagTreeManager {
  void addListener(ITagTreeManagerListener listener);

  /**
   * Creates a new tag that is managed by the tag manager.
   * 
   * @param parent The parent tag of the new tag.
   * @param name The name of the new tag.
   * 
   * @return The new tag.
   * 
   * @throws CouldntSaveDataException Thrown if the new tag could not be stored to the database.
   */
  ITreeNode<CTag> addTag(final ITreeNode<CTag> parent, final String name)
      throws CouldntSaveDataException;

  /**
   * Deletes a tag from the manager.
   * 
   * @param tag The tag to delete.
   * 
   * @throws CouldntDeleteException Thrown if the tag could not be deleted from the database.
   */
  void deleteTag(ITreeNode<CTag> tag) throws CouldntDeleteException;

  /**
   * Deletes an entire tag subtree from the tag manager.
   * 
   * @param tag The tag to delete.
   * 
   * @throws CouldntDeleteException Thrown if the tag tree could not be deleted from the database.
   */
  void deleteTagSubTree(ITreeNode<CTag> tag) throws CouldntDeleteException;

  /**
   * Returns the root tag of the tag tree.
   * 
   * @return The root tag of the tag tree.
   */
  ITreeNode<CTag> getRootTag();

  /**
   * Inserts a tag after a given parent tag. All original child tags of the parent tag are child
   * tags of the new tag once insertion is complete.
   * 
   * @param parent The parent tag of the new tag.
   * @param name The name of the new tag.
   * 
   * @return The new tag.
   * 
   * @throws CouldntSaveDataException Thrown if the new tag could not be stored to the database.
   */
  ITreeNode<CTag> insertTag(final ITreeNode<CTag> parent, final String name)
      throws CouldntSaveDataException;

  /**
   * Moves a tag.
   * 
   * @param parent The new parent node of the tag.
   * @param child The tag to move.
   * 
   * @throws CouldntSaveDataException Thrown if the tag could not be moved.
   */
  void moveTag(ITreeNode<CTag> parent, ITreeNode<CTag> child) throws CouldntSaveDataException;

  void removeListener(ITagTreeManagerListener listener);
}
