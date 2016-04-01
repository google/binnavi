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
package com.google.security.zynamics.zylib.types.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.general.Pair;

public class CollectionHelpers {
  public static <ItemType> boolean all(final Collection<ItemType> collection,
      final ICollectionFilter<ItemType> callback) {
    for (final ItemType item : collection) {
      if (!callback.qualifies(item)) {
        return false;
      }
    }

    return true;
  }

  public static <ItemType> boolean any(final Collection<ItemType> collection,
      final ICollectionFilter<ItemType> callback) {
    for (final ItemType item : collection) {
      if (callback.qualifies(item)) {
        return true;
      }
    }

    return false;
  }

  public static <ItemType> int count(final Collection<? extends ItemType> collection,
      final ItemType item) {
    
    return (int) collection.stream()
                   .filter(itemType -> itemType == item)
                   .count();
  }

  public static <ItemType> int countIf(final Collection<? extends ItemType> collection,
      final ICollectionFilter<ItemType> item) {
    
    return (int) collection.stream()
                   .filter(itemType -> item.qualifies(itemType))
                   .count();
  }

  public static <ItemType> List<ItemType> filter(final Collection<? extends ItemType> collection,
      final ICollectionFilter<ItemType> callback) {
    final List<ItemType> filteredItems = new ArrayList<>();		
 		 
    for (final ItemType item : collection) {		
      if (callback.qualifies(item)) {	
        filteredItems.add(item);	
      }		
    }		
		
    return filteredItems;
  }

  public static <T> Collection<T> flatten(final Collection<? extends Collection<T>> second) {
    final Collection<T> returnList = new ArrayList<>();

    for (final Collection<T> collection : second) {
      returnList.addAll(collection);
    }

    return returnList;
  }

  /**
   * Performs an operation on all items that pass a filter check.
   * 
   * @param <ItemType> The type of the items in the collection.
   * @param <CollectionType> The type of the collection.
   * 
   * @param collection The collection that provides the items.
   * @param filter The filter that selects the items that qualify.
   * @param callback The callback that is invoked once for each item that passes the filter check.
   */
  public static <ItemType, CollectionType extends IIterableCollection<IItemCallback<ItemType>>> void iterate(
      final CollectionType collection, final ICollectionFilter<ItemType> filter,
      final IItemCallback<ItemType> callback) {
    Preconditions.checkNotNull(collection, "Error: Graph argument can't be null");

    Preconditions.checkNotNull(callback, "Error: Callback argument can't be null");

    Preconditions.checkNotNull(filter, "Error: Filter argument can't be null");

    collection.iterate(new IItemCallback<ItemType>() {
      @Override
      public IterationMode next(final ItemType node) {
        if (filter.qualifies(node)) {
          return callback.next(node);
        }

        return IterationMode.CONTINUE;
      }
    });
  }

  /**
   * Performs an operation on all items that pass a filter check.
   * 
   * @param <ItemType> The type of the items in the collection.
   * @param <CollectionType> The type of the collection.
   * 
   * @param collection The collection that provides the items.
   * @param callback The callback that filters the collection and performs an operation on each item
   *        that passes the filter check.
   */
  public static <ItemType, CollectionType extends IIterableCollection<IItemCallback<ItemType>>> void iterate(
      final CollectionType collection, final IFilteredItemCallback<ItemType> callback) {
    collection.iterate(new IItemCallback<ItemType>() {
      @Override
      public IterationMode next(final ItemType node) {
        if (callback.qualifies(node)) {
          return callback.next(node);
        }

        return IterationMode.CONTINUE;
      }
    });
  }

  public static <InputType, OutputType> List<OutputType> map(
      final Collection<? extends InputType> elements,
      final ICollectionMapper<InputType, OutputType> mapper) {
    final List<OutputType> list = new ArrayList<>();		
		
    for (final InputType element : elements) {		
      list.add(mapper.map(element));		
    }		
 		 
    return list;
  }

  public static <ItemType> ItemType nth(final Collection<? extends ItemType> collection,
      final ICollectionFilter<ItemType> callback, final int index) {
    Preconditions.checkArgument(index >= 0 && index < collection.size(), 
      "Error: Index argument should be between 0 and collection size");
    Object[] array = collection.toArray();
    ItemType itemType = (ItemType) array[index];
    if (callback.qualifies(itemType)) {
      return itemType;
    } else {
      throw new IllegalStateException("Error: nth element does not exist");
    }
  }

  public static <S, T> Pair<Collection<S>, Collection<T>> unzip(
      final Collection<Pair<S, T>> elements) {
    final Collection<S> firstList = new ArrayList<>(elements.size());
    final Collection<T> secondList = new ArrayList<>(elements.size());

    for (final Pair<S, T> pair : elements) {
      firstList.add(pair.first());
      secondList.add(pair.second());
    }

    return new Pair<>(firstList, secondList);
  }
}
