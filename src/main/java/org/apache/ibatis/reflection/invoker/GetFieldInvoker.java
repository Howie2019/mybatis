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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * 通过反射, 获取字段的值
 */
public class GetFieldInvoker implements Invoker {
    private Field field;

    /**
     * @param field 待取值的字段. 也就是调用invoke将得到target的对应的字段值
     */
    public GetFieldInvoker(Field field) {
        this.field = field;
    }

    /**
     * 获得target的field字段的值
     */
    @Override
    public Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException {
        return field.get(target);
    }

    /**
     * @return a Class object identifying the declared type of the field represented by this object
     */
    @Override
    public Class<?> getType() {
        return field.getType();
    }
}
