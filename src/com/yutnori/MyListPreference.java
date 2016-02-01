/* @file MyListPreferences.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief Yutnori option list
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.yutnori;

import android.content.Context;
import android.preference.Preference;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.preference.Preference.OnPreferenceChangeListener;

/**
 */
public class MyListPreference extends ListPreference
{
  Context mContext;
  CharSequence mSummary;

  public MyListPreference( Context c, AttributeSet a ) 
  {
    super(c,a);
    mContext = c;
    init();
  }

  public MyListPreference( Context c )
  {
    super( c );
    mContext = c;
    init();
  }

  private void init()
  {
    mSummary = getSummary();
    setOnPreferenceChangeListener( new OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange( Preference p, Object v ) // called before the state is changed
      {
        String value = YutnoriPrefs.getNameFromValue( mContext, p.getKey(), (String)v );
        if ( value != null ) {
          p.setSummary( mSummary + " " + value );
        } else {
          p.setSummary( mSummary );
        }
        return true;
      }
    } );
    
    String value = YutnoriPrefs.getNameFromCurrent( mContext, getKey() );
    if ( value != null ) {
      setSummary( mSummary + " " + value );
    } else {
      setSummary( mSummary );
    }
  }

  void updateSummary()
  {
    CharSequence entry = getEntry();
    if ( entry != null ) {
      setSummary( mSummary + " " + entry );
    } else {
      setSummary( mSummary );
    }
  }

  // @Override
  // public CharSequence getSummary() { return super.getEntry(); }
}

