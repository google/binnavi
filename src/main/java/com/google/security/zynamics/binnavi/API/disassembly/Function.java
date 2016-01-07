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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.API.reil.InternalTranslationException;
import com.google.security.zynamics.binnavi.API.reil.ReilFunction;
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.REIL.InstructionFinders;
import com.google.security.zynamics.binnavi.disassembly.IBlockEdge;
import com.google.security.zynamics.binnavi.disassembly.IBlockNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.reil.translators.ReilTranslator;
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.zylib.disassembly.IFunction;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.graphs.DirectedGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


// / Represents a single function.
/**
 * Function objects describe original functions from imported modules. This includes things like the
 * name or the description of the function as well as the flowgraph of the function.
 *
 *  The structure of functions is immutable. That means the flowgraph of functions can not be
 * modified. To modify the flowgraph of a function it is necessary to convert it to a {@link View}
 * first.
 */
public final class Function implements ApiObject<INaviFunction> {
  /**
   * The wrapped internal function object.
   */
  private final INaviFunction m_function;

  /**
   * The module the function belongs to.
   */
  private final Module m_module;

  /**
   * The flow graph of the object.
   */
  private FlowGraph m_graph;

  /**
   * Listeners that are notified about changes in the function.
   */
  private final ListenerProvider<IFunctionListener> m_listeners =
      new ListenerProvider<IFunctionListener>();

  /**
   * Keeps the API function object synchronized with the internal function object.
   */
  private final InternalListener m_listener = new InternalListener();

  /**
   * Used to translate the code of the function to REIL.
   */
  private static final ReilTranslator<INaviInstruction> m_translator =
      new ReilTranslator<INaviInstruction>();

  /**
   * The REIL code of the function.
   */
  private ReilFunction m_reilGraph = null;

  // / @cond INTERNAL
  /**
   * Creates a new API function object.
   *
   * @param module The module the function belongs to.
   * @param function The wrapped internal function object.
   */
  // / @endcond
  public Function(final Module module, final INaviFunction function) {
    m_module = Preconditions.checkNotNull(module, "Error: Module argument can not be null");
    m_function = Preconditions.checkNotNull(function, "Error: Function argument can't be null");

    if (function.isLoaded()) {
      convertData();
    }

    function.addListener(m_listener);
  }

  /**
   * Converts internal function data to API function data.
   */
  private void convertData() {
    final DirectedGraph<IBlockNode, IBlockEdge> graph = m_function.getGraph();

    final List<BasicBlock> blocks = new ArrayList<BasicBlock>();
    final List<BlockEdge> edges = new ArrayList<BlockEdge>();

    final HashMap<IBlockNode, BasicBlock> blockMap = new HashMap<IBlockNode, BasicBlock>();

    for (final IBlockNode block : graph.getNodes()) {
      final BasicBlock newBlock = new BasicBlock(block, this);

      blockMap.put(block, newBlock);

      blocks.add(newBlock);
    }

    for (final IBlockEdge edge : graph.getEdges()) {
      final BasicBlock source = blockMap.get(edge.getSource());
      final BasicBlock target = blockMap.get(edge.getTarget());

      edges.add(new BlockEdge(edge, source, target));
    }

    m_graph = new FlowGraph(blocks, edges);
  }

  @Override
  public INaviFunction getNative() {
    return m_function;
  }

  // ! Adds a function listener.
  /**
   * Adds an object that is notified about changes in the function.
   *
   * @param listener The listener object that is notified about changes in the function.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object is already listening on the
   *         function.
   */
  public void addListener(final IFunctionListener listener) {
    m_listeners.addListener(listener);
  }

  // ! Appends a function comment.
  /**
   * Appends a global function comment to the function.
   *
   * @param comment The string of the comment to get appended.
   *
   * @throws CouldntSaveDataException if the comment could not be stored in the database.
   */
  public List<IComment> appendComment(final String comment) throws CouldntSaveDataException,
      CouldntLoadDataException {
    try {
      return m_function.appendGlobalComment(comment);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException exception) {
      throw new CouldntSaveDataException(exception);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException exception) {
      throw new CouldntLoadDataException(exception);
    }
  }

