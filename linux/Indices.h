/** @file Indices.h
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
#ifndef YUTNORI_INDICES_H
#define YUTNORI_INDICES_H


    // -----------------------------------------
    //     [16]   15   14   13   12   [11]
    //      17   31               22   10
    //      18       30       23        9
    //                   24 
    //      19       25       28        8
    //      20   26               27    7
    //   [21/1]    2    3    4    5   [ 6]
    //      {0}

#define POS_SKIP    29
#define POS_CENTER  24
#define POS_HOME    32
#define POS_START    1
#define POS_CORNER1  6
#define POS_CORNER2 11
#define POS_CORNER3 16
#define POS_CORNER4 21

/** get a fixed value (22) minus the distance from the home
 * @param k   index on the board
 * @return 22 minus the distance from the home
 */
int distance( int k );

/** maps player (+/-1) to index (1/0 resp.)
 * @param player  player
 * @return index of the player
 */
int yut_index( int player );

#endif
