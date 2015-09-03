/*
Copyright 2015 Google Inc. All Rights Reserved.

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
import com.google.security.zynamics.binnavi.APIHelpers.ObjectFinders;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.REIL.InstructionFinders;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.CFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.CGroupNode;
import com.google.security.zynamics.binnavi.disassembly.CNaviViewEdge;
import com.google.security.zynamics.binnavi.disassembly.CNaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.CTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.ViewLoadEvents;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.types.graphs.IDirectedGraph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// / Represents a single view.
/**
 * Views are arbitrary graphs that can be displayed. In most cases views are either Flow graphs and
 * Call graphs that can be shown in com.google.security.zynamics.binnavi. The nodes of a view are
 * not limited to one kind though. A view can have different kinds of nodes and if you work on the
 * nodes of a view you have to be aware of this.
 *
 * Common node types are {@link CodeNode} objects which are the view equivalent of basic blocks and
 * {@link FunctionNode} which are the equivalent of Call graph nodes.
 */
public final class View implements ApiObject<INaviView> {

  /**
   * View container the view belongs to.
   */
  private final ViewContainer viewContainer;

  /**
   * Wrapped internal view object.
   */
  private final INaviView naviView;

  /**
   * Graph of the view.
   */
  private ViewGraph viewGraph;

  /**
   * Node tag manager of the database.
   */
  private final TagManager nodeTagManager;

  /**
   * View tag manager of the database.
   */
  private final TagManager viewTagManager;

  /**
   * Tags the view is tagged with.
   */
  private final List<Tag> viewTags = new ArrayList<Tag>();

  /**
   * Listeners that are notified about changes in the view.
   */
  private final ListenerProvider<IViewListener> viewListeners =
      new ListenerProvider<IViewListener>();

  /**
   * Keeps the API view object synchronized with the internal view object.
   */
  private final InternalViewListener viewListener = new InternalViewListener();

  /**
   * For performance reasons we cache the relation between internal node objects and API node
   * objects.
   */
  private final Map<INaviViewNode, ViewNode> cachedNodes = new HashMap<INaviViewNode, ViewNode>();

  /**
   * For performance reasons we cache the relation between internal edge objects and API edge
   * objects.
   */
  private final Map<INaviEdge, ViewEdge> cachedEdges = new HashMap<INaviEdge, ViewEdge>();

  // / @cond INTERNAL
  /**
   * Creates a new API view object.
   *
   * @param container View container the view belongs to.
   * @param view Wrapped internal view object.
   * @param nodeTagManager Node tag manager of the database.
   * @param viewTagManager View tag manager of the database.
   */
  // / @endcond
  public View(final ViewContainer container, final INaviView view, final TagManager nodeTagManager,
      final TagManager viewTagManager) {
    viewContainer =
        Preconditions.checkNotNull(container, "Error: Container argument can not be null");
    naviView = Preconditions.checkNotNull(view, "Error: View argument can't be null");
    this.nodeTagManager = Preconditions.checkNotNull(nodeTagManager,
        "Error: Node tag manager argument can't be null");
    this.viewTagManager = Preconditions.checkNotNull(viewTagManager,
        "Error: View tag manager argument can't be null");

    for (final CTag tag : view.getConfiguration().getViewTags()) {
      viewTags.add(viewTagManager.getTag(tag));
    }

    if (view.isLoaded()) {
      convertData();
    }

    view.addListener(viewListener);
  }

  /**
   * Assigns the attributes of an internal view node to an API view node.
   *
   * @param node The target node.
   * @param newNode The source node.
   */
  private void adjustAttributes(final ViewNode node, final CNaviViewNode newNode) {
    newNode.setBorderColor(node.getBorderColor());
    newNode.setColor(node.getColor());
  }

