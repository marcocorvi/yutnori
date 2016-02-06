/* @file Main.java
 *
 * @author marco corvi
 * @date nov 2015
 *
 * @brief Yutnory main drawing activity
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
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

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
{
  final static String TAG = "yutnori";

  final static int sleepAfterHighlight      =  5;
  final static int sleepBeforeCompareStarts = 20;
  final static int sleepAfterExecPawns      =  5;
  final static int sleepAfterAndroid        =  5;
  final static int sleepAndroid             =  5;
  final static int sleepBeforeAndroid       =  5;

  final static int USER = 1;
  final static int ANDROID = -1;

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
  private MenuItem mMenuJoin     = null;
  private MenuItem mMenuSettings = null;
  private MenuItem mMenuHelp     = null;
  private MenuItem mMenuAbout    = null;

  ConnectDialog mConnectDialog = null;
  SplashDialog  mSplashDialog = null;

  Yutnori  mYutnori   = null;
  Strategy mStrategy1 = null;
  Strategy mStrategy2 = null;
  Board mBoard;
  Moves mMoves;
  int myPawnNr = 0;
  int yourPawnNr = 0;

  String  mRemote  = null;     // name of the other yutnori
  boolean mPlaying = false;    // whether it is playing
  boolean mExited  = false;    // whether this yutnori has exited
  boolean mJoining = false;    // whether i am joinint the other yutnori
  boolean mConnected = false;  // whether i am connected to the other yutnori

  DrawingSurface mDrawingSurface;

  BluetoothAdapter  mBTAdapter  = null;
  ConnectionHandler mConnection = null;

  int mThisStart  = -1; // to decide who starts
  int mOtherStart = -1;
  int mLastWinner =  0; // 1: you, -1 your friend/Android

  private void resetStarts()
  {
    mThisStart  = -1;
    mOtherStart = -1;
  }

  private int[] mDisclosure;
  private int mDisclosureIndex = 0;

  static final int THROW = 0;
  static final int MOVE  = 1;
  static final int READY = 2;
  static final int OVER  = 3;
  static final int START = 4;
  static final int WAIT  = 5;    // wait at start
  static final int HOLD  = 6;    // wait for a friend to join
  static final int NR_STATE = 7; // number of states
  static String mStateStr[];  // = { "THROW", "MOVE", "WAIT", "OVER", "THROW", "WAIT", "HOLD" };
  int mState = THROW;
  boolean mMoving = false;

  // private String mStrategyString = EMPTY;

  private class PlayAndroid extends AsyncTask< Void, Integer, Void >
  {
    @Override
    protected Void doInBackground( Void... v )
    {
      Delay.sleep( sleepBeforeAndroid );
      mYutnori.playOnce( sleepAndroid );
      // Delay.sleep( sleepAfterAndroid );
      // Log.v( TAG, "PlayAndroid done" );
      return null;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) { }

    @Override
    protected void onPostExecute( Void v ) {
      mState = (mBoard.winner() == 0)? THROW : OVER;
      // Log.v( TAG, "PlayAndroid on Post Exec: state " + mStateStr[ mState ] );
      setTheTitle( );
      if ( mState == OVER ) alertWinner();
      mPlaying = true;
    }
  };

  protected void setTheTitle( )
  {
    if ( mExited ) return;
    // Log.v( TAG, "setTheTitle state " + mState + " " + mStateStr[ mState ] + " Remote " + mRemote );
    mLastWinner = mBoard.winner();
    StringBuilder sb = new StringBuilder();
    sb.append( " Y U T N O R I " );
    if ( mRemote == null ) {
      // sb.append( mStrategyString );
    } else {
      sb.append( "[" + mRemote + "]" );
    }

    if ( mLastWinner == 1 ) {
      mState = OVER;
      sb.append( " -- " + getResources().getString( R.string.you_won ) );
    } else if ( mLastWinner == -1 ) {
      mState = OVER;
    }
    if ( mState >= 0 && mState < NR_STATE ) sb.append( " " + mStateStr[ mState ] );
    setTitle( sb.toString() );
    setTitleColor( ( mState == READY )? 0xffff0000 : 0xff000000 );
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
      Resources res = getResources();
      String quit  = res.getString( R.string.button_quit );
      String again = res.getString( R.string.button_again );
      dismissAlert();
      mAlert = new YutnoriAlertDialog( this, res, str, quit, again,
          new DialogInterface.OnClickListener() {
            @Override public void onClick( DialogInterface dialog, int btn ) { askExit(); }
          },
          new DialogInterface.OnClickListener() {
            @Override public void onClick( DialogInterface dialog, int btn ) { doNewGame(); }
          }
      );
    }
  }

  public void onSharedPreferenceChanged( SharedPreferences sp, String k ) 
  {
    YutnoriPrefs.check( sp, k, this );
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

    mStateStr = new String[ NR_STATE ];
    mStateStr[0] = getResources().getString( R.string.state_throw );
    mStateStr[1] = getResources().getString( R.string.state_move  );
    mStateStr[2] = getResources().getString( R.string.state_ready );
    mStateStr[3] = getResources().getString( R.string.state_over  );
    mStateStr[4] = getResources().getString( R.string.state_start_throw );
    mStateStr[5] = getResources().getString( R.string.state_start_ready );
    mStateStr[6] = getResources().getString( R.string.state_hold );

    // Display display = getWindowManager().getDefaultDisplay();
    // DisplayMetrics dm = new DisplayMetrics();
    // display.getMetrics( dm );
    // int width = dm widthPixels;
    mDrawingSurface.setDimensions(
      getResources().getDisplayMetrics().widthPixels,
      getResources().getDisplayMetrics().heightPixels );
    mDrawingSurface.setApp( this );

    // mIsNotMultitouch = ! getPackageManager().hasSystemFeature( PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH );

    mBoard = mDrawingSurface.mBoard;
    mMoves = mDrawingSurface.mMoves;
    mYutnori   = new Yutnori(   mBoard, mMoves );
    mStrategy1 = new Strategy1( mBoard, mMoves, mDrawingSurface, ANDROID ); // strategy plays for Android
    mStrategy2 = new Strategy2( mBoard, mMoves, mDrawingSurface, ANDROID );
    // mStrategyString = YUT1;
    setEngine( YutnoriPrefs.mEngine );

    mDrawingSurface.setOnTouchListener(this);

    mBTAdapter = BluetoothAdapter.getDefaultAdapter();
    mConnection = new ConnectionHandler( this );
    startRemote();

    setTheTitle();
    mRemote  = null;
    mPlaying = true;
    
    mSplashDialog = new SplashDialog( this, this );
    mSplashDialog.show();
    // alertNumber( R.string.color_none );

    mDisclosure = new int[10];
    for ( int k = 0; k<mDisclosure.length; ++ k ) {
      int v = 6 + 5 * ((int)( Math.random()*4.9999 ));
      if ( v > 21 ) v = 24;
      mDisclosure[k] = v;
      // Log.v( TAG, "Disclose " + k + " = " + v );
    }
    mDisclosureIndex = 0;
  }

  void setStartAs( int start )
  {
    switch ( start ) {
      case 0:
        mPlaying = true;
        mState   = THROW;
        setTheTitle();
        alertNumber( R.string.color_none );
        break;
      case 1:
        mPlaying = false;
        mState   = HOLD;
        setTheTitle();
        break;
      case 2:
        mPlaying = true;
        mState   = HOLD;
        setTheTitle();
        mConnectDialog = new ConnectDialog( this, this );
        mConnectDialog.show();
        break;
    }
  }

  int getConnectionType() { return ( mConnection == null )? SyncService.STATE_NONE : mConnection.getType(); }
  int getAcceptState()    { return ( mConnection == null )? SyncService.STATE_NONE : mConnection.getAcceptState(); }
  int getConnectState()   { return ( mConnection == null )? SyncService.STATE_NONE : mConnection.getConnectState(); }
  String getConnectionStateStr()      { return ( mConnection == null )? NONE: mConnection.getConnectStateStr(); }
  String getConnectedDeviceName()     { return ( mConnection == null )? null : mConnection.getConnectedDeviceName(); }
  String getConnectionStateTitleStr() { return ( mConnection == null )? EMPTY : mConnection.getConnectionStateTitleStr(); }

  void syncRemoteYutnori( BluetoothDevice device ) 
  {
    if ( mConnection != null ) mConnection.syncDevice( device );
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
      // Log.v( TAG, "connect remote device " + mRemote );
      resetStatus( false );
      mState = WAIT;
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
    mState = THROW;
    setTheTitle();
    alertNumber( R.string.color_none );
  }

  void startRemote( ) { if ( mConnection != null ) mConnection.startSyncService( this ); }
  void stopRemote( )  
  { 
    mRemote  = null;
    mPlaying = ( mState == THROW || mState == MOVE );
    if ( mConnection != null ) mConnection.stop( );
  }
  void restartRemote()
  {
    stopRemote();
    startRemote();
  }

  void syncConnectionFailed()
  {
    // Toast.makeText( this, R.string.sync_conn_fail, Toast.LENGTH_SHORT ).show();
    mRemote  = null;
    mDrawingSurface.resetPawns();
    resetStatus( true );
    mState = THROW;
    alertNumber( R.string.sync_conn_fail );
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
      execReset( THROW );
    }
  }

  void doAcceptConnect( String name )
  {
    // mDrawingSurface.setStartColor( 0 );
    execReset( START );
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
        mDrawingSurface.setPawns( -1 ); // color = -1
        mConnection.sendReset( START );
        execReset( WAIT );
        alertString( String.format( getResources().getString(R.string.color_red), mRemote ) );
      } else {
        mConnection.sendAccept( 0, 0 );
        mConnected = false;
        // if ( mConnection != null ) mConnection.disconnect( /* null */ );
        alertString( String.format( getResources().getString(R.string.connection_refused), mRemote ) );
        mRemote  = null;
        restartRemote(); // FIXME if connection has been refused it is safe to restart the sync-service
      }
    } else {
      mConnected = false;
      if ( mConnection != null ) mConnection.disconnect( /* null */ );
      mRemote  = null;
      alertNumber( R.string.color_none );
    }
  }

  void execReset( int state )
  {
    // Log.v( TAG, "exec RESET state " + state );
    resetStatus( true );
    mState = state;
    resetStarts();
    setTheTitle();
  }

  void execStart( int move ) 
  {
    mOtherStart = move;
    mMoves.add( mOtherStart );
    compareStarts( START, START );
  }

  void execThrow( int move ) { mMoves.add( move ); }
  void execMove( int from, int to ) 
  { 
    mBoard.move( from, to, ANDROID, yourPawnNr );
    mDrawingSurface.addPosition( to );
  }
  void execMoved( int index ) { mMoves.shift( index ); }
  void execHighlight( int pos )     
  { 
    if ( pos == 1 ) pos = 0;
    mDrawingSurface.setHighlight( pos );
  }
  void execDone()                   
  {
    if ( mBoard.winner() != 0 ) {
      mState   = OVER;
      mPlaying = false;
    } else {
      mState   = THROW;
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
    mState   = OVER;
    // mPlaying = false;
  }

  void connStateChanged( int connect_state ) 
  {
    // Log.v( TAG, "connection state changed to " + connect_state );
    switch ( connect_state ) {
      case 10: // connect: none
        connectionLost();
        break;
      case 12: // connect: connecting
        return;
      case 13: // connect: connected
        // already handled by syncConnectedDevice
        return;
      case 50: // connection lost
      case 51: // connection reset
        connectionLost();
        break;
      case 60: // connection failed
        // already handled by syncConnectionFailed
        break;
      case 70: // accept: none
        break;
      case 71: // accept: listen
        break;
    }
    setTheTitle(); 
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
      if ( pos ==  0 ) pos =  1;
      if ( pos == 33 ) pos = 32;
      // Log.v( TAG, "onTouch() " + pos + " X-Y " + x_canvas + " " + y_canvas + " state " + mState );
      if ( mState == HOLD && mDisclosureIndex >= 0 ) {
        if ( pos == mDisclosure[ mDisclosureIndex ] ) {
          mDisclosureIndex ++;
          if ( mDisclosureIndex == 3 ) {
            Toast.makeText( this, "One tap to temporary full mode", Toast.LENGTH_SHORT).show();
          } else if ( mDisclosureIndex == 4 ) {
            YutnoriPrefs.mDisclosed = true;
            Toast.makeText( this, "Temporary enabled full mode", Toast.LENGTH_SHORT).show();
          } else if ( mDisclosureIndex == mDisclosure.length ) {
            YutnoriPrefs.setDisclosed( mPrefs );
            Toast.makeText( this, "Permanently enabled full mode", Toast.LENGTH_SHORT).show();
            mDisclosureIndex = -1;
          }
        } else {
          mDisclosureIndex = -1;
        }
      }
      if ( mPlaying ) {
        int old_state = mState;
        if ( mState == MOVE ) {
          if ( YutnoriPrefs.mSplitGroup && mDrawingSurface.isShowingPawnNrs() ) {
            int nr = mDrawingSurface.getPawnNr( (int)x_canvas, (int)y_canvas );
            // Log.v( TAG, "got pawns " + nr );
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
          if ( mMoving && pos > 1 ) {
            if ( YutnoriPrefs.mSplitGroup && pos == mStartPos ) {
              int nr = mBoard.value( pos );
              if ( nr > 1 ) {
                // Log.v( TAG, "main set pawn nr " + nr + " pos " + pos );
                mDrawingSurface.setPawnNr( nr, pos );
                return true;
              }
            }
            // try to move:
            int m = Board.difference( mStartPos, pos );
            int k = -1;
            if ( m < 0 && m > -100 ) {
              m = -m;
              k = mMoves.minMove( m );
            } else {
              k = mMoves.hasMove( m );
            }
            // Log.v( TAG, "difference " + pos + "-" + mStartPos + " " + m + " move-index " + k );
            if ( k >= 0 && mYutnori.canMove( mStartPos, pos, m ) ) {
              if ( mConnected ) {
                mConnection.sendMoved( k ); 
                mConnection.sendMove( mStartPos, pos );
              }
              mMoves.shift( k );
              if ( mBoard.move( mStartPos, pos, USER, myPawnNr ) ) { 
                mState = THROW;
                mDrawingSurface.refresh();
              } else if ( mBoard.winner() != 0 ) {
                mState = OVER;
                yourPawnNr = 0;
                if ( mConnected ) mConnection.sendDone();
              } else {
                mState = ( mMoves.size() > 0 )? MOVE : READY;
              }
            }
            mStartPos = -1;
            mDrawingSurface.setHighlight( mStartPos );
            mMoving = false;
            if ( mConnected ) mConnection.sendHighlight( -1 );
          } else if ( pos >= 1 && pos <= 31) {
            // Log.v( TAG, "pos " + pos + " board " + mBoard.value(pos) );
            if ( (pos == 1 && mBoard.start(pos) > 0 ) 
              || (pos > 1 && mBoard.value( pos ) > 0 ) ) {
              mStartPos = pos;
              myPawnNr = mBoard.value( pos );
              mDrawingSurface.setHighlight( mStartPos );
              if ( mConnected ) mConnection.sendHighlight( mStartPos );
              mMoving = true;
            }
          }
        } else if ( mState == START ) {
          if ( mDrawingSurface.isThrow( (int)x_canvas, (int)y_canvas ) ) {
            mThisStart = Yutnori.throwYut();
            mMoves.add( mThisStart );
            if ( mConnected ) mConnection.sendStart( mThisStart );
            compareStarts( WAIT, WAIT );
          }
        } else if ( mState == THROW ) {
          if ( mDrawingSurface.isThrow( (int)x_canvas, (int)y_canvas ) ) {
            int m = Yutnori.throwYut();
            if ( mConnected ) mConnection.sendThrow( m );
            mMoves.add( m );
            if ( m <= 3 ) mState = MOVE;
          }
        }
        // Log.v( TAG, "onTouch() UP. State " + old_state + " -> " + mState );
        setTheTitle();
        if ( mState == READY ) {
          mPlaying = false;
          if ( mConnected ) {
            mConnection.sendDone();
          } else {
            new PlayAndroid().execute();
          }
          // if ( mBoard.winner() != 0 ) {
          //   mState = OVER;
          //   alertWinner();
          // }
        } else if ( mState == OVER ) {
          alertWinner();
        }
      }
    }
    return true;
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

  private void askExit()
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
    mMenuJoin     = menu.add( R.string.menu_join );
    if ( YutnoriPrefs.mDisclosed ) mMenuSettings = menu.add( R.string.menu_settings );
    mMenuHelp     = menu.add( R.string.menu_help );
    mMenuAbout    = menu.add( R.string.menu_about );
    return true;
  }

  private void resetStatus( boolean playing )
  {
    mPlaying = playing;
    mMoving  = false;
    mBoard.reset();
    mMoves.clear();
  }

  void setEngine( int engine ) // used by YutnoriPrefs
  {
    switch ( engine ) {
      case YutnoriPrefs.ENGINE_0:
        mYutnori.mStrategy = ( Math.random() < 0.5 )? mStrategy1 : mStrategy2;
        break;
      case YutnoriPrefs.ENGINE_1:
        mYutnori.mStrategy = mStrategy1;
        break;
      case YutnoriPrefs.ENGINE_2:
        mYutnori.mStrategy = mStrategy2;
        break;
    }
  }

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
      execReset( THROW );
      alertNumber( R.string.color_none );
    }
    setTheTitle();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    if ( item == mMenuNew ) {
      doNewGame();
     
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
    } else if ( item == mMenuJoin ) {
      mConnectDialog = new ConnectDialog( this, this );
      mConnectDialog.show();
      
    } else if ( item == mMenuSettings && YutnoriPrefs.mDisclosed ) {
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


  private void compareStarts( int state1, int state2 )
  {
    if ( mThisStart >= 0 && mOtherStart >= 0 ) {
      Delay.sleep( sleepBeforeCompareStarts );
      String str = null;
      if ( mThisStart > mOtherStart ) {
        str = getResources().getString(R.string.you_start);
        mState = THROW;
      } else if ( mThisStart < mOtherStart ) {
        if ( mConnected ) {
          str = getResources().getString(R.string.friend_start);
        } else {
          str = getResources().getString(R.string.i_start);
        }
        mState = READY;
      } else { 
        mState = state1;
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
      mState = state2;
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
      mState = WAIT;
      if ( mDrawingSurface.mColor == 1 ) off = 1;
      mDrawingSurface.setStartColor( off );
      mConnection.sendReset( START );
      execReset( WAIT );
      mConnection.sendOffset( off );
    } else if ( mLastWinner == 1 ) {
      // mState = THROW;
      mConnection.sendReset( READY );
      execReset( THROW );
    } else { // mLastWinner == -1
      // mState = READY;
      mConnection.sendReset( THROW );
      execReset( READY );
    }
  }

}
