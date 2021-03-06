/** @file YutnoriPrefs.java
 * 
 * @author marco corvi
 * @date dec 2015
 *
 * @brief Yut Nori game presferences
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.yutnori;

import android.util.Log;

import android.content.Context;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.os.Bundle;

class YutnoriPrefs
{
  // static final String TAG = "Yutnori";

  final static String KEY_DISCLOSED = "YUTNORI_DISCLOSED";
  final static String KEY_SPLIT     = "YUTNORI_SPLIT";
  final static String KEY_BACKYUTS  = "YUTNORI_BACKYUTS";
  final static String KEY_LEVEL     = "YUTNORI_LEVEL";
  final static String KEY_ENGINE    = "YUTNORI_ENGINE";
  final static String KEY_DELAY     = "YUTNORI_DELAY";
  final static String KEY_SPECIAL   = "YUTNORI_SPECIAL";
  // final static String KEY_SPECIAL_RULES   = "YUTNORI_SPECIAL_RULES";
  // final static String KEY_BACKDO    = "YUTNORI_BACKDO";

  final static int POS_NO      = 0; // level
  final static int POS_PARTIAL = 1;
  final static int POS_TOTAL   = 2;

  final static int ENGINE_0 = 0; // must agree with xml/preferences.xml
  final static int ENGINE_1 = 1;
  final static int ENGINE_2 = 2;
  final static int ENGINE_RANDOM = 3;

  final static int NONE   = 0; // must agree with xml/preferences.xml
  final static int SEOUL  = 1;
  final static int BUSAN  = 2;
  final static int BACKDO = 3;
  final static int SKIPDO = 4;
  final static int SPOTDO = 5;
  final static int CAGEDO = 6;

  final static int DO_NONE = 10;  // revert do
  final static int DO_SKIP = 11;  // skip turn
  final static int DO_SPOT = 12;  // do to cham_moeki
  final static int DO_CAGE = 13;  // do to cage

  // static boolean mDisclosed  = false;
  static int     mPos    = POS_NO;
  static boolean doPos   = true;
  static int     mEngine     = ENGINE_RANDOM;
  static int     mDelayIndex = 2;
  static boolean mTiToFreeze = false;

  static boolean mSplitGroup  = false;
  static int     mSpecialRule = NONE;
  static int     mBackDo      = DO_NONE;
  static int     mBackYuts    = 1;
  static int     mDefaultRule        = NONE;
  static int     mDefaultSpecialRule = NONE;
  static int     mDefaultBackDo      = DO_NONE;
  static int     mDefaultBackYuts = 1;
  // --------------------------------------------------------------
  static void saveState( Bundle bundle )
  {
    bundle.putShort( "YUTNORI_DELAY",    (short)mDelayIndex );
    bundle.putShort( "YUTNORI_POS",      (short)mPos );
    bundle.putShort( "YUTNORI_ENGINE",   (short)mEngine );
    bundle.putShort( "YUTNORI_SPECIAL",  (short)mSpecialRule );
    bundle.putShort( "YUTNORI_BACKDO",   (short)mBackDo );
    bundle.putShort( "YUTNORI_BACKYUTS", (short)mBackYuts );
  }

  static void restoreState( Bundle bundle )
  {
    setDelay( bundle.getShort( "YUTNORI_DELAY" ) );
    mPos         = bundle.getShort( "YUTNORI_POS" );
    mEngine      = bundle.getShort( "YUTNORI_ENGINE" );
    mSpecialRule = bundle.getShort( "YUTNORI_SPECIAL" );
    mBackDo      = bundle.getShort( "YUTNORI_BACKDO" );
    mBackYuts    = bundle.getShort( "YUTNORI_BACKYUTS" );
  }

  // --------------------------------------------------------------
  static boolean isSpecial() { return mSpecialRule > 0; }

  static boolean isBackDo() { return mSpecialRule == BACKDO; }
  static boolean isSeoul()  { return mSpecialRule == SEOUL; }
  static boolean isBusan()  { return mSpecialRule == BUSAN; }
  static boolean isSeoulOrBusan() { return mSpecialRule == SEOUL || mSpecialRule == BUSAN; }

  static boolean isDoNone() { return mSpecialRule == BACKDO && mBackDo == DO_NONE; }
  static boolean isDoSkip() { return mSpecialRule == BACKDO && mBackDo == DO_SKIP; }
  static boolean isDoSpot() { return mSpecialRule == BACKDO && mBackDo == DO_SPOT; }
  static boolean isDoCage() { return mSpecialRule == BACKDO && mBackDo == DO_CAGE; }

  static int getBackYuts() // { return mBackYuts; }
  { return ( mSpecialRule > 0 )? (isSeoulOrBusan() ? 1 : mBackYuts) : 0; } 

  static void setSpecial( int rule ) 
  {
    mSpecialRule = rule;
  }

  static void setDefaultRule( int r ) 
  {
    mDefaultRule = r;
    switch ( r ) {
      case 1:
        mDefaultSpecialRule = SEOUL;
        mDefaultBackDo = DO_NONE;
        break;
      case 2:
        mDefaultSpecialRule = BUSAN;
        mDefaultBackDo = DO_NONE;
        break;
      case 3:
        mDefaultSpecialRule = BACKDO;
        mDefaultBackDo = DO_NONE;
        break;
      case 4:
        mDefaultSpecialRule = BACKDO;
        mDefaultBackDo = DO_SKIP;
        break;
      case 5:
        mDefaultSpecialRule = BACKDO;
        mDefaultBackDo = DO_SPOT;
        break;
      case 6:
        mDefaultSpecialRule = BACKDO;
        mDefaultBackDo = DO_CAGE;
        break;
      default:
        mDefaultSpecialRule = NONE;
        mDefaultBackDo = DO_NONE;
        break;
    }
  }

  static final int[] mDelayUnits = { 500, 200, 100, 50, 20 };
  static int     mDelayUnit  = 100;

  static int getPos() { return ( doPos ? mPos : POS_NO ); }

  static void setPos( int pos ) { if ( pos >= 0 && pos <= POS_TOTAL ) mPos = pos; }

  static void setDelay( int d )
  {
    if ( d >= 0 && d < 5 ) {
      mDelayIndex = d;
      mDelayUnit = mDelayUnits[ mDelayIndex ];
    }
  }

  static void setEngine( int e )
  {
    if ( e >= ENGINE_0 && e <= ENGINE_RANDOM ) {
      mEngine = e;
    }
  }

  // static void setDisclosed( SharedPreferences prefs )
  // {
  //   if ( prefs == null ) return;
  //   Editor editor = prefs.edit();
  //   editor.putBoolean( YutnorePrefs.KEY_DISCLOSED, true ); 
  //   editor.commit();
  //   mDisclosed = true;
  // }

  static void load( SharedPreferences prefs )
  {
    // mDisclosed  = prefs.getBoolean( YutnoriPrefs.KEY_DISCLOSED, false );
    mSplitGroup   = prefs.getBoolean( YutnoriPrefs.KEY_SPLIT, false );
    // mSpecialRule  = prefs.getBoolean( YutnoriPrefs.KEY_SPECIAL_RULES, false );
    setDefaultRule( Integer.parseInt( prefs.getString( YutnoriPrefs.KEY_SPECIAL, "0" ) ) );
    mSpecialRule = mDefaultSpecialRule;
    mBackDo      = mDefaultBackDo;
    // mBackDo       = Integer.parseInt( prefs.getString( YutnoriPrefs.KEY_BACKDO, "0" ) );
    mDefaultBackYuts = Integer.parseInt( prefs.getString( YutnoriPrefs.KEY_BACKYUTS, "1" ) );
    mBackYuts = mDefaultBackYuts;
    setPos(    Integer.parseInt( prefs.getString( YutnoriPrefs.KEY_LEVEL, "0" ) ) );
    setEngine( Integer.parseInt( prefs.getString( YutnoriPrefs.KEY_ENGINE, "3" ) ) );
    setDelay(  Integer.parseInt( prefs.getString( YutnoriPrefs.KEY_DELAY, "2" ) ) );
  }

  static void checkPreference( SharedPreferences prefs, String k, Main app )
  {
    if ( k.equals( YutnoriPrefs.KEY_LEVEL ) ) {
      setPos( Integer.parseInt( prefs.getString( YutnoriPrefs.KEY_LEVEL, "0" ) ) );
    } else if ( k.equals( YutnoriPrefs.KEY_SPECIAL ) ) {
      setDefaultRule( Integer.parseInt( prefs.getString( YutnoriPrefs.KEY_SPECIAL, "0" ) ) );
    // } else if ( k.equals( YutnoriPrefs.KEY_BACKDO ) ) {
    //   mBackDo       = Integer.parseInt( prefs.getString( YutnoriPrefs.KEY_BACKDO, "0" ) );
    } else if ( k.equals( YutnoriPrefs.KEY_BACKYUTS ) ) {
      mDefaultBackYuts     = Integer.parseInt( prefs.getString( YutnoriPrefs.KEY_BACKYUTS, "1" ) );
    } else if ( k.equals( YutnoriPrefs.KEY_ENGINE ) ) {
      setEngine( Integer.parseInt( prefs.getString( YutnoriPrefs.KEY_ENGINE, "3" ) ) );
      app.setEngine( mEngine );
    } else if ( k.equals( YutnoriPrefs.KEY_DELAY ) ) {
      setDelay( Integer.parseInt( prefs.getString( YutnoriPrefs.KEY_DELAY, "2" ) ) );
    }
  }

  static String getNameFromValue( Context ctx, String key, CharSequence value )
  {
    if ( value == null || key == null ) return null;
    Resources res = ctx.getResources();
    String names[]  = null;
    String values[] = null;
    if ( key.equals(KEY_LEVEL) ) {
      names  = res.getStringArray( R.array.level );
      values = res.getStringArray( R.array.levelValue );
    } else if ( key.equals(KEY_BACKYUTS) ) {
      names  = res.getStringArray( R.array.backyuts );
      values = res.getStringArray( R.array.backyuts );
    } else if ( key.equals(KEY_ENGINE) ) {
      names  = res.getStringArray( R.array.engine );
      values = res.getStringArray( R.array.engineValue );
    } else if ( key.equals(KEY_DELAY) ) {
      names  = res.getStringArray( R.array.delay );
      values = res.getStringArray( R.array.delayValue );
    } else if ( key.equals( YutnoriPrefs.KEY_SPECIAL ) ) {
      names  = res.getStringArray( R.array.rule );
      values = res.getStringArray( R.array.ruleValue );
    } 
    if ( names != null && values != null ) {
      for (int k=0; k<names.length; ++k ) if ( values[k].equals( value ) ) return names[k];
    }
    return null;
  }

  static String getNameFromCurrent( Context ctx, String key )
  {
    if ( key == null ) return null;
    Resources res = ctx.getResources();
    String names[]  = null;
    String values[] = null;
    if ( key.equals(KEY_LEVEL) ) {
      names  = res.getStringArray( R.array.level );
      return names[ mPos ];
    } else if ( key.equals(KEY_BACKYUTS) ) {
      names  = res.getStringArray( R.array.backyuts );
      return names[ mDefaultBackYuts - 1 ]; // mDefaultBackYuts ranges 1, 2, 3
    } else if ( key.equals(KEY_ENGINE) ) {
      names  = res.getStringArray( R.array.engine );
      return names[ mEngine ];
    } else if ( key.equals(KEY_DELAY) ) {
      names  = res.getStringArray( R.array.delay );
      return names[ mDelayIndex ];
    } else if ( key.equals( YutnoriPrefs.KEY_SPECIAL ) ) {
      names  = res.getStringArray( R.array.rule );
      return names[ mDefaultRule ];
    } 
    return null;
  }

}
