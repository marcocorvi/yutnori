/** @file SyncService.java
 * 
 * this class is made after the sample BluetoothChat by the 
 * The Android Open Source Project which is licenced under
 * the Apache License, Version 2.0 (the "License");
 */
package com.yutnori;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.res.Resources;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.util.Log;

public class SyncService 
{
  final static String TAG = "yutnori";

  // Name for the SDP record when creating server socket
  private static final String NAME = "YutnoriSync";

  // Unique UUID for this application
  private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

  // Member fields
  private Context mContext;
  private Main mApp;

  private BluetoothAdapter mAdapter;
  private BluetoothDevice  mRemoteDevice;
  private final Handler    mHandler;
  private AcceptThread     mAcceptThread;
  private ConnectingThread mConnectingThread;
  private ConnectedThread  mConnectedThread;

  private int mConnectState; // NONE   --> CONNECTING --> CONNECTED --> NONE
  private int mAcceptState;  // LISTEN --> NONE ................... --> LISTEN

  static final String mStateName[] = { "none", "listen", "connecting", "connected" };
  private int mType;   // the service type. either server (LISTEN) or client (CONNECTING)
  private boolean mConnectRun;
  private boolean mAcceptRun;

  // Constants that indicate the current connection state
  static final int STATE_NONE = 0;       // we're doing nothing
  static final int STATE_LISTEN = 1;     // now listening for incoming connections
  static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
  static final int STATE_CONNECTED = 3;  // now connected to a remote device

  static final int MESSAGE_CONNECT_STATE  = 1;
  static final int MESSAGE_DEVICE = 2;
  static final int MESSAGE_READ   = 3;
  static final int MESSAGE_WRITE  = 4;
  static final int MESSAGE_LOST_CONN = 5;
  static final int MESSAGE_FAIL_CONN = 6;
  static final int MESSAGE_ACCEPT_STATE = 7;

  static final String DEVICE = "DEVICE";

  public SyncService( Context context, Main app, Handler handler )
  {
    // mContext = context;
    mApp     = app;
    mAdapter = mApp.mBTAdapter; // BluetoothAdapter.getDefaultAdapter();
    mRemoteDevice = null;
    mConnectState = STATE_NONE;
    mAcceptState  = STATE_NONE;

    mType       = STATE_NONE;
    mHandler    = handler;
    mConnectRun = false;
    mAcceptRun  = false;
  }

  private synchronized void setConnectState(int state)
  {
    // Log.v( TAG, "sync connect state: " + mStateName[mConnectState] + " -> " + mStateName[state] );
    // if ( state == STATE_NONE ) mRemoteDevice = null;
    mConnectState = state;
    mHandler.obtainMessage( MESSAGE_CONNECT_STATE, state, -1).sendToTarget();
  }

  private synchronized void setAcceptState(int state)
  {
    // Log.v( TAG, "sync accept state " + mStateName[mAcceptState] + " -> " + mStateName[state] );
    mAcceptState = state;
    mHandler.obtainMessage( MESSAGE_ACCEPT_STATE, state, -1).sendToTarget();
  }

  public synchronized int getConnectState() { return mConnectState; }
  public synchronized int getAcceptState() { return mAcceptState; }

  String getConnectStateStr()
  {
    switch ( mConnectState ) {
      case STATE_NONE: return "NONE";
      // case STATE_LISTEN: return "LISTEN";
      case STATE_CONNECTING: return "CONNECTING";
      case STATE_CONNECTED: return "CONNECTED " + mRemoteDevice.getName();
    }
    return "UNKNOWN";
  }

  String getConnectedDeviceName()
  {
    return ( mRemoteDevice != null )? mRemoteDevice.getName() : null;
  }

  public int getType() { return mType; }

  public synchronized void start( Context context ) 
  {
    // Log.v( TAG, "sync start()" );
    mContext    = context;
    mAcceptRun  = false;
    mConnectRun = false;
    if (mConnectingThread != null) { mConnectingThread.cancelJT(); mConnectingThread = null; }
    if (mConnectedThread  != null) { mConnectedThread.cancelCT();  mConnectedThread  = null; }
    startAccept( );
  }

  private synchronized void startAccept( )
  {
    // Log.v( TAG, "sync start accept-thread" );
    if (mAcceptThread != null) {
      mAcceptRun = false;
      setAcceptState(STATE_NONE);
      try {
        mAcceptThread.join();
      } catch ( InterruptedException e ) { }
    }

    mAcceptRun = true;
    mType = STATE_LISTEN;
    mAcceptThread = new AcceptThread( mContext );
    mAcceptThread.start();
    setAcceptState(STATE_LISTEN);
  }

