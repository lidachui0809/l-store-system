package com.ldc.store.modules.file.controller;

import com.google.common.base.Splitter;
import utils.UserInfoHolder;
import com.ldc.store.core.constants.RPanConstants;
import com.ldc.store.core.response.R;
import com.ldc.store.core.utils.IdUtil;
import com.ldc.store.modules.file.constants.FileConstants;
import com.ldc.store.modules.file.context.*;
import com.ldc.store.modules.file.convert.FileConverter;
import com.ldc.store.modules.file.enums.DelFlagEnum;
import com.ldc.store.modules.file.service.IFileChunkService;
import com.ldc.store.modules.file.service.IUserFileService;
import com.ldc.store.modules.file.vo.*;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/file")
@Api(tags = "文件模块")
@Validated
public class FileController {


    @Autowired
    private FileConverter fileConverter;

    @Autowired
    private IUserFileService fileUserService;

    @Autowired
    private IFileChunkService iFileChunkService;


    /**
     * 获得指定目录下的所有文件信息
     * @return
     */
    @GetMapping("/list")
    public R<List<UserFileResultVO>> list(@NotBlank(message = "parentId不可以为空")@RequestParam(value = "parentId") String parentId,
                                          @RequestParam(value = "fileType" ,defaultValue = FileConstants.ALL_TYPE_FILE) String fileTypes) {
        //解密parentId
        Long deParentId= IdUtil.decrypt(parentId);

        //前端有可能会传入多个fileType
        List<Integer> fileType = null;
        if(!Objects.equals(fileTypes, FileConstants.ALL_TYPE_FILE)){
            fileType= Splitter.on(RPanConstants.COMMON_SEPARATOR).
                    splitToList(fileTypes).stream().map(Integer::valueOf).collect(Collectors.toList());
        }
        QueryFileListContext queryFileListContext = new QueryFileListContext();
        queryFileListContext.setParentId(deParentId);
        queryFileListContext.setFileTypeArray(fileType);
        queryFileListContext.setDelFlag(DelFlagEnum.NO.getCode());
        queryFileListContext.setUserId(UserInfoHolder.get());
        //根据parentId查找该目录下的 指定文件类型的文件
        List<UserFileResultVO> userFileResultVOList = fileUserService.selectFolderList(queryFileListContext);
        return R.data(userFileResultVOList);
    }

    //新建文件夹 父文件id 文件名
    @PostMapping("/folder")
    public R createFolder(@NotBlank(message = "parentId不可以为空")@RequestParam(value = "parentId") String parentId,
                                                @NotBlank(message = "文件夹名不可以为空") @RequestParam(value = "folderName") String folderName){
        CreateFileContext context =
                fileConverter.create2CreateFileContext(parentId,folderName);
        Long folderId = fileUserService.createFolder(context);
        return R.data(IdUtil.encrypt(folderId));
    }

    @PutMapping("/rename")
    public R createFolder(@RequestBody UpdateFilenameVO updateFilenameVO){
        //重命名 判断file_id合法性 判断重命名的字符是否符合规则 检验是否有权限 检验是否存在同级相同文件名
        UpdateFilenameContext context
                = fileConverter.updateFileNameVO2UpdateFilenameContext(updateFilenameVO);
        return fileUserService.updateFileName(context);
    }

    @DeleteMapping("/delete")
    public R createFolder(@RequestBody DeleteFileVO deleteFileVO){
        //重命名 判断file_id合法性 判断重命名的字符是否符合规则 检验是否有权限 检验是否存在同级相同文件名
        DeleteFileContext context
                = fileConverter.deleteFilePO2DeleteFileContext(deleteFileVO);
        List<Long> fileIds = Splitter.on(deleteFileVO.getFileIds()).splitToList(RPanConstants.COMMON_SEPARATOR)
                .stream().map(IdUtil::decrypt).collect(Collectors.toList());
        context.setFileIdList(fileIds);
        fileUserService.deleteFiles(context);
        return R.success();
    }

    /**
     * 文件秒传 主要是依赖于文件的唯一标识 文件并非真实上传 只是在数据表中插入了文件记录 建立关联
     * @param secUploadFileVO
     * @return
     */
    @PostMapping("/sec-upload")
    public R secUpload(@RequestBody SecUploadFileVO secUploadFileVO){
        SecUploadFileContext context=fileConverter.secUploadFileVO2SecUploadFileContext(secUploadFileVO);
        boolean success=fileUserService.secUpload(context);
        if(success){
            return R.success();
        }
        return R.fail("文件秒传失败！请手动上传！");
    }


    /**
     * 文件上传  先记录文件信息 再将文件流交给存储引擎存储 最后完善数据库文件实体信息
     * @param uploadFileVO
     * @return
     */
    @PostMapping("/upload")
    public R upload(@RequestBody  UploadFileVO uploadFileVO){
        UploadFileContext context=fileConverter.uploadFileVO2UploadFileContext(uploadFileVO);
        fileUserService.upload(context);
        return R.success();
    }

    /**
     * 文件分片上传
     * @param chunkUploadFileVO
     * @return
     */
    @PostMapping("/chunk-upload")
    public R chunkUpload(@RequestBody ChunkUploadFileVO chunkUploadFileVO){
        ChunkUploadFileContext context=fileConverter.chunkUploadFileVO2ChunkUploadFileContext(chunkUploadFileVO);
        ChunkFileUploadResultVO chunkFileUploadVO=iFileChunkService.chunkFileUpload(context);
        return R.data(chunkFileUploadVO);
    }
}
