/** @file Board.java
 *
 * @author marco corvi
 * @date dec 2010
 *
 * @brief yutnori board
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.yutnori;

import android.util.Log;

class Board 
{
  static final String TAG = "yutnori";

  private int mBoard[];
    // -----------------------------------------
    //     [16]   15   14   13   12   [11]
    //      17   31               22   10
    //      18       30       23        9
    //                   24 
    //      19       25       28        8
    //      20   26               27    7
    //   [21/1]    2    3    4    5   [ 6]
    //      {0}

  private int mStart[];
  private int mHome[];
  private static boolean cross24 = false; //!< whether can cross 24

  Board()
  {
    mBoard = new int[33];
    mStart = new int[2];
    mHome  = new int[2];
    reset();
  }

  void reset()
  {
    synchronized( this ) {
      for ( int k=0; k<33; ++k) mBoard[k] = 0;
      mStart[0] = 4;
      mStart[1] = 4;
      mHome[0] = 0;
      mHome[1] = 0;
    }
    cross24 = false;
  }

  int winner() 
  {
    if ( mHome[0] == 4 ) return -1;
    if ( mHome[1] == 4 ) return +1;
    return 0;
  }

  int value( int k ) { return mBoard[k]; }
  int start( int k ) { return mStart[k]; }
  int home( int k )  { return mHome[k]; }

  // total move required to go between two position
  // @from first position index
  // @to   second position index
  // @return 0 if cannot go
  //         negative if going out
  static int difference( int from, int to )
  {
    if ( from == 0 ) from = 1;
      
    if ( from == 6 ) {
      if ( to == 24 ) return 3;
      if ( to < 16 ) return to - from;
      if ( to >= 16 && to <= 21 ) return 6 + to - 16;
      if ( to >  24 && to <= 26 ) return 3 + to - 24;
      if ( to >= 27 && to <= 31 ) return 0 + to - 26;
      if ( to >= 32 ) return -12;
    } else if ( from == 11 ) {
      if ( to < 21 ) return to - from;
      if ( to == 21 ) return 6;
      if ( to >= 22 && to <= 26 ) return 0 + to - 21;
      if ( to >= 30 && to <= 31 ) return 3 + to - 29;
      if ( to >= 32 ) return -7;
    } else if ( from == 16 ) {
      if ( to > 16 && to <= 21 ) return to - from;
      if ( to >= 32 ) return -6;
    } else if ( from == 21 ) {
      if ( to == 32 ) return -1;
    } else if ( from == 24 ) {
      if ( to >= 16 && to <  21 ) return 3 + to - 16;
      if ( to == 21 ) return 3;
      if ( to >= 25 && to <= 26 ) return to - from;
      if ( to >= 30 && to <= 31 ) return to - 29;
      if ( to >= 32 ) return -4;
    } else if ( from >= 22 && from <= 26 ) {
      if ( to == 21 ) return 27 - from;
      if ( to > from && to <= 26 ) return to - from;
      if ( to >= 32 ) return -(28 - from);
    } else if ( from >= 27 && from <= 31 ) {
      if ( to == 24 ) to = 29;
      if ( to >= 16 && to <= 21 ) return to - 16 + 32 - from;
      if ( to > from && to <= 31 ) return to - from;
      if ( to >= 32 ) return -( 7 + 32 - from);
    } else {
      if ( to <= 21 ) return to - from;
      if ( to <= 26 && from >= 22 ) return to - from;
      if ( to <= 31 && from >= 27 ) return to - from;
      if ( to >= 32 ) return -(22 - from);
    }
    return 0;
  }

  static int distance( int from, int to )
  {
    if ( from == 0 ) {
      if ( to <= Indices.POS_CORNER1 ) return Probability.value(to-1);
      return 0;
    }
    int t = to;
    if ( from <= Indices.POS_CORNER4 ) {
      if ( to > from ) {
        int k = to - from; 
        if ( k <= 5 ) return Probability.value(k);
        if ( from == Indices.POS_CORNER1 ) {
          t = ( to == 24 ) ? 29 : to;
          t -= 26;
          if ( t > 0  && t <= 5 ) return Probability.value(t);
        } else if ( from == Indices.POS_CORNER2 ) {
          t = to - 21;
          if ( t > 0  && t <= 5 ) return Probability.value(t);
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
        if ( t > 0  && t <= 5 ) return Probability.value(t);
      } else if ( t <= 27 ) {
        t = t - from;
        if ( t > 0  && t <= 5 ) return Probability.value(t);
      }
      return 0;
    } else {
      if ( t == 24 ) { 
        t = 29;
      } else if ( t >= 16 && t < 21 ) {
        t = t-16+32;
      }
      t = t - from;
      if ( t > 0  && t <= 5 ) return Probability.value(t);
    }
    return 0;
  }

  boolean move( int from, int to, int player, int pawn_nr )
  {
    boolean ret = false;
    int me  = Indices.yut_index(player);
    int you = Indices.yut_index(-player);
    if ( from <= 1 ) {
      assert( mStart[me]>0 );
    } else {
      assert( mBoard[from]*player > 0 );
    }
    synchronized( this ) {
      int b = mBoard[to];
      if ( b*player < 0 ) { 
        mStart[ you ] += Math.abs(b);
        mBoard[ to ] = 0;
        ret = true;
        // printf("sent %d you back to mStart %d \n", b, mStart[you]);
      }
      if ( from <= 1 ) {    // move from mStart
        mBoard[to] += player;
        mStart[me] --;
        // printf("moved me from mStart %d \n", mStart[me] );
      } else {              // move forward
        if ( pawn_nr == 0 ) pawn_nr = mBoard[from];
        assert ( pawn_nr <= mBoard[from] );
        mBoard[to]   += pawn_nr;
        mBoard[from] -= pawn_nr;
        //  mBoard[from] = 0;
      }
      if ( to == Indices.POS_HOME ) { // got mHome
        mHome[ me ] += Math.abs( mBoard[to] );
        // Log.v( TAG, "moved to home " + mHome[0] + "/" + mHome[1] + mBoard[to] );
        mBoard[to] = 0;
      }
      if ( from == Indices.POS_CENTER ) {
        cross24 = false;
      } else if ( to == Indices.POS_CENTER ) {
        if ( from > 26 || from == Indices.POS_CORNER1 ) {
          if ( Math.abs(mBoard[to]) > 1 ) {
            cross24 = true;
          }
        } else {
          cross24 = false;
        }
      }
    }
    return ret;
  }

  private static int firstDiagonal( int from, int mov )
  {
    int ret = from + mov;
    return ( ret == 27 )? Indices.POS_CORNER4 : ( ret > 27 )? Indices.POS_HOME : ret;
  }
  
  private static int secondDiagonal( int from, int mov )
  {
    int ret = from + mov;
    return ( ret == Indices.POS_SKIP )? Indices.POS_CENTER : ( ret > 37 )? Indices.POS_HOME : ( ret > 31 )? ret-16 : ret;
  }

  /** compute the next possible position(s)
   * the result is written in pos[]
   */
  static void nextPositions( int from, int mov, int pos[] )
  {
    pos[0] = -1;
    pos[1] = -1;
    if ( mov <= 0 ) return;

    if ( from <= 1 ) {
      pos[0] = 1 + mov;
    } else  if ( from <= Indices.POS_CORNER4 ) {
      pos[0] = from + mov;
      if ( pos[0] > Indices.POS_CORNER4 ) pos[0] = Indices.POS_HOME;
  
      if ( from == Indices.POS_CORNER1 ) {
        pos[1] = secondDiagonal( 26, mov );
      } else if ( from == Indices.POS_CORNER2 ) {
        pos[1] = firstDiagonal( 21, mov );
      }
    } else if ( from == 24 ) {
      pos[0] = ( mov < 3 )? from+mov : ( mov == 3 )? Indices.POS_CORNER4 : Indices.POS_HOME;
      pos[1] = ( mov < 3 )? 29+mov   : 16 + mov-3;
    } else if ( from <= 26 ) {
      pos[0] = firstDiagonal( from, mov );
      // if ( cross24 && from == Indices.POS_CENTER ) pos[1] = secondDiagonal( 29, mov );
    } else {
      pos[0] = secondDiagonal( from, mov );
    }
  }

/*
void
Board::print()
{
  printf("    ---------------------------------------------\n");
  printf("          %3d   %3d   %3d         %3d   %3d   %3d\n",
         mBoard[16], mBoard[15], mBoard[14], mBoard[13], mBoard[12], mBoard[11]);
  printf("          %3d   %3d                     %3d   %3d\n",
         mBoard[17], mBoard[31], mBoard[22], mBoard[10] );
  printf("          %3d         %3d         %3d         %3d\n",
         mBoard[18], mBoard[30], mBoard[23], mBoard[9] );
  printf("                            %3d \n",  mBoard[24] );
  printf("          %3d         %3d         %3d         %3d\n",
         mBoard[19], mBoard[25], mBoard[28], mBoard[8] );
  printf("          %3d   %3d                     %3d   %3d\n",
         mBoard[20], mBoard[26], mBoard[27], mBoard[7] );
  printf("%1d.%1d   %1d.%1d %3d   %3d   %3d         %3d   %3d   %3d\n",
         mHome[0], mHome[1],
         start[0], start[1], mBoard[21],
         mBoard[2], mBoard[3], mBoard[4], mBoard[5], mBoard[6]);
  printf("    --------------------------------------------\n");
}
*/

}

