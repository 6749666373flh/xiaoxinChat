package com.xiaoxin.pojo;


import java.io.Serializable;
import java.util.BitSet;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @Database: friends_request
 */

@Entity
@Table(name ="friends_request")
public class FriendsRequest implements Serializable{

	private static final Long serialVersionUID = 7902292900371322389L;

  	@Id
	@Column(name = "id")
	private String id;

  	@Column(name = "send_user_id")
	private String sendUserId;

  	@Column(name = "accept_user_id")
	private String acceptUserId;

  	@Column(name = "request_data_time")
	private java.util.Date requestDataTime;

  	@Column(name = "status")
  	private int status;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSendUserId() {
		return sendUserId;
	}

	public void setSendUserId(String sendUserId) {
		this.sendUserId = sendUserId;
	}

	public String getAcceptUserId() {
		return acceptUserId;
	}

	public void setAcceptUserId(String acceptUserId) {
		this.acceptUserId = acceptUserId;
	}

	public java.util.Date getRequestDataTime() {
		return requestDataTime;
	}

	public void setRequestDataTime(java.util.Date requestDataTime) {
		this.requestDataTime = requestDataTime;
	}

}
