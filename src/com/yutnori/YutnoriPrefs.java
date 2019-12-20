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

import android.content.Context;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.util.Log;

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
  final static String KEY_BACKDO    = "YUTNORI_BACKDO";

  final static int POS_NO      = 0; // level
  final static int POS_PARTIAL = 1;
  final static int POS_TOTAL   = 2;

  final static int ENGINE_0 = 0; // must agree with xml/preferences.xml
  final static int ENGINE_1 = 1;
  final static int ENGINE_2 = 2;
  final static int ENGINE_RANDOM = 3;

  final static int NONE   = 0; // must agree with xml/preferences.xml
  final static int BACKDO = 1;
  final static int SEOUL  = 2;
  final static int BUSAN  = 3;

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

  static boolean isSpecial() { return mSpecialRule > 0; }

  static boolean isBackDo() { return mSpecialRule == BACKDO; }
  static boolean isSeoul()  { return mSpecialRule == SEOUL; }
  static boolean isBusan()  { return mSpecialRule == BUSAN; }
  static boolean isSeoulOrBusan() { return mSpecialRule == SEOUL || mSpecialRule == BUSAN; }

  static boolean isDoNone() { return mSpecialRule == BACKDO && mBackDo == DO_NONE; }
  static boolean isDoSkip() { return mSpecialRule == BACKDO && mBackDo == DO_SKIP; }
  static boolean isDoSpot() { return mSpecialRule == BACKDO && mBackDo == DO_SPOT; }
  static boolean isDoCage() { return mSpecialRule == BACKDO && mBackDo == DO_CAGE; }

  static void setSpecial( int rule ) 
  {
    mSpecialRule = rule;
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
    mSpecialRule  = Integer.parseInt( prefs.getString( YutnoriPrefs.KEY_SPECIAL, "0" ) );
    mBackDo       = Integer.parseInt( prefs.getString( YutnoriPrefs.KEY_BACKDO, "0" ) );
    mBackYuts     = Integer.parseInt( prefs.getString( YutnoriPrefs.KEY_BACKYUTS, "1" ) );
    setPos(    Integer.parseInt( prefs.getString( YutnoriPrefs.KEY_LEVEL, "0" ) ) );
    setEngine( Integer.parseInt( prefs.getString( YutnoriPrefs.KEY_ENGINE, "3" ) ) );
    setDelay(  Integer.parseInt( prefs.getString( YutnoriPrefs.KEY_DELAY, "2" ) ) );
  }

  static void check( SharedPreferences prefs, String k, Main app )
  {
    if ( k.equals( YutnoriPrefs.KEY_LEVEL ) ) {
      setPos( Integer.parseInt( prefs.getString( YutnoriPrefs.KEY_LEVEL, "0" ) ) );
    } else if ( k.equals( YutnoriPrefs.KEY_SPECIAL ) ) {
      mSpecialRule  = Integer.parseInt( prefs.getString( YutnoriPrefs.KEY_SPECIAL, "0" ) );
    } else if ( k.equals( YutnoriPrefs.KEY_BACKDO ) ) {
      mBackDo       = Integer.parseInt( prefs.getString( YutnoriPrefs.KEY_BACKDO, "0" ) );
    } else if ( k.equals( YutnoriPrefs.KEY_BACKYUTS ) ) {
      mBackYuts     = Integer.parseInt( prefs.getString( YutnoriPrefs.KEY_BACKYUTS, "1" ) );
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
      return names[ mBackYuts - 1 ]; // mBackYuts ranges 1, 2, 3
    } else if ( key.equals(KEY_ENGINE) ) {
      names  = res.getStringArray( R.array.engine );
      return names[ mEngine ];
    } else if ( key.equals(KEY_DELAY) ) {
      names  = res.getStringArray( R.array.delay );
      return names[ mDelayIndex ];
    } 
    return null;
  }

}
