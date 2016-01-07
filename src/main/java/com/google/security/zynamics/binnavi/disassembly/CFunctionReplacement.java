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

/**
 * Represents an operand tree replacement that refers to a function.
 */
public class CFunctionReplacement extends CAbstractReplacement {
  /**
   * The function the replacement refers to.
   */
  private final INaviFunction m_function;

  /**
   * Keeps track of the refered function.
   */
  private final IFunctionListener<IComment> m_listener = new InternalFunctionListener();

  /**
   * Creates a new function replacement object.
   * 
   * @param function The function the replacement refers to.
   */
  public CFunctionReplacement(final INaviFunction function) {
    m_function = function;

    m_function.addListener(m_listener);
  }

  @Override
  public CFunctionReplacement cloneReplacement() {
    return new CFunctionReplacement(m_function);
  }

  @Override
  public void close() {
    m_function.removeListener(m_listener);
  }

  /**
   * Returns the function the replacement refers to.
   * 
   * @return The function the replacement refers to.
   */
  public INaviFunction getFunction() {
    return m_function;
  }

  @Override
  public String toString() {
    return m_function.getName();
  }

  /**
   * Keeps track of the refered function.
   */
  private class InternalFunctionListener extends CFunctionListenerAdapter {
    @Override
    public void changedName(final IFunction function, final String name) {
      notifyListeners();
    }
  }
}
