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

import org.apache.ibatis.session.TransactionIsolationLevel;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;

/**
 * Creates {@link Transaction} instances.
 */
public interface TransactionFactory {

    /**
     * Sets transaction factory custom properties.
     * 设置TransactionFactory的属性, 这些属性会影响到工厂的产品
     */
    //JdbcTransactionFactory实现为空方法
    //ManagedTransactionFactory只设置了closeConnection字段
    //如果你要自己实现一个工厂可以利用Properties里的更多属性
    void setProperties(Properties props);

    /**
     * Creates a {@link Transaction} out of an existing connection.
     *
     * @param conn Existing database connection
     * @return Transaction
     * @since 3.1.0
     */
    Transaction newTransaction(Connection conn);

    /**
     * Creates a {@link Transaction} out of a datasource.
     *
     * @param dataSource DataSource to take the connection from
     * @param level      Desired isolation level
     * @param autoCommit Desired autocommit
     * @return Transaction
     * @since 3.1.0
     */
    //根据数据源和事务隔离级别创建Transaction
    Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit);

}
