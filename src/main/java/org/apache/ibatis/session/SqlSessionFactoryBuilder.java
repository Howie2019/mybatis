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
package org.apache.ibatis.session;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

/**
 * 这个Builder返回的是DefaultSqlSessionFactory对象
 * <p>
 * Builds {@link SqlSessionFactory} instances.
 */

public class SqlSessionFactoryBuilder {

    //SqlSessionFactoryBuilder有9个build()方法
    //发现mybatis文档老了,http://www.mybatis.org/core/java-api.html,关于这块对不上

    //以下3个方法都是调用下面第4种方法
    public SqlSessionFactory build(Reader reader) {
        return build(reader, null, null);
    }

    public SqlSessionFactory build(Reader reader, String environment) {
        return build(reader, environment, null);
    }

    public SqlSessionFactory build(Reader reader, Properties properties) {
        return build(reader, null, properties);
    }

    //第4种方法是最常用的，它使用了一个参照了XML文档或更特定的SqlMapConfig.xml文件的Reader实例。
    //可选的参数是environment和properties。Environment决定加载哪种环境(开发环境/生产环境)，包括数据源和事务管理器。
    //如果使用properties，那么就会加载那些properties（属性配置文件），那些属性可以用${propName}语法形式多次用在配置文件中。和Spring很像，一个思想？
    public SqlSessionFactory build(Reader reader, String environment, Properties properties) {
        try {
            //委托XMLConfigBuilder来解析xml文件，并构建
            XMLConfigBuilder parser = new XMLConfigBuilder(reader, environment, properties);
            return build(parser.parse());
        } catch (Exception e) {
            //这里是捕获异常，包装成自己的异常并抛出的idiom？，最后还要reset ErrorContext
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            ErrorContext.instance().reset();
            try {
                reader.close();
            } catch (IOException e) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }

    //以下3个方法都是调用下面第8种方法
    public SqlSessionFactory build(InputStream inputStream) {
        return build(inputStream, null, null);
    }

    public SqlSessionFactory build(InputStream inputStream, String environment) {
        return build(inputStream, environment, null);
    }

    public SqlSessionFactory build(InputStream inputStream, Properties properties) {
        return build(inputStream, null, properties);
    }

    /**
     * 另外三个参数更少的重载都基于此方法
     *
     * @param inputStream .xml的输入流
     * @param environment 使用哪一个<environment>
     * @param properties  properties
     * @return 返回建造好的工厂
     */
    //第8种方法和第4种方法差不多，Reader换成了InputStream
    public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
        try {
            XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, environment, properties);
            return build(parser.parse());
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            ErrorContext.instance().reset();
            try {
                inputStream.close();
            } catch (IOException e) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }

    /**
     * 这个方法是最底层的, 换言之, 不管你通过什么方式build SqlSessionFactory, 最后都转化为通过SqlSessionFactory对象来build.
     *
     * @param config Configuration
     * @return 返回DefaultSqlSessionFactory
     */
    //这里我们可以自己实现SqlSessionFactory接口, 然后在子类覆盖此方法, 从而得到自己的SqlSessionFactory
    public SqlSessionFactory build(Configuration config) {
        return new DefaultSqlSessionFactory(config);
    }

}
