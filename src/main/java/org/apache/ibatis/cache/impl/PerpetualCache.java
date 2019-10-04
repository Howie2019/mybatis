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
package org.apache.ibatis.cache.impl;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * 持有一个HashMap &lt Object, Object &gt
 * <p>
 * 永久缓存, 一旦存入就一直保持. mybatis默认使用这种缓存, 我们可以实现Cache接口自定义缓存
 * <p>
 * Session级别的一级缓使用PerpetualCache,
 * <p>
 * 当然PerpetualCache并非只能用来做一级缓存, 什么缓存它都可以做
 * <p>
 * 在没有配置的默认情况下，它只开启一级缓存
 */
public class PerpetualCache implements Cache {
    //每个永久缓存有一个ID来识别
    private String id;

    /**
     * cache的K和V都是Object, 说明这个缓存是通用的
     */
    private Map<Object, Object> cache = new HashMap<Object, Object>();

    public PerpetualCache(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getSize() {
        return cache.size();
    }

    @Override
    public void putObject(Object key, Object value) {
        cache.put(key, value);
    }

    @Override
    public Object getObject(Object key) {
        return cache.get(key);
    }

    @Override
    public Object removeObject(Object key) {
        return cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    /**
     * 这个缓存似乎不是线程安全的
     */
    @Override
    public ReadWriteLock getReadWriteLock() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        //只要id相等就认为两个cache相同
        if (getId() == null) {
            throw new CacheException("Cache instances require an ID.");
        }
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cache)) {
            return false;
        }

        Cache otherCache = (Cache) o;
        return getId().equals(otherCache.getId());
    }

    @Override
    public int hashCode() {
        if (getId() == null) {
            throw new CacheException("Cache instances require an ID.");
        }
        return getId().hashCode();
    }

}
