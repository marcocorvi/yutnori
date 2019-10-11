/** @file Yutnori.cpp
 *
 * @author marco corvi
 * @date dec 2010
 *
 * @brief Yut Nori game
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

#include <stdio.h>
#include <unistd.h>

#include "Delay.h"
#include "Yutnori.h"

Yutnori::Yutnori( Board & b, int other )
  : board( b )
  , strategy( NULL )
  // , strategy( board, other )  // other
  , player( -other )          // my player
  , n_moves( 0 )
  , drawer(  NULL ) 
{ }


bool 
Yutnori::CanMove( int from, int to, int m )
{
  int pos[2];
  if ( to <= 1 ) to = 32;
  if ( from > 1 && from < 30 && board[from]*player<=0 ) return false; 
  board.nextPositions( from, m, pos );
  return ( pos[0] == to || pos[1] == to );
}

int 
Yutnori::Play( )
{
  int winner = board.Winner();
  while ( winner == 0 ) {
    PlayOnce( false );
    winner = board.Winner();
  }
  return winner;
}

void
Yutnori::PlayOnce( bool doze )
{
  int winner = board.Winner();
  bool again = true;
  n_moves = 0;
  do {
    if ( again ) {
      again = false;
      do {
        moves[n_moves] = dice.roll();
        if ( drawer ) drawer->drawMove( moves[n_moves], n_moves );
        n_moves ++;
        if ( doze ) sleep( SLEEP_TIME );
      } while ( moves[n_moves-1] > 3 );
    }
    // int max = n_moves;
    printf("moves: ");
    for (int k=0; k<n_moves; ++k) printf(" %d", moves[k] );
    printf("\n");

    again = strategy->move( moves, n_moves );
    // board.print();
    // if ( drawer ) {
    //   drawer->drawMoves( moves, n_moves, max );
    //   drawer->drawYutnori( board );
    // }
    if ( doze ) sleep( SLEEP_TIME );
    winner = board.Winner();
    // printf("Winner %d again %d \n", winner, again );
  } while ( winner == 0 && (again || n_moves > 0) );
}


int 
Yutnori::main( bool do_sleep, bool do_getchar )
{
  Dice dice;
  Board board;
  Strategy1 s1( board, +1 );
  Strategy1 s2( board, -1 );
  int moves[20];
  int n_moves;
  
  board.print();
  int winner = 0;
  bool again = false;
  while ( winner == 0 ) {
    // printf("Player %d Moves: ", s1.Player() );
    do {
      n_moves = 0;
      do {
        moves[n_moves] = dice.roll();
        // printf(" %d", moves[n_moves] );
        n_moves ++;
      } while ( moves[n_moves-1] > 3 ); 
      // printf("\n");
      again = s1.move( moves, n_moves );
      board.print();
      winner = board.Winner();
      if ( winner != 0 ) return winner;
    } while (again);
    int ch;
    if ( do_sleep)  sleep(2 * SLEEP_TIME);
    if ( do_getchar ) ch = getchar();

    do {
      n_moves = 0;
      // printf("PLayer %d Moves: ", s2.Player() );
      do {
        moves[n_moves] = dice.roll();
        // printf(" %d", moves[n_moves] );
        n_moves ++;
      } while ( moves[n_moves-1] > 3 ); 
      // printf("\n");
      again = s2.move( moves, n_moves );
      board.print();
      winner = board.Winner();
      if ( winner != 0 ) return winner;
    } while ( again );
    if ( do_sleep)  sleep(2 * SLEEP_TIME);
    if ( do_getchar ) ch = getchar();
  }
  return 0;
}

#ifdef MAIN

int main( int argc, char ** argv )
{
  bool do_sleep = false;
  bool do_getchar = false;
  int k = 1;
  while ( k < argc ) {
    if ( strcmp( argv[k], "-s") == 0 ) {
      do_sleep = true;
    } else if ( strcmp( argv[k], "-c") == 0 ) {
      do_getchar = true;
    } else if ( strcmp( argv[k], "-h") == 0 ) {
      printf("Usage: %s [options]\n", argv[0] );
      printf("options: -s do sleep\n");
      printf("         -c do get char \n");
      printf("         -h this help\n");
    }
    ++k;
  }  
  int winner = Yutnori::main( do_sleep, do_getchar );
  printf("The winner is player %d\n", winner );
  return 0;
}

#endif   

