package com.yutnori;

interface ISender
{
  void sendMyMove( int k, int from, int to, int pawns );

  void sendMySkip( boolean clear );

}
