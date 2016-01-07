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
package com.google.security.zynamics.binnavi.Gui.Debug.MemorySectionPanel;

import com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection;
import com.google.security.zynamics.zylib.gui.DefaultWrapper;

import java.util.Locale;



/**
 * Wraps memory section objects to put them into list boxes.
 */
public final class CMemorySectionWrapper extends DefaultWrapper<MemorySection> {
  /**
   * Creates a new wrapper object.
   *
   * @param object The memory section to wrap.
   */
  protected CMemorySectionWrapper(final MemorySection object) {
    super(object);
  }

  /**
   * Creates the section size formatter string depending on the size of the section.
   *
   * @param size The size of the section.
   *
   * @return The formatter string.
   */
  private String toString(final int size) {
    if (size < 1024) {
      return String.format("%d bytes", size);
    } else if (size < 1024 * 1024) {
      return String.format(Locale.ENGLISH, "%.02f KB", 1.0 * size / 1024);
    } else {
      return String.format(Locale.ENGLISH, "%.02f MB", 1.0 * size / 1024 / 1024);
    }
  }

  @Override
  public String toString() {
    final MemorySection section = getObject();

    return String.format("%s - %s (%s)", section.getStart().toHexString(),
        section.getEnd().toHexString(), toString(section.getSize()));
  }
}
