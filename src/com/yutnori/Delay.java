/** @file Delay.java
 *
 * @author marco corvi 
 * @date jan 2016
 *
 * @brief Delays
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.yutnori;

class Delay
{
  static void sleep( int n )
  {
    if ( n > 0 ) {
      try {
        Thread.sleep( n * YutnoriPrefs.mDelayUnit );
      } catch( InterruptedException e ) {
        // OK
      }
    }
  }

}


