/*
 *    Copyright 2009-2014 the original author or authors.
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
package org.apache.ibatis.datasource.pooled;

import org.apache.ibatis.reflection.ExceptionUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 池化的连接. 相当于在Connection外面加了一层拦截.<br/>
 * 这是一个InvocationHandler.
 */
class PooledConnection implements InvocationHandler {
    private static final String CLOSE = "close";
    /** 只有Connection.class一个元素的数组*/
    private static final Class<?>[] IFACES = new Class<?>[]{Connection.class};

    private int hashCode = 0;
    private PooledDataSource dataSource;
    //真正的连接
    private Connection realConnection;
    //代理的连接
    private Connection proxyConnection;
    private long checkoutTimestamp;
    private long createdTimestamp;
    private long lastUsedTimestamp;
    private int connectionTypeCode;
    /** a flag, 用于invalidate方法 */
    private boolean valid;

    /**
     * Constructor for SimplePooledConnection that uses the Connection and PooledDataSource passed in
     *
     * @param connection - the connection that is to be presented as a pooled connection
     * @param dataSource - the dataSource that the connection is from
     */
    public PooledConnection(Connection connection, PooledDataSource dataSource) {
        this.hashCode = connection.hashCode();
        this.realConnection = connection;
        this.dataSource = dataSource;
        this.createdTimestamp = System.currentTimeMillis();
        this.lastUsedTimestamp = System.currentTimeMillis();
        this.valid = true;
        this.proxyConnection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), IFACES, this);
    }

    /**
     * Invalidates the connection
     */
    public void invalidate() {
        valid = false;
    }

    /**
     * to see if the connection(this PooledConnection itself) is usable
     *
     * @return True if the connection is usable
     */
    //1. valid 为真
    //2. realConnection不为空
    //3. dataSource ping 本对象 ping 得通
    public boolean isValid() {
        return valid && realConnection != null && dataSource.pingConnection(this);
    }

    /*
     * Getter for the *real* connection that this wraps
     *
     * @return The connection
     */
    public Connection getRealConnection() {
        return realConnection;
    }

    /**
     * @return The proxyConnection
     */
    public Connection getProxyConnection() {
        return proxyConnection;
    }

    /*
     * Gets the hashcode of the real connection (or 0 if it is null)
     *
     * @return The hashcode of the real connection (or 0 if it is null)
     */
    public int getRealHashCode() {
        return realConnection == null ? 0 : realConnection.hashCode();
    }

    /*
     * Getter for the connection type (based on url + user + password)
     *
     * @return The connection type
     */
    public int getConnectionTypeCode() {
        return connectionTypeCode;
    }

    /*
     * Setter for the connection type
     *
     * @param connectionTypeCode - the connection type
     */
    public void setConnectionTypeCode(int connectionTypeCode) {
        this.connectionTypeCode = connectionTypeCode;
    }

    /*
     * Getter for the time that the connection was created
     *
     * @return The creation timestamp
     */
    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    /*
     * Setter for the time that the connection was created
     *
     * @param createdTimestamp - the timestamp
     */
    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    /*
     * Getter for the time that the connection was last used
     *
     * @return - the timestamp
     */
    public long getLastUsedTimestamp() {
        return lastUsedTimestamp;
    }

    /*
     * Setter for the time that the connection was last used
     *
     * @param lastUsedTimestamp - the timestamp
     */
    public void setLastUsedTimestamp(long lastUsedTimestamp) {
        this.lastUsedTimestamp = lastUsedTimestamp;
    }

    /*
     * Getter for the time since this connection was last used
     *
     * @return - the time since the last use
     */
    public long getTimeElapsedSinceLastUse() {
        return System.currentTimeMillis() - lastUsedTimestamp;
    }

    /**
     * @return 这个PooledConnection从创建那一刻起, 过了多少毫秒(这就是age)
     */
    public long getAge() {
        return System.currentTimeMillis() - createdTimestamp;
    }

    /*
     * Getter for the timestamp that this connection was checked out
     *
     * @return the timestamp
     */
    public long getCheckoutTimestamp() {
        return checkoutTimestamp;
    }

    /*
     * Setter for the timestamp that this connection was checked out
     *
     * @param timestamp the timestamp
     */
    public void setCheckoutTimestamp(long timestamp) {
        this.checkoutTimestamp = timestamp;
    }

    /*
     * Getter for the time that this connection has been checked out
     *
     * @return the time
     */
    public long getCheckoutTime() {
        return System.currentTimeMillis() - checkoutTimestamp;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    /*
     * Allows comparing this connection to another
     *
     * @param obj - the other connection to test for equality
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PooledConnection) {
            return realConnection.hashCode() == (((PooledConnection) obj).realConnection.hashCode());
        } else if (obj instanceof Connection) {
            return hashCode == obj.hashCode();
        } else {
            return false;
        }
    }

    /**
     * 所有对realConnection的调用, 转为调用proxyConnection对应方法.
     * Required for InvocationHandler implementation.
     *
     * @param proxy  - not used
     * @param method - the method to be executed
     * @param args   - the parameters to be passed to the method
     * @see java.lang.reflect.InvocationHandler#invoke(Object, java.lang.reflect.Method, Object[])
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        //如果调用close的话，忽略它，反而将这个connection加入到池中
        if (CLOSE.hashCode() == methodName.hashCode() && CLOSE.equals(methodName)) {
            dataSource.pushConnection(this);
            return null;
        } else {
            try {
                if (!Object.class.equals(method.getDeclaringClass())) {
                    // issue #579 toString() should never fail
                    // throw an SQLException instead of a Runtime
                    //除了toString()方法，其他方法调用之前要检查connection是否还是合法的,不合法要抛出SQLException
                    checkConnection();
                }
                //其他的方法，则交给真正的connection去调用
                //realConnection.methodXX();
                return method.invoke(realConnection, args);
            } catch (Throwable t) {
                throw ExceptionUtil.unwrapThrowable(t);
            }
        }
    }

    /**
     * 检查Connection is valid or not
     *
     * @throws SQLException if invalid
     */
    private void checkConnection() throws SQLException {
        if (!valid) {
            throw new SQLException("Error accessing PooledConnection. Connection is invalid.");
        }
    }

}
