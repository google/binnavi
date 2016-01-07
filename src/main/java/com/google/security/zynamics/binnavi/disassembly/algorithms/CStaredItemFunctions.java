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
package com.google.security.zynamics.binnavi.disassembly.algorithms;

import java.util.List;

import com.google.security.zynamics.binnavi.disassembly.IStaredItem;


/**
 * Contains helper functions for working with stared items.
 */
public final class CStaredItemFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CStaredItemFunctions() {
  }

  /**
   * Determines whether all elements of an array are unstared.
   * 
   * @param <T> Type of the array elements.
   * 
   * @param items The items to check.
   * 
   * @return True, if all items of the array are unstared. False, if at least one item is stared.
   */
  public static <T extends IStaredItem> boolean allNotStared(final T[] items) {
    for (final IStaredItem item : items) {
      if (item.isStared()) {
        return false;
      }
    }

    return true;
  }

  /**
   * Determines whether all elements of an array are stared.
   * 
   * @param <T> Type of the array elements.
   * 
   * @param items The items to check.
   * 
   * @return True, if all items of the array are stared. False, if at least one item is unstared.
   */
  public static <T extends IStaredItem> boolean allStared(final T[] items) {
    for (final IStaredItem item : items) {
      if (!item.isStared()) {
        return false;
      }
    }

    return true;
  }


  /**
   * Takes a list of starable items and sorts the stared items to the top.
   * 
   * @param <T> Type of the elements in the list.
   * @param items The list to sort.
   */
  public static <T extends IStaredItem> void sort(final List<T> items) {
    int insertCounter = 0;

    for (int i = 0; i < items.size(); i++) {
      final T item = items.get(i);

      if (item.isStared()) {
        items.remove(i);
        items.add(insertCounter++, item);
      }
    }
  }
}
