package com.example.liuhx.viewpagerdemo;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  private ViewPager mViewPager;
  private ViewPageAdapter mViewPageAdapter;

  private class ViewPageAdapter extends PagerAdapter {
    private List<ItemView> mViewRecycler = new LinkedList<>();
    private Map<OnePiece, ItemView> mOnePieceItemViewMap = new HashMap<>();

    @Override
    public int getCount() {
      return PieceModel.getInstance().pieces.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
      OnePiece onePiece = (OnePiece) object;
      return view == mOnePieceItemViewMap.get(onePiece);
    }

    @Override
    public Object instantiateItem (ViewGroup container, int position) {
      ItemView itemView;
      if (mViewRecycler.size() > 0) {
        itemView = mViewRecycler.remove(0);
      } else {
        itemView = (ItemView) LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.item_view, container, false);
      }
      container.addView(itemView);

      OnePiece onePiece = PieceModel.getInstance().pieces.get(position);
      itemView.setOnePiece(onePiece);
      mOnePieceItemViewMap.put(onePiece, itemView);

      return onePiece;
    }

    @Override
    public void destroyItem (ViewGroup container, int position, Object object) {
      OnePiece onePiece = (OnePiece) object;
      ItemView itemView = mOnePieceItemViewMap.remove(onePiece);
      container.removeView(itemView);
      itemView.reset();
      mViewRecycler.add(itemView);
    }

    @Override
    public int getItemPosition(Object object) {
      OnePiece onePiece = (OnePiece) object;
      if (PieceModel.getInstance().pieces.contains(onePiece)) {
        return PieceModel.getInstance().pieces.indexOf(onePiece);
      } else {
        return POSITION_NONE;
      }
    }
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mViewPager = (ViewPager) findViewById(R.id.view_pager);
    mViewPageAdapter = new ViewPageAdapter();
    mViewPager.setAdapter(mViewPageAdapter);
    mViewPager.setOffscreenPageLimit(1);

    findViewById(R.id.add_head).setOnClickListener(this);
    findViewById(R.id.add_tail).setOnClickListener(this);
    findViewById(R.id.remove_head).setOnClickListener(this);
    findViewById(R.id.remove_tail).setOnClickListener(this);
    findViewById(R.id.change).setOnClickListener(this);

    PieceModel.getInstance().setOnPiecesChangedListener(new PieceModel.OnPiecesChangedListener() {
      @Override
      public void onPiecesChanged() {
        mViewPageAdapter.notifyDataSetChanged();
      }
    });
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.add_head:
        PieceModel.getInstance().addPiece(false);
        break;
      case R.id.add_tail:
        PieceModel.getInstance().addPiece(true);
        break;
      case R.id.remove_head:
        if (PieceModel.getInstance().pieces.size() == 0) {
          Toast.makeText(this, "Please press ADD first", Toast.LENGTH_SHORT).show();
        } else {
          PieceModel.getInstance().removePiece(false);
        }
        break;
      case R.id.remove_tail:
        if (PieceModel.getInstance().pieces.size() == 0) {
          Toast.makeText(this, "Please press ADD first", Toast.LENGTH_SHORT).show();
        } else {
          PieceModel.getInstance().removePiece(true);
        }
        break;
      case R.id.change:
        if (PieceModel.getInstance().pieces.size() == 0) {
          Toast.makeText(this, "Please press ADD first", Toast.LENGTH_SHORT).show();
        } else {
          PieceModel.getInstance().changePieces();
        }
        break;
      default:
        break;
    }
  }
}
