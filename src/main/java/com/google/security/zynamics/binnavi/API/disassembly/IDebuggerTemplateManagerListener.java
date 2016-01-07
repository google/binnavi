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

// / Used to listen on debugger template managers.
/**
 * Interface that can be implemented by objects that want to be notified about changes in
 * DebuggerTemplateManager objects.
 */
public interface IDebuggerTemplateManagerListener {
  // ! Signals a new debugger template.
  /**
   * Invoked after new debugger template was added to the debugger template manager.
   *
   * @param manager The debugger template manager where the new debugger template configuration was
   *        added.
   * @param template The new debugger template that was added to the debugger template manager.
   */
  void addedDebuggerTemplate(DebuggerTemplateManager manager, DebuggerTemplate template);

  // ! Signals the deletion of a debugger template.
  /**
   * Invoked after a debugger template was deleted from the debugger template manager.
   *
   * @param manager The debugger template manager where the debugger template was deleted.
   * @param template The debugger template that was deleted.
   */
  void deletedDebuggerTemplate(DebuggerTemplateManager manager, DebuggerTemplate template);
}
