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

import java.util.Date;
import java.util.Set;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.ViewType;



/**
 * Interface for objects that want to serve as view configuration objects.
 */
public interface IViewConfiguration {
  /**
   * Returns the creation date of the view.
   * 
   * @return The creation date of the view.
   */
  Date getCreationDate();

  /**
   * Returns the description of the view.
   * 
   * @return The description of the view.
   */
  String getDescription();

  /**
   * Returns the ID of the view.
   * 
   * @return The ID of the view.
   */
  int getId();

  /**
   * Returns the modification date of the view.
   * 
   * @return The modification date of the view.
   */
  Date getModificationDate();

  /**
   * Returns the module the view belongs to.
   * 
   * @return The module the view belongs to.
   */
  INaviModule getModule();

  /**
   * Returns the name of the view.
   * 
   * @return The name of the view.
   */
  String getName();

  /**
   * Returns the project the view belongs to.
   * 
   * @return The project the view belongs to.
   */
  INaviProject getProject();

  /**
   * Returns the type of the view.
   * 
   * @return The type of the view.
   */
  ViewType getType();

  /**
   * Returns the tags the view is tagged with.
   * 
   * @return The tags the view is tagged with.
   */
  Set<CTag> getViewTags();

  /**
   * Returns whether the view is stared.
   * 
   * @return True, if the view is stared. False, otherwise.
   */
  boolean isStared();

  /**
   * Returns a flag that says whether the view is tagged or not.
   * 
   * @return True, if the view is tagged.
   */
  boolean isTagged();

  /**
   * Determines whether this view is tagged with parameter tag.
   * 
   * @param tag
   * 
   * @return True, if the view is tagged. False, otherwise.
   */
  boolean isTagged(CTag tag);

  /**
   * Changes the description of the view.
   * 
   * @param description The new description of the view.
   * 
   * @throws CouldntSaveDataException Thrown if the new description could not be saved.
   */
  void setDescription(String description) throws CouldntSaveDataException;

  /**
   * Changes the ID of the view.
   * 
   * @param viewId The new ID of the view.
   */
  void setId(int viewId);

  /**
   * Changes the name of the view.
   * 
   * @param name The new name of the view.
   * 
   * @throws CouldntSaveDataException Thrown if the new name could not be saved.
   */
  void setName(String name) throws CouldntSaveDataException;

  void setNameInternal(final String name);

  void setDescriptionInternal(final String description);

  void setStaredInternal(final boolean isStared);

  void setModificationDateInternal(final Date modificationDate);

  /**
   * Changes the star state of the view.
   * 
   * @param stared The new star state.
   * 
   * @throws CouldntSaveDataException Thrown if the star state could not be updated.
   */
  void setStared(boolean stared) throws CouldntSaveDataException;

  /**
   * Assigns a tag to the view.
   * 
   * @param tag The tag.
   * 
   * @throws CouldntSaveDataException Thrown if the tag assignment could not be saved to the
   *         database.
   */
  void tagView(CTag tag) throws CouldntSaveDataException;

  /**
   * Removes a tag from the view.
   * 
   * @param tag The tag to remove.
   * 
   * @throws CouldntSaveDataException Thrown if the tag removal could not be stored in the database.
   */
  void untagView(CTag tag) throws CouldntSaveDataException;

  /**
   * Updates the modification date of the view.
   */
  void updateModificationDate();

  int getUnloadedEdgeCount();

  GraphType getUnloadedGraphType();

  int getUnloadedNodeCount();
}
