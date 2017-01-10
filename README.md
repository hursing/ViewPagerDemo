from http://blog.csdn.net/hursing/article/details/54311695

#1.目标
主界面要求水平移动翻页效果，每次只能翻一页，可以翻无数页。

#2.实现思路
针对“每次只能翻一页”这个要求，简单使用SDK的话只有用ViewPager。ViewPager的PageAdapter是没有实现RecyclerView的ViewHolder.itemView回收机制的。即使是子类FragmentStatePagerAdapter，也只是保存状态后销毁Fragment，Fragment本身是在不断地创建和销毁，没有重复利用。

正确地使用PageAdapter，必须先理解`Object instantiateItem (ViewGroup container, int position)`函数中返回的Object的意义。如果View能重复利用，则表明它仅用于展示数据，没有业务逻辑。数据的部分，我们通常用Model表示。`instantiateItem`函数的返回值Object，正是要求返回在`position`位置的Model数据对象，与实现这个函数时创建的View形成映射关系。

#3.自定义PageAdapter
直接在代码注释中讲解：
```java
// 转载请注明出处：http://blog.csdn.net/hursing
private class ViewPageAdapter extends PagerAdapter {
  // 用List保存本来要销毁的View，需要的时候再取出来
  private List<ItemView> mViewRecycler = new LinkedList<>();
  // 用Map保存Model Object（这里是OnePiece）和View的映射关系
  private Map<OnePiece, ItemView> mOnePieceItemViewMap = new HashMap<>();

  @Override
  public int getCount() {
    // Model保存着OnePiece的对象数组
    return mPieceModel.pieces.size();
  }

  @Override
  public boolean isViewFromObject(View view, Object object) {
    // 如果不需要回收利用View，都是用`return view == object`实现的，
    // 与此对应`instantiateItem()`返回View，
    // `destroyItem()`要销毁View。
    OnePiece onePiece = (OnePiece) object;
    return view == mOnePieceItemViewMap.get(onePiece);
  }

  @Override
  public Object instantiateItem (ViewGroup container, int position) {
    ItemView itemView;
    // 这个if else是实现回收利用的关键处之一。如果有缓存则取缓存，
    // 没有则创建对象
    if (mViewRecycler.size() > 0) {
      itemView = mViewRecycler.remove(0);
    } else {
      itemView = (ItemView) LayoutInflater.from(MainActivity.this)
              .inflate(R.layout.item_view, container, false);
    }
    // container实际是ViewPager对象。从这个函数的实现看，
    // ViewPager并不知道对它add的View是做什么用的，所以
    // 需要`isViewFromObject()`函数来询问这个View是否有
    // 意义（是ItemView），如果是，后面会对它做正确layout
    container.addView(itemView);

    OnePiece onePiece = mPieceModel.pieces.get(position);
    // 这里是itemView根据Model object来重新设置界面元素的时机。
    itemView.setOnePiece(onePiece);
    mOnePieceItemViewMap.put(onePiece, itemView);

	// 如果不需要回收利用View，这里的实现是`return itemView`。
	// 要回收，就要建立Model object和View的映射关系，因此
	// 这里返回Model object
    return onePiece;
  }

  @Override
  public void destroyItem (ViewGroup container, int position, Object object) {
    OnePiece onePiece = (OnePiece) object;
    ItemView itemView = mOnePieceItemViewMap.remove(onePiece);
    // 必须自行remove itemView
    container.removeView(itemView);
    // itemView要被回收放入缓存了，这里提供一个时机做清理。
    itemView.reset();
    // 实现回收的关键处，把itemView保存到List里。而不是任由GC销毁。
    mViewRecycler.add(itemView);
  }

  // 必须重写此函数。super固定地`return POSITION_UNCHANGED`，
  // 这就会导致`notifyDataSetChanged()`没有效果，因为它的意义
  // 是说这个Model object的位置没变化，自然不需要刷新。
  @Override
  public int getItemPosition(Object object) {
    OnePiece onePiece = (OnePiece) object;
    if (mPieceModel.pieces.contains(onePiece)) {
      return mPieceModel.pieces.indexOf(onePiece);
    } else {
	  // 返回POSITION_NONE说明这个model object已经被废弃了，
	  // 接下来就会由`destroyItem()`回调来销毁它对应的View。
      return POSITION_NONE;
    }
  }
}
```

#4.完整代码
代码放在 https://github.com/hursing/ViewPagerDemo

