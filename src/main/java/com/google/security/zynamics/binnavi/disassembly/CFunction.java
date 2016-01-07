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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CFunctionHelpers;
import com.google.security.zynamics.binnavi.Gui.Users.CUserManager;
import com.google.security.zynamics.binnavi.Gui.WindowManager.CWindowManager;
import com.google.security.zynamics.binnavi.disassembly.functions.FunctionManager;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.FunctionType;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.ICodeEdge;
import com.google.security.zynamics.zylib.disassembly.IFunctionListener;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.graphs.DirectedGraph;
import com.google.security.zynamics.zylib.types.graphs.MutableDirectedGraph;
import com.google.security.zynamics.zylib.types.lists.FilledList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a native function from the original target file. Functions are raw data. This means
 * their structure can not be changed. Only their name and description can be modified.
 */
public final class CFunction implements INaviFunction {
  /**
   * Module the function belongs to.
   */
  private final INaviModule module;

  /**
   * The view that backs the function.
   */
  private final INaviView view;

  /**
   * Address of the function.
   */
  private final IAddress address;

  /**
   * Name of the function.
   */
  private String name;

  /**
   * The original name of the function.
   */
  private final String originalName;

  /**
   * Description of the function.
   */
  private String description;

  /**
   * Type of the function.
   */
  private final FunctionType type;

  /**
   * Listeners that are notified about changes in the function.
   */
  private final ListenerProvider<IFunctionListener<IComment>> functionListeners =
      new ListenerProvider<>();

  /**
   * Directed graph that contains the basic blocks and edges of the graph.
   */
  private DirectedGraph<IBlockNode, IBlockEdge> functionGraph;

  /**
   * The number of incoming calls to this function.
   */
  private final int indegree;

  /**
   * The number of outgoing calls from this function.
   */
  private final int outdegree;

  /**
   * The number of basic blocks of the function. This variable is only used before the function is
   * loaded.
   */
  private final int blockCount;

  /**
   * The number of edges in the function. This variable is only used before the function is loaded.
   */
  private final int edgeCount;

  /**
   * SQL provider that is used to communicate with the database where the function is stored.
   */
  private final SQLProvider provider;

  /**
   * Address of the function this function is forwarded to.
   */
  private IAddress forwardedFunctionAddress;

  /**
   * ID of the module this function is forwarded to.
   */
  private int forwardedFunctionModuleId;

  /**
   * Name of the module this function belongs to. This is useful for dynamically linked functions
   * because otherwise you would not know from where these functions are imported.
   */
  private final String forwardedFunctionModuleName;

  /**
   * Keeps this function updated when global comments change.
   */
  private final CommentListener commentListener = new InternalCommentListener();

  /**
   * The optional stack frame of the function.
   */
  private BaseType stackFrame;

  /**
   * The optional prototype of the function.
   */
  private BaseType prototype;

  /**
   * Creates a new function object that represents a function from the project.
   *
   * @param module The module the function belongs to.
   * @param view The view that backs the function.
   * @param address The address of the function.
   * @param name The name of the function.
   * @param originalName The original name of the function.
   * @param description The comment associated with the function.
   * @param indegree Number of functions calling that function.
   * @param outdegree Number of functions called by that function.
   * @param blockCount Number of blocks in the function.
   * @param edgeCount Number of edges in the function.
   * @param type The type of the function.
   * @param forwardedFunctionModuleName Name of the module this function is forwarded to.
   * @param forwardedFunctionModuleId ID of the module this function is forwarded to.
   * @param forwardedFunctionAddress Function this function is forwarded to.
   * @param prototype The id of the base type which describes this functions prototype.
   * @param provider The SQL provider that is used to load more information about the function.
   * @throws IllegalArgumentException Thrown if any of the arguments is null.
   */
  public CFunction(final INaviModule module,
      final INaviView view,
      final IAddress address,
      final String name,
      final String originalName,
      final String description,
      final int indegree,
      final int outdegree,
      final int blockCount,
      final int edgeCount,
      final FunctionType type,
      final String forwardedFunctionModuleName,
      final int forwardedFunctionModuleId,
      final IAddress forwardedFunctionAddress,
      final BaseType stackFrame,
      final BaseType prototype,
      final SQLProvider provider) {

    this.module = Preconditions.checkNotNull(module, "IE00069: Module can not be null");
    this.view = Preconditions.checkNotNull(view, "IE00268: View argument can not be null");
    this.address = Preconditions.checkNotNull(address, "IE00070: Function address can not be null");
    this.name = name;
    this.originalName =
        Preconditions.checkNotNull(originalName, "IE00642: OriginalName argument can not be null");
    this.description = description;
    Preconditions.checkArgument(indegree >= 0, "IE00643: Indegree argument can not be smaller 0");
    this.indegree = indegree;
    Preconditions.checkArgument(outdegree >= 0, "IE01102: Outdegree argument can not be smaller 0");
    this.outdegree = outdegree;
    Preconditions.checkArgument(edgeCount >= 0,
        "IE01103: Edge count argument can not be smaller 0");
    this.edgeCount = edgeCount;
    Preconditions.checkArgument(blockCount >= 0,
        "IE02175: Block count argument can not be smaller 0");
    this.blockCount = blockCount;
    this.type = Preconditions.checkNotNull(type, "IE00073: Function type can not be null");
    this.provider = Preconditions.checkNotNull(provider, "IE00074: SQL provider can not be null");

    this.forwardedFunctionModuleId = forwardedFunctionModuleId;
    this.forwardedFunctionAddress = forwardedFunctionAddress;
    this.forwardedFunctionModuleName = forwardedFunctionModuleName;
    this.stackFrame = stackFrame;
    this.prototype = prototype;

    CommentManager.get(provider).addListener(commentListener);
    FunctionManager.get(provider).putFunction(this);
  }

