package com.rei.stats.gsampler.jdbc

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.SQLFeatureNotSupportedException

import javax.sql.DataSource

class ConnectionFactory implements DataSource {

    def url
    def user
    def password

    protected ConnectionFactory() {}

    ConnectionFactory(url, user, password) {
        this.url = url
        this.user = user
        this.password = password
    }

    Connection getConnection() {
        return getConnection(user, password)
    }

    Connection getConnection(String username, String password) {
        Properties props = new Properties();
        if (user != null) {
            props.setProperty("user", user);
        }
        if (password != null) {
            props.setProperty("password", password);
        }
        return DriverManager.getConnection(url, props)
    }

    java.io.PrintWriter getLogWriter() throws SQLException { throw new UnsupportedOperationException() }
    void setLogWriter(java.io.PrintWriter out) throws SQLException { throw new UnsupportedOperationException() }
    void setLoginTimeout(int seconds) throws SQLException { throw new UnsupportedOperationException() }
    int getLoginTimeout() throws SQLException { throw new UnsupportedOperationException() }
    java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException { throw new UnsupportedOperationException() }

    @Override
    <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException()
    }

    boolean isWrapperFor(java.lang.Class<?> iface) throws java.sql.SQLException { throw new UnsupportedOperationException() }
}
