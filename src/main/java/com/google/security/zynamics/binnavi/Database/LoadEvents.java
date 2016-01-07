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
 * Contains all possible database load events.
 */
public enum LoadEvents {
  /**
   * Signals that BinNavi checking the validity of the exporter tables.
   */
  CHECKING_EXPORTER_TABLE_FORMAT,

  /**
   * Signals that BinNavi is connecting to the database.
   */
  CONNECTING_TO_DATABASE,

  /**
   * Signals that BinNavi is checking the tables initialization status.
   */
  CHECKING_INITIALIZATION_STATUS,

  /**
   * Signals that BinNavi is initializing tables.
   */
  INITIALIZING_DATABASE_TABLES,

  /**
   * Signals that BinNavi is trying to determine the database version.
   */
  DETERMINING_DATABASE_VERSION,

  /**
   * Signals that BinNavi is loading view tags.
   */
  LOADING_VIEW_TAGS,

  /**
   * Signals that BinNavi is loading node tags.
   */
  LOADING_NODE_TAGS,

  /**
   * Signals that BinNavi is loading debuggers.
   */
  LOADING_DEBUGGERS,

  /**
   * Signals that BinNavi is loading projects.
   */
  LOADING_PROJECTS,

  /**
   * Signals that BinNavi is loading modules.
   */
  LOADING_MODULES,

  /**
   * Signals that BinNavi is loading raw modules.
   */
  LOADING_RAW_MODULES,

  /**
   * Signals that BinNavi starts loading a database.
   */
  LOADING_DATABASE,

  /**
   * Signals that BinNavi is loading users.
   */
  LOADING_USERS,

  /**
   * Signals that BinNavi finished loading a database.
   */
  LOADING_FINISHED;
}
