/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.transaction.jdbc;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Delays connection retrieval until getConnection() is called.
 * Ignores commit or rollback requests when autocommit is on.
 *
 * @see JdbcTransactionFactory
 */

/**
 * Jdbc事务。直接利用JDBC的commit,rollback。
 * 它依赖于从数据源得 到的连接来管理事务范围。
 * 直观地讲，就是JdbcTransaction是使用的java.sql.Connection 上的commit和rollback功能，
 * JdbcTransaction只是相当于对java.sql.Connection事务处理进行了一次包装（wrap），
 * Transaction的事务管理都是通过java.sql.Connection实现的。
 */
public class JdbcTransaction implements Transaction {
    private static final Log log = LogFactory.getLog(JdbcTransaction.class);

    /**
     * JdbcTransaction依赖于Connection的commit和rollback
     */
    protected Connection connection;
    protected DataSource dataSource;
    protected TransactionIsolationLevel level;
    protected boolean autoCommmit;

    public JdbcTransaction(DataSource ds, TransactionIsolationLevel desiredLevel, boolean desiredAutoCommit) {
        dataSource = ds;
        level = desiredLevel;
        autoCommmit = desiredAutoCommit;
    }

    public JdbcTransaction(Connection connection) {
        this.connection = connection;
    }

    /**
     * 这说明每个Transaction都持有一个Connection
     */
    @Override
    public Connection getConnection() throws SQLException {
        if (connection == null) {
            openConnection();
        }
        return connection;
    }

    /**
     * 委托给Connection的commit方法
     */
    @Override
    public void commit() throws SQLException {
        //!connection.getAutoCommit()表明开启事务了
        if (connection != null && !connection.getAutoCommit()) {
            if (log.isDebugEnabled()) {
                log.debug("Committing JDBC Connection [" + connection + "]");
            }
            connection.commit();
        }
    }

    /**
     * 委托给Connection的rollback方法
     */
    @Override
    public void rollback() throws SQLException {
        if (connection != null && !connection.getAutoCommit()) {
            if (log.isDebugEnabled()) {
                log.debug("Rolling back JDBC Connection [" + connection + "]");
            }
            connection.rollback();
        }
    }

    /**
     * 关闭连接, 同时关闭事务
     */
    @Override
    public void close() throws SQLException {
        if (connection != null) {
            resetAutoCommit();//关闭事务
            if (log.isDebugEnabled()) {
                log.debug("Closing JDBC Connection [" + connection + "]");
            }
            connection.close();
        }
    }

    protected void setDesiredAutoCommit(boolean desiredAutoCommit) {
        try {
            //和原来的比一下，再设置autocommit，是考虑多次重复设置的性能问题？
            if (connection.getAutoCommit() != desiredAutoCommit) {
                if (log.isDebugEnabled()) {
                    log.debug("Setting autocommit to " + desiredAutoCommit + " on JDBC Connection [" + connection + "]");
                }
                connection.setAutoCommit(desiredAutoCommit);
            }
        } catch (SQLException e) {
            // Only a very poorly implemented driver would fail here,
            // and there's not much we can do about that.
            throw new TransactionException("Error configuring AutoCommit.  " + "Your driver may not support getAutoCommit() or setAutoCommit(). " + "Requested setting: " + desiredAutoCommit + ".  Cause: " + e, e);
        }
    }

    /**
     * 重置自动提交为true(关闭事务)
     */
    //见下面注释，貌似是说是对有些DB的一个workaround
    protected void resetAutoCommit() {
        try {
            if (!connection.getAutoCommit()) {//AutoCommit=false, 表明开启事务
                // MyBatis does not call commit/rollback on a connection if just selects were performed.
                // Some databases start transactions with select statements
                // and they mandate a commit/rollback before closing the connection.
                // A workaround is setting the autocommit to true before closing the connection.
                // Sybase throws an exception here.
                if (log.isDebugEnabled()) {
                    log.debug("Resetting autocommit to true on JDBC Connection [" + connection + "]");
                }
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            log.debug("Error resetting autocommit to true " + "before closing the connection.  Cause: " + e);
        }
    }

    protected void openConnection() throws SQLException {
        if (log.isDebugEnabled()) {
            log.debug("Opening JDBC Connection");
        }
        connection = dataSource.getConnection();
        if (level != null) {
            connection.setTransactionIsolation(level.getLevel());
        }
        setDesiredAutoCommit(autoCommmit);
    }

}
