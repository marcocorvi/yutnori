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
  // static final String TAG = "Yutnori-TITO";

  static final int NONE  = -1;   // not a valid state
  static final int THROW = 0;
  static final int MOVE  = 1;
  static final int READY = 2;
  static final int OVER  = 3;
  static final int START = 4;
  static final int WAIT  = 5;    // wait at start
  static final int HOLD  = 6;    // wait for a friend to join
  static final int SKIP  = 7;    // wait for a friend to join
  static final int NR_STATE = 8; // number of states
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
  void setMoveOrThrow( boolean cond ) { mState = cond? MOVE : THROW; }
  void setMoveOrReady( boolean cond ) { mState = cond? MOVE : READY; }


  static boolean isSkipping( int player )         { return mSkipping[ Indices.yut_index( player ) ]; }
  static private void setSkipping( int player )   { mSkipping[ Indices.yut_index( player ) ] = true; }
  static void clearSkipping( int player )         { mSkipping[ Indices.yut_index( player ) ] = false; }

  // return true if something has been done - false : nothing to do 
  // if upon return mState is READY or SKIP caller must clear moves
  static int checkSkip( int player, State state, Moves moves, boolean clear ) 
  {
    int ret = NONE;
    if ( YutnoriPrefs.mTiTo && YutnoriPrefs.mTiToSkip ) {
      if ( isSkipping(player) ) {
        if ( clear ) moves.clear();
        clearSkipping( player );
        ret = READY;
      }
    }
    if ( ret >= 0 && state != null ) state.setState( ret );
    return ret;
  }

  // @param player    player number (+1 USER , -1 ANDROID)
  // @param state     (optional) state to update
  // @param board
  // @param moves
  // @param clear     if set clear moves when necessary
  // @return new state or -1 if no action has been taken
  static int checkBackDo( int player, State state, Board board, Moves moves, boolean clear ) 
  {
    int ret = NONE;
    // moves.print("State check back-do (" + player + ")" );
    assert( board != null );
    if ( ! YutnoriPrefs.mTiTo || ! moves.hasSkip() ) {
      // Log.v( TAG, "NO BACK_DO");
      return ret;
    }
    if ( board.countPlayer( player ) == 0 ) {
      if ( board.playerStart( player ) == 0 ) {
        // Log.v(TAG, "State[1] player " + player + " do-spot " + board.playerDoSpot( player ) );
        if ( moves.removeSkip() ) {
          // Log.v(TAG, "Remove skip " + player );
          int move = board.doMoveFromDoSpot( player );
          if ( move == 1 ) {
            ret = THROW;
          } else if ( move == 0 ) {
            ret = ( moves.size() > 0 )? MOVE : READY;
          } else {
            // Log.v(TAG, "ERROR State bad doMoveFromDoSpot");
            moves.clear();
            ret = READY;
          }
        } else { // cannot play
          if ( clear ) moves.clear();
          ret = SKIP;
        }
      } else { // board is empty - start is not empty
        if ( YutnoriPrefs.mSeoul ) {
          int move = board.doMoveToSeoulOrBusan( player, moves, Board.SEOUL );
          // Log.v(TAG, "State[2] move to S " + move );
          if ( move == -1 ) { // cannot move (empty START)
            ret = MOVE;
          } else if ( move == 0 ) {
            ret = ( moves.size() > 0 )? MOVE : READY;
          } else { 
            ret = THROW;
          }
        } else if ( YutnoriPrefs.mBusan ) {
          int move = board.doMoveToSeoulOrBusan( player, moves, Board.BUSAN );
          // Log.v(TAG, "State[2] move to B " + move );
          if ( move == -1 ) { // cannot move (empty START)
            ret = MOVE;
          } else if ( move == 0 ) {
            ret = ( moves.size() > 0 )? MOVE : READY;
          } else { 
            ret = THROW;
          }
        } else if ( YutnoriPrefs.mDoSpot ) {
          // Log.v(TAG, "State[2] player " + player + " do-spot " + board.playerDoSpot( player ) );
          if ( board.playerDoSpot( player ) > 0 ) {
            if ( moves.removeSkip() ) {
              // Log.v(TAG, "Remove skip " + player );
              int move = board.doMoveFromDoSpot( player );
              if ( move == 1 ) { // sent opponent to START
                ret = THROW;
              } else if ( move == 0 ) {
                ret = ( moves.size() > 0 )? MOVE : READY;
              } else {
                // Log.v(TAG, "ERROR State bad doMoveFromDoSpot");
                moves.clear();
                ret = READY;
              }
            }
          } else { // do-spot is empty
            if ( moves.hasAllSkips() ) {
              board.doMoveToDoSpot( 1, player, 1 );
              if ( clear ) moves.clear();
              ret = READY;
            } else {
              // normal move
            }
          }
        } else if ( YutnoriPrefs.mTiToSkip ) {
          if ( moves.hasAllSkips() ) {
            setSkipping( player );
            if ( clear ) moves.clear();
            ret = READY;
          } else {
            // normal move;
          }
        } else { 
          // Log.v(TAG, "State - empty board : set revert-do");
          moves.setRevertDo();
        }
      }
    } else { // board not empty
      if ( YutnoriPrefs.mSeoul ) {
        int move = board.doMoveToSeoulOrBusan( player, moves, Board.SEOUL );
        // Log.v(TAG, "State[3] move to S " + move );
        if ( move == -1 ) { // cannot move (empty START)
          ret = MOVE;
        } else if ( move == 0 ) {
          ret = ( moves.size() > 0 )? MOVE : READY;
        } else { 
          ret = THROW;
        }
      } else if ( YutnoriPrefs.mBusan ) {
        int move = board.doMoveToSeoulOrBusan( player, moves, Board.BUSAN );
        // Log.v(TAG, "State[3] move to B " + move );
        if ( move == -1 ) { // cannot move (empty START)
          ret = MOVE;
        } else if ( move == 0 ) {
          ret = ( moves.size() > 0 )? MOVE : READY;
        } else { 
          ret = THROW;
        }
      } else if ( YutnoriPrefs.mDoSpot ) {
        // Log.v(TAG, "State[3] player " + player + " do spot " + board.playerDoSpot( player ) );
        if ( board.playerDoSpot( player ) > 0 ) {
          if ( moves.removeSkip() ) {
            int move = board.doMoveFromDoSpot( player );
            if ( move == 1 ) {
              ret = THROW;
            } else if ( move == 0 ) {
              ret = ( moves.size() > 0 )? MOVE : READY;
            } else {
              // Log.v(TAG, "STATE bad doMoveFromDoSpot");
              moves.clear();
              ret = READY;
            }
          } else {
            // normal
          }
        } else {
          // normal
        }
      } else if ( YutnoriPrefs.mTiToSkip ) {
        // normal
      } else {
        // normal
      }
    }
    // Log.v( TAG, "State check back do returns " + State.toString(ret) );
    if ( ret >= 0 && state != null ) state.setState( ret );
    return ret;
  }

  @Override
  public String toString() { return mStateStr[ mState ]; }

  static public String toString( int state ) { return ( state >= 0 )? mStateStr[ state ] : "NONE"; }
}
