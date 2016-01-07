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

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.ConcreteTree.IFilterExpression;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.Wrappers.CViewWrapper;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import java.math.BigInteger;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Takes a filter string and converts it into a filter expression for filtering views according to
 * the instructions they contain.
 */
public final class CInstructionGenerator implements IPredicateGenerator<CViewWrapper> {
  /**
   * Regular expression that matches valid filter strings.
   */
  private static final String RULE_REGEX = "\\s*contains\\s*(==)\\s*([0-9A-Fa-f]+)\\s*";

  /**
   * View container to search through.
   */
  private final IViewContainer m_container;

  /**
   * Creates a new generator object.
   *
   * @param container View container to search through.
   */
  public CInstructionGenerator(final IViewContainer container) {
    m_container = container;
  }

  @Override
  public boolean canParse(final String text) {
    return text.matches(RULE_REGEX);
  }

  @Override
  public IFilterExpression<CViewWrapper> createExpression(final String text) {
    final Pattern pattern = Pattern.compile(RULE_REGEX);
    final Matcher matcher = pattern.matcher(text);

    matcher.matches();

    final String value = matcher.group(2);

    try {
      final List<INaviView> views =
          m_container.getViewsWithAddresses(
              Lists.newArrayList(new UnrelocatedAddress(new CAddress(new BigInteger(value, 16)))),
              true);

      return new CInstructionFilterExpression(views);
    } catch (final CouldntLoadDataException e) {
      return null;
    }
  }
}
