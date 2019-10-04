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
package org.apache.ibatis.executor;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cache.TransactionalCacheManager;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * 装饰者, 增加缓存功能
 * <p>
 * 二级缓存就是通过这个类实现的
 */
public class CachingExecutor implements Executor {
    private Executor delegate;
    private TransactionalCacheManager transactionalCacheManager = new TransactionalCacheManager();

    public CachingExecutor(Executor delegate) {
        this.delegate = delegate;
        delegate.setExecutorWrapper(this);
    }

    @Override
    public Transaction getTransaction() {
        return delegate.getTransaction();
    }

    @Override
    public void close(boolean forceRollback) {
        try {
            //issues #499, #524 and #573
            if (forceRollback) {
                transactionalCacheManager.rollback();
            } else {
                transactionalCacheManager.commit();
            }
        } finally {
            delegate.close(forceRollback);
        }
    }

    @Override
    public boolean isClosed() {
        return delegate.isClosed();
    }

    @Override
    public int update(MappedStatement ms, Object parameterObject) throws SQLException {
        //刷新缓存完再update
        flushCacheIfRequired(ms);
        return delegate.update(ms, parameterObject);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        BoundSql boundSql = ms.getBoundSql(parameterObject);
        //query时传入一个cachekey参数
        CacheKey cacheKey = createCacheKey(ms, parameterObject, rowBounds, boundSql);
        return query(ms, parameterObject, rowBounds, resultHandler, cacheKey, boundSql);
    }

    /**
     * 先从cache里面查
     *
     * @param rowBounds 行限制, 用于分页
     * @param cacheKey  cacheKey
     */
    //被ResultLoader.selectList调用
    @Override
    public <E> List<E> query(MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql) throws SQLException {
        Cache cache = mappedStatement.getCache();
        //默认情况下是没有开启缓存的(二级缓存).要开启二级缓存,你需要在你的 SQL 映射文件中添加一行: <cache/>
        //简单的说，就是先查CacheKey，查不到再委托给实际的执行器去查
        if (cache != null) {//这个SQL语句还没有
            flushCacheIfRequired(mappedStatement);
            if (mappedStatement.isUseCache() && resultHandler == null) {
                ensureNoOutParams(mappedStatement, parameterObject, boundSql);
                //先从cache里面查
                @SuppressWarnings("unchecked") List<E> list = (List<E>) transactionalCacheManager.getObject(cache, cacheKey);
                if (list == null) {
                    list = delegate.<E>query(mappedStatement, parameterObject, rowBounds, resultHandler, cacheKey, boundSql);
                    transactionalCacheManager.putObject(cache, cacheKey, list); // issue #578 and #116
                }
                return list;
            }
        }
        return delegate.<E>query(mappedStatement, parameterObject, rowBounds, resultHandler, cacheKey, boundSql);
    }

    @Override
    public List<BatchResult> flushStatements() throws SQLException {
        return delegate.flushStatements();
    }

    @Override
    public void commit(boolean required) throws SQLException {
        delegate.commit(required);
        transactionalCacheManager.commit();
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        try {
            delegate.rollback(required);
        } finally {
            if (required) {
                transactionalCacheManager.rollback();
            }
        }
    }

    private void ensureNoOutParams(MappedStatement ms, Object parameter, BoundSql boundSql) {
        if (ms.getStatementType() == StatementType.CALLABLE) {
            for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
                if (parameterMapping.getMode() != ParameterMode.IN) {
                    throw new ExecutorException("Caching stored procedures with OUT params is not supported.  Please configure useCache=false in " + ms.getId() + " statement.");
                }
            }
        }
    }

    @Override
    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
        return delegate.createCacheKey(ms, parameterObject, rowBounds, boundSql);
    }

    @Override
    public boolean isCached(MappedStatement ms, CacheKey key) {
        return delegate.isCached(ms, key);
    }

    @Override
    public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {
        delegate.deferLoad(ms, resultObject, property, key, targetType);
    }

    @Override
    public void clearLocalCache() {
        delegate.clearLocalCache();
    }

    private void flushCacheIfRequired(MappedStatement ms) {
        Cache cache = ms.getCache();
        if (cache != null && ms.isFlushCacheRequired()) {
            transactionalCacheManager.clear(cache);
        }
    }

    @Override
    public void setExecutorWrapper(Executor executor) {
        throw new UnsupportedOperationException("This method should not be called");
    }

}
