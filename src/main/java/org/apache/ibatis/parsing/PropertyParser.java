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
package org.apache.ibatis.parsing;

import java.util.Properties;

/**
 * 用properties标签引入或者配置一系列property, 然后通过${xxx}的形式在.xml其他地方引用
 * <p>
 * 而这个类就是用于解析.xml文件中的property引用
 */
public class PropertyParser {

    private PropertyParser() {
        // Prevent Instantiation
    }

    /**
     * 识别${}这种模式
     */
    public static String parse(String string, Properties variables) {
        VariableTokenHandler handler = new VariableTokenHandler(variables);
        GenericTokenParser genericTokenParser = new GenericTokenParser("${", "}", handler);
        return genericTokenParser.parse(string);
    }

    //就是一个map，用相应的value替换key
    private static class VariableTokenHandler implements TokenHandler {
        private Properties variables;

        public VariableTokenHandler(Properties variables) {
            this.variables = variables;
        }

        @Override
        public String handleToken(String content) {
            if (variables != null && variables.containsKey(content)) {
                return variables.getProperty(content);
            }
            return "${" + content + "}";
        }
    }
}
