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

  // private CheckBox mCBmalsplit;
  private CheckBox mCBbackdo;
  private CheckBox mCBdoskip;
  private CheckBox mCBdospot;
  private CheckBox mCBdocage;
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
      // mCBmalsplit   = (CheckBox) findViewById( R.id.cb_malsplit );
      mCBbackdo     = (CheckBox) findViewById( R.id.cb_backdo );
      mCBseoul      = (CheckBox) findViewById( R.id.cb_seoul    );
      mCBbusan      = (CheckBox) findViewById( R.id.cb_busan    );
      mCBdoskip     = (CheckBox) findViewById( R.id.cb_doskip );
      mCBdospot     = (CheckBox) findViewById( R.id.cb_dospot   );
      mCBdocage     = (CheckBox) findViewById( R.id.cb_docage   );

      if ( YutnoriPrefs.isSpecial() ) {
        // mCBmalsplit.setChecked( YutnoriPrefs.mSplitGroup );
        mCBbackdo.setChecked( YutnoriPrefs.isBackDo() );
        mCBseoul.setChecked( YutnoriPrefs.isSeoul() );
        mCBbusan.setChecked( YutnoriPrefs.isBusan() );
        mCBbackdo.setOnClickListener( this );
        mCBseoul.setOnClickListener( this );
        mCBbusan.setOnClickListener( this );

        mCBdoskip.setChecked( YutnoriPrefs.isDoSkip() );
        mCBdospot.setChecked( YutnoriPrefs.isDoSpot() );
        mCBdocage.setChecked( YutnoriPrefs.isDoCage() );
        mCBdoskip.setOnClickListener( this );
        mCBdospot.setOnClickListener( this );
        mCBdocage.setOnClickListener( this );
      } else {
        findViewById( R.id.special_rules ).setVisibility( View.GONE );
        findViewById( R.id.backdo_rules  ).setVisibility( View.GONE );
      }

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
        dismiss();
        mApp.doNewGame();
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

      case R.id.cb_backdo:
        if ( mCBbackdo.isChecked() ) {
          mCBseoul.setChecked( false );
          mCBbusan.setChecked( false );
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
    }
  }

}

