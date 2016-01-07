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

import com.google.security.zynamics.binnavi.disassembly.Modules.CFunctionContainer;
import com.google.security.zynamics.binnavi.disassembly.Modules.CTraceContainer;
import com.google.security.zynamics.binnavi.disassembly.Modules.CViewContainer;
import com.google.security.zynamics.binnavi.disassembly.types.SectionContainer;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainer;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.IModuleContent;

/**
 * Interface for all classes that want to provide module content.
 */
public interface INaviModuleContent extends IModuleContent<INaviFunction, INaviView> {

  /**
   * Returns the {@link CFunctionContainer container} for this {@link INaviModule module}.
   *
   * @return the {@link CFunctionContainer container} for this {@link INaviModule module}.
   */
  CFunctionContainer getFunctionContainer();

  /**
   * Returns the native call graph view of this module.
   *
   * @return The native call graph view of this module.
   */
  CCallgraph getNativeCallgraph();

  /**
   * Returns the container that holds all sections for this module.
   *
   * @return The section container for this module.
   */
  SectionContainer getSections();

  /**
   * Returns the {@link CTraceContainer container} that holds all traces for this {@link INaviModule
   * module}.
   *
   * @return The {@link CTraceContainer} for this {@link INaviModule}.
   */
  CTraceContainer getTraceContainer();

  /**
   * Returns the per-module container for type instances.
   *
   * @return The type instance container for this module.
   */
  TypeInstanceContainer getTypeInstanceContainer();

  CViewContainer getViewContainer();
}
