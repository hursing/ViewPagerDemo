package com.example.liuhx.viewpagerdemo;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by liuhx on 9/1/2017.
 */

public class PieceModel {
  public interface OnPiecesChangedListener {
    void onPiecesChanged();
  }

  public List<OnePiece> pieces = new LinkedList<>();

  private OnPiecesChangedListener mOnPiecesChangedListener;

  private char mLetterSeed = 'A';
  private int mNumberSeed = 0;

  public void setOnPiecesChangedListener(OnPiecesChangedListener listener) {
    mOnPiecesChangedListener = listener;
  }

  public void addPiece(boolean toTail) {
    OnePiece onePiece = new OnePiece(mLetterSeed, mNumberSeed++);
    if (toTail)
      pieces.add(onePiece);
    else
      pieces.add(0, onePiece);
    mOnPiecesChangedListener.onPiecesChanged();
  }

  public void removePiece(boolean fromTail) {
    if (fromTail)
      pieces.remove(pieces.size() - 1);
    else
      pieces.remove(0);
    mOnPiecesChangedListener.onPiecesChanged();
  }

  public void changePieces() {
    ++mLetterSeed;
    mNumberSeed = 0;
    int size = pieces.size();
    pieces.clear();
    for (; mNumberSeed < size; ++mNumberSeed) {
      OnePiece onePiece = new OnePiece(mLetterSeed, mNumberSeed);
      pieces.add(onePiece);
    }
    mOnPiecesChangedListener.onPiecesChanged();
  }
}
