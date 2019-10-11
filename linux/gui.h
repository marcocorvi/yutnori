/** @file gui.h
 *
 * @author marco corvi <marco_corvi@geocities.com>
 * @date dec 2005
 *
 * @brief Graphical User Interface (GUI) for the game "sudoku"
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
#ifndef YUTNORI_GUI_H
#define YUTNORI_GUI_H

#include <stdio.h>        // for sprintf, sscanf, fclose
#include <stdlib.h>       // for malloc, NULL
#include <string.h>       // for strcpy
#include <math.h>
#include <time.h>         // time
#include <unistd.h>       // sleep 

/* ============== X WINDOW STUFF ==================== */
#include <X11/Xlib.h>
#include <X11/Xos.h>
#include <X11/Xutil.h>
#include <X11/X.h>
#include <X11/cursorfont.h>
#include <X11/Xatom.h>

#define nMenu  10

// Menu codes (used in the switch statement)
#define NEW       -1    // new game
#define THROW     -2    // throw or play
#define HELP      -3    // help
#define QUIT      -4    // quit the game
// #define MOVE   -5
// #define WAIT   -6   // play
#define PLAY1     -7   // which player: not clickable
#define PLAY2     -8   // which player: not clickable
// #define STRAT1 -9 
// #define STRAT2 -10
#define STRAT     -5   // toggle strategy

/* ======= LAYOUT SPECIFICATIONS =================== */
#define GAPX    3      /* h. gap between the two cards in the layout*/
#define EGAP    5      /* extra gap between cards */
#define GAPY    3      /* v. gap between the two cards in the layout*/
#define OFFX    6      /* horiz. offset */
#define OFFY    5      /* vert. skip between the menu bar and the game board*/
#define MYZERO  3      /* vertical offset of the menu items*/

#define BOARD_SIZE 35

int xoffset[BOARD_SIZE];
int yoffset[BOARD_SIZE];
int yoffset0;
int menu_yoffset;
int board_yoffset;
int xoffsetm[nMenu];


/* The color structure: a forward linked list */
struct myColor {
  XColor xc;                // the XColor
  char name[6];             // the color rgb name 
  struct myColor * next;    // link to the next color
};

/* The cards: display information */
struct cmap {
  int w, h, c;              // width, height and colors number
  char * col;               // 
  char ** color;            // array of color rgb names  
  unsigned char * map;      // pixmap (xpm)
  unsigned long * pixel;    // pixels <-- NEW
  XImage * xi;              // X image
};

/* The menu items: */
struct cmenu {
  int x, y;            // item position (x horiz., y vertic.)
  char name[10];       // item label
};

/* ------------------------------------------------------------------ */ 

class Yutnori;

/* ------------------------------------------------------------------ */ 
int ringBell();
int initLayout( );

int getIndex(int x1, int y1 );
int read_cmap(char * name, struct cmap * map, int * box_width, int * box_height );

void colorRemap(struct cmap * mycard, int n1, int n2);
void colorDoTable(struct cmap * mycard, int n1, int n2);

void initGraphics();
void initColors();
void initWindow( );
void initImages(struct cmap * mycard, int n1, int n2); 

// draw
void drawBoard( Yutnori * yutnori );
void drawMenu();
void drawTitle( int seed, int diff, int max );
void drawCard( int b, int pos, bool use_z = false );

int mouseClick(int * ii1, int * ii2, unsigned long * ev_time );
int initGUI( );
/* ------------------------------------------------------------------ */ 

#endif // YUTNORI_GUI_H
