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
package com.google.security.zynamics.binnavi.Gui.Debug.EventLists;

import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.ConcreteTree.IFilterExpression;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceEvent;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;

/**
 * Default predicate that matches any filter text for trace events.
 */
public class CTraceFilterExpression implements IFilterExpression<CTraceEventWrapper> {
  /**
   * The text to match.
   */
  private final String m_text;

  /**
   * Creates a new expression object.
   *
   * @param text The input text.
   */
  public CTraceFilterExpression(final String text) {
    m_text = text;
  }

  @Override
  public boolean evaluate(final CTraceEventWrapper element) {
    final ITraceEvent event = element.unwrap();
    final INaviModule currentModule = event.getOffset().getModule();
    String functionName = "";
    String moduleName = "";

    if (currentModule != null) {
      final INaviFunction function = currentModule.isLoaded() ? currentModule.getContent()
          .getFunctionContainer().getFunction(event.getOffset().getAddress().getAddress()) : null;

      if (function != null) {
        functionName = function.getName();
      }

      moduleName = currentModule.getConfiguration().getName();
    }

    return String.valueOf(event.getThreadId()).contains(m_text)
        || event.getOffset().getAddress().getAddress().toHexString().contains(m_text)
        || moduleName.contains(m_text) || functionName.contains(m_text);
  }
}