  /**
   * Converts a view graph to a function graph.
   *
   * @param viewGraph The graph to convert.
   *
   * @return The converted graph.
   */
  private DirectedGraph<IBlockNode, IBlockEdge> convert(
      final MutableDirectedGraph<INaviViewNode, INaviEdge> viewGraph) {
    final Map<INaviViewNode, IBlockNode> blockMap = new LinkedHashMap<INaviViewNode, IBlockNode>();
    final List<IBlockEdge> edges = new FilledList<IBlockEdge>();

    for (final INaviViewNode viewNode : viewGraph) {
      if (viewNode instanceof INaviCodeNode) {
        final INaviCodeNode cnode = (INaviCodeNode) viewNode;
        final CBasicBlock block =
            new CBasicBlock(1, "", Lists.newArrayList(cnode.getInstructions()));
        final CBlockNode blockNode = new CBlockNode(block);

        blockMap.put(cnode, blockNode);
      }
    }

    for (final INaviEdge viewEdge : viewGraph.getEdges()) {
      final INaviViewNode source = viewEdge.getSource();
      final INaviViewNode target = viewEdge.getTarget();

      edges.add(new CFunctionEdge(blockMap.get(source), blockMap.get(target), viewEdge.getType()));
    }

    return new DirectedGraph<IBlockNode, IBlockEdge>(new ArrayList<IBlockNode>(blockMap.values()),
        edges);
  }

  /**
   * Adds a listener to the function object that is notified about changes in the function object.
   *
   * @param listener The listener object to add.
   */
  @Override
  public void addListener(final IFunctionListener<IComment> listener) {
    functionListeners.addListener(listener);
  }

  @Override
  public List<IComment> appendGlobalComment(final String commentText)
      throws CouldntSaveDataException, CouldntLoadDataException {
    Preconditions.checkNotNull(commentText, "IE02531: comment argument can not be null");
    return CommentManager.get(provider).appendGlobalFunctionComment(this, commentText);
  }

  @Override
  public boolean close() {
    if (!isLoaded()) {
      throw new IllegalStateException("IE00075: Function is not loaded");
    }

    for (final IBlockNode block : functionGraph.getNodes()) {
      for (final INaviInstruction instruction : block.getInstructions()) {
        instruction.close();
      }
    }

    functionGraph = null;

    for (final IFunctionListener<IComment> listener : functionListeners) {
      listener.closed(this);
    }

    if (view.isLoaded() && !CWindowManager.instance().isOpen(view)) {
      view.close();
    }

    CommentManager.get(provider).unloadGlobalFunctionComment(this, getGlobalComment());
    CommentManager.get(provider).removeListener(commentListener);

    return true;
  }

  @Override
  public void deleteGlobalComment(final IComment comment) throws CouldntDeleteException {
    Preconditions.checkNotNull(comment, "IE02532: New comment can not be null");
    CommentManager.get(provider).deleteGlobalFunctionComment(this, comment);
  }

