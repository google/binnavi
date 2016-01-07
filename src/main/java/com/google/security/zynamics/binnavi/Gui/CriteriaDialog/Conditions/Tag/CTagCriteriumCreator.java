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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Tag;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICriteriumCreator;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;

/**
 * Factory class for Tag state criteria objects.
 */
public final class CTagCriteriumCreator implements ICriteriumCreator {
  /**
   * Tag manager that provides tagging information.
   */
  private final ITagManager m_tagManager;

  /**
   * Creates a new creator object.
   *
   * @param tagManager Tag manager that provides tagging information.
   */
  public CTagCriteriumCreator(final ITagManager tagManager) {
    m_tagManager = tagManager;
  }

  @Override
  public ICriterium createCriterium() {
    return new CTagCriterium(m_tagManager);
  }

  @Override
  public String getCriteriumDescription() {
    return "Select Nodes by Tag";
  }
}
