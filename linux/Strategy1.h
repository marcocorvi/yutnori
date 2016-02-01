/** @file Strategy1.h
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
#ifndef YUTNORY_STRATEGY_1_H
#define YUTNORY_STRATEGY_1_H

#include <vector>

#include "Weight.h"
#include "Strategy.h"

class Strategy1 : public Strategy
{

   private:
     // int player;

   public:
     Strategy1( Board & b, int p = -1 ) 
       : Strategy( b, p )
     { }

     ~Strategy1() {}

     bool move( int * moves, int & n_moves );

  private:
     int bestMove( const std::vector<int> & moves,
                   int n_moves, int & from, int & to, bool do_print=false );

     // single move scores
     double movingBackScore( int move, int & from, int & to,
                            const Weight & wei_from, const Weight & wei_to );

     double movingHomeScore( int move, int & from,
                             const Weight & wei_from );

     double movingForScore( int move, int & from, int & to,
                            const Weight & wei_from, const Weight & wei_to );
    
     void updateWeight( Weight & w, const Board & b );

};

#endif

