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
package com.google.security.zynamics.binnavi.Database;

/**
 * The database package provides all classes and methods that are necessary to interact with the
 * BinNavi databases. The general idea is that we have one provider class for each supported
 * database type (like {@link PostgreSQLProvider}) which implements all functionality that is
 * necessary to get BinNavi to work with this database type.
 * 
 * All provider classes should inherit from {@link CAbstractSqlProvider} because this class already
 * encapsulates the general strategy for interacting with the database for non-trivial composite
 * statements.
 */
