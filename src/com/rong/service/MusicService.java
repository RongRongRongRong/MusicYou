package com.rong.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.rong.music.PlayListActivity;

import android.app.IntentService;
import android.content.Intent;
import android.widget.Toast;

public class MusicService  extends IntentService  {
	public MusicService() {
		super("MusicService");
	
	}
	
	@Override
	public void onCreate() {
//		Toast.makeText(getApplicationContext(), "Prepare for LRC...", 0).show();
		super.onCreate();
	}
	
	

	@Override
	protected void onHandleIntent(Intent intent) {
		 getFiles("/storage/emulated/legacy/");
		  PlayListActivity.lrc_maps=lrc_maps;
		
	}
	@Override
	public void onDestroy() {
//		Toast.makeText(getApplicationContext(), "LRC is ready!", 0).show();
		super.onDestroy();
	}
	
	private void getFiles(String url) {
		File files = new File(url); // 创建文件对象
		File[] file = files.listFiles();
		try {
			for (File f : file) { // 通过for循环遍历获取到的文件数组
				if (f.isDirectory()) { // 如果是目录，也就是文件夹
					getFiles(f.getAbsolutePath()); // 递归调用
				} else {
					if (isAudioFile(f.getPath())) { // 如果是歌词文件
						String temp = f.getName();
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("path", f.getPath());
						map.put("lrc", temp.substring(0, temp.length() - 4));
						lrc_maps.add(map);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(); // 输出异常信息
		}

	}
	private static boolean isAudioFile(String path) {
		for (String format : imageFormatSet) { // 遍历数组
			if (path.contains(format)) {
				return true;
			}
		}
		return false;
	}
	private static String[] imageFormatSet = new String[] { ".lrc" };
	private ArrayList<HashMap<String, String>> lrc_maps = new ArrayList<HashMap<String, String>>();
	
	
}
