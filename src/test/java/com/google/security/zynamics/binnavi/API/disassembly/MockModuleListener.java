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

import com.google.security.zynamics.binnavi.API.debug.Debugger;
import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.API.disassembly.DebuggerTemplate;
import com.google.security.zynamics.binnavi.API.disassembly.IModuleListener;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.API.disassembly.Trace;
import com.google.security.zynamics.binnavi.API.disassembly.View;


public final class MockModuleListener implements IModuleListener {
  public String events = "";

  @Override
  public void addedTrace(final Module module, final Trace trace) {
    events += "addedTrace;";
  }

  @Override
  public void addedView(final Module module, final View view) {
    events += "addedView;";
  }

  @Override
  public void changedDebugger(final Module module, final Debugger debugger) {
    events += "changedDebugger;";
  }

  @Override
  public void changedDebuggerTemplate(final Module module, final DebuggerTemplate template) {
    events += "changedDebuggerTemplate;";
  }

  @Override
  public void changedDescription(final Module module, final String description) {
    events += "changedDescription;";
  }

  @Override
  public void changedFilebase(final Module module, final Address fileBase) {
    events += "changedFilebase;";
  }

  @Override
  public void changedImagebase(final Module module, final Address imageBase) {
    events += "changedImagebase;";
  }

  @Override
  public void changedModificationDate(final Module module, final Date date) {
    events += "changedModificationDate;";
  }

  @Override
  public void changedName(final Module module, final String name) {
    events += "changedName;";
  }

  @Override
  public void closedModule(final Module module) {
    events += "closedModule;";
  }

  @Override
  public boolean closingModule(final Module module) {
    events += "closingModule;";

    return true;
  }

  @Override
  public void deletedTrace(final Module module, final Trace trace) {
    events += "deletedTrace;";
  }

  @Override
  public void deletedView(final Module module, final View view) {
    events += "deletedView;";
  }

  @Override
  public void loadedModule(final Module module) {
    events += "loadedModule;";
  }
}