  // ! Edit a function comment.
  /**
   * Edit a function comment.
   *
   * @param comment The {@link IComment} which is edited.
   * @param newComment The {@link String} to replace the current comment text.
   * @return The edited comment.
   *
   * @throws CouldntSaveDataException if the edit operation failed.
   */
  public IComment editComment(final IComment comment, final String newComment)
      throws CouldntSaveDataException {
    try {
      return m_function.editGlobalComment(comment, newComment);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException exception) {
      throw new CouldntSaveDataException(exception);
    }
  }

  // ! Delete a function comment.
  /**
   * Delete a function comment.
   *
   * @param comment The {@link IComment} to delete.
   *
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException
   */
  public void deleteComment(final IComment comment)
      throws com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException {
    try {
      m_function.deleteGlobalComment(comment);
    } catch (final CouldntDeleteException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException(
          exception);
    }
  }

  /**
   * Unloads the function.
   */
  public void close() {
    m_graph = null;
    m_reilGraph = null;
    m_function.close();
    m_function.removeListener(m_listener);
  }

  // ! The address of the function.
  /**
   * Returns the start address of the function.
   *
   * @return The start address of the function.
   */
  public Address getAddress() {
    return new Address(m_function.getAddress().toBigInteger());
  }

  // ! Number of basic blocks in the function.
  /**
   * Returns the number of basic blocks in the function. This function can be called if the function
   * has not yet been loaded.
   *
   * @return The number of basic blocks in the function.
   */
  public int getBlockCount() {
    return m_function.getBasicBlockCount();
  }

  // ! Comment of the function.
  /**
   * Returns the function comment. This is the comment that is shown as the global comment in views
   * that show function nodes.
   *
   * @return The function comment.
   */
  public List<IComment> getComment() {
    return m_function.getGlobalComment();
  }

  // ! Description of the function.
  /**
   * Returns the description of the function. This is the string that is shown as the description of
   * the function in the main window.
   *
   * @return The function description.
   */
  public String getDescription() {
    return m_function.getDescription();
  }

  // ! Number of edges in the function.
  /**
   * Returns the number of edges in the function. This function can be called if the function has
   * not yet been loaded.
   *
   * @return The number of edges in the view.
   */
  public int getEdgeCount() {
    return m_function.getEdgeCount();
  }

  // ! The graph of the function.
  /**
   * Returns the flowgraph of the function. This graph contains the basic blocks of the function and
   * edges that show how control flow can propagate through the function.
   *
   * @return The flowgraph of the function.
   *
   * @throws IllegalStateException Thrown if the function is not loaded.
   */
  public FlowGraph getGraph() {
    if (!isLoaded()) {
      throw new IllegalArgumentException("Error: The function is not loaded");
    }

    return m_graph;
  }

  // ! The module the function belongs to.
  /**
   * Returns the module the function belongs to.
   *
   * @return The module the function belongs to.
   */
  public Module getModule() {
    return m_module;
  }

  // ! The name of the function.
  /**
   * Returns the name of the function.
   *
   * @return The name of the function.
   */
  public String getName() {
    return m_function.getName();
  }

  // ! REIL code of the function.
  /**
   * Converts the View to REIL code. This function can only be used if the view has already been
   * loaded.
   *
   *  Using this function over manual translation via ReilTranslator has the advantage that REIL
   * translation results are automatically cached. Subsequent uses of this function requires no
   * additional re-translation of the view provided that nothing relevant (like added/removed code
   * nodes) changed.
   *
   * @return The REIL representation of the view.
   *
   * @throws InternalTranslationException Thrown if the REIL translation failed.
   */
  public ReilFunction getReilCode() throws InternalTranslationException {
    if (!isLoaded()) {
      throw new IllegalStateException("Error: Function must be loaded first");
    }

    if (m_reilGraph == null) {
      try {
        m_reilGraph =
            new ReilFunction(m_translator.translate(new StandardEnvironment(), m_function));
      } catch (final com.google.security.zynamics.reil.translators.InternalTranslationException e) {
        throw new InternalTranslationException(e,
            InstructionFinders.findInstruction(this, e.getInstruction()));
      }
    }

    return m_reilGraph;
  }

