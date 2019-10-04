/*
 *    Copyright 2009-2011 the original author or authors.
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
package org.apache.ibatis.executor.keygen;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;

import java.sql.Statement;

/**
 * 键值生成器,定了2个回调方法，processBefore,processAfter.<br/>
 * 这意味着, 所有实现这个接口的类都能接受别人回调
 */
public interface KeyGenerator {

    /** 回调方法, 在BaseStatementHandler中被调用 */
    void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter);

    /** 回调方法 */
    void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter);

}
