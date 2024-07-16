package com.ldc.store.modules.file.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ldc.store.modules.file.domain.RPanUserFile;
import com.ldc.store.web.serializer.IdEncryptSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Objects;

@Data
public class BreadcrumbsVO {

    private static final long serialVersionUID = -6113151935665730951L;

    @ApiModelProperty("文件ID")
    @JsonSerialize(using = IdEncryptSerializer.class)
    private Long id;

    @ApiModelProperty("父文件夹ID")
    @JsonSerialize(using = IdEncryptSerializer.class)
    private Long parentId;

    @ApiModelProperty("文件夹名称")
    private String name;

    /**
     * 实体转换
     *
     * @param record
     * @return
     */
    public static BreadcrumbsVO transfer(RPanUserFile record) {
        BreadcrumbsVO vo = new BreadcrumbsVO();

        if (Objects.nonNull(record)) {
            vo.setId(record.getFileId());
            vo.setParentId(record.getParentId());
            vo.setName(record.getFilename());
        }

        return vo;
    }

}
