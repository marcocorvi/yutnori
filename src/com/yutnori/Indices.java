/** @file Indices.java
 * 
 * @author marco corvi
 * @date dec 2015
 *
 * @brief implement indices functions
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.yutnori;

    // -----------------------------------------
    //     [16]   15   14   13   12   [11]
    //      17   31               22   10
    //      18       30       23        9
    //                   24 
    //      19       25       28        8
    //      20   26               27    7
    //   [21/1]    2    3    4    5   [ 6]
    //      {0}

class Indices
{
  static final int POS_SKIP    = 29;
  static final int POS_CENTER  = 24;
  static final int POS_HOME    = 32;
  static final int POS_START   =  1;
  static final int POS_CORNER1 =  6;
  static final int POS_CORNER2 = 11;
  static final int POS_CORNER3 = 16;
  static final int POS_CORNER4 = 21;

  /** get a fixed value (22) minus the distance from the home
   * @param k   index on the board
   * @return 22 minus the distance from the home
   */
  static int distance( int k )
  {
    if ( k <= POS_CORNER4 ) return k;
    if ( k <= 26 ) return k-6;
    return k - 16;
  }

  /** maps player (+/-1) to index (1/0 resp.)
   * @param player  player
   * @return index of the player
   */
  static int yut_index( int player ) 
  {
    return (player>0)? 1 : 0;
  }

}
