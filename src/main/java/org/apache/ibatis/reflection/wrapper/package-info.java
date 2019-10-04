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

/**
 * 不管Bean, Map 还是 Collection, 只要包装一下, 就能把他们统一对待(利用了多态)
 * <p>
 * 把XX包装一下就得到了XXWrapper. "包装一下"意味着XXWrapper持有一个XX
 * <p>
 * Object wrappers.
 */
package org.apache.ibatis.reflection.wrapper;
