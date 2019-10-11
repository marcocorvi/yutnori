/** @file Strategy2.cpp
 *
 * @author marco corvi
 * @date dec 2010
 *
 * @brief Yutnori strategy nr. 2
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
#include <unistd.h>
#include <vector>

#include "Weight.h"
#include "Strategy2.h"

#include "Indices.h"
#include "Probability.h"
#include "Delay.h"

#define WF_GET    0.8 // weight increment to get you
#define WF_GET2   0.2 // weight increment to get you
#define WF_GOT    0.33 // weight increment to get caught by you
#define WF_ADD    0.9 // weight incrememt to join me
#define WF_JOIN   1.2 // score increment to join me forward
#define WF_JOIN0  0.8 // score increment to join me at the beginning
#define WF_HOME   1.5 // score increment to get home
#define WF_DANGER 0.50

#define SCORE_MIN -1000

/** This file implements the PC yutnori strategy
 *
 * Two different weight are considered: the weight at the "from" position and the weight at 
 * the "to" position. Each weight is computed from the base weight using the state of the
 * board:
 *   - the presence of the opponent inceases the weight of the position (WF_GET) and at
 *     the preceeding positions (WF_GET2)
 *     and decreases those of the following positions (WF_GOT)
 *   - increase the weight wher ethere are already this player pawns
*/

double GetFactors[] = { 1.0, 1.6, 1.3, 1.1, 1.0 };


void 
Strategy2::updateWeight( Weight & w, const Board & board )
{
  double get = GetFactors[ board.Start( yut_index( opponent() ) ) ];
  for (int k=1; k<POS_HOME; ++k ) {
    if ( k == POS_SKIP ) continue;
    int b = board[k];
    if ( b * player() < 0 ) {
      if ( k <= POS_CORNER4 ) {
        w[k] += abs(b) * k * get * WF_GET ;
        for (int k1=1; k1<=5 && k1+k<=POS_CORNER4; ++k1) {
          w[k1+k] -= (k1+k) * WF_GOT * Probability[k1];
        }
        for (int k1=1; k1<=5 && k-k1>1; ++k1 ) {
          int k2 = k - k1;
          w[k2] += abs(b) * (k2) * WF_GET2 * Probability[k1] * get;
        }
        if ( k == POS_CORNER3 ) {
          for (int k1=1; k1<=5; ++k1 ) {
            int k2 = 32 - k1;
            if ( k2 == 29 ) k2 = 24;
            w[k2] += abs(b) * (32-k1-16) * WF_GET2 * Probability[k1] * get;
          }
        } else if ( k == POS_CORNER4 ) {
          for (int k1=1; k1<=5; ++k1 ) {
            int k2 = 27 - k1;
            w[k2] += abs(b) * (k2-6) * WF_GET2 * Probability[k1] * get;
          }
        }
      } else if ( k <= 26 ) {
        w[k] += abs(b) * (k-6) * WF_GET * get;
        for (int k1=1; k1<=5 && k1+k<=27; ++k1) {
          int k2 = k1+k;
          if ( k2 == 27 ) k2 = POS_CORNER4;
          w[k2] -= (k1+k-6) * WF_GOT * Probability[k1];
        }
        for (int k1=1; k1<=5 && k-k1>=21; ++k1 ) {
          int k2 = k - k1;
          if ( k2 == 21 ) k2 = 11;
          w[k2] += abs(b) * (k-k1-6) * WF_GET2 * Probability[k1] * get;
        }
      } else {
        w[k] += abs(b) * (k-16) * WF_GET * get;
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
          w[k2] += abs(b) * (k-k1-16) * WF_GET2 * Probability[k1] * get;
        }
      }
    } else if ( b*player() > 0 ) {
      w[k] += abs(b) * distance(k) * WF_ADD;
    }
  }
}

