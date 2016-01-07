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


import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.Users.Interfaces.IUser;

import java.util.List;
import java.util.Objects;

public class CComment implements IComment {
  /**
   * The id of the comment.
   */
  private final Integer id;

  /**
   * The identity which created the comment (owner).
   */
  private final IUser user;

  /**
   * The comment id of the potential parent of the comment.
   */
  private final IComment parent;

  /**
   * The actual comment string.
   */
  private final String comment;

  /**
   * For convenience: The comment string as a set of substrings, each per-line.
   */
  private final String[] lines;

  /**
   * Creates a new comment object
   *
   * @param id The id of the comment.
   * @param user The user of the comment.
   * @param parent The id of a potential parent comment, can be null.
   * @param comment The actual comment string.
   */
  public CComment(final Integer id, final IUser user, final IComment parent, final String comment) {
    Preconditions.checkArgument(((id == null) || (id > 0)),
        "Error: id can only be larger then zero or null");
    this.id = id;
    this.user = Preconditions.checkNotNull(user, "IE02631: user argument can not be null");
    this.parent = parent;
    this.comment = Preconditions.checkNotNull(comment, "IE02632: comment argument can not be null");
    Preconditions.checkArgument(!comment.isEmpty(), "Error: comment must be a non empty string");
    List<String> linesList = Splitter.on('\n').splitToList(comment);
    lines = linesList.toArray(new String[linesList.size()]);
   }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CComment other = (CComment) obj;

    return Objects.equals(this.id, other.id) && Objects.equals(this.user, other.user)
        && Objects.equals(this.parent, other.parent) && Objects.equals(this.comment, other.comment);
  }

  @Override
  public String getComment() {
    return comment;
  }

  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public IComment getParent() {
    return parent;
  }

  @Override
  public IUser getUser() {
    return user;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getUser(), getParent(), getComment());
  }

  @Override
  public boolean hasParent() {
    return (parent != null);
  }

  @Override
  public boolean isStored() {
    return id != null;
  }

  @Override
  public int getNumberOfCommentLines() {
    return lines.length;
  }

  @Override
  public String getCommentLine(int index) {
    if (index >= lines.length) {
      return null;
    }
    return lines[index];
  }

  @Override
  public String[] getCommentLines() {
    return lines;
  }
}
