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
 * This interface must be implemented by all objects that want to be notified about changes in a
 * project.
 */
public interface IProjectListener {
  /**
   * Invoked when an address space was added to the project.
   * 
   * @param project The project where the address space was added.
   * @param space The address space that was added to the project.
   */
  void addedAddressSpace(INaviProject project, CAddressSpace space);

  /**
   * Invoked when a debugger was added to the project.
   * 
   * @param project The project where the debugger was added.
   * @param debugger The debugger that was added to the project.
   */
  void addedDebugger(INaviProject project, DebuggerTemplate debugger);

  /**
   * Invoked after a trace was added to the project.
   * 
   * @param project The project the trace was added to.
   * @param trace The trace added to the project.
   */
  void addedTrace(INaviProject project, TraceList trace);

  /**
   * Invoked after a view was added to the project.
   * 
   * @param project The project the view was added to.
   * @param view The view added to the project.
   */
  void addedView(final INaviProject project, final INaviView view);

  /**
   * Invoked when the project description changed.
   * 
   * @param project The project whose description changed.
   * @param description The new project description.
   */
  void changedDescription(INaviProject project, String description);

  /**
   * Invoked when the modification date of a project changed.
   * 
   * @param project The project whose modification date changed.
   * @param date The new modification date.
   */
  void changedModificationDate(INaviProject project, Date date);

  /**
   * Invoked when the name of a project changed.
   * 
   * @param project The project whose name changed.
   * @param name The new name of the project.
   */
  void changedName(INaviProject project, String name);

  /**
   * Invoked after a project was closed.
   * 
   * @param project The closed project.
   */
  void closedProject(CProject project);

  /**
   * Invoked before a project is closed.
   * 
   * @param project The project to be closed.
   * 
   * @return True, to allow the operation. False, to keep the project open.
   */
  boolean closingProject(CProject project);

  /**
   * Invoked after a view was removed from a project.
   * 
   * @param project The project from which the view was removed.
   * @param view The removed view.
   */
  void deletedView(INaviProject project, INaviView view);

  /**
   * Invoked before a trace is deleted from a project.
   * 
   * @param project The project from which the trace is deleted.
   * @param trace The trace to be deleted.
   * 
   * @return True, to allow the deletion. False, to veto it.
   */
  boolean deletingTrace(INaviProject project, TraceList trace);

  /**
   * Invoked when the project is loaded.
   * 
   * @param project The project that was loaded.
   */
  void loadedProject(CProject project);

  /**
   * Invoked after a new load event happened.
   * 
   * @param event The event.
   * @param counter The number of the event.
   * 
   * @return True, to continue loading. False, to cancel it.
   */
  boolean loading(ProjectLoadEvents event, int counter);

  /**
   * Invoked when an address space was removed from the project.
   * 
   * @param project The project where the address space was removed.
   * @param addressSpace The address space that was removed.
   */
  void removedAddressSpace(INaviProject project, INaviAddressSpace addressSpace);

  /**
   * Invoked when a debugger was removed from the project.
   * 
   * @param project The project where the debugger was removed.
   * @param debugger The debugger that was removed from the project.
   */
  void removedDebugger(INaviProject project, DebuggerTemplate debugger);

  /**
   * Invoked after a trace was removed from a project.
   * 
   * @param project The project from which the trace was removed.
   * @param trace The removed trace.
   */
  void removedTrace(INaviProject project, TraceList trace);
}
