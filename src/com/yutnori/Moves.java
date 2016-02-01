/* @file Moves.java
 *
 * @author marco corvi
 * @date nov 2015
 *
 * @brief Yutnori moves
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.yutnori;

import java.util.ArrayList;

import android.util.Log;

class Moves
{
  private ArrayList<Integer> move;

  Moves()
  {
    move = new ArrayList<Integer>();
  }

  synchronized void shift( int k ) 
  {
    assert( k < move.size() );
    for ( ++k; k < move.size(); ++ k ) {
      move.set( k-1, move.get( k ) );
    }
    move.remove( k-1 );
  }

  synchronized int size() { return move.size(); }

  synchronized int value( int k ) 
  { 
    assert( k < move.size() );
    return move.get(k).intValue();
  }

  synchronized void add( int v ) { move.add( new Integer(v) ); }

  synchronized void clear() { move.clear(); }

  synchronized int hasMove( int m )
  {
    // StringBuilder sb = new StringBuilder();
    // sb.append("moves:");
    // for ( int k = 0; k < move.size(); ++ k ) sb.append(" " + move.get(k).intValue() );
    // Log.v("yutnori", sb.toString() );

    for ( int k = 0; k < move.size(); ++ k ) {
      if ( move.get(k).intValue() == m ) {
        return k;
      }
    }
    return -1;
  }

  synchronized int minMove( int m )
  {
    int k0 = -1;
    int m0 = 100;
    for ( int k = 0; k <  move.size(); ++k ) {
      int mm = move.get(k).intValue();
      if ( mm >= m && mm < m0 ) { m0 = mm; k0 = k; }
    }
    return k0;
  }

  synchronized void sortUnique()
  {
    if ( move.size() < 2 ) return;
    int k = 0;
    while ( k+1 < move.size() ) {
      int i0 = value( k );
      int i1 = value( k+1 );
      if ( i0 < i1 ) {
        ++ k;
      } else if ( i0 == i1 ) {
        move.remove( k+1 );
      } else { // i0 > i1 
        move.set( k, new Integer( i1 ) );
        move.set( k+1, new Integer( i0 ) );
        if ( k > 0 ) --k;
      }  
    }
  }

}
