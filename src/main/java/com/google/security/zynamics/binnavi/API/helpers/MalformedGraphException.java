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
package com.google.security.zynamics.binnavi.API.helpers;

/**
 * Exception that is thrown if there is any kind of problem with the structure of a graph.
 */
public final class MalformedGraphException extends Exception {

  /**
   * Creates a new exception object.
   *
   * @param exception Cause of the exception.
   */
  public MalformedGraphException(final Exception exception) {
    super(exception);
  }
}
