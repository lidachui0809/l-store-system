package com.ldc.store.modules.file.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ChunkFileUploadResultVO {


    @ApiModelProperty("是否需要合并文件 0 不需要 1 需要")
    private Integer mergeFlag;

}
