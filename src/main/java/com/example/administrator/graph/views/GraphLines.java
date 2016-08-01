package com.example.administrator.graph.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.example.administrator.graph.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/5/25.
 */
public class GraphLines extends View {

//    <attr name="title_textSize" format="dimension"/>
//    <attr name="ytextSize" format="dimension"/>
//    <attr name="xtextSize" format="dimension"/>
//    <attr name="axis_color" format="color"/>
//    <attr name="axis_height" format="dimension"/>
//    <attr name="line_color" format="color"/>
//    <attr name="line_height" format="dimension"/>
    private String Tag = "houyafei" ;

    //定义默认值
    private  static final int DEFAULT_TITLE_TEXT_SIZE = 15 ; // sp
    private  static final int DEFAULT_LINE_COLOR = 0xffd3d6da ;
    private  static final int DEFAULT_AXIS_COLOR = 0xFFFc00d1;
    private  static final int DEFAULT_AXIS_HEIGHT = 3 ; // dp
    private  static final int DEFAULT_LINE_HEIGHT= 1 ; // dp
    private  static final int DEFAULT_XTEXT_SIZE = 10 ; // sp
    private  static final int DEFAULT_YTEXT_SIZE= 10 ; // sp

    //
    private float mTitletextSize = sp2px(DEFAULT_TITLE_TEXT_SIZE);
    private float mXtextSize = sp2px(DEFAULT_XTEXT_SIZE);
    private float mYtextSize = sp2px(DEFAULT_YTEXT_SIZE);
    private int mlineColor = DEFAULT_LINE_COLOR;
    private int mAxisColor = DEFAULT_AXIS_COLOR;
    private int mAxisHeight = dp2px(DEFAULT_AXIS_HEIGHT);
    private int mLineHeight = dp2px(DEFAULT_LINE_HEIGHT);


    private ArrayList<ArrayList<Integer>> mVluesArray = new ArrayList<ArrayList<Integer>>();


    //数据
    private ArrayList<Integer> mvalues = new ArrayList<>();

    //画笔
    private Paint mPaint = new Paint();

    //
    private int mYmax = 100 ;
    private int mYmin = 0 ;

    //x,y轴的标签数量
    private int mYLabelNums = 10 ;
    private int mXLabelNums = 10 ;

    //图表标题
    private String mTitle = "LineAB" ;


    public GraphLines(Context context) {
        //super(context);
        this(context, null);
    }

