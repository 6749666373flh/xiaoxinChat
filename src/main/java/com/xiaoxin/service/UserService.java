package com.xiaoxin.service;

import com.xiaoxin.netty.ChatMsg;
import com.xiaoxin.pojo.Users;
import com.xiaoxin.pojo.vo.FriendRequestVO;
import com.xiaoxin.pojo.vo.MyFriendsVO;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface UserService {

    // 查询用户是否存在
    public boolean queryUserNameIsExist(String userName);

    // 查询是否登录
    public Users queryUserForLogin(String userName, String passWord);

    // 根据用户名查询
    public Users queryByUserName(String userName);

    // 保存新用户
    public Users saveUser(Users user) throws Exception;

    // 更新用户信息
    public Users updateUserInfo(Users user);

    // 搜索朋友前置条件
    public Integer preconditionSearchFriends(String myUserId, @RequestParam String friendUserName);

    // 发送添加好友请求,保存到数据库
    void sendFriendRequest(String myUserId, String friendUserName);

    // 查询所以好友申请
    public List<FriendRequestVO> queryFriendRequestList(String acceptUserId);

    // 更新好友请求记录,标记已处理
    public void updateFriendRequest(String sendUserId, String acceptUserId);

    // 通过好友请求
    public void passFriendRequest(String sendUserId, String acceptUserId);

    // 查询好友列表
    public List<MyFriendsVO> searchUserFriendsList(String userId);

    // 保存聊天记录
    public String saveMsg(ChatMsg msg);

    // 更新聊天消息为签收状态
    public void updateMsgSigned(String msgId);

    // 获取未读消息
    public List<com.xiaoxin.pojo.ChatMsg> getUnReadMsgList(String acceptUserId);
}
