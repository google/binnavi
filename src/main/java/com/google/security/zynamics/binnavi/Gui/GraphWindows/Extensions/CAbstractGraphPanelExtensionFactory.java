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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Extensions;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract factory class that provides the factories that create the components that extend the
 * bottom panel in graph windows.
 */
public final class CAbstractGraphPanelExtensionFactory {
  /**
   * List of registered extension creator objects.
   */
  private static final List<IGraphPanelExtensionCreator> m_creators =
      new ArrayList<IGraphPanelExtensionCreator>();

  /**
   * You are not supposed to instantiate this class.
   */
  private CAbstractGraphPanelExtensionFactory() {
  }

  /**
   * Returns a list of known extensions to the bottom panel.
   *
   * @return A list of bottom panel extensions.
   */
  public static List<IGraphPanelExtension> getExtensions() {
    final List<IGraphPanelExtension> extensions = new ArrayList<IGraphPanelExtension>();

    for (final IGraphPanelExtensionCreator creator : m_creators) {
      extensions.add(creator.create());
    }

    return extensions;
  }

  /**
   * Registers a new graph panel extension creator.
   *
   * @param creator The creator to register.
   */
  public static void register(final IGraphPanelExtensionCreator creator) {
    m_creators.add(creator);
  }
}
