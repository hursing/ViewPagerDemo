package com.example.liuhx.viewpagerdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ItemView extends LinearLayout {
  private TextView mTextView1;
  private TextView mTextView2;

  private OnePiece mOnePiece;

  public ItemView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setOnePiece(OnePiece onePiece) {
    mOnePiece = onePiece;
    mTextView1.setText(String.valueOf(onePiece.letter));
    mTextView2.setText(String.valueOf(onePiece.number));
  }

  public void reset() {
    // Release resource here: network connection, database cursor, ...
    mOnePiece = null;
  }

  @Override
  public void onFinishInflate() {
    // To differentiate View objects.
    ((TextView) findViewById(R.id.this_info)).setText(this.toString());

    mTextView1 = (TextView) findViewById(R.id.text1);
    mTextView2 = (TextView) findViewById(R.id.text2);
  }
}
