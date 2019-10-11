/** @file Board.cpp
 *
 * @author marco corvi <marco_corvi@geocities.com>
 * @date dec 2005
 * 
 * @brief the Yut Nori board
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
#include "assert.h"
#include "stdlib.h"
#include "math.h"

#include "Board.h"
#include "Indices.h"
#include "Probability.h"


    // -----------------------------------------
    //     [16]   15   14   13   12   [11]
    //      17   31               22   10
    //      18       30       23        9
    //                   24 
    //      19       25       28        8
    //      20   26               27    7
    //   [21/1]    2    3    4    5   [ 6]
    //      {0}

int 
Board::distance( int from, int to )
{
  if ( from == 0 ) {
    if ( to <= POS_CORNER1 ) return Probability[to-1];
    return 0;
  }
  int t = to;
  if ( from <= POS_CORNER4 ) {
    if ( to > from ) {
      int k = to - from; 
      if ( k <= 5 ) return Probability[k];
      if ( from == POS_CORNER1 ) {
        t = ( to == 24 ) ? 29 : to;
        t -= 26;
        if ( t > 0  && t <= 5 ) return Probability[t];
      } else if ( from == POS_CORNER2 ) {
        t = to - 21;
        if ( t > 0  && t <= 5 ) return Probability[t];
      } 
    }
  } else if ( from < 27 ) {
    if ( t == 21 ) { 
      t = 27;
    } else if ( t < 20 ) {
      t += 16;
    }
    if ( from == 24 ) {
      if ( t >= 30 ) {
        t = t - 29;
      }
      if ( t > 0  && t <= 5 ) return Probability[t];
    } else if ( t <= 27 ) {
      t = t - from;
      if ( t > 0  && t <= 5 ) return Probability[t];
    }
    return 0;
  } else {
    if ( t == 24 ) { 
      t = 29;
    } else if ( t >= 16 && t < 21 ) {
      t = t-16+32;
    }
    t = t - from;
    if ( t > 0  && t <= 5 ) return Probability[t];
  }
  return 0;
}
  


bool
Board::move( int from, int to, int player )
{
  printf("Board::move() player %d from %d to %d \n", player, from, to );
  bool ret = false;
  int me  = yut_index(player);
  int you = yut_index(-player);
  if ( from <= 1 ) {
    assert( start[me]>0 );
  } else {
    assert( board[from]*player > 0 );
  }
  int b = board[to];
  if ( b*player < 0 ) { 
    start[ you ] += abs(b);
    board[ to ] = 0;
    ret = true;
    // printf("sent %d you back to start %d \n", b, start[you]);
  }
  if ( from <= 1 ) {    // move from start
    board[to] += player;
    start[me] --;
    // printf("moved me from start %d \n", start[me] );
  } else {              // move forward
    board[to] += board[from];
    board[from] = 0;
  }
  if ( to == POS_HOME ) { // got home
    home[ me ] += abs( board[to] );
    board[to] = 0;
  }
  if ( from == POS_CENTER ) {
    cross24 = false;
  } else if ( to == POS_CENTER ) {
    if ( from > 26 || from == POS_CORNER1 ) {
      if ( abs(board[to]) > 1 ) {
        cross24 = true;
      }
    } else {
      cross24 = false;
    }
  }
  return ret;
}

int 
Board::firstDiagonal( int from, int mov )
{
  int ret = from + mov;
  if ( ret == 27 ) {
    ret = POS_CORNER4;
  } else if ( ret > 27 ) {
    ret = POS_HOME;
  }
  return ret;
}

int 
Board::secondDiagonal( int from, int mov )
{
  int ret = from + mov;
  if ( ret == POS_SKIP ) {
    ret = POS_CENTER;
  } else if ( ret > 31 ) {
    ret -= 16; // pos[0] = pos[0] - 32 + 16;
    if ( ret > POS_CORNER4 ) ret = POS_HOME;
  }
  return ret;
}

void 
Board::nextPositions( int from, int mov, int pos[2] )
{
  pos[1] = -1;
  if ( from <= 1 ) {
    pos[0] = 1 + mov;
  } else  if ( from <= POS_CORNER4 ) {
    pos[0] = from + mov;
    if ( pos[0] > POS_CORNER4 ) pos[0] = POS_HOME;

    if ( from == POS_CORNER1 ) {
      pos[1] = secondDiagonal( 26, mov );
    } else if ( from == POS_CORNER2 ) {
      pos[1] = firstDiagonal( 21, mov );
    }
  } else if ( from <= 26 ) {
    pos[0] = firstDiagonal( from, mov );
    if ( cross24 && from == POS_CENTER ) {
      pos[1] = secondDiagonal( 29, mov );
    }
  } else {
    pos[0] = secondDiagonal( from, mov );
  }
}

void
Board::print()
{
  printf("    ---------------------------------------------\n");
  printf("          %3d   %3d   %3d         %3d   %3d   %3d\n",
         board[16], board[15], board[14], board[13], board[12], board[11]);
  printf("          %3d   %3d                     %3d   %3d\n",
         board[17], board[31], board[22], board[10] );
  printf("          %3d         %3d         %3d         %3d\n",
         board[18], board[30], board[23], board[9] );
  printf("                            %3d \n",  board[24] );
  printf("          %3d         %3d         %3d         %3d\n",
         board[19], board[25], board[28], board[8] );
  printf("          %3d   %3d                     %3d   %3d\n",
         board[20], board[26], board[27], board[7] );
  printf("%1d.%1d   %1d.%1d %3d   %3d   %3d         %3d   %3d   %3d\n",
         home[0], home[1],
         start[0], start[1], board[21],
         board[2], board[3], board[4], board[5], board[6]);
  printf("    --------------------------------------------\n");
}
