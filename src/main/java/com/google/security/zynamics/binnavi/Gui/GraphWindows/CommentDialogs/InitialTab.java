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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs;

/**
 * Enumeration class used to select the initially visible tab in node comment editing dialogs.
 */
public enum InitialTab {
  /**
   * Initially select the local line comments editor.
   */
  LocalLineComments,

  /**
   * Initially select the global line comments editor.
   */
  GlobalLineComments,

  /**
   * Initially select the local node comments editor.
   */
  LocalNodeComments,

  /**
   * Initially select the global node comments editor.
   */
  GlobalNodeComments,

  /**
   * Initially select the function comments editor.
   */
  FunctionComments
}
