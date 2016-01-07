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
package com.google.security.zynamics.binnavi.debug.models.memoryexpressions;

/**
 * In the BinNavi GUI it is possible to jump to addresses in the memory window. However, simple
 * numerical addresses are not convenient enough. The users also want to follow register values
 * (like eax) or even complex expressions like [4 * eax + ecx].
 *
 *  This package contains code that parses string expressions to turn them into expression trees.
 * These expression trees can then be evaluated once the concrete values for registers or memory
 * locations are known.
 */
