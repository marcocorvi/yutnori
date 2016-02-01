/* @file ManualDialog.java
 *
 * @author marco corvi
 * @date nov 2015
 *
 * @brief Yutnori help
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.yutnori;

import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
// import android.content.Context;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.net.Uri;

import android.widget.Toast;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.util.Log;

public class ManualDialog extends Activity
{
  private WebView mTVtext;

  private void load( String filename )
  {
    StringBuilder html = new StringBuilder();
    mTVtext.loadUrl("file:///android_asset/" + filename );
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.manual_dialog);
    mTVtext   = (WebView) findViewById(R.id.manual_text );

    mTVtext.setWebViewClient( new WebViewClient() {
      @Override 
      public boolean shouldOverrideUrlLoading( WebView view, String url ) {
        view.loadUrl( url );
        return false;
      }
    } );
    // WebSettings ws = mTVtext.getSettings();
    mTVtext.getSettings().setJavaScriptEnabled( false ); // no JS
    mTVtext.getSettings().setSupportZoom( true ); 

    setTitle( getResources().getString( R.string.app_help ) );
    String locale = Locale.getDefault().getLanguage();
    if ( "ko".equals( locale ) ) {
      load( "help-ko.htm" );
    } else if ( "it".equals( locale ) ) {
      load( "help-it.htm" );
    } else { 
      load( "help.htm" );
    }
  }


  @Override
  public void onBackPressed()
  {
    String url = mTVtext.getUrl();
    if ( url.indexOf("#") < 0 ) finish();
    mTVtext.goBack();
  }

}


