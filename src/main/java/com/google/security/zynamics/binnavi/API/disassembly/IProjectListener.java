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

// / Used to listen on address projects.
/**
 * Interface that can be implemented by objects that want to be notified about changes in
 * {@link Project} objects.
 */
public interface IProjectListener {
  // ! Signals a new address space in the project.
  /**
   * Invoked after an address space was added to the project.
   *
   * @param project The project where the address space was added.
   * @param addressSpace The address space that was added to the project.
   */
  void addedAddressSpace(Project project, AddressSpace addressSpace);

  // ! Signals a new debugger template in the project.
  /**
   * Invoked after a debugger template was added to the project.
   *
   * @param project The project where the debugger template was added.
   * @param template The debugger template that was added to the project.
   */
  void addedDebuggerTemplate(Project project, DebuggerTemplate template);

  // ! Signals a new project trace.
  /**
   * Invoked after a new trace was added to the project.
   *
   * @param project The project where the new trace was created.
   * @param trace The new trace that was added to the project.
   */
  void addedTrace(Project project, Trace trace);

  // ! Signals a new project view.
  /**
   * Invoked after a view was added to the project.
   *
   * @param project The project where the view was added.
   * @param view The view that was added to the project.
   */
  void addedView(Project project, View view);

  // ! Signals a change in the project description.
  /**
   * Invoked after the description of the project changed.
   *
   * @param project The project whose description changed.
   * @param description The new description value of the project.
   */
  void changedDescription(Project project, String description);

  // ! Signals a change in the project modification date.
  /**
   * Invoked after the modification date of the project changed.
   *
   * @param project The project whose modification date changed.
   * @param date The new modification date of the project.
   */
  void changedModificationDate(Project project, Date date);

  // ! Signals a change in the project name.
  /**
   * Invoked after the name of the project changed.
   *
   * @param project The project whose name changed.
   * @param name The new name of the project.
   */
  void changedName(Project project, String name);

  // ! Signals that the project was closed.
  /**
   * Invoked after a project was closed.
   *
   *  After this function was invoked, using parts of the project which must be loaded before they
   * can be used leads to undefined behavior.
   *
   * @param project The project that was closed.
   */
  void closedProject(Project project);

  // ! Signals that the project is about to be closed.
  /**
   * Invoked right before a project is closed. The listening object has the opportunity to veto the
   * close process if it still needs to work with the project.
   *
   * @param project The project that is about to be closed.
   *
   * @return True, to indicate that the project can be closed. False, to veto the close process.
   */
  boolean closingProject(Project project);

  // ! Signals the deletion of an address space.
  /**
   * Invoked after an address space was deleted from the project.
   *
   *  After this function was invoked, further usage of the deleted address space or objects inside
   * that address space lead to undefined behavior.
   *
   * @param project The project where the address space was deleted.
   * @param addressSpace The address space that was deleted from the project.
   */
  void deletedAddressSpace(Project project, AddressSpace addressSpace);

  // ! Signals the deletion of a project trace.
  /**
   * Invoked after a trace was deleted from the project.
   *
   *  After this function was invoked, further usage of the deleted trace or objects inside that
   * trace lead to undefined behavior.
   *
   * @param project The project where the trace was deleted.
   * @param trace The trace that was deleted from the project.
   */
  void deletedTrace(Project project, Trace trace);

  // ! Signals the deletion of a view.
  /**
   * Invoked after a view was deleted from the project.
   *
   *  After this function was invoked, further usage of the deleted view or objects inside that view
   * lead to undefined behavior.
   *
   * @param project The project where the view was deleted.
   * @param view The view that was deleted from the project.
   */
  void deletedView(Project project, View view);

  // ! Signals that the project was loaded.
  /**
   * Invoked after the project was loaded.
   *
   * @param project The project that was loaded.
   */
  void loadedProject(Project project);

  // ! Signals the removal of a debugger template.
  /**
   * Invoked after a debugger template was deleted from the project.
   *
   * @param project The project where the trace was deleted.
   * @param template The debugger template that was deleted from the project.
   */
  void removedDebuggerTemplate(Project project, DebuggerTemplate template);
}
