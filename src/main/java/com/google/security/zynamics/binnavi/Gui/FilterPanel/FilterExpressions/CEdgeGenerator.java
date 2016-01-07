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
package com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.ConcreteTree.IFilterExpression;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.Wrappers.CViewWrapper;


/**
 * Takes a filter string and converts it into a filter expression for filtering views according to
 * the number of their edges.
 */
public final class CEdgeGenerator implements IPredicateGenerator<CViewWrapper> {
  /**
   * Regular expression that matches valid filter strings.
   */
  private static final String RULE_REGEX = "\\s*edges\\s*(==|!=|<|>|<=|>=|<>)\\s*(\\d+)\\s*";

  @Override
  public boolean canParse(final String text) {
    return text.matches(RULE_REGEX);
  }

  @Override
  public IFilterExpression<CViewWrapper> createExpression(final String text) {
    final Pattern pattern = Pattern.compile(RULE_REGEX);
    final Matcher matcher = pattern.matcher(text);

    matcher.matches();

    final FilterRelation predicate = FilterRelation.parse(matcher.group(1));
    final String value = matcher.group(2);

    return new CEdgeFilterExpression(predicate, Long.valueOf(value));
  }

}
