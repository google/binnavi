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


// / Used to listen on modules.
/**
 * Interface that can be implemented by objects that want to be notified about changes in
 * {@link Module} objects.
 */
public interface IModuleListener {

  // ! Signals a new module trace.
  /**
   * Invoked after a new trace was added to the module.
   *
   * @param module The module where the new trace was created.
   * @param trace The new trace that was added to the module.
   */
  void addedTrace(Module module, Trace trace);

  // ! Signals a new module view.
  /**
   * Invoked after a new view was created in the module.
   *
   * @param module The module where the new view was created.
   * @param view The new view that was created in the module.
   */
  void addedView(Module module, View view);

  // ! Signals a change in the module debugger.
  /**
   * Invoked after the debugger used to debug the module changed.
   *
   * @param module The module whose debugger changed.
   * @param debugger The new debugger of the module.
   */
  void changedDebugger(Module module, Debugger debugger);

  // ! Signals a change in the module debugger template.
  /**
   * Invoked after the debugger template of the module changed.
   *
   * @param module The module whose debugger template changed.
   * @param template The new debugger template of the module.
   */
  void changedDebuggerTemplate(Module module, DebuggerTemplate template);

  // ! Signals a change in the module description.
  /**
   * Invoked after the description of the module changed.
   *
   * @param module The module whose description changed.
   * @param description The new description of the module.
   */
  void changedDescription(Module module, String description);

  // ! Signals a change in the module file base.
  /**
   * Invoked after the file base of the module changed.
   *
   * @param module The module whose file base changed.
   * @param fileBase The new file base of the module.
   */
  void changedFilebase(Module module, Address fileBase);

  // ! Signals a change in the module image base.
  /**
   * Invoked after the image base of the module changed.
   *
   * @param module The module whose image base changed.
   * @param imageBase The new image base of the module.
   */
  void changedImagebase(Module module, Address imageBase);

  // ! Signals a change in the module modification date.
  /**
   * Invoked after the modification date of the module changed.
   *
   * @param module The module whose modification date changed.
   * @param date The new modification date of the module.
   */
  void changedModificationDate(Module module, Date date);

  // ! Signals a change in the module name.
  /**
   * Invoked after the name of the module changed.
   *
   * @param module The module whose name changed.
   * @param name The new name of the module.
   */
  void changedName(Module module, String name);

  // ! Signals that the module was closed.
  /**
   * Invoked after the module was closed.
   *
   *  After this function was invoked, using parts of the module which must be loaded before they
   * can be used leads to undefined behavior.
   *
   * @param module The module that was closed.
   */
  void closedModule(Module module);

  // ! Signals that the module is about to be closed.
  /**
   * Invoked right before a module is closed. The listening object has the opportunity to veto the
   * close process if it still needs to work with the module.
   *
   * @param module The module that is about to be closed.
   *
   * @return True, to indicate that the module can be closed. False, to veto the close process.
   */
  boolean closingModule(Module module);

  // ! Signals the deletion of a module trace.
  /**
   * Invoked after a trace was deleted from the module.
   *
   *  After this function was invoked, further usage of the deleted trace or objects inside that
   * trace lead to undefined behavior.
   *
   * @param module The module where the trace was deleted.
   * @param trace The trace that was deleted from the module.
   */
  void deletedTrace(Module module, Trace trace);

  // ! Signals the deletion of a module view.
  /**
   * Invoked after a view was deleted from the module.
   *
   *  After this function was invoked, further usage of the deleted view or objects inside that view
   * lead to undefined behavior.
   *
   * @param module The module where the view was deleted.
   * @param view The view that was deleted from the module.
   */
  void deletedView(Module module, View view);

  // ! Signals that module information was loaded.
  /**
   * Invoked after the module was loaded.
   *
   * @param module The module that was loaded.
   */
  void loadedModule(Module module);
}
