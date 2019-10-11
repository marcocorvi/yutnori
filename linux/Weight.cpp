/** @file Weight.cpp
 *
 * @author marco corvi
 * @date dec 2010
 *
 * @brief Board weights
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

#include "Weight.h"

void
Weight::initWeight()
{
   weight[0]  =  0;
   weight[1]  =  1 + 0.01;
   weight[2]  =  2 + 0.03;
   weight[3]  =  3 + 0.06;
   weight[4]  =  4 + 0.10;
   weight[5]  =  5 + 0.15;
   weight[6]  = 11 + 0.21;
   weight[7]  =  7 + 0.28;
   weight[8]  =  8 + 0.36;
   weight[9]  =  9 + 0.45;
   weight[10] = 10 + 0.55;
   weight[11] = 15 + 0.66;
   weight[12] = 12 + 0.78;
   weight[13] = 13 + 0.91;
   weight[14] = 14 + 1.05;
   weight[15] = 15 + 1.20;
   weight[16] = 16 + 1.36;
   weight[17] = 17 + 1.53;
   weight[18] = 18 + 1.71;
   weight[19] = 19 + 1.90;
   weight[20] = 20 + 2.10;
   weight[21] = 21 + 2.31;
   weight[22] = 16 + 1.36 + 0.50;
   weight[23] = 17 + 1.53 + 0.60;
   weight[24] = 18 + 1.71 + 0.70;
   weight[25] = 19 + 1.90 + 0.80;
   weight[26] = 20 + 2.10 + 0.90;
   weight[27] = 12 + 0.78 + 0.50;
   weight[28] = 13 + 0.91 + 0.60;
   weight[29] = weight[24];
   weight[30] = 15 + 1.20 + 0.30; 
   weight[31] = 16 + 1.36 + 0.30;
   weight[32] = 25;
}

