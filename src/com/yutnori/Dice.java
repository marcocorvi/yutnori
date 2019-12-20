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
    // static int[] roll2 = { 11,  1,  2,  3, 11, 22, 11,  2, 11,  1,  3,  2, 11,  2 };
    // static int[] rollS = { 1,  11, 11,  2, 11, 11,  2,  3, 11, 22,  3, 11,  2,  1 };  
    // static int[] rollX = { 11,  1,  1, 11,  1,  2, 11, 11,  2,  3, 11, 22,  3, 11 };  
    // static int size = rollX.length;
    // static int pos = -1;
    // static int roll()
    // {
    //   pos = (pos + 1) % size;
    //   return rollS[pos];
    // }

    // FIXME prob of head/tail is set to 0.5
    static int roll()
    {
      int back = 0;
      int ret = 0;
      for (int k = 0; k<4; ++k) {
        ret += ( Math.random() > 0.5 )? 1 : 0;
        if ( YutnoriPrefs.isSpecial() ) {
          if ( YutnoriPrefs.isSeoulOrBusan() ) {
            if ( k < 1 ) ++ back;
          } else {
            if ( k < YutnoriPrefs.mBackYuts ) ++ back;
          }
        }
      }
      if ( ret == 0 ) ret = 5;
      // Log.v( "Yutnori-TITO", "Dice roll " + ret + " back " + back );
      return ( ret + 10 * back );
    }
};

