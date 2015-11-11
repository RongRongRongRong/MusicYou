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
	private float width; // �����ͼ���
	private float height; // �����ͼ�߶�
	private Paint currentPaint; // ��ǰ���ʶ���
	private Paint notCurrentPaint; // �ǵ�ǰ���ʶ���
	private float textHeight = 30; // �ı��߶�
	private float textSize = 22; // �ı���С
	private int index = 0; // list�����±�
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
		setFocusable(true); // ���ÿɶԽ�

		// ��������
		currentPaint = new Paint();
		currentPaint.setAntiAlias(true); // ���ÿ���ݣ����������۱���
		currentPaint.setTextAlign(Paint.Align.CENTER);// �����ı����뷽ʽ

		// �Ǹ�������
		notCurrentPaint = new Paint();
		notCurrentPaint.setAntiAlias(true);
		notCurrentPaint.setTextAlign(Paint.Align.CENTER);
	}

	/**
	 * �滭���
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
				// ��������֮ǰ�ľ���
				for (int i = index - 1; i >= 0; i--) {
					// ��������
					tempY = tempY - textHeight;
					if (tempY < 0) {
						break;
					}
					canvas.drawText(mLrcList.get(i).getLrc_sentence(), width / 2, tempY, notCurrentPaint);
				}
				tempY = middleY;
				// ��������֮��ľ���
				for (int i = index + 1; i < mLrcList.size(); i++) {
					// ��������
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
	 * ��view��С�ı��ʱ����õķ���
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.width = w * 0.5f;
		this.height = h;
		this.middleY = h * 0.5f;
	}

	public long updateIndex(long time) {
		// ������
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
		// ���ظ�ʳ�����ʱ�䣬�����ʱ����sleep
		return currentDunringTime;
	}
}
