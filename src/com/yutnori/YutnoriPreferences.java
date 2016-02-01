/* @file YutnoriPreferences.java
 *
 * @author marco corvi
 * @date jul 2014
 *
 * @brief Yutnori options dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.yutnori;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;

/**
 */
public class YutnoriPreferences extends PreferenceActivity 
{

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    addPreferencesFromResource(R.xml.preferences);
  }

}

