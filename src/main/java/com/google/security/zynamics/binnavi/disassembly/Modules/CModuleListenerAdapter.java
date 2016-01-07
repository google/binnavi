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

import java.util.Date;
import java.util.List;

import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphView;
import com.google.security.zynamics.binnavi.disassembly.IFlowgraphView;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.IAddress;



/**
 * Listener adapter for module listeners that do not want to implement all module notification
 * methods.
 */
public class CModuleListenerAdapter implements IModuleListener {
  @Override
  public void addedView(final INaviModule module, final INaviView view) {
    // Empty default implementation
  }

  @Override
  public void changedData(final CModule module, final byte[] data) {
    // Empty default implementation
  }

  @Override
  public void changedDebugger(final INaviModule module, final IDebugger debugger) {
    // Empty default implementation
  }

  @Override
  public void changedDebuggerTemplate(final INaviModule module, final DebuggerTemplate template) {
    // Empty default implementation
  }

  @Override
  public void changedDescription(final INaviModule module, final String description) {
    // Empty default implementation
  }

  @Override
  public void changedFileBase(final INaviModule module, final IAddress fileBase) {
    // Empty default implementation
  }

  @Override
  public void changedImageBase(final INaviModule module, final IAddress imageBase) {
    // Empty default implementation
  }

  @Override
  public void changedModificationDate(final INaviModule module, final Date date) {
    // Empty default implementation
  }

  @Override
  public void changedName(final INaviModule module, final String name) {
    // Empty default implementation
  }

  @Override
  public void changedStarState(final INaviModule module, final boolean isStared) {
    // Empty default implementation
  }

  @Override
  public void closedModule(final CModule module, final ICallgraphView callgraphView,
      final List<IFlowgraphView> flowgraphs) {
    // Empty default implementation
  }

  @Override
  public boolean closingModule(final CModule module) {
    // Empty default implementation
    return true;
  }

  @Override
  public void deletedView(final INaviModule module, final INaviView view) {
    // Empty default implementation
  }

  @Override
  public boolean initializing(final ModuleInitializeEvents event, final int counter) {
    // Empty default implementation
    return true;
  }

  @Override
  public void loadedModule(final INaviModule module) {
    // Empty default implementation
  }

  @Override
  public boolean loading(final ModuleLoadEvents event, final int counter) {
    // Empty default implementation
    return true;
  }

  @Override
  public void initializedModule(final INaviModule module) {
    // Empty default implementation
  }
}
