package org.apache.ibatis.cache.decorators;
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

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple blocking decorator
 * Sipmle and inefficient version of EhCache's BlockingCache decorator.
 * It sets a lock over a cache key when the element is not found in cache.
 * This way, other threads will wait until this element is filled instead of hitting the database.
 *
 * @author Eduardo Macarron
 */
public class BlockingCache implements Cache {

    private long timeout;
    private final Cache delegate;
    /** a ConcurrentHashMap */
    private final ConcurrentHashMap<Object, ReentrantLock> locks;

    public BlockingCache(Cache delegate) {
        this.delegate = delegate;
        this.locks = new ConcurrentHashMap<Object, ReentrantLock>();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    @Override
    public void putObject(Object key, Object value) {
        try {
            delegate.putObject(key, value);
        } finally {
            releaseLock(key);
        }
    }

    @Override
    public Object getObject(Object key) {
        acquireLock(key);
        Object value = delegate.getObject(key);
        if (value != null) {
            releaseLock(key);
        }
        return value;
    }

    @Override
    public Object removeObject(Object key) {
        return delegate.removeObject(key);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return null;
    }

    /** 每个key都有自己的lock对象, 此方法得到key对应的的lock对象 */
    private ReentrantLock getLockForKey(Object key) {
        ReentrantLock lock = new ReentrantLock();
        //键值对已存在则get,不存在则put.
        ReentrantLock previous = locks.putIfAbsent(key, lock);
        return previous == null ? lock : previous;
    }

    /** 对key对应的锁对象上锁, 根据timeout字段选择执行trylock方法还是lock方法 */
    private void acquireLock(Object key) {
        Lock lock = getLockForKey(key);
        if (timeout > 0) {//定时等待
            try {
                boolean acquired = lock.tryLock(timeout, TimeUnit.MILLISECONDS);
                if (!acquired) {//获取锁超时
                    throw new CacheException("Couldn't get a lock in " + timeout + " for the key " + key + " at the cache " + delegate.getId());
                }
            } catch (InterruptedException e) {
                throw new CacheException("Got interrupted while trying to acquire lock for key " + key, e);
            }
        } else {//永远等待
            lock.lock();
        }
    }

    /** 解锁 */
    private void releaseLock(Object key) {
        ReentrantLock lock = locks.get(key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}