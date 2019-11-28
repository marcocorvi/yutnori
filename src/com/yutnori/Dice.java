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
    // FIXME prob of head/tail is set to 0.5
    static int roll()
    {
      int back = 0;
      int ret = 0;
      for (int k = 0; k<4; ++k) {
        ret += ( Math.random() > 0.5 )? 1 : 0;
        if ( YutnoriPrefs.mTiTo && k < YutnoriPrefs.mBackYuts ) ++ back;
      }
      if ( ret == 0 ) ret = 5;
      // Log.v( "Yutnori-TITO", "Dice roll " + ret + " back " + back );
      return ( ret + 10 * back );
    }
};

