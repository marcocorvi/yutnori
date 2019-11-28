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

  final static String DISCLOSED = "YUTNORI_DISCLOSED";
  final static String SPLIT     = "YUTNORI_SPLIT";
  final static String TITO      = "YUTNORI_TITO";
  final static String TITO_SKIP = "YUTNORI_TITO_SKIP";
  final static String BACKYUTS  = "YUTNORI_BACKYUTS";
  final static String LEVEL     = "YUTNORI_LEVEL";
  final static String ENGINE    = "YUTNORI_ENGINE";
  final static String DELAY     = "YUTNORI_DELAY";
  final static String SPECIAL   = "YUTNORI_SPECIAL_RULES";

  final static int POS_NO      = 0; // level
  final static int POS_PARTIAL = 1;
  final static int POS_TOTAL   = 2;

  final static int ENGINE_0 = 0;
  final static int ENGINE_1 = 1;
  final static int ENGINE_2 = 2;
  final static int ENGINE_RANDOM = 3;

  // static boolean mDisclosed  = false;
  static int     mPos    = POS_NO;
  static boolean doPos   = true;
  static int     mEngine     = ENGINE_RANDOM;
  static int     mDelayIndex = 2;

  static boolean mSpecialRules = false;
  static boolean mSplitGroup = false;

  static boolean mTiTo       = false; // 0: no TiTo, 1: one-step TiTo, 2: all count -1, 3: any counts its value
  static boolean mTiToSkip   = false; // whether TiTo with empty board is skip: 0 no skip, 1: one skip, 2 count skips
  static boolean mTiToFreeze = false;
  static boolean mDoSpot     = false;
  static boolean mSeoul      = false;
  static boolean mBusan      = false;
  static int     mBackYuts   = 1;

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
  //   editor.putBoolean( "YUTNORI_DISCLOSED", true ); 
  //   editor.commit();
  //   mDisclosed = true;
  // }

  static void load( SharedPreferences prefs )
  {
    // mDisclosed  = prefs.getBoolean( DISCLOSED, false );
    mSplitGroup = prefs.getBoolean( SPLIT, false );
    mSpecialRules  = prefs.getBoolean( SPECIAL, false );
    // mTiTo       = prefs.getBoolean( TITO, false ) );
    // mTiToSkip   = prefs.getBoolean( TITO_SKIP, false ) );
    // mDoSpot     = prefs.getBoolean( DO_SPOT, false ) );
    mBackYuts   = Integer.parseInt( prefs.getString( BACKYUTS, "1" ) );
    setPos( Integer.parseInt( prefs.getString( LEVEL, "0" ) ) );
    setEngine( Integer.parseInt( prefs.getString( ENGINE, "3" ) ) );
    setDelay( Integer.parseInt( prefs.getString( DELAY, "2" ) ) );
  }

  static void check( SharedPreferences prefs, String k, Main app )
  {
    // if ( k.equals( SPLIT ) ) {
    //   mSplitGroup = prefs.getBoolean( SPLIT, false );
    // } else if ( k.equals( TITO ) && ! mTiToFreeze ) {
    //   mTiTo = prefs.getBoolean( TITO, false ) );
    // } else if ( k.equals( TITO_SKIP ) && ! mTiToFreeze ) {
    //   mTiToSkip = prefs.getBoolean( TITO_SKIP, false ) );
    // } else if ( k.equals( DO_SPOT ) && ! mTiToFreeze ) {
    //   mDoSpot = prefs.getBoolean( DO_SPOT, false ) );
    // } else 
    if ( k.equals( LEVEL ) ) {
      setPos( Integer.parseInt( prefs.getString( LEVEL, "0" ) ) );
    } else if ( k.equals( SPECIAL ) ) {
      mSpecialRules  = prefs.getBoolean( SPECIAL, false );
    } else if ( k.equals( BACKYUTS ) ) {
      mBackYuts   = Integer.parseInt( prefs.getString( BACKYUTS, "1" ) );
    } else if ( k.equals( ENGINE ) ) {
      setEngine( Integer.parseInt( prefs.getString( ENGINE, "3" ) ) );
      app.setEngine( mEngine );
    } else if ( k.equals( DELAY ) ) {
      setDelay( Integer.parseInt( prefs.getString( DELAY, "2" ) ) );
    }
  }

  static String getNameFromValue( Context ctx, String key, CharSequence value )
  {
    if ( value == null || key == null ) return null;
    Resources res = ctx.getResources();
    String names[]  = null;
    String values[] = null;
    if ( key.equals(LEVEL) ) {
      names  = res.getStringArray( R.array.level );
      values = res.getStringArray( R.array.levelValue );
    } else if ( key.equals(BACKYUTS) ) {
      names  = res.getStringArray( R.array.backyuts );
      values = res.getStringArray( R.array.backyuts );
    } else if ( key.equals(ENGINE) ) {
      names  = res.getStringArray( R.array.engine );
      values = res.getStringArray( R.array.engineValue );
    } else if ( key.equals(DELAY) ) {
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
    if ( key.equals(LEVEL) ) {
      names  = res.getStringArray( R.array.level );
      return names[ mPos ];
    } else if ( key.equals(BACKYUTS) ) {
      names  = res.getStringArray( R.array.backyuts );
      return names[ mBackYuts - 1 ]; // mBackYuts ranges 1, 2, 3
    } else if ( key.equals(ENGINE) ) {
      names  = res.getStringArray( R.array.engine );
      return names[ mEngine ];
    } else if ( key.equals(DELAY) ) {
      names  = res.getStringArray( R.array.delay );
      return names[ mDelayIndex ];
    } 
    return null;
  }

}
