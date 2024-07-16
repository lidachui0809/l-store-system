package com.ldc.store.modules.file.service;

import com.ldc.store.core.response.R;
import com.ldc.store.modules.file.context.*;
import com.ldc.store.modules.file.domain.RPanUserFile;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldc.store.modules.file.enums.FolderFlagEnum;
import com.ldc.store.modules.file.vo.BreadcrumbsVO;
import com.ldc.store.modules.file.vo.FileSearchResultVO;
import com.ldc.store.modules.file.vo.FolderTreeNodeVO;
import com.ldc.store.modules.file.vo.UserFileResultVO;

import java.util.List;

/**
* @author 李Da锤
* @description 针对表【r_pan_user_file(用户文件信息表)】的数据库操作Service
* @createDate 2024-07-12 13:37:32
*/
public interface IUserFileService extends IService<RPanUserFile> {

    Long createFolder(CreateFileContext createFileContext);

    RPanUserFile getUserRootFile(Long userId);

    List<UserFileResultVO> selectFolderList(QueryFileListContext queryFileListContext);

    R updateFileName(UpdateFilenameContext context);

    void deleteFiles(DeleteFileContext context);

    boolean secUpload(SecUploadFileContext context);

    void upload(UploadFileContext context);

    void saveMergeAfterFileRecord(ChunkFileMergeContext context);

    void downloadFile(FileDownloadContext fileDownloadContext);

    void previewFile(FilePreviewContext filePreviewContext);

    List<FolderTreeNodeVO> getFolderTree(QueryFolderTreeContext queryFolderTreeContext);

    void transferFile(FileTransferContext fileTransferContext);

    void copyFiles(FileCopyContext fileTransferContext);

    List<FileSearchResultVO> search(FileSearchContext context);

    List<BreadcrumbsVO> getBreadcrumbs(BreadcrumbsContext context);
}
