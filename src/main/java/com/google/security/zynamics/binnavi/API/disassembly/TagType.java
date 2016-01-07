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

/* ! \file TagType.java \brief Contains the TagType enumeration * */

/**
 * Tags can be either view tags or node tags.
 */
public enum TagType {
  /**
   * Tag type of tags that can be used to tag views.
   */
  ViewTag,

  /**
   * Tag type of tags that can be used to tag nodes.
   */
  NodeTag;

  // / @cond INTERNAL
  /**
   * Converts an internal tag type to an API tag type.
   *
   * @param type The tag type to convert.
   *
   * @return The converted tag type.
   */
  public static TagType convert(final com.google.security.zynamics.binnavi.Tagging.TagType type) {
    switch (type) {
      case NODE_TAG:
        return NodeTag;
      case VIEW_TAG:
        return ViewTag;
      default:
        throw new IllegalArgumentException("Error: Unknown tag type");
    }
  }

  /**
   * Converts an API tag type to an internal tag type.
   *
   * @return The internal tag type.
   */
  // / @endcond
  public com.google.security.zynamics.binnavi.Tagging.TagType getNative() {
    switch (this) {
      case NodeTag:
        return com.google.security.zynamics.binnavi.Tagging.TagType.NODE_TAG;
      case ViewTag:
        return com.google.security.zynamics.binnavi.Tagging.TagType.VIEW_TAG;
      default:
        throw new IllegalArgumentException("Error: Unknown tag type");
    }
  }
}
