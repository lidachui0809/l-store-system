package com.ldc.store.modules.file.context;

import com.ldc.store.modules.file.domain.RPanFile;
import lombok.Data;

@Data
public class ChunkFileMergeContext {

    /**
     * 文件名称
     */
    private String filename;

    /**
     * 文件唯一标识
     */
    private String identifier;

    /**
     * 文件总大小
     */
    private Long totalSize;

    /**
     * 文件的父文件夹ID
     */
    private Long parentId;

    /**
     * 当前登录的用户ID
     */
    private Long userId;

    /**
     * 物理文件记录
     */
    private RPanFile record;

}
