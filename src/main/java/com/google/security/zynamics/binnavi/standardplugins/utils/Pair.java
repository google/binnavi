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
package com.google.security.zynamics.binnavi.standardplugins.utils;

/**
 * Simple pair class.
 * 
 * @param <S> The type of the first element.
 * @param <T> The type of the second element.
 */
public final class Pair<S, T> {
  /**
   * The first element of the pair.
   */
  private final S first;

  /**
   * The second element of the pair.
   */
  private final T second;

  /**
   * Creates a new pair.
   * 
   * @param first The first element of the pair.
   * @param second The second element of the pair.
   */
  public Pair(final S first, final T second) {
    this.first = first;
    this.second = second;
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof Pair)) {
      return false;
    }

    final Pair<?, ?> p = (Pair<?, ?>) obj;

    return p.first.equals(first) && p.second.equals(second);
  }

  /**
   * Returns the first element of the pair.
   * 
   * @return The first element of the pair.
   */
  public S first() {
    return first;
  }

  @Override
  public int hashCode() {
    return first.hashCode() * second.hashCode();
  }

  /**
   * The second element of the pair.
   * 
   * @return The second element of the pair.
   */
  public T second() {
    return second;
  }

  @Override
  public String toString() {
    return "< " + first + ", " + second + ">";
  }
}
