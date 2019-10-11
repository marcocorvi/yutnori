/** @file Indices.cpp
 * 
 * @author marco corvi
 * @date dec 2010
 *
 * @brief implement indices functions
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
#include "Indices.h"

    // -----------------------------------------
    //     [16]   15   14   13   12   [11]
    //      17   31               22   10
    //      18       30       23        9
    //                   24 
    //      19       25       28        8
    //      20   26               27    7
    //   [21/1]    2    3    4    5   [ 6]
    //      {0}

int yut_index( int player ) 
{
  return (player+1)/2;
}

int distance( int k )
{
  if ( k <= POS_CORNER4 ) return k;
  if ( k <= 26 ) return k-6;
  return k - 16;
}
