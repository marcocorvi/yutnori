/* @file Main.java
 *
 * @author marco corvi
 * @date nov 2015
 *
 * @brief Yutnori main drawing activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.yutnori;

import android.app.Activity;
import android.os.AsyncTask;
import android.content.Context;
import android.content.res.Resources;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;

import android.os.Bundle;

import android.view.Menu;
// import android.view.SubMenu;
import android.view.MenuItem;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Display;
import android.util.DisplayMetrics;
import android.widget.Toast;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;


import android.util.Log;

/**
 */
public class Main extends Activity
                  implements View.OnTouchListener
                           , OnSharedPreferenceChangeListener
                           , ISender
{
  static String TAG = "Yutnori-TITO";

  final static int sleepAfterHighlight      =  5;
  final static int sleepBeforeCompareStarts = 20;
  final static int sleepAfterExecPawns      =  5;
  final static int sleepAfterAndroid        =  5;
  final static int sleepAndroid             =  5;
  final static int sleepBeforeAndroid       =  5;

  // final static String YUT1  = "[Yut1]";
  // final static String YUT2  = "[Yut2]";
  final static String EMPTY = "";
  static String NONE   = null;
  static String LISTEN = null;

  private SharedPreferences mPrefs;

  YutnoriAlertDialog mAlert = null;

  void dismissAlert()
  {
    if ( mAlert != null ) {
      mAlert.dismiss();
      mAlert = null;
    }
  }

  private MenuItem mMenuNew      = null;
  // private MenuItem mMenuYut   = null;
  // private MenuItem mMenuJoin     = null;
  private MenuItem mMenuSettings = null;
  private MenuItem mMenuHelp     = null;
  private MenuItem mMenuAbout    = null;

  ConnectDialog mConnectDialog = null;
  SplashDialog  mSplashDialog = null;

  Yutnori  mYutnori   = null;
  Board mBoard;
  Moves mMoves;
  int myPawnNr = 0;
  int yourPawnNr = 0;
  // int mMySkip = 0;

  String  mRemote  = null;     // name of the other yutnori
  boolean mPlaying = false;    // whether it is playing
  boolean mExited  = false;    // whether this yutnori has exited
  boolean mJoining = false;    // whether i am joinint the other yutnori
  boolean mConnected = false;  // whether i am connected to the other yutnori

  DrawingSurface mDrawingSurface;

  BluetoothAdapter  mBTAdapter  = null;
  static ConnectionHandler mConnection = null;

  int mThisStart  = -1; // to decide who starts
  int mOtherStart = -1;
  int mLastWinner =  0; // 1: you, -1 your friend/Android

  private void resetStarts()
  {
    mThisStart  = -1;
    mOtherStart = -1;
  }

  // private int[] mDisclosure;
  // private int mDisclosureIndex = 0;

  State   mState;
  int     mPlayer = Player.USER;
  boolean mMoving = false;

  // private String mStrategyString = EMPTY;

  private class PlayAndroid extends AsyncTask< Void, Integer, Integer >
  {
    Context mContext;

    PlayAndroid( Context context )
    {
      mContext = context;
    }
 
    // return final android state
    @Override
    protected Integer doInBackground( Void... v )
    {
      Delay.sleep( sleepBeforeAndroid );
      return mYutnori.playOnce( sleepAndroid );
      // Delay.sleep( sleepAfterAndroid );
      // Log.v( TAG, "PlayAndroid done" );
    }

    @Override
    protected void onProgressUpdate(Integer... progress) { }

    @Override
    protected void onPostExecute( Integer v )
    {
      int ret = v.intValue();
      if ( ret < 0 ) {
        if ( YutnoriPrefs.isDoSkip() ) {  // if ( ! YutnoriPrefs.isSeoulOrBusan() )
          if ( ! YutnoriPrefs.isDoNone() ) {
            Toast.makeText( mContext, R.string.android_skipping, Toast.LENGTH_LONG ).show();
          }
          Delay.sleep( sleepAndroid );
        }
      }
      mMoves.clear();
      if ( mBoard.winner() != 0 ) {
        mState.setOver();
      } else { 
        if ( mState.isSkipping( mPlayer ) ) {
          mState.setSkip();
        } else {
          mState.setThrow();
        }
      } 
      // Log.v( TAG, "PlayAndroid on Post Exec: ret " + ret + " state " + mState.toString() );
      setTheTitle( );
      if ( mState.isOver() ) alertWinner();
      mPlaying = true;
      // Log.v( TAG, "USER turn" );
    }
  };

  protected void setTheTitle( )
  {
    if ( mExited ) return;
    boolean long_title = false;

    // Log.v( TAG, "setTheTitle state " + mState.toString() + " Remote " + mRemote );
    mLastWinner = mBoard.winner();
    StringBuilder sb = new StringBuilder();
    // if ( long_title ) 
    //   sb.append( getResources().getString( R.string.title_yutnori ) ); 
    // if ( YutnoriPrefs.mSplitGroup ) { sb.append( " {" ); } else { sb.append( " [" ); }
    if ( YutnoriPrefs.isBackDo() ) {
      if ( YutnoriPrefs.isDoSkip() )      { sb.append( getResources().getString( R.string.title_skipdo ) ); }
      else if ( YutnoriPrefs.isDoSpot() ) { sb.append( getResources().getString( R.string.title_dospot ) ); }
      else if ( YutnoriPrefs.isDoCage() ) { sb.append( getResources().getString( R.string.title_docage ) ); }
      else if ( YutnoriPrefs.isDoNone() ) { sb.append( getResources().getString( R.string.title_backdo ) ); }
    } else if ( YutnoriPrefs.isSeoul()  ) {
      sb.append( getResources().getString( R.string.title_seoul    ) ); 
    } else if ( YutnoriPrefs.isBusan()  )  {
      sb.append( getResources().getString( R.string.title_busan    ) ); 
    } else {
      sb.append( getResources().getString( R.string.title_traditional   ) );
    }
    // if ( ! mConnected ) {
    //   // sb.append( "-" );
    //   // sb.append( mYutnori.getEngine() );
    // } else {
    //   if ( mRemote == null ) {
    //     // sb.append( mStrategyString );
    //   } else {
    //     if ( long_title ) sb.append( " " + mRemote ); 
    //   }
    // }
    // if ( YutnoriPrefs.mSplitGroup ) { sb.append( "} " ); } else { sb.append( "] " ); }

    if ( mLastWinner == 1 ) {
      mState.setOver();
      sb.append( " -- " + getResources().getString( R.string.you_won ) );
    } else if ( mLastWinner == -1 ) {
      mState.setOver();
    }
    sb.append( " - " + mState.toString() );
    setTitle( sb.toString() );
    setTitleColor( ( mState.isReady() )? 0xffff0000 : 0xff000000 );
  }

  private void alertWinner()
  {
    YutnoriPrefs.doPos = false;
    String str = null;
    if ( mLastWinner == 1 ) {
      str = getResources().getString( R.string.you_won );
    } else if ( mLastWinner == -1 ) {
      if ( mConnected ) {
        str = getResources().getString( R.string.friend_won );
      } else {
        str = getResources().getString( R.string.i_won );
      }
    } else {
      return;
    }
    if ( str != null ) {
      dismissAlert();
      (new NewGameDialog( this, this, str)).show();
      // Resources res = getResources();
      // String quit  = res.getString( R.string.button_quit );
      // String again = res.getString( R.string.button_again );
      // mAlert = new YutnoriAlertDialog( this, res, str, quit, again,
      //     new DialogInterface.OnClickListener() {
      //       @Override public void onClick( DialogInterface dialog, int btn ) { askExit(); }
      //     },
      //     new DialogInterface.OnClickListener() {
      //       @Override public void onClick( DialogInterface dialog, int btn ) { doNewGame(); }
      //     }
      // );
    }
  }

  public void onSharedPreferenceChanged( SharedPreferences sp, String k ) 
  {
    YutnoriPrefs.checkPreference( sp, k, this );
  }

  void setPrefs( boolean mal_split, int rule, int backdo )
  {
    YutnoriPrefs.mSplitGroup  = mal_split;
    YutnoriPrefs.mSpecialRule = rule;
    YutnoriPrefs.mBackDo      = backdo;
    // Editor editor = mPrefs.edit();
    // editor.putInt( YutnoriPrefs.KEY_SPECIAL, YutnoriPrefs.mSpecialRule );
    // editor.putInt( YutnoriPrefs.KEY_BACKDO,  YutnoriPrefs.mBackDo );
    // editor.apply();

    // TODO pick Board State and Yutnori
    resetStatus(  true );
  }

  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    NONE   = getResources().getString( R.string.NONE );
    LISTEN = getResources().getString( R.string.LISTEN );

    setContentView(R.layout.main);

    // preferences ---------------------
    mPrefs = PreferenceManager.getDefaultSharedPreferences( this );
    YutnoriPrefs.load( mPrefs );
    mPrefs.registerOnSharedPreferenceChangeListener( this );
 
    // ---------------------------------   

    mDrawingSurface = (DrawingSurface) findViewById(R.id.drawingSurface);
    // mDrawingSurface.makeCards( getResources() );

    State.initStateStrings( getResources() );

    // Display display = getWindowManager().getDefaultDisplay();
    // DisplayMetrics dm = new DisplayMetrics();
    // display.getMetrics( dm );
    // int width = dm widthPixels;
    mDrawingSurface.setDimensions(
      getResources().getDisplayMetrics().widthPixels,
      getResources().getDisplayMetrics().heightPixels );
    mDrawingSurface.setApp( this );

    // mIsNotMultitouch = ! getPackageManager().hasSystemFeature( PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH );

    // mStrategyString = YUT1;

    mDrawingSurface.setOnTouchListener(this);

    mBTAdapter = BluetoothAdapter.getDefaultAdapter();
    mConnection = new ConnectionHandler( this );
    startRemote();

    mRemote  = null;
    mPlaying = true;
    
    mSplashDialog = new SplashDialog( this, this );
    mSplashDialog.show();
    // alertNumber( R.string.color_none );

    // mDisclosure = new int[4];
    // for ( int k = 0; k<mDisclosure.length; ++ k ) {
    //   int v = 6 + 5 * ((int)( Math.random()*4.9999 ));
    //   if ( v > 21 ) v = 24;
    //   mDisclosure[k] = v;
    //   // Log.v( TAG, "Disclose " + k + " = " + v );
    // }
    // mDisclosureIndex = 0;
    mBoard = mDrawingSurface.getBoard();
    mMoves = mDrawingSurface.getMoves();
    mYutnori = new Yutnori( mBoard, mMoves, mDrawingSurface );
    mState   = new State();
    setEngine( YutnoriPrefs.mEngine );
    setTheTitle();
  }

  void setStartAs( int start )
  {
    switch ( start ) {
      case 0:
        mPlaying = true;
        mState.setThrow();
        setTheTitle();
        alertNumber( R.string.color_none );
        break;
      case 1: // wait a friend
        mPlaying = false;
        mState.setHold();
        setTheTitle();
        break;
      case 2: // ask a friend 
        mPlaying = true;
        mState.setHold();
        setTheTitle();
        mConnectDialog = new ConnectDialog( this, this );
        mConnectDialog.show();
        break;
    }
  }

  // ------------------------------------------------------------------------

  @Override
  protected synchronized void onResume()
  {
    super.onResume();
    // Log.v( TAG, "Main Activity onResume " );
    mDrawingSurface.isDrawing = true;
  }

  @Override
  protected synchronized void onPause() 
  { 
    super.onPause();
    // Log.v( TAG, "Main Activity onPause " );
    mDrawingSurface.isDrawing = false;
  }

  @Override
  protected synchronized void onStart()
  {
    super.onStart();
    // Log.v( TAG, "Main Activity onStart ");
  }

  @Override
  protected synchronized void onStop()
  {
    super.onStop();
    // Log.v( TAG, "Main Activity onStop ");
    if ( mConnected ) {
      if ( mConnection != null ) mConnection.disconnect();
    }
  }

  @Override
  protected synchronized void onDestroy()
  {
    super.onDestroy();
    // Log.v( TAG, "Main activity onDestroy");
    stopRemote();
  }

  // ------------------------------------------------------------------------
  
  // private void dumpEvent( MotionEventWrap ev )
  // {
  //   String name[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE", "PTR_DOWN", "PTR_UP", "7?", "8?", "9?" };
  //   StringBuilder sb = new StringBuilder();
  //   int action = ev.getAction();
  //   int actionCode = action & MotionEvent.ACTION_MASK;
  //   sb.append( "Event action_").append( name[actionCode] );
  //   if ( actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP ) {
  //     sb.append( "(pid " ).append( action>>MotionEvent.ACTION_POINTER_ID_SHIFT ).append( ")" );
  //   }
  //   sb.append( " [" );
  //   for (int i=0; i<ev.getPointerCount(); ++i ) {
  //     sb.append( "#" ).append( i );
  //     sb.append( "(pid " ).append( ev.getPointerId(i) ).append( ")=" ).append( (int)(ev.getX(i)) ).append( "." ).append( (int)(ev.getY(i)) );
  //     if ( i+1 < ev.getPointerCount() ) sb.append( ":" );
  //   }
  //   sb.append( "]" );
  //   Log.v( TAG, sb.toString() );
  // }
  
  // ------------------------------------------------------------------------
  int mStartPos = -1;

  @Override
  public boolean onTouch( View view, MotionEvent rawEvent )
  {
    MotionEventWrap event = MotionEventWrap.wrap(rawEvent);
    // dumpEvent( event );

    int act = event.getAction();
    int action = act & MotionEvent.ACTION_MASK;
    int id = 0;

    if (action == MotionEvent.ACTION_POINTER_DOWN) {
      return true;
    } else if ( action == MotionEvent.ACTION_POINTER_UP) {
      int np = event.getPointerCount();
      if ( np > 2 ) return true;
      /* fall through */
    }

    if (action == MotionEvent.ACTION_DOWN) {

    // ---------------------------------------- MOVE
    } else if ( action == MotionEvent.ACTION_MOVE ) {

    // ---------------------------------------- UP
    } else if (action == MotionEvent.ACTION_UP) {
      float x_canvas = event.getX(id);
      float y_canvas = event.getY(id);
      int pos = mDrawingSurface.getIndex( (int)x_canvas, (int)y_canvas );
      // Log.v(TAG, "USER tap at pos " + pos );
      if ( pos ==  0 ) pos =  1; // start
      if ( pos == 33 ) pos = 32; // home
      // Log.v( TAG, "onTouch() " + pos + " X-Y " + x_canvas + " " + y_canvas + " state " + mState.toString() );
      // if ( mState.isHold() && mDisclosureIndex >= 0 ) {
      //   if ( pos == mDisclosure[ mDisclosureIndex ] ) {
      //     mDisclosureIndex ++;
      //     if ( mDisclosureIndex == 1 ) {
      //       Toast.makeText( this, "One tap to temporary full mode", Toast.LENGTH_SHORT).show();
      //     } else if ( mDisclosureIndex == 2 ) {
      //       YutnoriPrefs.mDisclosed = true;
      //       Toast.makeText( this, "Temporary enabled full mode", Toast.LENGTH_SHORT).show();
      //     } else if ( mDisclosureIndex == mDisclosure.length ) {
      //       YutnoriPrefs.setDisclosed( mPrefs );
      //       Toast.makeText( this, "Permanently enabled full mode", Toast.LENGTH_SHORT).show();
      //       mDisclosureIndex = -1;
      //     }
      //   } else {
      //     mDisclosureIndex = -1;
      //   }
      // }
      if ( mPlaying ) {
        int state = -1;
        state = State.checkSkip( mPlayer, mState, mMoves, true, this );
        // Log.v( TAG, "USER check skip - state " + State.toString(state) );

        if ( state >= 0 ) {
          Toast.makeText( this, R.string.skipping, Toast.LENGTH_SHORT ).show();
          Delay.sleep( sleepBeforeAndroid );
        } else { // not skipped turn
          // String old_state = mState.toString();
          if ( mState.isMove() ) {
            if ( playMove( pos, (int)x_canvas, (int)y_canvas ) ) return true;
          } else if ( mState.isToStart() ) {
            if ( playToStart( (int)x_canvas, (int)y_canvas ) ) return true;
          } else if ( mState.isStart() ) {
            playStart( (int)x_canvas, (int)y_canvas );
          } else if ( mState.isThrowOrSkip() ) {
            playThrowOrSkip( (int)x_canvas, (int)y_canvas );
          }
        }
        // Log.v( TAG, "onTouch() UP. State " + old_state + " -> " + mState.toString() );
        setTheTitle();
        if ( mState.isReady() ) {
          mPlaying = false;
          if ( mConnected ) {
            mConnection.sendDone();
          } else {
            new PlayAndroid( this ).execute();
          }
          // if ( mBoard.winner() != 0 ) {
          //   mState.setOver();
          //   alertWinner();
          // }
        } else if ( mState.isOver() ) {
          alertWinner();
        }
      }
    }
    return true;
  }

  // @param pawns   nr of pawns to move - 0: all the pawns
  public void sendMyMove( int k, int from, int to, int pawns )
  {
    if ( mConnected ) {
      // Log.v("Yutnori-EXEC", "send my move " + k + " from " + from + " to " + to );
      mConnection.sendMoved( k ); 
      mConnection.sendMove( from, to, pawns );
    }
  }

  public void sendMySkip( boolean clear )
  {
    if ( mConnected ) {
      // Log.v("Yutnori", "send my skip " + clear );
      mConnection.sendSkip( clear? 1 : 0 ); 
    }
  }

  private boolean playMove( int pos, int x, int y )
  {
    int state = mBoard.checkBackDo( mPlayer, mState, mMoves, true, this );
    // Log.v( TAG, "USER play move: " + mBoard.name() + " check back-do - state " + State.toString(state) );

    if ( state == State.TO_START ) {
      // Toast.makeText( this, "to start", Toast.LENGTH_SHORT ).show();
      mStartPos = 2;
      mDrawingSurface.setHighlight( mStartPos );
      Delay.sleep( sleepAfterHighlight );
      mDrawingSurface.resetPawnNr();
      mMoving = true;
      mState.setToStart(); 
      return true;
    } else if ( state >= 0 ) {
      if ( mState.isSkip() ) {
        Toast.makeText( this, R.string.nomove, Toast.LENGTH_SHORT ).show();
        mState.setReady();
      }
    } else { // normal move
      if ( YutnoriPrefs.mSplitGroup && mDrawingSurface.isShowingPawnNrs() ) {
        int nr = mDrawingSurface.getPawnNr( x, y );
        // Log.v( TAG, "Play got pawns " + nr );
        if ( nr >= 1 ) {
          myPawnNr = nr;
          mDrawingSurface.pressedPawn = myPawnNr - 1;
        }
        if ( mConnected ) mConnection.sendPawns( myPawnNr );
        mDrawingSurface.setHighlight( mStartPos );
        Delay.sleep( sleepAfterHighlight );
        mDrawingSurface.resetPawnNr();
        return true;
      }
      if ( mMoving && ( pos > 1 || YutnoriPrefs.isSpecial() ) ) {
        if ( YutnoriPrefs.mSplitGroup && pos == mStartPos ) {
          int nr = mBoard.value( pos );
          // Log.v( TAG, "Play set pawn nr " + nr + " pos " + pos );
          if ( nr > 1 ) {
            mDrawingSurface.setPawnNr( nr, pos );
            return true;
          }
        }

        int m = mBoard.posDifference( mStartPos, pos );
        // Log.v(TAG, "Play try to move: from " + mStartPos + " to " + pos + " board diff " + m );
        // mMoves.print();

        int k = -1;
        if ( m > Board.HOME ) { // going home
          m -= Board.HOME; 
          k = mMoves.minMove( m );
        } else if ( m < Board.START ) { // going start
          m -= Board.START; // move to range -1 .. -4
          k = mMoves.hasMove( m );
        } else {
          k = mMoves.hasMove( m );
        }
        // Log.v( TAG, "Play pos-diff " + pos + "-" + mStartPos + " " + m + " move-index " + k );
        if ( k >= 0 && mYutnori.canMove( mStartPos, pos, m ) ) {
          sendMyMove( k, mStartPos, pos, myPawnNr );
          mMoves.shift( k );
          if ( mBoard.doMove( mStartPos, pos, mPlayer, myPawnNr ) ) { 
            // Log.v(TAG, "play-move new state THROW" );
            mState.setThrow();
            // mDrawingSurface.refresh(); // this is a race cond with drawing thread
          } else if ( mBoard.winner() != 0 ) {
            mState.setOver();
            yourPawnNr = 0;
            if ( mConnected ) mConnection.sendDone();
          } else {
            mState.setMoveOrReady( mMoves.size() > 0 );
          }
        } else {
          // Log.v( TAG, "Play cannot move " + m );
        }
        mStartPos = -1;
        mDrawingSurface.setHighlight( mStartPos );
        mMoving = false;
        if ( mConnected ) mConnection.sendHighlight( -1 );
      } else if ( pos >= 1 && pos <= 31) {
        // Log.v( TAG, "Play pos " + pos + " board " + mBoard.value(pos) );
        if ( (pos == 1 && mBoard.start(pos) > 0 ) || (pos > 1 && mBoard.value( pos ) > 0 ) ) {
          mStartPos = pos;
          myPawnNr = mBoard.value( pos );
          mDrawingSurface.setHighlight( mStartPos );
          if ( mConnected ) mConnection.sendHighlight( mStartPos );
          mMoving = true;
        }
      }
    }
    return false;
  }

  private boolean playToStart( int x, int y )
  {
    // Log.v( TAG, "USER playToStart");
    if ( YutnoriPrefs.mSplitGroup && mDrawingSurface.isShowingPawnNrs() ) {
      int nr = mDrawingSurface.getPawnNr( x, y );
      // Log.v( TAG, "Play got pawns " + nr );
      if ( nr >= 1 ) {
        myPawnNr = nr;
        mDrawingSurface.pressedPawn = myPawnNr - 1;
      }
      if ( mConnected ) mConnection.sendPawns( myPawnNr );
      mDrawingSurface.setHighlight( mStartPos );
      Delay.sleep( sleepAfterHighlight );
      mDrawingSurface.resetPawnNr();
      return true;
    }
    // if ( pos < 1 ) pos = 1;
    int pos = 1;
    int m = mBoard.posDifference( mStartPos, pos );
    // Log.v(TAG, "Play try to move to start: from " + mStartPos + " to " + pos + " board diff " + m );
    // mMoves.print();

    int k = -1;
    if ( m < Board.START ) { // going start
      m -= Board.START; // move to range -1 .. -4
      k = mMoves.hasMove( m );
    }
    // Log.v( TAG, "Play pos-diff " + pos + "-" + mStartPos + " " + m + " move-index " + k );
    if ( k >= 0 && mYutnori.canMove( mStartPos, pos, m ) ) {
      sendMyMove( k, mStartPos, pos, myPawnNr );
      mMoves.shift( k );
      mBoard.doMove( mStartPos, pos, mPlayer, myPawnNr );
      mState.setReady();
    }
    return false;
  }

  private void playStart( int x, int y )
  {
    // Log.v( TAG, "USER playStart");
    if ( mDrawingSurface.isThrow( x, y ) ) {
      mThisStart = Yutnori.throwYut( );
      mMoves.add( mThisStart );
      if ( mConnected ) {
        // Log.v("Yutnori-EXEC", "send start " + mThisStart );
        mConnection.sendStart( mThisStart );
      }
      compareStarts( State.WAIT, State.WAIT );
    }
  }

  private void playThrowOrSkip( int x, int y )
  {
    // Log.v( TAG, "USER playThrowOrSkip player " + mPlayer );
    if ( mDrawingSurface.isThrow( x, y ) ) {
      int m = Yutnori.throwYut( );
      // Log.v( TAG, "  move " + m );
      if ( mConnected ) mConnection.sendThrow( m );
      if ( YutnoriPrefs.isSpecial() ) {
        if ( Moves.getMoveValue( m, false ) == -1 ) {
          if ( mMoves.hasAllSkips() ) {
            if ( mBoard.countPlayer( mPlayer ) == 0 ) {
              // Log.v(TAG, "moves has all skip - empty board ");
              // mMySkip ++;
              if ( YutnoriPrefs.isSeoulOrBusan() ) {
                mMoves.add( m );
                mState.setMove();
              } else if ( YutnoriPrefs.isDoNone() ) {
                mMoves.addAndRevert( m, true );
                mState.setMove();
              } else if ( YutnoriPrefs.isDoSkip() ) {
                mMoves.add( m );
                State.setSkipping( mPlayer );
                // mState.setReady();
                mState.setMove();
              } else if ( YutnoriPrefs.isDoSpot() ) {
                // mMoves.add( m );    // this is to skip
                // mState.setReady();
                mMoves.addAndRevert( m, true ); // this is to revert and move as normal do
                mState.setMove();
              } else if ( YutnoriPrefs.isDoCage() ) {
                mMoves.add( m );
                mState.setMove();
              }
            } else if ( mBoard.hasPlayerOnlyAtStation( mPlayer, 2 ) ) {
              // Log.v(TAG, "moves has all skip - mals only at DO " );
              mMoves.add( m );
              if ( YutnoriPrefs.isSeoulOrBusan() ) {
              } else if ( YutnoriPrefs.isDoNone() ) {
              } else if ( YutnoriPrefs.isDoSkip() ) {
                State.setSkipping( mPlayer );
              } else if ( YutnoriPrefs.isDoSpot() ) {
              } else if ( YutnoriPrefs.isDoCage() ) {
              }
              mState.setMove();
            } else {
              // Log.v(TAG, "moves has all skip - mals on board");
              mMoves.add( m );
              mState.setMove();
            }
          } else {
            // Log.v(TAG, "moves has some no-skip" );
            mMoves.add( m );
            mState.setMove();
          }
        } else {
          mMoves.add( m );
          mState.setMoveOrThrow( ! Moves.rethrow(m) ); // Math.abs(m) < 4 );
        }
      } else {
        mMoves.add( m );
        mState.setMoveOrThrow( ! Moves.rethrow(m) ); // Math.abs(m) < 4 );
      }
      // mMoves.print( "USER" );
    }
  }

  // ------------------------------------------------------------------------

  // @Override
  // public boolean onLongClick( View view ) 
  // {
  //   Button b = (Button)view;
  //   return true;
  // }

  // @Override
  // public void onClick(View view)
  // {
  //   Button b = (Button)view;
  // }

  // ------------------------------------------------------------------------

  // @Override
  // public boolean onSearchRequested()
  // {
  //   // Intent intent = new Intent( this, TopoDroidPreferences.class );
  //   // intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_PLOT );
  //   // startActivity( intent );
  //   return true;
  // }

  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        // super.onBackPressed();
        askExit();
        return true;
      case KeyEvent.KEYCODE_SEARCH:
      // case KeyEvent.KEYCODE_MENU:   // HARDWRAE MENU (82)
        return onSearchRequested();
      case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        // Log.v( TAG, "key down: code " + code );
    }
    return false;
  }

  void askExit()
  {
    Resources res = getResources();
    String title = res.getString( R.string.ask_quit );
    String ok = res.getString( R.string.button_ok );
    String no = res.getString( R.string.button_cancel );
    dismissAlert();
    mAlert = new YutnoriAlertDialog( this, res, title, ok, no,
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          mExited = true;
          finish();
        }
    }, null );
  }

  // ------------------------------------------------------------------------

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    super.onCreateOptionsMenu( menu );
    mMenuNew      = menu.add( R.string.menu_new  );
    // mMenuYut      = menu.add( YUT2   );
    // mMenuJoin     = menu.add( R.string.menu_join );
    /* if ( YutnoriPrefs.mDisclosed ) */ mMenuSettings = menu.add( R.string.menu_settings );
    mMenuHelp     = menu.add( R.string.menu_help );
    mMenuAbout    = menu.add( R.string.menu_about );
    return true;
  }

  private void resetStatus( boolean playing )
  {
    mPlaying = playing;
    mMoving  = false;
    Board board = null;
    switch ( YutnoriPrefs.mSpecialRule ) {
      case YutnoriPrefs.NONE:  board = new Board(); break;
      case YutnoriPrefs.SEOUL:
      case YutnoriPrefs.BUSAN: board = new BoardSeoul(); break;
      case YutnoriPrefs.BACKDO: 
        switch ( YutnoriPrefs.mBackDo ) {
          case YutnoriPrefs.DO_NONE: board = new BoardDoNone(); break;
          case YutnoriPrefs.DO_SKIP: board = new BoardDoSkip(); break;
          case YutnoriPrefs.DO_SPOT: board = new BoardDoSpot(); break;
          case YutnoriPrefs.DO_CAGE: board = new BoardDoCage(); break;
        }
    }
    if ( board != null && board != mBoard ) {
      mBoard = board;
      mDrawingSurface.setBoard( mBoard );
      mYutnori.setBoard( mBoard );
    } 
    // Log.v( TAG, "reset status " + YutnoriPrefs.mSpecialRule + " " + YutnoriPrefs.mBackDo + " " + playing + " board " + mBoard.name() );
    mBoard.reset();
    mMoves.clear();
    
  }

  void setEngine( int engine ) { mYutnori.setEngine( engine ); }

  void startHelp()
  {
    startActivity( new Intent( Intent.ACTION_VIEW ).setClass( this, ManualDialog.class ) );
  }

  void doNewGame()
  {
    resetStatus( true );
    YutnoriPrefs.doPos = true;
    if ( mConnected ) {
      mConnection.sendNewGame( );
    } else {
      setEngine( YutnoriPrefs.mEngine );
      mDrawingSurface.resetPawns();
      execReset( State.THROW );
      alertNumber( R.string.color_none );
    }
    setTheTitle();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    if ( item == mMenuNew ) {
      // if ( YutnoriPrefs.isSpecial() ) {
        (new NewGameDialog( this, this, getResources().getString(R.string.special_rules) ) ).show();
      // } else {
      //   doNewGame();
      // }
     
    // } else if ( item == mMenuYut ) {
    //   if ( mYutnori.mStrategy == mStrategy1 ) {
    //     mMenuYut.setTitle( YUT1 );
    //     // mStrategyString = YUT2;
    //     mYutnori.mStrategy = mStrategy2;
    //   } else {
    //     mMenuYut.setTitle( YUT2 );
    //     // mStrategyString = YUT1;
    //     mYutnori.mStrategy = mStrategy1;
    //   }
    // } else if ( item == mMenuJoin ) {
    //   mConnectDialog = new ConnectDialog( this, this );
    //   mConnectDialog.show();
      
    } else if ( item == mMenuSettings /* && YutnoriPrefs.mDisclosed */ ) {
      startActivity( new Intent( this, YutnoriPreferences.class ) );
    } else if ( item == mMenuHelp ) {
      startHelp();
    } else if ( item == mMenuAbout ) {
      String version = "";
      int version_code = 0;
      try {
        version = getPackageManager().getPackageInfo( getPackageName(), 0 ).versionName;
        version_code = getPackageManager().getPackageInfo( getPackageName(), 0 ).versionCode;
      } catch ( NameNotFoundException e ) { }
      new AboutDialog( this, version, version_code ).show();
    }
    return true;
  }

  // ----------------------------------------------------------------------------------
  private void compareStarts( int state1, int state2 )
  {
    int me    = mThisStart % 10;
    int other = mOtherStart % 10;
    // Log.v("Yutnori-EXEC", "compare " + me + " " + other );
    if ( me >= 0 && other >= 0 ) {
      Delay.sleep( sleepBeforeCompareStarts );
      String str = null;
      if ( me > other ) {
        str = getResources().getString(R.string.you_start);
        mConnection.sendBackDo( YutnoriPrefs.mSpecialRule, YutnoriPrefs.mBackDo, YutnoriPrefs.mBackYuts );
        mState.setThrow();
      } else if ( me < other ) {
        if ( mConnected ) {
          str = getResources().getString(R.string.friend_start);
        } else {
          str = getResources().getString(R.string.i_start);
        }
        mState.setReady();
      } else { 
        mState.setState( state1 );
      }
      if ( str != null ) {
        Resources res = getResources();
        String ok = res.getString( R.string.button_ok );
        dismissAlert();
        mAlert = new YutnoriAlertDialog( this, res, str, ok, null, null, null );
      }
      resetStarts();
      mMoves.clear();
    } else {
      mState.setState( state2 );
    }
    setTheTitle();
  }

  private void alertNumber( int str_nr )
  {
    Resources res = getResources();
    String str = res.getString( str_nr );
    String ok  = res.getString( R.string.button_ok );
    dismissAlert();
    mAlert = new YutnoriAlertDialog( this, res, str, ok, null, null, null );
  }

  private void alertString( String str )
  {
    Resources res = getResources();
    String ok  = res.getString( R.string.button_ok );
    dismissAlert();
    mAlert = new YutnoriAlertDialog( this, res, str, ok, null, null, null );
  }

  void execPawns( int nr ) 
  {
    yourPawnNr = nr;
    mDrawingSurface.pressedPawn = yourPawnNr - 1;
    Delay.sleep( sleepAfterExecPawns );
    mDrawingSurface.resetPawnNr();
  }

  void execNewGame( int pos )
  {
    YutnoriPrefs.setPos( pos );
    YutnoriPrefs.doPos = true;

    Resources res = getResources();
    String title = String.format( res.getString( R.string.ask_new_game, mRemote ) );
    String ok = res.getString( R.string.accept );
    String no = res.getString( R.string.decline );
    dismissAlert();
    mAlert = new YutnoriAlertDialog( this, res, title, ok, no, 
      new DialogInterface.OnClickListener() {
        @Override public void onClick( DialogInterface dialog, int btn ) { doAcceptNewGame( ); }
      },
      new DialogInterface.OnClickListener() {
        @Override public void onClick( DialogInterface dialog, int btn ) { doDeclineNewGame( ); }
      }
    );
  }

  void execOkGame( int bool )
  {
    Resources res = getResources();
    String str;
    String ok = res.getString( R.string.button_ok );
    if ( bool == 1 ) {
      // startNewGame();
      str = String.format( res.getString( R.string.another_game ), mRemote );
    } else {
      // disconnectRemoteYutnori();
      str = String.format( res.getString( R.string.not_another_game ), mRemote );
    }
    dismissAlert();
    mAlert = new YutnoriAlertDialog( this, res, str, ok, null, null, null );
  }

  void doDeclineNewGame()
  {
    mConnection.sendOkGame( 0 );
    // disconnectRemoteYutnori(); // let the peer disconnect
  }

  void doAcceptNewGame()
  {
    mConnection.sendOkGame( 1 );
    startNewGame();
  }

  private void startNewGame()
  {
    int off = 0;
    if ( mLastWinner == 0 ) {
      mState.setWait();
      if ( mDrawingSurface.mColor == 1 ) off = 1;
      mDrawingSurface.setStartColor( off );
      mConnection.sendReset( State.START );
      execReset( State.WAIT );
      mConnection.sendOffset( off );
    } else if ( mLastWinner == 1 ) {
      // mState.setThrow();
      mConnection.sendReset( State.READY );
      execReset( State.THROW );
    } else { // mLastWinner == -1
      // mState.setReady();
      mConnection.sendReset( State.THROW );
      execReset( State.READY );
    }
  }

  // ======================================================================================

  static int getConnectionType() { return ( mConnection == null )? SyncService.STATE_NONE : mConnection.getType(); }
  static int getAcceptState()    { return ( mConnection == null )? SyncService.STATE_NONE : mConnection.getAcceptState(); }
  static int getConnectState()   { return ( mConnection == null )? SyncService.STATE_NONE : mConnection.getConnectState(); }
  static String getConnectionStateStr()      { return ( mConnection == null )? NONE: mConnection.getConnectStateStr(); }
  static String getConnectedDeviceName()     { return ( mConnection == null )? null : mConnection.getConnectedDeviceName(); }
  static String getConnectionStateTitleStr() { return ( mConnection == null )? EMPTY : mConnection.getConnectionStateTitleStr(); }

  static void syncRemoteYutnori( BluetoothDevice device ) 
  {
    if ( mConnection != null ) {
      mConnection.syncDevice( device );
      // sync TITO settings
      mConnection.sendBackDo( YutnoriPrefs.mSpecialRule, YutnoriPrefs.mBackDo, YutnoriPrefs.mBackYuts );
      YutnoriPrefs.mTiToFreeze = true;
    }
  }

  void closeConnectDialog()
  {
    if ( mConnectDialog != null ) {
      mConnectDialog.dismiss();
      mConnectDialog = null;
    }
  }

  void closeSplashDialog() {
    if ( mSplashDialog != null ) {
      mSplashDialog.dismiss();
      mSplashDialog = null;
    }
  }

  void connectRemoteYutnori( BluetoothDevice device )
  {
    // Log.v( TAG, "Main: connect remote device " + device.getName() );
    mLastWinner = 0;
    if ( mConnection != null ) {
      mJoining = true;
      mRemote  = device.getName();
      mConnection.connect( device ); 
      // Log.v( "Yutnori-EXEC", "connect remote device " + mRemote + " send rules");
      // mConnection.sendBackDo( YutnoriPrefs.mSpecialRule, YutnoriPrefs.mBackDo, YutnoriPrefs.mBackYuts );
      // mConnection.syncDevice( device );
      resetStatus( false );
      mState.setWait();
      setTheTitle();
      YutnoriPrefs.mTiToFreeze = true;
    }
  }

  void disconnectRemoteYutnori( /* BluetoothDevice device */ )
  {
    mConnected = false;
    mJoining   = false;
    if ( mConnection != null ) mConnection.disconnect( /* device */ );
    mRemote  = null;
    mDrawingSurface.resetPawns();
    resetStatus( true );
    mState.setThrow();
    setTheTitle();
    alertNumber( R.string.color_none );
    YutnoriPrefs.mTiToFreeze = false;
  }

  void startRemote( ) { if ( mConnection != null ) mConnection.startSyncService( this ); }

  void stopRemote( )  
  { 
    mRemote  = null;
    mPlaying = mState.isPlaying();
    if ( mConnection != null ) mConnection.stop( );
  }

  void restartRemote()
  {
    stopRemote();
    startRemote();
  }

  void syncConnectionFailed()
  {
    // Log.v( TAG, "sync connection failed ");
    // Toast.makeText( this, R.string.sync_conn_fail, Toast.LENGTH_SHORT ).show();
    mRemote  = null;
    mDrawingSurface.resetPawns();
    resetStatus( true );
    mState.setThrow();
    alertNumber( R.string.sync_conn_fail );
    YutnoriPrefs.mTiToFreeze = false;
  }

  void syncConnectedDevice( String name )
  {
    mDrawingSurface.setStartColor( 0 );
    mRemote = name;
    mLastWinner = 0;
    // Log.v( TAG, "sync connected device " + name + " joining " + mJoining + " connected " + mConnected );
    // Toast.makeText( this, String.format( getResources().getString( R.string.sync_conn ), name ), Toast.LENGTH_SHORT ).show();
    if ( mConnection != null ) {
      mConnected = true;
      if ( ! mJoining ) {
        Resources res = getResources();
        String title = String.format( res.getString( R.string.ask_accept, mRemote ) );
        String ok = res.getString( R.string.accept );
        String no = res.getString( R.string.decline );
        closeConnectDialog();
        closeSplashDialog();
        dismissAlert();
        mAlert = new YutnoriAlertDialog( this, res, title, ok, no, 
          new DialogInterface.OnClickListener() {
            @Override public void onClick( DialogInterface dialog, int btn ) { doAcceptConnect( mRemote ); }
          },
          new DialogInterface.OnClickListener() {
            @Override public void onClick( DialogInterface dialog, int btn ) { doDeclineConnect(); }
          }
        );
      }
    } else {
      execReset( State.THROW );
    }
  }

  void doAcceptConnect( String name )
  {
    // mDrawingSurface.setStartColor( 0 );
    execReset( State.START );
    alertString( String.format( getResources().getString( R.string.color_blue ), name ) );
    mConnection.sendAccept( 1, YutnoriPrefs.mPos );
  }

  void doDeclineConnect( )
  {
    mConnection.sendAccept( 0, 0 );
  }

  void execColor( int offset )
  {
    // Log.v( TAG, "exec color " + offset );
    mDrawingSurface.setStartColor( offset );
  }

  // called by connectionLost()
  // and on receiving a sendAccept
  void execAccept( int bool, int pos )
  {
    // Log.v( TAG, "exec Accept " + bool );
    if ( mJoining ) {
      mJoining = false;
      Resources res = getResources();
      if ( bool == 1 ) {
        YutnoriPrefs.setPos( pos );
        YutnoriPrefs.mTiToFreeze = true;
        mDrawingSurface.setPawns( -1 ); // color = -1
        mConnection.sendReset( State.START );
        execReset( State.WAIT );
        alertString( String.format( getResources().getString(R.string.color_red), mRemote ) );
      } else {
        mConnection.sendAccept( 0, 0 );
        mConnected = false;
        YutnoriPrefs.mTiToFreeze = false;
        // if ( mConnection != null ) mConnection.disconnect( /* null */ );
        alertString( String.format( getResources().getString(R.string.connection_refused), mRemote ) );
        mRemote  = null;
        restartRemote(); // FIXME if connection has been refused it is safe to restart the sync-service
      }
    } else {
      mConnected = false;
      YutnoriPrefs.mTiToFreeze = false;
      if ( mConnection != null ) mConnection.disconnect( /* null */ );
      mRemote  = null;
      alertNumber( R.string.color_none );
    }
  }

  void execSetBackDo( int special, int backdo, int backyuts ) 
  {
    // Log.v("Yutnori-EXEC", "set special " + special + " backdo " + backdo + " Back-yuts " + backyuts );
    YutnoriPrefs.setSpecial( special );
    YutnoriPrefs.mBackDo       = backdo;
    YutnoriPrefs.mBackYuts     = backyuts;
    YutnoriPrefs.mTiToFreeze   = true;
  }

  void execSkip( int clear )
  {
    // Log.v("Yutnori-EXEC", "skip " + clear );
    if ( clear != 0 ) mMoves.clear();
    Toast.makeText( this, R.string.friend_skipping, Toast.LENGTH_LONG ).show();
    Delay.sleep( sleepAndroid );
  }

  void execReset( int state )
  {
    // Log.v( TAG, "exec RESET state " + state );
    resetStatus( true );
    mState.setState( state );
    resetStarts();
    setTheTitle();
  }

  void execStart( int move ) 
  {
    // Log.v("Yutnori-EXEC", "other start " + move );
    mOtherStart = move;
    mMoves.add( mOtherStart );
    compareStarts( State.START, State.START );
  }

  void execThrow( int move ) { mMoves.add( move ); }
  void execMove( int from, int to, int pawns ) 
  { 
    // Log.v("Yutnori-EXEC", "move " + pawns + " from " + from + " to " + to );
    mBoard.doMove( from, to, Player.ANDROID, pawns );
    mDrawingSurface.addPosition( to );
  }
  void execMoved( int index ) 
  { 
    // Log.v("Yutnori-EXEC", "moved " + index );
    mMoves.shift( index );
  }

  void execHighlight( int pos )     
  { 
    if ( pos == 1 ) pos = 0;
    mDrawingSurface.setHighlight( pos );
  }
  void execDone()                   
  {
    if ( mBoard.winner() != 0 ) {
      mState.setOver();
      mPlaying = false;
    } else {
      mState.setThrow();
      mPlaying = true;
    }
    setTheTitle();
    if ( mBoard.winner() != 0 ) alertWinner();
  }

  private void connectionLost()
  {
    // Log.v( TAG, "connection lost");
    if ( mConnected ) {
      if ( mJoining ) {
        execAccept( 0, 0 );
        mJoining = false;
      } else {
        mConnected = false;
        if ( mConnection != null ) mConnection.disconnect( /* null */ );
        Toast.makeText( this, R.string.sync_conn_lost, Toast.LENGTH_SHORT ).show();
      }
    }
    mRemote  = null;
    mState.setOver();
    // mPlaying = false;
  }

  void connStateChanged( int connect_state ) 
  {
    // Log.v( TAG, "connection state changed to " + connect_state );
    switch ( connect_state ) {
      case 10: // connect: none
        connectionLost();
        YutnoriPrefs.mTiToFreeze = false;
        break;
      case 12: // connect: connecting
        YutnoriPrefs.mTiToFreeze = true;
        return;
      case 13: // connect: connected
        // already handled by syncConnectedDevice
        YutnoriPrefs.mTiToFreeze = true;
        return;
      case 50: // connection lost
      case 51: // connection reset
        connectionLost();
        YutnoriPrefs.mTiToFreeze = false;
        break;
      case 60: // connection failed
        // already handled by syncConnectionFailed
        // Toast.makeText( this, R.string.sync_conn_failed, Toast.LENGTH_SHORT ).show();
        YutnoriPrefs.mTiToFreeze = false;
        break;
      case 70: // accept: none
        break;
      case 71: // accept: listen
        break;
    }
    setTheTitle(); 
  } 

}
