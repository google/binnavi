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

import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.FilterRelation;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.ConcreteTree.IFilterExpression;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceRegister;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceEvent;

/**
 * Filter expression to filter trace events by register values.
 */
public class CRegisterFilterExpression implements IFilterExpression<CTraceEventWrapper> {
  /**
   * Register name to search for.
   */
  private final String m_register;

  /**
   * Relation between the register value and the search value.
   */
  private final FilterRelation m_predicate;

  /**
   * Value to search for.
   */
  private final long m_value;

  /**
   * Creates a new filter expression.
   *
   * @param register Register name to search for.
   * @param predicate Relation between the register value and the search value.
   * @param value Value to search for.
   */
  public CRegisterFilterExpression(
      final String register, final FilterRelation predicate, final long value) {
    m_register = register;
    m_predicate = predicate;
    m_value = value;
  }

  /**
   * Evaluates an expression.
   *
   * @param predicate The expression predicate.
   * @param register The left-hand side of the expression.
   * @param value The right-hand side of the expression.
   *
   * @return The result of the evaluated expression.
   */
  private static boolean evaluateExpression(
      final FilterRelation predicate, final TraceRegister register, final long value) {
    switch (predicate) {
      case EQUAL_TO:
        return register.getValue().toLong() == value;
      case GREATER_EQUAL_THAN:
        return register.getValue().toLong() >= value;
      case GREATER_THAN:
        return register.getValue().toLong() > value;
      case LESS_EQUAL_TO:
        return register.getValue().toLong() <= value;
      case LESS_THAN:
        return register.getValue().toLong() < value;
      case NOT_EQUAL_TO:
        return register.getValue().toLong() != value;
      default:
        throw new IllegalStateException("IE00264: Unknown predicate");
    }
  }

  @Override
  public boolean evaluate(final CTraceEventWrapper element) {
    final ITraceEvent trace = element.unwrap();

    boolean returnValue = false;

    for (final TraceRegister register : trace.getRegisterValues()) {
      if ("any".equalsIgnoreCase(m_register) || register.getName().equalsIgnoreCase(m_register)) {
        returnValue |= evaluateExpression(m_predicate, register, m_value);
      }
    }

    return returnValue;
  }
}
