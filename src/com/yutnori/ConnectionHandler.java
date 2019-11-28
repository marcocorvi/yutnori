/* @file ConnectionHandler.java
 *
 * @author marco corvi
 * @date dec 2014
 *
 * @brief Yutnori connection handler
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.yutnori;

import android.os.Handler;
import android.os.Message;
import android.os.Bundle;

import android.content.Context;
import android.content.Intent;

import android.util.Log;

import java.util.ArrayList;
// import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Locale;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

class ConnectionHandler extends Handler
                        // implements DataListener
{
  final static String TAG = "Yutnori";
  SyncService mSyncService;
  
  private byte mSendCounter;  // send counter
  private byte mRecvCounter;  // recv counter must be equal to the peer send counter
                              // it is increased after the ack
  void resetCounters()
  {
    // Log.v( TAG, "reset counters");
    mSendCounter = (byte)0;
    mRecvCounter = (byte)0;
  }

  byte mAck[];     // cnt ACL   EOL
                   // cnt SYNC  EOL
  ConnectionQueue mBufferQueue;
  Main mApp;
  BluetoothDevice mDevice;
  boolean mClient;   // whether this Yutnori initiated the connection
  boolean mRun;
  boolean mAccepted = false;

  SendThread mSendThread;

  ConnectionHandler( Main app )
  {
    mApp = app;
    mAck   = new byte[3];
    // mThrow = new byte[4];
    // mMove  = new byte[5];
    // mBufferQueue = new ConcurrentLinkedQueue< byte[] >();
    mBufferQueue = new ConnectionQueue();

    mSyncService = new SyncService( mApp, mApp, this );
    mDevice = null;
    mClient = false;
    mRun    = false;
    mSendThread = null;
    resetCounters();
  }

  int getType() { return mSyncService.getType(); }

  int getAcceptState() { return ( mSyncService == null )? SyncService.STATE_NONE : mSyncService.getAcceptState(); }
  int getConnectState() { return ( mSyncService == null )? SyncService.STATE_NONE : mSyncService.getConnectState(); }
  String getConnectStateStr() { return ( mSyncService == null )? "UNKNOWN" : mSyncService.getConnectStateStr(); }
  String getConnectedDeviceName() { return ( mSyncService == null )? null : mSyncService.getConnectedDeviceName(); }

  String getConnectionStateTitleStr()
  {
    if ( mSyncService == null ) return "";

    String s1 = "";
    if ( mSyncService.getConnectState() == SyncService.STATE_CONNECTING ) {
      s1 = "<.>";
    } else if ( mSyncService.getConnectState() == SyncService.STATE_CONNECTED ) {
      s1 = "<->";
    }
    String s2 = ( mSyncService.getAcceptState() != SyncService.STATE_LISTEN )? "" : "(*)";
    return s2 + s1;
  }

  void startSyncService( Context context )
  {  
    // Log.v( TAG, "ConnectionHandler start()");
    mClient = false;
    mDevice = null;
    mSyncService.start( context );
  }

  void stop() 
  { 
    // Log.v( TAG, "ConnectionHandler stop()");
    stopSendThread( true );
    mSyncService.stop();
  }

  void connect( BluetoothDevice device )
  {
    // Log.v( TAG, "ConnectionHandler connect to " + device.getName() );
    if ( mDevice != device ) {
      mRun = false;
      if ( mSendThread != null ) {
        // Log.v( TAG, "ConnectionHandler join send-thread");
        stopSendThread( true );
      }
    }
    // Log.v( TAG, "ConnectionHandler now connect");
    resetCounters();
    mAccepted = false;
    mDevice = device;
    if ( mDevice != null ) {
      mClient = true;
      mSyncService.connect( mDevice );
    }
    // Log.v( TAG, "ConnectionHandler connect done");
  }

  // called when the connection has been lost
  private void reconnect()
  {
    if ( mDevice == null ) return;
    if ( ! mClient ) return;
    // Log.v( TAG, "ConnectionHandler reconnect() ");
    if ( mSendThread != null ) stopSendThread( false );
    while ( mSyncService.getConnectState() == SyncService.STATE_NONE ) {
      try {
        Thread.sleep( 200 );
      } catch ( InterruptedException e ) { }
      mSyncService.connect( mDevice );
    }
    if ( mSyncService.getConnectState() == SyncService.STATE_CONNECTED ) {
      doSyncCounter(); // FIXME what if sync-counter fail
    }
  }
  
  void connectionFailed()
  {
    // Log.v( TAG, "ConnectionHandler connectionFailed() ");
    if ( mClient ) {
      mClient = false;
      mDevice = null;
      mApp.syncConnectionFailed(); // this makes AlertDialog
    } else {
      // mSyncService.start( context );
    }
  }

  // device will be used when n-n (instead of 1-1)
  void disconnect( /* BluetoothDevice device */ )
  {
    // Log.v( TAG, "ConnectionHandler disconnect() ");
    // if ( device.getName() != null && device.getName().equals( mDevice.getName() ) {
      stopSendThread( true );
      mSyncService.disconnect();
      mDevice = null;
    // }
  }

  void syncDevice( BluetoothDevice device )
  {
    // Log.v( TAG, "ConnectionHandler syncDevice() ");
    // if ( device.getName() != null && device.getName().equals( mDevice.getName() ) {
      doSyncCounter();
    // }
  }

  private boolean writeBytes( byte[] buffer ) 
  {
    // Log.v( TAG, "ConnectionHandler write CNT " + buffer[0] + " key " + buffer[1] );
    return mSyncService.writeBuffer( buffer );
  }

  private void startSendThread()
  {
    // Log.v( TAG, "ConnectionHandler start send-thread()");
    mRun = true;
    // mSendThread = new SendThread( mBufferQueue );
    mSendThread = new SendThread( );
    mSendThread.start();
  }

  private void stopSendThread( boolean empty_queue )
  {
    // Log.v( TAG, "ConnectionHandler stop send-thread");
    if ( empty_queue ) {
      mBufferQueue.clear(); // flush the queue
    }
    mRun = false;
    if ( mSendThread != null ) {
      if ( mSendThread.isRunning() ) {
        try {
          mSendThread.join();
        } catch ( InterruptedException e ) { }
      }
      mSendThread = null;
    }
    // Log.v( TAG, "ConnectionHandler stop send-thread done");
  }


  // -----------------------------------------

  static final byte SHUTDOWN = (byte)0;
  static final byte SYNC     = (byte)0xfd;
  static final byte ACK = (byte)0xfe;
  static final byte EOL = (byte)0xff;
  static final byte THROW   = (byte)1;
  static final byte MOVED   = (byte)2;
  static final byte HIGH    = (byte)3;
  static final byte MOVE    = (byte)4;
  static final byte HIGHOFF = (byte)5;
  static final byte START   = (byte)6;
  static final byte DONE    = (byte)7;
  static final byte RESET   = (byte)8;
  static final byte ACCEPT  = (byte)9;
  static final byte OFFSET  = (byte)10;
  static final byte NEWGAME = (byte)11;
  static final byte OKGAME  = (byte)12;
  static final byte PAWNS   = (byte)13;
  static final byte BACKDO  = (byte)14;

  byte increaseCounter( byte cnt ) { return ( cnt == (byte)0xfe )? (byte)0 : (byte)(cnt+1); }

  boolean doAcknowledge( int cnt )
  {
    // Log.v( TAG, "ACK write <" + cnt + ">" );
    mAck[0] = (byte)cnt;
    mAck[1] = ACK;
    mAck[2] = EOL;
    return writeBytes( mAck );
  }

   // tell the peer my send counter
   boolean doSyncCounter( )
   {
     // Log.v( TAG, "SYNC write <" + mSendCounter + ">" );
     mAck[0] = (byte)mSendCounter;
     mAck[1] = SYNC;
     mAck[2] = EOL;
     return writeBytes( mAck );
   }

   // received command
   // the received command is terminated by 0xff
   void onRecv( int bytes, byte[] buffer ) 
   {
     // Log.v( TAG, "recv " + bytes + " length " + + buffer.length + " cnt " + buffer[0] + " key " + buffer[1] 
     //   + " " + ( (bytes>=3)? buffer[2] : "") + " " + ( (bytes>=4)? buffer[3] : "") 
     //   + " " + ( (bytes>=5)? buffer[4] : "") );

     if ( buffer.length < 2 ) {
       return;
     }
     byte cnt = buffer[0];
     byte key = buffer[1];

     if ( key == SYNC ) { // sync request
       mRecvCounter = cnt;
       return;
     } else if ( key == ACK ) {
       synchronized( mBufferQueue ) {
         ConnectionQueueItem item = mBufferQueue.find( cnt );
         if ( item != null ) {
           mBufferQueue.remove( item );
           // Log.v( TAG, "recv ACK <" + cnt + "> removed. queue size " + mBufferQueue.size() );
         } else {
           Log.e( TAG, "recv ACK <" + cnt + "> not found" );
         }
       }
       return;
     }

     if ( mRecvCounter != cnt ) {
       Log.e( TAG, "recv ERROR <" + cnt + "|" + key + "> expected " + mRecvCounter );
       // should ack again ?
       // doAcknowledge( cnt );
       doSyncCounter(); 
       return;
     }

     doAcknowledge( cnt ); // FIXME if fails ?
     mRecvCounter = increaseCounter( mRecvCounter );

     int value, from, to;
     switch ( key ) {
       case THROW:
         mApp.execThrow( (int)buffer[2] );
         break; 
       case MOVED:
         mApp.execMoved( (int)buffer[2] );
         break;
       case HIGH:
         mApp.execHighlight( (int)buffer[2] );
         break;
       case MOVE:
         mApp.execMove( (int)buffer[2], (int)buffer[3] );
         break;
       case HIGHOFF:
         mApp.execHighlight( -1 );
         break;
       case START:
         mApp.execStart( (int)buffer[2] );
         break; 
       case DONE:
         mApp.execDone();
         break;
       case RESET:
         mApp.execReset( (int)buffer[2] );
         break;
       case ACCEPT:
         mApp.execAccept( (int)buffer[2], ( (buffer.length > 3) ? (int)buffer[3] : 0) );
         break;
       case OFFSET:
         mApp.execColor( (int)buffer[2] );
         break;
       case NEWGAME:
         mApp.execNewGame( ( (buffer.length > 2)? (int)buffer[2] : 0) );
         break;
       case OKGAME:
         mApp.execOkGame( (int)buffer[2] );
         break;
       case PAWNS:
         mApp.execPawns( (int)buffer[2] );
         break;
       case BACKDO:
         mApp.execSetBackDo( (int)buffer[2], (int)buffer[3] );
         break;
     }
  }

  // put a command onto the queue
  // the buffer has two header bytes, followed by the command string, terminated by 0xff
  //
  // data must be terminated by adding 0xff
  private void enqueue( byte key, int v1, int v2 )
  {
    byte[] buf = null;
    switch ( key ) {
       case THROW:
       case START:
       case MOVED:
       case HIGH:
       case RESET:
       case OFFSET:
       case OKGAME:
       case NEWGAME:
       case PAWNS:
         buf = new byte[4];
         buf[0] = mSendCounter;
         buf[1] = key;
         buf[2] = (byte)v1;
         buf[3] = EOL;
         break; 
       case ACCEPT:
       case MOVE:
       case BACKDO:
         buf = new byte[5];
         buf[0] = mSendCounter;
         buf[1] = key;
         buf[2] = (byte)v1;
         buf[3] = (byte)v2;
         buf[4] = EOL;
         break;
       case DONE:
       case HIGHOFF:
         buf = new byte[3];
         buf[0] = mSendCounter;
         buf[1] = key;
         buf[2] = EOL;
         break;
    }
    if ( buf != null ) {
      mBufferQueue.add( buf );
      // Log.v( TAG, "enqueue <" + mSendCounter + "|" + key + "> queue " + mBufferQueue.size() );
      mSendCounter = increaseCounter( mSendCounter );
    }
  }

  void sendBackDo( int tito, int skip ) { enqueue( BACKDO, tito, skip ); }
  void sendStart( int move )  { enqueue( START, move, 0 ); }
  void sendThrow( int move )  { enqueue( THROW, move, 0 ); }
  void sendMoved( int index ) { enqueue( MOVED, index, 0 ); }

  void sendHighlight( int pos )     
  { 
    if ( pos >= 0 ) enqueue( HIGH, pos, 0 );
    else            enqueue( HIGHOFF, 0, 0 );
  }

  void sendMove( int from, int to )
  {
    enqueue( MOVE, from, to );
    if ( YutnoriPrefs.mPos > YutnoriPrefs.POS_NO ) Delay.sleep( 5 );
  }

  void sendDone( )            { enqueue( DONE, 0, 0 );  }
  void sendNewGame( )         { enqueue( NEWGAME, YutnoriPrefs.mPos, 0 );  }
  void sendOkGame( int bool ) { enqueue( OKGAME, bool, 0 );  }
  void sendPawns( int nr )    { enqueue( PAWNS, nr, 0 );  }
  void sendReset( int state ) { enqueue( RESET, state, 0 ); }
  void sendOffset( int off )  { enqueue( OFFSET, off, 0 ); }

  void sendAccept( int bool, int pos )
  {
    mAccepted = (bool == 1);
    enqueue( ACCEPT, bool, pos );
  }

  // -------------------------------------------------------------------
  // need a thread to empty the queue and write to the SyncService connected thread
  // incoming messages are handled by this class directly

  static int SLEEP_DEQUE =  100; 
  static int SLEEP_EMPTY = 1000;

  private class SendThread extends Thread
  {
    boolean mRunning;

    SendThread( )
    {
      mRunning = false;
    }

    boolean isRunning() { return mRunning; }
  
    @Override
    public void run()
    {
      mRunning = true;
      int cnt = 0;
      byte lastByte = (byte)0xff;
      // Log.v( "DistoX", "SendThread running ...");
      while( mRun ) {
        try {
          // Log.v( TAG, "SendThread queue " + mBufferQueue.size() + " - run " + mRun );
          if ( mBufferQueue.isEmpty() ) {
            Thread.sleep( SLEEP_EMPTY );
          } else {
            // byte buffer[] = mBufferQueue.peek();
            // write( buffer );
            ConnectionQueueItem item = mBufferQueue.peek();
            if ( item != null ) {
              byte[] buffer = item.mData;
              if ( buffer[0] == lastByte ) {
                ++ cnt;
              }
              // Log.v( TAG, "lastByte " + lastByte + " cnt " + cnt );
              if ( cnt > 4 ) {
                // bail-out
                disconnect( /* mDevice */ );
                mApp.connStateChanged( 51 );
              } else {
                lastByte = buffer[0];
                // Log.v( TAG, "data write <" + buffer[0] + "|" + buffer[1] + ">" );
                if ( writeBytes( item.mData ) ) {
                  cnt = 0;
                } else {
                  ++ cnt;
                }
              }
            }
            Thread.sleep( SLEEP_DEQUE );   
          }
        } catch ( InterruptedException e ) {
        }
      }
      mRunning = false;
      // Log.v( TAG, "SendThread exiting");
    }
  }

  @Override
  public void handleMessage( Message msg )
  {
    // Log.v(TAG, "CH handle message. Arg1: " + msg.arg1 + " what " + msg.what );
    Bundle bundle; 
    switch (msg.what) {
      case SyncService.MESSAGE_LOST_CONN: // 5
        // Log.v( TAG, "CH lost connection ");
        if ( mAccepted ) {
          mApp.connStateChanged( 50 );
          reconnect();
        }
        break;
      case SyncService.MESSAGE_FAIL_CONN: // 6
        // Log.v( TAG, "CH failed connection ");
        // if ( mAccepted ) {
          connectionFailed();
          mApp.connStateChanged( 60 );
        // }
        break;
      case SyncService.MESSAGE_CONNECT_STATE: // 1
        // Log.v( TAG, "handle message: CH connect state " + msg.arg1 );
        mApp.connStateChanged( 10+msg.arg1 );
        break;
      case SyncService.MESSAGE_ACCEPT_STATE: // 7
        // Log.v( TAG, "handle message: CH accept state " + msg.arg1 );
        mApp.connStateChanged( 70+msg.arg1 );
        break;
      case SyncService.MESSAGE_DEVICE: // 2
        resetCounters();
        bundle = msg.getData();
        String name = bundle.getString( SyncService.DEVICE );
        // Log.v( TAG, "CH device " + name );
        mApp.syncConnectedDevice( name );
        startSendThread();
        break;
      case SyncService.MESSAGE_READ: // 3
        int bytes = msg.arg1;
        // Log.v( TAG, "READ bytes " + bytes );
        byte[] buffer = ( byte[] ) msg.obj;
        onRecv( bytes, buffer );
        break;
      case SyncService.MESSAGE_WRITE: // 4
        // Log.v( TAG, "WRITE " );
        // nothing
        break;
    }

  }

}

