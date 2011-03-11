/*******************************************************************************
 * Copyright (c) 2010 Oracle.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at 
 *     http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *     mkeith - CLient/Server Derby JDBC support 
 ******************************************************************************/

package org.eclipse.gemini.dbaccess.derby;

import java.io.PrintWriter;
import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Driver;

import org.apache.derby.jdbc.ClientDriver;
import org.apache.derby.jdbc.EmbeddedDriver;

import static org.osgi.service.jdbc.DataSourceFactory.*;

/** 
 * An abbreviated/simplified DataSource impl that takes a URL from the client
 * and just returns a thin data source wrapper around the basic JDBC driver.
 */
class UrlBasedDriverDataSource implements javax.sql.DataSource {

    boolean embedded;
    Driver driver;
    Properties properties = null;
    String url = null;

    /**
     * @param properties The properties to use for operations on the driver
     * @param embedded Whether to wrap an embedded or a client driver
     */
    public UrlBasedDriverDataSource(Properties properties, boolean embedded) {
        this.embedded = embedded;
        this.driver = embedded ? new EmbeddedDriver() : new ClientDriver();
        this.properties = (Properties) properties.clone();
        this.url = properties.getProperty(JDBC_URL);
    }

    public UrlBasedDriverDataSource(Properties properties) {
        this(properties, true);
    }

    public Connection getConnection() throws java.sql.SQLException {
        return driver.connect(url, properties);
    }

    public Connection getConnection(String user, String password) throws java.sql.SQLException {
        Properties localProps = (Properties) properties.clone();
        localProps.put(JDBC_USER, user);
        localProps.put(JDBC_PASSWORD, password);
        return driver.connect(url, localProps);
    }
 
    public boolean isWrapperFor(Class<?> cls) { 
        return embedded 
            ? (cls == EmbeddedDriver.class)
            : (cls == ClientDriver.class);
    }
    
    public <T> T unwrap(Class<T> cls) { 
        return (isWrapperFor(cls)) 
            ? (T) driver 
            : null;
    }

    public PrintWriter getLogWriter() throws SQLException { 
        return DriverManager.getLogWriter(); 
    }

    public int getLoginTimeout() throws SQLException { 
        return DriverManager.getLoginTimeout(); 
    }

    // Don't support setting log writer or timeout 

    public void setLogWriter(PrintWriter writer) throws SQLException {
        throw new SQLException("Can't set Log Writer on URL data source");
    }
    
    public void setLoginTimeout(int timeout) throws SQLException {
        throw new SQLException("Can't set Login Timeout on URL data source");
    }
}