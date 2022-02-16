package com.fan.community.event;

import com.alibaba.fastjson.JSONObject;
import com.fan.community.entity.DiscussPost;
import com.fan.community.entity.Event;
import com.fan.community.entity.Message;
import com.fan.community.service.DiscussPostService;
import com.fan.community.service.ElasticsearchServer;
import com.fan.community.service.MessageService;
import com.fan.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchServer elasticsearchServer;

    @Value("${wk.image.commend}")
    private String wkImageCommend;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleComment(ConsumerRecord record) {
        if (record == null && record.value() == null) {
            logger.error("消息内容为空");
            return;
        }
        //将JSON的字符串恢复成相关的对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误");
            return;
        }

        //发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

    //消费发帖事件
    @KafkaListener(topics = (TOPIC_PUBLISH))
    public void handlePublishMessage(ConsumerRecord record) {
        if (record == null && record.value() == null) {
            logger.error("消息内容为空");
            return;
        }
        //将JSON的字符串恢复成相关的对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误");
            return;
        }

        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchServer.saveDiscussPost(post);
    }

    //消费删帖事件
    @KafkaListener(topics = (TOPIC_DELETE))
    public void handleDeleteMessage(ConsumerRecord record) {
        if (record == null && record.value() == null) {
            logger.error("消息内容为空");
            return;
        }
        //将JSON的字符串恢复成相关的对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误");
            return;
        }
        elasticsearchServer.deleteDiscussPost(event.getEntityId());
    }

    //消费分享事件
    @KafkaListener(topics = TOPIC_SHARE)
    public void handleShareMessage(ConsumerRecord record) {
        if (record == null && record.value() == null) {
            logger.error("消息内容为空");
            return;
        }
        //将JSON的字符串恢复成相关的对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误");
            return;
        }

        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        String cmd = wkImageCommend + "--quality 75 " + htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("生成长图成功：" + cmd);
        } catch (IOException e) {
            logger.error("生成长图失败：" + e.getMessage());
        }
    }
}
