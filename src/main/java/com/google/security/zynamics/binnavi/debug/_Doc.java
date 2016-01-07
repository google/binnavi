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
package com.google.security.zynamics.binnavi.debug;

/**
 * The Debug package is responsible for communicating with the debug clients. The classes in this
 * package handle the low-level communication aspect of the interaction with the debug client and
 * convert events into their corresponding high-level events. An example for such a conversion is
 * changing the EIP of a program when a new batch of register values arrives.
 */
