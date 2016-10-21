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
public class ParallaxListView extends ListView {
	/**
	 * getFirstVisiblePosition()包含header
	 * */
	/**视差效果的View，添加在ListView头部*/
	private View mHeader;
	/**header原始高度*/
	private int mHeaderHeight;
	/**按下时Y坐标*/
	private int mDownY;
	/**滑动后最新Y坐标*/
	private int mMoveY;
	/**滑动距离*/
	private int mDistance;
	/**头部可放大倍数，决定可下拉距离，以原始高度为基准*/
	private final float SCALE = 2;
	/**底部可上拉回弹最大距离*/
	private int DISTANCE = 200;
	/**header布局参数，控制放大缩小*/
	private android.widget.RelativeLayout.LayoutParams mLayoutParams;
	/**头尾回弹动画*/
	private ValueAnimator mHeaderAnim, mFooterAnim;
	/**尾部原始paddingbottom*/
	private int mPaddingBottom = 0;

	public ParallaxListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ParallaxListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ParallaxListView(Context context) {
		this(context, null);
	}

	private void init() {
		DisplayMetrics metric = new DisplayMetrics();
		WindowManager wm = (WindowManager) getContext().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metric);
		DISTANCE = (int) (DISTANCE * metric.density);
	}

	/**设置视差效果View，添加在ListView头部*/
	public void setParallaxView(View view) {
		if (view == null)
			return;
		view.measure(0, 0);
		mHeaderHeight = view.getMeasuredHeight();
//		log("mHeaderHeight=" + mHeaderHeight);
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
				// log("onAnimationUpdate.value="+value+"==="+animation);
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
//				 log("FOOTER--onAnimationUpdate.value="+value+"==="+animation);
				setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), value);
			}
		});
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		//第一次记录下原始paddingbottom，不然尾部每次尺寸改变都会记录
//		if(mPaddingBottom==0)
//			mPaddingBottom = getPaddingBottom();
//		log("onLayout::getPaddingBottom"+getPaddingBottom());
	}

	/** 没用 所有回调参数的都是0，只有调用scrollBy和scrollTo才有值 */
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		// log("onScrollChanged(int l, int t, int oldl, int oldt)\nl="+l+" # t="+t+" # oldl="+oldl+" # oldt="+oldt);
		// log("getFirstVisiblePosition："+getFirstVisiblePosition());
		/*
		 * if(t>oldt) {//向上滚动 mIsUp = 1; log("滚动方向："+"上--↑"); }else if(t<oldt) {
		 * mIsUp = 0; log("滚动方向："+"下---↓"); }
		 */
		super.onScrollChanged(l, t, oldl, oldt);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownY = (int) ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			mMoveY = (int) ev.getY();
			mDistance = mMoveY - mDownY;
//			log("ACTION_MOVE::newY="+mMoveY+"  oldY="+mDownY +"  移动距离=" + mDistance);
			if (mDistance < 0) {
//				log("↑--::向上滑动::getLastVisiblePosition="+getLastVisiblePosition() +"   getAdapter().getCount()="+ (getAdapter().getCount()));
				if(getLastVisiblePosition()==getAdapter().getCount()-1) {
					if(getPaddingBottom()<mPaddingBottom+DISTANCE) {
						setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom()-mDistance);
					}
				}
			} else if (mDistance > 0) {
//				log("↓↓--::向下滑动::" + mDistance);
				if (getFirstVisiblePosition() == 0 && mHeader != null) {
					if (mHeader.getHeight() < mHeaderHeight * SCALE) {
						mLayoutParams.height = mHeader.getHeight() + mDistance/2;
						mHeader.setLayoutParams(mLayoutParams);
					}
				}
			}
			mDownY = mMoveY;
			break;
		case MotionEvent.ACTION_UP:
			if (mHeader.getHeight() > mHeaderHeight) {
//				log("放手恢复原始高度");
				mHeaderAnim.setIntValues(mHeader.getHeight(), mHeaderHeight);
				mHeaderAnim.start();
			}
			if(getPaddingBottom()>mPaddingBottom) {
//				log("放手恢复原始高度mPaddingBottom="+mPaddingBottom);
				mFooterAnim.setIntValues(getPaddingBottom(), mPaddingBottom);
				mFooterAnim.start();
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(ev);
	}

	/** 一次touch执行一次，如按下移动抬起只在按下时调用一次 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
//		log("onInterceptTouchEvent");
		return super.onInterceptTouchEvent(ev);
	}

	/**向上过载，deltaY为过载距离，正数，向下为负数*/
	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX,
			int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
//		log("overScrollBy:deltaY=" + deltaY + ", scrollY=" + scrollY + ", scrollRangeY=" + scrollRangeY
//				+ ", maxOverScrollY=" + maxOverScrollY + ", isTouchEvent" + isTouchEvent);
		return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX,
				maxOverScrollY, isTouchEvent);
	}

	private void log(String msg) {
		Log.e("ParallaxListView", msg == null ? "null" : msg);
	}
}
