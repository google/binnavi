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
package com.google.security.zynamics.binnavi.disassembly.Modules;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.CModuleViewGenerator;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphView;
import com.google.security.zynamics.binnavi.disassembly.IFlowgraphView;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.CViewListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviViewListener;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.ViewType;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.graphs.IDirectedGraph;
import com.google.security.zynamics.zylib.types.lists.FilledList;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Provides access to the views of a module.
 */
public final class CViewContainer {
  /**
   * The module the views belong to.
   */
  private final INaviModule m_module;

  /**
   * The native call graph view of the module.
   */
  private final ICallgraphView m_nativeCallgraphView;

  /**
   * A map which maps {@link INaviView} to {@link INaviFunction} for native views.
   */
  private final ImmutableBiMap<INaviView, INaviFunction> m_viewFunctionBiMap;

  /**
   * The native flow graph views of the module.
   */
  private final ImmutableList<IFlowgraphView> m_nativeFlowgraphs;

  /**
   * The user-created non-native views of the module.
   */
  private final List<INaviView> m_customViews;

  /**
   * Synchronizes the module with its views.
   */
  private final INaviViewListener m_viewListener = new InternalViewListener();

  /**
   * Listeners that are notified about changes in the views.
   */
  private final ListenerProvider<IModuleListener> m_listeners;

  /**
   * Map that stores a lookup for views by view id.
   */
  private final Map<Integer, INaviView> viewIdView = Maps.newHashMap();

  /**
   * Synchronizes changes in the views with the database.
   */
  private final SQLProvider m_provider;

  /**
   * Creates a new view container object.
   *
   * @param module The module the views belong to.
   * @param nativeCallgraph The native Call graph view of the module.
   * @param nativeFlowgraphs The native Flow graph views of the module.
   * @param customViews The user-created non-native views of the module.
   * @param viewFunctionBiMap Keeps track of the connection between functions and views.
   * @param listeners Listeners that are notified about changes in the views.
   * @param provider Synchronizes changes in the views with the database.
   */
  public CViewContainer(final INaviModule module,
      final ICallgraphView nativeCallgraph,
      final ImmutableList<IFlowgraphView> nativeFlowgraphs,
      final List<INaviView> customViews,
      final ImmutableBiMap<INaviView, INaviFunction> viewFunctionBiMap,
      final ListenerProvider<IModuleListener> listeners,
      final SQLProvider provider) {
    m_module = Preconditions.checkNotNull(module, "IE02395: module argument can not be null");
    m_listeners = listeners;
    m_provider = Preconditions.checkNotNull(provider, "IE02396: provider argument can not be null");

    m_nativeCallgraphView = nativeCallgraph;
    viewIdView.put(nativeCallgraph.getConfiguration().getId(), nativeCallgraph);
    m_nativeFlowgraphs = nativeFlowgraphs;
    m_customViews = customViews;

    m_viewFunctionBiMap = viewFunctionBiMap;

    for (final INaviView view : m_customViews) {
      view.addListener(m_viewListener);
      viewIdView.put(view.getConfiguration().getId(), view);
    }

    for (final INaviView view : m_nativeFlowgraphs) {
      view.addListener(m_viewListener);
      viewIdView.put(view.getConfiguration().getId(), view);
    }
  }

  /**
   * Closes a bunch of views.
   *
   * @param views The views to close.
   *
   * @return True, if all views were closed. False, if at least one view could not be closed.
   */
  private static boolean closeViews(final List<? extends INaviView> views) {
    for (final INaviView view : new FilledList<INaviView>(views)) {
      if (view.isLoaded() && !view.close()) {
        return false;
      }
    }

    return true;
  }

