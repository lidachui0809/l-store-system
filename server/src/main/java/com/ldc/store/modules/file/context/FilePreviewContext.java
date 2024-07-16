package com.ldc.store.modules.file.context;

import lombok.Data;

import javax.servlet.http.HttpServletResponse;

@Data
public class FilePreviewContext {

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 请求响应对象
     */
    private HttpServletResponse response;

    /**
     * 当前登录的用户ID
     */
    private Long userId;
}
