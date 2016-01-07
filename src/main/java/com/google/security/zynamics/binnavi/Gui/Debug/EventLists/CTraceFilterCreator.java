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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.DefaultFilter;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilter;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.FilterRelation;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.IPredicateGenerator;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.ConcreteTree.IFilterExpression;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.Wrappers.IFilterWrapper;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.Wrappers.IWrapperCreator;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.filters.CDefaultFilterCreator;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceEvent;

/**
 * Creator class for trace event filters.
 */
public class CTraceFilterCreator extends CDefaultFilterCreator<ITraceEvent, CTraceEventWrapper> {
  /**
   * Creates a new filter creator object.
   */
  @SuppressWarnings("unchecked")
  public CTraceFilterCreator() {
    super(Lists.newArrayList(new MemoryPredicateGenerator(), new RegisterPredicateGenerator(),
        new PlainTextPredicateGenerator()));
  }

  @Override
  protected IFilter<ITraceEvent> createFilter(final IFilterExpression<CTraceEventWrapper> expression) {
    return new DefaultFilter<ITraceEvent, CTraceEventWrapper>(expression,
        new IWrapperCreator<ITraceEvent>() {
          @Override
          public IFilterWrapper<ITraceEvent> wrap(final ITraceEvent element) {
            return new CTraceEventWrapper(element);
          }
        });
  }

  /**
   * Creates event list memory content filters.
   */
  private static class MemoryPredicateGenerator implements IPredicateGenerator<CTraceEventWrapper> {
    /**
     * Regular expression for parsing memory content filter strings.
     */
    private static final String RULE_REGEX = "\\s*mem\\s*==\\s*([0-9a-fA-F]+)\\s*";

    @Override
    public boolean canParse(final String text) {
      return text.matches(RULE_REGEX);
    }

    @Override
    public IFilterExpression<CTraceEventWrapper> createExpression(final String text) {
      final Pattern pattern = Pattern.compile(RULE_REGEX);
      final Matcher matcher = pattern.matcher(text);

      matcher.matches();

      final String data =
          (matcher.group(1).length() % 2) == 0 ? matcher.group(1) : "0" + matcher.group(1);

      return new CMemoryFilterExpression(data);
    }
  }

  /**
   * Creates default event list filters.
   */
  private static class PlainTextPredicateGenerator implements
      IPredicateGenerator<CTraceEventWrapper> {
    @Override
    public boolean canParse(final String text) {
      return true;
    }

    @Override
    public IFilterExpression<CTraceEventWrapper> createExpression(final String text) {
      return new CTraceFilterExpression(text.trim());
    }
  }

  /**
   * Creates predicates for filtering eventy by register values.
   */
  private static class RegisterPredicateGenerator implements
      IPredicateGenerator<CTraceEventWrapper> {
    /**
     * Regular expression that matches valid filter strings.
     */
    private static final String RULE_REGEX =
        "\\s*([a-zA-Z0-9]+)\\s*(==|!=|<|>|<=|>=|<>)\\s*(0x)?([0-9a-fA-F]+)\\s*";

    @Override
    public boolean canParse(final String text) {
      final Pattern pattern = Pattern.compile(RULE_REGEX);
      final Matcher matcher = pattern.matcher(text);

      if (!matcher.matches()) {
        return false;
      }

      final boolean hasHexPrefix = matcher.group(3) != null;

      final String value = matcher.group(4);

      try {
        Long.valueOf(value, hasHexPrefix ? 16 : 10);

        return true;
      } catch (final NumberFormatException exception) {
        return false;
      }
    }

    @Override
    public IFilterExpression<CTraceEventWrapper> createExpression(final String text) {
      final Pattern pattern = Pattern.compile(RULE_REGEX);
      final Matcher matcher = pattern.matcher(text);

      matcher.matches();

      final boolean hasHexPrefix = matcher.group(3) != null;

      final String register = matcher.group(1);
      final FilterRelation predicate = FilterRelation.parse(matcher.group(2));
      final String value = matcher.group(4);

      return new CRegisterFilterExpression(register, predicate, Long.valueOf(value, hasHexPrefix
          ? 16 : 10));
    }
  }
}
