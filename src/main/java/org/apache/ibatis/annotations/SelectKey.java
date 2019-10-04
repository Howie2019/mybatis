/*
 *    Copyright 2009-2011 the original author or authors.
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
package org.apache.ibatis.annotations;

import org.apache.ibatis.mapping.StatementType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 对应.xml的selectKey标签
 * <p>
 * 用于在插入一条记录后不执行select直接获得插入记录的主键值(包括自增主键和非自增主键).
 * <p>
 * 注意: 这个主键不是javaBean中本身就有的, 而是数据库自动生成的(设置autoincrement或者调用uuid/nextval函数).
 * mybatis会把这个数据库生成的主键回填到javaBean中
 * <p>
 * 见P36
 *
 * @author Clinton Begin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SelectKey {
    /**
     * SQL语句, MySQL数据库传入"SELECT LAST_INSERT_ID()"
     */
    String[] statement();

    /**
     * 自增主键对应的javaBean的属性名
     */
    String keyProperty();

    String keyColumn() default "";

    /**
     * 函数在insert之前执行吗?
     * <p>
     * 自增主键填after, 非自增主键填before
     */
    boolean before();

    /**
     * 函数返回值类型(在java中对应的类型)
     */
    Class<?> resultType();

    /**
     * 从StatementType这个枚举类型中选一填入
     */
    StatementType statementType() default StatementType.PREPARED;
}
