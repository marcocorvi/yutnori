/** @file Yutnori.h
 * 
 * @author marco corvi
 * @date dec 2010
 *
 * @brief Yut Nori game
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
#ifndef YUTNORI_YUTNORI_H
#define YUTNORI_YUTNORI_H

#include "Strategy1.h"
#include "Strategy2.h"
#include "Dice.h"
#include "Drawer.h"

class Yutnori
{
  private:
    Dice dice;
    Board & board;
    Strategy * strategy; //!< my strategy
    int player;          //!< my player
    int moves[20];
    int n_moves;
    Drawer * drawer;
 
  public:
    /**
     * @param me  my player [default +1 ]
     */
    Yutnori( Board & b, int me = +1 );

    Board & GetBoard() { return board; }
    int * GetMoves() { return moves; }
    int GetNrMoves() { return n_moves; }
    int GetWinner() { return board.Winner(); }

    void Reset() { board.Reset(); }
    void SetDrawer( Drawer * d ) 
    { 
      drawer = d;
      if ( strategy ) {
        strategy->SetDrawer( drawer );
      }
    }

    void SetStrategy ( Strategy * s ) 
    {
      strategy = s;
      if ( strategy ) {
        strategy->SetDrawer( drawer );
      }
    }

    // throw dice
    int Throw() { return dice.roll(); }

    // move of the other player
    // @return true if the other player sent me back to start
    bool Move( int from, int to ) 
    { 
      if ( to <= 1 ) to = 32;
      return board.move( from, to, player );
    }

    // check if the other player can move from to with move "m"
    bool CanMove( int from, int to, int m );



    // @return 0 normal, or winner
    int Play( );
    void PlayOnce( bool doze = false );

    static int main( bool do_sleep, bool do_getchar );

};

#endif
