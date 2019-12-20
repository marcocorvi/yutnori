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

  // private CheckBox mCBmalsplit;
  private CheckBox mCBbackdo;
  private CheckBox mCBseoul;
  private CheckBox mCBbusan;
  private CheckBox mCBdoskip;
  private CheckBox mCBdospot;
  private CheckBox mCBdocage;

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

    // mCBmalsplit   = (CheckBox) findViewById( R.id.cb_malsplit );
    mCBbackdo     = (CheckBox) findViewById( R.id.cb_backdo );
    mCBseoul      = (CheckBox) findViewById( R.id.cb_seoul    );
    mCBbusan      = (CheckBox) findViewById( R.id.cb_busan    );

    mCBdoskip = (CheckBox) findViewById( R.id.cb_doskip );
    mCBdospot     = (CheckBox) findViewById( R.id.cb_dospot   );
    mCBdocage     = (CheckBox) findViewById( R.id.cb_docage   );

    if ( YutnoriPrefs.isSpecial() ) {
      switch ( YutnoriPrefs.mSpecialRule ) {
        case YutnoriPrefs.BACKDO:
          mCBbackdo.setChecked( true );
          break;
        case YutnoriPrefs.SEOUL:
          mCBseoul.setChecked( true );
          break;
        case YutnoriPrefs.BUSAN:
          mCBbusan.setChecked( true );
          break;
      }
      mCBbackdo.setOnClickListener( this );
      mCBseoul.setOnClickListener( this );
      mCBbusan.setOnClickListener( this );

      switch ( YutnoriPrefs.mBackDo ) {
        case YutnoriPrefs.DO_SKIP:
          mCBdoskip.setChecked( true );
          break;
        case YutnoriPrefs.DO_SPOT:
          mCBdospot.setChecked( true );
          break;
        case YutnoriPrefs.DO_CAGE:
          mCBdocage.setChecked( true );
          break;
      }
      mCBdoskip.setOnClickListener( this );
      mCBdospot.setOnClickListener( this );
      mCBdocage.setOnClickListener( this );

      // mCBmalsplit.setChecked( YutnoriPrefs.mSplitGroup );
    } else {
      findViewById( R.id.special_rules ).setVisibility( View.GONE );
      findViewById( R.id.backdo_rules  ).setVisibility( View.GONE );
    }


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
        if ( YutnoriPrefs.isSpecial() ) {
          int rule = mCBbackdo.isChecked() ? YutnoriPrefs.BACKDO :
                     mCBseoul.isChecked() ? YutnoriPrefs.SEOUL :
                     mCBbusan.isChecked() ? YutnoriPrefs.BUSAN :
                     YutnoriPrefs.NONE;
          int backdo = mCBdoskip.isChecked() ? YutnoriPrefs.DO_SKIP :
                       mCBdospot.isChecked() ? YutnoriPrefs.DO_SPOT :
                       mCBdocage.isChecked() ? YutnoriPrefs.DO_CAGE :
                       YutnoriPrefs.DO_NONE;
          mApp.setPrefs(
            false, // mCBmalsplit.isChecked(), 
            rule,
            backdo
          );
        }
        onBackPressed();
        break;
      case R.id.btn_help:
        mApp.startHelp();
        break;
      case R.id.cb_doskip:
        if ( mCBdoskip.isChecked() ) {
          mCBdospot.setChecked( false );
          mCBdocage.setChecked( false );
        }
        break;
      case R.id.cb_dospot:
        if ( mCBdospot.isChecked() ) {
          mCBdoskip.setChecked( false );
          mCBdocage.setChecked( false );
        }
        break;
      case R.id.cb_docage:
        if ( mCBdocage.isChecked() ) {
          mCBdoskip.setChecked( false );
          mCBdospot.setChecked( false );
        }
        break;
      case R.id.cb_seoul:
        if ( mCBseoul.isChecked() ) {
          mCBbackdo.setChecked( false );
          mCBbusan.setChecked( false );
        }
        break;
      case R.id.cb_busan:
        if ( mCBbusan.isChecked() ) {
          mCBbackdo.setChecked( false );
          mCBseoul.setChecked( false );
        }
        break;
      case R.id.cb_backdo:
        if ( mCBbackdo.isChecked() ) {
          mCBseoul.setChecked( false );
          mCBbusan.setChecked( false );
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

