/** @file Strategy.h
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
#ifndef YUTNORY_STRATEGY_H
#define YUTNORY_STRATEGY_H

#include <stdlib.h>

#include "Weight.h"
#include "Player.h"

class Strategy : public Player
{

   private:
     // int player;

   public:
     Strategy( Board & b, int p = -1 ) 
       : Player( b, p )
     { }

     virtual ~Strategy() {}

     double positionDanger( int at );
 
     virtual bool move( int * moves, int & n_moves ) = 0;

   protected:
     /** randomize scores
      * @return a rnadom number between 0.95 and 1.05
      */
     double yut_random()
     {
       return 0.95 + 0.1 * ( (double)rand() ) / RAND_MAX;
     }

};

#endif

