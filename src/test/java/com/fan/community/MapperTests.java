package com.fan.community;

import com.fan.community.dao.DiscussPostMapper;
import com.fan.community.dao.LoginTicketMapper;
import com.fan.community.dao.UserMapper;
import com.fan.community.entity.DiscussPost;
import com.fan.community.entity.LoginTicket;
import com.fan.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;


@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private LoginTicketMapper loginTicketMapper;


    @Test
    public void testselectById(){
        User user = userMapper.selectById(101);
        System.out.println(user);
    }
    @Test
    public void testselectDiscussPost(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPost(0,0,10);
        for (DiscussPost post : list) {
            System.out.println(post);
        }
        int row = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(row);
    }
    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }
    //org.mybatis.spring.MyBatisSystemException: nested exception is org.apache.ibatis.reflection.ReflectionException: Error instantiating interface com.fan.community.dao.LoginTicketMapper with invalid types () or values (). Cause: java.lang.NoSuchMethodException: com.fan.community.dao.LoginTicketMapper.<init>()
//    @Test
//    public void testSelectByTicket(){
//        LoginTicketMapper loginTicket = loginTicketMapper.selectByTicket("abc");
//        System.out.println(loginTicket);
//        loginTicketMapper.updateStatus("abc",1);
//        loginTicket = loginTicketMapper.selectByTicket("abc");
//        System.out.println(loginTicket);
//    }
}
