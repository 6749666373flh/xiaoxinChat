package com.xiaoxin.controller;


import com.xiaoxin.aop.LoginTrack;
import com.xiaoxin.enums.OperatorFriendRequestTypeEnum;
import com.xiaoxin.enums.SearchFriendsStatusEnum;
import com.xiaoxin.mapper.MyFriendsMapper;
import com.xiaoxin.mapper.UserMapper;
import com.xiaoxin.pojo.ChatMsg;
import com.xiaoxin.pojo.Users;
import com.xiaoxin.pojo.bo.UsersBO;
import com.xiaoxin.pojo.vo.FriendRequestVO;
import com.xiaoxin.pojo.vo.MyFriendsVO;
import com.xiaoxin.pojo.vo.UsersVO;
import com.xiaoxin.service.UserService;
import com.xiaoxin.utils.FastDFSClient;
import com.xiaoxin.utils.FileUtils;
import com.xiaoxin.utils.MD5Utils;
import com.xiaoxin.utils.MyJSONResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    private StringRedisTemplate redisTemplate;


    /**
    * @Description: 注册或登录
    * @date: 2021/1/27 21:35
    */
    @PostMapping("/login")
    @LoginTrack(count = 5)
    public MyJSONResult login(@RequestBody Users user, HttpServletRequest request) throws Exception {

        if (StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())) {
            return MyJSONResult.errorMap("用户名或密码不能为空");
        }
        if (user.getUsername().length() > 12 || user.getUsername().length() < 6 ||
                user.getPassword().length() > 12 || user.getPassword().length() < 6) {
            return MyJSONResult.errorMsg("用户名和密码长度请在6-12之间");
        }


        Users userResult = userService.queryUserForLogin(user.getUsername(), MD5Utils.getMD5Str(user.getPassword()));
        if(userResult == null) {

            return MyJSONResult.errorMsg("用户或密码不正确");
        }

        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(userResult,usersVO);
        return MyJSONResult.ok(usersVO);
    }

    @PostMapping("/register")
    @LoginTrack(count = 5)
    public MyJSONResult register(@RequestBody Users user) throws Exception {

        if (StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword()) || StringUtils.isBlank(user.getEmail())) {
            return MyJSONResult.errorMap("用户名或密码不能为空");
        }
        if (user.getUsername().length() > 12 || user.getUsername().length() < 6 ||
                user.getPassword().length() > 12 || user.getPassword().length() < 6) {
            return MyJSONResult.errorMsg("用户名和密码长度请在6-12之间");
        }
        // 规则
        String regEx = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        // 编译正则表达式
        Pattern pattern = Pattern.compile(regEx);
        // 忽略大小写的写法
        // Pattern pat = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(user.getEmail());
        // 字符串是否与正则表达式相匹配
        boolean rs = matcher.matches();
        if(!rs) return MyJSONResult.errorMsg("邮箱格式不正确");

        boolean userIsExist = userService.queryUserNameIsExist(user.getUsername());
        Users userResult = null;
        if (userIsExist) {
            return MyJSONResult.errorMsg("用户名已存在");
        }else{
            // 注册
            userResult = userService.saveUser(user);
        }
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(userResult,usersVO);
        return MyJSONResult.ok(usersVO);
    }

    /**
    * @Description: 上传头像
    * @date: 2021/1/27 21:35
    */
    @PostMapping("/uploadFaceBase64")
    public MyJSONResult uploadFaceBase64(@RequestBody UsersBO usersBO) throws Exception {
        if (StringUtils.isBlank(usersBO.getFaceData()) || StringUtils.isBlank(usersBO.getUserId())) {
            return MyJSONResult.errorMap("上传数据不正确");
        }
        // 前端传过来的base64转换为文件后上传
        String base64Face = usersBO.getFaceData();
        String userFacePath = usersBO.getUserId() + "userface.png";
        FileUtils.base64ToFile(userFacePath, base64Face);

        MultipartFile multipartFile = FileUtils.fileToMultipart(userFacePath);
        String path = fastDFSClient.uploadBase64(multipartFile);
        System.out.println(path);

        //获取缩略图
        String tmp = "_80x80.";
        String[] arr = path.split("\\.");
        String imageUrl = arr[0] + tmp + arr[1];
        System.out.println(imageUrl);

        // 更新用户头像
        Users user = new Users();
        user.setId(usersBO.getUserId());
        user.setFaceImage(imageUrl);
        user.setFaceImageBig(path);

        Users result = userService.updateUserInfo(user);

        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(result,usersVO);
        return MyJSONResult.ok(usersVO);
    }

    /**
    * @Description: 设置昵称
    * @date: 2021/1/28 10:32
    */
    @PostMapping("/setNickName")
    public MyJSONResult setNickName(@RequestBody UsersBO usersBO) throws Exception{
        if ( StringUtils.isBlank(usersBO.getNickname()) ) {
            return MyJSONResult.errorMap("上传数据不正确");
        }
        if (usersBO.getNickname().length() > 12 || usersBO.getNickname().length() < 2) {
            return MyJSONResult.errorMsg("长度请在2-12之间");
        }
        // 更新昵称
        Users user = new Users();
        user.setId(usersBO.getUserId());
        user.setNickname(usersBO.getNickname());

        Users result = userService.updateUserInfo(user);
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(result,usersVO);
        return MyJSONResult.ok(usersVO);
    }

    /**
    * @Description: 账号匹配查询,非模糊查询
    * @date: 2021/1/28 12:01
    */
    @GetMapping("/search")
    public MyJSONResult searchUser(@RequestParam String myUserId, @RequestParam String friendUserName) {
        if (StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUserName)) {
            return MyJSONResult.errorMsg("数据不正确");
        }

        // 前置条件判断,是否可添加给好友
        Integer status = userService.preconditionSearchFriends(myUserId, friendUserName);
        if(status.equals(SearchFriendsStatusEnum.SUCCESS.status)){
            Users friend = userService.queryByUserName(friendUserName);
            UsersVO usersVO = new UsersVO();
            BeanUtils.copyProperties(friend,usersVO);
            return MyJSONResult.ok(usersVO);
        }else{
            String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            return MyJSONResult.errorMsg(errorMsg);
        }

    }

    /**
    * @Description: 发送好友申请
    * @date: 2021/1/28 20:34
    */
    @GetMapping("/addFriendRequest")
    public MyJSONResult addFriendRequest(@RequestParam String myUserId, @RequestParam String friendUserName) {
        if (StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUserName)) {
            return MyJSONResult.errorMsg("数据不正确");
        }

        // 前置条件判断,是否可添加给好友
        Integer status = userService.preconditionSearchFriends(myUserId, friendUserName);
        if(status.equals(SearchFriendsStatusEnum.SUCCESS.status)){
            userService.sendFriendRequest(myUserId, friendUserName);
        }else{
            String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            return MyJSONResult.errorMsg(errorMsg);
        }
        return MyJSONResult.ok("发送成功");
    }

    /**
    * @Description: 查询添加好友请求
    * @date: 2021/1/28 21:05
    */
    @PostMapping("/queryFriendRequest")
    public MyJSONResult queryFriendRequest(@RequestParam String userId) {
        if (StringUtils.isBlank(userId)) {
            return MyJSONResult.errorMsg("数据不正确");
        }
        // 查询用户接受到的未处理的朋友申请
        List<FriendRequestVO> friendRequestList = userService.queryFriendRequestList(userId);
        friendRequestList.remove(null);
        return MyJSONResult.ok(friendRequestList);
    }

    /**
     * @Description: 操作添加好友请求
     * @date: 2021/1/28 21:05
     */
    @PostMapping("/operFriendRequest")
    public MyJSONResult operFriendRequest(@RequestParam String acceptUserId,@RequestParam String sendUserId,@RequestParam Integer operType) {
        if (StringUtils.isBlank(acceptUserId) || StringUtils.isBlank(sendUserId) || operType == null) {
            return MyJSONResult.errorMsg("数据不正确");
        }
        // 操作类型不合法
        if(StringUtils.isBlank(OperatorFriendRequestTypeEnum.getMsgByType(operType))){
            return MyJSONResult.errorMsg("数据不正确");
        }

        if(operType.equals(OperatorFriendRequestTypeEnum.IGNORE.type)){
            // 忽略好友请求,更新数据库
            userService.updateFriendRequest(sendUserId,acceptUserId);
            return MyJSONResult.ok("忽略请求");
        }else if(operType.equals(OperatorFriendRequestTypeEnum.PASS.type)){
            // 判断通过好友请求,互相增加好友到数据库,更新数据库
            userService.passFriendRequest(sendUserId,acceptUserId);
        }
        return MyJSONResult.ok("添加成功");
    }

    /**
    * @Description: 查询好友列表
    * @date: 2021/1/29 17:23
    */
    @PostMapping("/myFriends")
    public MyJSONResult myFriends(@RequestParam String userId) {
        if (StringUtils.isBlank(userId) ) {
            return MyJSONResult.errorMsg("数据不正确");
        }

        List<MyFriendsVO> myFriends = userService.searchUserFriendsList(userId);
        // System.out.println(myFriends);

        return MyJSONResult.ok(myFriends);
    }

    /**
    * @Description: 获取未签收消息
    * @date: 2021/1/31 18:30
    */
    @PostMapping("getUnReadMsgList")
    public MyJSONResult getUnReadMsgList(@RequestParam String acceptUserId){
        if (StringUtils.isBlank(acceptUserId) ) {
            return MyJSONResult.errorMsg("数据不正确");
        }

        List<ChatMsg> unReadMsgList = userService.getUnReadMsgList(acceptUserId);
        return MyJSONResult.ok(unReadMsgList);
    }

}
