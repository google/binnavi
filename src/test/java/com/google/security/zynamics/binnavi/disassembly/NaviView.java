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
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.views.CViewLoaderReporter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviViewListener;
import com.google.security.zynamics.binnavi.disassembly.views.IViewConfiguration;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContent;
import com.google.security.zynamics.binnavi.disassembly.views.ViewLoadEvents;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.ICodeEdge;
import com.google.security.zynamics.zylib.disassembly.ViewType;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.graphs.IDirectedGraph;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class NaviView implements INaviView {

  private final SQLProvider provider;
  private final ListenerProvider<INaviViewListener> listeners =
      new ListenerProvider<INaviViewListener>();
  private final IViewConfiguration configuration;
  private final CViewLoaderReporter reporter = new CViewLoaderReporter(listeners);
  private final IViewContent content;
  private final Set<CTag> viewNodeTags;

  public NaviView(final SQLProvider provider, final IViewConfiguration configuration,
      final IViewContent content, final Set<CTag> viewNodeTags) {
    this.provider = provider;
    this.configuration = configuration;
    this.content = content;
    this.viewNodeTags = viewNodeTags;
  }

  @Override
  public void addListener(final INaviViewListener listener) {
    Preconditions.checkNotNull(listener, "Error: listener argument can not be null");
    listeners.addListener(listener);
  }

  @Override
  public boolean close() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public int getEdgeCount() {
    return isLoaded() ? content.getEdgeCount() : configuration.getUnloadedEdgeCount();
  }

  @Override
  public GraphType getGraphType() {
    return isLoaded() ? content.getGraphType() : configuration.getUnloadedGraphType();
  }

  @Override
  public String getName() {
    return configuration.getName();
  }

  @Override
  public int getNodeCount() {
    return isLoaded() ? content.getNodeCount() : configuration.getUnloadedNodeCount();
  }

  @Override
  public ViewType getType() {
    return configuration.getType();
  }

  @Override
  public boolean isLoaded() {
    return content != null;
  }

  @Override
  public void removeListener(final INaviViewListener listener) {
    listeners.removeListener(listener);
  }

  @Override
  public boolean inSameDatabase(final IDatabaseObject databaseObject) {
    Preconditions.checkNotNull(databaseObject, "databaseObjects argument can not be null");
    return databaseObject.inSameDatabase(provider);
  }

  @Override
  public boolean inSameDatabase(final SQLProvider provider) {
    Preconditions.checkNotNull(provider, "Error: provider argument can not be null");
    return this.provider.equals(provider);
  }

  @Override
  public List<? extends ICodeEdge<?>> getBasicBlockEdges() {
    return isLoaded() ? content.getBasicBlockEdges() : null;
  }

  @Override
  public boolean isStared() {
    return configuration.isStared();
  }

  @Override
  public List<CCodeNode> getBasicBlocks() {
    return isLoaded() ? content.getBasicBlocks() : null;
  }

  @Override
  public IViewConfiguration getConfiguration() {
    return configuration;
  }

  @Override
  public IViewContent getContent() {
    return content;
  }

  @Override
  public List<INaviView> getDerivedViews() throws CouldntLoadDataException {
    return provider.getDerivedViews(this);
  }

  @Override
  public IDirectedGraph<INaviViewNode, INaviEdge> getGraph() {
    return isLoaded() ? content.getGraph() : null;
  }

  @Override
  public int getLoadState() {
    // TODO(timkornau) think about how to move this method somewhere where it makes
    // more sense.
    return 0;
  }

  @Override
  public Set<CTag> getNodeTags() {
    return viewNodeTags;
  }

  @Override
  public void load() {
    reporter.report(ViewLoadEvents.Started);
    reporter.report(ViewLoadEvents.Finished);
  }

  @Override
  public Map<String, String> loadSettings() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void save() {
    // TODO Auto-generated method stub
  }

  @Override
  public void saveSettings(final Map<String, String> settings) {
    // TODO Auto-generated method stub
  }

  @Override
  public boolean wasModified() {
    // TODO Auto-generated method stub
    return false;
  }
}
