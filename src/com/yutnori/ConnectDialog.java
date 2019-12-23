/* @file ConnectDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief Connection dialog with another Yutnori
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.yutnori;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import android.app.Dialog;
import android.os.Bundle;
// import android.os.AsyncTask;

import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;

import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import android.widget.Toast;

import android.util.Log;

import android.bluetooth.BluetoothDevice;

public class ConnectDialog extends Dialog
                           implements View.OnClickListener
                           , OnItemClickListener
{
  private static final int REQUEST_DEVICE    = 1;
  // static final String TAG = "Yutnori-TITO"

  private TextView mTVaddress;

  // private ArrayAdapter<String> mArrayAdapter;
  private ListItemAdapter mArrayAdapter;
  private ListView mList;

  // private String mAddress;

  // private Button mBtnCancel;
  private Button mBtnConnect;
  private Button mBtnDisconnect;
  // private Button mBtnStart;
  // private Button mBtnStop;
  private Button mBtnSync;

  private TextView mTVstate;

  private Context mContext;
  private Main mApp;

  private String mName = null;
  Set<BluetoothDevice> mDevices;

  // void setButtons( int state ) 
  // {
  //   switch ( state ) {
  //     case SyncService.STATE_NONE:
  //       mBtnConnect.setText( "ATTACH" );
  //       mBtnStart.setText( "START" );
  //       break;
  //     case SyncService.STATE_LISTEN:
  //       mBtnStart.setText( "READY" );
  //       break;
  //     case SyncService.STATE_CONNECTING:
  //       break;
  //     case SyncService.STATE_CONNECTED:
  //       mBtnConnect.setText( "ATTACHED" );
  //       break;
  //   }
  // }

  ConnectDialog( Context context, Main app )
  {
    super( context );
    mContext = context;
    mApp     = app;
    mName    = mApp.mRemote;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    // mAddress = getIntent().getExtras().getString( DEVICE_ADDR );

    setContentView( R.layout.connect_dialog );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mTVaddress = (TextView) findViewById( R.id.device_address );
    mTVstate   = (TextView) findViewById( R.id.conn_state );

    if ( mApp.getAcceptState() == SyncService.STATE_LISTEN ) {
      String str = String.format( mContext.getResources().getString( R.string.device_status ), 
                                  mApp.LISTEN + " | " + mApp.getConnectionStateStr() );
      mTVstate.setText( str );
    } else {
      mTVstate.setText( mApp.getConnectionStateStr() );
    }
    if ( mName != null ) mTVaddress.setText( mName );

    // mArrayAdapter = new ArrayAdapter<String>( this, R.layout.message );
    mArrayAdapter = new ListItemAdapter( mContext, R.layout.message );
    mList = (ListView) findViewById(R.id.dev_list);
    mList.setAdapter( mArrayAdapter );
    // mList.setLongClickable( true );
    // mList.setOnItemLongClickListener( this );
    mList.setDividerHeight( 2 );

    mBtnConnect = (Button) findViewById( R.id.button_connect );
    mBtnDisconnect = (Button) findViewById( R.id.button_disconnect );
    // mBtnStart = (Button) findViewById( R.id.button_start );
    // mBtnStop = (Button) findViewById( R.id.button_stop );
    mBtnSync = (Button) findViewById( R.id.button_sync );

    if ( mApp.mConnected ) {
      mBtnConnect.setEnabled( false );
      mBtnDisconnect.setOnClickListener( this );
      mBtnSync.setOnClickListener( this );
    } else {
      mList.setOnItemClickListener( this );
      mBtnConnect.setOnClickListener( this );
      mBtnDisconnect.setEnabled( false );
      mBtnSync.setEnabled( false );
    }

    // mBtnStart.setOnClickListener( this );
    // mBtnStop.setOnClickListener( this );

    setTitle( mContext.getResources().getString( R.string.devices ) );
    updateList();
  }

  private void updateList( )
  {
    // Log.v( TAG, "updateList" );
    // mList.setAdapter( mArrayAdapter );
    mArrayAdapter.clear();
    if ( mApp.mBTAdapter != null ) {
      mDevices = mApp.mBTAdapter.getBondedDevices(); // get paired devices
      if ( mDevices.isEmpty() ) {
        // Toast.makeText(this, "no_paired_device", Toast.LENGTH_SHORT).show();
      } else {
        for ( BluetoothDevice device : mDevices ) {
          // String addr = device.getAddress();
          mArrayAdapter.add( device.getName() );
        }
      }
    }
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    CharSequence item = ((TextView) view).getText();
    String name = item.toString();
    // if ( mName != null && ! mName.equals( name ) ) {
    //   if ( mApp.getConnectionType() == SyncService.STATE_CONNECTED ) {
    //     disconnectDevice(); // FIXME do this ?
    //   // } else if ( mApp.getConnectionType() == SyncService.STATE_LISTEN ) {
    //   //   stopDevice();
    //   }
    // }
    mName = name;
    mTVaddress.setText( mName );
  }

  private boolean connectDevice()
  {
    // Log.v( TAG, "ConnectDialog::connect Device() state " + mApp.getConnectionStateStr() );
    if ( mName != null ) {
      for ( BluetoothDevice device : mDevices ) {
        if ( mName.equals( device.getName() ) ) {
          mApp.connectRemoteYutnori( device );
          return true;
        }
      }
    }
    return false;
  }

  private boolean disconnectDevice()
  {
    // Log.v( TAG, "disconnectDevice " + mName );
    // Log.v( TAG, "disconnectDevice state " + mApp.getConnectionStateStr() );
    // Let the user choose which device to disconnect from

    // Use this if n-n 
    // for ( BluetoothDevice device : mDevices ) {
    //   if ( mName != null && mName.equals( device.getName() ) ) {
    //     mApp.disconnectRemoteYutnori( device );
    //     return;
    //   }
    // }
    if ( mName != null ) {
      mApp.disconnectRemoteYutnori( );
      return true;
    }
    return false;
  }

  private void syncDevice()
  {
    // Log.v( TAG, "syncDevice " + mName );
    for ( BluetoothDevice device : mDevices ) {
      if ( mName != null && mName.equals( device.getName() ) ) {
        mApp.syncRemoteYutnori( device );
        return;
      }
    }
  }

  // private void startDevice()
  // {
  //   Log.v( TAG, "startDevice state " + mApp.getConnectionStateStr() );
  //   if ( mApp.getConnectState() != SyncService.STATE_NONE ) return;
  //   if ( mApp.getConnectionType() == SyncService.STATE_NONE ) {
  //     mApp.startRemote( );
  //   }
  // }

  // private void stopDevice()
  // {
  //   Log.v( TAG, "stopDevice state " + mApp.getConnectionStateStr() );
  //   if ( mApp.getConnectState() != SyncService.STATE_CONNECTED 
  //     && mApp.getConnectState() != SyncService.STATE_LISTEN ) return;
  //   if ( mApp.getConnectionType() == SyncService.STATE_LISTEN ) {
  //     mApp.stopRemote( );
  //   }
  // }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;
    // if ( b == mBtnStart ) {
    //   startDevice();
    //   mApp.closeConnectDialog();
    // } else if ( b == mBtnStop ) {
    //   stopDevice();
    //   mApp.closeConnectDialog();
    // } else
    if ( b == mBtnConnect ) {
      if ( mApp.getConnectState() != SyncService.STATE_NONE ) {
        Toast.makeText( mContext, R.string.already_connected, Toast.LENGTH_SHORT).show();
      } else if ( connectDevice() ) {
        Log.v("Yutnori-EXEC", "connect device and close dialog" );
        mApp.closeConnectDialog();
      } else {
        Toast.makeText( mContext, R.string.no_device, Toast.LENGTH_SHORT).show();
      } 
    } else if ( b == mBtnDisconnect ) {
      if ( mApp.getConnectState() != SyncService.STATE_CONNECTED ) {
        Toast.makeText( mContext, R.string.not_connected, Toast.LENGTH_SHORT).show();
      } else if ( disconnectDevice() ) { // this is true iff mName != null
        mApp.closeConnectDialog();
      } else {
        Toast.makeText( mContext, R.string.no_device, Toast.LENGTH_SHORT).show();
      }
    } else if ( b == mBtnSync ) {
      if ( mApp.getConnectState() != SyncService.STATE_CONNECTED ) {
        Toast.makeText( mContext, R.string.not_connected, Toast.LENGTH_SHORT).show();
      } else if ( mName != null ) {
        syncDevice();
        mApp.closeConnectDialog();
      } else {
        Toast.makeText( mContext, R.string.no_device, Toast.LENGTH_SHORT).show();
      }
    // } else if ( b == mBtnCancel ) {
    //   /* nothing */
    } 
    //   mApp.closeConnectDialog();
  }

  @Override
  public void onBackPressed()
  {
    mApp.closeConnectDialog();
  }

}

