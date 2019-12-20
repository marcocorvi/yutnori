/** @file Yutnori.java
 * 
 * @author marco corvi
 * @date dec 2015
 *
 * @brief Yut Nori game
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.yutnori;

import android.util.Log;

import android.content.Context;

class Yutnori 
{
  static final String TAG = "Yutnori-TITO";

  private Board mBoard;
  private Moves mMoves;
  Strategy mStrategy; //!< my strategy
  Strategy mStrategy0 = null;
  Strategy mStrategy1 = null;
  Strategy mStrategy2 = null;
  private int mEngine;
  private int mCurrentEngine;
  private int player; //!< my player
 
  /**
   * @param other index of the other player
   */
  // Yutnori( Board b, Moves m, int other, DrawingSurface surface )
  // {
  //   mBoard    = b;
  //   mMoves    = m;
  //   player    = -other;
  //   mStrategy = null;
  //   mStrategy0 = new Strategy ( mBoard, mMoves, surface, Player.ANDROID ); // strategy plays for Android
  //   mStrategy1 = new Strategy1( mBoard, mMoves, surface, Player.ANDROID ); // strategy plays for Android
  //   mStrategy2 = new Strategy2( mBoard, mMoves, surface, Player.ANDROID );
  //   mEngine = YutnoriPrefs.ENGINE_RANDOM;
  // }

  Yutnori( Board b, Moves m, DrawingSurface surface )
  {
    mBoard    = b;
    mMoves    = m;
    player   = -1;
    mStrategy = null;
    mStrategy0 = new Strategy ( mBoard, mMoves, surface, Player.ANDROID ); // strategy plays for Android
    mStrategy1 = new Strategy1( mBoard, mMoves, surface, Player.ANDROID ); // strategy plays for Android
    mStrategy2 = new Strategy2( mBoard, mMoves, surface, Player.ANDROID );
    mEngine = YutnoriPrefs.ENGINE_RANDOM;
  }

  void setBoard( Board board ) 
  { 
    mBoard = board;
    mStrategy0.setBoard( mBoard );
    mStrategy1.setBoard( mBoard );
    mStrategy2.setBoard( mBoard );
  } 

  int getEngine() { return mCurrentEngine; }

  void setEngine( int engine )
  {
    mEngine = engine;
    setStrategy();
  }

  private void setStrategy()
  {
    mCurrentEngine = mEngine;
    if ( mCurrentEngine == YutnoriPrefs.ENGINE_RANDOM ) {
      double ran = Math.random();
      mCurrentEngine = ( ran < 0.333 )? 0 : ( ran < 0.667 )? 1 : 2;
    } 
    switch ( mCurrentEngine ) {
      case YutnoriPrefs.ENGINE_0: 
        mStrategy = mStrategy0;
        break;
      case YutnoriPrefs.ENGINE_1:
        mStrategy = mStrategy1;
        break;
      case YutnoriPrefs.ENGINE_2:
        mStrategy = mStrategy2;
        break;
    }
  }

  // Board getBoard() { return mBoard; }
  // int getWinner()  { return mBoard.winner(); }
  // void reset() { mBoard.reset(); }

  // throw dice
  static int throwYut( )
  { 
    return Dice.roll(); 
  }

  // move of the other player
  // @return true if the other player sent me back to start
  // boolean moveOther( int from, int to ) 
  // { 
  //   if ( to <= 1 ) to = 32;
  //   return mBoard.move( from, to, player, 0 );
  // }

  // check if the other player can move from to with move "m"
  boolean canMove( int from, int to, int m ) 
  {
    return mBoard.canMovePlayer( player, from, to, m );
  }

  // @return 0 normal, or winner
  // int play( )
  // {
  //   while ( mBoard.winner() == 0 ) {
  //     playOnce( 1 );
  //   }
  //   return mBoard.winner();
  // }

  // this is Android strategy playing
  int playOnce( int doze )
  {
    // Log.v( TAG, "ANDROID turn");

    // this is for an extra skip on BackDo with empty board
    // if ( mStrategy.mustSkip() ) {
    //   // Log.v( TAG, "ANDROID must skip ");
    //   mMoves.clear();
    //   return State.NONE;
    // }

    // setStrategy();
    mMoves.clear();

    boolean again = true;
    int state = State.NONE;
    do {
      if ( again ) {
        again = false;
        do {
          int m = throwYut( );
          mMoves.add( m );
          Delay.sleep( 2 * doze ); // was 2
        } while ( Math.abs( mMoves.getValue( mMoves.size()-1 ) ) > 3 );
      }
      if ( mStrategy != null ) {
        // mMoves.print( "Android " );
        // int on_board = mBoard.count( player );
        state = mStrategy.movePlayer( mMoves, doze );
        // Log.v( TAG, "ANDROID moved player state " + state );
        again = ( state == State.THROW );
        if ( state == State.NONE ) return State.NONE;
        // Delay.sleep( 1 * doze ); // was missing
      }
      Delay.sleep( 1 * doze ); // was 1
      // Log.v( TAG, "PlayOnce winner " + mBoard.winner() + " " + mBoard.home(0) + "/" + mBoard.home(1)
      //             + " moves " + mMoves.size() + " again " + again );
    } while ( mBoard.winner() == 0 && ( again || mMoves.size() > 0 ) );
    // Log.v(TAG, "ANDROID return state " + State.toString( state ) );
    return state; 
  }

}
