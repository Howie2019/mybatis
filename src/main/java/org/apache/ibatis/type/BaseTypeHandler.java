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
package org.apache.ibatis.type;

import org.apache.ibatis.session.Configuration;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 运用模板方法模式: setParameter方法是一个模板, setNonNullParameter方法则由子类去填充
 */
public abstract class BaseTypeHandler<T> extends TypeReference<T> implements TypeHandler<T> {

    protected Configuration configuration;

    public void setConfiguration(Configuration c) {
        this.configuration = c;
    }

    //模板, 将设置Statement参数分为null和nonnull两种情况, 而nonnull延迟到子类实现
    @Override
    public void setParameter(PreparedStatement ps, int parameterIndex, T value, JdbcType jdbcType) throws SQLException {
        //特殊情况，parameter为null,在这里已经能处理了
        if (value == null) {
            if (jdbcType == null) {
                //如果没设置jdbcType，报错啦
                //不指定jdbcType, ps不知道设置为哪种SQL NULL
                throw new TypeException("JDBC requires that the JdbcType must be specified for all nullable parameters.");
            }
            try {
                //SQL NULL分为很多种, 这里是把某个参数设为jdbcType对应的NULL
                //Sets the designated parameter to SQL NULL.
                ps.setNull(parameterIndex, jdbcType.TYPE_CODE);
            } catch (SQLException e) {
                throw new TypeException("Error setting null for value #" + parameterIndex + " with JdbcType " + jdbcType + " . " + "Try setting a different JdbcType for this value or a different jdbcTypeForNull configuration property. " + "Cause: " + e, e);
            }
        } else {
            //parameter不为null，怎么设还得交给不同的子类完成, setNonNullParameter是一个抽象方法
            setNonNullParameter(ps, parameterIndex, value, jdbcType);
        }
    }

    @Override
    public T getResult(ResultSet rs, String columnName) throws SQLException {
        T result = getNullableResult(rs, columnName);
        //通过ResultSet.wasNull判断是否为NULL
        if (rs.wasNull()) {
            return null;
        } else {
            return result;
        }
    }

    @Override
    public T getResult(ResultSet rs, int columnIndex) throws SQLException {
        T result = getNullableResult(rs, columnIndex);
        if (rs.wasNull()) {
            return null;
        } else {
            return result;
        }
    }

    @Override
    public T getResult(CallableStatement cs, int columnIndex) throws SQLException {
        T result = getNullableResult(cs, columnIndex);
        //通过CallableStatement.wasNull判断是否为NULL
        if (cs.wasNull()) {
            return null;
        } else {
            return result;
        }
    }

    //非NULL情况，怎么设参数还得交给不同的子类完成
    public abstract void setNonNullParameter(PreparedStatement ps, int parameterIndex, T value, JdbcType jdbcType) throws SQLException;

    /**
     * 增加对null列值为NULL的处理, 其实mybatis也没做啥处理,
     * 因为这个方法直接委托给数据库驱动去做, 数据库驱动已经进行NULL处理了.
     * 不过对于没有进行NULL处理的驱动, 这个方法还是有意义的.
     */
    public abstract T getNullableResult(ResultSet rs, String columnName) throws SQLException;

    /**
     * 增加对null列值为NULL的处理, 其实mybatis也没做啥处理, 因为数据库驱动已经进行NULL处理了
     */
    public abstract T getNullableResult(ResultSet rs, int columnIndex) throws SQLException;

    /**
     * 增加对null列值为NULL的处理, 其实mybatis也没做啥处理, 因为数据库驱动已经进行NULL处理了
     */
    public abstract T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException;

}
