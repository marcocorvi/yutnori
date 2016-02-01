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

class Dice
{
    // FIXME prob of head/tail is set to 0.5
    static int roll()
    {
      int ret = 0;
      for (int k = 0; k<4; ++k) {
        ret += ( Math.random() > 0.5 )? 1 : 0;
      }
      return ( ret == 0 ) ? 5 : ret;
    }
};

