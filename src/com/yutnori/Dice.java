/** @file Dice.java
 *
 * @author marco corvi
 * @date dec 2015
 *
 * @brief Throw the yutnori sticks
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.yutnori;

import android.util.Log;

class Dice
{
  /*
    static int[] roll2 = { 11,  1,  2,  3, 11, 22, 11,  2, 11,  1,  3,  2, 11,  2 };
    static int[] rollS = { 1,  11, 11,  2, 11, 11,  2,  3, 11, 22,  3, 11,  2,  1 };  
    static int[] rollX = { 11,  1,  1, 11,  1,  2, 11, 11,  2,  3, 11, 22,  3, 11 };  
    static int[] roll5 = { 11, 24,  5, 13,  2,  5, 24, 22,  2,  3, 11, 22,  3, 11 };  
    static int[] rollC = { 11,  1, 11,  1,  1,  1, 11,  1, 11,  1,  1,  1, 11,  1, 11, 1, 1, 1, 11, 1, 2, 1 };

    static int[] rolls = rollC; // which one to use
    static int size = rolls.length;
    static int pos = -1;
    static int roll()
    {
      pos = (pos + 1) % size;
      return rolls[pos];
    }
  */
  // FIXME prob of head/tail is set to 0.5
  static int roll()
  {
    int back = 0;
    int ret = 0;
    for (int k = 0; k<4; ++k) {
      if ( Math.random() > 0.5 ) {
        if ( k < YutnoriPrefs.getBackYuts() ) ++ back;
        ret += 1;
      }
    }
    if ( ret == 0 ) ret = 5;
    // Log.v( "Yutnori-TITO", "Dice roll " + ret + " back " + back );
    return ( ret + 10 * back );
  }
  //
}

