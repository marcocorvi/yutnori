package com.yutnori;

import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Timer;

import android.content.Context;

class Positions
{
  Context mContext;
  Timer mTimer;
  ArrayList< Integer > mPos;

  Positions( Context context )
  {
    mContext = context;
    mTimer = new Timer();
    mPos   = new ArrayList< Integer >();
  }

  synchronized boolean contains( int k )
  {
    for ( Integer pos : mPos ) if ( k == pos.intValue() ) return true;
    return false;
  }

  synchronized void remove( Integer ii ) { mPos.remove( ii ); }

  synchronized private void doAdd( int k )
  { 
    final Integer ii = new Integer( k );
    mPos.add( ii );
    mTimer.schedule( new TimerTask() {
        public void run() {
          remove( ii );
        }
      }, 1000 );
  }

  void add( int pos )
  {
    if ( pos <= 1 || pos >= 32 ) return;
    // if ( mBoard.winner() != 0 ) return;
    if ( YutnoriPrefs.getPos() == YutnoriPrefs.POS_PARTIAL ) {
      doAdd( pos );
    }
  }

}

