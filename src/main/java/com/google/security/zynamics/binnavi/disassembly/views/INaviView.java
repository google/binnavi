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

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.IDatabaseObject;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.IStaredItem;
import com.google.security.zynamics.zylib.disassembly.IBlockContainer;
import com.google.security.zynamics.zylib.disassembly.IView;
import com.google.security.zynamics.zylib.types.graphs.IDirectedGraph;



/**
 * Interface that represents views.
 */
public interface INaviView extends IView<INaviViewNode, INaviViewListener>, IDatabaseObject,
    IBlockContainer<INaviInstruction>, IStaredItem {
  @Override
  List<CCodeNode> getBasicBlocks();

  /**
   * Returns the configuration object of the view.
   * 
   * @return The configuration object.
   */
  IViewConfiguration getConfiguration();

  /**
   * Returns the content of the view.
   * 
   * @return The content of the view.
   */
  IViewContent getContent();

  /**
   * Returns the derived views of the view.
   * 
   * @return The derived views of the view.
   * 
   * @throws CouldntLoadDataException Thrown if the derived views could not be loaded.
   */
  List<INaviView> getDerivedViews() throws CouldntLoadDataException;

  @Override
  IDirectedGraph<INaviViewNode, INaviEdge> getGraph();

  /**
   * Returns the index of the current load step.
   * 
   * @return The index of the current load step.
   */
  int getLoadState();

  /**
   * Returns the node tags used to tag nodes of this view.
   * 
   * @return The node tags used.
   */
  Set<CTag> getNodeTags();

  /**
   * Loads the view.
   * 
   * @throws CouldntLoadDataException Thrown if the view could not be loaded.
   * @throws CPartialLoadException Thrown if not all necessary modules required to load the view are
   *         loaded.
   * @throws LoadCancelledException Thrown if the usre cancelled loading.
   */
  void load() throws CouldntLoadDataException, CPartialLoadException, LoadCancelledException;

  /**
   * Loads the view settings.
   * 
   * @return The loaded view settings.
   * 
   * @throws CouldntLoadDataException Thrown if the view settings could not be loaded.
   */
  Map<String, String> loadSettings() throws CouldntLoadDataException;

  /**
   * Saves the view to the database.
   * 
   * @throws CouldntSaveDataException Thrown if the view could not be saved.
   */
  void save() throws CouldntSaveDataException;

  /**
   * Saves the view settings to the database.
   * 
   * @param settings The view settings to save.
   * 
   * @throws CouldntSaveDataException Thrown if the view settings could not be saved.
   */
  void saveSettings(Map<String, String> settings) throws CouldntSaveDataException;

  /**
   * Returns whether the view was modified since the last save operation.
   * 
   * @return True, if the view was modified. False, if it was not.
   */
  boolean wasModified();
}
