package com.rong.music;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rong.jazzylistview.JazzyHelper;
import com.rong.jazzylistview.JazzyListView;
import com.rong.jazzyviewpager.JazzyViewPager;
import com.rong.jazzyviewpager.JazzyViewPager.TransitionEffect;
import com.rong.service.MusicService;
import com.rong.util.Blur;
import com.rong.util.BlurImageView;
import com.rong.util.ImageUtils;
import com.rong.util.VisualizerView;

import android.R.color;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("InflateParams")
public class PlayListActivity extends Activity {
	private PlayListAdapter playListAdapter;
	private PlayLrcAdapter playLrcAdapter;
	private static MyPagerAdapter viewpagerAdapter;
	private int clockswitch = 0;
	private int playmodel = 1;
	private int SongCount, clik_count = 0;
	private static MediaPlayer mediaPlayer; // 声明MediaPlayer对象
	// private ListView lrc_listview;
	private JazzyListView song_listview, lrc_listview;
	// private ListView song_listview;

	private ImageButton play_playprevious, play_playnext, play_playmodel, setting_clock;
	private TextView song_name, play_current_time, play_song_total_time, song_info;
	private int currentItem = 0; // 当前播放歌曲的索引
	private ImageButton play_playbutton;
	private SeekBar play_progress;
	private JazzyViewPager viewpager;
	private View paly_album;
	private ImageView page_now, page_next, page_last, page_laster;
	private BlurImageView blurImageView;
	private ImageView song_album;

	private ArrayList<Object> viewList = new ArrayList<Object>();
	private ArrayList<HashMap<String, Object>> songinfo = new ArrayList<HashMap<String, Object>>();
	public static ArrayList<HashMap<String, String>> lrc_maps = new ArrayList<HashMap<String, String>>();

	private int screenHeiht = 0;
	private int albums[] = { R.drawable.album_10, R.drawable.album_2, R.drawable.album_3, R.drawable.album_4,
			R.drawable.album_5, R.drawable.album_6, R.drawable.album_7, R.drawable.album_8 };;
	private DelayThread dThread = null;
	private boolean ThreadWatcher = true;
	private VisualizerView mVisualizerView;
	private Visualizer mVisualizer;
	private View fxview;
	private int mScrollState = 0;

	private int[] shuffleTransitionEffect = { JazzyHelper.FLY, JazzyHelper.ZIPPER, JazzyHelper.CARDS, JazzyHelper.CURL,
			JazzyHelper.FADE, JazzyHelper.FAN, JazzyHelper.FLIP, JazzyHelper.GROW, JazzyHelper.HELIX,
			JazzyHelper.REVERSE_FLY, JazzyHelper.SLIDE_IN, JazzyHelper.STANDARD, JazzyHelper.TILT, JazzyHelper.TWIRL,
			JazzyHelper.WAVE };
	// private String[] TransitionEffectIntduction = { "FLY", "ZIPPER", "CARDS",
	// "CURL", "FADE", "FAN", "FLIP", "GROW",
	// "HELIX", "REVERSE_FLY", "SLIDE_IN", "STANDARD", "TILT", "TWIRL", "WAVE"
	// };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play_main);

		Intent back_lrc = new Intent(this, MusicService.class);
		startService(back_lrc);

		findViews();
		findMusic();
		if (songinfo != null) {
			if (songinfo.size() != 0) {

				setupJazziness();

				viewListening();
				audioFx();
			}

		} else {
			Toast.makeText(getApplicationContext(), "无歌曲", 0).show();
		}

		NotificationManager notifyManager=(NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
		Notification notify=new Notification();
		notify.icon=R.drawable.music_icon;
		notify.when=System.currentTimeMillis();
		RemoteViews contentView =new RemoteViews(getPackageName(), R.layout.notify);
		notify.contentView=contentView;
		notifyManager.notify(1,notify);
	}

