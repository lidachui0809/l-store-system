package com.ldc.store.modules.file.context;

import com.ldc.store.modules.file.domain.RPanUserFile;
import lombok.Data;

import java.util.List;

@Data
public class FileTransferContext {

    private List<Long> transferFileId;

    private Long userId;

    private Long targetParentId;

    private List<RPanUserFile> prepareFiles;
}
