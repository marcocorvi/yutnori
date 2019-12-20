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

import android.util.Log;

import android.content.res.Resources;

class State
{
  static final String TAG = "Yutnori-TITO";

  static final int FALL_THRU = -2;
  static final int NONE  = -1;   // not a valid state
  static final int THROW = 0;
  static final int MOVE  = 1;
  static final int READY = 2;
  static final int OVER  = 3;
  static final int START = 4;
  static final int WAIT  = 5;    // wait at start
  static final int HOLD  = 6;    // wait for a friend to join
  static final int SKIP  = 7;    // wait for a friend to join
  static final int TO_START = 8;
  static final int NR_STATE = 9; // number of states
  static String mStateStr[];  // = { "THROW", "MOVE", "WAIT", "OVER", "THROW", "WAIT", "HOLD" };

  static void initStateStrings( Resources res )
  {
    mStateStr = new String[ NR_STATE ];
    mStateStr[0] = res.getString( R.string.state_throw );
    mStateStr[1] = res.getString( R.string.state_move  );
    mStateStr[2] = res.getString( R.string.state_ready );
    mStateStr[3] = res.getString( R.string.state_over  );
    mStateStr[4] = res.getString( R.string.state_start_throw );
    mStateStr[5] = res.getString( R.string.state_start_ready );
    mStateStr[6] = res.getString( R.string.state_hold );
    mStateStr[7] = res.getString( R.string.state_skip );
    mStateStr[8] = "TO START";
  }

  int mState;            // app (USER) state
  static private boolean[] mSkipping = new boolean[2];

  State( )
  {
    mState  = READY;
    mSkipping[0] = false;
    mSkipping[1] = false;
  }

  int getState() { return mState; }
  void setState( int state ) { mState = state; }

  boolean isWaitOrStart()  { return mState == WAIT  || mState == START; }
  boolean isThrowOrStart() { return mState == THROW || mState == START; }
  boolean isThrowOrSkip()  { return mState == THROW || mState == SKIP; }
  boolean isNotOver() { return mState != OVER; }
  boolean isOver()    { return mState == OVER; }
  boolean isMove()    { return mState == MOVE; }
  boolean isSkip()    { return mState == SKIP; }
  boolean isToStart() { return mState == TO_START; }
  boolean isReady()   { return mState == READY; }
  boolean isStart()   { return mState == START; }
  boolean isPlaying() { return mState == THROW || mState == SKIP || mState == MOVE; }

  void setOver()  { mState = OVER; }
  void setHold()  { mState = HOLD; }
  void setWait()  { mState = WAIT; }
  void setMove()  { mState = MOVE; }
  void setThrow() { mState = THROW; }
  void setReady() { mState = READY; }
  void setSkip()  { mState = SKIP; }
  void setToStart() { mState = TO_START; }
  void setMoveOrThrow( boolean cond ) { mState = cond? MOVE : THROW; }
  void setMoveOrReady( boolean cond ) { mState = cond? MOVE : READY; }


  static boolean isSkipping( int player ) { return mSkipping[ Indices.yut_index( player ) ]; }
  static void setSkipping( int player )   { 
    // Log.v( TAG, "set skipping player " + player );
    mSkipping[ Indices.yut_index( player ) ] = true; 
  }
  static void clearSkipping( int player ) { 
    // Log.v( TAG, "clear skipping player " + player );
    mSkipping[ Indices.yut_index( player ) ] = false;
  }


  // return true if something has been done - false : nothing to do 
  // if upon return mState is READY or SKIP caller must clear moves
  static int checkSkip( int player, State state, Moves moves, boolean clear ) 
  {
    int ret = NONE;
    if ( YutnoriPrefs.isDoSkip() ) {
      if ( isSkipping(player) ) {
        if ( clear ) moves.clear();
        clearSkipping( player );
        ret = READY;
      }
    }
    if ( ret >= 0 && state != null ) state.setState( ret );
    // Log.v( TAG, "check skipping " + player + " returns " + ret );
    return ret;
  }


  @Override
  public String toString() { return mStateStr[ mState ]; }

  static public String toString( int state ) { return ( state >= 0 )? mStateStr[ state ] : "NONE"; }
}
