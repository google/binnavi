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
package com.google.security.zynamics.binnavi.models.Bookmarks.code;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Represents a single code bookmark.
 */
public final class CCodeBookmark {
  /**
   * Module the code bookmark belongs to.
   */
  private final INaviModule m_module;

  /**
   * Address of the code bookmark.
   */
  private final IAddress m_address;

  /**
   * Description of the code bookmark.
   */
  private String m_description;

  /**
   * Listeners that are notified about changes in the code bookmark.
   */
  private final ListenerProvider<ICodeBookmarkListener> m_listeners =
      new ListenerProvider<ICodeBookmarkListener>();

  /**
   * Creates a new code bookmark.
   *
   * @param module Module the code bookmark belongs to.
   * @param address Address of the code bookmark.
   * @param description Description of the code bookmark.
   */
  public CCodeBookmark(final INaviModule module, final IAddress address, final String description) {
    m_module = Preconditions.checkNotNull(module, "IE00324: Module argument can not be null");
    m_address = Preconditions.checkNotNull(address, "IE00325: Address argument can not be null");
    m_description =
        Preconditions.checkNotNull(description, "IE00326: Description argument can not be null");
  }

  /**
   * Adds a listener object that is notified about changes in the code bookmark.
   *
   * @param listener The listener object to add.
   */
  public void addListener(final ICodeBookmarkListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Returns the address of the code bookmark.
   *
   * @return The address of the code bookmark.
   */
  public IAddress getAddress() {
    return m_address;
  }

  /**
   * Returns the description of the code bookmark.
   *
   * @return The description of the code bookmark.
   */
  public String getDescription() {
    return m_description;
  }

  /**
   * Returns the module of the code bookmark.
   *
   * @return The module of the code bookmark.
   */
  public INaviModule getModule() {
    return m_module;
  }

  /**
   * Removes a listener object from the code bookmark.
   *
   * @param listener The listener object to remove.
   */
  public void removeListener(final ICodeBookmarkListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Changes the description of the code bookmark.
   *
   * @param description The new description of the code bookmark.
   */
  public void setDescription(final String description) {
    Preconditions.checkNotNull(description, "IE00327: Description argument can not be null");

    if (description.equals(m_description)) {
      return;
    }

    m_description = description;

    for (final ICodeBookmarkListener listener : m_listeners) {
      // ESCA-JAVA0166: Catch Exception because we are calling a listener function.
      try {
        listener.changedDescription(this);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}
