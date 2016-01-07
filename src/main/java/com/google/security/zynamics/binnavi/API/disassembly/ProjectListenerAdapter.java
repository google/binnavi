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

// / Adapter class for projects
/**
 * Adapter class that can be used by objects that want to listen on projects but only need to
 * process few events.
 */
public class ProjectListenerAdapter implements IProjectListener {
  @Override
  public void addedAddressSpace(final Project project, final AddressSpace addressSpace) {
    // Adapter method
  }

  @Override
  public void addedDebuggerTemplate(final Project project, final DebuggerTemplate template) {
    // Adapter method
  }

  @Override
  public void addedTrace(final Project project, final Trace trace) {
    // Adapter method
  }

  @Override
  public void addedView(final Project project, final View view) {
    // Adapter method
  }

  @Override
  public void changedDescription(final Project project, final String description) {
    // Adapter method
  }

  @Override
  public void changedModificationDate(final Project project, final Date date) {
    // Adapter method
  }

  @Override
  public void changedName(final Project project, final String name) {
    // Adapter method
  }

  @Override
  public void closedProject(final Project project) {
    // Adapter method
  }

  @Override
  public boolean closingProject(final Project project) {
    return true;
  }

  @Override
  public void deletedAddressSpace(final Project project, final AddressSpace addressSpace) {
    // Adapter method
  }

  @Override
  public void deletedTrace(final Project project, final Trace trace) {
    // Adapter method
  }

  @Override
  public void deletedView(final Project project, final View view) {
    // Adapter method
  }

  @Override
  public void loadedProject(final Project project) {
    // Adapter method
  }

  @Override
  public void removedDebuggerTemplate(final Project project, final DebuggerTemplate template) {
    // Adapter method
  }
}