	private void findViews() {
		mediaPlayer = new MediaPlayer(); // 实例化一个MediaPlayer对象
		play_playbutton = (ImageButton) findViewById(R.id.play_playbutton); // 获取“暂停/继续”按钮
		play_playprevious = (ImageButton) findViewById(R.id.play_playprevious); // 获取“上一首”按钮
		play_playnext = (ImageButton) findViewById(R.id.play_playnext); // 获取“下一首”按钮
		setting_clock = (ImageButton) findViewById(R.id.clock_set);// 闹钟设置
		play_playmodel = (ImageButton) findViewById(R.id.play_playmodel);// 播放模式

		song_name = (TextView) findViewById(R.id.song_name);
		play_progress = (SeekBar) findViewById(R.id.play_progress);
		play_song_total_time = (TextView) findViewById(R.id.play_song_total_time);
		play_current_time = (TextView) findViewById(R.id.play_current_time);
		page_last = (ImageView) findViewById(R.id.page_last);
		page_next = (ImageView) findViewById(R.id.page_next);
		page_now = (ImageView) findViewById(R.id.page_now);
		page_laster = (ImageView) findViewById(R.id.page_laster);
		song_info = (TextView) findViewById(R.id.song_info);
		blurImageView = (BlurImageView) findViewById(R.id.BlurImageView);
		screenHeiht = ImageUtils.getScreenHeight(this);

		LayoutInflater inflater = LayoutInflater.from(this);
		View songlist = inflater.inflate(R.layout.playlist_listview, null);
		View lrclist = inflater.inflate(R.layout.playlrc_listview, null);
		paly_album = inflater.inflate(R.layout.play_album, null);

		fxview = inflater.inflate(R.layout.playfx_view, null);
		song_listview = (JazzyListView) songlist.findViewById(R.id.song_list);
		lrc_listview = (JazzyListView) lrclist.findViewById(R.id.lrc_listview);
		song_album = (ImageView) paly_album.findViewById(R.id.song_album);
		mVisualizerView = (VisualizerView) fxview.findViewById(R.id.mVisualizerView);
		viewList.clear();
		viewList.add(song_listview);
		viewList.add(lrc_listview);
		viewList.add(paly_album);
		viewList.add(fxview);

		viewpager = (JazzyViewPager) findViewById(R.id.view_pager);

		int viewpagereffectnum = new Random().nextInt(mViewPagerEffects.length);
		viewpager.setTransitionEffect(mViewPagerEffects[viewpagereffectnum]);
		// viewpager.setTransitionEffect(TransitionEffect.Accordion);
		viewpagerAdapter = new MyPagerAdapter(this);
		viewpager.setAdapter(viewpagerAdapter);
		viewpager.setCurrentItem(400);

		// viewpager.setAdapter(new MyPagerAdapter(viewList));

		// viewpager.setOffscreenPageLimit(1);
		// viewpager.setTransitionEffect(effect); // 设置页面之间的切换效果
		// viewpager.setPageTransformer(false, new ParallaxPagerTransformer(
		// R.id.parallaxContent)); // 设置页面上的切换效果
		// viewpager.setAdapter(new MyPagerAdapter(
		// getSupportFragmentManager(), fragments));

		playListAdapter = new PlayListAdapter(this);
		song_listview.setAdapter(playListAdapter);

		playLrcAdapter = new PlayLrcAdapter(this);
		lrc_listview.setAdapter(playLrcAdapter);

		play_playbutton.setImageResource(R.drawable.button_play);
		song_name.setText("我的音乐");
		song_info.setText("JR");
		int rand = albums[new Random().nextInt(8)];
		song_album.setImageResource(rand);
		setBlur(null, rand);
		play_song_total_time.setText("00:00");
		play_current_time.setText("00:00");
		

	}

	public class MyPagerAdapter extends PagerAdapter {

		private ArrayList<Object> views;
		Context context;

		public MyPagerAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {

			// return views.size();
			return 800;
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			// if (view instanceof OutlineContainer) {
			// return ((OutlineContainer) view).getChildAt(0) == obj;
			// } else {
			// return view == obj;
			// }
			return view == obj;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {

			((ViewPager) container).removeView((View) viewList.get(position % viewList.size()));

		}

		@Override
		public Object instantiateItem(View container, int position) {

			((ViewPager) container).addView((View) viewList.get(position % viewList.size()));
			viewpager.setObjectForPosition(viewList.get(position % viewList.size()), position);
			return viewList.get(position % viewList.size());

		}
	}

