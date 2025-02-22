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
package org.apache.ibatis.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表示一个方法签名, (type,method,args)这个三维坐标能唯一定位一个方法
 * <p>
 * 用作Intercepts的参数,
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Signature {
    /**
     * 拦截哪个类
     */
    //就是定义哪些类，方法，参数需要被拦截
    Class<?> type();

    /**
     * 拦截哪个方法
     */
    String method();

    /**
     * 方法参数的类型, 有重载方法的话需要靠参数类型来区分
     */
    Class<?>[] args();
}