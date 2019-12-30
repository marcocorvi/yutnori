/** @file Strategy2.java
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
 *  This file implements the PC yutnori strategy
 *
 * Two different weight are considered: the weight at the "from" position and the weight at 
 * the "to" position. Each weight is computed from the base weight using the state of the
 * board:
 *   - the presence of the opponent inceases the weight of the position (WF_GET) and at
 *     the preceeding positions (WF_GET2)
 *     and decreases those of the following positions (WF_GOT)
 *   - increase the weight wher ethere are already this player pawns
 */
package com.yutnori;

import android.util.Log;

class Strategy2 extends Strategy
{
  Strategy2( Board b, Moves m, DrawingSurface s, int p ) 
  {
    super( b, m, s, p );
  }

  private static final float WF_GET    = 0.8f; // weight increment to get you
  private static final float WF_GET2   = 0.2f; // weight increment to get you
  private static final float WF_GOT    = 0.33f; // weight increment to get caught by you
  private static final float WF_ADD    = 0.9f; // weight incrememt to join me
  private static final float WF_JOIN   = 1.2f; // score increment to join me forward
  private static final float WF_JOIN0  = 0.8f; // score increment to join me at the beginning
  private static final float WF_HOME   = 1.5f; // score increment to get home
  private static final float WF_DANGER = 0.50f;
  private static final float WF_START  = 0.30f;
  private static final float WF_SEOUL  = 1.80f;
  private static final float WF_BUSAN  = 1.00f;
  
  private static final float SCORE_MIN = -1000f;

  private static float getFactors[] = { 1.0f, 1.6f, 1.3f, 1.1f, 1.0f };


  void updateWeight( Weight w, Board board )
  {
    float get = getFactors[ mBoard.start( Indices.yut_index( opponent() ) ) ];
    for (int k=1; k< Indices.POS_HOME; ++k ) { // POS_HOME=32 is over the end of the board
      if ( k == Indices.POS_SKIP ) continue;
      int b = mBoard.value(k);
      if ( b * player() < 0 ) {
        if ( k <= Indices.POS_CORNER4 ) { // last corner
          w.add( k, Math.abs(b) * k * get * WF_GET );
          for (int k1=1; k1<=5 && k1+k<= Indices.POS_CORNER4; ++k1) {
            w.add(k1+k, -(k1+k) * WF_GOT * Probability.value(k1) );
          }
          for (int k1=1; k1<=5 && k-k1>1; ++k1 ) {
            int k2 = k - k1;
            w.add( k2, Math.abs(b) * (k2) * WF_GET2 * Probability.value(k1) * get );
          }
          if ( k == Indices.POS_CORNER3 ) {
            for (int k1=1; k1<=5; ++k1 ) {
              int k2 = 32 - k1;
              if ( k2 == 29 ) k2 = 24;
              w.add( k2, Math.abs(b) * (32-k1-16) * WF_GET2 * Probability.value(k1) * get );
            }
          } else if ( k == Indices.POS_CORNER4 ) {
            for (int k1=1; k1<=5; ++k1 ) {
              int k2 = 27 - k1;
              w.add( k2, Math.abs(b) * (k2-6) * WF_GET2 * Probability.value(k1) * get );
            }
          }
        } else if ( k <= 26 ) { // regular outer board
          w.add( k, Math.abs(b) * (k-6) * WF_GET * get );
          for (int k1=1; k1<=5 && k1+k<=27; ++k1) {
            int k2 = k1+k;
            if ( k2 == 27 ) k2 = Indices.POS_CORNER4;
            w.add( k2, -(k1+k-6) * WF_GOT * Probability.value(k1) );
          }
          for (int k1=1; k1<=5 && k-k1>=21; ++k1 ) {
            int k2 = k - k1;
            if ( k2 == 21 ) k2 = 11;
            w.add( k2, Math.abs(b) * (k-k1-6) * WF_GET2 * Probability.value(k1) * get );
          }
          // if ( k == 1 ) Log.v("Yutnori-TITO", "w2[1] " + w.value(k) );
        } else {
          w.add( k, Math.abs(b) * (k-16) * WF_GET * get );
          for (int k1=1; k1<=5; ++k1 ) {
            int k2 = k1+k;
            if ( k2 == Indices.POS_SKIP ) k2 = Indices.POS_CENTER;
            if ( k2 >= Indices.POS_HOME ) k2 -= 16;
            w.add( k2, -(k1+k-16) * WF_GOT * Probability.value(k1) );
          }
          for (int k1=1; k1<=5 && k-k1>=26; ++k1) {
            int k2 = k - k1;
            if ( k2 == 26 ) { k2 = 6; }
            else if ( k2 == 29 ) { k2 = 24; }
            w.add( k2, Math.abs(b) * (k-k1-16) * WF_GET2 * Probability.value(k1) * get );
          }
        }
      } else if ( b*player() > 0 ) {
        w.add( k, Math.abs(b) * Indices.distance(k) * WF_ADD );
        if ( k == Board.SEOUL && YutnoriPrefs.isSeoul() ) w.add( k, WF_SEOUL );
        else if ( k == Board.BUSAN && YutnoriPrefs.isBusan() ) w.add( k, WF_BUSAN );
      }
    }
  }

