package com.xiaoxin.netty;

import com.xiaoxin.SpringUtil;
import com.xiaoxin.enums.MsgActionEnum;
import com.xiaoxin.service.UserService;
import com.xiaoxin.utils.JsonUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    public static ChannelGroup users = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        users.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
//        System.out.println(ctx.channel().id().asLongText());
        users.remove(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        cause.printStackTrace();
        users.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        //System.out.println(msg.text());

        //users.writeAndFlush(new TextWebSocketFrame("服务器收到消息:" + text.text()));
        Channel channel = ctx.channel();
        String content = msg.text();
        // 获取客户端发来的消息
        UserService userServiceImpl = (UserService) SpringUtil.getBean("userServiceImpl");
        DataContent dataContent = JsonUtils.jsonToPojo(content, DataContent.class);
        assert dataContent != null;
        Integer action = dataContent.getAction();
        // 根据消息类型进行不同处理
        if (action.equals(MsgActionEnum.CONNECT.type)) {
            // 初始化连接请求, 把channel和userId关联起来
            String senderId = dataContent.getChatMsg().getSenderId();
            UserChannelRel.put(senderId, channel);

        } else if (action.equals(MsgActionEnum.CHAT.type)) {
            // 聊天请求, 把聊天记录保存到数据库,同时标记消息未签收
            ChatMsg chatMsg = dataContent.getChatMsg();

            String msgText = chatMsg.getMsg();
            String receiverId = chatMsg.getReceiverId();
            String senderId = chatMsg.getSenderId();
            // 保存到数据库,标记未签收
            String msgId = userServiceImpl.saveMsg(chatMsg);
            chatMsg.setMsgId(msgId);

            // 发送消息
            // 获取接收方channel
            Channel receiverChannel = UserChannelRel.get(receiverId);
            if (receiverChannel == null) {
                // 接收方未连接(离线)

            }else{
                Channel receiverChannelId = users.find(receiverChannel.id());
                if (receiverChannelId != null) {
                    // 用户在线
                    receiverChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContent)));
                }else {
                    // 离线
                }
            }

        } else if (action.equals(MsgActionEnum.SIGNED.type)) {
            // 签收消息,对消息进行签收,修改数据库中对应消息的签收状态
            // 扩展字段在signed类型的消息中,代表需要去签收的消息id,逗号间隔
            String msgIdsStr = dataContent.getExtand();
            String[] ids = msgIdsStr.split(",");

            Arrays.stream(ids).forEach((id -> {
                if (!StringUtils.isBlank(id)) {
                    // 批量签收
                    userServiceImpl.updateMsgSigned(id);
                }
            }));

        } else if (action.equals(MsgActionEnum.KEEPALIVE.type)) {
            // 心跳消息

        }
    }
}
