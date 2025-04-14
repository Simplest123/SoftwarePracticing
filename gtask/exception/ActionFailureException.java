/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.micode.notes.gtask.exception;

/**
 * 自定义运行时异常类，用于表示任务操作过程中发生的动作失败
 * 继承自RuntimeException，说明这是非受检异常，通常由程序逻辑错误引起
 */
public class ActionFailureException extends RuntimeException {
    // 序列化版本号，用于保证序列化/反序列化的兼容性
    private static final long serialVersionUID = 4425249765923293627L;

    /**
     * 空参构造器，创建没有详细错误信息的异常对象
     * 适用于简单抛出异常场景
     */
    public ActionFailureException() {
        super();
    }

    /**
     * 创建包含错误描述的异常对象
     * 
     * @param paramString 错误信息文本，用于说明具体的失败原因
     *                    例如："网络请求超时"、"数据解析失败"
     */
    public ActionFailureException(String paramString) {
        super(paramString);
    }

    /**
     * 创建包含错误描述和根本原因的异常对象
     * 
     * @param paramString    错误信息文本
     * @param paramThrowable 触发当前异常的原始异常对象（根本原因）
     *                       例如：捕获的IO异常、JSON解析异常等
     *                       便于通过getCause()方法追踪异常链
     */
    public ActionFailureException(String paramString, Throwable paramThrowable) {
        super(paramString, paramThrowable);
    }
}
