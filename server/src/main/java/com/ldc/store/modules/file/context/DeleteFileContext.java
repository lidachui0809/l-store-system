package com.ldc.store.modules.file.context;


import com.ldc.store.modules.file.domain.RPanUserFile;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 文件批量删除上下文对象
 */
@Data
public class DeleteFileContext implements Serializable {

    /**
     * 要删除的文件ID
     */
    private List<Long> fileIdList;

    /**
     * 当前的登录用户ID
     */
    private Long userId;

}
