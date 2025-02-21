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
package org.apache.ibatis.transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 对应config.xml的transactionManager节点. transactionManager的type属性为JDBC则使用JdbcTransaction这个实现类
 * <p>
 * Wraps a database connection.
 * Handles the connection lifecycle that comprises: its creation, preparation, commit/rollback and close.
 * 事务，包装了一个Connection, 包含commit,rollback,close方法
 * 在 MyBatis 中有两种事务管理器类型(也就是 type=”[JDBC|MANAGED]”):
 *
 * @author Clinton Begin
 */
public interface Transaction {

    /**
     * Retrieve inner database connection
     *
     * @return DataBase connection
     */
    Connection getConnection() throws SQLException;

    /**
     * Commit inner database connection.
     */
    void commit() throws SQLException;

    /**
     * Rollback inner database connection.
     */
    void rollback() throws SQLException;

    /**
     * Close inner database connection.
     */
    void close() throws SQLException;

}
