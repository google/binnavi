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

import java.util.Date;

import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;


/**
 * Adapter class that can be used instead of IProjectListener if only few of the project listener
 * methods are implemented by a listener object.
 */
public class CProjectListenerAdapter implements IProjectListener {
  @Override
  public void addedAddressSpace(final INaviProject project, final CAddressSpace space) {
    // Empty default implementation
  }

  @Override
  public void addedDebugger(final INaviProject project, final DebuggerTemplate debugger) {
    // Empty default implementation
  }

  @Override
  public void addedTrace(final INaviProject project, final TraceList trace) {
    // Empty default implementation
  }

  @Override
  public void addedView(final INaviProject project, final INaviView view) {
    // Empty default implementation
  }

  @Override
  public void changedDescription(final INaviProject project, final String description) {
    // Empty default implementation
  }

  @Override
  public void changedModificationDate(final INaviProject project, final Date date) {
    // Empty default implementation
  }

  @Override
  public void changedName(final INaviProject project, final String name) {
    // Empty default implementation
  }

  @Override
  public void closedProject(final CProject project) {
    // Empty default implementation
  }

  @Override
  public boolean closingProject(final CProject project) {
    // Empty default implementation
    return true;
  }

  @Override
  public void deletedView(final INaviProject project, final INaviView view) {
    // Empty default implementation
  }

  @Override
  public boolean deletingTrace(final INaviProject project, final TraceList trace) {
    // Empty default implementation
    return true;
  }

  @Override
  public void loadedProject(final CProject project) {
    // Empty default implementation
  }

  @Override
  public boolean loading(final ProjectLoadEvents event, final int counter) {
    // Empty default implementation
    return true;
  }

  @Override
  public void removedAddressSpace(final INaviProject project, final INaviAddressSpace addressSpace) {
    // Empty default implementation
  }

  @Override
  public void removedDebugger(final INaviProject project, final DebuggerTemplate debugger) {
    // Empty default implementation
  }

  @Override
  public void removedTrace(final INaviProject project, final TraceList trace) {
    // Empty default implementation
  }
}
