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
package org.apache.ibatis.binding;

import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.SqlSession;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * MapperProxy这个命名误导人, 其实它并不是代理对象, 它只是个InvocationHandler, 代理实例是由Proxy.newProxyInstance方法生成的
 * <p>
 * 以前这样写: sqlSession.selectOne("select标签的id",参数)
 * <p>
 * 现在这样写: mapper.select标签的id(参数)
 * <p>
 * 这样不用担心把"select标签的id"这个字符串输错啦
 * <p>
 * 这个类实现了InvocationHandler, 表明这个类有能力处理对委托类的方法调用
 */
// InvocationHandler相当于一个拦截器,
// 所有对委托者的调用都转由InvocationHandler处理,
// InvocationHandler再去调用委托者.
public class MapperProxy<T> implements InvocationHandler, Serializable {
    private static final long serialVersionUID = -6424540398559729838L;

    private final SqlSession sqlSession;
    private final Class<T> mapperInterface;
    /**
     * MapperMethod对象缓存
     */
    private final Map<Method, MapperMethod> methodCache;

    /**
     * 初始化本类所有字段
     */
    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
        this.methodCache = methodCache;
    }

    /**
     * invoke代表一次方法调用(从反射的角度)
     *
     * @param proxy  在谁身上调用
     * @param method 调用什么方法
     * @param args   传入这个方法的参数
     * @return 这个方法的返回
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //代理以后，所有Mapper的方法调用时，都会调用这个invoke方法

        //效率优化: 并不是任何一个方法都需要执行调用代理对象进行执行，
        // 如果这个方法是Object中的方法（toString、hashCode等）,直接调用此方法
        // 否则在methodCache找MapperMethod对象, 执行MapperMethod对象的execute方法
        if (Object.class.equals(method.getDeclaringClass())) {
            try {
                //第一个参数: the object the underlying method is invoked from
                //第一个参数: 指明这个方法在哪个对象上调用, 一般是委托人(被代理者)
                return method.invoke(this, args);
            } catch (Throwable t) {
                throw ExceptionUtil.unwrapThrowable(t);
            }
        }
        //这里优化了，去缓存中找MapperMethod
        final MapperMethod mapperMethod = cachedMapperMethod(method);
        //让这个方法执行自己
        return mapperMethod.execute(sqlSession, args);
    }

    /**
     * 去缓存中找MapperMethod, 如果没有则建立Method和MapperMethod的映射
     *
     * @param method key
     * @return value
     */
    private MapperMethod cachedMapperMethod(Method method) {
        MapperMethod mapperMethod = methodCache.get(method);
        if (mapperMethod == null) {
            //找不到才去new
            mapperMethod = new MapperMethod(mapperInterface, method, sqlSession.getConfiguration());
            methodCache.put(method, mapperMethod);
        }
        return mapperMethod;
    }

}