  private void nullAccept()
  {
    if ( /* mConnectState == STATE_LISTEN && */ mAcceptThread != null ) {
      mAcceptThread.cancelAT();
      mAcceptThread = null;
    }
  }

  private void nullConnecting()
  {
    if ( /* mConnectState == STATE_CONNECTING && */ mConnectingThread != null ) {
      mConnectingThread.cancelJT();
      mConnectingThread = null;
    }
  }

  private void nullConnected()
  {
    if ( /* mConnectState == STATE_CONNECTED && */ mConnectedThread != null ) {
      mConnectedThread.cancelCT();
      mConnectedThread = null;
    }
  }

  /**
   * Start the ConnectingThread to initiate a connection to a remote device.
   * @param device  The BluetoothDevice to connect
   */
  public synchronized void connect( BluetoothDevice device )
  {
    // Log.v( TAG, "sync connect to " + device.getName() + " connect-state " + mConnectState );
    mConnectRun = false;
    nullConnecting();
    nullConnected();

    mRemoteDevice = device;
    reconnect();
    // Log.v( TAG, "sync connect done");
  }

  private synchronized void reconnect()
  {
    if ( mRemoteDevice == null ) return;
    // Log.v( TAG, "sync reconnect to " + mRemoteDevice.getName() );
    mType = STATE_CONNECTING;
    mConnectRun = true;
    mConnectingThread = new ConnectingThread( mRemoteDevice );
    mConnectingThread.start();
    setConnectState(STATE_CONNECTING);
  }

  /**
   * Start the ConnectedThread to begin managing a Bluetooth connection
   * @param socket  The BluetoothSocket on which the connection was made
   * @param device  The BluetoothDevice that has been connected
   */
  public synchronized void connected(BluetoothSocket socket, BluetoothDevice device)
  {
    // Log.v( TAG, "sync connected. remote device " + device.getName() );
    nullConnecting();
    nullConnected();
    // nullAccept(); // ONE-TO-ONE

    String name = device.getName();
    if ( name != null ) {
      mRemoteDevice = device;
      mConnectRun = true;

      mConnectedThread = new ConnectedThread(socket);
      mConnectedThread.start();

      Message msg = mHandler.obtainMessage( MESSAGE_DEVICE );
      Bundle bundle = new Bundle();
      
      bundle.putString( DEVICE, name );
      msg.setData(bundle);
      mHandler.sendMessage(msg);

      setConnectState( STATE_CONNECTED );
    } else {
      disconnect();
    }
  }

  public synchronized void disconnect() 
  {
    // Log.v( TAG, "sync disconnect");
    nullConnecting();

    if ( mConnectState == STATE_CONNECTED ) {
      byte shutdown[] = new byte[4];
      shutdown[0] = 0;
      shutdown[1] = ConnectionHandler.SHUTDOWN;
      shutdown[2] = 0;
      shutdown[3] = ConnectionHandler.EOL;
      writeBuffer( shutdown ); // FIXME if failure ? nothing: connectedThread already closed
    }
    if ( mConnectedThread != null ) { mConnectedThread.cancelCT(); mConnectedThread = null; }
    mRemoteDevice = null;
    setConnectState( STATE_NONE );
    // startAccept( ); // ONE-TO-ONE ??
  }

  public synchronized void stop() 
  {
    // Log.v( TAG, "sync stop");
    // disconnect();
    nullAccept();
    setAcceptState( STATE_NONE );
    mType    = STATE_NONE;
    mContext = null;
  }

  // --------------------------------------------------------------------

  public boolean writeBuffer( byte[] buffer ) 
  {
    // Log.v( TAG, "sync write (conn state " + mConnectState + " length " + buffer.length + ") " + buffer[0] + " " + buffer[1] + " ... "); 
    ConnectedThread r;    // Create temporary object
    synchronized (this) { // Synchronize a copy of the ConnectedThread
      if ( mConnectState != STATE_CONNECTED ) return false;
      r = mConnectedThread;
    }
    if ( r.doWriteBuffer( buffer ) ) {  // Perform the write unsynchronized
      return true;
    } // else {
    mConnectedThread.cancelCT();
    mConnectedThread = null;
    mRemoteDevice = null;
    setConnectState( STATE_NONE );
    return false;
  }

  // called by the Connect-Thread
  private void connectionFailed()  
  {
    // Log.v( TAG, "sync connection failed");
    mRemoteDevice = null;
    setConnectState(STATE_NONE);
    mType = STATE_NONE;
    Message msg = mHandler.obtainMessage( MESSAGE_FAIL_CONN );
    mHandler.sendMessage(msg);
  }

  // called by the connected-Thread
  private void connectionLost() 
  {
    // Log.v( TAG, "sync connection lost");
    mRemoteDevice = null;
    setConnectState(STATE_NONE);
    Message msg = mHandler.obtainMessage( MESSAGE_LOST_CONN );
    mHandler.sendMessage(msg);
  }