  /**
   * Converts an internal view node to an API view node.
   *
   * @param node The node to convert.
   *
   * @return The converted node.
   */
  private ViewNode convert(final INaviViewNode node) {
    if (node instanceof INaviCodeNode) {
      return new CodeNode(View.this, (INaviCodeNode) node, nodeTagManager);
    } else if (node instanceof INaviFunctionNode) {
      final Function function = getFunction(((INaviFunctionNode) node).getFunction());

      return new FunctionNode(View.this, (INaviFunctionNode) node, function, nodeTagManager);
    } else if (node instanceof INaviTextNode) {
      return new TextNode(View.this, (INaviTextNode) node, nodeTagManager);
    } else if (node instanceof INaviGroupNode) {
      return new GroupNode(View.this, (INaviGroupNode) node, nodeTagManager);
    } else {
      throw new IllegalStateException("Error: Unknown node type");
    }
  }

  /**
   * Converts internal view data to API view data.
   */
  private void convertData() {
    final IDirectedGraph<INaviViewNode, INaviEdge> graph = naviView.getGraph();

    final List<ViewNode> blocks = new ArrayList<ViewNode>();
    final List<ViewEdge> edges = new ArrayList<ViewEdge>();

    for (final INaviViewNode block : graph.getNodes()) {
      // ESCA-JAVA0177:
      final ViewNode newBlock = convert(block);

      cachedNodes.put(block, newBlock);

      blocks.add(newBlock);
    }

    for (final INaviViewNode block : graph.getNodes()) {
      if (block.getParentGroup() != null) {
        ((GroupNode) cachedNodes.get(block.getParentGroup())).addNode(cachedNodes.get(block));
      }
    }

    for (final INaviEdge edge : graph.getEdges()) {
      final ViewNode source = cachedNodes.get(edge.getSource());
      final ViewNode target = cachedNodes.get(edge.getTarget());

      final ViewEdge viewEdge = new ViewEdge(edge, source, target);

      edges.add(viewEdge);

      cachedEdges.put(edge, viewEdge);
    }

    viewGraph = new ViewGraph(blocks, edges);
  }

  /**
   * This function was introduced to enable listener functions when the view has been fully
   * initialized, converted. Initially only the view is loaded listener function is enabled to have
   * no listener code perform actions on unitialized variables.
   *
   * @return true if the internal graph has been initialized.
   */
  private boolean isConverted() {
    return viewGraph != null;
  }


  /**
   * TODO(timkornau): This is a ridiculous hack caused on the improper separation between the origin
   * of a function and its context. The problem that led to this hack is this:
   *
   *  1. Create an address space with two modules 2. Open one of the call graphs 3. Insert the other
   * call graph into that view
   *
   *  Result without the hack: Crash because the container of this View object is the module and not
   * the project and therefore the function object can not be found.
   *
   * @param function
   *
   * @return The API object for the given function.
   */
  private Function getFunction(final INaviFunction function) {
    Function apiFunction = viewContainer.getFunction(function);

    if (apiFunction != null) {
      return apiFunction;
    }

    for (final Project project : viewContainer.getDatabase().getProjects()) {
      if (project.isLoaded()) {
        apiFunction = project.getFunction(function);

        if (apiFunction != null) {
          return apiFunction;
        }
      }
    }

    throw new IllegalStateException(String.format(
        "Error: Could not determine API function object for native function object '%s'",
        function.getName()));
  }

  @Override
  public INaviView getNative() {
    return naviView;
  }

  // ! Adds a view listener.
  /**
   * Adds an object that is notified about changes in the view.
   *
   * @param listener The listener object that is notified about changes in the view.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object is already listening on the view.
   */
  public void addListener(final IViewListener listener) {
    viewListeners.addListener(listener);
  }

