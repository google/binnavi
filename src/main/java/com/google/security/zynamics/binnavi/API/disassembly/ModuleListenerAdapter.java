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
package com.google.security.zynamics.binnavi.API.disassembly;

import java.util.Date;

import com.google.security.zynamics.binnavi.API.debug.Debugger;


// / Simplifies module listeners.
/**
 * Adapter class that can be used by classes that want to listen on modules but only need to be
 * notified about few events.
 */
public class ModuleListenerAdapter implements IModuleListener {
  @Override
  public void addedTrace(final Module module, final Trace trace) {
    // Adapter method
  }

  @Override
  public void addedView(final Module module, final View view) {
    // Adapter method
  }

  @Override
  public void changedDebugger(final Module module, final Debugger debugger) {
    // Adapter method
  }

  @Override
  public void changedDebuggerTemplate(final Module module, final DebuggerTemplate template) {
    // Adapter method
  }

  @Override
  public void changedDescription(final Module module, final String description) {
    // Adapter method
  }

  @Override
  public void changedFilebase(final Module module, final Address fileBase) {
    // Adapter method
  }

  @Override
  public void changedImagebase(final Module module, final Address imageBase) {
    // Adapter method
  }

  @Override
  public void changedModificationDate(final Module module, final Date date) {
    // Adapter method
  }

  @Override
  public void changedName(final Module module, final String name) {
    // Adapter method
  }

  @Override
  public void closedModule(final Module module) {
    // Adapter method
  }

  @Override
  public boolean closingModule(final Module module) {
    return true;
  }

  @Override
  public void deletedTrace(final Module module, final Trace trace) {
    // Adapter method
  }

  @Override
  public void deletedView(final Module module, final View view) {
    // Adapter method
  }

  @Override
  public void loadedModule(final Module module) {
    // Adapter method
  }
}
