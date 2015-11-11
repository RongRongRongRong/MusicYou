package com.rong.util;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class LrcView extends TextView {
	private float width; // 歌词视图宽度
	private float height; // 歌词视图高度
	private Paint currentPaint; // 当前画笔对象
	private Paint notCurrentPaint; // 非当前画笔对象
	private float textHeight = 30; // 文本高度
	private float textSize = 22; // 文本大小
	private int index = 0; // list集合下标
	long current_time;

	private ArrayList<LrcInfo> mLrcList = new ArrayList<LrcInfo>();
	private float middleY;

	public void setmLrcList(ArrayList<LrcInfo> mLrcList) {
		this.mLrcList = mLrcList;

	}

	public LrcView(Context context) {
		super(context);
		init();
	}

	public LrcView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public LrcView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		setFocusable(true); // 设置可对焦

		// 高亮部分
		currentPaint = new Paint();
		currentPaint.setAntiAlias(true); // 设置抗锯齿，让文字美观饱满
		currentPaint.setTextAlign(Paint.Align.CENTER);// 设置文本对齐方式

		// 非高亮部分
		notCurrentPaint = new Paint();
		notCurrentPaint.setAntiAlias(true);
		notCurrentPaint.setTextAlign(Paint.Align.CENTER);
	}

	/**
	 * 绘画歌词
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		currentPaint.setColor(Color.argb(210, 251, 248, 29));
		notCurrentPaint.setColor(Color.argb(140, 255, 255, 255));

		currentPaint.setTextSize(24);
		currentPaint.setTypeface(Typeface.SERIF);

		notCurrentPaint.setTextSize(textSize);
		notCurrentPaint.setTypeface(Typeface.DEFAULT);
		
		
			try {
				canvas.drawText(mLrcList.get(index).getLrc_sentence(), width / 2, height / 2, currentPaint);

				float tempY = middleY;
				// 画出本句之前的句子
				for (int i = index - 1; i >= 0; i--) {
					// 向上推移
					tempY = tempY - textHeight;
					if (tempY < 0) {
						break;
					}
					canvas.drawText(mLrcList.get(i).getLrc_sentence(), width / 2, tempY, notCurrentPaint);
				}
				tempY = middleY;
				// 画出本句之后的句子
				for (int i = index + 1; i < mLrcList.size(); i++) {
					// 往下推移
					tempY = tempY + textHeight;
					if (tempY > height) {
						break;
					}
					canvas.drawText(mLrcList.get(i).getLrc_sentence(), width / 2, tempY, notCurrentPaint);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	

	/**
	 * 当view大小改变的时候调用的方法
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.width = w * 0.5f;
		this.height = h;
		this.middleY = h * 0.5f;
	}

	public long updateIndex(long time) {
		// 歌词序号
		if(mLrcList!=null){
			index = mLrcList.indexOf(time);
		}
		
		if (index == -1)
			return -1;

		long currentDunringTime;
//		if (index < mLrcList.size() - 1) {
//			currentDunringTime = mLrcList.get(index + 1).getLrc_time() - time;
//		} else {
			currentDunringTime = 100;
//		}
		// 返回歌词持续的时间，在这段时间内sleep
		return currentDunringTime;
	}
}
