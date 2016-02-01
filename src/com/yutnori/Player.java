/** @file Player.java
 *
 * @author marco corvi
 * @date dec 2015
 *
 * @brief Yutnori player
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.yutnori;

import android.util.Log;

class Player
{
  protected DrawingSurface mDrawingSurface;
  protected Board mBoard;
  protected Moves mMoves;
  protected int me;
  protected int other;

  /**
   * @param p   my player number (+1 or -1)
   */
  Player( Board b, Moves m, DrawingSurface surface, int p ) 
  {
    mBoard = b;
    mMoves = m;
    mDrawingSurface = surface;
    me     = p;
    other  = -p;
  }

  // assume moves sorted
  // @return true if send opponent back to start
  boolean do_move( int from, int to, int doze ) 
  {
    
    if ( mDrawingSurface != null ) {
      mDrawingSurface.setHighlight( (from <= 1)? 0 : from );
      Delay.sleep( doze );
      mDrawingSurface.setHighlight( -1 );
    }
    boolean ret = mBoard.move( from, to, me, 0 );
    // Log.v("yutnori", "Player do move " + from + " -> " + to + " return " + ret );
    return ret;
  }


  int player() { return me; }

  int opponent() { return other; }

}

