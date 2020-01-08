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
import android.os.Bundle;

class Board 
{
  static final String TAG = "Yutnori-TITO";

  protected int mBoard[];
    // -----------------------------------------
    // {34}  {0}  [16]   15   14   13   12   [11]
    // {35}  {1}    17   31               22   10
    //              18       30       23        9
    //                           24 
    //              19       25       28        8
    //      {32}    20   26               27    7
    //      {33} [21/1]    2    3    4    5   [ 6]
    //
  static final protected short BOARD_PLAIN  = 0;
  static final protected short BOARD_SEOUL  = 1;
  static final protected short BOARD_DONONE = 2;
  static final protected short BOARD_DOSKIP = 3;
  static final protected short BOARD_DOSPOT = 4;
  static final protected short BOARD_DOCAGE = 5;

  protected short getBoardIndex() { return BOARD_PLAIN; }

  static final int SEOUL = 24;
  static final int BUSAN =  6;
  static final int CHAM_MOKI  = 21;
  static final int DO_STATION =  2;

  static final int HOME  =  100;
  static final int START = -100;
  static final int BACK  =    0;
  static final int BACK1 =   -1;

  protected int mStart[];
  protected int mHome[];
  protected int mCage[];
  protected static boolean cross24 = false; //!< whether can cross 24

  Board()
  {
    mBoard = new int[33];
    mStart = new int[2];
    mHome  = new int[2];
    mCage  = new int[2];
    reset();
  }

  Board( short b[] )
  {
    mBoard = new int[33];
    mStart = new int[2];
    mHome  = new int[2];
    mCage  = new int[2];
    if ( b == null ) {
      reset();
    } else {
      // Log.v("Yutnori", "restore board data");
      int k = 0;
      for ( ; k < 33; ++ k ) mBoard[k] = b[k];
      mStart[0] = b[k++];
      mStart[1] = b[k++];
      mHome[0]  = b[k++];
      mHome[1]  = b[k++];
      mCage[0]  = b[k++];
      mCage[1]  = b[k++];
    }
  }

  static private Board makeBoard( short index, short b[] )
  {
    // Log.v("Yutnori", "make board " + index );
    switch ( index ) {
      case BOARD_SEOUL:  return new BoardSeoul(b);
      case BOARD_DONONE: return new BoardDoNone(b);
      case BOARD_DOSKIP: return new BoardDoSkip(b);
      case BOARD_DOSPOT: return new BoardDoSpot(b);
      case BOARD_DOCAGE: return new BoardDoCage(b);
      default: return new Board(b);
    }
  }

  void saveState( Bundle bundle )
  {
    short b[] = new short[39];
    int k = 0;
    for ( ; k < 33; ++ k ) b[k] = (short)(mBoard[k]);
    b[k++] = (short)mStart[0];
    b[k++] = (short)mStart[1];
    b[k++] = (short)mHome[0];
    b[k++] = (short)mHome[1];
    b[k++] = (short)mCage[0];
    b[k++] = (short)mCage[1];
    short index = (short)getBoardIndex();
    // Log.v("Yutnori", "save board " + index );
    bundle.putShort( "BOARD_INDEX", getBoardIndex() );
    bundle.putShortArray( "BOARD_ARRAY", b );
  }

  static Board restoreState( Bundle bundle )
  {
    short index = bundle.getShort( "BOARD_INDEX" );
    short b[]   = bundle.getShortArray( "BOARD_ARRAY" );
    return makeBoard( index, b );
  }

  String name() { return "Plain"; }

