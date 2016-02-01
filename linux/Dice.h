/** @file Dice.h
 *
 * @author marco corvi
 * @date dec 2010
 *
 * @brief Throw the yutnori sticks
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
#ifndef YUTNORI_DICE_H
#define YUTNORI_DICE_H

#include <stdlib.h>
#include <time.h>

class Dice
{
  
  public:
    Dice()
    {
      srand( time( NULL ) );
    }

    int roll()
    {
      int ret = 0;
      for (int k = 0; k<4; ++k) {
        ret += rand() % 2;
      }
      return ( ret == 0 ) ? 5 : ret;
    }
};

#endif
