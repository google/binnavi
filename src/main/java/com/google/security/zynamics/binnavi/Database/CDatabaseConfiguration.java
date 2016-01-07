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


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabaseListener;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Contains configuration data of a database object.
 */
public final class CDatabaseConfiguration {
  private String m_identity;

  /**
   * Database which is configured.
   */
  private final IDatabase m_database;

  /**
   * Listeners to be notified about changes in the configuration.
   */
  private final ListenerProvider<IDatabaseListener> m_listeners;

  /**
   * Description of the database.
   */
  private String m_description;

  /**
   * The driver that is used to connect to the database.
   */
  private String m_driver;

  /**
   * Host location of the database.
   */
  private String m_host;

  /**
   * Name of the database.
   */
  private String m_name;

  /**
   * The user that is used to connect to the database.
   */
  private String m_user;

  /**
   * The password that is used to log in to the database.
   */
  private String m_password;

  /**
   * A flag that says whether this database should be automatically connected when BinNavi starts.
   */
  private boolean m_autoConnect;

  /**
   * A flag that determines whether the password used to connect to this database should be saved in
   * the configuration file.
   */
  private boolean m_savePassword;

  /**
   * Creates a new configuration object.
   * 
   * @param database Database which is configured.
   * @param listeners Listeners to be notified about changes in the configuration.
   * @param description Description of the database.
   * @param driver The driver that is used to connect to the database.
   * @param host Host location of the database.
   * @param name Name of the database.
   * @param user The user that is used to connect to the database.
   * @param password The password that is used to log in to the database.
   * @param identity The identity under which the current user interacts with the database.
   * @param autoConnect A flag that says whether this database should be automatically connected
   *        when BinNavi starts.
   * @param savePassword A flag that determines whether the password used to connect to this
   *        database should be saved in the config file.
   */
  public CDatabaseConfiguration(final IDatabase database,
      final ListenerProvider<IDatabaseListener> listeners, final String description,
      final String driver, final String host, final String name, final String user,
      final String password, final String identity, final boolean autoConnect,
      final boolean savePassword) {
    m_database = Preconditions.checkNotNull(database, "IE02401: database argument can not be null");
    m_listeners =
        Preconditions.checkNotNull(listeners, "IE02402: listeners argument can not be null");
    m_description =
        Preconditions.checkNotNull(description, "IE02403: description argument can not be null");
    m_driver = Preconditions.checkNotNull(driver, "IE02404: driver argument can not be null");
    m_host = Preconditions.checkNotNull(host, "IE02405: host argument can not be null");
    m_name = Preconditions.checkNotNull(name, "IE02406: name argument can not be null");
    m_user = Preconditions.checkNotNull(user, "IE02407: user argument can not be null");
    m_password = Preconditions.checkNotNull(password, "IE02408: password argument can not be null");
    m_identity = Preconditions.checkNotNull(identity, "IE00065: identity argument can not be null");
    if (m_identity.isEmpty()) {
      m_identity = "identity";
    }
    m_autoConnect = autoConnect;
    m_savePassword = savePassword;
  }

  /**
   * Returns the description string of the database.
   * 
   * @return The description string.
   */
  public String getDescription() {
    return m_description;
  }

  /**
   * Returns the driver string that is used to connect to the database.
   * 
   * @return The driver string.
   */
  public String getDriver() {
    return m_driver;
  }

  /**
   * Returns the host of the database.
   * 
   * @return The host of the database.
   */
  public String getHost() {
    return m_host;
  }

  public String getIdentity() {
    return m_identity;
  }

  /**
   * Returns the name of the database.
   * 
   * @return The name of the database.
   */
  public String getName() {
    return m_name;
  }

  /**
   * Returns the password string that is used to connect to the database.
   * 
   * @return The password string.
   */
  public String getPassword() {
    return m_password;
  }

  public String getUrl() {
    return String.format("jdbc:postgresql://%s/%s", m_host, m_name);
  }

  /**
   * Returns the user string that is used to connect to the database.
   * 
   * @return The user string.
   */
  public String getUser() {
    return m_user;
  }

