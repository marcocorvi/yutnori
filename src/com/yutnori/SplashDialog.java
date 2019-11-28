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
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import android.widget.Toast;

import android.util.Log;

public class SplashDialog extends Dialog
                          implements View.OnClickListener
{
  // static final String TAG = "yutnori";

  private Main mApp;
  private Context mContext;

  private RadioButton mCBwait;
  private RadioButton mCBjoin;
  private RadioButton mCBandroid;

  private CheckBox mCBmalsplit;
  private CheckBox mCBbackdo;
  private CheckBox mCBdoskip;
  private CheckBox mCBdospot;
  private CheckBox mCBseoul;
  private CheckBox mCBbusan;

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

    mCBmalsplit   = (CheckBox) findViewById( R.id.cb_malsplit );
    mCBbackdo     = (CheckBox) findViewById( R.id.cb_backdo );

    mCBdoskip = (CheckBox) findViewById( R.id.cb_doskip );
    mCBdospot     = (CheckBox) findViewById( R.id.cb_dospot   );
    mCBseoul      = (CheckBox) findViewById( R.id.cb_seoul    );
    mCBbusan      = (CheckBox) findViewById( R.id.cb_busan    );

    if ( YutnoriPrefs.mSpecialRules ) {
      mCBbackdo.setChecked( ( YutnoriPrefs.mTiTo ) );
      if ( YutnoriPrefs.mTiToSkip ) { 
        mCBdoskip.setChecked( true );
      } else if ( YutnoriPrefs.mDoSpot ) {
        mCBdospot.setChecked( true );
      } else if ( YutnoriPrefs.mSeoul ) {
        mCBseoul.setChecked( true );
      } else if ( YutnoriPrefs.mBusan ) {
        mCBbusan.setChecked( true );
      }
      mCBmalsplit.setChecked( YutnoriPrefs.mSplitGroup );
    } else {
      findViewById( R.id.special_rules ).setVisibility( View.GONE );
      findViewById( R.id.backdo_rules  ).setVisibility( View.GONE );
    }

    mCBdoskip.setOnClickListener( this );
    mCBdospot.setOnClickListener( this );
    mCBseoul.setOnClickListener( this );
    mCBbusan.setOnClickListener( this );

    mBTok = (Button) findViewById( R.id.btn_ok );
    mBThelp = (Button) findViewById( R.id.btn_help );
    mBTok.setOnClickListener( this );
    mBThelp.setOnClickListener( this );

    setTitle( mApp.getResources().getString( R.string.app_name ) );

  }

  @Override
  public void onClick(View v) 
  {
    switch ( v.getId() ) {
      case R.id.btn_ok:
        if ( YutnoriPrefs.mSpecialRules ) {
          mApp.setPrefs( mCBmalsplit.isChecked(), mCBbackdo.isChecked(), 
            mCBdoskip.isChecked(), mCBdospot.isChecked(), mCBseoul.isChecked(), mCBbusan.isChecked() );
        }
        onBackPressed();
        break;
      case R.id.btn_help:
        mApp.startHelp();
        break;
      case R.id.cb_doskip:
        if ( mCBdoskip.isChecked() ) {
          mCBdospot.setChecked( false );
          mCBseoul.setChecked( false );
          mCBbusan.setChecked( false );
        }
        break;
      case R.id.cb_dospot:
        if ( mCBdospot.isChecked() ) {
          mCBdoskip.setChecked( false );
          mCBseoul.setChecked( false );
          mCBbusan.setChecked( false );
        }
        break;
      case R.id.cb_seoul:
        if ( mCBseoul.isChecked() ) {
          mCBdoskip.setChecked( false );
          mCBdospot.setChecked( false );
          mCBbusan.setChecked( false );
        }
        break;
      case R.id.cb_busan:
        if ( mCBbusan.isChecked() ) {
          mCBdoskip.setChecked( false );
          mCBdospot.setChecked( false );
          mCBseoul.setChecked( false );
        }
        break;
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
    // dismiss();
    mApp.closeSplashDialog();
    mApp.setStartAs( start );
  }

}

