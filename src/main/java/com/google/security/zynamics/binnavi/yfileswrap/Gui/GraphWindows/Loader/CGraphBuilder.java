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
package com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Loader;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Loader.CViewSettingsGenerator;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.types.BaseTypeTransferHandler;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.ZyGraphBuilderManager;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.types.DragAndDropSupportWrapper;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.Builders.ZyGraphBuilder;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraphDragAndDropSupport;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.ViewType;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyGraph2DView;

import y.view.Graph2D;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains helper classes for turning a raw view into a display able graph.
 */
public final class CGraphBuilder {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGraphBuilder() {}

  /**
   * Generates the default graph settings for a native view.
   *
   * @param view The view whose graph settings are generated.
   *
   * @return The generated graph settings.
   */
  private static Pair<Map<String, String>, ZyGraphViewSettings> loadNativeSettings(
      final INaviView view) {
    final ConfigManager configFile = ConfigManager.instance();

    // Native views receive the default settings when they are loaded.
    final ZyGraphViewSettings graphSettings =
        view.getGraphType() == GraphType.CALLGRAPH ? configFile.getDefaultCallGraphSettings()
            : configFile.getDefaultFlowGraphSettings();

    graphSettings.getProximitySettings().setProximityBrowsing(view.getGraph().nodeCount()
        >= graphSettings.getProximitySettings().getProximityBrowsingActivationThreshold());

    return new Pair<Map<String, String>, ZyGraphViewSettings>(new HashMap<String, String>(),
        graphSettings);
  }

  /**
   * Generates the graph settings for a non-native view.
   *
   * @param view The view whose graph settings are generated.
   *
   * @return The generated graph settings.
   */
  private static Pair<Map<String, String>, ZyGraphViewSettings> loadNonNativeSettings(
      final INaviView view) {
    // Non-native views get their settings from the database. If the database settings
    // could not be loaded, the default values from the settings file are used too.

    try {
      return CViewSettingsGenerator.createSettings(view);
    } catch (final CouldntLoadDataException exception) {
      final ConfigManager configFile = ConfigManager.instance();

      final ZyGraphViewSettings graphSettings =
          view.getGraphType() == GraphType.CALLGRAPH ? configFile.getDefaultCallGraphSettings()
              : configFile.getDefaultFlowGraphSettings();

      return new Pair<Map<String, String>, ZyGraphViewSettings>(new HashMap<String, String>(),
          graphSettings);
    }
  }

  /**
   * Generates the graph settings for a view.
   *
   * @param view The view whose graph settings are generated.
   *
   * @return The generated graph settings.
   */
  private static Pair<Map<String, String>, ZyGraphViewSettings> loadSettings(final INaviView view) {
    return view.getType() == ViewType.Native ? loadNativeSettings(view)
        : loadNonNativeSettings(view);
  }

  /**
   * Builds a graph that supports drag and drop operations from the type editor onto operand tree
   * nodes in order to assign type substitutions.
   *
   * @param view The view to build the graph from.
   * @param typeManager THe type manager for the module that contains the given view.
   * @return Returns a new instance of the created graph.
   * @throws LoadCancelledException
   */
  public static ZyGraph buildDnDGraph(final INaviView view, final TypeManager typeManager)
      throws LoadCancelledException {
    final ZyGraph graph = buildGraph(view);
    final ZyGraphDragAndDropSupport dndSupport = new ZyGraphDragAndDropSupport(graph,
        new BaseTypeTransferHandler(typeManager, new DragAndDropSupportWrapper(graph)));
    dndSupport.enableDndSupport();
    return graph;
  }

  /**
   * Builds a displayable graph from a view.
   *
   * @param view The view to be turned into a graph.
   *
   * @return The generated graph.
   * @throws LoadCancelledException Thrown if loading the graph was canceled.
   */
  public static ZyGraph buildGraph(final INaviView view) throws LoadCancelledException {
    Preconditions.checkNotNull(view, "IE01763: View argument can't be null");

    final Pair<Map<String, String>, ZyGraphViewSettings> viewSettings = loadSettings(view);

    final Map<String, String> rawSettings = viewSettings.first();
    final ZyGraphViewSettings graphSettings = viewSettings.second();

    graphSettings.rawSettings = rawSettings;

    final ZyGraphBuilder builder = new ZyGraphBuilder();

    ZyGraphBuilderManager.instance().setBuilder(view, builder);

    final Graph2D graph = builder.convert(view.getGraph().getNodes(), view.getGraph().getEdges(),
        graphSettings, view.getType() == ViewType.Native);

    final ZyGraph2DView graphView = new ZyGraph2DView(graph);

    final ZyGraph zyGraph =
        new ZyGraph(view, builder.getNodeMap(), builder.getEdgeMap(), graphSettings, graphView);

    zyGraph.getView().setCenter(
        CViewSettingsGenerator.createDoubleSetting(rawSettings, "view_center_x", 0),
        CViewSettingsGenerator.createDoubleSetting(rawSettings, "view_center_y", 0));
    zyGraph.getView().setWorldRect(
        CViewSettingsGenerator.createIntegerSetting(rawSettings, "world_rect_x", 0),
        CViewSettingsGenerator.createIntegerSetting(rawSettings, "world_rect_y", 0),
        CViewSettingsGenerator.createIntegerSetting(rawSettings, "world_rect_width", 800),
        CViewSettingsGenerator.createIntegerSetting(rawSettings, "world_rect_height", 600));
    zyGraph.getView().setZoom(CViewSettingsGenerator.createDoubleSetting(rawSettings, "zoom", 1));


    ZyGraphBuilderManager.instance().removeBuilder(view);

    return zyGraph;
  }
}
