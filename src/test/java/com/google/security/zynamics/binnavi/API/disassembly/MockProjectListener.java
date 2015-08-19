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
package com.google.security.zynamics.binnavi.API.disassembly;

import java.util.Date;

import com.google.security.zynamics.binnavi.API.disassembly.AddressSpace;
import com.google.security.zynamics.binnavi.API.disassembly.DebuggerTemplate;
import com.google.security.zynamics.binnavi.API.disassembly.IProjectListener;
import com.google.security.zynamics.binnavi.API.disassembly.Project;
import com.google.security.zynamics.binnavi.API.disassembly.Trace;
import com.google.security.zynamics.binnavi.API.disassembly.View;


public final class MockProjectListener implements IProjectListener {
  public String events = "";

  @Override
  public void addedAddressSpace(final Project project, final AddressSpace addressSpace) {
    events += "addedAddressSpace;";
  }

  @Override
  public void addedDebuggerTemplate(final Project project, final DebuggerTemplate template) {
    events += "addedDebuggerTemplate;";
  }

  @Override
  public void addedTrace(final Project project, final Trace trace) {
    events += "addedTrace;";
  }

  @Override
  public void addedView(final Project project, final View view) {
    events += "addedView;";
  }

  @Override
  public void changedDescription(final Project project, final String description) {
    events += "changedDescription;";
  }

  @Override
  public void changedModificationDate(final Project project, final Date date) {
    events += "changedModificationDate;";
  }

  @Override
  public void changedName(final Project project, final String name) {
    events += "changedName;";
  }

  @Override
  public void closedProject(final Project project) {
    events += "closedProject;";
  }

  @Override
  public boolean closingProject(final Project project) {
    events += "closingProject;";

    return true;
  }

  @Override
  public void deletedAddressSpace(final Project project, final AddressSpace addressSpace) {
    events += "deletedAddressSpace;";
  }

  @Override
  public void deletedTrace(final Project project, final Trace trace) {
    events += "deletedTrace;";
  }

  @Override
  public void deletedView(final Project project, final View view) {
    events += "deletedView;";
  }

  @Override
  public void loadedProject(final Project project) {
    events += "loadedProject;";
  }

  @Override
  public void removedDebuggerTemplate(final Project project, final DebuggerTemplate template) {
    events += "removedDebuggerTemplate;";
  }
}
