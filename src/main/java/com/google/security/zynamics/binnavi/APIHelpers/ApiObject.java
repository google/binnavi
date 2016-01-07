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
package com.google.security.zynamics.binnavi.APIHelpers;

/**
 * This interface must be implemented by all API objects that encapsulate an internal object.
 *
 *  The general idea of this interface is to have a generic way to access the wrapped internal
 * object represented by a plugin API object. This interface is then used directly or by helper
 * classes like ObjectFinders.
 *
 * @param <T> Native type of the object.
 */
public interface ApiObject<T> {
  // ESCA-JAVA0021:
  /**
   * For each API object that implements this interface, this method returns the encapsulated native
   * object.
   *
   * @return The encapsulated native object.
   */
  T getNative();
}
