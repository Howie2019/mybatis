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
package org.apache.ibatis.reflection.factory;

import java.util.List;
import java.util.Properties;

/**
 * 当把一条记录映射为javaBean时就要用ObjectFactory反射创建javaBean实例
 */

public interface ObjectFactory {

    /**
     * Sets configuration properties.
     * 对应objectFactory节点的property子节点
     * <p>
     * 设置工厂的某些属性. 这个方法不是拿来给程序员用的. 而是给XMLConfigBuilder的objectFactoryElement方法用的
     *
     * @param properties configuration properties
     */
    void setProperties(Properties properties);

    /**
     * 用默认构造器创建一个type类型的对象对象. 反射机制
     *
     * @param type 被创建对象的类型
     */
    <T> T create(Class<T> type);

    /**
     * Creates a new object with the specified constructor and params.
     * 生产对象，使用指定的构造函数和构造函数参数
     *
     * @param type                Object type
     * @param constructorArgTypes 参数类型列表
     * @param constructorArgs     参数值列表
     */
    <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);

    /**
     * type是否是集合
     * <p>
     * Returns true if this object can have a set of other objects.
     * It's main purpose is to support non-java.util.Collection objects like Scala collections.
     * <p>
     * 为了支持Scala collections？
     * *
     *
     * @param type Object type
     * @return whether it is a collection or not
     * @since 3.1.0
     */
    <T> boolean isCollection(Class<T> type);

}
