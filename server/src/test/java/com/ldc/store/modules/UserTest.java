package com.ldc.store.modules;

import cn.hutool.core.lang.Assert;
import com.ldc.store.StoreApplication;
import com.ldc.store.modules.user.context.UserRegisterContext;
import com.ldc.store.modules.user.service.IRPanUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = StoreApplication.class)
@Transactional//测试类的数据注解 会将测试的数据 回滚 以免污染数据
public class UserTest {


    @Autowired
    private IRPanUserService irPanUserService;

    @Test
    public void testReg(){
        UserRegisterContext userRegisterContext = new UserRegisterContext();
        userRegisterContext.setUsername("lidachui");
        userRegisterContext.setPassword("lidachui");
        userRegisterContext.setQuestion("question");
        userRegisterContext.setAnswer("answer");
        String s = irPanUserService.register(userRegisterContext).getData().toString();
        Assert.isTrue(s.length()>0);
    }


    @Test
    public void test2(){
        UserRegisterContext userRegisterContext = new UserRegisterContext();
        userRegisterContext.setUsername("lidachui");
        userRegisterContext.setPassword("lidachui");
        userRegisterContext.setQuestion("question");
        userRegisterContext.setAnswer("answer");
        String s = irPanUserService.register(userRegisterContext).getData().toString();
        UserRegisterContext userRegisterContext2 = new UserRegisterContext();
        userRegisterContext2.setUsername("lidachui");
        userRegisterContext2.setPassword("lidachui");
        userRegisterContext2.setQuestion("question");
        userRegisterContext2.setAnswer("answer");
        String s2 = irPanUserService.register(userRegisterContext2).getData().toString();


    }


}
