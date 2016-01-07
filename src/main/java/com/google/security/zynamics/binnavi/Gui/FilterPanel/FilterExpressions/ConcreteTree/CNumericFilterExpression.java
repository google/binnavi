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
package com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.ConcreteTree;

import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.FilterRelation;

/**
 * Base class for the evaluation of predicates on objects.
 * 
 * @param <T> Type of the elements to filter.
 */
public abstract class CNumericFilterExpression<T> implements IFilterExpression<T> {
  /**
   * Relation used to filter.
   */
  private final FilterRelation m_relation;

  /**
   * Value to compare the dynamic value to.
   */
  private final long m_value;

  /**
   * Creates a new expression object.
   * 
   * @param relation Relation used to filter.
   * @param value Value to compare the dynamic value to.
   */
  public CNumericFilterExpression(final FilterRelation relation, final long value) {
    m_relation = relation;
    m_value = value;
  }

  /**
   * Evaluates a single predicate.
   * 
   * @param dynamicValue The value of the object to filter.
   * 
   * @return True, if the object passes the filter. False, otherwise.
   */
  protected boolean evaluate(final long dynamicValue) {
    switch (m_relation) {
      case EQUAL_TO:
        return dynamicValue == m_value;
      case GREATER_EQUAL_THAN:
        return dynamicValue >= m_value;
      case GREATER_THAN:
        return dynamicValue > m_value;
      case LESS_EQUAL_TO:
        return dynamicValue <= m_value;
      case LESS_THAN:
        return dynamicValue < m_value;
      case NOT_EQUAL_TO:
        return dynamicValue != m_value;
      default:
        throw new IllegalStateException("IE01144: Unknown predicate");
    }
  }
}
