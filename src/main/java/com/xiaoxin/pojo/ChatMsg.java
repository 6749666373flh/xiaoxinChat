package com.xiaoxin.pojo;


import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @Database: chat_msg
 */

@Entity
@Table(name ="chat_msg")
public class ChatMsg implements Serializable{

	private static final Long serialVersionUID = -7979890153806663916L;

  	@Id
	@Column(name = "id")
	private String id;

  	@Column(name = "send_user_id")
	private String sendUserId;

  	@Column(name = "accept_user_id")
	private String acceptUserId;

  	@Column(name = "msg")
	private String msg;

  	@Column(name = "sign_flag")
	private Integer signFlag;

  	@Column(name = "create_time")
	private java.util.Date createTime;

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

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Integer getSignFlag() {
		return signFlag;
	}

	public void setSignFlag(Integer signFlag) {
		this.signFlag = signFlag;
	}

	public java.util.Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(java.util.Date createTime) {
		this.createTime = createTime;
	}

}
