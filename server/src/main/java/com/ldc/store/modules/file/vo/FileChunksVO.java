package com.ldc.store.modules.file.vo;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FileChunksVO {

    /**
     * 已经上传的文件分片编号
     */
    private List<Integer> fileUploadedChunks;
}
