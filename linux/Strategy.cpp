/** @file Strategy.cpp
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

#include "Indices.h"
#include "Probability.h"
#include "Strategy.h"


double
Strategy::positionDanger( int at )
{
  double danger = 0.0;
  if ( board.Start( yut_index( opponent() ) ) > 0 ) {
    danger += board.distance( 0, at );
  }
  for (int k=2; k<POS_HOME; ++k ) {
    if ( k == POS_SKIP ) continue;
    if ( board[k] * player() < 0 ) {
      danger += board.distance( k, at );
    }
  }
  return danger;
}

