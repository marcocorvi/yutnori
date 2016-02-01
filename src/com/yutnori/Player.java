/** @file Player.java
 *
 * @author marco corvi
 * @date dec 2010
 *
 * @brief Yutnori player
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

