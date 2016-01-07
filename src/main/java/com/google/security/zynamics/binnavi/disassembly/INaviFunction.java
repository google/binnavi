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
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IBlockContainer;
import com.google.security.zynamics.zylib.disassembly.IFunction;
import com.google.security.zynamics.zylib.disassembly.IFunctionListener;
import com.google.security.zynamics.zylib.types.graphs.DirectedGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface that represents functions.
 */
public interface INaviFunction extends IFunction, IDatabaseObject,
    IBlockContainer<INaviInstruction> {
  /**
   * Appends a global comment to the list of global comments.
   *
   * @return The id of the newly generated comment.
   *
   * @throws CouldntSaveDataException if the comment could not be saved to the database.
   * @throws CouldntLoadDataException
   */
  List<IComment> appendGlobalComment(String commentText) throws CouldntSaveDataException,
      CouldntLoadDataException;

  /**
   * Closes the function.
   *
   * @return True, if the function was closed. False, if the close operation was vetoed.
   */
  boolean close();

  /**
   * Deletes a global comment from the list of comments.
   *
   * @param comment The comment to be deleted.
   *
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  void deleteGlobalComment(IComment comment) throws CouldntDeleteException;

  /**
   * Edits a global comment
   *
   * @param comment The comment that will be added.
   *
   * @throws CouldntSaveDataException if the comment changes could not be saved to the database.
   */
  IComment editGlobalComment(IComment comment, String commentText) throws CouldntSaveDataException;

  @Override
  List<IBlockNode> getBasicBlocks();

  /**
   * Returns the global comment of the function.
   *
   * @return The global comment of the function.
   */
  List<IComment> getGlobalComment();

  /**
   * Returns the graph built from the basic blocks and edges of the function.
   *
   *  Note that the function must be loaded before you can use this function. Using this function
   * without loading the function first results in an {@link IllegalStateException}.
   *
   * @return The graph of the function.
   */
  DirectedGraph<IBlockNode, IBlockEdge> getGraph();

  /**
   * Returns the module the function belongs to.
   *
   * @return The module the function belongs to.
   */
  INaviModule getModule();

  /**
   * Returns the original module name the function belongs to.
   *
   * @return The original module name the function belongs to.
   */
  String getOriginalModulename();

  /**
   * Returns the original name of the function.
   *
   * @return The original name of the function.
   */
  String getOriginalName();

  /**
   * Returns the address of the function this function is forwarded to.
   *
   * @return The address of the forwarding target.
   */
  IAddress getForwardedFunctionAddress();

  /**
   * Returns the ID of the module this function is forwarded to.
   *
   * @return The ID of the forwarding target module.
   */
  int getForwardedFunctionModuleId();

  /**
   * Returns the stack frame base type that is associated with this function. Null if no stack frame
   * exists.
   *
   * @return The stack frame base type of this function.
   */
  BaseType getStackFrame();

  /**
   * Initializes the global comments of the function.
   *
   * @param comments The new comments of the function.
   *
   * @throws CouldntSaveDataException Thrown if the comments could not be saved.
   */
  void initializeGlobalComment(ArrayList<IComment> comments) throws CouldntSaveDataException;

  /**
   * Determines whether the function has been loaded or not.
   *
   * @return True, if the function was loaded. False, otherwise.
   */
  boolean isLoaded();

  /**
   * Checks if the current active user is the owner of the comment in question.
   *
   * @param comment The comment to check.
   *
   * @return True if the current active user is the owner of the comment.
   */
  boolean isOwner(IComment comment);

  boolean isForwarded();

  /**
   * Loads the graph of the function. After a function was loaded, you can access the nodes and
   * edges of the function.
   *
   * @throws CouldntLoadDataException Thrown if the function could not be loaded.
   */
  void load() throws CouldntLoadDataException;

  /**
   * Removes a listener from the function object.
   *
   * @param listener The listener to remove.
   */
  void removeListener(IFunctionListener<IComment> listener);

  /**
   * Adds a listener to the function object.
   *
   * @param listener The listener to add.
   */
  void addListener(IFunctionListener<IComment> listener);

  /**
   * Changes the description of the function.
   *
   * @param comment The new description.
   *
   * @throws CouldntSaveDataException Thrown if the description could not be saved to the database.
   */
  void setDescription(String comment) throws CouldntSaveDataException;

  void setDescriptionInternal(String description);

  /**
   * Changes the name of the function.
   *
   * @param name The new name.
   *
   * @throws CouldntSaveDataException Thrown if the name could not be saved to the database.
   */
  void setName(String name) throws CouldntSaveDataException;

  void setNameInternal(String name);

  /**
   * Forwards the function to another function.
   *
   * @param function The function to forward to.
   *
   * @throws CouldntSaveDataException Thrown if the forwarding information could not be saved.
   */
  void setForwardedFunction(final INaviFunction function) throws CouldntSaveDataException;

  void removeForwardedFunction() throws CouldntSaveDataException;

  void setForwardedFunctionInternal(final INaviFunction function);

  void removeForwardedFunctionInternal();

  /**
   * Associates a stack frame base type with this function.
   *
   * @param stackFrame The stack frame base type.
   */
  void setStackFrame(BaseType stackFrame);

  /**
   * Returns the function prototype associated with this function, if such a function prototype
   * exists.
   *
   * @return The prototype of this function.
   */
  BaseType getPrototype();

  /**
   * Associates the given {@link BaseType prototype} with this function.
   *
   * @param prototype The {@link BaseType} which should be associated with this function.
   */
  void setPrototype(BaseType prototype);
}