	private void setupJazziness() {
		int songeffectnum = new Random().nextInt(shuffleTransitionEffect.length);
		int lrceffectnum = new Random().nextInt(shuffleTransitionEffect.length);
		song_listview.setTransitionEffect(shuffleTransitionEffect[songeffectnum]);
		lrc_listview.setTransitionEffect(shuffleTransitionEffect[lrceffectnum]);
	}

	ArrayList<HashMap<String, Object>> lrc_info = new ArrayList<HashMap<String, Object>>();

	public void FindLrc(String songname) throws IOException {
		lrc_info.clear();
		if (lrc_maps != null) {
			for (int i = 0; i < lrc_maps.size(); i++) {
				if (lrc_maps.get(i).get("lrc").toString().contains(songname)) {
					File file = new File(lrc_maps.get(i).get("path").toString());
					try {
						BufferedInputStream bin = new BufferedInputStream(new FileInputStream(file));
						// bin.mark(4);
						// int p = (bin.read() << 8) + bin.read();

						byte[] buf = new byte[4];

						// byte[] frist3byte= bin.read(3);
						int nread = bin.read(buf);
						String code = null;
						code = "GBK";
						if (nread > 3) {
							
							int b1 = buf[0] & 0xFF;
							int b2 = buf[1] & 0xFF;
							int b3 = buf[2] & 0xFF;
							int b4 = buf[3] & 0xFF;
							if (b1 == 0xEF) {
								code = "utf-8";
							}
						}
						
						// switch (p) {
						// case 0xefbb:
						// code = "UTF-8";
						// break;
						// case 0xfffe:
						// code = "Unicode";
						// break;
						// case 0xfeff:
						// code = "UTF-16BE";
						// break;
						// default:
						// code = "GBK";
						// }
						bin.close();
						InputStream ins = new FileInputStream(file);
						InputStreamReader inr = new InputStreamReader(ins, code);
						BufferedReader reader = new BufferedReader(inr);
						String line = null;
						while ((line = reader.readLine()) != null) {
							parserLine(line);
						}
						reader.close();
						inr.close();
						ins.close();

					} catch (FileNotFoundException e) {

						e.printStackTrace();
					}

				}
			}
		}

	}

	private void parserLine(String str) {
		HashMap<String, Object> map_lrc_info = new HashMap<String, Object>();
		str.trim();
		long currentTime = 0;
		String sentence = "";
		String reg = "\\[(\\d{2}:\\d{2}\\.\\d{2})\\]";
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(str);
		// 如果存在匹配项，则执行以下操作
		while (matcher.find()) {
			// 得到匹配的所有内容
			String msg = matcher.group();
			// 得到这个匹配项开始的索引
			int start = matcher.start();
			// 得到这个匹配项结束的索引
			int end = matcher.end();

			// 得到这个匹配项中的组数
			int groupCount = matcher.groupCount();
			// 得到每个组中内容
			for (int i = 0; i <= groupCount; i++) {
				String timeStr = matcher.group(i);
				if (i == 1) {
					// 将第二组中的内容设置为当前的一个时间点
					currentTime = strToLong(timeStr);
				}
			}
			// 得到时间点后的内容
			String[] content = pattern.split(str);
			// 输出数组内容
			for (int i = 0; i < content.length; i++) {
				if (i == content.length - 1) {
					// 将内容设置为当前内容
					sentence = content[i];
				}
			}
			// 设置时间点和内容的映射
			map_lrc_info.put("sentence", sentence);
			map_lrc_info.put("time", currentTime);
			lrc_info.add(map_lrc_info);

		}

	}

	private long strToLong(String timeStr) {
		// 因为给如的字符串的时间格式为XX:XX.XX,返回的long要求是以毫秒为单位
		// 1:使用：分割 2：使用.分割
		String[] s = timeStr.split(":");
		int min = Integer.parseInt(s[0]);
		String[] ss = s[1].split("\\.");
		int sec = Integer.parseInt(ss[0]);
		int mill = Integer.parseInt(ss[1]);
		return min * 60 * 1000 + sec * 1000 + mill * 10;
	}

