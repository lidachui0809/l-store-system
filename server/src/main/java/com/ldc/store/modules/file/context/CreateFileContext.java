package com.ldc.store.modules.file.context;

import lombok.Data;

@Data
public class CreateFileContext {

    private Long parentId;
    private String folderName;
    private Long userId;

}