  /**
   * Returns the flag that says whether a connection to this database is established automatically
   * when BinNavi starts.
   * 
   * @return True, if the database is connected automatically. False, otherwise.
   */
  public boolean isAutoConnect() {
    return m_autoConnect;
  }

  /**
   * Returns the flag that says whether the password of this database should be stored in the
   * configuration file.
   * 
   * @return True, if the password should be saved. False, otherwise.
   */
  public boolean isSavePassword() {
    return m_savePassword;
  }

  /**
   * Changes the flag that says whether to automatically connect to this database or not.
   * 
   * @param autoConnect True to automatically connect. False, otherwise.
   */
  public void setAutoConnect(final boolean autoConnect) {
    if (autoConnect == isAutoConnect()) {
      return;
    }

    m_autoConnect = autoConnect;

    for (final IDatabaseListener listener : m_listeners) {
      try {
        listener.changedAutoConnect(m_database, autoConnect);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the description of the database.
   * 
   * @param description The new description of the database.
   * 
   * @throws IllegalArgumentException Thrown if the given description is invalid.
   */
  public void setDescription(final String description) {
    Preconditions.checkNotNull(description, "IE00689: Database description can not be null");

    if (description.equals(m_description)) {
      return;
    }

    m_description = description;

    for (final IDatabaseListener listener : m_listeners) {
      try {
        listener.changedDescription(m_database, description);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the driver that is used to connect to the database.
   * 
   * @param driver The new driver string.
   * 
   * @throws IllegalArgumentException Thrown if the given driver is invalid.
   */
  public void setDriver(final String driver) {
    Preconditions.checkNotNull(driver, "IE00690: Database driver can not be null");

    if (driver.equals(getDriver())) {
      return;
    }

    m_driver = driver;

    for (final IDatabaseListener listener : m_listeners) {
      try {
        listener.changedDriver(m_database, driver);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the database host.
   * 
   * @param host The new host value.
   */
  public void setHost(final String host) {
    Preconditions.checkNotNull(host, "IE00040: Database host can not be null");

    if (host.equals(m_host)) {
      return;
    }

    m_host = host;

    for (final IDatabaseListener listener : m_listeners) {
      try {
        listener.changedHost(m_database, host);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the identity of the current user in this database.
   * 
   * @param identity The new identity of the user.
   */
  public void setIdentity(final String identity) {
    Preconditions.checkNotNull(identity, "IE00067: identity argument can not be null");

    if (identity.equals(m_identity)) {
      return;
    }

    m_identity = identity;

    for (final IDatabaseListener listener : m_listeners) {
      try {
        listener.changedIdentity(m_database, identity);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the database name.
   * 
   * @param name The new database name.
   */
  public void setName(final String name) {
    Preconditions.checkNotNull(name, "IE00159: Database name can not be null");

    if (name.equals(m_name)) {
      return;
    }

    m_name = name;

    for (final IDatabaseListener listener : m_listeners) {
      try {
        listener.changedName(m_database, name);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the password that is used to connect to the database.
   * 
   * @param password The new password.
   * 
   * @throws IllegalArgumentException Thrown if the given password is invalid.
   */
  public void setPassword(final String password) {
    Preconditions.checkNotNull(password, "IE00691: Database password can not be null");

    if (password.equals(getPassword())) {
      return;
    }

    m_password = password;

    for (final IDatabaseListener listener : m_listeners) {
      try {
        listener.changedPassword(m_database, password);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the flag that says whether the password of this database is stored in the configuration
   * file.
   * 
   * @param savePassword True, to save the password. False, otherwise.
   */
  public void setSavePassword(final boolean savePassword) {
    if (savePassword == isSavePassword()) {
      return;
    }

    m_savePassword = savePassword;

    for (final IDatabaseListener listener : m_listeners) {
      try {
        listener.changedSavePassword(m_database, savePassword);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the user name that is used to connect to the database.
   * 
   * @param user The new user name.
   * 
   * @throws IllegalArgumentException Thrown if the given user name is invalid.
   */
  public void setUser(final String user) {
    Preconditions.checkNotNull(user, "IE00692: Database user can not be null");

    if (user.equals(getUser())) {
      return;
    }

    m_user = user;

    for (final IDatabaseListener listener : m_listeners) {
      try {
        listener.changedUser(m_database, user);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}