bool 
Strategy2::move( int * moves, int & n_moves )
{
  struct Trial
  {
    double s; // score
    int k;    // move index
    int f;    // from position

    void update( double s0, int f0, int k0 ) {
      if ( s0 > s ) {
        s = s0;
        f = f0;
        k = k0;
      }
    }
  };

  Trial tried[33];
  Weight wei_from;
  Weight wei_to;
  updateWeight( wei_to, board );
  
  for (int k=0; k<33; ++k ) {
    tried[k].s = - 1000.0;
    tried[k].k = - 1;
    tried[k].f = - 1;
  }

  if ( board.Start( yut_index(player()) ) > 0 ) {
    for ( int k=0; k<n_moves; ++k) {
      int t = 1 + moves[k];
      // TODO compute the score to get from 0 to "t"
      double s = ComputeScore( 0, t, moves[k], wei_from, wei_to );
      tried[t].update( s, 0, k );
    }
  }
  for (int n=2; n<32; ++n ) {
    if ( n == POS_SKIP ) continue;
    if ( board[n] * player() > 0 ) {
      for ( int k=0; k<n_moves; ++k) {
        int pos[2];
        board.nextPositions( n, moves[k], pos );
        int t = pos[0];
        double s = ComputeScore( 0, t, moves[k], wei_from, wei_to );
        tried[t].update( s, n, k );
        if ( ( t = pos[1] ) > 0 ) {
          s = ComputeScore( 0, t, moves[k], wei_from, wei_to );
          tried[t].update( s, n, k );
        }
      }
    }
  }
      
  int to = 0;
  double score = - 1000.0;
  for (int k = 2; k<33; ++k ) {
    if ( tried[k].s > score ) {
      to = k;
      score = tried[k].s;
    }
  }

  if ( to > 0 ) {
    // printf("Strategy 2::move() from %d to %d move %d moves %d \n",
    //   tried[to].f, to, tried[to].k, n_moves );
    --n_moves;
    int k1 = tried[to].k;
    while ( k1 < n_moves ) {
      moves[k1] = moves[k1+1];
      ++k1;
    }
    bool throw_again = do_move( tried[to].f, to );
    if ( drawer ) {
      drawer->drawMoves( moves, n_moves, n_moves+1 );
      drawer->drawYutnori( board );
    }
    if ( throw_again ) return true;
    if ( board.Winner() != 0 ) return false;
    if ( n_moves == 0 ) return false;
    sleep( SLEEP_TIME );
  }

  return false;

}

double
Strategy2::ComputeScore( int f, int t, int m, Weight & wei_from, Weight & wei_to )
{
  double score = wei_to[ t ] - wei_from[ f ];
  if ( board[t] * player() < 0 ) {
    score += abs( board[t] ) * wei_from[t];
  }

  // merge with my mals
  if ( board[t] * player() > 0 ) {
    if ( t < 6 ) {
      if ( board.Start( yut_index( opponent() ) ) > 0 ) {
        score += (2 - abs( board[t] ) ) * WF_JOIN0;
      } else {
        score -= Probability[t-1] * 0.33;
      }
    } else {
      if ( board.Start( yut_index( opponent() ) ) > 0 ) {
        score += abs( board[t] ) * WF_JOIN; 
      } else {
        score -= 2.0;
      }
    }
  }

  // getting the opponents
  if ( board[t] * opponent() > 0 ) {
    score += abs(board[t]) * wei_to[t] * 2.0;
  }

  // joining me


  // avoid the opponent mals and try to stay behind them
  if ( t <= 21 ) {
    for (int k=1; k<=5; ++k ) {
      int k1 = t-k;
      if ( k1 >= 2 && board[k1] * player() < 0 ) {
        score -= Probability[k] * 0.33;
      }
    }
    if ( t == 21 ) {
      for (int k=1; k<=5; ++k ) {
        int k1 = 27 - k;
        if ( board[k1] * player() < 0 ) {
          score -= Probability[k] * 0.33;
        }
      }
    } else if ( t == 16 ) {
      for (int k=1; k<=5; ++k ) {
        int k1 = 32 - k;
        if ( k1 == 29 ) k1 = 24;
        if ( board[k1] * player() < 0 ) {
          score -= Probability[k] * 0.33;
        }
      }
    }
  } else if ( t < 27 ) {
    for ( int k=1; k<=5; ++k ) {
      int k1 = t-k;
      if ( k1 < 21 ) break;
      if ( k1 == 21 ) k1 = 11;
      if ( board[k1] * player() < 0 ) {
        score -= Probability[k] * 0.33;
      }
    }
  } else if ( t < 32 ) {
    for ( int k=1; k<=5; ++k ) {
      int k1 = t-k;
      if ( k1 < 26 ) break;
      if ( k1 == 26 ) k1 = 6;
      if ( k1 == 29 ) k1 = 24;
      if ( board[k1] * player() < 0 ) {
        score -= Probability[k] * 0.33;
      }
    }
  } else {
    // score to go home
    int waste = ( f < 22 ) ? 22 - f : 28 - f;
    waste = m - waste;
    score += abs( board[f] ) * ( 5 - waste);
  }
  return score;
}
