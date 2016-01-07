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

import com.google.common.base.Preconditions;

/**
 * Provides a small helper method that can be used to search for a BinNavi object in a list of
 * plugin API objects.
 */
public final class ObjectFinders {
  /**
   * Private constructor because you are not supposed to instantiate this class.
   */
  private ObjectFinders() {
    // You are not supposed to instantiate this class
  }

  /**
   * Searches for an API object in a list of API objects.
   *
   * @param <InternalType> BinNavi object type.
   * @param <ApiType> API object type.
   *
   * @param internalObject BinNavi object that corresponds to the API object to search for.
   * @param apiObjects List of API objects to search through.
   *
   * @return The API object that corresponds to the given BinNavi object. Null, if there is no such
   *         object.
   */
  public static <InternalType, ApiType extends ApiObject<?>> ApiType getObject(
      final InternalType internalObject, final Iterable<ApiType> apiObjects) {
    Preconditions.checkNotNull(internalObject, "IE00003: Internal object argument can not be null");
    Preconditions.checkNotNull(apiObjects, "IE00004: API objects argument can not be null");

    for (final ApiType apiObject : apiObjects) {
      if (apiObject.getNative() == internalObject) {
        return apiObject;
      }
    }

    return null;
  }
}