  int countPlayer( int player )  // totalCount
  {
    int ret = 0;
    for ( int k = 2; k <= 32; ++ k ) {
      if ( k == 29 ) continue;
      if ( mBoard[k] * player > 0 ) ++ ret; // if there are player's pawns
    }
    return ret;
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
  int doCage( int k ) { return 0; }

  int getStationValue( int k )   { return mBoard[ k ]; }
  int playerStart( int player )  { return mStart[ Indices.yut_index( player ) ]; }
  int playerDoCage( int player ) { return 0; }
  int playerAtStation( int player, int k ) { return mBoard[k] * player; }


  int doMoveFromCage( int player ) { return -1; } // ERROR
  void doMoveToCage( int from, int player, int pawn_nr ) { } 
  int doMoveToSeoulOrBusan( int player, Moves moves, int station ) { return -1; }

  // boolean doMoveToStart( int from, int player, int pawns )
  // {
  //   int p = mBoard[from] * player;
  //   if ( p <= 0 ) return false;
  //   if ( p < pawns ) pawns = p;
  //   mStart[ Indices.yut_index( player ) ] += pawns;
  //   mBoard[from] -= (player * pawns);
  //   return true;
  // }

  boolean doMoveToStart( int from, int player )
  {
    int pawns = mBoard[from] * player;
    if ( pawns <= 0 ) return false;
    mStart[ Indices.yut_index( player ) ] += pawns;
    mBoard[from] = 0;
    return true;
  }

  boolean hasPlayerAtStation( int player, int k )
  {
    if ( k < 2 ) return false;
    if ( k > 31 ) return false;
    if ( k == 29 ) k = 24;
    return ( mBoard[k] * player > 0 );
  }

  boolean hasPlayerOnlyAtStation( int player, int k )
  {
    if ( mBoard[k] * player <= 0 ) return false;
    for ( int j = 2; j < 32; ++ j ) {
      if ( j == k ) continue;
      if ( j == 29 ) continue;
      if ( mBoard[j] * player > 0 ) return false;
    }
    return true;
  }

  boolean canMovePlayer( int player, int from, int to, int m )
  {
    if ( from > 1 && from < 32 && mBoard[from]*player == 0 ) return false; 
    // if ( YutnoriPrefs.isDoSpot() && from == 2 && to == 21 && m == -1 ) return true; // DO_SPOT to CHAM_MEOKI
    // if ( /* YutnoriPrefs.isBackDo() > 0 && */ from + m <= 1 && to <= 1 ) return true; // TITO
    if ( to <= 1 ) to = 32;
    int[] pos = new int[2];
    nextPositions( from, m, pos );
    return ( pos[0] == to || pos[1] == to );
  }

  // total move required to go between two position: used only by Main
  // @from first position index
  // @to   second position index
  // @return 0 if cannot go
  //         HOME + min moves to go out (moving straight)
  //         START - max moves to go back to START
  //         BACK  - max moves to go back 
  int posDifference( int from, int to )
  {
    // Log.v( TAG, name() + " Pos diff from " + from + " to " + to );
    // if ( YutnoriPrefs.isDoSpot() && from == 2 && to == 21 ) { // DO_SPOT to CHAM_MEOKI
    //   return BACK1;
    // }
    if ( from == 0 || from == 1 ) {
      if ( to >= 2 && to < 21 ) return to - from;
      if ( to == 21 ) return 14;
      if ( to == 24 ) return 8;
      if ( to >= 22 && to <= 26 ) return (to-21) + 10;
      if ( to >= 27 && to <= 31 ) return (to-26) +  5;
    } else if ( from == 6 ) { // first corner
      if ( YutnoriPrefs.isBackDo() ) {
        if ( to == 5 ) return BACK1;
      }
      if ( to == 24 ) return 3;
      if ( to >=  7 && to <  16 ) return to - 6;     // B2 and B3
      if ( to >= 16 && to <= 21 ) return 6 + to - 16;   // B4
      if ( to >= 24 && to <= 26 ) return 3 + to - 24;   // (D3) + C + D2
      if ( to >= 27 && to <= 31 ) return 0 + to - 26;   // D3 & D4
      if ( to == 32 || to == 33 ) return HOME + 11;                 // (D3 + D4 + B4) or (D3 + C + D2)
    } else if ( from == 11 ) { // second corner
      if ( YutnoriPrefs.isBackDo() ) {
        if ( to == 10 ) return BACK1;
      }
      if ( to >= 12 && to < 21 ) return to - 11;
      if ( to == 21 ) return 6;
      if ( to >= 22 && to <= 26 ) return 0 + to - 21;
      if ( to == 30 || to == 31 ) return 3 + to - 29;
      if ( to == 32 || to == 33 ) return HOME + 7;
    } else if ( from == 16 ) { // third corner
      if ( YutnoriPrefs.isBackDo() ) {
        if ( to == 15 || to == 31 ) return BACK1;
      }
      if ( to >= 17 && to <= 21 ) return to - 16;
      if ( to == 24 ) to = 29;
      if ( to >= 28 && to <= 31 ) return -(32-to);
      if ( to == 32 || to == 33 ) return HOME + 6;
    } else if ( from == 21 ) { // fourth corner
      if ( YutnoriPrefs.isBackDo() ) {
        if ( to == 20 || to == 26 ) return BACK1;
      }
      if ( to == 32 || to == 33 ) return HOME + 1;
    } else if ( from == 24 ) { // middle crossway
      if ( YutnoriPrefs.isBackDo() ) {
        if ( to == 23 || to == 28 ) return BACK1;
      }
      if ( to >= 16 && to <  21 ) return 3 + to - 16;
      if ( to == 21 ) return 3;
      if ( to == 25 || to == 26 ) return to - 24;
      if ( to == 30 || to == 31 ) return to - 29;
      if ( to == 32 || to == 33 ) return HOME + 4;
    } else if ( from >= 22 && from <= 26 ) { // second diagonal D1 & D2
      if ( YutnoriPrefs.isBackDo() ) {
        if ( from == 22 ) {
          if ( to == 11 ) return BACK1;
        } else {
          if ( to == from - 1 ) return BACK1;
        }
      }
      if ( to == 21 ) return 27 - from;
      if ( to >  22 && to <= 26 ) return to - from;
      if ( to == 32 || to == 33 ) return HOME + (28 - from);
    } else if ( from >= 27 && from <= 31 ) { // first diagonal D3 & D4
      if ( to == 24 ) to = 29;
      if ( YutnoriPrefs.isBackDo() ) {
        if ( from == 27 ) {
          if ( to == 6 ) return BACK1;
        } else {
          if ( to == from - 1 ) return BACK1;
        }
      }
      if ( to >= 16 && to <= 21 ) return to - 16 + 32 - from;
      if ( to > from && to <= 31 ) return to - from;
      if ( to == 32 || to == 33 ) return HOME + 6 + (32 - from);
    } else if ( from >= 2 && from < 21 ) { // board 1-4
      if ( YutnoriPrefs.isBackDo() ) {
        if ( from == 2 ) {
          if ( to <= 1 || to >= 34 ) return START - 1;
        } else { 
          if ( to == from - 1 ) return BACK1;
        }
      }
      if ( to > from && to <= 21 ) return to - from;
      if ( to == 32 || to == 33 ) return HOME + (22 - from);
    }
    return 0;
  }

  int distance( int from, int to )
  {
    if ( from == 0 ) {
      if ( to <= Indices.POS_CORNER1 ) return Probability.value(to-1); // corner-1 is bottom-right
      return 0;
    }
    int t = to;
    if ( from <= Indices.POS_CORNER4 ) { // from = 2 3 4 5 6
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
      } else if ( to < from ) {
        if ( t < 2 ) return Probability.value( from );
        return 1; // low prob.
      }
    } else if ( from < 27 ) {  // 27-31 is the first diagonal
      if ( t == 21 ) {         // last corner: 27
        t = 27;
      } else if ( t < 20 ) {   // border: border+16
        t += 16;
      }
      if ( from == 24 ) { // diagonal cross-point
        if ( t >= 30 ) {  // 30: 1,  31:2
          t = t - 29;     
        }
        if ( t > 0  && t <= 5 ) return Probability.value(t);
        if ( t < 0 ) return 1;
      } else if ( t <= 27 ) {
        t = t - from;
        if ( t > 0  && t <= 5 ) return Probability.value(t);
        if ( t < 0 ) return 1;
      }
      return 0;
    } else { // border (no-lower row) or second diagonal
      if ( t == 24 ) { 
        t = 29;
      } else if ( t >= 16 && t < 21 ) {
        t = t-16+32;
      }
      t = t - from;
      if ( t > 0  && t <= 5 ) return Probability.value(t);
      if ( t < 0 ) return 1;
    }
    return 0;
  }

