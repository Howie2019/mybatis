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
package org.apache.ibatis.cache.decorators;

import org.apache.ibatis.cache.Cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Least Recently Used最近最少使用<br/>
 * 基于 LinkedHashMap 覆盖其 removeEldestEntry 方法实现。
 */
public class LruCache implements Cache {
    private final Cache delegate;
    /** 额外用了一个map来做lru，但是delegate其实也是一个map，这样等于用2倍的内存实现lru功能 */
    private Map<Object, Object> keyMap;
    /** 在keyMap中移除entry时, 把被移除的entry的key存到这里, 以便让delegate同步移除这个entry */
    private Object eldestKey;

    /**
     * 默认用于LRU的map大小为1024
     */
    public LruCache(Cache delegate) {
        this.delegate = delegate;
        setSize(1024);
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    /**
     * @param size 用于LRU的map的初始大小
     */
    public void setSize(final int size) {
        keyMap = new LinkedHashMap<Object, Object>(size, .75F, true) {
            private static final long serialVersionUID = 4267176411845948333L;

            /**
             * @return <tt>true</tt> if the eldest entry should be removed,
             *         <tt>false</tt> if it should be retained.
             */
            //核心就是覆盖 LinkedHashMap.removeEldestEntry方法,
            //返回true或false告诉 LinkedHashMap要不要删除此最老键值
            //
            //LinkedHashMap内部其实就是每次访问或者插入一个元素都会把元素放到链表末尾，
            // 这样不经常访问的键值肯定就在链表开头啦
            @Override
            protected boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {
                boolean tooBig = size() > size;
                if (tooBig) {
                    //eldest的key存入实例变量eldestKey, 稍后根据这个key移除委托者的键值对
                    eldestKey = eldest.getKey();
                }
                //如果map撑大了,则返回true, 这意味着最老的entry会被移除
                return tooBig;
            }
        };
    }

    @Override
    public void putObject(Object key, Object value) {
        delegate.putObject(key, value);
        //增加新纪录后，判断是否要将最老元素移除
        cycleKeyList(key);
    }

    /** get的时候调用一下LinkedHashMap.get，让经常访问的值移动到链表末尾 */
    @Override
    public Object getObject(Object key) {
        //get的时候调用一下LinkedHashMap.get，让经常访问的值移动到链表末尾
        keyMap.get(key); //touch
        return delegate.getObject(key);
    }

    @Override
    public Object removeObject(Object key) {
        return delegate.removeObject(key);
    }

    @Override
    public void clear() {
        delegate.clear();
        keyMap.clear();
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return null;
    }

    /**
     * 把keyMap中的KeyList转(cycle)一下
     *
     * @param key 这个key会把最老的key置换掉
     */
    private void cycleKeyList(Object key) {
        keyMap.put(key, key);
        //keyMap是linkedhashmap，最老的记录已经被移除了，然后这里我们还需要移除被委托的那个cache的记录
        if (eldestKey != null) {
            delegate.removeObject(eldestKey);
            eldestKey = null;
        }
    }

}
