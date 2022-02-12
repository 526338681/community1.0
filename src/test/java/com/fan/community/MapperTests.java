package com.fan.community;

import com.fan.community.dao.DiscussPostMapper;
import com.fan.community.dao.LoginTicketMapper;
import com.fan.community.dao.MessageMapper;
import com.fan.community.dao.UserMapper;
import com.fan.community.entity.DiscussPost;
import com.fan.community.entity.LoginTicket;
import com.fan.community.entity.Message;
import com.fan.community.entity.User;
import org.junit.Test;

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
    @Autowired
    private MessageMapper messageMapper;

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
    @Test
    public void testSelectLetter() {
        List<Message> messages = messageMapper.selectConversations(111, 0, 20);
        for (Message message : messages) {
            System.out.println(message);
        }
        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);
        List<Message> list = messageMapper.selectLetters("111_112", 0, 10);
        for (Message message : list) {
            System.out.println(message);
        }
        int i = messageMapper.selectLetterCount("111_112");
        System.out.println(i);
        int selectLetterUnreadCount = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println(selectLetterUnreadCount);
    }
}
