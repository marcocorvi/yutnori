/** @file Weight.h
 * 
 * @author marco corvi
 * @date dec 2010
 * 
 * @brief board strategy weights
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
#ifndef YUTNORI_WEIGHT_H
#define YUTNORI_WEIGHT_H

#define DIMW 33

/** weights of the board positions.
 * The higher the weight the better the position
 */
class Weight
{
  private:
    double weight[DIMW];

  public:
    Weight()
    {
      initWeight();
    }

    double operator[]( int k ) const { return weight[k]; }

    double & operator[]( int k ) { return weight[k]; }

  private:
    void initWeight();
};

#endif
