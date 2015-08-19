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

import java.util.Date;

import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.IProjectListener;
import com.google.security.zynamics.binnavi.disassembly.ProjectLoadEvents;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;


public class MockProjectListener implements IProjectListener {

  @Override
  public void addedAddressSpace(final INaviProject project, final CAddressSpace space) {
  }

  @Override
  public void addedDebugger(final INaviProject project, final DebuggerTemplate debugger) {
  }

  @Override
  public void addedTrace(final INaviProject project, final TraceList trace) {
  }

  @Override
  public void addedView(final INaviProject project, final INaviView view) {
  }

  @Override
  public void changedDescription(final INaviProject project, final String description) {
  }

  @Override
  public void changedModificationDate(final INaviProject project, final Date date) {
  }

  @Override
  public void changedName(final INaviProject project, final String name) {
  }

  @Override
  public void closedProject(final CProject project) {
  }

  @Override
  public boolean closingProject(final CProject project) {
    return false;
  }

  @Override
  public void deletedView(final INaviProject project, final INaviView view) {

  }

  @Override
  public boolean deletingTrace(final INaviProject project, final TraceList trace) {
    return false;
  }

  @Override
  public void loadedProject(final CProject project) {
  }

  @Override
  public boolean loading(final ProjectLoadEvents event, final int counter) {
    return false;
  }

  @Override
  public void removedAddressSpace(final INaviProject project, final INaviAddressSpace addressSpace) {
  }

  @Override
  public void removedDebugger(final INaviProject project, final DebuggerTemplate debugger) {

  }

  @Override
  public void removedTrace(final INaviProject project, final TraceList trace) {
  }
}
