package com.example ;

import java.io.Serializable;

public class User implements Serializable{
	/**
	 * u
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String faceboookId;
	private String accessToken;
	private String deviceToken;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFaceboookId() {
		return faceboookId;
	}
	public void setFaceboookId(String faceboookId) {
		this.faceboookId = faceboookId;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getDeviceToken() {
		return deviceToken;
	}
	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}
	@Override
	public int hashCode() {
		return 1;
	}
	
	@Override
	public boolean equals(Object arg0) {
		User user= (User)arg0;
		return user.getFaceboookId().equals(this.faceboookId);
	}
	@Override
	public String toString() {
		return "User [name=" + name + ", faceboookId=" + faceboookId
				+ ", accessToken=" + accessToken + ", deviceToken="
				+ deviceToken + "]";
	}
	
}