	/**
	 * 播放控制
	 */
	// 播放音乐
	public void playMusic(String path) {
		// findViews();
		try {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.stop(); // 停止当前音频的播放
			}
			mediaPlayer.reset(); // 重置MediaPlayer
			mediaPlayer.setDataSource(path); // 指定要播放的音频文件
			mediaPlayer.prepare(); // 预加载音频文件
			mediaPlayer.start();

			if (dThread == null) {
				dThread = new DelayThread();
				dThread.start();
			}
			setupJazziness();

			playListAdapter.setSelectItem(currentItem);
			playListAdapter.notifyDataSetInvalidated();
			play_playbutton.setImageResource(R.drawable.button_pause);
			play_song_total_time.setText(formatTime(mediaPlayer.getDuration()));
			song_name.setText(songinfo.get(currentItem).get("title").toString());
			song_info.setText(songinfo.get(currentItem).get("artist").toString() + "|专辑:"
					+ songinfo.get(currentItem).get("album").toString());
			getArtWork(songinfo.get(currentItem).get("albumId").toString());// 专辑封面
			FindLrc(songinfo.get(currentItem).get("title").toString());
		
			playLrcAdapter.setLyric(lrc_info);
			playLrcAdapter.notifyDataSetInvalidated();
			// int viewpagereffectnum = new
			// Random().nextInt(mViewPagerEffects.length);
			// //
			// viewpager.setTransitionEffect(mViewPagerEffects[viewpagereffectnum]);
			// viewpager.setTransitionEffect();
			// setupViewPagerJazziness();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setupViewPagerJazziness() {
		viewpager = (JazzyViewPager) this.findViewById(R.id.view_pager);
		int viewpagereffectnum = new Random().nextInt(mViewPagerEffects.length);
		viewpager.setTransitionEffect(mViewPagerEffects[viewpagereffectnum]);
		// viewpager.setTransitionEffect(effect);
		// viewpager.setAdapter(new MyPagerAdapter(this));
		viewpagerAdapter.notifyDataSetChanged();

	}

	private TransitionEffect[] mViewPagerEffects = { TransitionEffect.Standard, TransitionEffect.Tablet,
			TransitionEffect.CubeOut, TransitionEffect.FlipVertical, TransitionEffect.Accordion };

	public class PlayLrcAdapter extends BaseAdapter {

		private ArrayList<HashMap<String, Object>> mLyricSentences;
		private Context mContext;
		private LayoutInflater mInflater;

		public PlayLrcAdapter(Context context) {
			this.mContext = context;
			this.mInflater = LayoutInflater.from(mContext);

		}

		// 设置歌词，由外部调用，
		public void setLyric(ArrayList<HashMap<String, Object>> map) {
			mLyricSentences = null;
			if (map != null) {
				this.mLyricSentences = map;
			}
		}

		@Override
		public int getCount() {
			if (mLyricSentences != null) {
				if (mLyricSentences.size() == 0) {
					return 1;
				} else {
					return mLyricSentences.size();
				}
			}
			return 1;
		}

		@Override
		public Object getItem(int position) {

			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.playlrc_items, null);
				holder.lyric_line = (TextView) convertView.findViewById(R.id.lrc_textview);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if (mLyricSentences != null) {
				if (mLyricSentences.size() == 0) {
					holder.lyric_line.setText("没有歌词");
				} else {
					// int randnum =new
					// Random().nextInt(lrc_text_colors.length);
					// holder.lyric_line.setTextColor(lrc_text_colors[randnum]);
					holder.lyric_line.setText(mLyricSentences.get(position).get("sentence").toString());

				}
			} else {
				holder.lyric_line.setText("没有歌词");
			}
			return convertView;
		}

		class ViewHolder {
			TextView lyric_line;
		}
	}

	private int[] lrc_text_colors = { R.color.yellow, R.color.mediumvioletred, R.color.papayawhip, R.color.peachpuff,
			R.color.lavenderblush, R.color.paleturquoise };

	private void audioFx() {
		final int maxCR = Visualizer.getMaxCaptureRate();

		mVisualizer = new Visualizer(mediaPlayer.getAudioSessionId());
		mVisualizer.setCaptureSize(256);
		mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
			public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
				mVisualizerView.updateVisualizer(bytes);
			}

