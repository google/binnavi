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

import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerProvider;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListProvider;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.List;

/**
 * Interface to be implemented by objects that are view containers.
 */
public interface IViewContainer extends ApiObject<Object> {
  /**
   * Adds a listener that is notified about changes in the view container.
   *
   * @param listener The listener object to add.
   */
  void addListener(IViewContainerListener listener);

  /**
   * Tests whether the view container contains a given module.
   *
   * @param module The module to check.
   *
   * @return True, if the view container contains the given module. False, otherwise.
   */
  boolean containsModule(INaviModule module);

  /**
   * Creates a new empty view in the view container.
   *
   * @param name The name of the new view.
   * @param description The description of the new view.
   *
   * @return The new view.
   */
  INaviView createView(String name, String description);

  /**
   * Deletes a view from the view container.
   *
   * @param view The view to delete.
   *
   * @throws CouldntDeleteException Thrown if the view could not be deleted.
   */
  void deleteView(INaviView view) throws CouldntDeleteException;

  /**
   * Frees allocated resources.
   */
  void dispose();

  /**
   * Returns the address spaces of the view container.
   *
   * @return The address spaces of the view container.
   */
  List<INaviAddressSpace> getAddressSpaces();

  /**
   * Returns the database the view container belongs to.
   *
   * @return The database the view container belongs to.
   */
  IDatabase getDatabase();

  /**
   * Returns the debugger provider for this view container.
   *
   * @return The debugger container for this view container.
   */
  DebuggerProvider getDebuggerProvider();

  /**
   * Maps a view to a function. This is useful if you want to know what function was used to
   * generate a view.
   *
   * @param view A view from the project.
   *
   * @return The function that was used to create the view or null if the function can not be
   *         determined.
   */
  INaviFunction getFunction(INaviView view);

  /**
   * Returns the functions in the view container.
   *
   * @return The functions in the view container.
   */
  List<INaviFunction> getFunctions();

  /**
   * Returns the modules of the view container.
   *
   * @return The modules of the view container.
   */
  List<INaviModule> getModules();

  /**
   * Returns the name of the view container.
   *
   * @return The name of the view container.
   */
  String getName();

  /**
   * Returns a list of all tagged views and the tags they are tagged with.
   *
   * @return A list of tagged views.
   */
  List<Pair<INaviView, CTag>> getTaggedViews();

  /**
   * Returns a list of views tagged with a given tag.
   *
   * @param tag The tag in question.
   *
   * @return List of views tagged with the given tag.
   */
  List<INaviView> getTaggedViews(CTag tag);

  /**
   * Returns the trace provider of the view container.
   *
   * @return The trace provider of the view container.
   */
  ITraceListProvider getTraceProvider();

  /**
   * Returns a list of all user views in the container.
   *
   * @return A list of all user views in the container.
   */
  List<INaviView> getUserViews();

  /**
   * Returns the view backed by a given function.
   *
   * @param function The function that backs the view.
   *
   * @return The view backed by a given function.
   */
  INaviView getView(INaviFunction function);

  int getViewCount();

  /**
   * Returns a list of all views that can be found in the module.
   *
   * @return A list of all views that can be found in the module.
   */
  List<INaviView> getViews();

  /**
   * Returns all views of the module that contain the given addresses.
   *
   * @param addresses The addresses to search for.
   * @param all True, to return views that contain all addresses. False, to return views that
   *        contain any address.
   *
   * @return The views with the given addresses.
   *
   * @throws CouldntLoadDataException Thrown if the views could not be determined.
   */
  List<INaviView> getViewsWithAddresses(List<UnrelocatedAddress> addresses,
      boolean all) throws CouldntLoadDataException;

  /**
   * Returns a flag that says whether the module was loaded already.
   *
   * @return True, if the module is loaded. False, otherwise.
   */
  boolean isLoaded();

  /**
   * Removes a listener that was previously notified about changes in the view container.
   *
   * @param listener The listener object to remove.
   */
  void removeListener(IViewContainerListener listener);
}
