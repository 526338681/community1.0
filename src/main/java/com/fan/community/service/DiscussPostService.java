package com.fan.community.service;

import com.fan.community.dao.DiscussPostMapper;
import com.fan.community.entity.DiscussPost;
import com.fan.community.util.RedisKeyUtil;
import com.fan.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private RedisTemplate redisTemplate;

    public List<DiscussPost> findDiscussPost(int userId,int offset,int limit,int orderMode){
        return discussPostMapper.selectDiscussPost(userId,offset,limit,orderMode);
    }
    public int findDiscussPostRows(int userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    //添加帖子
    public int addDiscussPost(DiscussPost post) {
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        //转义HTML标签
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        //过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));
        return discussPostMapper.insertDiscussPost(post);
    }

    //查询帖子详情
    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    //更新帖子的数量
    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    //置顶
    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    //加精
    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    //更新帖子分数
    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }

    //查询当前帖子是否置顶
//    public boolean hasTop(int entityType, int entityId) {
//        String topKey = RedisKeyUtil.getTopKey(entityType, entityId);
//        return redisTemplate.opsForZSet().score(topKey, entityId) != null;
//    }
}
