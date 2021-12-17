package com.fan.community;

import com.fan.community.dao.DiscussPostMapper;
import com.fan.community.dao.UserMapper;
import com.fan.community.entity.DiscussPost;
import com.fan.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;


@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;
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
}
