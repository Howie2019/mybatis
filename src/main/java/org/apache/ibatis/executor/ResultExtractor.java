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
package org.apache.ibatis.executor;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Array;
import java.util.List;

/**
 * 结果抽取器
 * 唯一公有方法: extractObjectFromList
 */
public class ResultExtractor {
    private final Configuration configuration;
    private final ObjectFactory objectFactory;

    public ResultExtractor(Configuration configuration, ObjectFactory objectFactory) {
        this.configuration = configuration;
        this.objectFactory = objectFactory;
    }

    /**
     * @param list
     * @param targetType 目标类型
     * @return
     */
    public Object extractObjectFromList(List<Object> list, Class<?> targetType) {
        Object result = null;
        if (targetType != null && targetType.isAssignableFrom(list.getClass())) {
            //1.如果targetType是list，直接返回list:List<Object>
            result = list;
        } else if (targetType != null && objectFactory.isCollection(targetType)) {
            //2.如果targetType是Collection，用反射创建一个targetType类型的实例, 返回
            result = objectFactory.create(targetType);
            MetaObject metaObject = configuration.newMetaObject(result);
            metaObject.addAll(list);
        } else if (targetType != null && targetType.isArray()) {
            //3.如果targetType是数组，则数组转list
            Class<?> arrayComponentType = targetType.getComponentType();
            Object array = Array.newInstance(arrayComponentType, list.size());
            if (arrayComponentType.isPrimitive()) {
                for (int i = 0; i < list.size(); i++) {
                    Array.set(array, i, list.get(i));
                }
                result = array;
            } else {
                result = list.toArray((Object[]) array);
            }
        } else {
            //4.最后返回list的第0个元素
            if (list != null && list.size() > 1) {
                throw new ExecutorException("Statement returned more than one row, where no more than one was expected.");
            } else if (list != null && list.size() == 1) {
                result = list.get(0);
            }
        }
        return result;
    }
}