  // ! Returns the type of the function.
  /**
   * Returns the type of the function.
   *
   * @return The type of the function.
   */
  public FunctionType getType() {
    return FunctionType.convert(m_function.getType());
  }

  // ! Checks if the function is loaded.
  /**
   * Returns a flag that indicates whether the function is loaded.
   *
   * @return True, if the function is loaded. False, otherwise.
   */
  public boolean isLoaded() {
    return m_function.isLoaded();
  }

  // ! Loads the function.
  /**
   * Loads the function data from the database.
   *
   * @throws IllegalStateException Thrown if the function is already loaded.
   * @throws CouldntLoadDataException Thrown if the function data could not be loaded from the
   *         database.
   */
  public void load() throws CouldntLoadDataException {
    if (isLoaded()) {
      return;
    }

    try {
      m_function.load();
      convertData();
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException e) {
      throw new CouldntLoadDataException(e);
    }
  }

  /**
   * Removes a listener object from the function.
   *
   * @param listener The listener object to remove from the function.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object was not listening on the function.
   */
  public void removeListener(final IFunctionListener listener) {
    m_listeners.removeListener(listener);
  }

  // ! Changes the function comment.
  /**
   * Changes the comment of the function. This is the comment that is shown as the global comment in
   * views that show function nodes.
   *
   * @param comment The new value of the comment.
   *
   * @throws IllegalArgumentException Thrown if the comment argument is null.
   * @throws CouldntSaveDataException Thrown if the comment could not be changed.
   */
  public void setComment(final ArrayList<IComment> comment) throws CouldntSaveDataException {
    try {
      m_function.initializeGlobalComment(comment);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Changes the function description.
  /**
   * Changes the description of the function.
   *
   * @param description The new value of the description.
   *
   * @throws IllegalArgumentException Thrown if the description argument is null.
   * @throws CouldntSaveDataException Thrown if the description could not be changed.
   */
  public void setDescription(final String description) throws CouldntSaveDataException {
    try {
      m_function.setDescription(description);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Changes the function name.
  /**
   * Changes the name of the function.
   *
   * @param name The new value of the name.
   *
   * @throws IllegalArgumentException Thrown if the name argument is null.
   * @throws CouldntSaveDataException Thrown if the name could not be changed.
   */
  public void setName(final String name) throws CouldntSaveDataException {
    try {
      m_function.setName(name);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Printable representation of the function.
  /**
   * Returns the string representation of the function.
   *
   * @return The string representation of the function.
   */
  @Override
  public String toString() {
    return String.format("%s %s", getAddress().toHexString(), getName());
  }

  /**
   * Keeps the API function object synchronized with the internal function object.
   */
  private class InternalListener implements
      com.google.security.zynamics.zylib.disassembly.IFunctionListener<IComment> {
    @Override
    public void appendedComment(final IFunction function, final IComment comment) {
      for (final IFunctionListener listener : m_listeners) {
        try {
          listener.appendedComment(Function.this, comment);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedDescription(final IFunction function, final String description) {
      for (final IFunctionListener listener : m_listeners) {
        try {
          listener.changedDescription(Function.this, description);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedName(final IFunction function, final String name) {
      for (final IFunctionListener listener : m_listeners) {
        try {
          listener.changedName(Function.this, name);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedForwardedFunction(final IFunction function) {}

    @Override
    public void closed(final IFunction function) {
      m_graph = null;
      m_reilGraph = null;

      for (final IFunctionListener listener : m_listeners) {
        try {
          listener.closedFunction(Function.this);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void deletedComment(final IFunction function, final IComment comment) {
      for (final IFunctionListener listener : m_listeners) {
        try {
          listener.deletedComment(Function.this, comment);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void editedComment(final IFunction function, final IComment comment) {
      for (final IFunctionListener listener : m_listeners) {
        try {
          listener.editedComment(Function.this, comment);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void initializedComment(final IFunction function, final List<IComment> comment) {
      for (final IFunctionListener listener : m_listeners) {
        try {
          listener.initializedComment(Function.this, comment);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }

    }

    @Override
    public void loadedFunction(final IFunction function) {
      convertData();

      for (final IFunctionListener listener : m_listeners) {
        try {
          listener.loadedFunction(Function.this);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }
}
