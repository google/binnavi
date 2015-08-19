/*
Copyright 2014 Google Inc. All Rights Reserved.

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
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.Users.CUserManager;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.disassembly.functions.FunctionManager;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.FunctionType;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.ICodeEdge;
import com.google.security.zynamics.zylib.disassembly.IFunctionListener;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.graphs.DirectedGraph;

import java.util.ArrayList;
import java.util.List;

public class MockFunction implements INaviFunction {
  private INaviModule module = new MockModule();

  private final ListenerProvider<IFunctionListener<IComment>> m_listeners =
      new ListenerProvider<IFunctionListener<IComment>>();

  private String m_description = "Mock Description";
  private String m_name = "Mock Function";
  private boolean m_loaded = false;
  private final SQLProvider provider;
  public final List<IBlockEdge> m_edges = new ArrayList<IBlockEdge>();
  public final List<IBlockNode> m_nodes = new ArrayList<IBlockNode>();
  private IAddress address = new CAddress(0x1234);

  private INaviFunction resolvedFunction;

  private BaseType stackFrame;

  public MockFunction() {
    provider = new MockSqlProvider();
    FunctionManager.get(provider).putFunction(this);
  }

  public MockFunction(final long address) {
    this.address = new CAddress(address);
    provider = new MockSqlProvider();
    FunctionManager.get(provider).putFunction(this);
  }

  public MockFunction(final SQLProvider provider) {
    this.provider = provider;
    FunctionManager.get(provider).putFunction(this);
  }

  public MockFunction(final SQLProvider provider, final IAddress address,
      final INaviModule module) {
    this.provider =
        Preconditions.checkNotNull(provider, "Error: provider argument can not be null");
    this.address = Preconditions.checkNotNull(address, "Error: address argument can not be null");
    this.module = Preconditions.checkNotNull(module, "Error: module argument can not be null");
    FunctionManager.get(provider).putFunction(this);
  }

  public MockFunction(final SQLProvider provider, final long address) {
    this.address = new CAddress(address);
    this.provider = provider;
    FunctionManager.get(provider).putFunction(this);
  }

  @Override
  public void addListener(final IFunctionListener<IComment> listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public List<IComment> appendGlobalComment(final String commentText)
      throws CouldntSaveDataException, CouldntLoadDataException {
    return CommentManager.get(provider).appendGlobalFunctionComment(this, commentText);
  }

  @Override
  public boolean close() {
    throw new IllegalStateException("Error: Not yet implemented");
  }

  @Override
  public void deleteGlobalComment(final IComment comment) throws CouldntDeleteException {
    CommentManager.get(provider).deleteGlobalFunctionComment(this, comment);
  }

  @Override
  public IComment editGlobalComment(final IComment comment, final String commentText)
      throws CouldntSaveDataException {
    return CommentManager.get(provider).editGlobalFunctionComment(this, comment, commentText);
  }

  @Override
  public IAddress getAddress() {
    return address;
  }

  @Override
  public int getBasicBlockCount() {
    return m_nodes == null ? 0 : m_nodes.size();
  }

  @Override
  public List<? extends ICodeEdge<?>> getBasicBlockEdges() {
    return new ArrayList<ICodeEdge<?>>(m_edges);
  }

  @Override
  public List<IBlockNode> getBasicBlocks() {
    return new ArrayList<IBlockNode>(m_nodes);
  }

  @Override
  public String getDescription() {
    return m_description;
  }

  @Override
  public int getEdgeCount() {
    return m_edges == null ? 0 : m_edges.size();
  }

  @Override
  public List<IComment> getGlobalComment() {
    return CommentManager.get(provider).getGlobalFunctionComment(this);
  }

  @Override
  public DirectedGraph<IBlockNode, IBlockEdge> getGraph() {
    return new DirectedGraph<IBlockNode, IBlockEdge>(m_nodes, m_edges);
  }

  @Override
  public int getIndegree() {
    return 0;
  }

  @Override
  public INaviModule getModule() {
    return module;
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public String getOriginalModulename() {
    throw new IllegalStateException("Error: Not yet implemented");
  }

  @Override
  public String getOriginalName() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public int getOutdegree() {
    return 0;
  }

  @Override
  public IAddress getForwardedFunctionAddress() {
    return resolvedFunction != null ? resolvedFunction.getAddress() : null;
  }

  @Override
  public int getForwardedFunctionModuleId() {
    return resolvedFunction != null ? resolvedFunction.getModule().getConfiguration().getId() : 0;
  }

  @Override
  public BaseType getStackFrame() {
    return stackFrame;
  }

  @Override
  public FunctionType getType() {
    return FunctionType.NORMAL;
  }

  @Override
  public void initializeGlobalComment(final ArrayList<IComment> comments) {
    CommentManager.get(provider).initializeGlobalFunctionComment(this, comments);
  }

  @Override
  public boolean inSameDatabase(final IDatabaseObject provider) {
    throw new IllegalStateException("Error: Not yet implemented");
  }

  @Override
  public boolean inSameDatabase(final SQLProvider provider) {
    return this.provider.equals(provider);
  }

  @Override
  public boolean isLoaded() {
    return m_loaded;
  }

  @Override
  public boolean isOwner(final IComment comment) {
    return CUserManager.get(provider).getCurrentActiveUser().equals(comment.getUser());
  }

  @Override
  public boolean isForwarded() {
    return resolvedFunction != null;
  }

  @Override
  public void load() {
    m_loaded = true;

    for (final IFunctionListener<IComment> listener : m_listeners) {
      listener.loadedFunction(this);
    }
  }

  @Override
  public void removeListener(final IFunctionListener<IComment> listener) {
    throw new IllegalStateException("Error: Not yet implemented");
  }

  @Override
  public void setDescription(final String comment) {
    m_description = comment;

    for (final IFunctionListener<IComment> listener : m_listeners) {
      listener.changedDescription(this, comment);
    }
  }

  @Override
  public void setDescriptionInternal(final String description) {
    setDescription(description);
  }

  @Override
  public void setName(final String name) {
    m_name = name;

    for (final IFunctionListener<IComment> listener : m_listeners) {
      listener.changedName(this, name);
    }
  }

  @Override
  public void setNameInternal(final String name) {
    setName(name);
  }

  @Override
  public void setForwardedFunction(final INaviFunction function) {
    resolvedFunction = function;
  }

  @Override
  public void setForwardedFunctionInternal(final INaviFunction function) {
    setForwardedFunction(function);
  }

  @Override
  public void setStackFrame(final BaseType stackFrame) {
    this.stackFrame = stackFrame;
  }

  @Override
  public BaseType getPrototype() {
    return null;
  }

  @Override
  public void setPrototype(BaseType prototype) {}

  @Override
  public void removeForwardedFunction() throws CouldntSaveDataException {
    resolvedFunction = null;
  }

  @Override
  public void removeForwardedFunctionInternal() {
    resolvedFunction = null;
  }
}
