/** @file GuiDrawer.h
 * 
 * @author marco corvi
 * @date dec 2010
 *
 * @brief the drawer interface
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
#ifndef YUTNORI_DRAWER_H
#define YUTNORI_DRAWER_H

class Board;

class Drawer
{
  public:
    virtual ~Drawer() {}

    virtual void drawMove( int move, int nr ) = 0;

    virtual void drawMoves( int * moves, int nr, int max ) = 0;

    virtual void drawYutnori( const Board & board ) = 0;
};

#endif
