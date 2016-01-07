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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.antlr.runtime.RecognitionException;

import com.google.security.zynamics.binnavi.Gui.FilterPanel.CCombinedFilter;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.CFilterRuleParser;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilter;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilterComponent;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilterFactory;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.IPredicateGenerator;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.AbstractTree.CAbstractAndExpression;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.AbstractTree.CAbstractOrExpression;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.AbstractTree.CPredicateExpression;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.AbstractTree.IAbstractNode;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.ConcreteTree.CAndExpression;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.ConcreteTree.COrExpression;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.ConcreteTree.IFilterExpression;


/**
 * Default filter creator which is the base class of all concrete filters that are used for
 * filtering tables.
 * 
 * @param <T> Type of the objects in the table.
 * @param <WrapperType> Type of the wrappers used by the filter.
 */
public abstract class CDefaultFilterCreator<T, WrapperType> implements IFilterFactory<T> {
  /**
   * List of individual object predicates applied by this filter.
   */
  private final Collection<IPredicateGenerator<WrapperType>> m_expressions;

  /**
   * Creates a new default filter.
   * 
   * @param expressions List of individual object predicates applied by this filter.
   */
  public CDefaultFilterCreator(
      final Collection<? extends IPredicateGenerator<WrapperType>> expressions) {
    m_expressions = new ArrayList<IPredicateGenerator<WrapperType>>(expressions);
  }

  /**
   * Converts a node tree into an expression tree.
   * 
   * @param node The root node of the tree to convert.
   * 
   * @return The converted expression tree.
   */
  private IFilterExpression<WrapperType> convert(final IAbstractNode node) {
    if (node instanceof CPredicateExpression) {
      final CPredicateExpression predicate = (CPredicateExpression) node;

      for (final IPredicateGenerator<WrapperType> expression : m_expressions) {
        if (expression.canParse(predicate.getText())) {
          return expression.createExpression(predicate.getText());
        }
      }

      throw new IllegalStateException();
    } else if (node instanceof CAbstractAndExpression) {
      final CAbstractAndExpression andExpression = (CAbstractAndExpression) node;

      final List<IFilterExpression<WrapperType>> children =
          new ArrayList<IFilterExpression<WrapperType>>();

      for (final IAbstractNode child : andExpression.getChildren()) {
        children.add(convert(child));
      }

      return new CAndExpression<WrapperType>(children);
    } else if (node instanceof CAbstractOrExpression) {
      final CAbstractOrExpression orExpression = (CAbstractOrExpression) node;

      final List<IFilterExpression<WrapperType>> children =
          new ArrayList<IFilterExpression<WrapperType>>();

      for (final IAbstractNode child : orExpression.getChildren()) {
        children.add(convert(child));
      }

      return new COrExpression<WrapperType>(children);
    }

    throw new IllegalStateException("IE01158: Not yet implemented");
  }

  /**
   * Validates a given object predicate expression tree.
   * 
   * @param root The root node of the tree to validate.
   * 
   * @return True, if the expression tree is a valid tree for filtering. False, otherwise.
   */
  private boolean validate(final IAbstractNode root) {
    if (root instanceof CPredicateExpression) {
      final CPredicateExpression predicate = (CPredicateExpression) root;

      for (final IPredicateGenerator<WrapperType> expression : m_expressions) {
        if (expression.canParse(predicate.getText())) {
          return true;
        }
      }

      return false;
    } else {
      return true;
    }
  }

  /**
   * Creates a filter for the given filter expression.
   * 
   * @param expression The expression the filter is created for.
   * 
   * @return The filter for the expression.
   */
  protected abstract IFilter<T> createFilter(final IFilterExpression<WrapperType> expression);

  @SuppressWarnings("unchecked")
  @Override
  public IFilter<T> createFilter(final String text) throws RecognitionException {
    final IFilterComponent<T> filterComponent = getFilterComponent();

    final IFilter<T> componentFilter =
        filterComponent == null ? null : filterComponent.createFilter();

    if ("".equals(text)) {
      return componentFilter == null ? null : componentFilter;
    }

    final IAbstractNode root = CFilterRuleParser.parse(text);

    if (validate(root)) {
      if (componentFilter == null) {
        return createFilter(convert(root));
      } else {
        final IFilter<T> filter = createFilter(convert(root));

        return new CCombinedFilter<T>(filter, componentFilter);
      }
    }

    return null;
  }

  @Override
  public void dispose() {
  }

  @Override
  public IFilterComponent<T> getFilterComponent() {
    return null;
  }
}
