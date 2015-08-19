/*
Copyright 2014 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Common;

/**
 * Factory interface used by the GenericEquivalenceRelationTest class to test arbitrary types. The
 * usefulness of the tests executed by the GenericEquivalenceRelationTest class is based on the fact
 * that all three instances are different in terms of equals().
 *
 * @author jannewger@google.com (Jan Newger)
 *
 */
public interface GenericEquivalenceRelationFactory<T> {

  public T createFirstInstance();

  public T createSecondInstance();

  public T createThirdInstance();
}
