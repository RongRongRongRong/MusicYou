package com.rong.util;


public class LrcInfo {

	/**
	 * 歌词实体类
	 */
	private long lrc_time;
	private long during_time;
	private String lrc_sentence;
	// 以下为getter() setter()
	

	public long getLrc_time() {
		return lrc_time;
	}

	public void setLrc_time(long lrc_time) {
		this.lrc_time = lrc_time;
	}

	public String getLrc_sentence() {
		return lrc_sentence;
	}

	public void setLrc_sentence(String lrc_sentence) {
		this.lrc_sentence = lrc_sentence;
	}

	public long getDuring_time() {
		return during_time;
	}

	public void setDuring_time(long during_time) {
		this.during_time = during_time;
	}

//
//	public HashMap<Long, String> getInfos() {
//		return infos;
//	}
//
//	public void setInfos(HashMap<Long, String> infos) {
//		this.infos = infos;
//	}

	

}
