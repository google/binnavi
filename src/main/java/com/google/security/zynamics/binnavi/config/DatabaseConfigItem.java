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
package com.google.security.zynamics.binnavi.config;

import com.google.security.zynamics.common.config.AbstractConfigItem;
import com.google.security.zynamics.common.config.TypedPropertiesWrapper;

public class DatabaseConfigItem extends AbstractConfigItem {
  private static final String DESCRIPTION = "Description";
  private static final String DESCRIPTION_DEFAULT = "";
  private String description = DESCRIPTION_DEFAULT;

  private static final String DRIVER = "Driver";
  private static final String DRIVER_DEFAULT = "";
  private String driver = DRIVER_DEFAULT;

  private static final String HOST = "Host";
  private static final String HOST_DEFAULT = "";
  private String host = HOST_DEFAULT;

  private static final String NAME = "Name";
  private static final String NAME_DEFAULT = "";
  private String name = NAME_DEFAULT;

  private static final String USER = "User";
  private static final String USER_DEFAULT = "";
  private String user = USER_DEFAULT;

  private static final String PASSWORD = "Password";
  private static final String PASSWORD_DEFAULT = "";
  private String password = PASSWORD_DEFAULT;

  private static final String IDENTITY = "Identity";
  private static final String IDENTITY_DEFAULT = "";
  private String identity = IDENTITY_DEFAULT;

  private static final String AUTO_CONNECT = "AutoConnect";
  private static final boolean AUTO_CONNECT_DEFAULT = false;
  private boolean autoConnect = AUTO_CONNECT_DEFAULT;

  private static final String SAVE_PASSWORD = "SavePassword";
  private static final boolean SAVE_PASSWORD_DEFAULT = false;
  private boolean savePassword = SAVE_PASSWORD_DEFAULT;

  @Override
  public void load(final TypedPropertiesWrapper properties) {
    description = properties.getString(DESCRIPTION, DESCRIPTION_DEFAULT);
    driver = properties.getString(DRIVER, DRIVER_DEFAULT);
    host = properties.getString(HOST, HOST_DEFAULT);
    name = properties.getString(NAME, NAME_DEFAULT);
    user = properties.getString(USER, USER_DEFAULT);
    password = properties.getString(PASSWORD, PASSWORD_DEFAULT);
    identity = properties.getString(IDENTITY, IDENTITY_DEFAULT);
    autoConnect = properties.getBoolean(AUTO_CONNECT, AUTO_CONNECT_DEFAULT);
    savePassword = properties.getBoolean(SAVE_PASSWORD, SAVE_PASSWORD_DEFAULT);
  }

  @Override
  public void store(final TypedPropertiesWrapper properties) {
    properties.setString(DESCRIPTION, description);
    properties.setString(DRIVER, driver);
    properties.setString(HOST, host);
    properties.setString(NAME, name);
    properties.setString(USER, user);
    properties.setString(PASSWORD, password);
    properties.setString(IDENTITY, identity);
    properties.setBoolean(AUTO_CONNECT, autoConnect);
    properties.setBoolean(SAVE_PASSWORD, savePassword);
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(final String value) {
    this.description = value;
  }

  public String getDriver() {
    return driver;
  }

  public void setDriver(final String value) {
    this.driver = value;
  }

  public String getHost() {
    return host;
  }

  public void setHost(final String value) {
    this.host = value;
  }

  public String getName() {
    return name;
  }

  public void setName(final String value) {
    this.name = value;
  }

  public String getUser() {
    return user;
  }

  public void setUser(final String value) {
    this.user = value;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(final String value) {
    this.password = value;
  }

  public String getIdentity() {
    return identity;
  }

  public void setIdentity(final String value) {
    this.identity = value;
  }

  public boolean isAutoConnect() {
    return autoConnect;
  }

  public void setAutoConnect(final boolean value) {
    this.autoConnect = value;
  }

  public boolean isSavePassword() {
    return savePassword;
  }

  public void setSavePassword(final boolean value) {
    this.savePassword = value;
  }
}