界面结构：5个按钮，分别是增、删、重置Model object。下面是ViewPager，按完按钮滚动区域会有变化。每一页有三个TextView，其中最上面的是显示ItemView本身的引用信息，留意它的引用地址（如截图中的12f46ad2），会发现最多创建4个ItemView（缓存了左、右、当前，还有一个预备），再怎么滑都会重复利用。

截图：
![截图](http://img.blog.csdn.net/20170110113832249?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvaHVyc2luZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

下面是贴些代码，可不看了：

OnePiece.java
```java
public class OnePiece {
  public char letter;
  public int number;

  public OnePiece(char letter, int number) {
    this.letter = letter;
    this.number = number;
  }
}
```

item_view.xml
```xml
<com.example.liuhx.viewpagerdemo.ItemView
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#ccc"
    android:orientation="vertical"
    android:paddingTop="40dp"
    android:paddingBottom="40dp"
    android:paddingLeft="20dp"
    android:paddingRight="20dp">

    <TextView
        android:id="@+id/this_info"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="reference info" />

    <TextView
        android:id="@+id/text1"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="sample1" />

    <TextView
        android:id="@+id/text2"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="sample2" />

</com.example.liuhx.viewpagerdemo.ItemView>
```

ItemView.java
```java
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
```

PieceModel.java
```java
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
```

activity_main.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/activity_main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context="com.example.liuhx.viewpagerdemo.MainActivity">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        <Button
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:id="@+id/add_head"
            android:text="add head"/>
        <Button
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:id="@+id/add_tail"
            android:text="add tail"/>
        <Button
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:id="@+id/remove_head"
            android:text="remove head"/>
        <Button
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:id="@+id/remove_tail"
            android:text="remove tail"/>
        <Button
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:id="@+id/change"
            android:text="change"/>
    </LinearLayout>

    <android.support.v4.view.ViewPager
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/view_pager" />
</LinearLayout>
```

MainActivity.java
```java
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  private ViewPager mViewPager;
  private ViewPageAdapter mViewPageAdapter;
  private PieceModel mPieceModel;

  private class ViewPageAdapter extends PagerAdapter {
    private List<ItemView> mViewRecycler = new LinkedList<>();
    private Map<OnePiece, ItemView> mOnePieceItemViewMap = new HashMap<>();

    @Override
    public int getCount() {
      return mPieceModel.pieces.size();
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

      OnePiece onePiece = mPieceModel.pieces.get(position);
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

    // Must override this method, or else notifyDataSetChanged() has no effect.
    @Override
    public int getItemPosition(Object object) {
      OnePiece onePiece = (OnePiece) object;
      if (mPieceModel.pieces.contains(onePiece)) {
        return mPieceModel.pieces.indexOf(onePiece);
      } else {
        return POSITION_NONE;
      }
    }
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mPieceModel = new PieceModel();
    mPieceModel.setOnPiecesChangedListener(new PieceModel.OnPiecesChangedListener() {
      @Override
      public void onPiecesChanged() {
        mViewPageAdapter.notifyDataSetChanged();
      }
    });

    mViewPager = (ViewPager) findViewById(R.id.view_pager);
    mViewPageAdapter = new ViewPageAdapter();
    mViewPager.setAdapter(mViewPageAdapter);
    mViewPager.setOffscreenPageLimit(1);

    findViewById(R.id.add_head).setOnClickListener(this);
    findViewById(R.id.add_tail).setOnClickListener(this);
    findViewById(R.id.remove_head).setOnClickListener(this);
    findViewById(R.id.remove_tail).setOnClickListener(this);
    findViewById(R.id.change).setOnClickListener(this);
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.add_head:
        mPieceModel.addPiece(false);
        break;
      case R.id.add_tail:
        mPieceModel.addPiece(true);
        break;
      case R.id.remove_head:
        if (mPieceModel.pieces.size() == 0) {
          Toast.makeText(this, "Please press ADD first", Toast.LENGTH_SHORT).show();
        } else {
          mPieceModel.removePiece(false);
        }
        break;
      case R.id.remove_tail:
        if (mPieceModel.pieces.size() == 0) {
          Toast.makeText(this, "Please press ADD first", Toast.LENGTH_SHORT).show();
        } else {
          mPieceModel.removePiece(true);
        }
        break;
      case R.id.change:
        if (mPieceModel.pieces.size() == 0) {
          Toast.makeText(this, "Please press ADD first", Toast.LENGTH_SHORT).show();
        } else {
          mPieceModel.changePieces();
        }
        break;
      default:
        break;
    }
  }
}
```

转载请注明出处：http://blog.csdn.net/hursing
