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
  private boolean mRevertDo = false;

  Moves()
  {
    move = new ArrayList<Integer>();
    mRevertDo = false;
  }

  static boolean rethrow( int m ) { return ( ( m % 10 ) >= 4 ); }

  void setRevertDo() { mRevertDo = true; }

  // get the index of the first back-do
  int getSkip()
  {
    for ( int k = 0; k < move.size(); ++ k ) {
      if ( getValue( k ) < 0 ) return k;
    }
    return -1;
  }
  
  // remove a skip move
  // @return true if found and removed a skip move, false otherwise
  boolean removeSkip()
  {
    for ( int k = 0; k < move.size(); ++ k ) {
      if ( getValue( k ) < 0 ) {
        shift( k );
        return true;
      }
    }
    return false;
  }

  // @return true if moves are all skips 
  //              or there are no moves
  boolean hasAllSkips()
  {
    if ( size() == 0 ) return true;
    for ( int k=0; k < move.size(); ++k ) {
      if ( getValue( k ) > 0 ) return false;
    }
    return true;
  }

  // #return the number of skips moves
  // int getSkipCount()
  // {
  //   int ret = 0;
  //   for ( int k=0; k<move.size(); ++k ) {
  //     if ( getValue(k) < 0 ) ++ ret;
  //   }
  //   return ret;
  // }

  boolean hasSkip()
  {
    for ( int k=0; k<move.size(); ++k ) {
      if ( getValue(k) < 0 ) return true;
    }
    return false;
  }

  // shift out a move
  // @param k   index of the move to shift out
  synchronized void shift( int k ) 
  {
    assert( k >= 0 && k < move.size() );
    mRevertDo = false;
    for ( ++k; k < move.size(); ++ k ) {
      move.set( k-1, move.get( k ) );
    }
    move.remove( k-1 );
  }

  synchronized int size() { return move.size(); }

  // --------------------------------------------------------
  synchronized int getValue( int k ) 
  { 
    assert( k >= 0 && k < move.size() );
    return getMoveValue( move.get(k).intValue(), mRevertDo );
  }

  static int getMoveValue( int val, boolean revert_do )
  {
    int ret = val % 10;
    if ( ! YutnoriPrefs.isSpecial() || revert_do ) return ret;
    return ( ret == 1 && val >= 10 && ! revert_do )? -ret : ret;
  }


  // used only by DrawingSurface
  synchronized int getRawValue( int k ) 
  { 
    assert( k >= 0 && k < move.size() );
    return move.get(k).intValue();
  }

  // not used
  // synchronized int getAbsValue( int k ) 
  // { 
  //   assert( k >= 0 && k < move.size() );
  //   return move.get(k).intValue() % 10;
  // }

  // -------------------------------------------------------

  synchronized void add( int v ) { move.add( new Integer(v) ); mRevertDo = false; }
  synchronized void addAndRevert( int v, boolean revert_do ) { move.add( new Integer(v) ); mRevertDo = revert_do; }

  synchronized void clear() { move.clear(); mRevertDo = false; }

  // get the move to achieve a displacement
  // @param m     displacement 
  synchronized int hasMove( int m )
  {
    for ( int k = 0; k < move.size(); ++ k ) {
      if ( getValue( k ) == m ) return k;
    }
    return -1;
  }

  // get the minimal (abs value) move to achieve a displacement
  // @param m     displacement 
  // @return  index of min move >= m (if m > 0)
  //          index of most negative move (if m < 0)
  synchronized int minMove( int m )
  {
    int k0 = -1;
    if ( m > 0 ) {
      int m0 = 100;
      for ( int k = 0; k < move.size(); ++k ) {
        int mm = getValue( k );
        if ( mm >= m && mm < m0 ) { m0 = mm; k0 = k; }
      }
    } else {
      int m0 = 0;
      for ( int k = 0; k < move.size(); ++k ) {
        int mm = getValue( k );
        if ( mm <= m && mm < m0 ) { m0 = mm; k0 = k; }
      }
    }  
    return k0;
  }

  synchronized void sortUnique()
  {
    if ( move.size() < 2 ) return;
    int k = 0;
    while ( k+1 < move.size() ) {
      int i0 = getValue( k );
      int i1 = getValue( k+1 );
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

  void print( String msg )
  {
    StringBuffer sb = new StringBuffer();
    sb.append( msg + " moves: (" + mRevertDo + ") " );
    for ( Integer ii : move ) sb.append( " " + ii.intValue() );
    Log.i("Yutnori-TITO", sb.toString() );
  }

}
