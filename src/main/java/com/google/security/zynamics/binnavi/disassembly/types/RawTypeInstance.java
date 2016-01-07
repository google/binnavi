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
package com.google.security.zynamics.binnavi.disassembly.types;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;

/**
 * Represents a type instance as loaded from the database.
 */
public class RawTypeInstance {

  /**
   * The module id of the {@link RawTypeInstance}.
   */
  private final int moduleId;

  /**
   * The id of the {@link RawTypeInstance}.
   */
  private final int id;

  /**
   * The name of the {@link RawTypeInstance}.
   */
  private final String name;

  /**
   * The comment id of the {@link IComment} associated to this {@link RawTypeInstance}.
   */
  private final Integer commentId;

  /**
   * The id of the {@link BaseType} associated with this {@link RawTypeInstance}.
   */
  private final int typeId;

  /**
   * The id of the {@link Section} the {@link RawTypeInstance} is located in.
   */
  private final int sectionId;

  /**
   * The offset within the {@link Section} the {@link RawTypeInstance} is located in.
   */
  private final long sectionOffset;

  /**
   * Creates a new raw type instance object.
   * 
   * @param moduleId The id of the module that contains this type instance.
   * @param id The id of this type instance.
   * @param name The name of the type instance (i.e. the name of the in-memory global variable).
   * @param commentId The id of the comment that is associated with this type instance (can be
   *        null).
   * @param typeId The id of the base type that is associated with this type instance.
   * @param sectionId The id of the section that contains this type instance.
   * @param sectionOffset The offset of this type instance relative to the beginning of the
   *        containing section.
   */
  public RawTypeInstance(final int moduleId, final int id, final String name,
      final Integer commentId, final int typeId, final int sectionId, final long sectionOffset) {
    this.moduleId = moduleId;
    this.id = id;
    this.name = Preconditions.checkNotNull(name, "Error: name can not be null");
    this.commentId = commentId;
    this.typeId = typeId;
    this.sectionId = sectionId;
    Preconditions.checkArgument(sectionOffset >= 0, "Error: section offset can not be negative.");
    this.sectionOffset = sectionOffset;
  }

  public Integer getCommentId() {
    return commentId;
  }

  public int getId() {
    return id;
  }

  public int getModuleId() {
    return moduleId;
  }

  public String getName() {
    return name;
  }

  public int getSectionId() {
    return sectionId;
  }

  public long getSectionOffset() {
    return sectionOffset;
  }

  public int getTypeId() {
    return typeId;
  }
}
