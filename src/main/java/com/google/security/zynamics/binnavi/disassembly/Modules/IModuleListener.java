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
package com.google.security.zynamics.binnavi.disassembly.Modules;

import java.util.Date;
import java.util.List;

import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphView;
import com.google.security.zynamics.binnavi.disassembly.IFlowgraphView;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.IAddress;



/**
 * Listener object that must be implemented by all objects that want to be notified about changes in
 * a module.
 */
public interface IModuleListener {
  /**
   * Invoked after a view was added to a module.
   * 
   * @param module The module the view was added to.
   * @param view The view that was added to the module.
   */
  void addedView(INaviModule module, INaviView view);

  /**
   * Invoked after the data of a module changed.
   * 
   * @param module The module whose data changed.
   * @param data The new data of the module.
   */
  void changedData(CModule module, byte[] data);

  /**
   * Invoked after the debugger assigned to a module changed.
   * 
   * @param module The module whose assigned debugger changed.
   * @param debugger The new assigned debugger. This value can be null if no debugger was assigned.
   */
  void changedDebugger(INaviModule module, IDebugger debugger);

  /**
   * Invoked after the debugger template of a module changed.
   * 
   * @param module The module whose assigned debugger template changed.
   * @param template The new assigned debugger template. This value can be null if no debugger
   *        template was assigned.
   */
  void changedDebuggerTemplate(INaviModule module, DebuggerTemplate template);

  /**
   * Invoked after the description of a module changed.
   * 
   * @param module The module whose description changed.
   * @param description The new description of the module.
   */
  void changedDescription(INaviModule module, String description);

  /**
   * Invoked after the filebase of a module changed.
   * 
   * @param module The module whose filebase changed.
   * @param fileBase The new filebase of the module.
   */
  void changedFileBase(INaviModule module, IAddress fileBase);

  /**
   * Invoked after the imagebase of a module changed.
   * 
   * @param module The module whose imagebase changed.
   * @param imageBase The new imagebase of the module.
   */
  void changedImageBase(INaviModule module, IAddress imageBase);

  /**
   * Invoked after the modification date of a module changed.
   * 
   * @param module The module whose modification date changed.
   * @param date The new modification date of the module.
   */
  void changedModificationDate(INaviModule module, Date date);

  /**
   * Invoked after the name of a module changed.
   * 
   * @param module The module whose name changed.
   * @param name The new name of the module.
   */
  void changedName(INaviModule module, String name);

  /**
   * Invoked after the star state of a module changed.
   * 
   * @param module The module whose star state changed.
   * @param isStared The new star state.
   */
  void changedStarState(INaviModule module, boolean isStared);

  /**
   * Invoked after a module was closed.
   * 
   * @param module The module that was closed.
   * @param callgraphView The Call graph view of the closed module.
   * @param flowgraphs The Flow graph views of the closed module.
   */
  void closedModule(CModule module, ICallgraphView callgraphView, List<IFlowgraphView> flowgraphs);

  /**
   * Invoked right before a module is closed.
   * 
   * @param module The module that is about to be closed.
   * 
   * @return True, to allow the module to be closed. False, to veto the close operation.
   */
  boolean closingModule(CModule module);

  /**
   * Invoked after a view was deleted from a module.
   * 
   * @param module The module from where the view was deleted.
   * @param view The view that was deleted from the module.
   */
  void deletedView(INaviModule module, INaviView view);

  /**
   * Invoked after a new initialization event happened.
   * 
   * @param event The event.
   * @param counter The number of the event.
   * 
   * @return True, to continue initializing. False, to cancel it.
   */
  boolean initializing(ModuleInitializeEvents event, int counter);

  /**
   * Invoked after a module has been initialized
   * 
   * @param module The module that was initialized.
   */
  void initializedModule(INaviModule module);

  /**
   * Invoked after a module is loaded.
   * 
   * @param module The module that was loaded.
   */
  void loadedModule(INaviModule module);

  /**
   * Invoked after a new load event happened.
   * 
   * @param event The event.
   * @param counter The number of the event.
   * 
   * @return True, to continue loading. False, to cancel it.
   */
  boolean loading(ModuleLoadEvents event, int counter);
}
