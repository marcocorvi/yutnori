/** @file Yutnori.java
 * 
 * @author marco corvi
 * @date dec 2010
 *
 * @brief Yut Nori game
 *
 * ----------------------------------------------------------
 *  Copyright(c) 2005 marco corvi
 *
 *  sudoku is free software.
 *
 *  You can redistribute it and/or modify it under the terms of 
 *  the GNU General Public License as published by the Free 
 *  Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public
 *  License along with this program; if not, write to the Free
 *  Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *  MA  02111-1307  USA
 *
 * ----------------------------------------------------------
 */
package com.yutnori;

import android.util.Log;

class Yutnori
{
  static final String TAG = "yutnori";

  private Board mBoard;
  private Moves mMoves;
  Strategy mStrategy; //!< my strategy
  private int player;          //!< my player
 
  /**
   * @param other index of the other player
   */
  Yutnori( Board b, Moves m, int other )
  {
    mBoard    = b;
    mMoves    = m;
    player    = -other;
    mStrategy = null;
  }

  Yutnori( Board b, Moves m )
  {
    mBoard    = b;
    mMoves    = m;
    player   = -1;
    mStrategy = null;
  }

  // Board getBoard() { return mBoard; }
  // int getWinner()  { return mBoard.winner(); }
  // void reset() { mBoard.reset(); }

  // throw dice
  static int throwYut() { return Dice.roll(); }

  // move of the other player
  // @return true if the other player sent me back to start
  // boolean moveOther( int from, int to ) 
  // { 
  //   if ( to <= 1 ) to = 32;
  //   return mBoard.move( from, to, player, 0 );
  // }

  // check if the other player can move from to with move "m"
  boolean canMove( int from, int to, int m )
  {
    if ( from > 1 && from < 32 && mBoard.value(from)*player == 0 ) return false; 
    if ( to <= 1 ) to = 32;
    int[] pos = new int[2];
    mBoard.nextPositions( from, m, pos );
    return ( pos[0] == to || pos[1] == to );
  }

  // @return 0 normal, or winner
  // int play( )
  // {
  //   while ( mBoard.winner() == 0 ) {
  //     playOnce( 1 );
  //   }
  //   return mBoard.winner();
  // }

  // this is Android strategy playing
  void playOnce( int doze )
  {
    mMoves.clear();
    boolean again = true;
    do {
      if ( again ) {
        again = false;
        do {
          int m = throwYut();
          mMoves.add( m );
          Delay.sleep( 2 * doze );
        } while ( mMoves.value( mMoves.size()-1 ) > 3 );
      }
  
      if ( mStrategy != null ) {
        again = mStrategy.movePlayer( mMoves, doze );
      }
      Delay.sleep( doze );
      // Log.v( TAG, "PlayOnce winner " + mBoard.winner() + " " + mBoard.home(0) + "/" + mBoard.home(1)
      //             + " moves " + mMoves.size() + " again " + again );
    } while ( mBoard.winner() == 0 && (again || mMoves.size() > 0) );
  }

}
