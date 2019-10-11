/** @file Strategy1.cpp
 *
 * @author marco corvi
 * @date dec 2010
 *
 * @brief Yutnori strategy
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
#include <assert.h>
#include <unistd.h>

#include <vector>
#include <algorithm>

#include "Indices.h"
#include "Probability.h"
#include "Strategy1.h"
#include "Delay.h"

#define WF_GET    0.8 // weight increment to get you
#define WF_GET2   0.2 // weight increment to get you
#define WF_GOT    0.33 // weight increment to get caught by you
#define WF_ADD    0.9 // weight incrememt to join me
#define WF_JOIN   0.6 // score increment to join me forward
#define WF_JOIN0  0.3 // score increment to join me at the beginning
#define WF_HOME   1.5 // score increment to get home
#define WF_DANGER 0.50

#define SCORE_MIN -1000

/** This file implements the PC yutnori strategy
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


void 
Strategy1::updateWeight( Weight & w, const Board & board )
{
  for (int k=1; k<POS_HOME; ++k ) {
    if ( k == POS_SKIP ) continue;
    int b = board[k];
    if ( b * player() < 0 ) {
      if ( k <= POS_CORNER4 ) {
        w[k] += abs(b) * k * WF_GET;
        for (int k1=1; k1<=5 && k1+k<=POS_CORNER4; ++k1) {
          w[k1+k] -= (k1+k) * WF_GOT * Probability[k1];
        }
        for (int k1=1; k1<=5 && k-k1>1; ++k1 ) {
          int k2 = k - k1;
          w[k2] += abs(b) * (k2) * WF_GET2 * Probability[k1];
        }
        if ( k == POS_CORNER3 ) {
          for (int k1=1; k1<=5; ++k1 ) {
            int k2 = 32 - k1;
            if ( k2 == 29 ) k2 = 24;
            w[k2] += abs(b) * (32-k1-16) * WF_GET2 * Probability[k1];
          }
        } else if ( k == POS_CORNER4 ) {
          for (int k1=1; k1<=5; ++k1 ) {
            int k2 = 27 - k1;
            w[k2] += abs(b) * (k2-6) * WF_GET2 * Probability[k1];
          }
        }
      } else if ( k <= 26 ) {
        w[k] += abs(b) * (k-6) * WF_GET;
        for (int k1=1; k1<=5 && k1+k<=27; ++k1) {
          int k2 = k1+k;
          if ( k2 == 27 ) k2 = POS_CORNER4;
          w[k2] -= (k1+k-6) * WF_GOT * Probability[k1];
        }
        for (int k1=1; k1<=5 && k-k1>=21; ++k1 ) {
          int k2 = k - k1;
          if ( k2 == 21 ) k2 = 11;
          w[k2] += abs(b) * (k-k1-6) * WF_GET2 * Probability[k1];
        }
      } else {
        w[k] += abs(b) * (k-16) * WF_GET;
        for (int k1=1; k1<=5; ++k1 ) {
          int k2 = k1+k;
          if ( k2 == POS_SKIP ) k2 = POS_CENTER;
          if ( k2 >= POS_HOME ) k2 -= 16;
          w[k2] -= (k1+k-16) * WF_GOT * Probability[k1];
        }
        for (int k1=1; k1<=5 && k-k1>=26; ++k1) {
          int k2 = k - k1;
          if ( k2 == 26 ) { k2 = 6; }
          else if ( k2 == 29 ) { k2 = 24; }
          w[k2] += abs(b) * (k-k1-16) * WF_GET2 * Probability[k1];
        }
      }
    } else if ( b*player() > 0 ) {
      w[k] += abs(b) * distance(k) * WF_ADD;
    }
  }
}


bool
Strategy1::move( int * moves, int & n_moves ) 
{
  // printf("Strategy::move() ");
  // for (int k=0; k<n_moves; ++k ) printf("%2d", moves[k] );
  // printf("\n");

  bool throw_again  = false;
  while ( n_moves > 0 ) {
    int max = n_moves;
    // TODO find all the possible composite moves
    std::vector< int > composite_moves;
    for ( int k1=0; k1<n_moves; ++k1) {
      composite_moves.push_back( moves[k1] );
    }
    for ( int k1=0; k1<n_moves-1; ++k1 ) {
      for ( int k2=k1+1; k2<n_moves; ++k2 ) {
        composite_moves.push_back( moves[k1] + moves[k2] );
      }
    }
    for ( int k1=0; k1<n_moves-2; ++k1 ) {
      for ( int k2=k1+1; k2<n_moves-1; ++k2 ) {
        for ( int k3=k2+1; k3<n_moves; ++k3 ) {
          composite_moves.push_back( moves[k1] + moves[k2] + moves[k3] );
        }
      }
    }
    std::sort( composite_moves.begin(), composite_moves.end() );
    std::unique( composite_moves.begin(), composite_moves.end() );
    int size = composite_moves.size();
    // printf("composite: ");
    // for (int k=0; k<size; ++k ) printf("%3d", composite_moves[k] );
    // printf("\n");
    // printf("moving ");

    int from, to;
    int m = bestMove( composite_moves, size, from, to, true );

    int k1, k2, k3;
    int to3 = -1;
    int to2 = -1;
    for ( k1=0; k1<n_moves; ++k1) {
      if ( moves[k1] == m ) goto found1;
    }
    for ( k1=0; k1<n_moves; ++k1 ) {
      for ( k2=0; k2<n_moves; ++k2 ) {
        if ( m == moves[k1]+moves[k2] ) goto found2;
      }
    }
    for ( k1=0; k1<n_moves; ++k1 ) {
      for ( k2=0; k2<n_moves; ++k2 ) {
        for ( k3=0; k3<n_moves; ++k3 ) {
          if ( m == moves[k1]+moves[k2]+moves[k3] ) goto found3;
        }
      }
    }
    printf("Error: \n");
    printf("Strategy::move() ");
    for (int k=0; k<n_moves; ++k ) printf("%2d", moves[k] );
    printf("\n");
    printf("composite: ");
    for (int k=0; k<size; ++k ) printf("%3d", composite_moves[k] );
    printf("\n");
    printf("best move %d\n", m );
    m = bestMove( composite_moves, size, from, to, true );
    assert( 0 );
  found3:
    // printf("found 3\n");
    {
      int pos3[2];
      int pos2[2];
      int pos1[2];
      board.nextPositions( from, moves[k3], pos3 );
      if ( pos3[0] < 32 ) {
        board.nextPositions( pos3[0], moves[k2], pos2 );
        if ( pos2[0] < 32 ) {
          board.nextPositions( pos2[0], moves[k1], pos1 );
          if ( pos1[0] == to || pos1[1] == to ) {
            to3 = pos3[0];
            to2 = pos2[0];
          } else if ( pos2[1] > 1 ) {
            board.nextPositions( pos2[1], moves[k1], pos1 );
            if ( pos1[0] == to || pos1[1] == to ) {
              to3 = pos3[0];
              to2 = pos2[1];
            }
          } else {
            board.nextPositions( pos3[1], moves[k2], pos2 );
            board.nextPositions( pos2[0], moves[k1], pos1 );
            if ( pos1[0] == to || pos1[1] == to ) {
              to3 = pos3[1];
              to2 = pos2[0];
            } else if ( pos2[1] > 1 ) {
              board.nextPositions( pos2[1], moves[k1], pos1 );
              if ( pos1[0] == to || pos1[1] == to ) {
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
    }
    // printf("(3) %d ", moves[k3] );
    --n_moves;
    while ( k3 < n_moves ) {
      moves[k3] = moves[k3+1];
      ++k3;
    }
    throw_again = do_move( from, to3 );
    if ( drawer ) {
      drawer->drawMoves( moves, n_moves, max );
      drawer->drawYutnori( board );
    }
    from = to3;
    if ( throw_again ) return true;
    if ( board.Winner() != 0 ) return false;
    if ( n_moves == 0 ) return false;
    sleep( SLEEP_TIME );
  found2:
    // printf("found 2\n");
    if ( to2 == -1 ) {
      int pos2[2];
      board.nextPositions( from, moves[k2], pos2 );
      if ( pos2[0] < 32 ) {
        int pos1[2];
        board.nextPositions( pos2[0], moves[k1], pos1 );
        if ( pos1[0] == to || pos1[1] == to ) {
          to2 = pos2[0];
        } else {
          to2 = pos2[1];
        }
        assert( to2 > 1 );
      } else {
        to2 = pos2[0];
      }
    }
    // printf("(2) %d ", moves[k2] );
    --n_moves;
    while ( k2 < n_moves ) {
      moves[k2] = moves[k2+1];
      ++k2;
    }
    throw_again = do_move( from, to2 );
    if ( drawer ) {
      drawer->drawMoves( moves, n_moves, max );
      drawer->drawYutnori( board );
    }
    from = to2;
    if ( throw_again ) return true;
    if ( board.Winner() != 0 ) return false;
    if ( n_moves == 0 ) return false;
    sleep( SLEEP_TIME );
  found1:
    // printf("(1) %d ", moves[k1] );
    --n_moves;
    while ( k1 < n_moves ) {
      moves[k1] = moves[k1+1];
      ++k1;
    }
    // printf("\n");
    // printf("Player %d does the move (from %d --> to %d)\n", player(), from, to );
    throw_again = do_move( from, to );
    if ( drawer ) {
      drawer->drawMoves( moves, n_moves, max );
      drawer->drawYutnori( board );
    }
    if ( throw_again ) return true;
    if ( board.Winner() != 0 ) return false;
    if ( n_moves == 0 ) return false;
    sleep( SLEEP_TIME );
  }
  return false;
}

int
Strategy1::bestMove( const std::vector<int> & moves,
                     int n_moves, int & from, int & to,
                     bool do_print ) 
{
  // if ( do_print ) printf("BestMove() scores ");
  double score = SCORE_MIN;
  int f0 = -1, t0 = -1;
  int f, t;
  int ret = -1;
  Weight wei_from;
  Weight wei_to;
  updateWeight( wei_to, board );

  for ( int k=0; k<n_moves; ++k) {
    if ( do_print ) printf("move [%d] ", moves[k] );
    double s = movingHomeScore( moves[k], f, wei_from );
    if ( do_print ) printf("Home %6.2f from %d ", s, f );
    if ( s > score ) {
      score = s;
      f0 = f;
      t0 = POS_HOME;
      ret = moves[k];
    }
    s = movingBackScore( moves[k], f, t, wei_from, wei_to );
    if ( do_print ) printf("Back %6.2f from %d to %d ", s, f, t );
    if ( s > score ) {
      score = s;
      f0 = f;
      t0 = t;
      ret = moves[k];
    }
    s = movingForScore( moves[k], f, t, wei_from, wei_to );
    if ( do_print ) printf("For %6.2f from %d to %d ", s, f, t );
    if ( s > score ) {
      score = s;
      f0 = f;
      t0 = t;
      ret = moves[k];
    }
  }
  if ( do_print ) printf(" Best from %d to %d ret %d \n", f0, t0, ret);
  from = f0;
  to   = t0;
  return ret;
}

double
Strategy1::movingForScore( int move, int & from, int & to,
                           const Weight & wei_from, const Weight & wei_to ) 
{
  double score = SCORE_MIN;
  from = -1;
  to = -1;
  if ( board.Start( yut_index( player() ) ) > 0 ) {
    int t = 1 + move;
    double s = wei_to[t] - wei_from[1];
    int b = board[t] * player();
    if ( b > 0 ) {
      double w = 1.0;
      if ( t < 11 && t != 6 ) w *= t/11.0;
      s += b * WF_JOIN0 * w;
    }
    s -= positionDanger( t ) * WF_DANGER;
    s *= yut_random();
    if ( s > score ) {
      score = s;
      from = 0;
      to   = t;
    }
  }
  for (int k=2; k<POS_HOME; ++k ) {
    if ( k == POS_SKIP ) continue;
    int b = board[k] * player();
    if ( b > 0 ) {
      // double danger = b * positionDanger( k ) * WF_DANGER * distance(k)/10.0;
      int pos[2];
      board.nextPositions( k, move, pos );
      for ( int j=0; j<2; ++j ) {
        int t = pos[j];
        if ( t > 0  && t < POS_HOME ) {
          double s = b * ( wei_to[t] - wei_from[k] );
          double danger = positionDanger( t ) * WF_DANGER;
          // printf("danger[%d] = %.2f ", t, danger );
          if (  board[ t ] * player() > 0 ) {
            double w = 1.0;
            if ( t < 11 ) w *= t/11.0;
            s += board[t] * player() * WF_JOIN * w;
          }
          s -= danger;
          // printf("movingForScore from %d %.2f to %d %.2f score %.2f\n", 
          //   k, wei_from[k], t, wei_to[t], s );
          s *= yut_random();
          if ( s > score ) {
            score = s;
            from  = k;
            to    = t;
          }
        } 
      }
    }
  }
  return score;
}

double
Strategy1::movingHomeScore( int move, int & from,
                           const Weight & wei_from )
{
  double score = SCORE_MIN;
  from = -1;
  for (int k=2; k<POS_HOME; ++k ) {
    if ( k == POS_SKIP ) continue;
    if ( board[k] * player() > 0 ) {
      int pos[2];
      board.nextPositions( k, move, pos );
      if ( pos[0] == POS_HOME ) {
        double s = board[k] * (wei_from[32] - wei_from[k]) * WF_HOME;
        s *= yut_random();
        if ( s > score ) {
          score = s;
          from = k;
        }
      }
    }
  }
  return score;
} 
       

double
Strategy1::movingBackScore( int move, int & from, int & to,
                            const Weight & wei_from, const Weight & wei_to ) 
{
  double score = SCORE_MIN;
  from = -1;
  to = -1;
  if ( board.Start( yut_index(player()) ) > 0 ) {
    int t = 1 + move;
    if ( board[t] * player() < 0 ) {
      double s = wei_to[t] - wei_from[1] 
               - board[t] * player() * ( wei_to[t] - wei_from[1] );
      s *= yut_random();
      if ( s > score ) {
        score = s;
        from = 1;
        to   = t;
      }
    }
  }
  for (int k=2; k<POS_HOME; ++k ) {
    if ( k == POS_SKIP ) continue;
    int w = board[k] * player();
    if ( w > 0 ) {
      int pos[2];
      board.nextPositions( k, move, pos );
      for ( int j=0; j<2; ++j ) {
        int t = pos[j];
        if ( t > 0  && t < POS_HOME && board[ t ] * player() < 0 ) {
          double s = w * ( wei_to[t] - wei_from[k] )
                   - (board[t] * player()) * (wei_to[t] - wei_from[1]); 
          s *= yut_random();
          if ( s > score ) {
            score = s;
            from  = k;
            to    = t;
          }
        }
      }
    }
  }
  return score;
}


