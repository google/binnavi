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
package com.google.security.zynamics.binnavi.disassembly.Modules;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.disassembly.CCallgraph;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphEdge;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphNode;
import com.google.security.zynamics.binnavi.disassembly.IFlowgraphView;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviModuleContent;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.types.SectionContainer;
import com.google.security.zynamics.binnavi.disassembly.types.SectionContainerBackend;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainer;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainerBackend;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.lists.FilledList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MockModuleContent implements INaviModuleContent {
  private final CCallgraph m_callgraph =
      new CCallgraph(new ArrayList<ICallgraphNode>(), new ArrayList<ICallgraphEdge>());

  private final MockView m_nativeCallgraphView;
  @SuppressWarnings("unused")
  private final FilledList<IFlowgraphView> m_flowgraphViews = new FilledList<IFlowgraphView>();
  @SuppressWarnings("unused")
  private final FilledList<INaviView> m_views = new FilledList<INaviView>();
  public Map<IAddress, INaviFunction> functionMap = new HashMap<IAddress, INaviFunction>();
  private final CTraceContainer m_traces;

  private final ListenerProvider<IModuleListener> m_listeners;

  @SuppressWarnings("unused")
  private final INaviModule m_module;
  private final SQLProvider m_provider;
  private final CViewContainer m_viewContainer;
  private final CFunctionContainer m_functionContainer;
  private SectionContainer sections = null;
  private TypeInstanceContainer instances = null;

  public MockModuleContent(final INaviModule module, final SQLProvider provider,
      final ListenerProvider<IModuleListener> mListeners, final List<INaviView> views,
      final List<INaviFunction> functions) {

    m_module = module;
    m_listeners = mListeners;
    m_provider = provider;
    m_traces = new CTraceContainer(module, new FilledList<TraceList>(), provider);
    m_nativeCallgraphView = new MockView(m_provider);
    final List<INaviView> customViews = new ArrayList<INaviView>();
    if (views != null) {
      customViews.addAll(views);
    }
    final FilledList<INaviFunction> customfunctions = new FilledList<INaviFunction>();
    if (functions != null) {
      customfunctions.addAll(functions);
    }

    m_viewContainer = new CViewContainer(module, m_nativeCallgraphView,
        new ImmutableList.Builder<IFlowgraphView>().build(), customViews,
        new ImmutableBiMap.Builder<INaviView, INaviFunction>().build(), m_listeners, provider);
    m_functionContainer = new CFunctionContainer(module, customfunctions);

    try {
      sections = new SectionContainer(new SectionContainerBackend(m_provider, module));
      instances = new TypeInstanceContainer(
          new TypeInstanceContainerBackend(m_provider, module, module.getTypeManager(), sections),
          provider);
    } catch (final CouldntLoadDataException e) {
      e.printStackTrace();
    }
  }

  @Override
  public CFunctionContainer getFunctionContainer() {
    return m_functionContainer;
  }

  @Override
  public CCallgraph getNativeCallgraph() {
    return m_callgraph;
  }

  @Override
  public SectionContainer getSections() {
    return sections;
  }

  @Override
  public CTraceContainer getTraceContainer() {
    return m_traces;
  }

  @Override
  public TypeInstanceContainer getTypeInstanceContainer() {
    return instances;
  }

  @Override
  public CViewContainer getViewContainer() {
    return m_viewContainer;
  }
}
