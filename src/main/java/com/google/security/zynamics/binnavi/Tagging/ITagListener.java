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
package com.google.security.zynamics.binnavi.Tagging;


/**
 * Listener class that can be implemented by objects that want to be notified about changes in tags.
 */
public interface ITagListener {
  /**
   * Invoked after the description of a tag changed.
   * 
   * @param tag The tag whose description changed.
   * @param description The new description of the tag.
   */
  void changedDescription(CTag tag, String description);

  /**
   * Invoked after the name of a tag changed.
   * 
   * @param tag The tag whose name changed.
   * @param name The new name of the of the tag.
   */
  void changedName(CTag tag, String name);

  /**
   * Invoked after a tag was deleted.
   * 
   * Note that using the deleted tag after this function was invoked leads to undefined behaviour.
   * 
   * @param tag The tag that was deleted.
   */
  void deletedTag(CTag tag);
}