  /**
   * Adds a custom view to the module.
   *
   * @param view The view to add.
   */
  public void addView(final INaviView view) {
    Preconditions.checkNotNull(view, "IE00151: View can not be null");
    Preconditions.checkArgument(view.getType() != ViewType.Native,
        "IE00152: Only non-native views can be added to modules");
    Preconditions.checkArgument(!m_customViews.contains(view),
        "IE00154: View can not be added to the module more than once");
    Preconditions.checkArgument(
        view.inSameDatabase(m_provider), "IE00155: View and module are not in the same database");

    m_customViews.add(view);
    if (view.getConfiguration().getId() != -1) {
      viewIdView.put(view.getConfiguration().getId(), view);
    }

    view.addListener(m_viewListener);

    for (final IModuleListener listener : m_listeners) {
      try {
        listener.addedView(m_module, view);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Closes the view container.
   *
   * @return True, if the container was closed. False, if something blocked closing the container.
   */
  public boolean close() {
    if (m_nativeCallgraphView.isLoaded() && !m_nativeCallgraphView.close()) {
      return false;
    }

    if (!closeViews(m_nativeFlowgraphs)) {
      return false;
    }

    if (!closeViews(m_customViews)) {
      return false;
    }

    for (final INaviView view : m_customViews) {
      view.removeListener(m_viewListener);
    }

    for (final INaviView view : m_nativeFlowgraphs) {
      view.removeListener(m_viewListener);
    }

    return true;
  }

  /**
   * Creates a new empty view in the module.
   *
   * @param name The name of the new view.
   * @param description The description of the new view.
   *
   * @return The new view.
   */
  public CView createView(final String name, final String description) {
    Preconditions.checkNotNull(name, "IE00164: Name argument can not be null");
    Preconditions.checkNotNull(description, "IE00165: Name description can not be null");

    final Date date = new Date();
    final CModuleViewGenerator generator = new CModuleViewGenerator(m_provider, m_module);
    final CView view = generator.generate(-1,
        name,
        description,
        ViewType.NonNative,
        GraphType.MIXED_GRAPH,
        date,
        date,
        0,
        0,
        new HashSet<CTag>(),
        new HashSet<CTag>(),
        false);

    try {
      view.load();
    } catch (CouldntLoadDataException | CPartialLoadException | LoadCancelledException e) {
      CUtilityFunctions.logException(e);
    }

    addView(view);

    return view;
  }

  /**
   * Deletes a view from the module.
   *
   * @param view The view to delete.
   *
   * @throws CouldntDeleteException Thrown if the view could not be deleted.
   */
  public void deleteView(final INaviView view) throws CouldntDeleteException {
    checkDeleteArguments(view);

    if (view.getConfiguration().getId() != -1) {
      m_provider.deleteView(view);
    }

    delete(view);
  }

  public void deleteViewInternal(final INaviView view) {
    Preconditions.checkNotNull(view, "IE02756: View argument can not be null");
    if (view.getType() == ViewType.Native) {
      // TODO: delete of modules can not be modeled yet.
      // This case happens when a module gets deleted and for all
      // deleted views the trigger is performed for a native view
      // as well and not only for the non native views.
      return;
    }
    Preconditions.checkArgument(
        m_customViews.contains(view), "IE00172: View is not part of the module");
    delete(view);
  }

  private void checkDeleteArguments(final INaviView view) {
    Preconditions.checkNotNull(view, "IE00169: View argument can not be null");
    Preconditions.checkState(
        view.getType() == ViewType.NonNative, "IE00171: Only non-native views can be deleted");
    Preconditions.checkArgument(
        m_customViews.contains(view), "IE00172: View is not part of the module");
  }

  private void delete(final INaviView view) {
    final INaviView currentStoredView = m_customViews.get(m_customViews.indexOf(view));
    m_customViews.remove(currentStoredView);
    viewIdView.remove(view.getConfiguration().getId());

    currentStoredView.removeListener(m_viewListener);

    for (final IModuleListener listener : m_listeners) {
      try {
        listener.deletedView(m_module, currentStoredView);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Returns the number of custom non-native views in the module.
   *
   * @return The number of custom non-native views in the module.
   */
  public int getCustomViewCount() {
    return m_customViews.size();
  }

  /**
   * Maps a view to a function. This is useful if you want to know what function was used to
   * generate a view.
   *
   * @param view A view from the project.
   *
   * @return The function that was used to create the view or null if the function can not be
   *         determined.
   */
  public INaviFunction getFunction(final INaviView view) {
    return m_viewFunctionBiMap.get(
        Preconditions.checkNotNull(view, "IE00174: View argument can not be null"));
  }

  /**
   * Returns the native call graph view of the module.
   *
   * @return The native call graph view of the module.
   */
  public ICallgraphView getNativeCallgraphView() {
    return m_nativeCallgraphView;
  }

  /**
   * Returns the native flow graph views that belong to this module.
   *
   * @return The native flow graph views that belong to this module.
   */
  public ImmutableList<IFlowgraphView> getNativeFlowgraphViews() {
    return m_nativeFlowgraphs;
  }

  /**
   * Returns the user views of the module.
   *
   * @return The user views of the module.
   */
  public List<INaviView> getUserViews() {
    return new ArrayList<INaviView>(m_customViews);
  }

  /**
   * Returns the view with the given function.
   *
   * @param function The function to search for.
   *
   * @return The view that represents the function.
   */
  public INaviView getView(final INaviFunction function) {
    Preconditions.checkNotNull(function, "IE00186: Function argument can not be null");
    Preconditions.checkArgument(function.inSameDatabase(m_provider),
        "IE00188: Function and module are not in the same database");
    return m_viewFunctionBiMap.inverse().get(function);
  }

  /**
   * Returns a list of all views that can be found in the module.
   *
   * @return A list of all views that can be found in the module.
   */
  public List<INaviView> getViews() {
    final List<INaviView> views = new ArrayList<INaviView>();

    views.add(m_nativeCallgraphView);
    views.addAll(m_nativeFlowgraphs);
    views.addAll(m_customViews);

    return views;
  }

  /**
   * Returns the {@link INaviView view} with the given {@link Integer viewId} if present.
   *
   * @param viewId The id of the {@link INaviView view}.
   *
   * @return The {@link INaviView view} with the given id if present.
   */
  public INaviView getView(final Integer viewId) {
    Preconditions.checkNotNull(viewId, "Error: viewId argument can not be null");
    Preconditions.checkArgument(viewId > 0, "Error: only saved views can be querried by id");
    return viewIdView.get(viewId);
  }

  /**
   * Determines whether a view belongs to this module.
   *
   * @param view The view in question.
   * @return True, if the view belongs to the module. False, otherwise.
   */
  public boolean hasView(final INaviView view) {
    Preconditions.checkNotNull(view, "IE00190: View argument can not be null");
    Preconditions.checkArgument(
        view.inSameDatabase(m_provider), "IE00192: View and module are not in the same database");

    return (view == m_nativeCallgraphView) || m_customViews.contains(view)
        || m_nativeFlowgraphs.contains(view);
  }

  /**
   * Synchronizes the module with its views.
   */
  private class InternalViewListener extends CViewListenerAdapter {
    @Override
    public void changedDescription(final INaviView view, final String description) {
      final INaviFunction function = getFunction(view);

      if (function != null) {
        try {
          function.setDescription(description);
        } catch (final CouldntSaveDataException e) {
          CUtilityFunctions.logException(e);
        }
      }
    }

    @Override
    public void changedName(final INaviView view, final String name) {
      final INaviFunction function = getFunction(view);

      if (function != null) {
        try {
          function.setName(name);
        } catch (final CouldntSaveDataException e) {
          CUtilityFunctions.logException(e);
        }
      }
    }

    @Override
    public void closedView(
        final INaviView view, final IDirectedGraph<INaviViewNode, INaviEdge> oldGraph) {
      if (view.getConfiguration().getId() == -1) {
        m_customViews.remove(view);

        view.removeListener(this);
      }
    }
  }
}
