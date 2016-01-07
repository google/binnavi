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
package com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Implementations;

import com.google.security.zynamics.binnavi.debug.models.memoryexpressions.MemoryExpression;
import com.google.security.zynamics.binnavi.debug.models.memoryexpressions.MinusExpression;
import com.google.security.zynamics.binnavi.debug.models.memoryexpressions.MultiplicationExpression;
import com.google.security.zynamics.binnavi.debug.models.memoryexpressions.NumericalValue;
import com.google.security.zynamics.binnavi.debug.models.memoryexpressions.PlusExpression;
import com.google.security.zynamics.binnavi.debug.models.memoryexpressions.Register;
import com.google.security.zynamics.binnavi.debug.models.memoryexpressions.SubExpression;
import com.google.security.zynamics.binnavi.debug.models.memoryexpressions.MemoryExpressionElement;
import com.google.security.zynamics.binnavi.debug.models.memoryexpressions.MemoryExpressionVisitor;
import com.google.security.zynamics.zylib.types.lists.FilledList;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * Visitor used to evaluate memory expressions.
 */
public class CEvaluationVisitor implements MemoryExpressionVisitor {
  /**
   * Expression bindings used for evaluation.
   */
  private final IMemoryExpressionBinding m_binding;

  /**
   * Keeps track of partially evaluated expressions.
   */
  private final Map<MemoryExpressionElement, BigInteger> m_partialEvaluationMap =
      new HashMap<MemoryExpressionElement, BigInteger>();

  /**
   * Error messages collected throughout the visitor.
   */
  private final List<String> m_errorMessages = new ArrayList<String>();

  /**
   * Creates a new visitor object.
   *
   * @param binding Expression bindings used for evaluation.
   */
  public CEvaluationVisitor(final IMemoryExpressionBinding binding) {
    m_binding = binding;
  }

  /**
   * Returns the error messages picked up during evaluation.
   *
   * @return The error messages picked up during evaluation.
   */
  public List<String> getErrorMessages() {
    return new FilledList<String>(m_errorMessages);
  }

  /**
   * Returns the value of an evaluated expression.
   *
   * @param expression The expression whose evaluated value is returned.
   *
   * @return The value of the expression.
   */
  public BigInteger getValue(final MemoryExpressionElement expression) {
    return m_partialEvaluationMap.get(expression);
  }

  @Override
  public void visit(final MemoryExpression memoryExpression) {
    final MemoryExpressionElement child = memoryExpression.getChild();

    child.visit(this);

    final BigInteger address = getValue(child);

    try {
      m_partialEvaluationMap.put(memoryExpression, m_binding.getValue(address));
    } catch (final CEvaluationException e) {
      m_errorMessages.add(String.format("Unknown memory address %s", address.toString(16)));
    }
  }

  @Override
  public void visit(final MinusExpression expression) {
    BigInteger value = null;

    for (final MemoryExpressionElement child : expression.getChildren()) {
      child.visit(this);

      final BigInteger childValue = getValue(child);

      if (childValue == null) {
        return;
      }

      if (value == null) {
        value = getValue(child);

        if (value == null) {
          return;
        }
      } else {
        value = value.subtract(getValue(child));
      }
    }

    m_partialEvaluationMap.put(expression, value);
  }

  @Override
  public void visit(final MultiplicationExpression expression) {
    BigInteger value = null;

    for (final MemoryExpressionElement child : expression.getChildren()) {
      child.visit(this);

      final BigInteger childValue = getValue(child);

      if (childValue == null) {
        return;
      }

      if (value == null) {
        value = getValue(child);

        if (value == null) {
          return;
        }
      } else {
        value = value.multiply(getValue(child));
      }
    }

    m_partialEvaluationMap.put(expression, value);
  }

  @Override
  public void visit(final NumericalValue expression) {
    m_partialEvaluationMap.put(expression, expression.getValue());
  }

  @Override
  public void visit(final PlusExpression expression) {
    BigInteger value = null;

    for (final MemoryExpressionElement child : expression.getChildren()) {
      child.visit(this);

      final BigInteger childValue = getValue(child);

      if (childValue == null) {
        return;
      }

      if (value == null) {
        value = getValue(child);

        if (value == null) {
          return;
        }
      } else {
        value = value.add(getValue(child));
      }
    }

    m_partialEvaluationMap.put(expression, value);
  }

  @Override
  public void visit(final Register expression) {
    try {
      m_partialEvaluationMap.put(expression, m_binding.getValue(expression.getName()));
    } catch (final CEvaluationException e) {
      m_errorMessages.add(String.format("Unknown register %s", expression.getName()));
    }
  }

  @Override
  public void visit(final SubExpression expression) {
    final MemoryExpressionElement child = expression.getChild();

    child.visit(this);

    final BigInteger childValue = getValue(child);

    if (childValue == null) {
      return;
    }

    m_partialEvaluationMap.put(expression, childValue);
  }
}
