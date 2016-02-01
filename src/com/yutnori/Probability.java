/** @file Probability.java
 *
 * @author marco corvi
 * @date dec 2015
 *
 * @brief yutnori stick throwing probabilities
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.yutnori;

class Probability
{
  private static final int[] mValue = { 0, 4, 6, 4, 1, 1 };

  static final int value( int k ) { return mValue[k]; }
}

