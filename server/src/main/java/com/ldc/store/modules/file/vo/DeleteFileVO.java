package com.ldc.store.modules.file.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 文件重命名参数对象
 */
@Data
public class DeleteFileVO implements Serializable {

    @NotBlank(message = "文件id不能为空")
    private String fileIds;

}
