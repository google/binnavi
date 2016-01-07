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
import com.google.security.zynamics.zylib.disassembly.IFunction;
import com.google.security.zynamics.zylib.disassembly.IFunctionListener;

import java.util.List;



/**
 * Adapter class for object that want to listen on just a few function events.
 */
public class CFunctionListenerAdapter implements IFunctionListener<IComment> {
  @Override
  public void appendedComment(final IFunction function, final IComment comment) {
    // Empty default implementation
  }

  @Override
  public void changedDescription(final IFunction function, final String description) {
    // Empty default implementation
  }

  @Override
  public void changedName(final IFunction function, final String name) {
    // Empty default implementation
  }

  @Override
  public void changedForwardedFunction(final IFunction function) {
    // Empty default implementation
  }

  @Override
  public void closed(final IFunction function) {
    // Empty default implementation
  }

  @Override
  public void deletedComment(final IFunction function, final IComment comment) {
    // Empty default implementation
  }

  @Override
  public void editedComment(final IFunction function, final IComment comment) {
    // Empty default implementation
  }

  @Override
  public void initializedComment(final IFunction function, final List<IComment> comment) {
    // Empty default implementation
  }

  @Override
  public void loadedFunction(final IFunction function) {
    // Empty default implementation
  }
}
