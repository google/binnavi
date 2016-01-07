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


// / Adapter class for functions
/**
 * Adapter class that can be used by objects that want to listen on functions but only need to
 * process few events.
 */
public class FunctionListenerAdapter implements IFunctionListener {
  @Override
  public void appendedComment(final Function function, final IComment comment) {
    // Adapter method
  }

  @Override
  public void changedDescription(final Function function, final String description) {
    // Adapter method
  }

  @Override
  public void changedName(final Function function, final String name) {
    // Adapter method
  }

  @Override
  public void closedFunction(final Function function) {
    // Adapter method
  }

  @Override
  public void deletedComment(final Function function, final IComment comment) {
    // Adapter method
  }

  @Override
  public void editedComment(final Function function, final IComment comment) {
    // Adapter method
  }

  @Override
  public void initializedComment(final Function function, final List<IComment> comment) {
    // Adapter method
  }

  @Override
  public void loadedFunction(final Function function) {
    // Adapter method
  }
}