  @Override
  public IComment editGlobalComment(final IComment comment, final String commentText)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(comment, "IE02533: comment argument can not be null");
    return CommentManager.get(provider).editGlobalFunctionComment(this, comment, commentText);
  }

  @Override
  public IAddress getAddress() {
    return address;
  }

  @Override
  public int getBasicBlockCount() {
    return isLoaded() ? functionGraph.nodeCount() : blockCount;
  }

  @Override
  public List<? extends ICodeEdge<?>> getBasicBlockEdges() {
    return functionGraph.getEdges();
  }

  @Override
  public List<IBlockNode> getBasicBlocks() {
    if (!isLoaded()) {
      throw new IllegalStateException("IE00076: Function must be loaded first");
    }

    return functionGraph.getNodes();
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public int getEdgeCount() {
    return isLoaded() ? functionGraph.edgeCount() : edgeCount;
  }

  @Override
  public List<IComment> getGlobalComment() {
    return CommentManager.get(provider).getGlobalFunctionComment(this);
  }

  @Override
  public DirectedGraph<IBlockNode, IBlockEdge> getGraph() {
    if (!isLoaded()) {
      throw new IllegalStateException("IE00077: Function must be loaded first");
    }

    return functionGraph;
  }

  @Override
  public int getIndegree() {
    return indegree;
  }

  @Override
  public INaviModule getModule() {
    return module;
  }

  @Override
  public String getName() {
    return name == null ? originalName : name;
  }

  @Override
  public String getOriginalModulename() {
    return forwardedFunctionModuleName == null ? module.getConfiguration().getName()
        : forwardedFunctionModuleName;
  }

  @Override
  public String getOriginalName() {
    return originalName;
  }

  @Override
  public int getOutdegree() {
    return outdegree;
  }

  @Override
  public IAddress getForwardedFunctionAddress() {
    return forwardedFunctionAddress;
  }

  @Override
  public int getForwardedFunctionModuleId() {
    return forwardedFunctionModuleId;
  }

  @Override
  public BaseType getStackFrame() {
    return stackFrame;
  }

  @Override
  public BaseType getPrototype() {
    return prototype;
  }

  @Override
  public void setPrototype(final BaseType prototype) {
    this.prototype = prototype;
  }

  @Override
  public FunctionType getType() {
    return type;
  }

  @Override
  public void initializeGlobalComment(final ArrayList<IComment> comments) {
    if (comments != null) {
      CommentManager.get(provider).initializeGlobalFunctionComment(this, comments);
    }
  }

  @Override
  public boolean inSameDatabase(final IDatabaseObject object) {
    Preconditions.checkNotNull(object, "IE00078: Object argument can not be null");
    return object.inSameDatabase(provider);
  }

  @Override
  public boolean inSameDatabase(final SQLProvider provider) {
    return provider.equals(provider);
  }

  @Override
  public boolean isLoaded() {
    return functionGraph != null;
  }

  @Override
  public boolean isOwner(final IComment comment) {
    return CUserManager.get(provider).getCurrentActiveUser().equals(comment.getUser());
  }

  @Override
  public boolean isForwarded() {
    return forwardedFunctionAddress != null;
  }

  @Override
  public void load() throws CouldntLoadDataException {
    if (isLoaded()) {
      throw new IllegalStateException("IE00079: Function is already loaded");
    }

    try {
      if (!view.isLoaded()) {
        view.load();
      }
      functionGraph = convert((MutableDirectedGraph<INaviViewNode, INaviEdge>) view.getGraph());
    } catch (final CPartialLoadException | LoadCancelledException e) {
      CUtilityFunctions.logException(e);
    }

    for (final IFunctionListener<IComment> listener : functionListeners) {
      listener.loadedFunction(this);
    }
  }

  @Override
  public void removeListener(final IFunctionListener<IComment> listener) {
    functionListeners.removeListener(listener);
  }

  @Override
  public void setDescription(final String description) throws CouldntSaveDataException {
    setDescription(description, true);
  }

  @Override
  public void setDescriptionInternal(final String description) {
    try {
      setDescription(description, false);
    } catch (CouldntSaveDataException exception) {
      // Can not happen as we are not writing to the persistence layer.
    }
  }

  /**
   * Set the description of a function.
   *
   * @param description The description of the {@link INaviFunction function} to be set.
   * @param saveToDatabase If true save the description to the database.
   * @throws CouldntSaveDataException if the persistence layer could not store the the description.
   */
  private void setDescription(final String description, final boolean saveToDatabase)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(description, "IE00080: New comment can not be null");
    if (description.equals(this.description)) {
      return;
    }

    if (saveToDatabase) {
      provider.setDescription(this, description);
    }

    this.description = description;

    for (final IFunctionListener<IComment> listener : functionListeners) {
      listener.changedDescription(this, description);
    }
  }

  @Override
  public void setName(final String name) throws CouldntSaveDataException {
    setName(name, true);
  }

  @Override
  public void setNameInternal(final String name) {
    try {
      setName(name, false);
    } catch (CouldntSaveDataException e) {
      // Can not happen as we do not write to the persistence layer.
    }
  }

  /**
   * Set the name of a function.
   *
   * @param name The name of the {@link INaviFunction function} to be set.
   * @param saveToDatabase if true saves the name to the database.
   * @throws CouldntSaveDataException if the persistence layer could not store the name.
   */
  private void setName(final String name, final boolean saveToDatabase)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(name, "IE00085: Null is not a valid function name");
    if (name.equals(this.name)) {
      return;
    }

    if (saveToDatabase) {
      provider.setName(this, name);
    }

    this.name = name;

    for (final IFunctionListener<IComment> listener : functionListeners) {
      listener.changedName(this, name);
    }
  }

  @Override
  public void setForwardedFunction(final INaviFunction function) throws CouldntSaveDataException {
    Preconditions.checkNotNull(function, "Error: function arugment can not be null.");
    setForwardedFunction(function, true /* save to database */);
  }

  @Override
  public void removeForwardedFunction() throws CouldntSaveDataException {
    removeForwardedFunction(true /* save to database */);
  }

  @Override
  public void setForwardedFunctionInternal(final INaviFunction function) {
    try {
      Preconditions.checkNotNull(function, "Error: function arugment can not be null.");
      setForwardedFunction(function, false /* save to database */);
    } catch (final CouldntSaveDataException exception) {
      // Can not happen as we do not write to the persistence layer.
    }
  }

  @Override
  public void removeForwardedFunctionInternal() {
    try {
      removeForwardedFunction(false /* save to database */);
    } catch (final CouldntSaveDataException exception) {
      // Can not happen as we do not write to the persistence layer.
    }
  }

  /**
   * Forwards this function to the given function.
   *
   * @param function The {@link INaviFunction function} this function should be forwarded too.
   * @param saveToDatabase if true stores the forward information to the database.
   * @throws CouldntSaveDataException
   */
  private void setForwardedFunction(final INaviFunction function, final boolean saveToDatabase)
      throws CouldntSaveDataException {
    Preconditions.checkArgument(CFunctionHelpers.isForwardableFunction(this),
        "IE00082: Only imported functions can be forwarded");
    Preconditions.checkNotNull(function, "Error: function argument can not be null");
    Preconditions.checkArgument(function.getType() != FunctionType.IMPORT,
        "IE00083: Imported functions can not be target functions");
    Preconditions.checkArgument(function.inSameDatabase(provider),
        "IE00084: Function and target function are not in the same database");

    if (function.getAddress().equals(forwardedFunctionAddress)
        && (function.getModule().getConfiguration().getId() == forwardedFunctionModuleId)) {
      return;
    }

    if (saveToDatabase) {
      provider.forwardFunction(this, function);
    }

    forwardedFunctionAddress = function.getAddress();
    forwardedFunctionModuleId = function.getModule().getConfiguration().getId();

    for (final IFunctionListener<IComment> listener : functionListeners) {
      listener.changedForwardedFunction(this);
    }
  }

  private void removeForwardedFunction(final boolean saveToDatabase)
      throws CouldntSaveDataException {

    if (forwardedFunctionAddress == null) {
      return;
    }

    if (saveToDatabase) {
      provider.forwardFunction(this, null /* function */);
    }

    forwardedFunctionAddress = null;
    forwardedFunctionModuleId = 0;

    for (final IFunctionListener<IComment> listener : functionListeners) {
      listener.changedForwardedFunction(this);
    }
  }

  @Override
  public void setStackFrame(final BaseType stackFrame) {
    this.stackFrame = stackFrame;
  }

  @Override
  public String toString() {
    return String.format("Function %s: %s", address.toHexString(), name);
  }

  /**
   * Keeps this function updated when global comments change.
   */
  private class InternalCommentListener extends CommentListenerAdapter {

    @Override
    public void appendedGlobalFunctionComment(final INaviFunction function,
        final IComment comment) {
      if (CFunction.this == function) {
        for (final IFunctionListener<IComment> listener : functionListeners) {
          listener.appendedComment(function, comment);
        }
      }
    }

    @Override
    public void deletedGlobalFunctionComment(final INaviFunction function, final IComment comment) {
      if (CFunction.this == function) {
        for (final IFunctionListener<IComment> listener : functionListeners) {
          listener.deletedComment(function, comment);
        }
      }
    }

    @Override
    public void editedGlobalFunctionComment(final INaviFunction function, final IComment comment) {
      if (CFunction.this == function) {
        for (final IFunctionListener<IComment> listener : functionListeners) {
          listener.editedComment(function, comment);
        }
      }
    }

    @Override
    public void initializedGlobalFunctionComments(final INaviFunction function,
        final List<IComment> comments) {
      if (CFunction.this == function) {
        for (final IFunctionListener<IComment> listener : functionListeners) {
          listener.initializedComment(function, comments);
        }
      }
    }
  }
}
