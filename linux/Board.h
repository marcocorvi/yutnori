/** @file Board.h
 *
 * @author marco corvi
 * @date dec 2010
 *
 * @brief yutnori board
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
#ifndef YUTNORI_BOARD_H
#define YUTNORI_BOARD_H

#include <stdio.h>
#include <string.h>


class Board 
{
  private:
    int board[33];
    // -----------------------------------------
    //     [16]   15   14   13   12   [11]
    //      17   31               22   10
    //      18       30       23        9
    //                   24 
    //      19       25       28        8
    //      20   26               27    7
    //   [21/1]    2    3    4    5   [ 6]
    //      {0}
    int start[2];
    int home[2];
    bool cross24; //!< whether can cross 24

  public:
    Board()
    {
      Reset();
    }

    void Reset()
    {
      memset( board, 0, 33*sizeof(int) );
      start[0] = 4;
      start[1] = 4;
      home[0] = 0;
      home[1] = 0;
      cross24 = false;
    }

    int Winner() const 
    {
      if ( home[0] == 4 ) return -1;
      if ( home[1] == 4 ) return +1;
      return 0;
    }

    int operator[]( int k ) const { return board[k]; }

    int Start( int k ) const { return start[k]; }
    int Home( int k ) const { return home[k]; }

    int distance( int from, int to );

    void nextPositions( int from, int move, int pos[2] );

    // player can be +1 or -1
    bool move( int from, int to, int player );

    void print();
  
  private:
    int firstDiagonal( int from, int mov );
    int secondDiagonal( int from, int mov );

};


#endif
