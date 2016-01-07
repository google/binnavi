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
package com.google.security.zynamics.binnavi.ZyGraph.Builders;

import java.awt.Color;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.Users.Interfaces.IUser;

public class CommentContainer {

  final IComment comment;
  final String userNameDelimiter = ": ";
  final Color commentColor;

  public CommentContainer(final IComment comment) {
    this.comment = Preconditions.checkNotNull(comment, "IE02730: comment argument can not be null");
    this.commentColor =
        Color.getHSBColor(
            Float.parseFloat("0." + String.valueOf(comment.getUser().getUserId()) + "f"), 1f, 0.8f);
  }

  public IComment getComment() {
    return this.comment;
  }

  public Color getCommentColor() {
    return this.commentColor;
  }

  public List<String> getCommentingString() {
    if (this.comment == null) {
      return null;
    }
    if (this.comment.getComment().split("\\n").length <= 1) {
      return Lists.newArrayList(this.comment.getUser().getUserName() + userNameDelimiter
          + this.comment.getComment());
    }
    if (this.comment.getComment().split("\\n").length > 1) {
      final String[] commentFragments = this.comment.getComment().split("\\n");
      final List<String> generatedComment =
          Lists.newArrayList(this.comment.getUser().getUserName() + userNameDelimiter
              + commentFragments[0]);
      for (int i = 1; i < commentFragments.length; i++) {
        generatedComment.add(Strings.repeat(" ", this.comment.getUser().getUserName().length()
            + userNameDelimiter.length())
            + commentFragments[i]);
      }
      return generatedComment;
    }
    return null;
  }

  public IUser getCommentUser() {
    return this.comment.getUser();
  }

  public int getCommentUserNameLength() {
    return this.comment.getUser().getUserName().length();
  }

  public int getUserNameDelimiterLength() {
    return this.userNameDelimiter.length();
  }
}
