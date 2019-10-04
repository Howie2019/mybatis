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
package org.apache.ibatis.executor.result;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.WrapperFactory;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;

import java.util.Map;

/**
 * 内部用Map存储结果的ResultHandler
 *
 * @see DefaultResultHandler
 */
public class DefaultMapResultHandler<K, V> implements ResultHandler {

    /** 内部实现是存了一个Map */
    private final Map<K, V> mappedResults;
    /***/
    private final String mapKey;
    private final ObjectFactory objectFactory;
    private final WrapperFactory wrapperFactory;

    @SuppressWarnings("unchecked")
    public DefaultMapResultHandler(String mapKey, ObjectFactory objectFactory, WrapperFactory wrapperFactory) {
        this.objectFactory = objectFactory;
        this.wrapperFactory = wrapperFactory;
        //用反射创建, 而不是new
        this.mappedResults = objectFactory.create(Map.class);
        this.mapKey = mapKey;
    }

    @Override
    public void handleResult(ResultContext context) {
        // TODO is that assignment always true?
        //得到一条记录
        final V resultObject = (V) context.getResultObject();
        //MetaObject.forObject,包装一下记录
        //MetaObject是用反射来包装各种类型
        final MetaObject metaObject = MetaObject.forObject(resultObject, objectFactory, wrapperFactory);
        // TODO is that assignment always true?
        final K key = (K) metaObject.getValue(mapKey);
        mappedResults.put(key, resultObject);
        //这个类主要目的是把得到的List转为Map
    }

    public Map<K, V> getMappedResults() {
        return mappedResults;
    }
}
