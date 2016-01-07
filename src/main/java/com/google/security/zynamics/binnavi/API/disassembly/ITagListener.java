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

// / Used to listen on tags.
/**
 * Interface that can be implemented by objects that want to be notified about changes in Tag
 * objects.
 */
public interface ITagListener {
  // ! Signals a new tag description.
  /**
   * Invoked after the description string of the tag changed.
   *
   * @param tag The tag whose description changed.
   * @param description The new description string of the tag.
   */
  void changedDescription(Tag tag, String description);

  // ! Signals a new tag name.
  /**
   * Invoked after the name string of the tag changed.
   *
   * @param tag The tag whose name changed.
   * @param name The new name string of the tag.
   */
  void changedName(Tag tag, String name);
}
