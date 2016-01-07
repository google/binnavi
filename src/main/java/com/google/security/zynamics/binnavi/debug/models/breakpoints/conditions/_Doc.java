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
package com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions;

/**
 * Contains code for dealing with breakpoint conditions. Breakpoint conditions are used to allow the
 * user to set conditional breakpoints.
 *
 *  Breakpoint conditions are implemented as expression trees generated from text strings with the
 * help of the {@link CConditionParser} class.
 *
 *  The generated expression trees can then be evaluated once concrete values for possible condition
 * expressions like register or memory values are known.
 *
 *  Please note that conditional breakpoints are evaluated in the debug client, not in com.google.security.zynamics.binnavi. This
 * is done for speed reasons. On the downside this means that the breakpoint condition tree has to
 * be transmitted to the debug client. This is done with the code in the
 * {@link ConditionTreeFlattener} class.
 */
