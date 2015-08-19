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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphView;
import com.google.security.zynamics.binnavi.disassembly.IFlowgraphView;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.IAddress;

public final class MockModuleListener implements IModuleListener {
  public String eventList = "";

  public List<TraceList> addedTraces = new ArrayList<TraceList>();

  public List<TraceList> deletedTraces = new ArrayList<TraceList>();

  public List<INaviView> addedViews = new ArrayList<INaviView>();

  public List<INaviView> deletedViews = new ArrayList<INaviView>();

  public boolean canClose = false;

  @Override
  public void addedView(final INaviModule module, final INaviView view) {
    eventList += "addedView/";

    addedViews.add(view);
  }

  @Override
  public void changedData(final CModule module, final byte[] data) {
  }

  @Override
  public void changedDebugger(final INaviModule module, final IDebugger debugger) {
  }

  @Override
  public void changedDebuggerTemplate(final INaviModule module, final DebuggerTemplate template) {
  }

  @Override
  public void changedDescription(final INaviModule module, final String description) {
    eventList += String.format("changedDescription=%s/", description);
  }

  @Override
  public void changedFileBase(final INaviModule module, final IAddress fileBase) {
    eventList += String.format("changedFileBase=%s/", fileBase.toHexString());
  }

  @Override
  public void changedImageBase(final INaviModule module, final IAddress imageBase) {
    eventList += String.format("changedImageBase=%s/", imageBase.toHexString());
  }

  @Override
  public void changedModificationDate(final INaviModule module, final Date date) {
  }

  @Override
  public void changedName(final INaviModule module, final String name) {
    eventList += String.format("changedName=%s/", name);
  }

  @Override
  public void changedStarState(final INaviModule module, final boolean isStared) {
    // TODO Auto-generated method stub
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void closedModule(final CModule module, final ICallgraphView callgraphView,
      final List<IFlowgraphView> flowgraphs) {
    eventList += "closedModule";
  }

  @Override
  public boolean closingModule(final CModule module) {
    eventList += "closingModule/";

    return canClose;
  }

  @Override
  public void deletedView(final INaviModule module, final INaviView view) {
    eventList += "deletedView/";

    deletedViews.add(view);
  }

  @Override
  public boolean initializing(final ModuleInitializeEvents event, final int counter) {
    // TODO Auto-generated method stub
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void loadedModule(final INaviModule module) {

  }

  @Override
  public boolean loading(final ModuleLoadEvents event, final int counter) {
    return true;
  }

  @Override
  public void initializedModule(final INaviModule module) {
    // TODO Auto-generated method stub

  }
}
