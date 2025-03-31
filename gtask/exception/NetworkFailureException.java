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
 * 自定义受检异常类，用于表示网络通信过程中发生的故障
 * 继承自Exception，说明这是受检异常，调用方必须进行捕获或声明抛出
 * 常见触发场景：网络连接超时、服务端无响应、SSL握手失败等网络相关问题
 */
public class NetworkFailureException extends Exception {
    // 序列化版本号，保证不同版本类的序列化兼容性
    // 当类结构变化时需校验此版本号，避免反序列化失败
    private static final long serialVersionUID = 2107610287180234136L;

    /**
     * 空参构造器，创建无详细错误信息的异常对象
     * 适用于快速抛出但不需要额外描述的场景
     */
    public NetworkFailureException() {
        super();
    }

    /**
     * 创建包含错误描述的异常对象
     * 
     * @param paramString 自定义错误信息，用于说明具体的网络故障类型
     *                    示例："连接超时，请检查网络"、"服务端返回500错误"
     */
    public NetworkFailureException(String paramString) {
        super(paramString);
    }

    /**
     * 创建包含错误描述和根本原因的异常对象
     * 
     * @param paramString    自定义错误信息
     * @param paramThrowable 触发当前异常的原始异常对象（如IOException等）
     *                       便于通过getCause()方法追溯异常根源
     *                       示例：包装底层SocketTimeoutException
     */
    public NetworkFailureException(String paramString, Throwable paramThrowable) {
        super(paramString, paramThrowable);
    }
}