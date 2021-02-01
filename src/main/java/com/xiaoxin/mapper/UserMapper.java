package com.xiaoxin.mapper;

import com.xiaoxin.pojo.FriendsRequest;
import com.xiaoxin.pojo.Users;
import com.xiaoxin.pojo.vo.FriendRequestVO;
import com.xiaoxin.pojo.vo.MyFriendsVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.List;

@Repository
public interface UserMapper extends Mapper<Users> {

    @Select("SELECT   \n" +
            "\t   u.nickname AS sendNickname,   \n" +
            "\t   u.face_image AS sendFaceImage,   \n" +
            "\t   u.username AS sendUsername,   \n" +
            "\t   u.id AS sendUserId    \n" +
            "FROM   \n" +
            "\t   `friends_request` fr   \n" +
            "\t   LEFT JOIN users u ON fr.send_user_id = u.id    \n" +
            "\t   AND fr.accept_user_id = #{acceptUserId} " +
            "     AND fr.status = 0  ")
    //@ResultType(FriendsRequest.class)
    public List<FriendRequestVO> queryFriendRequestList(@Param("acceptUserId")String acceptUserId);

    @Select("SELECT\n" +
            "\t     u.id AS friendUserId,               \n" +
            "\t     u.username AS friendUsername,       \n" +
            "\t     u.face_image AS friendFaceImage,    \n" +
            "\t     u.nickname AS friendNickname        \n" +
            "       FROM                                \n" +
            "\t     users u                             \n" +
            "       WHERE                               \n" +
            "\t     id IN (                             \n" +
            "\t     SELECT                              \n" +
            "\t\t   my_friend_user_id                   \n" +
            "\t     FROM                                \n" +
            "\t\t   `my_friends`                        \n" +
            "\t     WHERE                               \n" +
            "\t     my_user_id = #{userId}              \n" +
            "\t)")
    public List<MyFriendsVO> searchUserFriendsList(@Param("userId") String userId);

}