  // ! Tags the view.
  /**
   * Tags the view with a tag.
   *
   * @param tag The tag used to tag the view.
   *
   * @throws CouldntSaveDataException Thrown if the tag could not be stored in the database.
   */
  public void addTag(final Tag tag) throws CouldntSaveDataException {
    Preconditions.checkNotNull(tag, "Error: Tag argument can't be null");

    try {
      naviView.getConfiguration().tagView(tag.getNative().getObject());
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Closes the view
  /**
   * Closed the view. This operation can be vetoed by other plugins or parts of
   * com.google.security.zynamics.binnavi.
   *
   * @return True, if the view was closed. False, if the close operation was vetoed.
   */
  public boolean close() {
    return naviView.close();
  }

  // ! Creates a new code node.
  /**
   * Creates a code node in the view.
   *
   * @param function The parent function of the code node. This argument is optional can can be
   *        null.
   * @param instructions The instructions that should be displayed in the code node.
   *
   * @return The created code node.
   */
  public CodeNode createCodeNode(final Function function, final List<Instruction> instructions) {
    Preconditions.checkNotNull(instructions, "Error: Instructions argument can't be null");

    final List<INaviInstruction> instructionsList = new ArrayList<INaviInstruction>();

    for (final Instruction instruction : instructions) {
      Preconditions.checkNotNull(instruction, "Error: Instruction list contains a null-element");

      instructionsList.add(instruction.getNative());
    }

    assert !instructions.isEmpty();

    final CCodeNode newCodenode = naviView.getContent().createCodeNode(
        function == null ? null : function.getNative(), instructionsList);

    newCodenode.setColor(ConfigManager.instance().getColorSettings().getBasicBlocksColor());

    return (CodeNode) cachedNodes.get(newCodenode);
  }

  // ! Creates a new edge.
  /**
   * Creates an edge in the view.
   *
   * @param source The source node of the edge.
   * @param target The target node of the edge.
   * @param edgeType The edge type of the edge.
   *
   * @return The created edge.
   */
  public ViewEdge createEdge(final ViewNode source, final ViewNode target,
      final EdgeType edgeType) {
    Preconditions.checkNotNull(source, "Error: Source argument can not be null");
    Preconditions.checkNotNull(target, "Error: Target argument can not be null");
    Preconditions.checkNotNull(edgeType, "Error: Edge type argument can not be null");

    final CNaviViewEdge newEdge =
        naviView.getContent().createEdge(source.getNative(), target.getNative(), edgeType.getNative());

    return cachedEdges.get(newEdge);
  }

  // ! Creates a new function node.
  /**
   * Creates a function node in the view.
   *
   * @param function The function the function node represents.
   *
   * @return The created function node.
   */
  public FunctionNode createFunctionNode(final Function function) {
    Preconditions.checkNotNull(function, "Error: Function argument can't be null");
    Preconditions.checkNotNull(getFunction(function.getNative()),
        "Error: Function does not belong to this container");

    final CFunctionNode functionNode = naviView.getContent().createFunctionNode(function.getNative());

    return (FunctionNode) cachedNodes.get(functionNode);
  }

  // ! Creates a new group node.
  /**
   * Creates a new group node in the view.
   *
   * @param text Text that is shown when the group node is collapsed.
   * @param elements Nodes that belong to the group node.
   *
   * @return The created group node.
   */
  public GroupNode createGroupNode(final String text, final List<ViewNode> elements) {
    Preconditions.checkNotNull(text, "Error: Text argument can not be null");
    Preconditions.checkNotNull(elements, "Error: Elements argument can not be null");

    final List<INaviViewNode> nodes = new ArrayList<INaviViewNode>();

    for (final ViewNode element : elements) {
      Preconditions.checkNotNull(element, "Error: Elements list contains a null-element");

      nodes.add(element.getNative());
    }

    final CGroupNode newGroupNode = naviView.getContent().createGroupNode(nodes);

    // TODO(timkornau): enable the comment setting in the API for group nodes again.

    // newGroupNode.setComment(text);

    return (GroupNode) cachedNodes.get(newGroupNode);
  }

  // ! Clones an existing node.
  /**
   * Creates a new view node by cloning an existing view node.
   *
   * @param node The node to clone.
   *
   * @return The cloned node.
   */
  public ViewNode createNode(final ViewNode node) {
    Preconditions.checkNotNull(node, "Error: Node argument can not be null");

    if (node instanceof CodeNode) {
      final List<INaviInstruction> instructionsList = new ArrayList<INaviInstruction>();

      for (final Instruction instruction : ((CodeNode) node).getInstructions()) {
        Preconditions.checkNotNull(instruction, "Error: Instruction list contains a null-element");

        instructionsList.add(instruction.getNative());
      }

      CCodeNode newNode;

      try {
        newNode = naviView.getContent().createCodeNode(
            ((INaviCodeNode) node.getNative()).getParentFunction(), instructionsList);
      } catch (final MaybeNullException e) {
        newNode = naviView.getContent().createCodeNode(null, instructionsList);
      }

      adjustAttributes(node, newNode);

      return cachedNodes.get(newNode);
    } else if (node instanceof FunctionNode) {
      final CFunctionNode newNode =
          naviView.getContent().createFunctionNode(((INaviFunctionNode) node.getNative()).getFunction());

      adjustAttributes(node, newNode);

      return cachedNodes.get(newNode);
    } else if (node instanceof TextNode) {
      final CTextNode newNode =
          naviView.getContent().createTextNode(((TextNode) node).getComments());

      adjustAttributes(node, newNode);

      return cachedNodes.get(newNode);
    } else if (node instanceof GroupNode) {
      throw new IllegalStateException("Group nodes can not be cloned");
    } else {
      throw new IllegalStateException("Error: Unknown node type");
    }
  }

  // ! Creates a new text node.
  /**
   * creates a new text node.
   *
   * @param comments The list of comments to fill the text node with.
   *
   * @return The newly generated text node.
   */
  public TextNode createTextNode(final ArrayList<IComment> comments) {
    Preconditions.checkNotNull(comments, "Error: Text argument can not be null");

    final CTextNode newTextNode = naviView.getContent().createTextNode(comments);

    return (TextNode) cachedNodes.get(newTextNode);
  }

  // ! Deletes an edge.
  /**
   * Deletes an edge from the view.
   *
   * @param edge The edge to delete.
   */
  public void deleteEdge(final ViewEdge edge) {
    Preconditions.checkNotNull(edge, "Error: Edge argument can't be null");

    naviView.getContent().deleteEdge(edge.getNative());
  }

  // ! Deletes a node.
  /**
   * Deletes a node from the view.
   *
   * @param node The node to delete.
   */
  public void deleteNode(final ViewNode node) {
    Preconditions.checkNotNull(node, "Error: Node argument can't be null");

    naviView.getContent().deleteNode(node.getNative());
  }

  // ! Returns the container of the view.
  /**
   * Returns the container of the view. This can be either a project or afor (final
   * INaviViewListener listener : m_listeners) { try { listener.loadedView(this); catch (final
   * IllegalArgumentException exception) { CUtilityFunctions.logException(exception);
   *
   * module.
   *
   * @return The container of the view.
   */
  public ViewContainer getContainer() {
    return viewContainer;
  }

  // ! Description of the view.
  /**
   * Returns the description of the view.
   *
   * @return The description of the view.
   */
  public String getDescription() {
    return naviView.getConfiguration().getDescription();
  }

  // ! Number of edges.
  /**
   * Returns the number of edges in the view. This function can be called if the view has not yet
   * been loaded.
   *
   * @return The number of edges in the view.
   */
  public int getEdgeCount() {
    return naviView.getEdgeCount();
  }

  // ! Graph of the view.
  /**
   * Returns the graph of the view. This is the visible part of the view that is displayed in the
   * graph windows of com.google.security.zynamics.binnavi.
   *
   * @return The graph of the view.
   *
   * @throws IllegalStateException Thrown if the view is not loaded.
   */
  public ViewGraph getGraph() {
    if (!isLoaded()) {
      throw new IllegalStateException("Error: View is not loaded");
    }

    return viewGraph;
  }

  // ! Graph type of the view.
  /**
   * Returns the type of the view graph. The graph of a view can either be a Flow graph (if all
   * nodes of the graph are of type {@link CodeNode}), a Call graph (if all nodes of the graph are
   * of type {@link FunctionNode}), or a mixed graph (if the graph contains arbitrarily mixed
   * nodes).
   *
   * @return The type of the graph.
   */
  public GraphType getGraphType() {
    return GraphType.convert(naviView.getGraphType());
  }

  // ! Name of the view.
  /**
   * Returns the name of the view.
   *
   * @return The name of the view.
   */
  public String getName() {
    return naviView.getName();
  }

  // ! Number of nodes.
  /**
   * Returns the number of nodes in the view. This function can be called if the view has not yet
   * been loaded.
   *
   * @return The number of nodes in the view.
   */
  public int getNodeCount() {
    return naviView.getNodeCount();
  }

  // ! REIL code of the view.
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
    try {
      return new ReilFunction(naviView.getContent().getReilCode());
    } catch (final com.google.security.zynamics.reil.translators.InternalTranslationException e) {
      throw new InternalTranslationException(e,
          InstructionFinders.findInstruction(this, e.getInstruction()));
    }
  }

  // ! Returns the tags of the view.
  /**
   * Returns all tags that are currently associated with the view.
   *
   * @return The tags of the view.
   */
  public List<Tag> getTags() {
    return new ArrayList<Tag>(viewTags);
  }

  // ! Type of the view.
  /**
   * Returns the type of the view. A view can be either native (i.e. an immutable view created by
   * the importer) or non-native (i.e. a mutable view created by the user).
   *
   * @return The type of the view.
   */
  public ViewType getType() {
    return ViewType.convert(naviView.getType());
  }

  // ! Load state of the view.
  /**
   * Returns a flag that indicates whether the view data has been loaded from the database.
   *
   * @return True, if the view has been loaded. False, otherwise.
   */
  public boolean isLoaded() {
    return naviView.isLoaded();
  }

  // ! Loads the view.
  /**
   * Loads the view data from the database.
   *
   * @throws IllegalStateException Thrown if the view is already loaded.
   * @throws CouldntLoadDataException Thrown if the data could not be loaded from the database.
   * @throws PartialLoadException Thrown if the view could not be loaded because not all involved
   *         modules could be loaded.
   */
  public void load() throws CouldntLoadDataException, PartialLoadException {
    if (isLoaded()) {
      return;
    }

    try {
      naviView.load();
    } catch (com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException | LoadCancelledException e) {
      throw new CouldntLoadDataException(e);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException e) {
      throw new PartialLoadException(e);
    } 
  }

  // ! Removes a view listener.
  /**
   * Removes a listener object from the view.
   *
   * @param listener The listener object to remove from the view.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object was not listening on the database.
   */
  public void removeListener(final IViewListener listener) {
    viewListeners.removeListener(listener);
  }

  // ! Untags the view.
  /**
   * Removes a tag from the view.
   *
   * @param tag The tag to remove from the view.
   *
   * @throws CouldntSaveDataException Thrown if the tag could not be removed from the view.
   */
  public void removeTag(final Tag tag) throws CouldntSaveDataException {
    try {
      naviView.getConfiguration().untagView(tag.getNative().getObject());
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Saves the view.
  /**
   * Saves the view to the database. Please note that only non-native views can be saved using this
   * function.
   *
   * @throws CouldntSaveDataException Thrown if the view could not be saved.
   */
  public void save() throws CouldntSaveDataException {
    try {
      naviView.save();
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Changes the view description.
  /**
   * Changes the description of the view.
   *
   * @param description The new description of the view.
   *
   * @throws CouldntSaveDataException Thrown if the new description could not be saved to the
   *         database
   */
  public void setDescription(final String description) throws CouldntSaveDataException {
    try {
      naviView.getConfiguration().setDescription(description);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Changes the view name.
  /**
   * Changes the name of the view.
   *
   * @param name The new name of the view.
   *
   * @throws CouldntSaveDataException Thrown if the new name could not be saved to the database
   */
  public void setName(final String name) throws CouldntSaveDataException {
    try {
      naviView.getConfiguration().setName(name);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! String representation of the view.
  /**
   * Returns the string representation of the view.
   *
   * @return The string representation of the view.
   */
  @Override
  public String toString() {
    return String.format("View '%s'", getName());
  }

  /**
   * Keeps the API view object synchronized with the internal view object.
   */
  private class InternalViewListener implements
      com.google.security.zynamics.binnavi.disassembly.views.INaviViewListener {

    @Override
    public void addedEdge(final INaviView view, final INaviEdge edge) {
      if (!isConverted()) {
        return;
      }

      final ViewNode source = cachedNodes.get(edge.getSource());
      final ViewNode target = cachedNodes.get(edge.getTarget());

      final ViewEdge newEdge = new ViewEdge(edge, source, target);
      viewGraph.addEdge(newEdge);
      cachedEdges.put(edge, newEdge);

      for (final IViewListener listener : viewListeners) {
        // ESCA-JAVA0166:
        try {
          listener.addedEdge(View.this, newEdge);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void addedNode(final INaviView view, final INaviViewNode node) {
      if (!isConverted()) {
        return;
      }

      final ViewNode newNode = convert(node);

      if (node instanceof INaviGroupNode) {
        final INaviGroupNode gnode = (INaviGroupNode) node;

        for (final INaviViewNode element : gnode.getElements()) {
          ((GroupNode) newNode).addNode(cachedNodes.get(element));
        }
      }

      cachedNodes.put(node, newNode);
      viewGraph.addNode(newNode);

      for (final IViewListener listener : viewListeners) {
        try {
          listener.addedNode(View.this, newNode);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void addedNodes(final INaviView view, final Collection<INaviViewNode> nodes) {
      // TODO(timkornau): forward this functionality to the API.
    }

    @Override
    public void appendedGlobalEdgeComment(final INaviView view, final INaviEdge edge) {
      // TODO(timkornau): forward this functionality to the API.
    }

    @Override
    public void appendedLocalEdgeComment(final INaviView view, final INaviEdge edge) {
      // TODO(timkornau): forward this functionality to the API.
    }

    @Override
    public void appendedLocalFunctionNodeComment(final INaviView view, final INaviFunctionNode node,
        final IComment comment) {
      if (!isConverted()) {
        return;
      }
      final FunctionNode viewNode = (FunctionNode) cachedNodes.get(node);
      for (final IFunctionNodeListener listener : viewNode.getFunctionListeners()) {
        try {
          listener.appendedComment(viewNode, comment);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedBorderColor(final INaviView view, final IViewNode<?> node,
        final Color color) {
      if (!isConverted()) {
        return;
      }
      final ViewNode viewNode = cachedNodes.get(node);
      for (final IViewNodeListener listener : viewNode.getListeners()) {
        try {
          listener.changedBorderColor(viewNode, color);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedColor(final INaviView view, final CNaviViewEdge edge, final Color color) {
      final ViewEdge viewEdge = cachedEdges.get(edge);
      if (!isConverted()) {
        return;
      }
      for (final IViewEdgeListener listener : viewEdge.getListeners()) {
        try {
          listener.changedColor(viewEdge, color);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedColor(final INaviView view, final IViewNode<?> node, final Color color) {
      final ViewNode viewNode = cachedNodes.get(node);
      if (!isConverted()) {
        return;
      }
      for (final IViewNodeListener listener : viewNode.getListeners()) {
        try {
          listener.changedColor(viewNode, color);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedDescription(final INaviView view, final String description) {
      if (!isConverted()) {
        return;
      }
      for (final IViewListener listener : viewListeners) {
        try {
          listener.changedDescription(View.this, description);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedGraphType(final INaviView view,
        final com.google.security.zynamics.zylib.disassembly.GraphType type,
        final com.google.security.zynamics.zylib.disassembly.GraphType oldType) {
      if (!isConverted()) {
        return;
      }
      for (final IViewListener listener : viewListeners) {
        try {
          listener.changedGraphType(View.this, GraphType.convert(type));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedModificationDate(final INaviView view, final Date modificationDate) {
      for (final IViewListener listener : viewListeners) {
        if (!isConverted()) {
          return;
        }
        try {
          listener.changedModificationDate(View.this, modificationDate);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedModificationState(final INaviView view, final boolean value) {
      // TODO(timkornau): forward this functionality to the API.
    }

    @Override
    public void changedName(final INaviView view, final String name) {
      for (final IViewListener listener : viewListeners) {
        if (!isConverted()) {
          return;
        }
        try {
          listener.changedName(View.this, name);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedParentGroup(final INaviView view, final INaviViewNode node,
        final INaviGroupNode groupNode) {
      if (!isConverted()) {
        return;
      }
      final ViewNode viewNode = cachedNodes.get(node);
      for (final IViewNodeListener listener : viewNode.getListeners()) {
        try {
          if (groupNode == null) {
            listener.changedParentGroup(viewNode, null);
          } else {
            listener.changedParentGroup(viewNode,
                (GroupNode) ObjectFinders.getObject(groupNode, viewGraph.getNodes()));
          }
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedSelection(final INaviView view, final IViewNode<?> node,
        final boolean selected) {
      if (!isConverted()) {
        return;
      }
      final ViewNode viewNode = cachedNodes.get(node);
      for (final IViewNodeListener listener : viewNode.getListeners()) {
        try {
          listener.changedSelection(viewNode, selected);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedStarState(final INaviView view, final boolean isStared) {
      // TODO(timkornau): forward this functionality to the API.
    }

    @Override
    public void changedVisibility(final INaviView view, final IViewEdge<?> edge) {
      if (!isConverted()) {
        return;
      }
      final ViewEdge viewEdge = cachedEdges.get(edge);
      for (final IViewEdgeListener listener : viewEdge.getListeners()) {
        try {
          listener.changedVisibility(viewEdge, viewEdge.isVisible());
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedVisibility(final INaviView view, final IViewNode<?> node,
        final boolean visible) {
      if (!isConverted()) {
        return;
      }
      final ViewNode viewNode = cachedNodes.get(node);
      for (final IViewNodeListener listener : viewNode.getListeners()) {
        try {
          listener.changedVisibility(viewNode, visible);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void closedView(final INaviView view,
        final IDirectedGraph<INaviViewNode, INaviEdge> oldGraph) {
      if (!isConverted()) {
        return;
      }
      viewGraph = null;
      cachedEdges.clear();
      cachedNodes.clear();

      for (final IViewListener listener : viewListeners) {
        try {
          listener.closedView(View.this);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public boolean closingView(final INaviView view) {
      if (!isConverted()) {
        return false;
      }
      for (final IViewListener listener : viewListeners) {
        try {
          if (!listener.closingView(View.this)) {
            return false;
          }
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }

      return true;
    }

    @Override
    public void deletedEdge(final INaviView view, final INaviEdge edge) {
      if (!isConverted()) {
        return;
      }
      final ViewEdge oldEdge = cachedEdges.get(edge);
      viewGraph.removeEdge(oldEdge);
      cachedEdges.remove(oldEdge);
      for (final IViewListener listener : viewListeners) {
        try {
          listener.deletedEdge(View.this, oldEdge);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void deletedGlobalEdgeComment(final INaviView view, final INaviEdge edge) {
      // TODO(timkornau): forward this functionality to the API.
    }

    @Override
    public void deletedLocalEdgeComment(final INaviView view, final INaviEdge edge) {
      // TODO(timkornau): forward this functionality to the API.
    }

    @Override
    public void deletedLocalFunctionNodeComment(final INaviView view, final INaviFunctionNode node,
        final IComment comment) {
      // TODO(timkornau): forward this functionality to the API.
    }

    @Override
    public void deletedNode(final INaviView view, final INaviViewNode node) {
      if (node instanceof INaviGroupNode || !isConverted()) {
        return;
      }

      final ViewNode oldNode = cachedNodes.get(node);
      viewGraph.removeNode(oldNode);
      cachedNodes.remove(node);
      for (final IViewListener listener : viewListeners) {
        try {
          listener.deletedNode(View.this, oldNode);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void deletedNodes(final INaviView view, final Collection<INaviViewNode> nodes) {
      for (final INaviViewNode node : nodes) {
        if (!isConverted()) {
          return;
        }
        final ViewNode oldNode = cachedNodes.get(node);
        viewGraph.removeNode(oldNode);
        cachedNodes.remove(node);
        for (final IViewListener listener : viewListeners) {
          try {
            listener.deletedNode(View.this, oldNode);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }

    @Override
    public void editedGlobalEdgeComment(final INaviView view, final INaviEdge edge) {
      // TODO(timkornau): forward this functionality to the API.
    }

    @Override
    public void editedLocalEdgeComment(final INaviView view, final INaviEdge edge) {
      // TODO(timkornau): forward this functionality to the API.
    }

    @Override
    public void editedLocalFunctionNodeComment(final INaviView view, final INaviFunctionNode node,
        final IComment comment) {
      // TODO(timkornau): forward this functionality to the API.
    }

    @Override
    public void heightChanged(final INaviView view, final IViewNode<?> node, final double height) {
      // Don't pass this to the API
    }

    @Override
    public void initializedGlobalEdgeComment(final INaviView view, final INaviEdge edge) {
      // TODO(timkornau): forward this functionality to the API.
    }

    @Override
    public void initializedLoalFunctionNodeComment(final INaviView view,
        final INaviFunctionNode node, final ArrayList<IComment> comments) {
      // TODO(timkornau): forward this functionality to the API.
    }

    @Override
    public void initializedLocalEdgeComment(final INaviView view, final INaviEdge edge) {
      // TODO(timkornau): forward this functionality to the API.
    }

    @Override
    public void loadedView(final INaviView view) {
      convertData();
    }

    @Override
    public boolean loading(final ViewLoadEvents event, final int counter) {
      return true;
    }

    @Override
    public void savedView(final INaviView view) {
      // TODO(timkornau): forward this functionality to the API.
    }

    @Override
    public void taggedNode(final INaviView view, final INaviViewNode node, final CTag tag) {
      if (!isConverted()) {
        return;
      }
      final Tag newTag = nodeTagManager.getTag(tag);
      final ViewNode viewNode = cachedNodes.get(node);
      for (final IViewNodeListener listener : viewNode.getListeners()) {
        try {
          listener.addedTag(viewNode, newTag);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void taggedView(final INaviView view, final CTag tag) {
      if (!isConverted()) {
        return;
      }
      final Tag newTag = viewTagManager.getTag(tag);
      viewTags.add(newTag);
      for (final IViewListener listener : viewListeners) {
        try {
          listener.taggedView(View.this, newTag);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void untaggedNodes(final INaviView view, final INaviViewNode node,
        final List<CTag> tags) {
      if (!isConverted()) {
        return;
      }
      for (final CTag tag : tags) {
        final Tag removedTag = nodeTagManager.getTag(tag);
        final ViewNode viewNode = cachedNodes.get(node);
        for (final IViewNodeListener listener : viewNode.getListeners()) {
          try {
            listener.removedTag(viewNode, removedTag);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }

    @Override
    public void untaggedView(final INaviView view, final CTag tag) {
      if (!isConverted()) {
        return;
      }
      final Tag removedTag = viewTagManager.getTag(tag);
      viewTags.remove(removedTag);
      for (final IViewListener listener : viewListeners) {
        try {
          listener.untaggedView(View.this, removedTag);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void widthChanged(final INaviView view, final IViewNode<?> node, final double height) {
      // Don't pass this to the API
    }

    @Override
    public void xposChanged(final INaviView view, final IViewNode<?> node, final double xpos) {
      if (!isConverted()) {
        return;
      }
      final ViewNode viewNode = cachedNodes.get(node);
      for (final IViewNodeListener listener : viewNode.getListeners()) {
        try {
          listener.changedX(viewNode, xpos);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void yposChanged(final INaviView view, final IViewNode<?> node, final double ypos) {
      if (!isConverted()) {
        return;
      }
      final ViewNode viewNode = cachedNodes.get(node);
      for (final IViewNodeListener listener : viewNode.getListeners()) {
        try {
          listener.changedY(viewNode, ypos);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }
}
