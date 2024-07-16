package com.ldc.store.modules.file.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class FileTransferVO {



    @ApiModelProperty("要转移的文件ID集合，多个使用公用分隔符隔开")
    @NotBlank(message = "请选择要转移的文件")
    private String fileIds;

    @ApiModelProperty("要转移到的目标文件夹的ID")
    @NotBlank(message = "请选择要转移到哪个文件夹下面")
    private String targetParentId;

}