			public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
				mVisualizerView.updateVisualizer(fft);
			}
		}, maxCR, false, true);

		mVisualizer.setEnabled(true);

	}

	private void setBlur(Bitmap bm, int id) {

		if (bm == null) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 2;
			Bitmap image = BitmapFactory.decodeResource(getResources(), id, options);
			Bitmap newImg = Blur.fastblur(PlayListActivity.this, image, 20);
			Bitmap bmpBlurred = Bitmap.createScaledBitmap(newImg, screenHeiht, screenHeiht, false);
			blurImageView.setoriginalImage(bmpBlurred);
		} else {
			Bitmap newImg = Blur.fastblur(PlayListActivity.this, bm, 20);
			Bitmap bmpBlurred = Bitmap.createScaledBitmap(newImg, screenHeiht, screenHeiht, false);
			blurImageView.setoriginalImage(bmpBlurred);
		}

	}

	private String getAlbumArt(String album_id) {
		String mUriAlbums = "content://media/external/audio/albums";
		String[] projection = new String[] { "album_art" };
		Cursor cur = this.getContentResolver().query(Uri.parse(mUriAlbums + "/" + album_id), projection, null, null,
				null);
		String album_art = null;
		if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
			cur.moveToNext();
			album_art = cur.getString(0);
		}
		cur.close();
		cur = null;
		return album_art;
	}

	private int[] anims = { R.anim.translate_alpha_down, R.anim.translate_alpha_up, R.anim.translate_alpha_left,
			R.anim.translate_alpha_right, R.anim.rotate_alpha_l, R.anim.rotate_alpha_r, R.anim.scale_alpha_down,
			R.anim.scale_alpha_up };

	public void getArtWork(String album_id) {
		int num = new Random().nextInt(8);
		int animsnum = new Random().nextInt(anims.length);
		Bitmap bm_album = null;
		String albumArt = getAlbumArt(album_id);
		// anims[animsnum]
		Animation animation = AnimationUtils.loadAnimation(this, anims[animsnum]);
		
		if (albumArt == null) {

			song_album.setImageResource(albums[num]);
			song_album.startAnimation(animation);
			setBlur(bm_album, albums[num]);
		} else {
			bm_album = BitmapFactory.decodeFile(albumArt);
			BitmapDrawable bmpDraw = new BitmapDrawable(bm_album);
			song_album.startAnimation(animation);
			song_album.setImageDrawable(bmpDraw);

			setBlur(bm_album, 0);
		}
	}

	public Handler mHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			play_progress.setMax(mediaPlayer.getDuration());
			play_progress.setProgress(mediaPlayer.getCurrentPosition());
			play_current_time.setText(formatTime(mediaPlayer.getCurrentPosition()));
		
			// if(lrc_info.contamediaPlayer.getCurrentPosition()){

			// }
			// playLrcAdapter.setLyric(lrc_maps);
		}
	};

	public class DelayThread extends Thread {

		public void run() {

			while (ThreadWatcher == true) {
				try {
					sleep(1000);

					mHandle.sendEmptyMessage(0);
					// mHandle.post(mUpdateResults);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

		}
	}

	/**
	 * 
	 * @param time
	 *            时间格式化方法
	 * @return
	 */
	public static String formatTime(long time) {
		String min = time / (1000 * 60) + "";
		String sec = time % (1000 * 60) + "";
		if (min.length() < 2) {
			min = "0" + time / (1000 * 60) + "";
		} else {
			min = time / (1000 * 60) + "";
		}
		if (sec.length() == 4) {
			sec = "0" + (time % (1000 * 60)) + "";
		} else if (sec.length() == 3) {
			sec = "00" + (time % (1000 * 60)) + "";
		} else if (sec.length() == 2) {
			sec = "000" + (time % (1000 * 60)) + "";
		} else if (sec.length() == 1) {
			sec = "0000" + (time % (1000 * 60)) + "";
		}

		return min + ":" + sec.trim().substring(0, 2);

	}

	/**
	 * 界面监听
	 */
	private void viewListening() {
		setting_clock.setOnClickListener(new OnClickListener() {
			private CountDownTimer countDown;

			@Override
			public void onClick(View v) {
				if (clockswitch == 0) {
					countDown = new CountDownTimer(30 * 60 * 1000, 1000) {
						@Override
						public void onTick(long millisUntilFinished) {
						}

						@Override
						public void onFinish() {
							mediaPlayer.pause();
							play_playbutton.setImageResource(R.drawable.button_play);
							clockswitch = 0;
						}
					};
					countDown.start();
					Toast.makeText(PlayListActivity.this, "30分钟后暂停播放", 0).show();
					clockswitch = 1;
				} else {

					countDown.cancel();
					countDown = null;
					Toast.makeText(PlayListActivity.this, "取消定时", 0).show();
					clockswitch = 0;
				}

			}
		});

		viewpager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				switch (arg0 % 4) {
				case 0:
					page_now.setImageDrawable(getResources().getDrawable(R.drawable.page_now));
					page_next.setImageDrawable(getResources().getDrawable(R.drawable.page));
					page_last.setImageDrawable(getResources().getDrawable(R.drawable.page));
					page_laster.setImageDrawable(getResources().getDrawable(R.drawable.page));
					break;
				case 1:
					page_next.setImageDrawable(getResources().getDrawable(R.drawable.page_now));
					page_now.setImageDrawable(getResources().getDrawable(R.drawable.page));
					page_last.setImageDrawable(getResources().getDrawable(R.drawable.page));
					page_laster.setImageDrawable(getResources().getDrawable(R.drawable.page));
					break;
				case 2:
					page_last.setImageDrawable(getResources().getDrawable(R.drawable.page_now));
					page_now.setImageDrawable(getResources().getDrawable(R.drawable.page));
					page_next.setImageDrawable(getResources().getDrawable(R.drawable.page));
					page_laster.setImageDrawable(getResources().getDrawable(R.drawable.page));
					break;
				case 3:
					page_last.setImageDrawable(getResources().getDrawable(R.drawable.page));
					page_now.setImageDrawable(getResources().getDrawable(R.drawable.page));
					page_next.setImageDrawable(getResources().getDrawable(R.drawable.page));
					page_laster.setImageDrawable(getResources().getDrawable(R.drawable.page_now));
					break;
				}

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			
			}
		});
		lrc_listview.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				mScrollState = scrollState;
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			}
		});
		song_listview.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				mScrollState = scrollState;

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			}
		});
		song_listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
				if (mScrollState == 0) {
					currentItem = position;
					playMusic(songinfo.get(currentItem).get("url").toString());// 调用playMusic()方法播放音乐
				}
			}
		});
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				switch (playmodel) {
				case 0:
					break;
				case 1:
					currentItem = new Random().nextInt(SongCount);
					break;
				case 2:
					if (++currentItem >= SongCount) {// 当对currentItem进行+1操作后，如果其值大于等于音频文件的总数
						currentItem = 0;
					}
					break;
				}
				playMusic(songinfo.get(currentItem).get("url").toString());
			}
		});
		play_progress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				play_progress.setMax(mediaPlayer.getDuration());
				mediaPlayer.seekTo(play_progress.getProgress());
			}
		});
		play_playbutton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mScrollState == 0) {
					if (mediaPlayer.isPlaying()) {
						mediaPlayer.pause(); // 停止播放音频
						((ImageButton) v).setImageResource(R.drawable.button_play);

					} else {
						mediaPlayer.start();
						((ImageButton) v).setImageResource(R.drawable.button_pause);
					}
				}

			}
		});
		play_playnext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (mScrollState == 0) {

					switch (playmodel) {
					case 0:

						break;

					case 1:

						currentItem = new Random().nextInt(SongCount);
						break;
					case 2:
						if (++currentItem >= SongCount) {// 当对currentItem进行+1操作后，如果其值大于等于音频文件的总数
							currentItem = 0;
						}
						break;
					}
					// setupJazziness();
					playMusic(songinfo.get(currentItem).get("url").toString());
					clik_count = 0;
				} else {
					clik_count = clik_count + 1;
					if (clik_count == 2) {
						Toast.makeText(getApplicationContext(), "点击过快(⊙_⊙)", 0).show();
					}
					if (clik_count == 3) {
						Toast.makeText(getApplicationContext(), "你能不能有点儿耐心(╰_╯)#", 0).show();
						clik_count = 0;
					}
				}
			}
		}

		);
		play_playprevious.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mScrollState == 0) {
					switch (playmodel) {
					case 0:
						break;
					case 1:
						currentItem = new Random().nextInt(SongCount);
						break;
					case 2:

						if (--currentItem >= 0) { // 当对currentItem进行-1操作后，如果其值大于等于0
							if (currentItem >= SongCount) { // 如果currentItem的值大于等于音频文件的总数
								currentItem = 0;
							}
						} else {
							currentItem = SongCount - 1; // currentItem的值设置为音频文件总数-1
						}
						break;
					}
					playMusic(songinfo.get(currentItem).get("url").toString()); // 调用playMusic()方法播放音乐
				}

			}
		});
		play_playmodel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (playmodel) {
				case 2:// 单曲
					((ImageView) v).setImageResource(R.drawable.button_playmode_repeat_single);
					playmodel = 0;
					break;
				case 0:// 随机
					((ImageView) v).setImageResource(R.drawable.button_playmode_shuffle);
					playmodel = 1;
					break;
				case 1:// 顺序
					((ImageView) v).setImageResource(R.drawable.button_playmode_repeat);
					playmodel = 2;
					break;
				}

			}
		});
	}

	public class PlayListAdapter extends BaseAdapter {

		private Context mContext;
		private LayoutInflater mInflater;

		public PlayListAdapter(Context context) {
			this.mContext = context;
			this.mInflater = LayoutInflater.from(context);

		}

		@Override
		public int getCount() {
			if (songinfo != null) {
				if (songinfo.size() == 0) {
					return 1;
				}
				return songinfo.size();
			}
			return 1;
		}

		@Override
		public Object getItem(int position) {

			return position;
		}

		@Override
		public long getItemId(int position) {

			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.playlist_items, null);
				holder.song = (TextView) convertView.findViewById(R.id.song_of_playlist);
				holder.artist = (TextView) convertView.findViewById(R.id.artist_of_playlist);
				holder.duration = (TextView) convertView.findViewById(R.id.duration_of_playlist);
				holder.album = (TextView) convertView.findViewById(R.id.album_of_playlist);
				holder.stat_image = (ImageView) convertView.findViewById(R.id.stat);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();// 取出ViewHolder对象
			}
			if (songinfo != null) {
				if (songinfo.size() == 0) {
					holder.song.setText("设备无可播放歌曲");

				} else {
					holder.song.setText(songinfo.get(position).get("title").toString());
					holder.artist.setText(songinfo.get(position).get("artist").toString());
					holder.duration.setText(songinfo.get(position).get("duration").toString());
					holder.album.setText(" | " + songinfo.get(position).get("album").toString());
				}

			}

			if (position == selectItem) {
				holder.stat_image.setImageResource(color.white);
			} else {
				holder.stat_image.setImageResource(Color.TRANSPARENT);
			}

			return convertView;
		}

		public void setSelectItem(int selectItem) {
			this.selectItem = selectItem;

		}

		private int selectItem = -1;
	}

	static class ViewHolder {
		TextView song;
		TextView artist;
		TextView duration;
		TextView album;
		ImageView stat_image;

	}

	/**
	 * 歌曲查找
	 */
	// 使用ListView组件显示SD卡上的全部音频文件
	private void findMusic() {
		Cursor cursor = this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		while (cursor.moveToNext()) {
			long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)); // 音乐id
			String title = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))); // 音乐标题
			String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)); // 艺术家
			String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)); // 专辑
			String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
			long albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
			long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)); // 时长
			long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)); // 文件大小
			String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)); // 文件路
			if (!displayName.contains("通话录音") && duration > 60000) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("id", id);
				map.put("title", title);
				map.put("artist", artist);
				map.put("album", album);
				map.put("displayName", displayName);
				map.put("albumId", String.valueOf(albumId));
				map.put("duration", formatTime(duration));
				map.put("size", String.valueOf(size));
				map.put("url", url);
				songinfo.add(map);

			}

		}
		cursor.close();
		SongCount = songinfo.size();
	}

	@Override
	protected void onDestroy() {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.stop(); // 停止音乐的播放
		}
		ThreadWatcher = false;
		if (dThread != null) {

			dThread.interrupt();
			dThread = null;
		}
		mediaPlayer.release(); // 释放资源
		mediaPlayer = null;

		super.onDestroy();
	}

}