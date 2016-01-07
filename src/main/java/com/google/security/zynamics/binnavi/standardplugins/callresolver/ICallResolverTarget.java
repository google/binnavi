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
package com.google.security.zynamics.binnavi.standardplugins.callresolver;

import java.util.List;

import com.google.security.zynamics.binnavi.API.debug.Debugger;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.API.disassembly.View;


/**
 * Interface to be implemented by objects that want to act as call resolver targets.
 */
public interface ICallResolverTarget {
  /**
   * Adds a listener object that is notified about changes in the call resolver target.
   * 
   * @param listener The listener object to add.
   */
  void addListener(ICallResolverTargetListener listener);

  /**
   * Creates a new view in the call resolver target.
   * 
   * @return The created view.
   */
  View createView();

  /**
   * Returns the debugger that is used to debug the call resolver target.
   * 
   * @return The active debugger.
   */
  Debugger getDebugger();

  /**
   * Returns the indirect calls of the call resolver target.
   * 
   * @return The indirect calls of the call resolver target.
   */
  List<IndirectCall> getIndirectCalls();

  /**
   * Returns all modules that belong to the call resolver target.
   * 
   * @return All modules that belong to the call resolver target.
   */
  List<Module> getModules();

  /**
   * Removes a listener object that was previously listening.
   * 
   * @param listener The listener object to remove.
   */
  void removeListener(ICallResolverTargetListener listener);
}
