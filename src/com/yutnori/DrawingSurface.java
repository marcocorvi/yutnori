/* @file DrawingSurface.java
 *
 * @author marco corvi
 * @date dec 2015
 *
 * @brief Yutnori drawing: drawing surface (canvas)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.yutnori;

import android.content.Context;
import android.content.res.Resources;

import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import android.graphics.Matrix;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;

import java.util.ArrayList;

import android.util.Log;

/**
 */
public class DrawingSurface extends SurfaceView
                            implements SurfaceHolder.Callback
{
  static final String TAG = "yutnori";

  private Main mApp = null;

  void setApp( Main app ) { mApp = app; }

  private int mXPawn = 0;
  private int mYPawn = 0;
  private int mWPawn = 0;
  private int mHPawn = 0;
  private int mPawnNr = 0;

  private Boolean _run;
  protected DrawThread thread;
  private Bitmap mBitmap;
  public boolean isDrawing = true;
  private SurfaceHolder mHolder; // canvas holder
  private Context mContext;
  private AttributeSet mAttrs;
  private int mCanvasWidth;            // canvas width
  private int mCanvasHeight;           // canvas height

  public int width()  { return mCanvasWidth; }
  public int height() { return mCanvasHeight; }

  Board mBoard = null;
  Moves mMoves = null;

  private Paint mPaint[];
  int mColor = 0;
  int mStartColor  = 0;

  void setStartColor( int off ) { mStartColor = off; }

  private Positions mPositions;
  void addPosition( int pos ) { mPositions.add( pos ); }
  
  public DrawingSurface(Context context, AttributeSet attrs) 
  {
    super(context, attrs);
    mContext = context;
    mCanvasWidth  = 0;
    mCanvasHeight = 0;

    mPositions = new Positions( mContext );

    mBoard = new Board();
    mMoves = new Moves();

    mPaint = new Paint[2];
    mPaint[0] = new Paint();
    mPaint[0].setDither(true);
    mPaint[0].setStyle(Paint.Style.FILL);
    mPaint[0].setStrokeJoin(Paint.Join.ROUND);
    mPaint[0].setStrokeCap(Paint.Cap.ROUND);
    mPaint[0].setColor(0x993333ff); // BLUE
    mPaint[1] = new Paint();
    mPaint[1].setDither(true);
    mPaint[1].setStyle(Paint.Style.FILL);
    mPaint[1].setStrokeJoin(Paint.Join.ROUND);
    mPaint[1].setStrokeCap(Paint.Cap.ROUND);
    mPaint[1].setColor(0x99ff3333); // RED

    // makePawns( mContext.getResources() );

    thread = null;
    mAttrs   = attrs;
    mHolder = getHolder();
    mHolder.addCallback(this);
  }


  void refresh()
  {
    Canvas canvas = null;
    try {
      canvas = mHolder.lockCanvas();
      if ( mBitmap == null ) {
        mBitmap = Bitmap.createBitmap (1, 1, Bitmap.Config.ARGB_8888);
      }
      final Canvas c = new Canvas (mBitmap);
      mCanvasWidth  = c.getWidth();
      mCanvasHeight = c.getHeight();

      c.drawColor(0xff993333);// , PorterDuff.Mode.CLEAR);
      canvas.drawColor(0xff993333); // , PorterDuff.Mode.CLEAR);

      executeAll( c, previewDoneHandler );
    
      canvas.drawBitmap (mBitmap, 0,  0,null);
    } finally {
      if ( canvas != null ) {
        mHolder.unlockCanvasAndPost( canvas );
      }
    }
  }

  private Handler previewDoneHandler = new Handler()
  {
    @Override
    public void handleMessage(Message msg) {
      isDrawing = false;
    }
  };

  class DrawThread extends  Thread
  {
    private SurfaceHolder mSurfaceHolder;

    public DrawThread(SurfaceHolder surfaceHolder)
    {
        mSurfaceHolder = surfaceHolder;
    }

    public void setRunning(boolean run)
    {
      _run = run;
    }

    @Override
    public void run() 
    {
      while ( _run ) {
        if ( isDrawing == true ) {
          refresh();
        }
      }
    }
  }

  @Override
  public void surfaceChanged(SurfaceHolder mHolder, int format, int width,  int height) 
  {
    // TODO Auto-generated method stub
    mBitmap =  Bitmap.createBitmap (width, height, Bitmap.Config.ARGB_8888);;
  }

  @Override
  public void surfaceCreated(SurfaceHolder mHolder) 
  {
    // TODO Auto-generated method stub
    if (thread == null ) {
      thread = new DrawThread(mHolder);
    }
    thread.setRunning(true);
    thread.start();
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder mHolder) 
  {
    // TODO Auto-generated method stub
    boolean retry = true;
    thread.setRunning(false);
    while (retry) {
      try {
        thread.join();
        retry = false;
      } catch (InterruptedException e) {
        // we will try it again and again...
      }
    }
    thread = null;
  }

  int mWidth, mHeight;
  int xoffset[];
  int yoffset[];
  int board_yoffset;

  void setDimensions( int w, int h )
  {
    mWidth  = w;
    mHeight = h;

    xoffset = new int[34];
    yoffset = new int[34];

    int row = (int)((w - 40) / 7);
    mCardRow = row;
    int row2 = row + row/5;
    // Log.v("yutnori", "display " + w + " x " + h + " row " + row );
    int ROW0 = 0;
    int ROW1 = 1 * row2;
    int ROW2 = 2 * row2;
    int ROW3 = 3 * row2;
    int ROW4 = 4 * row2;
    int ROW5 = 5 * row2;
    int ROW6 = 6 * row2;

    int XOW0 = 0;
    int XOW1 = 1 * row;
    int XOW2 = 2 * row;
    int XOW3 = 3 * row;
    int XOW4 = 4 * row;
    int XOW5 = 5 * row;

    int OFFX = 0;
    int OFFY = row;
    board_yoffset = OFFY + ROW0;
    
    xoffset[ 0] = OFFX + ROW0; yoffset[ 0] = OFFY + ROW6;
    xoffset[ 1] = OFFX + ROW1; yoffset[ 1] = OFFY + ROW6;

    xoffset[ 2] = OFFX + ROW5; yoffset[ 2] = OFFY + ROW4;
    xoffset[ 3] = OFFX + ROW5; yoffset[ 3] = OFFY + ROW3;
    xoffset[ 4] = OFFX + ROW5; yoffset[ 4] = OFFY + ROW2;
    xoffset[ 5] = OFFX + ROW5; yoffset[ 5] = OFFY + ROW1;
    xoffset[ 6] = OFFX + ROW5; yoffset[ 6] = OFFY + ROW0;

    xoffset[ 7] = OFFX + ROW4; yoffset[ 7] = OFFY + ROW0;
    xoffset[ 8] = OFFX + ROW3; yoffset[ 8] = OFFY + ROW0;
    xoffset[ 9] = OFFX + ROW2; yoffset[ 9] = OFFY + ROW0;
    xoffset[10] = OFFX + ROW1; yoffset[10] = OFFY + ROW0;
    xoffset[11] = OFFX + ROW0; yoffset[11] = OFFY + ROW0;

    xoffset[12] = OFFX + ROW0; yoffset[12] = OFFY + ROW1;
    xoffset[13] = OFFX + ROW0; yoffset[13] = OFFY + ROW2;
    xoffset[14] = OFFX + ROW0; yoffset[14] = OFFY + ROW3;
    xoffset[15] = OFFX + ROW0; yoffset[15] = OFFY + ROW4;
    xoffset[16] = OFFX + ROW0; yoffset[16] = OFFY + ROW5;

    xoffset[17] = OFFX + ROW1; yoffset[17] = OFFY + ROW5;
    xoffset[18] = OFFX + ROW2; yoffset[18] = OFFY + ROW5;
    xoffset[19] = OFFX + ROW3; yoffset[19] = OFFY + ROW5;
    xoffset[20] = OFFX + ROW4; yoffset[20] = OFFY + ROW5;
    xoffset[21] = OFFX + ROW5; yoffset[21] = OFFY + ROW5;

    xoffset[22] = OFFX + XOW1; yoffset[22] = OFFY + XOW1;
    xoffset[23] = OFFX + XOW2; yoffset[23] = OFFY + XOW2;
    xoffset[24] = OFFX + XOW3; yoffset[24] = OFFY + XOW3;
    xoffset[25] = OFFX + XOW4; yoffset[25] = OFFY + XOW4;
    xoffset[26] = OFFX + XOW5; yoffset[26] = OFFY + XOW5;

    xoffset[27] = OFFX + XOW5; yoffset[27] = OFFY + XOW1;
    xoffset[28] = OFFX + XOW4; yoffset[28] = OFFY + XOW2;
    xoffset[29] = OFFX + XOW3; yoffset[29] = OFFY + XOW3;
    xoffset[30] = OFFX + XOW2; yoffset[30] = OFFY + XOW4;
    xoffset[31] = OFFX + XOW1; yoffset[31] = OFFY + XOW5;

    xoffset[32] = OFFX + ROW4;
    yoffset[32] = OFFY + ROW6;
    xoffset[33] = OFFX + ROW5;
    yoffset[33] = OFFY + ROW6;

    // for ( int k=0; k<34; ++k ) {
    //   Log.v("yutnori", "Offset[" + k + "] " + xoffset[k] + " " + yoffset[k] );
    // }

    makePawns( mContext.getResources() );
  }


  int getIndex(int x1, int y1 )
  {
    int  k;
    if ( y1 >= board_yoffset ) {   // board
      for ( k=0; k<34; ++k ) {
        if ( k==29 ) continue;
        if ( yoffset[k] < y1 && yoffset[k]+mCardRow > y1 &&
             xoffset[k] < x1 && xoffset[k]+mCardRow > x1 ) return k;
      }
    }
    return -100;
  }

  boolean isThrow( int x1, int y1 )
  {
    return ( x1 > mYutX && x1 < mYutX + mYutRow && y1 > mYutY && y1 < mYutY + mYutRow );
  }

  // -----------------------------------------------------
  // Drawer interface
  int pressedPawn = -1;

  void drawPawnNrs( Canvas c )
  {
    if ( mPawnNr <= 1 ) return;
    for ( int m = 0; m<mPawnNr; ++ m ) {
      drawPawnNr( c, m, mXPawn, mYPawn + m * mHPawn );
    }
  }

  void drawPawnNr( Canvas c, int m, int x, int y )
  {
    c.drawBitmap( ((m == pressedPawn )? mPawn[m] : mpawn[m]), x, y, null );
  }

  boolean isShowingPawnNrs() { return (mPawnNr > 1); }

  void setPawnNr( int nr, int pos ) 
  {
    pressedPawn = -1;
    mPawnNr = nr;
    mXPawn = xoffset[ pos ];
    mYPawn = yoffset[ pos ] + mCardRow;
    // Log.v( TAG, "set pawn " + nr + " at " + mXPawn + " " + mYPawn );
  }

  int getPawnNr( int x, int y ) 
  {
    // Log.v( TAG, "get pawn at " + x + " " + y + " pawn " + mPawnNr );
    if ( mPawnNr <= 1 ) return -1;
    int ret = 0;
    if ( x >= mXPawn && x < mXPawn + mWPawn && y >= mYPawn && y < mYPawn + mPawnNr* mHPawn ) {
      for ( int m = 0; m < mPawnNr; ++m ) {
        int y0 = mYPawn + m *  mHPawn;
        if ( y >= y0 && y < y0 +  mHPawn ) ret = m+1;
      }
    }
    return ret;
  }

  void resetPawnNr()
  {
    mPawnNr = 0;
    pressedPawn = -1;
  }
  
  int mMax = 0;

  void drawMoves( Canvas c )
  {
    int k = 0;
    for ( ; k < mMoves.size(); ++k ) drawYut( c, mMoves.value(k), k, 0, 0 );
    for ( ; k < mMax; ++k ) drawYut( c, 0, k, 0, 0 );
    mMax = mMoves.size();
  }

  void drawStartMoves( Canvas c )
  {
    int k = 0;
    for ( ; k < mMoves.size(); ++k ) {
      drawYut( c, mMoves.value(k), k, mColor, mStartColor );
    }
    for ( ; k < mMax; ++k ) drawYut( c, 0, k, 0, 0 );
    mMax = mMoves.size();
  }

  private void executeAll( Canvas c, Handler h )
  {
    if ( ! mCanDraw ) { // this is a generic delay before the surface can draw
      Delay.sleep( 5 );
      return;
    }
    if ( mBoard != null ) drawBoard( c );
    if ( mMoves != null ) {
      if ( mApp.mState == Main.WAIT || mApp.mState == Main.START ) {
        drawStartMoves( c );
      } else {
        drawMoves( c );
      }
    }

    if ( mApp.mState == Main.THROW || mApp.mState == Main.START ) {
      drawThrow( c );
    } else {
      drawPawnNrs( c );
    }
  }

  // -----------------------------------------------------
  private static int mpawnindex[] = {
    R.drawable.p1,
    R.drawable.p2,
    R.drawable.p3,
    R.drawable.p4
  };

  private static int mPawnindex[] = {
    R.drawable.pp1,
    R.drawable.pp2,
    R.drawable.pp3,
    R.drawable.pp4
  };


  private static int mzcardindex[] = {
    R.drawable.z0,
    R.drawable.z1,
    R.drawable.z2,
    R.drawable.z3,
    R.drawable.z4,
    R.drawable.z5,
    R.drawable.z6,
    R.drawable.z7,
    R.drawable.z8
  };

  private static int mZcardindex[] = {
    R.drawable.zz0,
    R.drawable.zz1,
    R.drawable.zz2,
    R.drawable.zz3,
    R.drawable.zz4,
    R.drawable.zz5,
    R.drawable.zz6,
    R.drawable.zz7,
    R.drawable.zz8
  };

  private static int mscardindex[] = {
    R.drawable.s0,
    R.drawable.s1,
    R.drawable.s2,
    R.drawable.s3,
    R.drawable.s4,
    R.drawable.s5,
    R.drawable.s6,
    R.drawable.s7,
    R.drawable.s8
  };

  private static int mScardindex[] = {
    R.drawable.ss0,
    R.drawable.ss1,
    R.drawable.ss2,
    R.drawable.ss3,
    R.drawable.ss4,
    R.drawable.ss5,
    R.drawable.ss6,
    R.drawable.ss7,
    R.drawable.ss8
  };

  private static int myutindex[] = {
    R.drawable.y0,
    R.drawable.y1,
    R.drawable.y2,
    R.drawable.y3,
    R.drawable.y4,
    R.drawable.y5
  };

  final static int NZ = 9;
  final static int NZ2 = 4; // (NZ-1)/2
  final static int NY = 6;

  private int mCardSize = 0;
  private int mCardRow  = 0;
  private int mYutSize  = 0;
  private int mYutRow   = 0;
  private int mYutX     = 0;
  private int mYutY     = 0;

  private boolean mCanDraw = false;

  private static Bitmap mzcard[]; // Big Spots
  private static Bitmap mZcard[]; // Small Spots
  private static Bitmap mscard[]; // Big Highlighted Spots
  private static Bitmap mScard[]; // Small Highlighted Spots
  private static Bitmap mzcard0[];
  private static Bitmap mZcard0[];
  private static Bitmap mscard0[];
  private static Bitmap mScard0[];
  private static Bitmap mpawn[];
  private static Bitmap mPawn[];

  private static Bitmap mArrow;
  private static Bitmap mYut[];

  

  private void makePawns( Resources res )
  {
    mzcard0 = new Bitmap[NZ];
    mZcard0 = new Bitmap[NZ];
    mscard0 = new Bitmap[NZ];
    mScard0 = new Bitmap[NZ];
    mzcard  = new Bitmap[NZ];
    mZcard  = new Bitmap[NZ];
    mscard  = new Bitmap[NZ];
    mScard  = new Bitmap[NZ];
    mpawn   = new Bitmap[4];
    mPawn   = new Bitmap[4];

    Bitmap bitmap = BitmapFactory.decodeResource( res, mzcardindex[0] );
    mCardSize = bitmap.getWidth();
    Matrix m = new Matrix();
    float s = (float)mCardRow / (float)mCardSize;
    m.preScale( s, s );
    
    for ( int k = 0; k < NZ; ++k ) {
      bitmap = BitmapFactory.decodeResource( res, mzcardindex[k] );
      mzcard0[k] = Bitmap.createBitmap( bitmap, 0, 0, mCardSize, mCardSize, m, true);
      bitmap = BitmapFactory.decodeResource( res, mZcardindex[k] );
      mZcard0[k] = Bitmap.createBitmap( bitmap, 0, 0, mCardSize, mCardSize, m, true);
      bitmap = BitmapFactory.decodeResource( res, mscardindex[k] );
      mscard0[k] = Bitmap.createBitmap( bitmap, 0, 0, mCardSize, mCardSize, m, true);
      bitmap = BitmapFactory.decodeResource( res, mScardindex[k] );
      mScard0[k] = Bitmap.createBitmap( bitmap, 0, 0, mCardSize, mCardSize, m, true);
    }
    resetPawns();

    // Log.v( "Yutnori", "scale " + s );

    bitmap = BitmapFactory.decodeResource( res, mpawnindex[0] );
    mWPawn = (int)(bitmap.getWidth()); // * s);
    mHPawn = (int)(bitmap.getHeight()); // * s);
    for ( int j = 0; j < 4; ++j ) {
      bitmap = BitmapFactory.decodeResource( res, mpawnindex[j] );
      mpawn[j] = Bitmap.createBitmap( bitmap, 0, 0, mWPawn,  mHPawn, m, true);
      bitmap = BitmapFactory.decodeResource( res, mPawnindex[j] );
      mPawn[j] = Bitmap.createBitmap( bitmap, 0, 0, mWPawn,  mHPawn, m, true);
    }
    mWPawn *= s;
    mHPawn *= s;

    mArrow = BitmapFactory.decodeResource( res, R.drawable.elblack );
    mYut   = new Bitmap[NY];
    for ( int k = 0; k < NY; ++k ) {
      bitmap = BitmapFactory.decodeResource( res, myutindex[k] );
      if ( k == 0 ) {
        mYutSize = bitmap.getWidth();
        mYutRow  = 1 + (int)( mYutSize * s ) + 5;  // 5 = padding
      }
      mYut[k] = Bitmap.createBitmap( bitmap, 0, 0, mYutSize, mYutSize, m, true );
    }

    mYutX = (mWidth - mYutRow)/2;
    mYutY = yoffset[0];

    mCanDraw = true;
  }

  void resetPawns()
  {
    mColor = 1;
    for ( int k = 0; k < NZ; ++k ) {
      mzcard[k] = mzcard0[k];
      mZcard[k] = mZcard0[k];
      mscard[k] = mscard0[k];
      mScard[k] = mScard0[k];
    }
  }

  void setPawns( int color )
  {
    mColor = color;
    if ( mColor == -1 ) {
      for ( int k = 1; k <= NZ2; ++k ) {
        mzcard[k]     = mzcard0[NZ2+k];
        mZcard[k]     = mZcard0[NZ2+k];
        mscard[k]     = mscard0[NZ2+k];
        mScard[k]     = mScard0[NZ2+k];
        mzcard[NZ2+k] = mzcard0[k];
        mZcard[NZ2+k] = mZcard0[k];
        mscard[NZ2+k] = mscard0[k];
        mScard[NZ2+k] = mScard0[k];
      }
    } else {
      for ( int k = 1; k <= NZ2; ++k ) {
        mzcard[k]     = mzcard0[k];
        mZcard[k]     = mZcard0[k];
        mscard[k]     = mscard0[k];
        mScard[k]     = mScard0[k];
        mzcard[NZ2+k] = mzcard0[NZ2+k];
        mZcard[NZ2+k] = mZcard0[NZ2+k];
        mscard[NZ2+k] = mscard0[NZ2+k];
        mScard[NZ2+k] = mScard0[NZ2+k];
      }
    }
  }


  int mHighlight      = -1;
  void setHighlight( int pos ) 
  {
    if ( pos == 29 ) pos = 24;

    mHighlight = -1;
    if ( pos >= 0 && pos < 2 ) {
      mHighlight = pos;
    } else if ( pos >= 2 && pos < 32 ) {
      if ( mBoard.value(pos) > 0 || YutnoriPrefs.getPos() != YutnoriPrefs.POS_TOTAL ) {
        mHighlight = pos;
      }
    }
  }

  private void drawBoard( Canvas c )
  {
    for ( int k=2; k<32; ++k ) {
      if ( k == 29 ) continue;
      int b = mBoard.value(k);
      if ( b < 0 && mApp.mState != Main.OVER ) {
        switch ( YutnoriPrefs.getPos() ) {
          case YutnoriPrefs.POS_PARTIAL:
            if ( ! mPositions.contains( k ) ) b = 0;
            break;
          case YutnoriPrefs.POS_TOTAL:
            b = 0;
            break;
          // case YutnoriPrefs.POS_NO:
          // default:
          //   break;
        }
      }
      drawCard( c, b, k );
    }
    drawStart( c );
    drawHome( c );
    drawArrow( c );
  }

  private void drawCard( Canvas c, int b, int pos )
  {
    Bitmap card = null;
    if ( b < 0 ) b = - b;
    else if ( b > 0 ) b += 4;
    if ( b >= NZ ) return;
    if ( pos >= 32 ) {
      card = ( b == 0 )? mscard[b] : mzcard[b];
    } else if ( pos <= 1 ) {
      card = ( pos == mHighlight )? mscard[b] : mzcard[b];
    } else if ( pos == 6 || pos == 11 || pos == 16 || pos == 21 || pos == 24 ) {
      card = (pos == mHighlight)? mscard[b] : mzcard[b];
    } else {
      card = (pos == mHighlight)? mScard[b] : mZcard[b];
    }
    if ( card != null ) {
      int x = xoffset[pos];
      int y = yoffset[pos];
      // draw card at x,y
      c.drawBitmap( card, x, y, null );
    }
  }
  
  private void drawStart( Canvas c )
  {
    drawCard( c, - mBoard.start(0), 0 );
    drawCard( c,   mBoard.start(1), 1 );
  }

  private void drawHome( Canvas c )
  {
    drawCard( c, - mBoard.home(0), 32 );
    drawCard( c,   mBoard.home(1), 33 );
  }

  private void drawArrow( Canvas c )
  {
    // int x = xoffset[2] + mCardRow;
    int x = xoffset[2];
    int y = yoffset[2];
    c.drawBitmap( mArrow, x, y, null );
  }

  private void drawYut( Canvas c, int m, int k, int color, int off )
  {
    int x = 0 + mYutRow * k;
    int y = 0;
    if ( color != 0 ) {
      c.drawCircle( x+mYutRow/2, y+mYutRow/2, mYutRow/2, mPaint[ (k+off)%2 ] );
    }
    c.drawBitmap( mYut[m], x, y, null );
  }

  private void drawThrow( Canvas c )
  {
    c.drawBitmap( mYut[5], mYutX, mYutY, null );
  }

}
