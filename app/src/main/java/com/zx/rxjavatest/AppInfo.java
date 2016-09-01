package com.zx.rxjavatest;

import android.content.Intent;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;

public class AppInfo {
	public String appName;
	public String pkgName;
	public Drawable appIcon;
	public String path;
	public String size;
	public String description;
	public Intent intent;
	public boolean isUserUpdate = false;
	public long memSize; // 占用内存
	public boolean Checked = false; // 是否选中
	// 应用大小
	public long codeSize;
	// 数据大小
	public long dataSize;
	// 缓存大小
	public long cacheSize;
	public int bootFlag = 30000;
	
	public ArrayList<String> bootReceivers;
	public ArrayList<String> netChangerReceivers;
	public ArrayList<String> mountReceivers;
	public Boolean bootState = true;
	public Boolean mountState = true;
	public Boolean netState = true;
	
	
	public AppInfo(String appName, String pkgName, Drawable appIcon) {
		super();
		this.appName = appName;
		this.pkgName = pkgName;
		this.appIcon = appIcon;
	}
	
	public AppInfo(Drawable icon, String appName, String description, String packageName, String path, String size) {
		this.appIcon = icon;
		this.appName = appName;
		this.description = description;
		this.pkgName = packageName;
		this.path = path;
		this.size = size;
	}

	public AppInfo() {
		// TODO Auto-generated constructor stub
	}
}