  // @param pawn_nr   0 to use all the pawns, or the (positive) number of pawns to move
  boolean doMove( int from, int to, int player, int pawn_nr )
  {
    boolean ret = false;
    int me  = Indices.yut_index(player);
    int you = Indices.yut_index(-player);
    if ( from <= 1 ) {
      assert( mStart[me]>0 );
    } else {
      assert( mBoard[from]*player > 0 );
    }
    // Log.v( TAG, name() + " Do Move player " + player + " from " + from + " to " + to );
    synchronized( this ) {
      if ( to > 1 && to < 34 ) {
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
          if ( pawn_nr == 0 ) pawn_nr = mBoard[from] * player;
          assert ( pawn_nr <= Math.abs(mBoard[from]) );
          mBoard[to]   += pawn_nr * player;
          mBoard[from] -= pawn_nr * player;
          //  mBoard[from] = 0;
        }
        if ( to == Indices.POS_HOME ) { // got mHome
          mHome[ me ] += Math.abs( mBoard[to] );
          // Log.v( TAG, name() + " moved to home " + mHome[0] + "/" + mHome[1] + mBoard[to] );
          mBoard[to] = 0;
        }
      } else { // to == 0, 1 or to == 34, 35
        if ( from > 1 ) {
          // if ( YutnoriPrefs.isDoSpot() ) {
          //   // Log.v(TAG, name() + " Player " + player + " move to DoSpot from " + from + " mal " + pawn_nr );
          //   doMoveToCage( from, player, pawn_nr ); // DO_SPOT CAGE
          // } else 
          {
            int b = mBoard[from];
            // Log.v(TAG, name() + " Player " + player + " move to Start from " + from + " mal " + pawn_nr + " " + b);
            if ( b*player > 0 ) { 
              mStart[ me ] += Math.abs(b);
              mBoard[from] = 0;
            }
          }
        }
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
  void nextPositions( int from, int mov, int pos[] )
  {
    pos[0] = -1;
    pos[1] = -1;
    if ( mov <= 0 && ! YutnoriPrefs.isBackDo() ) return;

    if ( mov > 0 ) {
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
    } else { // mov < 0 
      if ( from == 2 && from+mov < 2 ) {
        pos[0] = 1; // DO_SPOT to START
      } else if ( from == 16 ) {
        pos[0] = 16 + mov;
        pos[1] = 32 + mov;
      } else if ( from == 21 ) {
        pos[0] = 21 + mov;
        pos[1] = 27 + mov;
      } else if ( from == 24 ) {
        pos[0] = 24 + mov;
        pos[1] = 29 + mov;
      } else if ( from > 2 && from <= 21 ) {
        pos[0] = from + mov;
      // } else if ( from == 22 ) {
      //   pos[0] = 12 + mov;
      } else if ( from >= 22 && from < 27 ) {
        pos[0] = from + mov; if ( pos[0] <= 21 ) pos[0] -= 10;
      // } else if ( from == 27 ) {
      //   pos[0] = 7 + mov;
      } else if ( from >= 27 && from < 32 ) {
        pos[0] = from + mov; if ( pos[0] <= 26 ) pos[0] -= 20;
        if ( pos[0] == 29 ) pos[0] = 24;
      }
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

  // @param player    player number (+1 USER , -1 ANDROID)
  // @param state     (optional) state to update
  // @param moves
  // @param clear     if set clear moves when necessary
  // @return new state or -1 if no action has been taken
  int checkBackDo( int player, State state, Moves moves, boolean clear, ISender sender ) 
  {
    return State.NONE;
  }

}

