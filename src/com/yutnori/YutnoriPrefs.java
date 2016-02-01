/** @file YutnoriPrefs.java
 * 
 * @author marco corvi
 * @date dec 2010
 *
 * @brief Yut Nori game presferences
 *
 * ----------------------------------------------------------
 *  Copyright(c) 2005 marco corvi
 *
 *  sudoku is free software.
 *
 *  You can redistribute it and/or modify it under the terms of 
 *  the GNU General Public License as published by the Free 
 *  Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public
 *  License along with this program; if not, write to the Free
 *  Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *  MA  02111-1307  USA
 *
 * ----------------------------------------------------------
 */
package com.yutnori;

import android.content.Context;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.util.Log;

class YutnoriPrefs
{
  static final String TAG = "yutnori";

  final static String DISCLOSED = "YUTNORI_DISCLOSED";
  final static String SPLIT  = "YUTNORI_SPLIT";
  final static String LEVEL  = "YUTNORI_LEVEL";
  final static String ENGINE = "YUTNORI_ENGINE";
  final static String DELAY  = "YUTNORI_DELAY";

  final static int POS_NO      = 0;
  final static int POS_PARTIAL = 1;
  final static int POS_TOTAL   = 2;

  final static int ENGINE_0 = 0;
  final static int ENGINE_1 = 1;
  final static int ENGINE_2 = 2;

  static boolean mDisclosed  = false;
  static boolean mSplitGroup = false;
  static int     mPos    = POS_NO;
  static boolean doPos   = true;
  static int     mEngine     = ENGINE_0;
  static int     mDelayIndex = 2;

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
    if ( e >= ENGINE_0 && e <= ENGINE_2 ) {
      mEngine = e;
    }
  }

  static void setDisclosed( SharedPreferences prefs )
  {
    if ( prefs == null ) return;
    Editor editor = prefs.edit();
    editor.putBoolean( "YUTNORI_DISCLOSED", true ); 
    editor.commit();
    mDisclosed = true;
  }

  static void load( SharedPreferences prefs )
  {
    mDisclosed  = prefs.getBoolean( DISCLOSED, false );
    mSplitGroup = prefs.getBoolean( SPLIT, false );
    setPos( Integer.parseInt( prefs.getString( LEVEL, "0" ) ) );
    setEngine( Integer.parseInt( prefs.getString( ENGINE, "1" ) ) );
    setDelay( Integer.parseInt( prefs.getString( DELAY, "2" ) ) );
  }

  static void check( SharedPreferences prefs, String k, Main app )
  {
    if ( k.equals( SPLIT ) ) {
      mSplitGroup = prefs.getBoolean( SPLIT, false );
    } else if ( k.equals( LEVEL ) ) {
      setPos( Integer.parseInt( prefs.getString( LEVEL, "0" ) ) );
    } else if ( k.equals( ENGINE ) ) {
      setEngine( Integer.parseInt( prefs.getString( ENGINE, "0" ) ) );
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
