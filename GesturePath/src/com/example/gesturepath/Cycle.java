package com.example.gesturepath;

public class Cycle {

	private int ox, oy;
	private float r; // 半径长度
	private Integer num; // 代表数值
	private boolean onTouch; // false=未选中
	private boolean canDraw = true, isVibrator = false;
	private float drawR;

	public boolean isVibrator() {
		return isVibrator;
	}

	public void setVibrator(boolean isVibrator) {
		this.isVibrator = isVibrator;
	}

	public int getOx() {
		return ox;
	}

	public void setOx(int ox) {
		this.ox = ox;
	}

	public int getOy() {
		return oy;
	}

	public void setOy(int oy) {
		this.oy = oy;
	}

	public float getR() {
		return r;
	}

	public void setR(float r) {
		this.r = r;
	}

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}

	public boolean isOnTouch() {
		return onTouch;
	}

	public void setOnTouch(boolean onTouch) {
		this.onTouch = onTouch;
	}

	public boolean isSelectIn(int x, int y) {
		double distance = Math.sqrt((x - ox) * (x - ox) + (y - oy) * (y - oy));
		return distance < r;
	}

	public boolean isCanDraw() {
		return canDraw;
	}

	public void setCanDraw(boolean canDraw) {
		this.canDraw = canDraw;
	}

	public float getDrawR() {
		return drawR;
	}

	public void setDrawR(float drawR) {
		this.drawR = drawR;
	}

}
