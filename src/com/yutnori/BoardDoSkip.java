/** @file BoardDoSkip.java
 *
 * @author marco corvi
 * @date dec 2010
 *
 * @brief yutnori board for Do Skip
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.yutnori;

import android.util.Log;

class BoardDoSkip extends BoardDoNone
{
  BoardDoSkip()
  {
    super();
  }

  @Override
  String name() { return "Do-Skip"; }

  boolean canMovePlayer( int player, int from, int to, int m )
  {
    if ( from > 1 && from < 32 && mBoard[from]*player == 0 ) return false; 
    if ( from == 2 && m < 0 && to <= 1 ) return true; // back to start
    return super.canMovePlayer( player, from, to, m );
  }


  // @param player    player number (+1 USER , -1 ANDROID)
  // @param state     (optional) state to update
  // @param moves
  // @param clear     if set clear moves when necessary
  // @return new state or -1 if no action has been taken
  int checkBackDo( int player, State state, Moves moves, boolean clear, ISender sender )
  {
    int ret = State.NONE;
    // moves.print( name() + " check back-do (" + player + ")" );
    if ( ! YutnoriPrefs.isDoSkip() || ! moves.hasSkip() ) {
      // Log.v( TAG, name() + " NO BACK_DO");
      return ret;
    }
    // here is DoSkip and moves has Skip
    if ( this.countPlayer( player ) == 0 ) { // board is empty - start is not empty
      if ( moves.hasAllSkips() ) {
        // FIXME_SKIPPING set this to skip a turn after a back-do with empty board
        State.setSkipping( player );
        if ( clear ) moves.clear();
        if ( sender != null ) sender.sendMySkip( clear );
        // ret = State.READY;
        ret = State.MOVE;
      } else {
        // normal move;
      }
    } else { // board not empty
      // if the board has only player at station 2 this most go back to START
      if ( this.hasPlayerOnlyAtStation( player, 2 ) && moves.hasAllSkips() ) {
        // Log.v(TAG, name() + "[2] player " + player + " only at 2");
        ret = State.TO_START;
      } else {
        // Log.v(TAG, name() + "[3] player " + player + " only at other stations than 2");
        // normal
      }
    }
    // Log.v( TAG, name() + " check back do returns " + State.toString(ret) );
    if ( ret >= 0 && state != null ) state.setState( ret );
    return ret;
  }
}

