package com.kou.picnicapp.model;

public class TargetData {

	public int sequence_id;
	public String number;
	public String name;
	public long lastCheckedTime;
	public String phoneNumber;
	public String uuid;
	public String major;
	public String minor;

	public int getSequence_id() {
		return sequence_id;
	}

	public void setSequence_id(int sequence_id) {
		this.sequence_id = sequence_id;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getLastCheckedTime() {
		return lastCheckedTime;
	}

	public void setLastCheckedTime(long lastCheckedTime) {
		this.lastCheckedTime = lastCheckedTime;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getMajor() {
		return major;
	}

	public void setMajor(String major) {
		this.major = major;
	}

	public String getMinor() {
		return minor;
	}

	public void setMinor(String minor) {
		this.minor = minor;
	}

}
