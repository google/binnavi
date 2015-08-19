/*
Copyright 2014 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.disassembly;

import java.awt.Color;
import java.util.HashSet;


import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.CTextNode;

public class CTextNodeFactory {
  public static CTextNode get() {

    final IComment comment = new CComment(null, CommonTestObjects.TEST_USER_1, null, "Foo");

    return new CTextNode(1, 0, 0, 0, 0, Color.RED, false, true, new HashSet<CTag>(),
        Lists.newArrayList(comment), new MockSqlProvider());
  }

  public static CTextNode getWithComment(final String commentText) {

    final IComment comment = new CComment(null, CommonTestObjects.TEST_USER_1, null, commentText);

    return new CTextNode(1, 0, 0, 0, 0, Color.RED, false, true, new HashSet<CTag>(),
        Lists.newArrayList(comment), new MockSqlProvider());
  }
}