  /**
   * This thread runs while listening for incoming connections. It behaves
   * like a server-side client. It runs until a connection is accepted
   * (or until cancelled).
   */
  private class AcceptThread extends Thread 
  {
    private Context mContext;
    private BluetoothServerSocket mSSocket;
    private boolean mAcceptConnect = false;

    public AcceptThread( Context context ) 
    {
      mContext = context;
      mAcceptState = STATE_NONE;
      createServerSocket( context );
    }
 
    private void createServerSocket( Context context )
    {
      BluetoothServerSocket tmp = null;
      try { // Create a new listening server socket
        tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        mAcceptState = STATE_LISTEN;
      } catch (IOException e) {
        Log.e( TAG, "listen() failed " + e.getMessage() );
      }
      mSSocket = tmp;
    }

    public void run()
    {
      // Log.v( TAG, "sync AcceptThread run. Server socket" + ((mSSocket!=null)? " not" : "") + " null" );
      setName("AcceptThread");
      BluetoothSocket socket = null;

      if ( mSSocket != null ) {
        while ( mAcceptRun && mAcceptState == STATE_LISTEN ) {
          try {
            // Log.v( TAG, "sync accept listening ... ");
            socket = mSSocket.accept(); // blocking call
          } catch (IOException e) {
            Log.e( TAG, "accept() failed " + e.getMessage() );
            break;
          }

          mAcceptConnect = false;
          if (socket != null) { // If a connection was accepted
            mAcceptConnect = true;
            
            // Resources res = mContext.getResources();
            // String title = String.format( res.getString( R.string.ask_accept ), socket.getRemoteDevice().getName() );
            // String ok = res.getString( R.string.accept );
            // String no = res.getString( R.string.decline );
            // new YutnoriAlertDialog( mContext, res, title, ok, no, 
            //   new DialogInterface.OnClickListener() {
            //     @Override public void onClick( DialogInterface dialog, int btn ) { mAcceptConnect = true; }
            //   },
            //   new DialogInterface.OnClickListener() {
            //     @Override public void onClick( DialogInterface dialog, int btn ) { mAcceptConnect = false; }
            //   }
            // );
          }
          if ( mAcceptConnect ) {
            // Log.v( TAG, "incoming connection request " + socket.getRemoteDevice().getName() );
            synchronized ( SyncService.this ) {
              switch ( mConnectState ) {
                case STATE_NONE:
                case STATE_CONNECTING: // Situation normal. Start the connected thread.
                  BluetoothDevice device = socket.getRemoteDevice();
                  if ( device != null ) {
                    connected( socket, device );
                    break;
                  } // else { fall-through }
                case STATE_CONNECTED: // Either not ready or already connected. Terminate new socket.
                  try {
                    socket.close();
                  } catch (IOException e) {
                    Log.e( TAG, "Could not close unwanted socket " + e.getMessage() );
                  }
                  break;
              }
            }
            socket = null;
          }
        }
      }
      // Log.v( TAG, "sync AcceptThread done");
    }

    public void cancelAT()
    {
      mAcceptState = STATE_NONE;
      mAcceptRun = false;
      try {
        if ( mSSocket != null ) mSSocket.close();
      } catch (IOException e) {
        Log.e( TAG, "close() of server failed " + e.getMessage() );
      }
      mSSocket = null;
    }
  }


  /**
   * This thread runs while attempting to make an outgoing connection with a device.
   * It runs straight through; the connection either succeeds or fails.
   */
  private class ConnectingThread extends Thread
  {
    private BluetoothSocket mJSocket;
    private final BluetoothDevice mJDevice;

    public ConnectingThread( BluetoothDevice device )
    {
      mJDevice = device;
      BluetoothSocket tmp = null;

      // Get a BluetoothSocket for a connection with the given BluetoothDevice
      try {
        // Class[] classes1 = new Class[ 1 ];
        // classes1[0] = int.class;
        // Method m = mJDevice.getClass().getMethod( "createInsecureRfcommSocket", classes1 );
        // tmp = (BluetoothSocket) m.invoke( mBTDevice, 1 );
        //
        tmp = mJDevice.createRfcommSocketToServiceRecord( MY_UUID );
      } catch (IOException e) {
        Log.e( TAG, "ConnectingThread cstr failed " + e.getMessage() );
      }
      mJSocket = tmp;
    }

