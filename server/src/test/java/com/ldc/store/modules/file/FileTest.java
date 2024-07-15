package com.ldc.store.modules.file;

import cn.hutool.core.lang.Assert;
import com.ldc.store.StoreApplication;
import com.ldc.store.modules.file.context.QueryFileListContext;
import com.ldc.store.modules.file.context.UploadFileContext;
import com.ldc.store.modules.file.enums.DelFlagEnum;
import com.ldc.store.modules.file.service.IFileService;
import com.ldc.store.modules.file.service.IUserFileService;
import com.ldc.store.modules.file.vo.UserFileResultVO;
import com.ldc.store.modules.user.context.UserRegisterContext;
import com.ldc.store.modules.user.service.IUserService;
import com.ldc.store.modules.user.vo.UserInfoVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    private UserInfoVO info(Long userId) {
        UserInfoVO userInfoVO = iUserService.info(userId);
        Assert.notNull(userInfoVO);
        return userInfoVO;
    }

    private static MultipartFile genarateMultipartFile() {
        MultipartFile file = null;
        try {
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < 1024 * 1024; i++) {
                stringBuffer.append("a");
            }
            file = new MockMultipartFile("file", "test.txt", "multipart/form-data", stringBuffer.toString().getBytes("UTF-8"));
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
