package cn.look.parallaxlistview;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ListView;

/**
 * <pre>
 * 视差特效ListView，底部回弹效果
 * 注意：通过setParallaxView添加视差view后，会给ListView添加一个header
 * 		后续使用position时注意-1
 * </pre>
 * */
public class ParallaxListView2 extends ListView {
	/** 视差效果的View，添加在ListView头部 */
	private View mHeader;
	/** header原始高度 */
	private int mHeaderHeight;
	/** 头部可放大倍数，决定可下拉距离，以原始高度为基准 */
	private final float SCALE = 2;
	/** 底部可上拉回弹最大距离 */
	private int DISTANCE = 200;
	/** header布局参数，控制放大缩小 */
	private android.widget.RelativeLayout.LayoutParams mLayoutParams;
	/** 头尾回弹动画 */
	private ValueAnimator mHeaderAnim, mFooterAnim;
	/** 尾部原始paddingbottom */
	private int mPaddingBottom = 0;

	public ParallaxListView2(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ParallaxListView2(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ParallaxListView2(Context context) {
		this(context, null);
	}

	private void init() {
		DisplayMetrics metric = new DisplayMetrics();
		WindowManager wm = (WindowManager) getContext().getApplicationContext()
				.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(metric);
		DISTANCE = (int) (DISTANCE * metric.density);
	}

	/** 设置视差效果View，添加在ListView头部 */
	public void setParallaxView(View view) {
		if (view == null)
			return;
		view.measure(0, 0);
		mHeaderHeight = view.getMeasuredHeight();
		mLayoutParams = new android.widget.RelativeLayout.LayoutParams(
				android.widget.RelativeLayout.LayoutParams.MATCH_PARENT, mHeaderHeight);
		mHeader = view;
		addHeaderView(view);
		mHeaderAnim = ValueAnimator.ofInt(mHeader.getHeight(), mHeaderHeight);
		mHeaderAnim.setDuration(300);
		mHeaderAnim.setInterpolator(new LinearInterpolator());
		mHeaderAnim.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				Integer value = (Integer) animation.getAnimatedValue();
				mLayoutParams.height = value;
				mHeader.setLayoutParams(mLayoutParams);
			}
		});
		mFooterAnim = ValueAnimator.ofInt(getPaddingBottom(), 0);
		mFooterAnim.setDuration(200);
		mFooterAnim.setInterpolator(new LinearInterpolator());
		mFooterAnim.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				Integer value = (Integer) animation.getAnimatedValue();
				setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), value);
			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_UP:
			if (mHeader.getHeight() > mHeaderHeight) {
				mHeaderAnim.setIntValues(mHeader.getHeight(), mHeaderHeight);
				mHeaderAnim.start();
			}
			if (getPaddingBottom() > mPaddingBottom) {
				mFooterAnim.setIntValues(getPaddingBottom(), mPaddingBottom);
				mFooterAnim.start();
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(ev);
	}

	/** deltaY为过载距离，正数底部向上拉，顶部向下拉为负数 */
	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX,
			int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
		if (isTouchEvent) {
			log("OverScrollBy:" + deltaY);
			if (getFirstVisiblePosition() == 0 && mHeader != null) {// 顶部下拉
				if (mHeader.getHeight() < mHeaderHeight * SCALE) {
					mLayoutParams.height = mHeader.getHeight() - deltaY / 2;
					mHeader.setLayoutParams(mLayoutParams);
				}
			} else if (getLastVisiblePosition() == getAdapter().getCount() - 1) {// 底部上拉
				if (getPaddingBottom() < mPaddingBottom + DISTANCE) {
					setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom() + deltaY);
				}
			}
		}
		return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX,
				maxOverScrollY, isTouchEvent);
	}

	private void log(String msg) {
		Log.e("ParallaxListView", msg == null ? "null" : msg);
	}
}
