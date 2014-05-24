/**
 * Copyright 2013-2014 Guoqiang Chen, Shanghai, China. All rights reserved.
 *
 * Email: subchen@gmail.com
 * URL: http://subchen.github.io/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrick.dao.jdbclog;

import java.sql.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: Wrapper class for Jdbc Driver.
 * <p>
 * <code><pre>
 * DriverManagerDataSource ds = new DriverManagerDataSource();
 * //ds.setDriverClassName("oracle.jdbc.driver.OracleDriver");
 * ds.setDriverClassName("jetbrick.commons.jdbc.log.JdbcLogDriver");
 * ds.setUrl("jdbc:oracle:thin:@localhost:1521:orcl");
 * ds.setUsername("sa");
 * ds.setPassword("");
 * </pre></code>
 * <p>
 * This JdbcLogDriver can auto identify following drivers.
 * <ul>
 * <li>MySQL</li>
 * <li>Oracle</li>
 * <li>JTDS</li>
 * <li>SQL Server 97/2000/2005</li>
 * <li>DB2</li>
 * <li>SyBase</li>
 * <li>PostgreSQL</li>
 * <li>HSqlDB</li>
 * <li>Derby</li>
 * <li>Informix</li>
 * <li>TimesTen</li>
 * <li>IBM-AS400</li>
 * <li>SAP DB</li>
 * <li>InterBase</li>
 * <li>JDBC-ODBC</li>
 * </ul>
 *
 * <p>
 * If you use other driver, you can add real driver class name into connection
 * url string. Pattern: CustomizeConnectionUrl =
 * <code>"jdbclog" ":" [DriverClassName] ":" ConnectionUrl</code>. In customize
 * connection url, the DriverClassName is optional.
 * <p>
 * For Oracle:
 * <code>jdbclog:oracle.jdbc.driver.OracleDriver:jdbc:oracle:thin:@localhost:1521:orcl</code>
 * <p>
 * If you use <code>Jdbc Odbc Bridge</code> or <code>Apache Derby</code>, you must use
 * customize connection url.
 * <p>
 * For Derby:
 * <code>jdbclog::jdbc:derby:MyDB;user=test;password=test</code>
 */
public class JdbcLogDriver implements Driver {
    private static final Logger log = LoggerFactory.getLogger(JdbcLogDriver.class);
    private static final String CONNECTION_URL_SUFFIX = "jdbclog:";
    private Map<String, Driver> drivers = new HashMap<String, Driver>();

    static {
        try {
            DriverManager.registerDriver(new JdbcLogDriver());
        } catch (SQLException e) {
            log.error("DriverManager.registerDriver Exception.", e);
        }
    }

    /**
     * url pattern = "JdbcLog" ":" [DriverClassName] ":" ConnectionUrl
     */
    private String getDriverClassName(String url) {
        String driverClassName = null;
        if (url.startsWith(CONNECTION_URL_SUFFIX)) {
            url = url.substring(CONNECTION_URL_SUFFIX.length());
            driverClassName = url.substring(0, url.indexOf(":"));
            if (driverClassName.length() > 0) {
                return driverClassName;
            }
            url = url.substring(url.indexOf(":") + 1);
        }

        if (url.startsWith("jdbc:oracle:thin:")) {
            driverClassName = "oracle.jdbc.driver.OracleDriver";
        } else if (url.startsWith("jdbc:mysql:")) {
            driverClassName = "com.mysql.jdbc.Driver";
        } else if (url.startsWith("jdbc:jtds:")) {
            // SQL Server or SyBase
            driverClassName = "net.sourceforge.jtds.jdbc.Driver";
        } else if (url.startsWith("jdbc:db2:")) {
            driverClassName = "com.ibm.db2.jdbc.net.DB2Driver";
        } else if (url.startsWith("jdbc:microsoft:sqlserver:")) {
            // SQL Server 7.0/2000
            driverClassName = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
        } else if (url.startsWith("jdbc:sqlserver:")) {
            // SQL Server 2005
            driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        } else if (url.startsWith("jdbc:postgresql:")) {
            driverClassName = "org.postgresql.Driver";
        } else if (url.startsWith("jdbc:hsqldb:")) {
            driverClassName = "org.hsqldb.jdbcDriver";
        } else if (url.startsWith("jdbc:derby://")) {
            driverClassName = "org.apache.derby.jdbc.ClientDriver";
        } else if (url.startsWith("jdbc:derby:")) {
            driverClassName = "org.apache.derby.jdbc.EmbeddedDriver";
        } else if (url.startsWith("jdbc:sybase:Tds:")) {
            driverClassName = "com.sybase.jdbc.SybDriver";
        } else if (url.startsWith("jdbc:informix-sqli:")) {
            driverClassName = "com.informix.jdbc.IfxDriver";
        } else if (url.startsWith("jdbc:odbc:")) {
            driverClassName = "sun.jdbc.odbc.JdbcOdbcDriver";
        } else if (url.startsWith("jdbc:timesten:client:")) {
            driverClassName = "com.timesten.jdbc.TimesTenDriver";
        } else if (url.startsWith("jdbc:as400:")) {
            driverClassName = "com.ibm.as400.access.AS400JDBCDriver";
        } else if (url.startsWith("jdbc:sapdb:")) {
            driverClassName = "com.sap.dbtech.jdbc.DriverSapDB";
        } else if (url.startsWith("jdbc:interbase:")) {
            driverClassName = "interbase.interclient.Driver";
        }
        return driverClassName;
    }

    private String getConnectionUrl(String url) {
        if (url.startsWith(CONNECTION_URL_SUFFIX)) {
            url = url.substring(CONNECTION_URL_SUFFIX.length());
            url = url.substring(url.indexOf(":") + 1);
        }
        return url;
    }

    private Driver getJdbcDriver(String url) {
        Driver driver = drivers.get(url);
        if (driver == null) {
            String driverClassName = getDriverClassName(url);
            String connectionUrl = getConnectionUrl(url);
            log.info("driverClassName = {}", driverClassName);
            log.info("connectionUrl = {}", connectionUrl);
            try {
                driver = (Driver) Class.forName(driverClassName).newInstance();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            drivers.put(url, driver);
        }
        return driver;
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        Driver driver = getJdbcDriver(url);
        String connectionUrl = getConnectionUrl(url);
        return driver.acceptsURL(connectionUrl);
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        Driver driver = getJdbcDriver(url);
        String connectionUrl = getConnectionUrl(url);
        log.info("JdbcLogDriver.connect = {}", driver.getClass());

        Connection conn = driver.connect(connectionUrl, info);
        return JdbcLogConnection.getInstance(conn);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        Driver driver = getJdbcDriver(url);
        String connectionUrl = getConnectionUrl(url);
        return driver.getPropertyInfo(connectionUrl, info);
    }

    @Override
    public int getMajorVersion() {
        if (drivers.size() == 1) {
            return getFirstDriver().getMajorVersion();
        }
        return 0;
    }

    @Override
    public int getMinorVersion() {
        if (drivers.size() == 1) {
            return getFirstDriver().getMinorVersion();
        }
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        if (drivers.size() == 1) {
            return getFirstDriver().jdbcCompliant();
        }
        return false;
    }

    // JDK 1.7
    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        if (drivers.size() == 1) {
            return getFirstDriver().getParentLogger();
        }
        return null;
    }

    private Driver getFirstDriver() {
        return drivers.values().iterator().next();
    }
}
