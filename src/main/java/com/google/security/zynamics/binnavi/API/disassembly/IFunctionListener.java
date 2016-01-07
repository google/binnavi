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

import java.util.List;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;


// / Used to listen on functions.
/**
 * Interface that can be implemented by objects that want to be notified about changes in
 * {@link Function} objects.
 */
public interface IFunctionListener {

  // ! Signals an appended function comment.
  /**
   * Invoked after a comment was appended to the function.
   * 
   * @param function The function to which the comment is associated.
   * @param comment The appended comment of the function.
   */
  void appendedComment(Function function, IComment comment);

  // ! Signals a new function description.
  /**
   * Invoked after the description of the function changed.
   * 
   * @param function The function whose description changed.
   * @param description The new description of the function.
   */
  void changedDescription(Function function, String description);

  // ! Signals a new function name.
  /**
   * Invoked after the name of the function changed.
   * 
   * @param function The function whose description changed.
   * @param name The new name of the function.
   */
  void changedName(Function function, String name);

  // ! Signals that function data was closed.
  /**
   * Invoked after the function was closed. The content of the function can not be used anymore
   * until the function is reloaded.
   * 
   * @param function The function that was closed.
   */
  void closedFunction(Function function);

  // ! Signals a deleted function comment.
  /**
   * Invoked after a comment of the function has been deleted.
   * 
   * @param function The function whose comment was deleted.
   * @param comment The deleted comment of the function.
   */
  void deletedComment(Function function, IComment comment);

  // ! Signals an edited function comment.
  /**
   * Invoked after the comment of the function has been edited.
   * 
   * @param function The function whose comment was edited.
   * @param comment The edited comment of the function.
   */
  void editedComment(Function function, IComment comment);

  // ! Signals an initialization of a function comment.
  /**
   * Invoked after the comment of a function has been initialized.
   * 
   * @param function The function whose comment was initialized.
   * @param comment The list of comments with which the functions comments where initialized.
   */
  void initializedComment(Function function, List<IComment> comment);

  // ! Signals that function data was loaded.
  /**
   * Invoked after the function was loaded from the database.
   * 
   * @param function The function that was loaded from the database.
   */
  void loadedFunction(Function function);
}
