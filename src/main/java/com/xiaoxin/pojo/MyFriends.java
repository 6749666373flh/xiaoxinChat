package com.xiaoxin.pojo;


import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @Database: my_friends
 */

@Entity
@Table(name ="my_friends")
public class MyFriends implements Serializable{

	private static final Long serialVersionUID = 6592488505563330046L;

  	@Id
	@Column(name = "id")
	private String id;

  	@Column(name = "my_user_id")
	private String myUserId;

  	@Column(name = "my_friend_user_id")
	private String myFriendUserId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMyUserId() {
		return myUserId;
	}

	public void setMyUserId(String myUserId) {
		this.myUserId = myUserId;
	}

	public String getMyFriendUserId() {
		return myFriendUserId;
	}

	public void setMyFriendUserId(String myFriendUserId) {
		this.myFriendUserId = myFriendUserId;
	}

	@Override
	public String toString() {
		return "MyFriends{" +
				"id='" + id + '\'' +
				", myUserId='" + myUserId + '\'' +
				", myFriendUserId='" + myFriendUserId + '\'' +
				'}';
	}
}
