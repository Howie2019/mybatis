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
package org.apache.ibatis.reflection.wrapper;

import org.apache.ibatis.reflection.MetaObject;

/**
 * 对象包装器工厂, 我们可以通过实现此接口来自定义WrapperFactory
 *
 * @see org.apache.ibatis.domain.misc.CustomBeanWrapperFactory
 */
public interface WrapperFactory {

    /** 有没有能用的包装器 */
    boolean hasWrapperFor(Object object);

    /** 得到包装器 */
    Wrapper getWrapperFor(MetaObject metaObject, Object object);

}
