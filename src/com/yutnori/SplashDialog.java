/* @file SplashDialog.java
 *
 * @author marco corvi
 * @date jan 2016
 *
 * @brief Yutnori splash dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.yutnori;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;

import android.widget.TextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import android.widget.Toast;

import android.util.Log;

public class SplashDialog extends Dialog
                          implements View.OnClickListener
{
  static final String TAG = "yutnori";

  private Main mApp;
  private Context mContext;

  private RadioButton mCBwait;
  private RadioButton mCBjoin;
  private RadioButton mCBandroid;
  private Button mBThelp;
  private Button mBTok;

  SplashDialog( Context context, Main app )
  {
    super( context );
    mContext = context;
    mApp     = app;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setContentView( R.layout.splash_dialog );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mCBwait    = (RadioButton) findViewById( R.id.cb_wait );
    mCBjoin    = (RadioButton) findViewById( R.id.cb_join );
    mCBandroid = (RadioButton) findViewById( R.id.cb_android );
    mCBandroid.setChecked( true );

    mBTok = (Button) findViewById( R.id.btn_ok );
    mBThelp = (Button) findViewById( R.id.btn_help );
    mBTok.setOnClickListener( this );
    mBThelp.setOnClickListener( this );

    setTitle( mApp.getResources().getString( R.string.app_name ) );

  }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;
    if ( b == mBTok ) {
      onBackPressed();
    } else if ( b == mBThelp ) {
      mApp.startHelp();
    }
  }

  @Override
  public void onBackPressed()
  {
    int start = 0;
    if ( mCBwait.isChecked() ) {
      start = 1;
    } else if ( mCBjoin.isChecked() ) {
      start = 2;
    }
    dismiss();
    mApp.setStartAs( start );
  }

}

