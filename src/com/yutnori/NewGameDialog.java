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
import android.widget.RadioButton;
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

  private RadioButton mCBone;
  private RadioButton mCBtwo;
  private RadioButton mCBthree;

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

    // Log.v("Yutnori-NEW", "new game" );

    // if ( mApp.mConnected ) {
    //   findViewById( R.id.special_rules ).setVisibility( View.GONE );
    //   findViewById( R.id.backdo_rules  ).setVisibility( View.GONE );
    //   findViewById( R.id.backyuts_nr  ).setVisibility( View.GONE );
    // } else {
      mCBone   = (RadioButton) findViewById( R.id.cb_one );
      mCBtwo   = (RadioButton) findViewById( R.id.cb_two );
      mCBthree = (RadioButton) findViewById( R.id.cb_three );

      if ( YutnoriPrefs.mBackYuts == 3 ) { mCBthree.setChecked( true ); }
      else if ( YutnoriPrefs.mBackYuts == 2 ) { mCBtwo.setChecked( true ); }
      else { mCBone.setChecked( true ); }

      // mCBmalsplit   = (CheckBox) findViewById( R.id.cb_malsplit );
      mCBbackdo = (CheckBox) findViewById( R.id.cb_backdo );
      mCBseoul  = (CheckBox) findViewById( R.id.cb_seoul    );
      mCBbusan  = (CheckBox) findViewById( R.id.cb_busan    );
      mCBdoskip = (CheckBox) findViewById( R.id.cb_doskip );
      mCBdospot = (CheckBox) findViewById( R.id.cb_dospot   );
      mCBdocage = (CheckBox) findViewById( R.id.cb_docage   );

      // if ( YutnoriPrefs.isSpecial() ) {
        // mCBmalsplit.setChecked( YutnoriPrefs.mSplitGroup );
        mCBseoul.setChecked( YutnoriPrefs.isSeoul() );
        mCBbusan.setChecked( YutnoriPrefs.isBusan() );
        mCBbackdo.setChecked( YutnoriPrefs.isDoNone() );
        mCBdoskip.setChecked( YutnoriPrefs.isDoSkip() );
        mCBdospot.setChecked( YutnoriPrefs.isDoSpot() );
        mCBdocage.setChecked( YutnoriPrefs.isDoCage() );

        mCBseoul.setOnClickListener( this );
        mCBbusan.setOnClickListener( this );
        mCBbackdo.setOnClickListener( this );
        mCBdoskip.setOnClickListener( this );
        mCBdospot.setOnClickListener( this );
        mCBdocage.setOnClickListener( this );
      // } else {
      //   findViewById( R.id.special_rules ).setVisibility( View.GONE );
      //   findViewById( R.id.backdo_rules  ).setVisibility( View.GONE );
      // }
    // }

    mBTagain = (Button) findViewById( R.id.btn_again );
    mBTquit  = (Button) findViewById( R.id.btn_quit );
    mBTagain.setOnClickListener( this );
    mBTquit.setOnClickListener( this );

    TextView tv = (TextView) findViewById( R.id.message );
    tv.setText( mTitle );

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
      case R.id.btn_quit:
        dismiss();
        mApp.askExit();
        break;
      case R.id.btn_again:
        // if ( ! mApp.mConnected ) { 
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
        dismiss();
        mApp.doNewGame();
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

}

