/** @file YutnoriAlertDialog.java
 * 
 * @author marco corvi
 * @date dec 2015
 * 
 * @brief board dialogs
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.yutnori;

import android.content.DialogInterface;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.Resources;

import android.widget.TextView;

class YutnoriAlertDialog 
{
  private AlertDialog mAlert = null;

  YutnoriAlertDialog( Context context, Resources res, String title, 
                      String ok, 
                      String no,
                      DialogInterface.OnClickListener ok_handler,
                      DialogInterface.OnClickListener no_handler )
  {
      // NEED API LEVEL 11 for custom background color

      AlertDialog.Builder alert_builder = new AlertDialog.Builder( context );

      alert_builder.setMessage( title );

      if ( ok != null ) {
        if ( ok_handler == null ) {
          ok_handler = new DialogInterface.OnClickListener() {
            @Override public void onClick( DialogInterface dialog, int btn ) { }
          };
        } 
        alert_builder.setNegativeButton( ok, ok_handler );
      }

      if ( no != null ) {
        if ( no_handler == null ) {
          no_handler = new DialogInterface.OnClickListener() {
            @Override public void onClick( DialogInterface dialog, int btn ) { }
          };
        } 
        alert_builder.setPositiveButton( no, no_handler );
      }

      mAlert = alert_builder.create();
      // alert.getWindow().setBackgroundDrawableResource( R.color.background );
      mAlert.show();
  }

  void dismiss() 
  {
    if ( mAlert != null ) mAlert.dismiss();
    mAlert = null;
  }
}
