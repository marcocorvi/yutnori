/* @file NewGameDialog.java
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
import android.widget.LinearLayout;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import android.widget.Toast;

import android.util.Log;

public class NewGameDialog extends Dialog
                           implements View.OnClickListener
{
  // static final String TAG = "Yutnori";

  private Main mApp;
  private Context mContext;

  private CheckBox mCBmalsplit;
  private CheckBox mCBbackdo;
  private CheckBox mCBdoskip;
  private CheckBox mCBdospot;
  private CheckBox mCBseoul;
  private CheckBox mCBbusan;

  private Button mBTquit;
  private Button mBTagain;

  private String mTitle;

  NewGameDialog( Context context, Main app, String title )
  {
    super( context );
    mContext = context;
    mApp     = app;
    mTitle   = title;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setContentView( R.layout.new_game_dialog );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    if ( mApp.mConnected ) {
      findViewById( R.id.special_rules ).setVisibility( View.GONE );
      findViewById( R.id.backdo_rules  ).setVisibility( View.GONE );
    } else {
      mCBmalsplit   = (CheckBox) findViewById( R.id.cb_malsplit );
      mCBbackdo     = (CheckBox) findViewById( R.id.cb_backdo );
      mCBdoskip     = (CheckBox) findViewById( R.id.cb_doskip );
      mCBdospot     = (CheckBox) findViewById( R.id.cb_dospot   );
      mCBseoul      = (CheckBox) findViewById( R.id.cb_seoul    );
      mCBbusan      = (CheckBox) findViewById( R.id.cb_busan    );

      if ( YutnoriPrefs.mSpecialRules ) {
        mCBmalsplit.setChecked( YutnoriPrefs.mSplitGroup );
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
      } else {
        findViewById( R.id.special_rules ).setVisibility( View.GONE );
        findViewById( R.id.backdo_rules  ).setVisibility( View.GONE );
      }

      mCBdoskip.setOnClickListener( this );
      mCBdospot.setOnClickListener( this );
      mCBseoul.setOnClickListener( this );
      mCBbusan.setOnClickListener( this );
    }

    mBTagain = (Button) findViewById( R.id.btn_again );
    mBTquit  = (Button) findViewById( R.id.btn_quit );
    mBTagain.setOnClickListener( this );
    mBTquit.setOnClickListener( this );

    TextView tv = (TextView) findViewById( R.id.message );
    tv.setText( mTitle );

    setTitle( mApp.getResources().getString( R.string.app_name ) );
  }

  @Override
  public void onClick(View v) 
  {
    switch ( v.getId() ) {
      case R.id.btn_quit:
        dismiss();
        mApp.askExit();
        break;
      case R.id.btn_again:
        if ( ! mApp.mConnected ) { 
          if ( YutnoriPrefs.mSpecialRules ) {
            mApp.setPrefs( mCBmalsplit.isChecked(), mCBbackdo.isChecked(),
              mCBdoskip.isChecked(), mCBdospot.isChecked(), mCBseoul.isChecked(), mCBbusan.isChecked() );
          }
        }
        dismiss();
        mApp.doNewGame();
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

}