  class Trial
  {
    float s; // score
    int k;   // move index
    int f;   // from position
    int t;   // to position

    Trial() 
    {
      s = - 1000.0f;
      k = - 1;
      f = - 1;
      t = - 1;
    }
  
    void update( float s0, int k0, int f0, int t0 ) {
      if ( s0 > s ) {
        s = s0;
        f = f0;
        t = t0;
        k = k0;
      }
    }
  }
  
  @Override
  int doMovePlayer( Moves moves, int doze )
  {
    // Log.v( TAG, "Android [2] do Move Player " + moves.size() );
    if ( moves.size() == 0 ) return State.NONE;
    // moves.print();
    int s = checkSkips( moves );
    // Log.v(TAG, "Android[2] check skip returns " + s );
    if ( s != State.FALL_THRU ) return s;

    Trial[] tried = new Trial[33];
    for ( int k=0; k<33; ++k ) tried[k] = new Trial();
    Weight wei_from = new Weight();
    Weight wei_to   = new Weight();
    updateWeight( wei_to, mBoard );
    
    if ( mBoard.start( Indices.yut_index(player()) ) > 0 ) {
      for ( int k = 0; k < moves.size(); ++k) {
        int m = moves.getValue(k);
        if ( m < 0 ) continue;
        int t  = 1 + m;
        int tm = (m > 0)? 1+m : 0 ;
        // TODO compute the score to get from 0 to "t"
        computeScore( k, 0, t, m, wei_from, wei_to, tried[tm] );
      }
    }
    for (int f=2; f<32; ++f ) {
      if ( f == Indices.POS_SKIP ) continue;
      if ( mBoard.value(f) * player() > 0 ) {
        for ( int k = 0; k < moves.size(); ++k) {
          int m = moves.getValue(k);
          int kkm = k + m;
          if ( YutnoriPrefs.isDoSpot() && kkm == 1 ) kkm = 21; // DO_SPOT to CHAM_MEOKI
          
          int[] pos = new int[2];
          mBoard.nextPositions( f, m, pos );
          int t = pos[0];
          if ( t > 0 ) {
            computeScore( k, f, t, m, wei_from, wei_to, tried[t] );
            // Log.v(TAG, "score " + f + " - " + t + ": " + tried[t].s );
          }
          if ( ( t = pos[1] ) > 0 ) {
            computeScore( k, f, t, m, wei_from, wei_to, tried[t] );
            // Log.v(TAG, "score " + f + " - " + t + ": " + tried[t].s );
          }
        }
      }
    }
        
    float score = - 1000.0f;
    int kbest = -1;
    int fbest = -1;
    int tbest = 0;
    for (int m = 1; m<33; ++m ) {
      if ( tried[m].s > score ) {
        fbest = tried[m].f;
        tbest = tried[m].t;
        kbest = tried[m].k;
        score = tried[m].s;
      }
    }
  
    if ( tbest > 0 ) {
      // Log.v( TAG, "move " + fbest + " -> " + tbest + " " + kbest + " m " + moves.getValue(kbest) );
      moves.shift( kbest );
      boolean throw_again = do_move( fbest, tbest, 2*doze );
      mDrawingSurface.addPosition( tbest );
      Delay.sleep( doze );
      // moves.print("[2] after move");

      if ( mBoard.winner() != 0 ) return State.MOVE;
      if ( throw_again ) return State.THROW;
      if ( moves.size() > 0 ) return State.MOVE;
    } else {
      // Log.v( TAG, "[2] no best move ");
      return State.NONE;
    }
    return State.MOVE;
  }
  
