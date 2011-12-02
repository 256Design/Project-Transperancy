package com.twofivesix.pt.data;


public class TileItem {
	private int imageRef;
	private int labelRef;
	private int notificationCount;
	private Class<?> activityDestination; 
	private int requestCode;

	public TileItem(int imageID, int labelString, Class<?> linkClass, int requestCode) {
		imageRef = imageID;
		labelRef = labelString;
		activityDestination = linkClass;
		this.requestCode = requestCode;
	}
	public int getImageRef() {
		return imageRef;
	}
	public void setImageRef(int imageRef) {
		this.imageRef = imageRef;
	}
	public int getLabelRef() {
		return labelRef;
	}
	public void setLabelRef(int labelRef) {
		this.labelRef = labelRef;
	}
	public int getNotificationCount() {
		return notificationCount;
	}
	public void setNotificationCount(int notificationCount) {
		this.notificationCount = notificationCount;
	}
	public Class<?> getActivityDestination() {
		return activityDestination;
	}
	public void setActivityDestination(Class<?> activityDestination) {
		this.activityDestination = activityDestination;
	}
	public int getRequestCode() {
		return requestCode;
	}
}
