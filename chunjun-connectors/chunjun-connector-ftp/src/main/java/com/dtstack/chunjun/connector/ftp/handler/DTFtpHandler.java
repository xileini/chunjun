/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dtstack.chunjun.connector.ftp.handler;

import com.dtstack.chunjun.connector.ftp.conf.FtpConfig;
import com.dtstack.chunjun.connector.ftp.extend.ftp.IFtpHandler;

import java.io.IOException;

public interface DTFtpHandler extends IFtpHandler {

    /**
     * 登录服务器
     *
     * @param ftpConfig 连接配置
     */
    void loginFtpServer(FtpConfig ftpConfig);

    /**
     * 登出服务器
     *
     * @throws IOException logout error
     */
    void logoutFtpServer() throws IOException;

    /**
     * 关闭ftp输入流
     *
     * @throws IOException 文件句柄操作异常
     */
    void completePendingCommand() throws IOException;
}
