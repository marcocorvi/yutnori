/***
 * Excerpted from "Hello, Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
***/
package com.yutnori;

import android.view.MotionEvent;

public class MotionEventEclair extends MotionEventWrap
{
   protected MotionEventEclair(MotionEvent event) {
      super(event);
   }
   public float getX(int pointerIndex) {
      return event.getX(pointerIndex);
   }
   public float getY(int pointerIndex) {
      return event.getY(pointerIndex);
   }
   public int getPointerCount() {
      return event.getPointerCount();
   }
   public int getPointerId(int pointerIndex) {
      return event.getPointerId(pointerIndex);
   }
}
