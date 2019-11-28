/** @file Weight.java
 * 
 * @author marco corvi
 * @date dec 2015
 * 
 * @brief board strategy weights
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.yutnori;


/** weights of the board positions.
 * The higher the weight the better the position
 */
class Weight
{
  private static final int DIMW = 33;
  private float weight[];

  Weight()
  {
    weight = new float[ DIMW ];
    initWeight();
  }

  float value( int k ) { return weight[k]; }

  void add( int k, float w ) { weight[k] += w; }

  void set29() { weight[29] = weight[24]; }

  private void initWeight()
  {
    weight[0]  =  0;
    weight[1]  =  1 + 0.01f;
    weight[2]  =  2 + 0.03f;
    weight[3]  =  3 + 0.06f;
    weight[4]  =  4 + 0.10f;
    weight[5]  =  5 + 0.15f;
    weight[6]  = 11 + 0.21f;
    weight[7]  =  7 + 0.28f;
    weight[8]  =  8 + 0.36f;
    weight[9]  =  9 + 0.45f;
    weight[10] = 10 + 0.55f;
    weight[11] = 15 + 0.66f;
    weight[12] = 12 + 0.78f;
    weight[13] = 13 + 0.91f;
    weight[14] = 14 + 1.05f;
    weight[15] = 15 + 1.20f;
    weight[16] = 16 + 1.36f;
    weight[17] = 17 + 1.53f;
    weight[18] = 18 + 1.71f;
    weight[19] = 19 + 1.90f;
    weight[20] = 20 + 2.10f;
    weight[21] = 21 + 2.31f;
    weight[22] = 16 + 1.36f + 0.50f;
    weight[23] = 17 + 1.53f + 0.60f;
    weight[24] = 18 + 1.71f + 0.70f;
    weight[25] = 19 + 1.90f + 0.80f;
    weight[26] = 20 + 2.10f + 0.90f;
    weight[27] = 12 + 0.78f + 0.50f;
    weight[28] = 13 + 0.91f + 0.60f;
    weight[29] = weight[24];
    weight[30] = 15 + 1.20f + 0.30f; 
    weight[31] = 16 + 1.36f + 0.30f;
    weight[32] = 100; // 25;
  }
}

