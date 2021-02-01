package com.xiaoxin.service.impl;

import com.xiaoxin.enums.MsgActionEnum;
import com.xiaoxin.enums.MsgSignFlagEnum;
import com.xiaoxin.enums.SearchFriendsStatusEnum;
import com.xiaoxin.idworker.Sid;
import com.xiaoxin.mapper.ChatMsgMapper;
import com.xiaoxin.mapper.FriendRequestMapper;
import com.xiaoxin.mapper.MyFriendsMapper;
import com.xiaoxin.mapper.UserMapper;
import com.xiaoxin.netty.ChatMsg;
import com.xiaoxin.netty.DataContent;
import com.xiaoxin.netty.UserChannelRel;
import com.xiaoxin.pojo.FriendsRequest;
import com.xiaoxin.pojo.MyFriends;
import com.xiaoxin.pojo.Users;
import com.xiaoxin.pojo.vo.FriendRequestVO;
import com.xiaoxin.pojo.vo.MyFriendsVO;
import com.xiaoxin.service.UserService;
import com.xiaoxin.utils.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MyFriendsMapper myFriendsMapper;

    @Autowired
    private FriendRequestMapper friendRequestMapper;

    @Autowired
    private ChatMsgMapper chatMsgMapper;

    @Autowired
    private Sid sid;

    @Autowired
    private QRCodeUtils qrCodeUtils;

    @Autowired
    private FastDFSClient fastDFSClient;


    @Override
    public boolean queryUserNameIsExist(String userName) {

        Users users = new Users();
        users.setUsername(userName);
        Users userResult = userMapper.selectOne(users);
        return userResult != null;
    }


    @Override
    public Users queryUserForLogin(String userName, String passWord) {
        Example example = new Example(Users.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username", userName);
        criteria.andEqualTo("password", passWord);

        return userMapper.selectOneByExample(example);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users saveUser(Users user) throws Exception {
        user.setNickname(user.getUsername());
        user.setFaceImage("");
        user.setFaceImageBig("");
        user.setPassword(MD5Utils.getMD5Str(user.getPassword()));

        // 创建二维码格式
        String qrCodePath = user.getUsername() + "qrcode.png";
        qrCodeUtils.createQRCode(qrCodePath, user.getUsername());
        MultipartFile qrCodeFile = FileUtils.fileToMultipart(qrCodePath);
        String qrCodeUrl = fastDFSClient.uploadQRCode(qrCodeFile);
        // System.out.println(qrCodeUrl);
        user.setQrcode(qrCodeUrl);
        user.setId(sid.nextShort());

        userMapper.insert(user);
        return user;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users updateUserInfo(Users user) {

        userMapper.updateByPrimaryKeySelective(user);
        return queryById(user.getId());
    }

    /**
    * @Description: 前置条件搜索,是否可添加该好友
    * @date: 2021/1/28 16:24
    * @param myUserId:
    * @param friendUserName:
    * @Return: java.lang.Integer
    */
    @Override
    public Integer preconditionSearchFriends(String myUserId, String friendUserName) {

        Users friend = queryByUserName(friendUserName);
        // 搜索不存在返回
        if(friend == null) return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        // 账号为自己不能添加
        if(friend.getId().equals(myUserId)) return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        // 该账号已为自己好友
        Example example = new Example(MyFriends.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("myUserId", myUserId);
        criteria.andEqualTo("myFriendUserId", friend.getId());
        MyFriends myFriends = myFriendsMapper.selectOneByExample(example);
        if(myFriends != null) return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;

        return SearchFriendsStatusEnum.SUCCESS.status;
    }

    @Override
    public void sendFriendRequest(String myUserId, String friendUserName) {
        // 根据用户名把朋友信息查询出来
        Users friend = queryByUserName(friendUserName);

        Example example = new Example(FriendsRequest.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("sendUserId", myUserId);
        criteria.andEqualTo("acceptUserId", friend.getId());

        FriendsRequest friendsRequest = friendRequestMapper.selectOneByExample(example);
        if (friendsRequest == null) {
            // 不是好友并且无未处理的好友添加记录,发送请求保存数据库
            String id = sid.nextShort();
            FriendsRequest request = new FriendsRequest();
            request.setId(id);
            request.setSendUserId(myUserId);
            request.setAcceptUserId(friend.getId());
            request.setRequestDataTime(new Date());
            request.setStatus(0);
            friendRequestMapper.insert(request);
        }
    }

    @Override
    public List<FriendRequestVO> queryFriendRequestList(String acceptUserId) {

        return userMapper.queryFriendRequestList(acceptUserId);
    }

    @Override
    public void updateFriendRequest(String sendUserId, String acceptUserId) {
        Example example = new Example(FriendsRequest.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("sendUserId", sendUserId);
        criteria.andEqualTo("acceptUserId", acceptUserId);
        FriendsRequest friendsRequest = friendRequestMapper.selectOneByExample(example);
        if (friendsRequest != null) {
            friendsRequest.setStatus(1);
            friendRequestMapper.updateByPrimaryKeySelective(friendsRequest);
        }
    }

    @Transactional
    @Override
    public void passFriendRequest(String sendUserId, String acceptUserId) {
        saveFriend(sendUserId,acceptUserId);
        saveFriend(acceptUserId,sendUserId);
        updateFriendRequest(sendUserId,acceptUserId);
        updateFriendRequest(acceptUserId,sendUserId);

        // 使用websocket主动推送消息到请求发起者,更新他的通讯录
        DataContent dataContent = new DataContent();
        dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);
        UserChannelRel.get(sendUserId).writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContent)));
    }

    @Override
    public List<MyFriendsVO> searchUserFriendsList(String userId) {

        return userMapper.searchUserFriendsList(userId);
    }


    @Override
    public String saveMsg(ChatMsg msg) {
        com.xiaoxin.pojo.ChatMsg msgDB = new com.xiaoxin.pojo.ChatMsg();
        msgDB.setId(Sid.next());
        msgDB.setAcceptUserId(msg.getReceiverId());
        msgDB.setSendUserId(msg.getSenderId());
        msgDB.setCreateTime(new Date());
        msgDB.setSignFlag(MsgSignFlagEnum.unsign.type);
        msgDB.setMsg(msg.getMsg());

        chatMsgMapper.insert(msgDB);
        return msgDB.getId();
    }

    @Override
    public void updateMsgSigned(String msgId) {
        com.xiaoxin.pojo.ChatMsg chatMsg = new com.xiaoxin.pojo.ChatMsg();
        chatMsg.setId(msgId);
        chatMsg.setSignFlag(1);
        chatMsgMapper.updateByPrimaryKeySelective(chatMsg);
    }

    @Override
    public List<com.xiaoxin.pojo.ChatMsg> getUnReadMsgList(String acceptUserId) {
        Example example = new Example(com.xiaoxin.pojo.ChatMsg.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("acceptUserId", acceptUserId);
        criteria.andEqualTo("signFlag",0);
        return chatMsgMapper.selectByExample(example);
    }


    private void saveFriend(String sendUserId, String acceptUserId) {

        Example example = new Example(MyFriends.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("myUserId", acceptUserId)
                .andEqualTo("myFriendUserId", sendUserId);

        MyFriends friend = myFriendsMapper.selectOneByExample(example);
        // System.out.println(friend);
        if(friend == null){
            MyFriends myFriends = new MyFriends();
            myFriends.setId(Sid.next());
            myFriends.setMyUserId(acceptUserId);
            myFriends.setMyFriendUserId(sendUserId);
            myFriendsMapper.insert(myFriends);
        }

    }

    private Users queryById(String userId) {
        return userMapper.selectByPrimaryKey(userId);
    }

    @Override
    public Users queryByUserName(String username) {
        Example example = new Example(Users.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username", username);
        return userMapper.selectOneByExample(example);
    }

}
