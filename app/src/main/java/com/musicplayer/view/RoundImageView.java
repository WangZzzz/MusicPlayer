package com.musicplayer.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 圆形ImageView，可设置最多两个宽度不同且颜色不同的圆形边框。
 * 
 * @author Alan
 */
public class RoundImageView extends ImageView {
	
	/** 边框宽度*/
	private int mBorderThickness = 30;
	/** 边框的差距*/
	private int gap= 2;
	
	// 控件默认长、宽
	private int defaultWidth = 0;
	private int defaultHeight = 0;

	public RoundImageView(Context context) {
		super(context);
	}

	public RoundImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Drawable drawable = getDrawable();
		if (drawable == null) {
			return;
		}

		if (getWidth() == 0 || getHeight() == 0) {
			return;
		}
		this.measure(0, 0);
		if (drawable.getClass() == NinePatchDrawable.class)
			return;
		
		Bitmap b = ((BitmapDrawable) drawable).getBitmap();
		Bitmap bitmap = b.copy(Config.ARGB_8888, true);
		if (defaultWidth == 0) {
			defaultWidth = getWidth();

		}
		if (defaultHeight == 0) {
			defaultHeight = getHeight();
		}
		int radius = 0;
		
		radius = (defaultWidth < defaultHeight ? defaultWidth
				: defaultHeight) / 2 - mBorderThickness - mBorderThickness+gap;
		// 画内圆
		drawCircleBorder(canvas, radius + (mBorderThickness-gap) / 2 + 2,
				1);
		// 画外圆
		drawCircleBorder(canvas, radius + mBorderThickness/2
				+ (mBorderThickness-gap), 2);
			
		Bitmap roundBitmap = getCroppedRoundBitmap(bitmap, radius+4);
		canvas.drawBitmap(roundBitmap, defaultWidth / 2 - radius-4, defaultHeight
				/ 2 - radius-4, null);
	}

	/**
	 * 获取裁剪后的圆形图片
	 * 
	 * @param radius
	 *            半径
	 */
	public Bitmap getCroppedRoundBitmap(Bitmap bmp, int radius) {
		Bitmap scaledSrcBmp;
		int diameter = radius * 2;

		// 为了防止宽高不相等，造成圆形图片变形，因此截取长方形中处于中间位置最大的正方形图片
		int bmpWidth = bmp.getWidth();
		int bmpHeight = bmp.getHeight();
		int squareWidth = 0, squareHeight = 0;
		int x = 0, y = 0;
		Bitmap squareBitmap;
		if (bmpHeight > bmpWidth) {// 高大于宽
			squareWidth = squareHeight = bmpWidth;
			x = 0;
			y = (bmpHeight - bmpWidth) / 2;
			// 截取正方形图片
			squareBitmap = Bitmap.createBitmap(bmp, x, y, squareWidth,
					squareHeight);
		} else if (bmpHeight < bmpWidth) {// 宽大于高
			squareWidth = squareHeight = bmpHeight;
			x = (bmpWidth - bmpHeight) / 2;
			y = 0;
			squareBitmap = Bitmap.createBitmap(bmp, x, y, squareWidth,
					squareHeight);
		} else {
			squareBitmap = bmp;
		}

		if (squareBitmap.getWidth() != diameter
				|| squareBitmap.getHeight() != diameter) {
			scaledSrcBmp = Bitmap.createScaledBitmap(squareBitmap, diameter,
					diameter, true);

		} else {
			scaledSrcBmp = squareBitmap;
		}
		Bitmap output = Bitmap.createBitmap(scaledSrcBmp.getWidth(),
				scaledSrcBmp.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		Paint paint = new Paint();
		Rect rect = new Rect(0, 0, scaledSrcBmp.getWidth(),
				scaledSrcBmp.getHeight());

		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		canvas.drawARGB(0, 0, 0, 0);
		canvas.drawCircle(scaledSrcBmp.getWidth() / 2,
				scaledSrcBmp.getHeight() / 2, scaledSrcBmp.getWidth() / 2,
				paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(scaledSrcBmp, rect, rect, paint);
		// 回收bitmap
		bmp = null;
		squareBitmap = null;
		scaledSrcBmp = null;
		return output;
	}

	/**
	 * 边缘画圆
	 * position 1 内框 2 外框
	 */
	private void drawCircleBorder(Canvas canvas, int radius,int position) {
		Paint paint = new Paint();
		/* 去锯齿 */
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		/* 设置paint的　style　为STROKE：空心 */
		paint.setStyle(Paint.Style.STROKE);
		if(position == 2){
			/* 设置paint的外框宽度 */
			paint.setARGB(255, 33, 33, 33);
			paint.setStrokeWidth(mBorderThickness);
		}else if (position == 1) {
			paint.setARGB(255, 38, 38, 38);
			paint.setStrokeWidth(mBorderThickness-gap);
		}
		canvas.drawCircle(defaultWidth / 2, defaultHeight / 2, radius, paint);
	}

}
