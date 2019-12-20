/** @file BoardDoSpot.java
 *
 * @author marco corvi
 * @date dec 2010
 *
 * @brief yutnori board for Do-Spot to Cham-Meoki
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.yutnori;

import android.util.Log;

class BoardDoSpot extends BoardDoNone
{
  BoardDoSpot()
  {
    super();
  }

  @Override
  String name() { return "Do-Spot"; }

  boolean canMovePlayer( int player, int from, int to, int m )
  {
    if ( from > 1 && from < 32 && mBoard[from]*player == 0 ) return false; 
    if ( YutnoriPrefs.isDoSpot() && from == 2 && to == 21 && m == -1 ) return true; // DO_SPOT to CHAM_MEOKI
    return super.canMovePlayer( player, from, to, m );
  }

  // total move required to go between two position: used only by Main
  // @from first position index
  // @to   second position index
  // @return 0 if cannot go
  //         HOME + min moves to go out (moving straight)
  //         START - max moves to go back to START
  //         BACK  - max moves to go back 
  int posDifference( int from, int to )
  {
    if ( YutnoriPrefs.isDoSpot() && from == 2 && to == 21 ) { // DO_SPOT to CHAM_MEOKI
      // Log.v( TAG, name() + " 2 - 21 returns " + BACK1 );
      return BACK1;
    }
    return super.posDifference( from, to );
  }

  /** compute the next possible position(s)
   * the result is written in pos[]
   */
  void nextPositions( int from, int mov, int pos[] )
  {
    if ( mov < 0 && YutnoriPrefs.isDoSpot() && from == 2 && from+mov < 2 ) {
      pos[0] = 21;
      pos[1] = -1;
    } else {
      super.nextPositions( from, mov, pos );
    }
  }

  // @param player    player number (+1 USER , -1 ANDROID)
  // @param state     (optional) state to update
  // @param moves
  // @param clear     if set clear moves when necessary
  // @return new state or -1 if no action has been taken
  int checkBackDo( int player, State state, Moves moves, boolean clear ) 
  {
    int ret = State.NONE;
    // moves.print( name() + " check back-do (" + player + ")" );
    if ( ! YutnoriPrefs.isDoSpot() || ! moves.hasSkip() ) {
      // Log.v( TAG, name() + "NO BACK_DO");
      return ret;
    }
    // here is DoSpot and moves has skip
    if ( this.countPlayer( player ) == 0 ) { // board is empty - start is not empty
      moves.setRevertDo();
    } else { // board not empty
      // DO_SPOT TO CHAM_MOEKI sort of normal but player can move 2-21
      // normal but with extra possibility 2-21
    }
    // Log.v( TAG, name() + " check back do returns " + State.toString(ret) );
    if ( ret >= 0 && state != null ) state.setState( ret );
    return ret;
  }

}

