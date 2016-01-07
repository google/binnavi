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
package com.google.security.zynamics.binnavi.disassembly.Modules;


import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.disassembly.CCallgraph;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphView;
import com.google.security.zynamics.binnavi.disassembly.IFlowgraphView;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviModuleContent;
import com.google.security.zynamics.binnavi.disassembly.types.SectionContainer;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainer;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.util.List;

/**
 * Contains the content of loaded modules.
 */
public final class CModuleContent implements INaviModuleContent {
  /**
   * Native call graph of the module.
   */
  private final CCallgraph m_callgraph;

  /**
   * Contains the traces of the module.
   */
  private final CTraceContainer m_traces;

  /**
   * Contains the functions of the module.
   */
  private final CFunctionContainer m_functions;

  /**
   * Contains the views of the module.
   */
  private final CViewContainer m_viewContainer;

  /**
   * Flag that indicates whether this module content object was closed or not. Closed module content
   * objects can not be used anymore.
   */
  private boolean m_closed = false;

  private final SectionContainer sections;

  private final TypeInstanceContainer instanceContainer;

  /**
   * Creates a new content object.
   *
   * @param module The module the content belongs to.
   * @param provider Synchronizes the content with the database.
   * @param listeners Listeners that are notified about changes in the module.
   * @param callgraph Native Call graph of the module.
   * @param functions The functions of the module.
   * @param nativeCallgraph Native Call graph of the module.
   * @param nativeFlowgraphs The native Flow graph views of the module.
   * @param customViews The user-created non-native views of the module.
   * @param viewFunctionMap Address => Function map for fast lookup of functions by address.
   * @param traces List of traces that were recorded for this module.
   */
  public CModuleContent(final INaviModule module, final SQLProvider provider,
      final ListenerProvider<IModuleListener> listeners, final CCallgraph callgraph,
      final List<INaviFunction> functions, final ICallgraphView nativeCallgraph,
      final ImmutableList<IFlowgraphView> nativeFlowgraphs, final List<INaviView> customViews,
      final ImmutableBiMap<INaviView, INaviFunction> viewFunctionMap,
      final List<TraceList> traces, final SectionContainer sections,
      final TypeInstanceContainer instanceContainer) {

    Preconditions.checkNotNull(module, "IE02176: Module argument can not be null");
    Preconditions.checkNotNull(provider, "IE02177: Provider argument can not be null");
    Preconditions.checkNotNull(listeners, "IE02178: Listeners argument can not be null");
    Preconditions.checkNotNull(callgraph, "IE02184: Call graph argument can not be null");
    Preconditions.checkNotNull(functions, "IE02185: Functions argument can not be null");
    Preconditions.checkNotNull(
        nativeCallgraph, "IE02204: Native Call graph argument can not be null");
    Preconditions.checkNotNull(
        nativeFlowgraphs, "IE02205: Native Flowgraphs argument can not be null");
    Preconditions.checkNotNull(customViews, "IE02206: Custom Views argument can not be null");
    Preconditions.checkNotNull(
        viewFunctionMap, "IE02207: View Function Map argument can not be null");
    Preconditions.checkNotNull(traces, "IE02208: Traces argument can not be null");
    this.sections = Preconditions.checkNotNull(sections);
    this.instanceContainer = Preconditions.checkNotNull(instanceContainer);
    m_traces = new CTraceContainer(module, traces, provider);
    m_viewContainer = new CViewContainer(
        module, nativeCallgraph, nativeFlowgraphs, customViews, viewFunctionMap, listeners,
        provider);

    m_callgraph = callgraph;
    m_functions = new CFunctionContainer(module, functions);
  }

  /**
   * Closes the module content.
   *
   * @return True, if the module content was closed. False, otherwise.
   */
  public boolean close() {
    Preconditions.checkState(!m_closed, "IE00203: Module content was closed before.");

    if (!m_functions.close()) {
      return false;
    }

    if (!m_viewContainer.close()) {
      return false;
    }

    m_closed = true;

    return true;
  }

  @Override
  public CFunctionContainer getFunctionContainer() {
    Preconditions.checkState(!m_closed, "IE00241: Module content was closed before.");
    return m_functions;
  }

  @Override
  public CCallgraph getNativeCallgraph() {
    Preconditions.checkState(!m_closed, "IE00261: Module content was closed before.");
    return m_callgraph;
  }

  /**
   * Returns the container that holds all sections for this module.
   *
   * @return The section container for this module.
   */
  @Override
  public SectionContainer getSections() {
    return sections;
  }

  @Override
  public CTraceContainer getTraceContainer() {
    Preconditions.checkState(!m_closed, "IE00262: Module content was closed before.");
    return m_traces;
  }

  /**
   * Returns the per-module container for type instanceContainer.
   *
   * @return The type instance container for this module.
   */
  @Override
  public TypeInstanceContainer getTypeInstanceContainer() {
    return instanceContainer;
  }

  @Override
  public CViewContainer getViewContainer() {
    Preconditions.checkState(!m_closed, "IE00263: Module content was closed before.");
    return m_viewContainer;
  }
}
