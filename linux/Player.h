/** @file Player.h
 *
 * @author marco corvi
 * @date dec 2010
 *
 * @brief Yutnori player
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
#ifndef YUTNORY_PLAYER_H
#define YUTNORY_PLAYER_H

#include <vector>

#include "Board.h"
#include "Weight.h"
#include "Drawer.h"

class Player
{

   protected:
     Board & board;
     Drawer * drawer;

   private:
     int me;
     int other;

   public:
     /**
      * @param p   my palyer number (+1 or -1)
      */
     Player( Board & b, int p = -1 ) 
       : board( b )
       , drawer( NULL )
       , me( p )
       , other( -p )
     { }

     void SetDrawer( Drawer * d ) { drawer = d; } 

     // assume moves sorted
     // @return true if send opponent back to start
     bool do_move( int from, int to ) 
     {
       return board.move( from, to, me );
     }

     int player() const { return me; }

     int opponent() const { return other; }

};

#endif

