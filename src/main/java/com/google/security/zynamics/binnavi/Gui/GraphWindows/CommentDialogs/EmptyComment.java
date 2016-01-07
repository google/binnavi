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

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.Users.Interfaces.IUser;

public class EmptyComment implements IComment {

  @Override
  public String getComment() {
    return null;
  }

  @Override
  public Integer getId() {
    return null;
  }

  @Override
  public IComment getParent() {
    return null;
  }

  @Override
  public IUser getUser() {
    return null;
  }

  @Override
  public boolean hasParent() {
    return false;
  }

  @Override
  public boolean isStored() {
    return false;
  }

  @Override
  public int getNumberOfCommentLines() {
    return 0;
  }

  @Override
  public String getCommentLine(int index) {
    return null;
  }

  @Override
  public String[] getCommentLines() {
    return new String[0];
  }
}
