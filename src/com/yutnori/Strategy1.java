/** @file Strategy1.java
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
 * The "best" move is searched by assigning scores to the differnt options
 *   1. moving out (to home)
 *      for every position of the player if there is a move to "home"
 *      the score is the number of pawns on the postion times the weight to-home 
 *      times a coeff (WF_HOME)
 *   2. getting to an opponent position
 *      the score is the weight-difference of advancing (times the number of player pawns)
 *      plus the weight of the opponent pawns
 *   3. moving forward
 *      the score is the difference of the weights between the two positions (times
 *      the number of pawns) with an extra score for joining other player pawns:
 *      a coeff WF_JOIN (WF_JOIN0 from the "start") times a cut-off distance from the "start". 
 *      To this it is subtracted the danger of getting cought by the opponent (WF_DANGER)
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

class Strategy1 extends Strategy
{
  // final static String TAG = "Yutnori-TITO";

  private static final float WF_GET  = 0.8f; // weight increment to get you
  private static final float WF_GET2 = 0.2f; // weight increment to get you
  private static final float WF_GOT  = 0.33f; // weight increment to get caught by you
  private static final float WF_ADD  = 0.9f; // weight incrememt to join me
  private static final float WF_JOIN = 0.6f; // score increment to join me forward
  private static final float WF_JOIN0= 0.3f; // score increment to join me at the beginning
  private static final float WF_HOME = 1.5f; // score increment to get home
  private static final float WF_MOVE = 1.0f; // score decrement to get home with big moves
  private static final float WF_DANGER=0.50f;
  private static final float WF_START =0.30f;


  private static final float SCORE_MIN = -1000f;

  Strategy1( Board b, Moves m, DrawingSurface s, int p ) 
  {
    super( b, m, s, p );
  }

  private void updateWeightFrom( Weight w )
  {
    int k;
    for (k=2; k<21; ++k ) {
      if ( mBoard.value(k) * player() > 0 ) { // my position
        for (int k1=1; k1<=5 && k-k1 > 1; ++k1) {
          if ( mBoard.value(k-k1) * player() < 0 ) { // other position
            w.add(k, WF_GOT * Probability.value(k1) );
          }
        }
      }
    }
    k = 21;
    int kk = 27;
    if ( mBoard.value(k) * player() > 0 ) { // my position
      for (int k1=1; k1<=5; ++k1) {
        if ( mBoard.value(k-k1) * player() < 0 ) { // other position
          w.add(k, WF_GOT * Probability.value(k1) );
        }
        if ( mBoard.value(kk-k1) * player() < 0 ) { // other position
          w.add(k, WF_GOT * Probability.value(k1) );
        }
      }
    }
    for (k=22; k<=26; ++k ) {
      if ( mBoard.value(k) * player() > 0 ) { // my position
        for (int k1=1; k1<=5; ++k1) {
          int kk1 = k - k1; if ( kk1 <= 21 ) kk1 -= 10;
          if ( mBoard.value(kk1) * player() < 0 ) { // other position
            w.add(k, WF_GOT * Probability.value(k1) );
          }
        }
        if ( k == 24 ) {
          for (int k1=1; k1<=5; ++k1) {
            int kk1 = 29 - k1; if ( kk1 <= 26 ) kk1 -= 20;
            if ( mBoard.value(kk1) * player() < 0 ) { // other position
              w.add(k, WF_GOT * Probability.value(k1) );
            }
          }
        }
      }
    }
    for (k=27; k<=31; ++k ) {
      if ( k == 29 ) continue;
      if ( mBoard.value(k) * player() > 0 ) { // my position
        for (int k1=1; k1<=5; ++k1) {
          int kk1 = k - k1; if ( kk1 <= 26 ) kk1 -= 20; if (kk1 == 29) kk1 = 24;
          if ( mBoard.value(kk1) * player() < 0 ) { // other position
            w.add(k, WF_GOT * Probability.value(k1) );
          }
        }
      }
    }
    w.set29();
  }

  private void updateWeightTo( Weight w )
  {
    for (int k=1; k< Indices.POS_HOME; ++k ) {
      if ( k ==  Indices.POS_SKIP ) continue;
      int b = mBoard.value(k);
      if ( b * player() < 0 ) {
        if ( k <=  Indices.POS_CORNER4 ) {
          w.add(k, Math.abs(b) * k * WF_GET);
          for (int k1=1; k1<=5 && k1+k<= Indices.POS_CORNER4; ++k1) {
            w.add(k1+k, -(k1+k) * WF_GOT * Probability.value(k1) );
          }
          for (int k1=1; k1<=5 && k-k1>1; ++k1 ) {
            int k2 = k - k1;
            w.add(k2, Math.abs(b) * k2 * WF_GET2 * Probability.value(k1) );
          }
          if ( k ==  Indices.POS_CORNER3 ) {
            for (int k1=1; k1<=5; ++k1 ) {
              int k2 = 32 - k1;
              if ( k2 == 29 ) k2 = 24;
              w.add(k2, Math.abs(b) * (32-k1-16) * WF_GET2 * Probability.value(k1) );
            }
          } else if ( k ==  Indices.POS_CORNER4 ) {
            for (int k1=1; k1<=5; ++k1 ) {
              int k2 = 27 - k1;
              w.add(27-k1, Math.abs(b) * (k2-6) * WF_GET2 * Probability.value(k1) );
            }
          }
        } else if ( k <= 26 ) {
          w.add(k, Math.abs(b) * (k-6) * WF_GET );
          for (int k1=1; k1<=5 && k1+k<=27; ++k1) {
            int k2 = k1+k;
            if ( k2 == 27 ) k2 =  Indices.POS_CORNER4;
            w.add(k2, -(k1+k-6) * WF_GOT * Probability.value(k1) );
          }
          for (int k1=1; k1<=5 && k-k1>=21; ++k1 ) {
            int k2 = k - k1;
            if ( k2 == 21 ) k2 = 11;
            w.add(k2, Math.abs(b) * (k-k1-6) * WF_GET2 * Probability.value(k1) );
          }
          // if ( k == 1 ) Log.v("Yutnori-TITO", "w1[1] " + w.value(k) );
        } else {
          w.add( k, Math.abs(b) * (k-16) * WF_GET );
          for (int k1=1; k1<=5; ++k1 ) {
            int k2 = k1+k;
            if ( k2 ==  Indices.POS_SKIP ) k2 =  Indices.POS_CENTER;
            if ( k2 >=  Indices.POS_HOME ) k2 -= 16;
            w.add( k2, -(k1+k-16) * WF_GOT * Probability.value(k1) );
          }
          for (int k1=1; k1<=5 && k-k1>=26; ++k1) {
            int k2 = k - k1;
            if ( k2 == 26 ) { k2 = 6; }
            else if ( k2 == 29 ) { k2 = 24; }
            w.add( k2, Math.abs(b) * (k-k1-16) * WF_GET2 * Probability.value(k1) );
          }
        }
      } else if ( b*player() > 0 ) {
        w.add(k, Math.abs(b) * Indices.distance(k) * WF_ADD );
      }
    }
    w.set29();
  }

  @Override
  int doMovePlayer( Moves moves, int doze )
  {
    // Log.v( TAG, "Android [1] do Move Player " + moves.size() );
    int max = moves.size();
    Moves composite_moves = new Moves();
    int kk = 0;
    for ( int k1=0; k1<max; ++k1) {
      composite_moves.add( moves.getValue(k1) );
    }
    for ( int k1=0; k1<max-1; ++k1 ) {
      for ( int k2=k1+1; k2<max; ++k2 ) {
        composite_moves.add( moves.getValue(k1) + moves.getValue(k2) );
      }
    }
    for ( int k1=0; k1<max-2; ++k1 ) {
      for ( int k2=k1+1; k2<max-1; ++k2 ) {
        for ( int k3=k2+1; k3<max; ++k3 ) {
          composite_moves.add( moves.getValue(k1) + moves.getValue(k2) + moves.getValue(k3) );
        }
      }
    }
    // Log.v( TAG, "android composite moves " + composite_moves.size() );
    composite_moves.sortUnique( );

    FromTo ft = new FromTo();
    int m0 = bestMove( composite_moves, ft );
    // composite_moves.print("Best " + m0 + " " + ft.from + "->" + ft.to );
    // Log.v( TAG, "best move " + m0 + " from " + ft.from + " to " + ft.to );
    if ( m0 >= 0 ) {
      if ( try1( moves, doze, m0, ft )
        || try2( moves, doze, m0, ft )
        || try3( moves, doze, m0, ft ) ) {
        return State.THROW; // 1; // throw again
      }
    } else { 
      // Log.v( TAG, "back move " + m0 );
      if ( YutnoriPrefs.mTiTo ) m0 = -1;
      if ( try1( moves, doze, m0, ft ) ) {
        return State.THROW; // 1;
      }
    }
    return State.MOVE;
  }

  boolean try1( Moves moves, int doze, int m0, FromTo ft )
  {
    if ( mBoard.winner() != 0 ) return false;
    for (int k1=0; k1<moves.size(); ++k1) {
      if ( m0 == moves.getValue(k1) ) {
        boolean ret = found1( moves, doze, mBoard, k1, ft );
        // Log.v( TAG, "   One at " + k1 + " found " + ret );
        return ret;
      }
    }
    return false;
  }

  boolean try2( Moves moves, int doze, int m0, FromTo ft )
  {
    if ( mBoard.winner() != 0 ) return false;
    if ( moves.size() < 2 ) return false;
    for (int k1=0; k1<moves.size(); ++k1 ) {
      for (int k2=0; k2<moves.size(); ++k2 ) {
        if ( k2 == k1 ) continue;
        if ( m0 == moves.getValue(k1)+moves.getValue(k2) ) {
          boolean ret = found2( moves, doze, mBoard, k1, k2, ft );
          // Log.v( TAG, "   Two at " + k1 + " " + k2 + " found " + ret );
          return ret;
        }
      }
    }
    return false;
  }

  boolean try3( Moves moves, int doze, int m0, FromTo ft )
  {
    if ( mBoard.winner() != 0 ) return false;
    if ( moves.size() < 3 ) return false;
    for (int k1=0; k1<moves.size(); ++k1 ) {
      for (int k2=0; k2<moves.size(); ++k2 ) {
        if ( k2 == k1 ) continue;
        for (int k3=0; k3<moves.size(); ++k3 ) {
          if ( k3 == k1 || k3 == k2 ) continue;
          if ( m0 == moves.getValue(k1)+moves.getValue(k2)+moves.getValue(k3) ) {
            boolean ret = found3( moves, doze, mBoard, k1, k2, k3, ft );
            // Log.v( TAG, "   Three at " + k1 + " " + k2 + " " + k3 + " found " + ret );
            return ret;
          }
        }
      }
    }
    return false;
  }

  private boolean found3( Moves moves, int doze, Board mBoard, int k1, int k2, int k3, FromTo ft )
  {
    int to3 = -1;
    int to2 = -1;
    int[] pos3 = new int[2];
    int[] pos2 = new int[2];
    int[] pos1 = new int[2];
    mBoard.nextPositions( ft.from, moves.getValue(k3), pos3 );
    if ( pos3[0] < 32 ) {
      mBoard.nextPositions( pos3[0], moves.getValue(k2), pos2 );
      if ( pos2[0] < 32 ) {
        mBoard.nextPositions( pos2[0], moves.getValue(k1), pos1 );
        if ( pos1[0] == ft.to || pos1[1] == ft.to ) { // can get to "to" in 3 step pos3[0]--pos2[0]--pos1[]
          to3 = pos3[0];
          to2 = pos2[0];
        } else if ( pos2[1] > 1 ) { // try from pos2[1]
          mBoard.nextPositions( pos2[1], moves.getValue(k1), pos1 );
          if ( pos1[0] == ft.to || pos1[1] == ft.to ) { // pos3[0]--pos2[1]--pos1[]
            to3 = pos3[0];
            to2 = pos2[1];
          }
        } else {
          mBoard.nextPositions( pos3[1], moves.getValue(k2), pos2 );
          mBoard.nextPositions( pos2[0], moves.getValue(k1), pos1 );
          if ( pos1[0] == ft.to || pos1[1] == ft.to ) { // pos3[1]--pos2[0]--pos1[]
            to3 = pos3[1];
            to2 = pos2[0];
          } else if ( pos2[1] > 1 ) {
            mBoard.nextPositions( pos2[1], moves.getValue(k1), pos1 );
            if ( pos1[0] == ft.to || pos1[1] == ft.to ) { // pos3[1]--pos2[1]--pos1[]
              to3 = pos3[1];
              to2 = pos2[1];
            }
          }
        }
      } else { 
        to3 = pos3[0];
        to2 = pos2[0];
      }
    } else {
      to3 = pos3[0];
    }
    assert( to3 > 1 );
    
    boolean throw_again = do_move( ft.from, to3, 2*doze );
    mDrawingSurface.addPosition( to3 );
    moves.shift( k3 );
    Delay.sleep( 1 * doze ); // was 1

    ft.from = to3; 
    if ( mBoard.winner() != 0 ) return false;
    if ( throw_again )          return true;
    if ( moves.size() == 0 )    return false;
    return found2( moves, doze, mBoard, k1, k2, ft ); // 3-step failed try 2-step
  }

  private boolean found2( Moves moves, int doze, Board mBoard, int k1, int k2, FromTo ft )
  {
    int to2 = -1; // FIXME recompute pos2
    if ( to2 == -1 ) {
      int[] pos2 = new int[2];
      mBoard.nextPositions( ft.from, moves.getValue(k2), pos2 );
      if ( pos2[0] < 32 ) {
        int[] pos1 = new int[2];
        mBoard.nextPositions( pos2[0], moves.getValue(k1), pos1 );
        if ( pos1[0] == ft.to || pos1[1] == ft.to ) {
          to2 = pos2[0];
        } else {
          to2 = pos2[1];
        }
        assert( to2 > 1 );
      } else {
        to2 = pos2[0];
      }
    }

    boolean throw_again = do_move( ft.from, to2, 2*doze );
    mDrawingSurface.addPosition( to2 );
    moves.shift( k2 );
    Delay.sleep( 1 * doze );

    ft.from = to2;
    if ( mBoard.winner() != 0 ) return false;
    if ( throw_again )          return true;
    if ( moves.size() == 0 )    return false;
    return found1( moves, doze, mBoard, k1, ft );
  }

  private boolean found1( Moves moves, int doze, Board mBoard, int k1, FromTo ft )
  {
    boolean throw_again = do_move( ft.from, ft.to, 2*doze );
    mDrawingSurface.addPosition( ft.to );
    moves.shift( k1 );
    Delay.sleep( 1 * doze );

    if ( mBoard.winner() != 0 ) return false;
    if ( throw_again )          return true;
    if ( moves.size() == 0 )    return false;
    return false;
  }

  private int bestMove( Moves moves, FromTo ft0 )
  {
    float score = SCORE_MIN;
    FromTo ft   = new FromTo();
    int ret = -1;
    Weight wei_from = new Weight();
    Weight wei_to   = new Weight();
    updateWeightFrom( wei_from );
    updateWeightTo( wei_to );
  
    for ( int k = 0; k < moves.size(); ++k) {
      int v   = moves.getValue(k);
      float s;
      if ( v > 0 ) {
        s  = movingHomeScore( v, ft, wei_from );
        // Log.v( TAG, "move " + v + " " + ft.from + "-" + ft.to + ": home score " + s );
        // if ( ft.from >= 0 ) Log.v("yutnori", " weights " + wei_from.value(32) + " " + wei_from.value( ft.from ) );
        if ( s > score ) {
          score = s;
          ft0.from = ft.from;
          ft0.to   = Indices.POS_HOME;
          ret = moves.getValue(k);
        }
      }

      s = movingBackScore( v, ft, wei_from, wei_to );
      // Log.v( TAG, "move " + v + " " + ft.from + "-" + ft.to + ": back score " + s );
      if ( s > score ) {
        score = s;
        ft0.from = ft.from;
        ft0.to   = ft.to;
        ret = moves.getValue(k);
      }

      s = movingForScore( v, ft, wei_from, wei_to );
      // Log.v( TAG, "move " + v + " " + ft.from + "-" + ft.to + ": fore score " + s );
      if ( s > score ) {
        score = s;
        ft0.from = ft.from;
        ft0.to   = ft.to;
        ret = moves.getValue(k);
      }

      s = movingStartScore( v, ft, wei_from, wei_to );
      // Log.v( TAG, "move " + v + " " + ft.from + "-" + ft.to + ": fore score " + s );
      if ( s > score ) {
        score = s;
        ft0.from = ft.from;
        ft0.to   = ft.to;
        ret = moves.getValue(k);
      }
    }
    return ret;
  }

  private float movingStartScore( int move, FromTo ft, Weight wei_from, Weight wei_to ) 
  {
    float score = SCORE_MIN;
    ft.from = -1;
    ft.to   = -1;
    if ( move > 0 ) return score;
    for (int k=2; k<= Indices.POS_CORNER1; ++k ) {
      // if ( k == Indices.POS_SKIP ) continue;
      int b = mBoard.value(k) * player();
      if ( b > 0 ) {
        if ( k + move < 2 ) {
          float s = (7-k) * player() * WF_START * yut_random();
          if ( s > score ) {
            score = s;
            ft.from = k;
            ft.to = 1;
          }
        }
      }
    }
    return score;
  }

  private float movingForScore( int move, FromTo ft, Weight wei_from, Weight wei_to ) 
  {
    float score = SCORE_MIN;
    ft.from = -1;
    ft.to   = -1;
    if ( move > 0 && mBoard.start( Indices.yut_index( player() ) ) > 0 ) {
      int t = 1 + move;
      float s = wei_to.value(t) - wei_from.value(1);
      int b = mBoard.value(t) * player();
      if ( b > 0 ) {
        float w = 1.0f;
        if ( t < 11 && t != 6 ) w *= t/11.0f;
        s += b * WF_JOIN0 * w;
      }
      s -= positionDanger( t ) * WF_DANGER;
      s *= yut_random();
      if ( s > score ) {
        score = s;
        ft.from = 0;
        ft.to   = t;
      }
    }
    for (int k=2; k< Indices.POS_HOME; ++k ) {
      if ( k == Indices.POS_SKIP ) continue;
      if ( (k+move) > 1 ) {
        int b = mBoard.value(k) * player();
        if ( b > 0 ) {
          // double danger = b * positionDanger( k ) * WF_DANGER * Indices.distance(k)/10.0f;
          int[] pos = new int[2];
          mBoard.nextPositions( k, move, pos );
          for ( int j=0; j<2; ++j ) {
            int t = pos[j];
            if ( t > 0  && t < Indices.POS_HOME ) {
              float s = b * ( wei_to.value(t) - wei_from.value(k) );
              float danger = positionDanger( t ) * WF_DANGER;
              // printf("danger[%d] = %.2f ", t, danger );
              if (  mBoard.value(t) * player() > 0 ) {
                float w = 1.0f;
                if ( t < 11 ) w *= t/11.0f;
                s += mBoard.value(t) * player() * WF_JOIN * w;
              }
              s -= danger;
              // printf("movingForScore from %d %.2f to %d %.2f score %.2f\n", 
              //   k, wei_from.value(k), t, wei_to.value(t), s );
              s *= yut_random();
              if ( s > score ) {
                score = s;
                ft.from  = k;
                ft.to    = t;
              }
            } 
          }
        }
      }
    }
    return score;
  }

  private float movingHomeScore( int move, FromTo ft, Weight wei_from ) // moving HOME
  {
    float score = SCORE_MIN;
    if ( move < 0 ) return score;
    ft.from = -1;
    for (int k=2; k< Indices.POS_HOME; ++k ) {
      if ( k ==  Indices.POS_SKIP ) continue;
      if ( mBoard.value(k) * player() > 0 ) {
        int[] pos = new int[2];
        mBoard.nextPositions( k, move, pos );
        if ( pos[0] == Indices.POS_HOME || pos[1] == Indices.POS_HOME ) {
          float s = Math.abs(mBoard.value(k)) * (wei_from.value(32) - wei_from.value(k)) * WF_HOME - move * WF_MOVE;
          s *= yut_random();
          if ( s > score ) {
            score = s;
            ft.from = k;
          }
        }
      }
    }
    return score;
  } 
       
  // moving so that the opponent goes back to START
  private float movingBackScore( int move, FromTo ft, Weight wei_from, Weight wei_to ) 
  {
    float score = SCORE_MIN;
    ft.from = -1;
    ft.to   = -1;
    if ( mBoard.start( Indices.yut_index(player()) ) > 0 && move > 0 ) { // moving from START
      int t = 1 + move;
      if ( mBoard.value(t) * player() < 0 ) {
        float s = wei_to.value(t) - wei_from.value(1) - mBoard.value(t) * player() * ( wei_to.value(t) - wei_from.value(1) );
        s *= yut_random();
        if ( s > score ) {
          score = s;
          ft.from = 1;
          ft.to   = t;
        }
      }
    }
    for (int k=2; k< Indices.POS_HOME; ++k ) {
      if ( k == Indices.POS_SKIP ) continue;
      if ( (k+move) > 1 ) { // only positions that can be reached
        int w = mBoard.value(k) * player();
        if ( w > 0 ) {
          int[] pos = new int[2];
          mBoard.nextPositions( k, move, pos );
          for ( int j=0; j<2; ++j ) {
            int t = pos[j];
            if ( t > 0  && t < Indices.POS_HOME && mBoard.value(t) * player() < 0 ) {
              float s = w * ( wei_to.value(t) - wei_from.value(k) ) - (mBoard.value(t) * player()) * (wei_to.value(t) - wei_from.value(1)); 
              s *= yut_random();
              if ( s > score ) {
                score = s;
                ft.from  = k;
                ft.to    = t;
              }
            }
          }
        }
      }
    }
    return score;
  }

}

