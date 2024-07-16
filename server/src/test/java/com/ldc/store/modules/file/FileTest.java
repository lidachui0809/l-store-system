package com.ldc.store.modules.file;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSONObject;
import com.ldc.store.StoreApplication;
import com.ldc.store.modules.file.constants.FileConstants;
import com.ldc.store.modules.file.context.*;
import com.ldc.store.modules.file.enums.DelFlagEnum;
import com.ldc.store.modules.file.enums.MergeFlagEnum;
import com.ldc.store.modules.file.service.IFileChunkService;
import com.ldc.store.modules.file.service.IFileService;
import com.ldc.store.modules.file.service.IUserFileService;
import com.ldc.store.modules.file.vo.ChunkFileUploadResultVO;
import com.ldc.store.modules.file.vo.FolderTreeNodeVO;
import com.ldc.store.modules.file.vo.UserFileResultVO;
import com.ldc.store.modules.user.context.UserRegisterContext;
import com.ldc.store.modules.user.service.IUserService;
import com.ldc.store.modules.user.vo.UserInfoVO;
import lombok.AllArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@SpringBootTest(classes = StoreApplication.class)
@RunWith(SpringRunner.class)
@Transactional
public class FileTest {

    @Autowired
    private IFileService iFileService;

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IUserFileService iUserFileService;

    @Autowired
    private IFileChunkService iFileChunkService;


    @Test
    public void testUploadFile() {

        Long userId = register();
        UserInfoVO userInfoVO = info(userId);

        UploadFileContext context = new UploadFileContext();
        MultipartFile file = genarateMultipartFile();
        context.setFile(file);
        context.setParentId(userInfoVO.getRootFileId());
        context.setUserId(userId);
        context.setIdentifier("12345678");
        context.setTotalSize(file.getSize());
        context.setFilename(file.getOriginalFilename());
        iUserFileService.upload(context);

        QueryFileListContext queryFileListContext = new QueryFileListContext();
        queryFileListContext.setDelFlag(DelFlagEnum.NO.getCode());
        queryFileListContext.setUserId(userId);
        queryFileListContext.setParentId(userInfoVO.getRootFileId());
        List<UserFileResultVO> fileList = iUserFileService.selectFolderList(queryFileListContext);
        Assert.notEmpty(fileList);
        Assert.isTrue(fileList.size() == 1);
    }


    /**
     * 测试文件分片上传成功
     */
    @Test
    public void uploadWithChunkTest() throws InterruptedException {
        Long userId = register();
        UserInfoVO userInfoVO = info(userId);

        CountDownLatch countDownLatch = new CountDownLatch(10);
        //CountDownLatch 一个用于多线程同步操作的计数器 只有当计数器归零 被await阻塞的线程才可以转为就绪态
        for (int i = 0; i < 10; i++) {
            new ChunkUploader(countDownLatch, i + 1, 10, iFileChunkService, userId, userInfoVO.getRootFileId()).start();
        }
        countDownLatch.await();
    }


    @AllArgsConstructor
    private static class ChunkUploader extends Thread {

        private CountDownLatch countDownLatch;

        private Integer chunk;

        private Integer chunks;

        private IFileChunkService iFileChunkService;


        private Long userId;

        private Long parentId;

        /**
         * 1、上传文件分片
         * 2、根据上传的结果来调用文件分片合并
         */
        @Override
        public void run() {
            super.run();
            MultipartFile file = genarateMultipartFile();
            Long totalSize = file.getSize() * chunks;
            String filename = "test.txt";
            String identifier = "123456789";

            //文件分片上传
            ChunkUploadFileContext fileChunkUploadContext = new ChunkUploadFileContext();
            fileChunkUploadContext.setFilename(filename);
            fileChunkUploadContext.setIdentifier(identifier);
            fileChunkUploadContext.setTotalChunks(chunks);
            fileChunkUploadContext.setChunkNumber(chunk);
            fileChunkUploadContext.setCurrentChunkSize(file.getSize());
            fileChunkUploadContext.setTotalSize(totalSize);
            fileChunkUploadContext.setFile(file);
            fileChunkUploadContext.setUserId(userId);
            ChunkFileUploadResultVO fileChunkUploadVO = iFileChunkService.chunkFileUpload(fileChunkUploadContext);

            if (fileChunkUploadVO.getMergeFlag().equals(MergeFlagEnum.READY.getCode())) {
                System.out.println("分片 " + chunk + " 检测到可以合并分片");

                ChunkFileMergeContext fileChunkMergeContext = new ChunkFileMergeContext();
                fileChunkMergeContext.setFilename(filename);
                fileChunkMergeContext.setIdentifier(identifier);
                fileChunkMergeContext.setTotalSize(totalSize);
                fileChunkMergeContext.setParentId(parentId);
                fileChunkMergeContext.setUserId(userId);
                iFileChunkService.mergeChunkFile(fileChunkMergeContext);
                countDownLatch.countDown();
            } else {
                countDownLatch.countDown();
            }
        }

    }

    @Test
    public void testFolderTreeNode(){
        Long userId = register();
        UserInfoVO info = info(userId);
        CreateFileContext context = new CreateFileContext();
        context.setFolderName("folder-1");
        context.setParentId(info.getRootFileId());
        context.setUserId(userId);
        Long folderId1 = iUserFileService.createFolder(context);

        context.setFolderName("folder-1-1");
        context.setParentId(folderId1);
        Long folderId1_1=iUserFileService.createFolder(context);

        context.setFolderName("folder-1-2");
        context.setParentId(folderId1);
        Long folderId1_2=iUserFileService.createFolder(context);


        context.setFolderName("folder-1-1-1");
        context.setParentId(folderId1_1);
        Long folderId1_1_1=iUserFileService.createFolder(context);

        context.setFolderName("folder-1-1-2");
        context.setParentId(folderId1_1);
        Long folderId1_1_2=iUserFileService.createFolder(context);

        QueryFolderTreeContext queryFolderTreeContext = new QueryFolderTreeContext();
        queryFolderTreeContext.setUserId(userId);
        List<FolderTreeNodeVO> folderTree = iUserFileService.getFolderTree(queryFolderTreeContext);
        String toJSONString = JSONObject.toJSONString(folderTree);
        System.out.println(toJSONString);


    }




    private UserInfoVO info(Long userId) {
        UserInfoVO userInfoVO = iUserService.info(userId);
        Assert.notNull(userInfoVO);
        return userInfoVO;
    }

    private static MultipartFile genarateMultipartFile() {
        MultipartFile file = null;
        try {
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < 10 ; i++) {
                String name = Thread.currentThread().getName();
                stringBuffer.append("线程: "+name);
                stringBuffer.append(" 第"+i+"文件分片 ");
                stringBuffer.append(" a ");
                stringBuffer.append(" lidachui\n\n");
            }
            file = new MockMultipartFile("file", "test.txt", "multipart/form-data", stringBuffer.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }


    private Long register() {
        UserRegisterContext context = createUserRegisterContext();
        Long register = iUserService.register(context);
        Assert.isTrue(register.longValue() > 0L);
        return register;
    }

    private UserRegisterContext createUserRegisterContext() {
        UserRegisterContext context = new UserRegisterContext();
        context.setUsername("USERNAME");
        context.setPassword("PASSWORD");
        context.setQuestion("QUESTION");
        context.setAnswer("ANSWER");
        return context;
    }

}
