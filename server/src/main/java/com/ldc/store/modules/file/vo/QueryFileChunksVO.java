package com.ldc.store.modules.file.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class QueryFileChunksVO {

    @NotBlank(message = "文件唯一标识不可以为空")
    private String identifier;
}
