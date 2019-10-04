/*
 *    Copyright 2009-2013 the original author or authors.
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

import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 映射器代理工厂
 */
public class MapperProxyFactory<T> {

    private final Class<T> mapperInterface;
    private Map<Method, MapperMethod> methodCache = new ConcurrentHashMap<Method, MapperMethod>();

    //以下三个方法不重要
    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public Class<T> getMapperInterface() {
        return mapperInterface;
    }

    public Map<Method, MapperMethod> getMethodCache() {
        return methodCache;
    }

    /**
     * Proxy.newProxyInstance需要三个参数, 前两个参数很容易得到, 第三个参数由本方法的参数传入
     *
     * @param invocationHandler 一个实现了Invocation接口的对象, 指明如何处理方法调用
     * @return 返回一个代理者实例
     */
    @SuppressWarnings("unchecked")
    //newProxyInstance返回Object, 这里不进行检查, 直接转换成T类型, 所以用了注解
    protected T newInstance(MapperProxy<T> invocationHandler) {
        //通过静态方法newProxyInstance，获得代理对象。
        //第一个参数指明，这个代理对象由哪个类加载器加载. 此处使用mapperInterface的类加载器
        //第二个参数指明，代理对象实现哪些接口. 这里的数组里只有mapperInterface一个对象
        //第三个参数指明，如何处理方法调用. mapperProxy是实现了Invocation接口的对象.
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, invocationHandler);
    }

    /**
     * 最终通过Proxy.newProxyInstance(,,)生成代理对象
     * <p>
     * 基于本方法另一个重载方法实现
     *
     * @see MapperProxyFactory#newInstance(org.apache.ibatis.binding.MapperProxy)
     */
    public T newInstance(SqlSession sqlSession) {
        //每个InvocationHandler都持有一个被代理的对象
        final MapperProxy<T> invocationHandler = new MapperProxy<T>(sqlSession, mapperInterface, methodCache);
        return this.newInstance(invocationHandler);
    }

}
