package com.example.gesturepath;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class GesturePathView extends View {

	private Vibrator vibrator;

	private Paint paintNormal;// 初始化的点
	private Paint paintOnTouch;// 被触摸
	private Paint paintInnerCycle;// 内部圆圈
	private Paint paintLines;// 线条
	private Paint paintInnerLarger;// 刚连接到的点 需要变大

	private Cycle[] cycles;// point的集合
	private Path linePath = new Path();// 连接线路
	private List<Integer> linedCycles = new ArrayList<Integer>();// 已经连接的点集合
	private OnGestureFinishListener onGestureFinishListener;// 回调结束方法
	private String key = "123456";// 连接路径的答案
	private int eventX, eventY;// 当前的坐标
	private boolean canContinue = true;// 是否能继续连接标识
	private boolean result;// 返回结果
	private Timer timer;// 清空
	private int OUT_CYCLE_NORMAL = Color.rgb(108, 119, 138); // 正常外圆颜色
	private int OUT_CYCLE_ONTOUCH = Color.rgb(025, 066, 103); // 选中外圆颜色
	private int INNER_CYCLE_ONTOUCH = Color.rgb(002, 210, 255); // 选择内圆颜色
	private int INNER_LARGER_ONTOUCH = Color.rgb(002, 010, 100); // 选择内圆颜色
	private int LINE_COLOR = Color.argb(127, 002, 210, 255); // 连接线颜色
	private int ERROR_COLOR = Color.argb(127, 255, 000, 000); // 连接错误醒目提示颜色
	private int minCountCycle = 1;
	private boolean isShowPattern = true;
	Bitmap bitmap;

	public interface OnGestureFinishListener {
		public void OnGestureFinish(boolean success);
	}

	public GesturePathView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initPaint();
	}

	public GesturePathView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPaint();
	}

	public GesturePathView(Context context) {
		super(context);
		initPaint();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		int perSize = 0;
		if (cycles == null && (perSize = getWidth() / 6) > 0) {
			cycles = new Cycle[9];
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					Cycle cycle = new Cycle();
					cycle.setNum(i * 3 + j);
					cycle.setOx(perSize * (j * 2 + 1));
					cycle.setOy(perSize * (i * 2 + 1));
					cycle.setR(perSize * 0.5f);
					cycle.setDrawR(perSize * 0.4f);
					cycles[i * 3 + j] = cycle;
				}
			}
		}
	}

	public float getJiaodu(int a, int b, int c) {
		double B = Math.acos((c * c + b * b - a * a) / (2.0 * b * c));
		return (float) (B = Math.toDegrees(B));
	}

	/*
	 * 初始化画笔
	 */
	@SuppressLint("NewApi")
	private void initPaint() {
		vibrator = (Vibrator) getContext().getSystemService(
				Context.VIBRATOR_SERVICE);
		bitmap = BitmapFactory.decodeResource(getContext().getResources(),
				R.drawable.point);
		paintNormal = new Paint();
		paintNormal.setAntiAlias(true);
		paintNormal.setStrokeWidth(3);
		paintNormal.setStyle(Paint.Style.STROKE);

		paintOnTouch = new Paint();
		paintOnTouch.setAntiAlias(true);
		paintOnTouch.setStrokeWidth(3);
		paintOnTouch.setStyle(Paint.Style.STROKE);

		paintInnerCycle = new Paint();
		paintInnerCycle.setAntiAlias(true);
		paintInnerCycle.setStyle(Paint.Style.FILL);

		paintInnerLarger = new Paint();
		paintInnerLarger.setAntiAlias(true);
		paintInnerLarger.setStyle(Paint.Style.FILL);

		paintLines = new Paint();
		paintLines.setAntiAlias(true);
		paintLines.setStyle(Paint.Style.STROKE);
		paintLines.setStrokeWidth(10);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			eventX = (int) event.getX();
			eventY = (int) event.getY();
			for (int i = 0; i < cycles.length; i++) {
				if (cycles[i].isSelectIn(eventX, eventY)) {
					cycles[i].setOnTouch(true);
					if (!linedCycles.contains(cycles[i].getNum())) {
						linedCycles.add(cycles[i].getNum());
					}
					if (!cycles[i].isVibrator()) {
						cycles[i].setVibrator(true);
						long[] pattern = { 10, 50 }; // 开启 停止
						vibrator.vibrate(pattern, -1);
					}
					break;
				}
			}

			break;
		case MotionEvent.ACTION_UP:
			canContinue = false;
			if (linedCycles.size() > minCountCycle) {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < linedCycles.size(); i++) {
					sb.append(linedCycles.get(i));
				}

				result = sb.toString().equals(key);
				if (onGestureFinishListener != null) {
					onGestureFinishListener.OnGestureFinish(result);
				}
			} else {
				Toast.makeText(getContext(), "连接数不能小于" + minCountCycle, 1)
						.show();
			}

			timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					// 还原
					eventX = eventY = 0;
					for (int i = 0; i < cycles.length; i++) {
						cycles[i].setOnTouch(false);
						cycles[i].setCanDraw(true);
						cycles[i].setVibrator(false);
						cycles[i].setDrawR((getWidth() / 6) * 0.4f);
					}
					linedCycles.clear();
					linePath.reset();
					canContinue = true;
					postInvalidate();
				}
			}, 500);

			break;
		}

		if (event.getAction() == MotionEvent.ACTION_MOVE
				&& linedCycles.size() > 0) {
			Cycle cycle = cycles[linedCycles.get(linedCycles.size() - 1)];

			int l, r, t, b;
			if (eventX - cycle.getOx() >= 0) {
				l = cycle.getOx();
				r = eventX;
			} else {
				l = eventX;
				r = cycle.getOx();
			}

			if (eventY - cycle.getOy() >= 0) {
				t = cycle.getOy();
				b = eventY;
			} else {
				t = eventY;
				b = cycle.getOy();

			}
			invalidate(new Rect(l, t, r, b));
		} else
			invalidate();
		return true;

	}

	private void drawInnerLarger(Canvas canvans, Cycle cycle, float r) {
		paintInnerLarger.setColor(INNER_LARGER_ONTOUCH);
		canvans.drawCircle(cycle.getOx(), cycle.getOy(), r, paintInnerLarger);
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);

		int length = cycles.length;

		for (int i = 0; i < length; i++) {
			if (!canContinue && !result) {// 结束
				paintOnTouch.setColor(ERROR_COLOR);
				paintInnerCycle.setColor(ERROR_COLOR);
				paintLines.setColor(ERROR_COLOR);
			} else if (cycles[i].isOnTouch()) {// 可连接点
				paintOnTouch.setColor(OUT_CYCLE_ONTOUCH);
				paintInnerCycle.setColor(INNER_CYCLE_ONTOUCH);
				paintLines.setColor(LINE_COLOR);
			} else {// 不可连接点
				paintNormal.setColor(OUT_CYCLE_NORMAL);
				paintInnerCycle.setColor(INNER_CYCLE_ONTOUCH);
				paintLines.setColor(LINE_COLOR);
			}
			canvas.drawCircle(cycles[i].getOx(), cycles[i].getOy(),
					getWidth() / 30, paintInnerCycle);
		}

		for (int i = 0; i < linedCycles.size(); i++) {
			if (cycles[linedCycles.get(i)].isCanDraw()) {
				if (cycles[linedCycles.get(i)].getDrawR() > getWidth() / 30) {
					Cycle cycle = cycles[linedCycles.get(i)];

					cycle.setDrawR(cycles[linedCycles.get(i)].getDrawR() - 2);
					drawInnerLarger(canvas, cycle, cycle.getDrawR());
					Rect rect = new Rect(
							(int) (cycle.getOx() - cycle.getDrawR()),
							(int) (cycle.getOy() - cycle.getDrawR()),
							(int) (cycle.getOx() + cycle.getDrawR()),
							(int) (cycle.getOy() + cycle.getDrawR()));
					invalidate(rect);
				} else {
					cycles[linedCycles.get(i)].setCanDraw(false);
				}
			}
		}
		drawLine(canvas);

	}

	private void drawLine(Canvas canvans) {
		if (!isShowPattern) {
			return;
		}

		linePath.reset();
		if (linedCycles.size() > 0) {
			for (int i = 0; i < linedCycles.size(); i++) {
				int index = linedCycles.get(i);
				float x = cycles[index].getOx();
				float y = cycles[index].getOy();
				if (i == 0) {
					linePath.moveTo(x, y);
				} else {
					linePath.lineTo(x, y);
				}
			}
			canvans.drawPath(linePath, paintLines);
			Cycle lastCycle = cycles[linedCycles.get(linedCycles.size() - 1)];
			linePath.lineTo(lastCycle.getOx(), lastCycle.getOy());
			int marginY = Math.abs(eventY - lastCycle.getOy());
			int marginX = Math.abs(eventX - lastCycle.getOx());
			if (canContinue && !result && !lastCycle.isSelectIn(eventX, eventY)) {
				int length = (int) Math.sqrt(marginX * marginX + marginY
						* marginY);
				float jiaodu = getJiaodu(marginX, marginY, length);
				if (eventX - lastCycle.getOx() <= 0
						&& eventY - lastCycle.getOy() <= 0) {// 第四区
					jiaodu = 180 - jiaodu;
				} else if (eventX - lastCycle.getOx() > 0
						&& eventY - lastCycle.getOy() >= 0) {// 第二区
					jiaodu = -jiaodu;
				} else if (eventX - lastCycle.getOx() > 0
						&& eventY - lastCycle.getOy() < 0) {// 第一区
					jiaodu = 180 + jiaodu;
				}
				if (length < 5) {
					length = 5;
				}
				canvans.rotate(jiaodu, lastCycle.getOx(), lastCycle.getOy());
				canvans.drawBitmap(zoomImage(bitmap, 10, length),
						lastCycle.getOx() - 5, lastCycle.getOy(),
						paintInnerLarger);
			}
			// if (canContinue && !result) {
			// linePath.lineTo(cycles[linedCycles.get(linedCycles.size() -
			// 1)].getOx(),
			// cycles[linedCycles.get(linedCycles.size() - 1)].getOy());
			//
			// } else {
			//
			// linePath.lineTo(
			// cycles[linedCycles.get(linedCycles.size() - 1)].getOx(),
			// cycles[linedCycles.get(linedCycles.size() - 1)].getOy());
			// }
		}
	}

	private void drawInnerBlueCycle(Cycle cycle, Canvas canvans) {
		canvans.drawCircle(cycle.getOx(), cycle.getOy(), cycle.getR(),
				paintInnerCycle);
	}

	public OnGestureFinishListener getOnGestureFinishListener() {
		return onGestureFinishListener;
	}

	public void setOnGestureFinishListener(
			OnGestureFinishListener onGestureFinishListener) {
		this.onGestureFinishListener = onGestureFinishListener;
	}

	@Override
	protected void onDetachedFromWindow() {
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}

		super.onDetachedFromWindow();
	}

	public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
			double newHeight) {
		// 获取这个图片的宽和高
		float width = bgimage.getWidth();
		float height = bgimage.getHeight();
		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();
		// 计算宽高缩放率
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 缩放图片动作
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
				(int) height, matrix, true);
		// if (!bitmap.equals(bgimage)) {
		// bgimage.recycle();
		// }
		return bitmap;
	}
}
