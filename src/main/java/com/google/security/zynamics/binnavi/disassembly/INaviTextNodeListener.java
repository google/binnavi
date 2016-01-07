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
package com.google.security.zynamics.binnavi.disassembly;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.ITextNodeListener;

import java.util.List;



/**
 * Interface for classes that want to be informed about changes in text nodes.
 * 
 * @author timkornau
 * 
 */
public interface INaviTextNodeListener extends INaviViewNodeListener, ITextNodeListener<CTextNode> {

  /**
   * Invoked after a text node comment has been appended.
   * 
   * @param node The text node where the comment has been appended.
   * @param comment The comment which has been appended.
   */
  void appendedTextNodeComment(INaviTextNode node, IComment comment);

  /**
   * Invoked after a text node comment has been deleted.
   * 
   * @param node The text node where the comment has been deleted.
   * @param comment The comment which has been deleted.
   */
  void deletedTextNodeComment(INaviTextNode node, IComment comment);

  /**
   * Invoked after a text node comment has been edited.
   * 
   * @param node The text node where the comment has been edited.
   * @param comment The comment which has been edited.
   */
  void editedTextNodeComment(INaviTextNode node, IComment comment);

  /**
   * Invoked after the comments of a text node have been initialized.
   * 
   * @param node The text node where the comments have been initialized.
   * @param comments The comments with which the the text node comments have been initialized.
   */
  void initializedTextNodeComment(INaviTextNode node, List<IComment> comments);
}
