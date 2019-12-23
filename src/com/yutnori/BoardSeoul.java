/** @file BoardSeoul.java
 *
 * @author marco corvi
 * @date dec 2010
 *
 * @brief yutnori board with Seoul rule
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.yutnori;

import android.util.Log;

class BoardSeoul extends Board
{
  BoardSeoul()
  {
    super();
  }

  @Override
  String name() { return "Seoul"; }

  // return -1 error
  //         0 ok move
  //         1 ok move and sent opponent back to START
  int doMoveToSeoulOrBusan( int player, Moves moves, int station )
  {
    int me  = Indices.yut_index( player);
    if ( ! moves.hasSkip() ) return -1;
    if ( mStart[ me ] == 0 ) {
      // Log.v(TAG, name() + " no start : set revert-do" );
      moves.setRevertDo();
      return -1;
    }
    moves.removeSkip();
    mStart[me] --;
    int pawns = mBoard[station] * player;
    if ( pawns < 0 ) {
      // Log.v(TAG, name() + " send home " + pawns );
      mBoard[station] = player;
      int you = 1 - me; // Indices.yut_index(-player);
      mStart[you] += Math.abs(pawns);
      return 1;
    } // else pawns >= 0
    mBoard[station] += player;
    return 0;
  }

  // @param player    player number (+1 USER , -1 ANDROID)
  // @param state     (optional) state to update
  // @param moves
  // @param clear     if set clear moves when necessary
  // @return new state or -1 if no action has been taken
  int checkBackDo( int player, State state, Moves moves, boolean clear, ISender sender ) 
  {
    int ret = State.NONE;
    // moves.print("Seoul check back-do (" + player + ")" );
    if ( ! YutnoriPrefs.isSeoulOrBusan() || ! moves.hasSkip() ) {
      // Log.v( TAG, name() + " NO BACK_DO");
      return ret;
    }
    int k = moves.getSkip();
    int to = ( YutnoriPrefs.isSeoul() ? Board.SEOUL : Board.BUSAN );
    if ( this.countPlayer( player ) == 0 ) { // board is empty - start is not empty
      int move = this.doMoveToSeoulOrBusan( player, moves, to );
      // Log.v(TAG, name() + "[2] move to S " + move );
      if ( move == -1 ) { // cannot move (empty START)
        ret = State.MOVE;
      } else {
        ret = (move != 0)? State.THROW : ( moves.size() > 0 )? State.MOVE : State.READY;
        if ( sender != null ) sender.sendMyMove( k, 1, to, 1 );
      }
    } else { // board not empty
      int move = this.doMoveToSeoulOrBusan( player, moves, to );
      // Log.v(TAG, name() + "[3] move to S " + move );
      if ( move == -1 ) { // cannot move (empty START)
        ret = State.MOVE;
      } else {
        ret = (move != 0)? State.THROW : ( moves.size() > 0 )? State.MOVE : State.READY;
        if ( sender != null ) sender.sendMyMove( k, 1, to, 1 );
      }
    }
    // Log.v( TAG, name() + " check back do returns " + State.toString(ret) );
    if ( ret >= 0 && state != null ) state.setState( ret );
    return ret;
  }

}

