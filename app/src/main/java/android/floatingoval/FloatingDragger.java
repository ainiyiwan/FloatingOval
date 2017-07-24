package android.floatingoval;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.LayoutRes;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * 可拖拽的"悬浮框"，效果类似iOS悬浮球
 *
 * @author hiphonezhu@gmail.com
 * @version [MRobot-Android, 16/12/26 14:24]
 */
public class FloatingDragger {
    public FloatingDraggedView floatingDraggedView;

    public FloatingDragger(Context context, @LayoutRes int layoutResID) {
        // 用户布局
        View contentView = LayoutInflater.from(context).inflate(layoutResID, null);
        // 悬浮球按钮
        View floatingView = LayoutInflater.from(context).inflate(R.layout.layout_floating_dragged, null);

        // ViewDragHelper的ViewGroup容器
        floatingDraggedView = new FloatingDraggedView(context);
        floatingDraggedView.addView(contentView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        floatingDraggedView.addView(floatingView, new FrameLayout.LayoutParams(dip2px(context, 45), dip2px(context,
                40)));

    }

    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public View getView() {
        return floatingDraggedView;
    }


    public class FloatingDraggedView extends FrameLayout {
        ViewDragHelper dragHelper;
        SharedPreferences sp = getContext().getSharedPreferences("FloatingDraggedView", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        Button floatingBtn;
        private Context mContext;

        public FloatingDraggedView(Context context) {
            super(context);
            mContext = context;
            init();
        }

        public FloatingDraggedView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public FloatingDraggedView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);

            int x = getMeasuredWidth() - floatingBtn.getMeasuredWidth();
            int y = getMeasuredHeight() * 2 / 3;
            floatingBtn.layout((int) x, (int) y, (int) x + floatingBtn.getMeasuredWidth(), (int) y + floatingBtn.getMeasuredHeight());
        }

        void init() {
            dragHelper = ViewDragHelper.create(FloatingDraggedView.this, 1.0f, new ViewDragHelper.Callback() {
                @Override
                public boolean tryCaptureView(View child, int pointerId) {
                    return child == floatingBtn;
                }

                @Override
                public int clampViewPositionVertical(View child, int top, int dy) {
                    if (top > getHeight() - child.getMeasuredHeight()) {
                        top = getHeight() - child.getMeasuredHeight();
                    } else if (top < 0) {
                        top = 0;
                    }
                    return top;
                }

                @Override
                public int clampViewPositionHorizontal(View child, int left, int dx) {
                    if (left > getWidth() - child.getMeasuredWidth()) {
                        left = getWidth() - child.getMeasuredWidth();
                    } else if (left < 0) {
                        left = 0;
                    }
                    return left;
                }

                @Override
                public int getViewVerticalDragRange(View child) {
                    return getMeasuredHeight() - child.getMeasuredHeight();
                }

                @Override
                public int getViewHorizontalDragRange(View child) {
                    return getMeasuredWidth() - child.getMeasuredWidth();
                }

                @Override
                public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                    super.onViewPositionChanged(changedView, left, top, dx, dy);
                }

                @Override
                public void onViewDragStateChanged(int state) {
                    super.onViewDragStateChanged(state);
                    if (state == ViewDragHelper.STATE_SETTLING) { // 拖拽结束，通知观察者
                    }
                }

                @Override
                public void onViewReleased(View releasedChild, float xvel, float yvel) {
                    if (releasedChild == floatingBtn) {
                        float x = floatingBtn.getX();
                        float y = floatingBtn.getY();
                        if (x < (getMeasuredWidth() / 2f - releasedChild.getMeasuredWidth() / 2f)) { // 0-x/2
                            if (x < releasedChild.getMeasuredWidth() / 3f) {
                                x = 0;
                            } else if (y < (releasedChild.getMeasuredHeight() * 3)) { // 0-y/3
                                y = 0;
                            } else if (y > (getMeasuredHeight() - releasedChild.getMeasuredHeight() * 3)) { // 0-(y-y/3)
                                y = getMeasuredHeight() - releasedChild.getMeasuredHeight();
                            } else {
                                x = 0;
                            }
                        } else { // x/2-x
                            if (x > getMeasuredWidth() - releasedChild.getMeasuredWidth() / 3f - releasedChild
                                    .getMeasuredWidth()) {
                                x = getMeasuredWidth() - releasedChild.getMeasuredWidth();
                            } else if (y < (releasedChild.getMeasuredHeight() * 3)) { // 0-y/3
                                y = 0;
                            } else if (y > (getMeasuredHeight() - releasedChild.getMeasuredHeight() * 3)) { // 0-(y-y/3)
                                y = getMeasuredHeight() - releasedChild.getMeasuredHeight();
                            } else {
                                x = getMeasuredWidth() - releasedChild.getMeasuredWidth();
                            }
                        }
                        // 移动到指定位置
                        dragHelper.smoothSlideViewTo(releasedChild, (int) x, (int) y);
                        invalidate();
                    }
                }
            });
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            floatingBtn = (Button) findViewById(R.id.floatingBtn);
            floatingBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getContext().startActivity(new Intent(getContext(), Main2Activity.class));
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return dragHelper.shouldInterceptTouchEvent(ev);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            dragHelper.processTouchEvent(event);
            return true;
        }

        @Override
        public void computeScroll() {
            if (dragHelper.continueSettling(true)) {
                invalidate();
            }
        }
    }

}
