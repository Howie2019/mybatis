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

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 实现java数据类型和数据库的数据类型的相互转化, 对应config.xml的typeHandlers标签
 * <p><br/>
 * setParameter方法用于在设置SQL参数时将java数据类型转换成数据库的数据类型;
 * <p>
 * getResult方法用于从ResultSet中取列值的时候, 将数据库的数据类型转换成java数据类型
 * <p>
 * 有时候mybatis自带的TypeHandler无法满足需要, 我们可以实现此接口来自定义TypeHandler. 然后在typeHandlers标签中配置进去就OK啦
 */
public interface TypeHandler<T> {

    /**
     * 为PreparedStatement设置参数
     *
     * @param ps             PreparedStatement
     * @param parameterIndex 参数索引, the first parameter is 1, the second is 2, ...
     * @param value          参数值
     * @param jdbcType       在value为null的时候就有用了
     */
    void setParameter(PreparedStatement ps, int parameterIndex, T value, JdbcType jdbcType) throws SQLException;

    /**
     * @return 如果rs为null, 则返回null. 如果rs不为null, 则返回columnName这一列的值
     */
    T getResult(ResultSet rs, String columnName) throws SQLException;

    /**
     * @return 如果rs为null, 则返回null. 如果rs不为null, 则返回columnIndex这一列的值
     */
    T getResult(ResultSet rs, int columnIndex) throws SQLException;

    /**
     * @return 如果cs为null, 则返回null. 如果cs不为null, 则返回columnIndex这一列的值
     */
    T getResult(CallableStatement cs, int columnIndex) throws SQLException;

}
