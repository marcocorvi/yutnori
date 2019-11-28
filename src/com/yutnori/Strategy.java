/** @file Strategy.java
 *
 * @author marco corvi
 * @date dec 2010
 *
 * @brief Yutnori strategy
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.yutnori;

import android.util.Log;

class Strategy extends Player
{
  static final String TAG = "Yutnori-TITO";

  Strategy( Board b, Moves m, DrawingSurface s, int p ) 
  {
    super( b, m, s, p );
  }

  // TITO skip -----------------------------------------------------------
  // protected int mSkip = 0;

  // boolean setSkip( Moves m ) 
  // { 
  //   if ( m.removeSkip() ) mSkip++;
  //   return mSkip > 0;
  // }
  // int getSkip() { return mSkip; }

  boolean mustSkip() 
  { 
    if ( State.isSkipping( Player.ANDROID ) ) {
      State.clearSkipping( Player.ANDROID );
      return true;
    }
    return false;
  }

  // void assert( boolean condition ) { }

  float positionDanger( int at )
  {
    float danger = 0.0f;
    if ( mBoard.start( Indices.yut_index( opponent() ) ) > 0 ) {
      danger += mBoard.distance( 0, at );
    }
    for (int k=2; k < Indices.POS_HOME; ++k ) {
      if ( k == Indices.POS_SKIP ) continue;
      if ( mBoard.value(k) * player() < 0 ) {
        danger += mBoard.distance( k, at );
      }
    }
    return danger;
  }

 
  // return final state
  int movePlayer( Moves moves, int doze )
  {
    // moves.print( "ANDROID" );
    while ( moves.size() > 0 && mBoard.winner() == 0 ) { // TODO find all the possible composite moves
      int state = State.checkSkip( Player.ANDROID, null, moves, true );
      if ( state >= 0 ) {
        // Log.v(TAG, "ANDROID has skipped - state READY");
        Delay.sleep( 2 * doze );
        return state; // -1;
      }
      state = State.checkBackDo( Player.ANDROID, null, mBoard, moves, true );
      // Log.v(TAG, "ANDROID check back do - state " + state ); // State.toString(state) );
      if ( state >= 0 ) {
        Delay.sleep( 2 * doze );
        if ( state == State.THROW ) return State.THROW; //  1;
        if ( state != State.MOVE  ) return State.NONE;  // -1;
      }
      state = doMovePlayer( moves, doze );
      // Log.v(TAG, "ANDROID move player - state " + state ); // State.toString(state) );
      if ( state != State.MOVE ) return state;
    }
    return State.MOVE;
  }

  static final float W_GET   = 7.0f;
  static final float W_JOIN  = 2.5f;
  static final float W_MOVE  = 2.0f;
  static final float W_HOME  = 4.5f;
  static final float W_GMOVE = 1.0f;
  static final float W_JMOVE = 0.5f;
  static final float W_DIAG1 = 1.3f;
  static final float W_DIAG2 = 1.6f;
  static final float W_START  = 0.9f;
  static final float W_STARTJ = 0.8f;
  static final float W_STARTG = 1.2f;
  static final float W_CORNER = 1.4f;
  static final float W_DANGER = 0.4f;

  static final int[] mDanger = { 0, 4, 6, 4, 1, 1 };
  static final int[] mMHome  = { 0, 6, 3, 2, 1, 1 };

  // to be overridden
  int doMovePlayer( Moves moves, int doze )
  {
    // Log.v( TAG, "Android [0] do Move Player " + moves.size() );
    moves.print("Android [0]");

    if ( moves.size() == 0 ) return State.READY;
    float score = -100;
    int fbest = -1;
    int tbest = -1;
    int jbest = -1;
    int me = player();

    int k;
    for (k=2; k<=21; ++k ) {
      int kf = k;
      if ( mBoard.hasPlayerAtStation( me, kf ) ) {
        for ( int j = 0; j < moves.size(); ++j ) {
          int m = moves.getValue( j );
          int kkm = k + m;
          float f = ( (kkm % 5) == 1 )? W_CORNER : 1;
          float s = score;
          if ( kkm >= 2 && kkm <= 21 ) {
            int b = mBoard.getStationValue( kkm );
            if ( b*me < 0 )      { s = W_GET  * Math.abs(b) + W_GMOVE * m; }
            else if ( b*me > 0 ) { s = W_JOIN * Math.abs(b) + W_JMOVE * m; }
            else { s = W_MOVE * m; }
            s *= f;
            for ( int jj = 1; jj <= 5; ++ jj ) {
              int kkmj = kkm - jj;
              if (kkmj < 2 ) break;
              if ( ( mBoard.getStationValue( kkmj ) * me ) < 0 ) s -= W_DANGER * mDanger[jj];
            }
            // TODO further dengers at the corners
          } else if ( m > 0 ) {
            kkm = 32;
            s = W_HOME * mMHome[m];
          }
          if ( s > score ) { score = s; fbest = kf; tbest = kkm; jbest = j; }
        }
      }
    }
    Log.v( TAG, "Android border " + jbest + ": " + fbest + " -> " + tbest + " " + score );
    for ( k = 21; k<27; ++k ) {
      int kf = k;
      if (kf == 21) kf = 11;
      if ( mBoard.hasPlayerAtStation( me, kf ) ) {
        for ( int j = 0; j < moves.size(); ++j ) {
          int m = moves.getValue( j );
          int kkm = k + m; 
          if ( m < 0 ) {
            if ( kkm == 21 ) kkm = 11;
            else if ( kkm == 20 ) kkm = 10;
          } else {
            if ( kkm == 27 ) kkm = 21;
          }
          float f = (kkm == 24 || kkm == 21)? W_CORNER : 1;
          float s = score;
          if ( kkm < 28 ) {
            int b = mBoard.getStationValue( kkm );
            if ( b*me < 0 )      { s = W_DIAG2 * W_GET  * Math.abs(b) + W_GMOVE * m; }
            else if ( b*me > 0 ) { s = W_DIAG2 * W_JOIN * Math.abs(b) + W_JMOVE * m; }
            else { s = W_DIAG2 * W_MOVE * m; }
            s *= f;
            for ( int jj = 1; jj <= 5; ++ jj ) {
              int kkmj = kkm - jj;
              if ( kkmj == 21 ) { kkmj = 11; }
              else if ( kkmj < 21 ) { break; }
              if ( ( mBoard.getStationValue( kkmj ) * me ) < 0 ) s -= W_DANGER * mDanger[jj];
            }
            // TODO further dengers at the center
          } else if ( kkm > 27 && m > 0 ) {
            kkm = 32;
            s = W_HOME * mMHome[m];
          }
          if ( s > score ) { score = s; fbest = kf; tbest = kkm; jbest = j; }
        }
      }
    }
    Log.v( TAG, "Android diag 1 " + jbest + ": " + fbest + " -> " + tbest + " " + score );
    for ( k = 26; k<32; ++k ) {
      if ( k == 29 ) continue;
      int kf = k;
      if (kf == 26) kf = 6;
      if ( mBoard.hasPlayerAtStation( me, kf ) ) {
        for ( int j = 0; j < moves.size(); ++j ) {
          int m = moves.getValue( j );
          int kkm = k + m; 
          if (kkm == 29) kkm = 24;
          else if ( kkm > 31 ) kkm = 16 + (kkm-32);
          else if ( kkm == 26 ) kkm = 6;
          else if ( kkm == 25 ) kkm = 5;
          float f = (kkm == 24 || kkm == 16)? W_CORNER : 1;
          float s = score;
          int b = mBoard.getStationValue( kkm );
          if ( b*me < 0 )      { s = W_DIAG1 * W_GET  * Math.abs(b) + W_GMOVE * m; }
          else if ( b*me > 0 ) { s = W_DIAG1 * W_JOIN * Math.abs(b) + W_JMOVE * m; }
          else { s = W_DIAG1 * W_MOVE * m; }
          s *= f;
          for ( int jj = 1; jj <= 5; ++ jj ) {
            int kkmj = kkm - jj;
            if (kkmj == 29 ) { kkmj = 24; }
            else if ( kkmj == 26 ) { kkmj = 6; }
            else if ( kkmj < 26 ) { break; }
            if ( ( mBoard.getStationValue( kkmj ) * me ) < 0 ) s -= W_DANGER * mDanger[jj];
          }
          if ( s > score ) { score = s; fbest = kf; tbest = kkm; jbest = j; }
        }
      }
    } 
    Log.v( TAG, "Android diag 2 " + jbest + ": " + fbest + " -> " + tbest + " " + score );
    if ( mBoard.playerStart( me ) > 0 ) {
      int kf = 0;
      for ( int j = 0; j < moves.size(); ++j ) {
        int m = moves.getValue( j );
        if ( m > 0 ) {
          int kkm = 1 + m;
          float f = (kkm == 6)? W_CORNER : 1;
          float s = score;
          int b = mBoard.getStationValue( kkm );
          if ( b*me < 0 )      { s = W_STARTG * W_GET  * Math.abs(b) + W_GMOVE * m; }
          else if ( b*me > 0 ) { s = W_STARTJ * W_JOIN * Math.abs(b) + W_JMOVE * m; }
          else { s = W_START * W_MOVE * m; }
          s *= f;
          for ( int jj = 1; jj <= 5; ++ jj ) {
            if ( kkm - jj < 2 ) break;
            if ( ( mBoard.getStationValue( kkm -jj ) * me ) < 0 ) s -= W_DANGER * mDanger[jj];
          }
          if ( s > score ) { score = s; fbest = kf; tbest = kkm; jbest = j; }
        }
      }
    }
    Log.v( TAG, "Android move   " + jbest + ": " + fbest + " -> " + tbest + " " + score );
    if ( jbest < 0 ) {
      Log.v( TAG, "[0] cannot find best move " );
      return State.READY;
    }
    boolean throw_again = do_move( fbest, tbest, 2*doze );
    Log.v( TAG, "Android move " + jbest + ": " + fbest + " -> " + tbest + " throw again " + throw_again );
    mDrawingSurface.addPosition( tbest );
    moves.shift( jbest );
    Delay.sleep( 1 * doze );
    return throw_again ? State.THROW : State.MOVE;
  }

          

  /** randomize scores
   * @return a random number between 0.95 and 1.05
   */
  static float yut_random()
  {
    return 0.95f + 0.1f * (float)Math.random();
  }

}

