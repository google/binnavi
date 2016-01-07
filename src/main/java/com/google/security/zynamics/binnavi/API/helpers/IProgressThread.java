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
package com.google.security.zynamics.binnavi.API.helpers;

// / Helper class for showing progress dialogs
/**
 * Interface that must be implemented by all objects that want to execute long operations while a
 * progress dialog is shown.
 *
 *  Please note that the implementing object does not actually have to be a thread.
 */
public interface IProgressThread {
  // ! Invoked to stop a progress thread.
  /**
   * This function is invoked every time the user clicks on the X button of the progress dialog.
   * Plugins that use the progress dialog have the change to clean up allocated resources before the
   * dialog is closed.
   *
   * @return True, to signal that the progress thread can be terminated. False, if the progress
   *         dialog can not be cancelled.
   */
  boolean close();

  // ! Executes a long operation.
  /**
   * Executes the long operation for which a progress dialog is shown.
   *
   * @throws Exception Exception thrown by the long-running operation.
   */
  void run() throws Exception;
}
