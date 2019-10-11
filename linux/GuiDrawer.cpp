/** @file GuiDrawer.cpp
 *
 * @author marco corvi
 * @date dec 2010
 *
 * @brief Yutnori drawer
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
#include "Board.h"
#include "GuiDrawer.h"

// functions from gui.cpp
void drawYut( int c, int pos );

void drawBoard( const Board & board );

void 
GuiDrawer::drawMoves( int * moves, int nr, int max )
{
  int k;
  for (k=0; k<nr; ++k ) {
    drawYut( moves[k], k );
  }
  for ( ; k<max; ++k ) {
    drawYut( 0, k );
  }
}

void 
GuiDrawer::drawMove( int move, int nr )
{
  // printf("GuiDrawer::drawMove() %d %d \n", move, nr );
  drawYut( move, nr );
}

void 
GuiDrawer::drawYutnori( const Board & board )
{
  // printf("GuiDrawer::drawBoard() \n");
  drawBoard( board );
}
