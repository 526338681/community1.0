package com.fan.community.dao;

import com.fan.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    //offset每一页起始行的行号，limit每一页的数量
    List<DiscussPost> selectDiscussPost(int userId,int offset,int limit);
    //如果方法只有一个参数，并且在if里使用必须加别名
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    //查询帖子功能
    DiscussPost selectDiscussPostById(int id);
}
