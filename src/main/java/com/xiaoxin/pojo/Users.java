package com.xiaoxin.pojo;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @Database: users
 */

@Entity
@Table(name ="users")
public class Users implements Serializable{

	private static final Long serialVersionUID = -6652973290808736902L;
	// id,username,password,faceImage,faceImageBig,nickname,qrcode,cid
  	@Id
	@Column(name = "id")
	private String id;

  	@Column(name = "username")
	private String username;

  	@Column(name = "password")
	private String password;

  	@Column(name = "face_image")
	private String faceImage;

  	@Column(name = "face_image_big")
	private String faceImageBig;

  	@Column(name = "nickname")
	private String nickname;

  	@Column(name = "qrcode")
	private String qrcode;

  	@Column(name = "cid")
	private String cid;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFaceImage() {
		return faceImage;
	}

	public void setFaceImage(String faceImage) {
		this.faceImage = faceImage;
	}

	public String getFaceImageBig() {
		return faceImageBig;
	}

	public void setFaceImageBig(String faceImageBig) {
		this.faceImageBig = faceImageBig;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getQrcode() {
		return qrcode;
	}

	public void setQrcode(String qrcode) {
		this.qrcode = qrcode;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	@Override
	public String toString() {
		return "Users{" +
				"id='" + id + '\'' +
				", username='" + username + '\'' +
				", password='" + password + '\'' +
				", faceImage='" + faceImage + '\'' +
				", faceImageBig='" + faceImageBig + '\'' +
				", nickname='" + nickname + '\'' +
				", qrcode='" + qrcode + '\'' +
				", cid='" + cid + '\'' +
				'}';
	}
}
