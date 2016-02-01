/** @file AboutDialog.java
 *
 * @author marco corvi
 * @date jan 2016
 *
 * @brief Yutnori about dialog
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.yutnori;

import android.content.Context;

import android.app.Dialog;
// import android.widget.Button;
import android.view.ViewGroup.LayoutParams;


class AboutDialog extends Dialog
                     // implements OnClickListener
{
  // private Button mBTok;
  private Context mContext;

  AboutDialog( Context context, String version, int version_code )
  {
    super( context );
    mContext = context;
    setContentView(R.layout.about_dialog );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    setTitle( mContext.getResources().getString(R.string.app_name) + " " +  version );

  }
  
}
