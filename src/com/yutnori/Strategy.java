/** @file Strategy.java
 *
 * @author marco corvi
 * @date dec 2010
 *
 * @brief Yutnori strategy
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

class Strategy extends Player
{
  Strategy( Board b, Moves m, DrawingSurface s, int p ) 
  {
    super( b, m, s, p );
  }

  // void assert( boolean condition ) { }

  float positionDanger( int at )
  {
    float danger = 0.0f;
    if ( mBoard.start( Indices.yut_index( opponent() ) ) > 0 ) {
      danger += mBoard.distance( 0, at );
    }
    for (int k=2; k < Indices.POS_HOME; ++k ) {
      if ( k == Indices.POS_SKIP ) continue;
      if ( mBoard.value(k) * player() < 0 ) {
        danger += mBoard.distance( k, at );
      }
    }
    return danger;
  }

 
  boolean movePlayer( Moves moves, int doze )
  {
    return false;
  }

  /** randomize scores
   * @return a random number between 0.95 and 1.05
   */
  static float yut_random()
  {
    return 0.95f + 0.1f * (float)Math.random();
  }

}