    public void run()
    {
      // Log.v( TAG, "sync ConnectingThread run");
      setName("ConnectingThread");
      mAdapter.cancelDiscovery(); // Always cancel discovery because it will slow down a connection

      if ( mJSocket != null ) {
        try { // Make a connection to the BluetoothSocket
          mJSocket.connect(); // blocking call
        } catch (IOException e) {
          Log.e( TAG, "sync ConnectingThread IOexception " + e.getMessage() );
          connectionFailed();
          cancelJT();
          // SyncService.this.start(); // Start the service over to restart listening mode
          return;
        }

        synchronized ( SyncService.this ) { // Reset the ConnectingThread because we're done
          mConnectingThread = null;
        }

        connected( mJSocket, mJDevice ); // Start the connected thread
      }
      // Log.v( TAG, "sync connecting thread done");
    }

    public void cancelJT()
    {
      try {
        if ( mJSocket != null ) mJSocket.close();
      } catch (IOException e) {
        Log.e( TAG, "close() of connect socket failed " + e.getMessage() );
      }
      mJSocket = null;
    }
  }

  /**
   * This thread runs during a connection with a remote device.
   * It handles all incoming and outgoing transmissions.
   */
  private class ConnectedThread extends Thread
  {
    private BluetoothSocket mCSocket;
    private InputStream     mCIn;
    private OutputStream    mCOut;

    public ConnectedThread(BluetoothSocket socket)
    {
      mCSocket = socket;
      InputStream  tmpIn  = null;
      OutputStream tmpOut = null;
      if ( mCSocket != null ) {
        try { // Get the BluetoothSocket input and output streams
          tmpIn  = mCSocket.getInputStream();
          tmpOut = mCSocket.getOutputStream();
        } catch (IOException e) {
          Log.e( TAG, "sockets I/O streams not created " + e.getMessage() );
        }
      } else {
        Log.e( TAG, "ERROR null BT socket");
      }

      mCIn  = tmpIn;
      mCOut = tmpOut;
    }

    public void run() 
    {
      // Log.v( TAG, "sync connected thread run");

      byte[] buffer = new byte[512];
      byte[] data   = new byte[4096];
      int bytes;
      int pos = 0; // data pos

      while ( mConnectRun && mCIn != null ) { // Keep listening to the InputStream while connected
        try {
          bytes = mCIn.read(buffer); // Read from the InputStream
          for ( int k=0; k<bytes; ++k ) {
            // add buffer to the data 
            if ( buffer[k] == ConnectionHandler.EOL ) {
              // end of message: send to upper layer
              byte[] tmp = new byte[pos];  
              for ( int j=0; j<pos; ++j) tmp[j] = data[j];
              // special handle shutdown message 
              if ( data[0] == 0 && data[1] == ConnectionHandler.SHUTDOWN && data[2] == 0 ) {
                cancelCT(); // mConnectRun = false;
                            // setConnectState( STATE_NONE );
                mRemoteDevice = null;
              } else {
                // Log.v( TAG, "read <" + data[0] + "|" + data[1] + ">" );
                mHandler.obtainMessage( MESSAGE_READ, pos, -1, tmp).sendToTarget();
                pos = 0;
              }
            } else {
              data[pos] = buffer[k];
              ++pos;
            }
          }
        } catch (IOException e) {
          // Log.v( TAG, "disconnected " + e.getMessage() );
          cancelCT();
          connectionLost();
          break;
        }
      }
      // Log.v( TAG, "sync ConnectedThread done type " + mStateName[mType] );
      if ( mType == STATE_LISTEN ) {
        // startAccept(); // ONE-TO-ONE
      } else if ( mType == STATE_CONNECTING ) {
        try {
          Thread.sleep( 200 );
        } catch ( InterruptedException e ) { }
        reconnect();
      }
    }

    /**
     * Write to the connected OutStream.
     * @param buffer  The bytes to write
     */
    public boolean doWriteBuffer( byte[] buffer ) 
    {
      // Log.v( TAG, "sync connected write " + buffer.length + ": <" + buffer[0] + "|" + buffer[1] + ">" );
      if ( mCOut != null ) {
        try {
          mCOut.write( buffer );
          // Share the sent message back to the UI Activity: NOT USED .... FIXME
          // mHandler.obtainMessage( MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
          return true;
        } catch (IOException e) {
          Log.e( TAG, "Write exception " + e.getMessage()  );
        }
      } else {
        Log.e( TAG, "null socket output stream");
      }
      return false;
    }

    public void cancelCT() 
    {
      mConnectRun = false;
      try {
        if ( mCIn  != null ) { mCIn.close(); }
        if ( mCOut != null ) { mCOut.flush(); mCOut.close(); }
        if ( mCSocket    != null ) mCSocket.close();
      } catch (IOException e) {
        Log.e( TAG, "close of connect socket failed " + e.getMessage()  );
      }
      mCIn  = null;
      mCOut = null;
      mCSocket    = null;
      setConnectState( STATE_NONE );
    }
  }
}
