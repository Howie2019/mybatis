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
package org.apache.ibatis.reflection.invoker;

import java.lang.reflect.InvocationTargetException;

/**
 * 调用者
 */
public interface Invoker {

    /**
     * @param target 在谁身上调用
     * @param args 调用传入的参数
     * @return 返回调用结果
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException;

    /**
     * @return 被invoke的对象是什么类型
     */
    Class<?> getType();
}
