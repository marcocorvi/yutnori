/** @file BoardDoCage.java
 *
 * @author marco corvi
 * @date dec 2010
 *
 * @brief yutnori board with Do-Cage
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.yutnori;

import android.util.Log;

class BoardDoCage extends BoardDoNone
{
  BoardDoCage()
  {
    super();
    // mCage = new int[2];
    reset();
  }

  BoardDoCage( short b[] ) { super( b ); }

  @Override
  protected short getBoardIndex() { return BOARD_DOCAGE; }

  @Override
  String name() { return "Do-Cage"; }

  @Override
  void reset()
  {
    super.reset();
    synchronized( this ) {
      if ( mCage != null ) {
        mCage[0] = 0;
        mCage[1] = 0;
      }
    }
  }

  int doCage( int k ) { return mCage[k]; }

  int playerDoCage( int player ) { return mCage[ Indices.yut_index( player ) ]; }


  boolean canMovePlayer( int player, int from, int to, int m )
  {
    if ( from > 1 && from < 32 && mBoard[from]*player == 0 ) return false; 
    // if ( YutnoriPrefs.mDoSpot && from == 2 && to == 21 && m == -1 ) return true; // DO_SPOT to CHAM_MEOKI
    if ( YutnoriPrefs.isDoCage() && from + m <= 1 && to <= 1 ) return true; // DO_CAGE
    return super.canMovePlayer( player, from, to, m );
  }

  boolean startToCage( int k ) // DO_SPOT CAGE
  {
    if ( mStart[k] == 0 ) return false;
    mStart[k] --;
    mCage[k] ++;
    return true;
  }

  void doMoveToCage( int from, int player, int pawn_nr ) // DO_SPOT CAGE
  {
    int me  = Indices.yut_index(player);
    if ( from <= 1 ) {
      mStart[ me ]  -= pawn_nr;
      mCage[ me ] += pawn_nr;
    } else if ( from == 2 ) {
      mBoard[ from ] -= pawn_nr * player;
      mCage[ me ]  += pawn_nr;
    }
    // Log.v( TAG, name() + " move from " + from + " to cage: mals " + mCage[me] );
  }

  // return -1 error
  //         0 ok move
  //         1 ok move and sent opponent back to START
  int doMoveFromCage( int player )
  {
    int me  = Indices.yut_index(player);
    int pawn_nr = mCage[ me ];
    mCage[ me ] = 0;
    // Log.v(TAG, name() + " player " + player + " move from do-cage mals " + pawn_nr );
    if ( pawn_nr < 1 ) return -1; // cannot move from DoSpot
    int b21 = mBoard[21] * player;
    if ( b21 < 0 ) {
      int you = Indices.yut_index(-player);
      mStart[ you ] -= b21;
      mBoard[21] = pawn_nr * player;
      return 1;
    }
    mBoard[21] = pawn_nr * player;
    return 0;
  }

  @Override
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
    // Log.v( TAG, name() + " do move " + player + " form " + from + " to " + to );
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
        } else if ( from >= 34 ) {
          doMoveFromCage( player ); // DO_SPOT CAGE
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
        if ( from < 2 ) {
          doMoveToCage( from, player, pawn_nr ); // DO_SPOT CAGE
        } else if ( from == 2 ) {
          // if ( YutnoriPrefs.isDoCage() ) { // This moves from Do-Spot to Do-Cage
          //   // Log.v(TAG, name() + " player " + player + " move to DoSpot from " + from + " mal " + pawn_nr );
          //   doMoveToCage( from, player, pawn_nr ); // DO_SPOT CAGE
          // } else 
          { // This moves from Do-Spot to START
            int b = mBoard[from];
            // Log.v(TAG, name() + " player " + player + " move to Start from " + from + " mal " + pawn_nr + " " + b);
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

  // @param player    player number (+1 USER , -1 ANDROID)
  // @param state     (optional) state to update
  // @param moves
  // @param clear     if set clear moves when necessary
  // @return new state or -1 if no action has been taken
  @Override
  int checkBackDo( int player, State state, Moves moves, boolean clear, ISender sender ) 
  {
    int ret = State.NONE;
    // moves.print( name() + " check back-do (" + player + ")" );
    if ( ! YutnoriPrefs.isDoCage() ) {
      return ret;
    }
    int k = moves.getSkip();
    if ( this.countPlayer( player ) == 0 ) {
      if ( this.playerStart( player ) == 0 ) {
        if ( moves.removeSkip() ) {
          // Log.v(TAG, name() + " skip from cage [1] " );
          int move = this.doMoveFromCage( player );
          if ( sender != null ) sender.sendMyMove( k, 34, 21, 1 );
          if ( move == 1 ) {
            ret = State.THROW;
          } else if ( move == 0 ) {
            ret = ( moves.size() > 0 )? State.MOVE : State.READY;
          } else {
            // Log.v(TAG, name() + " ERROR State bad doMoveFromCage");
            moves.clear();
            ret = State.READY;
          }
        } else {
          // Log.v(TAG, name() + "Do Cage: must skip [1] " + player );
          // State.setSkipping( player );
          if ( clear ) moves.clear();
          if ( sender != null ) sender.sendMySkip( clear );
          ret = State.SKIP;
        }
      } else { // board is empty - start is not empty
        if ( this.playerDoCage( player ) > 0 ) {
          if ( moves.removeSkip() ) {
            // Log.v(TAG, name() + " skip from cage [2]" );
            int move = this.doMoveFromCage( player );
            if ( sender != null ) sender.sendMyMove( k, 34, 21, 1 );
            if ( move == 1 ) { // sent opponent to START
              ret = State.THROW;
            } else if ( move == 0 ) {
              ret = ( moves.size() > 0 )? State.MOVE : State.READY;
            } else {
              // Log.v(TAG, name() + " ERROR State bad doMoveFromCage");
              moves.clear();
              ret = State.READY;
            }
          } else {
            Log.i(TAG, name() + " ERROR no skip [2] " + player );
          }
        } else { // do-cage is empty
          if ( moves.hasAllSkips() ) {
            // Log.v(TAG, name() + " move to cage " + player );
            this.doMoveToCage( 1, player, 1 );
            if ( sender != null ) sender.sendMyMove( k, 0, 34, 1 );
            if ( clear ) moves.clear();
            ret = State.READY;
          } else {
            // normal move
            // Log.v(TAG, name() + " normal[1] " + player );
          }
        }
      }
    } else { // board not empty
      if ( this.playerDoCage( player ) > 0 ) {
        if ( moves.removeSkip() ) {
          // Log.v(TAG, name() + " skip from cage " + player );
          int move = this.doMoveFromCage( player );
          if ( sender != null ) sender.sendMyMove( k, 34, 21, 1 );
          if ( move == 1 ) {
            ret = State.THROW;
          } else if ( move == 0 ) {
            ret = ( moves.size() > 0 )? State.MOVE : State.READY;
          } else {
            // Log.v(TAG, name() + " STATE bad doMoveFromCage");
            moves.clear();
            ret = State.READY;
          }
        } else {
          // Log.v(TAG, name() + " normal[2] " + player );
          // normal
        }
      } else if ( this.hasPlayerOnlyAtStation( player, 2 ) && moves.hasAllSkips() ) {
        // Log.v(TAG, name() + "[3a] player " + player + " only at 2");
        ret = State.TO_START;
      } else {
        // normal
        // Log.v(TAG, name() + " normal[3] " + player );
      }
    }
    
    // Log.v( TAG, name() + " check back do returns " + State.toString(ret) );
    if ( ret >= 0 && state != null ) state.setState( ret );
    return ret;
  }

}

