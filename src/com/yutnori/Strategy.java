/** @file Strategy.java
 *
 * @author marco corvi
 * @date dec 2010
 *
 * @brief Yutnori strategy
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
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

