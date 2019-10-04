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

/**
 * 通用标记解析器处理的是SQL脚本中#{parameter}、${parameter}参数，根据给定TokenHandler（标记处理器）来进行处理.
 * 解析器只是处理器处理的前提工序——解析，本类重在解析，而非处理，具体的处理会调用具体的TokenHandler的handleToken()方法来完成。
 */
public class GenericTokenParser {

    /** 开始记号 */
    private final String openToken;
    /** 结束记号 */
    private final String closeToken;
    /** 主要依赖TokenHandler */
    private final TokenHandler handler;

    /**
     * @param openToken  开始标记
     * @param closeToken 结束标记
     * @param handler    标记处理器
     */
    public GenericTokenParser(String openToken, String closeToken, TokenHandler handler) {
        this.openToken = openToken;
        this.closeToken = closeToken;
        this.handler = handler;
    }

    /**
     * 通过参数的开始标记与结束标记，识别text中的token，并让{@link GenericTokenParser#handler}对其进行一定的处理，组合成新串之后，架构新串返回。
     *
     * @param text text其实一般是SQL脚本字符串
     */
    public String parse(String text) {
        StringBuilder builder = new StringBuilder();
        if (text != null && text.length() > 0) {//判断text是否有解析的意义. text为null或者空字符串没有解析意义, 直接返回"".
            char[] src = text.toCharArray();
            int offset = 0;//就像指针一样
            int start = text.indexOf(openToken, offset);//让start指向第一个openToken
            //#{favouriteSection,jdbcType=VARCHAR}
            //这里是循环解析参数，参考GenericTokenParserTest,比如可以解析${first_name} ${initial} ${last_name} reporting.这样的字符串,里面有3个 ${}
            while (start > -1) {
                //判断一下 ${ 前面是否是反斜杠，这个逻辑在老版的mybatis中（如3.1.0）是没有的
                if (start > 0 && src[start - 1] == '\\') {//跳过这个变量
                    // the variable is escaped. remove the backslash(反斜线).
                    //新版已经没有调用substring了，改为调用如下的offset方式，提高了效率
                    //issue #760
                    //从offset开始从src中截取(start-offset-1)个字符, 拼接到builder
                    //这句话就是
                    builder.append(src, offset, start - offset - 1).append(openToken);
                    offset = start + openToken.length();//更新offset
                } else {//如果start为0或者openToken前面有\
                    int end = text.indexOf(closeToken, start);
                    if (end == -1) {
                        builder.append(src, offset, src.length - offset);
                        offset = src.length;
                    } else {
                        builder.append(src, offset, start - offset);
                        offset = start + openToken.length();
                        String content = new String(src, offset, end - offset);
                        //得到一对大括号里的字符串后，调用handler.handleToken,比如替换变量这种功能
                        builder.append(handler.handleToken(content));
                        offset = end + closeToken.length();
                    }
                }
                start = text.indexOf(openToken, offset);//更新start, 让其指向下一个openToken
            }
            if (offset < src.length) {
                builder.append(src, offset, src.length - offset);
            }
        }
        return builder.toString();
    }

}
