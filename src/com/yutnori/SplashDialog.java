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
  private RadioButton mCBone;
  private RadioButton mCBtwo;
  private RadioButton mCBthree;

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

    mCBone   = (RadioButton) findViewById( R.id.cb_one );
    mCBtwo   = (RadioButton) findViewById( R.id.cb_two );
    mCBthree = (RadioButton) findViewById( R.id.cb_three );

    if ( YutnoriPrefs.mBackYuts == 3 ) { mCBthree.setChecked( true ); }
    else if ( YutnoriPrefs.mBackYuts == 2 ) { mCBtwo.setChecked( true ); }
    else { mCBone.setChecked( true ); }

    // mCBmalsplit   = (CheckBox) findViewById( R.id.cb_malsplit );
    mCBseoul  = (CheckBox) findViewById( R.id.cb_seoul    );
    mCBbusan  = (CheckBox) findViewById( R.id.cb_busan    );
    mCBbackdo = (CheckBox) findViewById( R.id.cb_backdo );
    mCBdoskip = (CheckBox) findViewById( R.id.cb_doskip );
    mCBdospot = (CheckBox) findViewById( R.id.cb_dospot   );
    mCBdocage = (CheckBox) findViewById( R.id.cb_docage   );

    mCBseoul.setChecked( YutnoriPrefs.isSeoul() );
    mCBbusan.setChecked( YutnoriPrefs.isBusan() );
    mCBbackdo.setChecked( YutnoriPrefs.isDoNone() );
    mCBdoskip.setChecked( YutnoriPrefs.isDoSkip() );
    mCBdospot.setChecked( YutnoriPrefs.isDoSpot() );
    mCBdocage.setChecked( YutnoriPrefs.isDoCage() );

    mCBbackdo.setOnClickListener( this );
    mCBseoul.setOnClickListener( this );
    mCBbusan.setOnClickListener( this );
    mCBdoskip.setOnClickListener( this );
    mCBdospot.setOnClickListener( this );
    mCBdocage.setOnClickListener( this );

    // mCBmalsplit.setChecked( YutnoriPrefs.mSplitGroup );

    mBTok = (Button) findViewById( R.id.btn_ok );
    mBThelp = (Button) findViewById( R.id.btn_help );
    mBTok.setOnClickListener( this );
    mBThelp.setOnClickListener( this );

    setTitle( mApp.getResources().getString( R.string.app_name ) );

    // showBackDos( mCBbackdo.isChecked() );
  }
          
  // private void showBackDos( boolean show )
  // {
  //   if ( show ) {
  //     findViewById( R.id.backdo_rules  ).setVisibility( View.VISIBLE );
  //   } else {
  //     findViewById( R.id.backdo_rules  ).setVisibility( View.INVISIBLE );
  //   }
  // }

  @Override
  public void onClick(View v) 
  {
    switch ( v.getId() ) {
      case R.id.btn_ok:
        // if ( YutnoriPrefs.isSpecial() ) {
          int rule   = YutnoriPrefs.NONE;
          int backdo = YutnoriPrefs.DO_NONE;
          if ( mCBseoul.isChecked() ) {
            rule = YutnoriPrefs.SEOUL;
          } else if ( mCBbusan.isChecked() ) {
            rule = YutnoriPrefs.BUSAN;
          } else if ( mCBbackdo.isChecked() ) {
            rule = YutnoriPrefs.BACKDO;
          } else if ( mCBdoskip.isChecked() ) {
            rule = YutnoriPrefs.BACKDO;
            backdo = YutnoriPrefs.DO_SKIP;
          } else if ( mCBdospot.isChecked() ) {
            rule = YutnoriPrefs.BACKDO;
            backdo = YutnoriPrefs.DO_SPOT;
          } else if ( mCBdocage.isChecked() ) {
            rule = YutnoriPrefs.BACKDO;
            backdo = YutnoriPrefs.DO_CAGE;
          }
          YutnoriPrefs.mBackYuts = mCBthree.isChecked() ? 3 : mCBtwo.isChecked() ? 2 : 1;
          mApp.setPrefs(
            false, // mCBmalsplit.isChecked(), 
            rule,
            backdo
          );
        // }
        onBackPressed();
        break;
      case R.id.btn_help:
        mApp.startHelp();
        break;
      case R.id.cb_doskip:
        if ( mCBdoskip.isChecked() ) {
          mCBseoul.setChecked( false );
          mCBbusan.setChecked( false );
          mCBbackdo.setChecked( false );
          // mCBdoskip.setChecked( false );
          mCBdospot.setChecked( false );
          mCBdocage.setChecked( false );
        }
        break;
      case R.id.cb_dospot:
        if ( mCBdospot.isChecked() ) {
          mCBseoul.setChecked( false );
          mCBbusan.setChecked( false );
          mCBbackdo.setChecked( false );
          mCBdoskip.setChecked( false );
          // mCBdospot.setChecked( false );
          mCBdocage.setChecked( false );
        }
        break;
      case R.id.cb_docage:
        if ( mCBdocage.isChecked() ) {
          mCBseoul.setChecked( false );
          mCBbusan.setChecked( false );
          mCBbackdo.setChecked( false );
          mCBdoskip.setChecked( false );
          mCBdospot.setChecked( false );
          // mCBdocage.setChecked( false );
        }
        break;
      case R.id.cb_seoul:
        if ( mCBseoul.isChecked() ) {
          // mCBseoul.setChecked( false );
          mCBbusan.setChecked( false );
          mCBbackdo.setChecked( false );
          mCBdoskip.setChecked( false );
          mCBdospot.setChecked( false );
          mCBdocage.setChecked( false );
        }
        break;
      case R.id.cb_busan:
        if ( mCBbusan.isChecked() ) {
          mCBseoul.setChecked( false );
          // mCBbusan.setChecked( false );
          mCBbackdo.setChecked( false );
          mCBdoskip.setChecked( false );
          mCBdospot.setChecked( false );
          mCBdocage.setChecked( false );
        }
        break;
      case R.id.cb_backdo:
        if ( mCBbackdo.isChecked() ) {
          mCBseoul.setChecked( false );
          mCBbusan.setChecked( false );
          // mCBbackdo.setChecked( false );
          mCBdoskip.setChecked( false );
          mCBdospot.setChecked( false );
          mCBdocage.setChecked( false );
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