    public GraphLines(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GraphLines(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        obtainStylable(attrs);
    }


    public void setmVluesArray(ArrayList<ArrayList<Integer>> mVluesArray) {
        this.mVluesArray = mVluesArray;
        if (mVluesArray==null){
            return ;
        }else{
            postInvalidate();
        }
    }


    /**
     * 设置坐标数据
     * @param values
     */
    public void setValues(ArrayList<Integer> values) {
        this.mvalues = values;
        if (values==null){
            return ;
        }else{
            postInvalidate();
          // invalidate();
           // draw(canvas);
           // onDraw(canvas);
        }

    }

    /**
     * 获取自己设置的属性
     * @param attrs
     */
    private void obtainStylable(AttributeSet attrs) {

        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.GraphLines);

        mTitletextSize = ta.getDimension(R.styleable.GraphLines_title_textSize,mTitletextSize);
        mXtextSize = ta.getDimension(R.styleable.GraphLines_xtextSize,mXtextSize);
         mYtextSize = ta.getDimension(R.styleable.GraphLines_ytextSize,mYtextSize);
        mlineColor = ta.getColor(R.styleable.GraphLines_line_color, mlineColor);
        mAxisColor = ta.getColor(R.styleable.GraphLines_axis_color, mAxisColor);
        mAxisHeight = (int) ta.getDimension(R.styleable.GraphLines_axis_height, mAxisHeight);
        mLineHeight = (int) ta.getDimension(R.styleable.GraphLines_line_height,mLineHeight);

        ta.recycle();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        canvas.save();
        //绘制其实坐标,将坐标原点移到画布的左下角
       // canvas.translate(0, getHeight());

        //测量标题的高度
        mPaint.setTextSize(mTitletextSize);
        mPaint.measureText(mTitle) ;
        int mTitleHeight = (int) (mPaint.descent()-mPaint.ascent());
        //计算y轴标签的宽度
        mPaint.setTextSize(mYtextSize);
        int yLabelWidth = (int) mPaint.measureText(String.valueOf(mYmax)); ;

        //绘制其实坐标,将坐标原点移到画布的左下角
        canvas.translate(yLabelWidth+4, getHeight());

        int realWidth = getWidth()-yLabelWidth-4 ;
        int realHeight = getHeight()-mTitleHeight  ;

        //绘制坐标轴
        mPaint.setStrokeWidth(mAxisHeight);
        mPaint.setColor(mAxisColor);
        mPaint.setAntiAlias(true);
        canvas.drawLine(0, 0, realWidth, 0, mPaint);
       // canvas.drawLine(0, 0, 0, -realHeight, mPaint);

        //绘制网格,坐标标签
        mPaint.setTextSize(mYtextSize);
        mPaint.setStrokeWidth(mLineHeight);
        mPaint.setColor(mlineColor);
        mPaint.setAntiAlias(true);
        int widthSeg = realWidth/mXLabelNums ;   //宽度间隔
        int heightSeg = realHeight/mYLabelNums ;  //高度间隔
        for (int i=0; i<mXLabelNums; i++){
           // canvas.drawLine((i+1)*widthSeg,0,(i+1)*widthSeg,-realHeight,mPaint);   //纵向线
        }
        for (int i=0;i<mYLabelNums;i++){
            canvas.drawText(String.valueOf((i+1)*(mYmax/mYLabelNums)),-yLabelWidth-1,-(i+1)*heightSeg+3,mPaint); //绘制标签
            canvas.drawLine(0,-(i+1)*heightSeg,realWidth,-(i+1)*heightSeg,mPaint);   //水平线
        }

       if(mVluesArray.size()>=2){
           //绘制曲线
           drawLines(canvas,realWidth,realHeight);
       }



        canvas.restore();
       // invalidate();
    }

    /**
     * 绘制曲线
     * @param canvas
     * @param realWidth
     * @param realHeight
     */
    private void drawLines(Canvas canvas, int realWidth, int realHeight) {
        //绘制曲线组 1
        //int yMax = 100 ;  //设置X轴最大值
        mPaint.setStrokeWidth(mLineHeight);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLUE);
        mPaint.setAntiAlias(true);
        int pointSeg = realWidth/40 ;   //数据点之间的间隔宽度间隔，40
        Path path = new Path() ;
        float ratio1 = mVluesArray.get(0).get(0)*1.0f/mYmax ;
        path.moveTo(0, -ratio1 * realHeight);
        for (int i=1; i<mVluesArray.get(0).size()&&i<40;i++){
             ratio1 = mVluesArray.get(0).get(i)*1.0f/mYmax ;

            path.lineTo((i + 1) * pointSeg, -ratio1 * realHeight);
        }

        canvas.drawPath(path, mPaint);

        //绘制曲线组 2
        //int yMax = 100 ;  //设置X轴最大值
        mPaint.setStrokeWidth(mLineHeight);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        Path path2 = new Path() ;
        float ratio = mVluesArray.get(1).get(0)*1.0f/mYmax ;
        path2.moveTo(0, -ratio*realHeight);
        for (int i=1; i<mVluesArray.get(1).size()&&i<40;i++){
             ratio = mVluesArray.get(1).get(i)*1.0f/mYmax ;

            path2.lineTo((i+1)*pointSeg,-ratio*realHeight);
        }
        canvas.drawPath(path2, mPaint);


    }

    private float sp2px(int spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, getResources().getDisplayMetrics());

    }



    private int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }
}