  // @param f   from position
  // @param t   to position
  // @param m   move value
  // @param wei_from
  // @param wei_to
  private void computeScore( int k, int f, int t, int m, Weight wei_from, Weight wei_to, Trial trial )
  {
    float score = wei_to.value( t ) - wei_from.value( f );
    if ( mBoard.value(t) * player() < 0 ) {
      score += (float)( Math.abs( mBoard.value(t) ) * wei_from.value(t) );
    }
  
    // merge with my mals
    if ( mBoard.value(t) * player() > 0 ) {
      if ( t < 6 ) {
        if ( mBoard.start( Indices.yut_index( opponent() ) ) > 0 ) {
          score += (float)(2 - Math.abs( mBoard.value(t) ) ) * WF_JOIN0;
        } else {
          score -= Probability.value(t-1) * 0.33f;
        }
      } else {
        if ( mBoard.start( Indices.yut_index( opponent() ) ) > 0 ) {
          score += (float)( Math.abs( mBoard.value(t) ) * WF_JOIN ); 
        } else {
          score -= 2.0f;
        }
      }
    }
  
    // getting the opponents
    if ( mBoard.value(t) * opponent() > 0 ) {
      score += (float)( Math.abs(mBoard.value(t)) * wei_to.value(t) * 2.0f );
    }
  
    // joining me
  
  
    // avoid the opponent mals and try to stay behind them
    if ( t <= 21 ) {
      for (int k0=1; k0<=5; ++k0 ) {
        int k1 = t-k0;
        if ( k1 >= 2 && mBoard.value(k1) * player() < 0 ) {
          score -= Probability.value(k0) * 0.33f;
        }
      }
      if ( t == 21 ) {
        for (int k0=1; k0<=5; ++k0 ) {
          int k1 = 27 - k0;
          if ( mBoard.value(k1) * player() < 0 ) {
            score -= Probability.value(k0) * 0.33f;
          }
        }
      } else if ( t == 16 ) {
        for (int k0=1; k0<=5; ++k0 ) {
          int k1 = 32 - k0;
          if ( k1 == 29 ) k1 = 24;
          if ( mBoard.value(k1) * player() < 0 ) {
            score -= Probability.value(k0) * 0.33f;
          }
        }
      }
    } else if ( t < 27 ) {
      for ( int k0=1; k0<=5; ++k0 ) {
        int k1 = t-k0;
        if ( k1 < 21 ) break;
        if ( k1 == 21 ) k1 = 11;
        if ( mBoard.value(k1) * player() < 0 ) {
          score -= Probability.value(k0) * 0.33f;
        }
      }
    } else if ( t < 32 ) {
      for ( int k0=1; k0<=5; ++k0 ) {
        int k1 = t-k0;
        if ( k1 < 26 ) break;
        if ( k1 == 26 ) k1 = 6;
        if ( k1 == 29 ) k1 = 24;
        if ( mBoard.value(k1) * player() < 0 ) {
          score -= Probability.value(k0) * 0.33f;
        }
      }
    } else {
      // score to go home
      int waste = ( f < 22 ) ? 22 - f : 28 - f;
      waste = m - waste;
      score += (float)( Math.abs( mBoard.value(f) ) * ( 5 - waste) );
    }
    if ( score > trial.s ) trial.update( score, k, f, t );
    // return score;
  }

}
