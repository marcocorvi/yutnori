/** @file BoardDoNone.java
 *
 * @author marco corvi
 * @date dec 2010
 *
 * @brief yutnori board for Do Skip that reverts do wen board is empty
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.yutnori;

import android.util.Log;

class BoardDoNone extends Board
{
  BoardDoNone()
  {
    super();
  }

  @Override
  String name() { return "Do-None"; }

  @Override
  void reset()
  {
    super.reset();
  }

  boolean canMovePlayer( int player, int from, int to, int m )
  {
    if ( from > 1 && from < 32 && mBoard[from]*player == 0 ) return false; 
    if ( from == 2 && m < 0 && to <= 1 ) return true; // back to start
    return super.canMovePlayer( player, from, to, m );
  }

  int posDifference( int from, int to )
  {
    // Log.v( TAG, name() + " Pos diff from " + from + " to " + to );
    if ( YutnoriPrefs.isBackDo() ) {
      if ( from ==  6 && to ==  5 ) return BACK1;
      if ( from == 11 && to == 10 ) return BACK1;
      if ( from == 16 && ( to == 15 || to == 31 ) ) return BACK1;
      if ( from == 21 && ( to == 20 || to == 26 ) ) return BACK1;
      if ( from == 24 && ( to == 23 || to == 28 ) ) return BACK1;
      if ( from == 22 && to == 11 ) return BACK1;
      if ( from == 27 && to ==  6 ) return BACK1;
      if ( from == 2 && ( to <= 1 || to >= 34 ) ) return START - 1;
      if ( from > 2 && to == from - 1 ) return BACK1;
    }
    return super.posDifference( from, to );
  }

  // void nextPositions( int from, int mov, int pos[] )
  // {
  //   super.nextPositions( from, mov, pos );
  // }

  // @param player    player number (+1 USER , -1 ANDROID)
  // @param state     optional state to update
  // @param moves
  // @param clear     if set clear moves when necessary
  // @return new state or -1 if no action has been taken
  int checkBackDo( int player, State state, Moves moves, boolean clear ) 
  {
    int ret = State.NONE;
    // moves.print("DoNone check back-do (" + player + ")" );
    if ( ! YutnoriPrefs.isDoNone() || ! moves.hasSkip() ) {
      // Log.v( TAG, name() + " NO BACK_DO");
      return ret;
    }
    if ( this.countPlayer( player ) == 0 ) { // board is empty - start is not empty
      moves.setRevertDo();
    } else { // board not empty
      // if the board has only player at station 2 this most go back to START
      if ( this.hasPlayerOnlyAtStation( player, 2 ) && moves.hasAllSkips() ) {
        // Log.v(TAG, name() + "[3b] player " + player + " only at 2");
        ret = State.TO_START;
      } else {
        // Log.v(TAG, name() + "[3c] player " + player + " only at other stations than 2");
        // normal
      }
    }
    // Log.v( TAG, name() + " check back do returns " + State.toString(ret) );
    if ( ret >= 0 && state != null ) state.setState( ret );
    return ret;
  }
}

