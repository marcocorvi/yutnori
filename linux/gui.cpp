/** @file gui.cpp
 *
 * @author marco corvi <marco_corvi@geocities.com>
 * @date dec 2005
 *
 * @brief Graphical user interface and main logic of the sudoku game
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
#include <unistd.h>
#include <signal.h>

#include "Delay.h"
#include "Board.h"
#include "Yutnori.h"
#include "GuiDrawer.h"
#include "gui.h"

/** uncomment to disable double-click
 */
#define USE_RIGHT_CLICK
#define USE_ACTIVE

/** Directory with the xpm files (can be overridden in the Makefile)
*/
#ifndef PIXMAPS
#define PIXMAPS  "./xpm"
#endif

#ifndef BGCOLOR
// #define BGCOLOR  "white" // "sea green"
#define BGCOLOR "rgb:8b/00/00"
#endif

#ifndef FGCOLOR
#define FGCOLOR  "black"
#endif

#define FONT_NAME "-adobe-helvetica-bold-r-normal-*-24-240-75-75-p-138-iso8859-1"

/* ============== Global X-window stuff ==================== */
Display * theDisplay;       /* the graphic display*/
GC        theGC;            /* the graphic context*/
int       theDepth;         /* the screen depth (15 bit planes ???)*/
Window    rootW;            /* the root window*/
Window    theWindow;        /* the window for the game */
Visual  * theVisual;
Colormap  theCmap;          /* the default colormap*/
XSetWindowAttributes xswa;
XColor    theBlack;
XColor    theWhite;
#ifdef USE_ACTIVE
XColor    theRed;
#endif

#define CLICK_NUMBER 0
#define CLICK_MENU   1
#define CLICK_EXPOSE 2
#define CLICK_RIGHT  4

#define CARD_NUMBER 9
#define YUT_NUMBER 6

// TODO make a layout struct
int boxW = 0;     //!< box width
int boxH = 0;     //!< box height
int boxMX = 0;
int boxMY = 0;

int players = 0;  //!< 0: play aginst the computer
                  //!< 1 or 2 the current player
#define NR_STRATEGIES 2
Strategy * strategies[NR_STRATEGIES];
int nr_strategy = 1;

Strategy * toggleStrategy( )
{
  nr_strategy = ( nr_strategy + 1) % NR_STRATEGIES;
  return strategies[ nr_strategy ];
}

/* The color structure: a forward linked list */
myColor * rootColor;

myColor * background = NULL;
myColor * foreground = NULL;

/* The simbols: display information */
struct cmap mscard[CARD_NUMBER];
struct cmap mzcard[CARD_NUMBER];
struct cmap mScard[CARD_NUMBER];
struct cmap mZcard[CARD_NUMBER];
struct cmap myyut[6];
struct cmap mymenu[nMenu];
struct cmap arrow;
struct cmap home;
struct cmap start;
void drawArrow();

const char * help_cmd = "/usr/bin/xterm";
const char * help_args[] = {
  "xterm", 
  "-T", "Yutnori Help", 
  "-e", "/usr/bin/less", "help",
  0
};

int my_moves[20];
int nr_my_moves = 0;
int to_throw = 1;
int has_move_from = -1;

// =================================================================

/* The menu items: */
// struct cmenu  guiMenu[nMenu];

/* ------------------------------------------------------------------ */ 
int ringBell() {
  XBell( theDisplay, 50);
  return 0;
}

extern int check();
void checkLimits()
{
#if 0
  XEvent event;
  int x, y;
  if ( check() != 1 ) {
    int ask = -1;
    int xoff = HOF_XOFF;
    int yoff = HOF_YOFF;
    const char * line1 = "******************************************";
    const char * line2 = "  You played too much today,            ";
    const char * line3 = "  take a rest and come back another day.";
    const char * line0 = "  Continue.                        Exit.";
    //exit(0);
    XSetForeground( theDisplay, theGC, theBlack.pixel );
    XSetBackground( theDisplay, theGC, theBlack.pixel );
    XFillRectangle( theDisplay, theWindow, theGC, 0, 0, HOF_WIDTH, HOF_HEIGHT );
    XSetForeground( theDisplay, theGC, theWhite.pixel );
    XDrawImageString(theDisplay, theWindow, theGC, xoff, yoff, line1, strlen(line1) ); yoff+=20;
    XDrawImageString(theDisplay, theWindow, theGC, xoff, yoff, line2, strlen(line2) ); yoff+=20;
    XDrawImageString(theDisplay, theWindow, theGC, xoff, yoff, line3, strlen(line3) ); yoff+=20;
    XDrawImageString(theDisplay, theWindow, theGC, xoff, yoff, line1, strlen(line1) ); yoff+=40;
    XDrawImageString(theDisplay, theWindow, theGC, xoff, yoff, line0, strlen(line0) );
    while ( ask == -1) {
      XNextEvent(theDisplay, &event);
      // fprintf(stderr, "event-1 type %d\n", event1.type );
      switch ( event.type ) {
        case Expose:
          // TODO
          break;
        case ButtonPress:   /*ButtonPress:4, ButtonRelease:5*/
          x = event.xbutton.x;
          y = event.xbutton.y;
          // fprintf(stderr, "x %d y %d \n", x, y);
          if ( y > yoff-20 && y < yoff ) {
            if ( x > 51 && x < 185 ) ask = 0;
            else if ( x > 260 && x < 310 ) ask = 1;
          }
          break;
      }
    }    
    if ( ask == 1 ) exit(0);
  }
#endif
}

// ================================================================== 
// server thread routine and main
// ------------------------------------------------------------------
#include <unistd.h>


/* ================================================================== */
#define OFF0  100
#define ROW0    0
#define ROW1   71
#define ROW2  148
#define ROW3  222
#define ROW4  296
#define ROW5  370
#define ROW6  450
#define XOW0    6
#define XOW1   66
#define XOW2  126
#define XOW3  186
#define XOW4  246
#define XOW5  306
#define XOW6  381

/* ------------------------------------------------------------------ */ 
/** initialize the board layout
 */
int initLayout( )
{
  int i;
  menu_yoffset = MYZERO;
  xoffsetm[0] = OFFX;
  for (i=1; i<nMenu; i++) {
    xoffsetm[i] = xoffsetm[i-1] + boxMX + GAPX;
  }
  yoffset0 =  menu_yoffset + MYZERO + boxMY + GAPY + 100;

  board_yoffset = yoffset0 + ROW0;
  // printf("board_yoffset %d \n", board_yoffset );

  xoffset[0] = OFFX +  OFF0 + ROW6;
  yoffset[0] = yoffset0 + ROW4;
  xoffset[1] = OFFX +  OFF0 + ROW6;
  yoffset[1] = yoffset0 + ROW5;

  xoffset[ 2] = OFFX + OFF0 + ROW5;
  yoffset[ 2] = yoffset0 + ROW4;
  xoffset[ 3] = OFFX + OFF0 + ROW5;
  yoffset[ 3] = yoffset0 + ROW3;
  xoffset[ 4] = OFFX + OFF0 + ROW5;
  yoffset[ 4] = yoffset0 + ROW2;
  xoffset[ 5] = OFFX + OFF0 + ROW5;
  yoffset[ 5] = yoffset0 + ROW1;
  xoffset[ 6] = OFFX + OFF0 + ROW5;
  yoffset[ 6] = yoffset0 + ROW0;

  xoffset[ 7] = OFFX + OFF0 + ROW4;
  yoffset[ 7] = yoffset0 + ROW0;
  xoffset[ 8] = OFFX + OFF0 + ROW3;
  yoffset[ 8] = yoffset0 + ROW0;
  xoffset[ 9] = OFFX + OFF0 + ROW2;
  yoffset[ 9] = yoffset0 + ROW0;
  xoffset[10] = OFFX + OFF0 + ROW1;
  yoffset[10] = yoffset0 + ROW0;
  xoffset[11] = OFFX + OFF0 + ROW0;
  yoffset[11] = yoffset0 + ROW0;

  xoffset[12] = OFFX + OFF0 + ROW0;
  yoffset[12] = yoffset0 + ROW1;
  xoffset[13] = OFFX + OFF0 + ROW0;
  yoffset[13] = yoffset0 + ROW2;
  xoffset[14] = OFFX + OFF0 + ROW0;
  yoffset[14] = yoffset0 + ROW3;
  xoffset[15] = OFFX + OFF0 + ROW0;
  yoffset[15] = yoffset0 + ROW4;
  xoffset[16] = OFFX + OFF0 + ROW0;
  yoffset[16] = yoffset0 + ROW5;

  xoffset[17] = OFFX + OFF0 + ROW1;
  yoffset[17] = yoffset0 + ROW5;
  xoffset[18] = OFFX + OFF0 + ROW2;
  yoffset[18] = yoffset0 + ROW5;
  xoffset[19] = OFFX + OFF0 + ROW3;
  yoffset[19] = yoffset0 + ROW5;
  xoffset[20] = OFFX + OFF0 + ROW4;
  yoffset[20] = yoffset0 + ROW5;
  xoffset[21] = OFFX + OFF0 + ROW5;
  yoffset[21] = yoffset0 + ROW5;

  xoffset[27] = OFFX + OFF0 + XOW5;
  yoffset[27] = yoffset0 + XOW1;
  xoffset[28] = OFFX + OFF0 + XOW4;
  yoffset[28] = yoffset0 + XOW2;
  xoffset[29] = OFFX + OFF0 + XOW3;
  yoffset[29] = yoffset0 + XOW3;
  xoffset[30] = OFFX + OFF0 + XOW2;
  yoffset[30] = yoffset0 + XOW4;
  xoffset[31] = OFFX + OFF0 + XOW1;
  yoffset[31] = yoffset0 + XOW5;

  xoffset[26] = OFFX + OFF0 + XOW5;
  yoffset[26] = yoffset0 + XOW5;
  xoffset[25] = OFFX + OFF0 + XOW4;
  yoffset[25] = yoffset0 + XOW4;
  xoffset[24] = OFFX + OFF0 + XOW3;
  yoffset[24] = yoffset0 + XOW3;
  xoffset[23] = OFFX + OFF0 + XOW2;
  yoffset[23] = yoffset0 + XOW2;
  xoffset[22] = OFFX + OFF0 + XOW1;
  yoffset[22] = yoffset0 + XOW1;

  xoffset[32] = OFFX;
  yoffset[32] = yoffset0 + ROW4;
  xoffset[33] = OFFX;
  yoffset[33] = yoffset0 + ROW5;

  return (0);
}
/* --------------------------------------------------------------------- */
/* The function getIndex() return the index of the element in the
 * window to which the point (x1,y1) belongs:
 *   indices from 0 to 55 denotes the card position in the game board.
 *   negative indices(-8 to -1) are used for the items of the menu bar
 *
 * @param x1      x coord of the mouse event
 * @param y1      y coord of the mouse event
*/
#define CELL_SIZE 60

int getIndex(int x1, int y1 ) 
{
  int  k;
  if ( y1 >= board_yoffset ) {   // board 
    k=0;
    if ( yoffset[k] < y1 && yoffset[k]+CELL_SIZE > y1 &&
         xoffset[k] < x1 && xoffset[k]+CELL_SIZE > x1 ) return k;
    k=1;
    if ( yoffset[k] < y1 && yoffset[k]+CELL_SIZE > y1 &&
         xoffset[k] < x1 && xoffset[k]+CELL_SIZE > x1 ) return k;
    for ( k=2; k<32; ++k ) {
      if ( k==29 ) continue;
      if ( yoffset[k] < y1 && yoffset[k]+CELL_SIZE > y1 &&
         xoffset[k] < x1 && xoffset[k]+CELL_SIZE > x1 ) return k;
    }
  } else if ( y1 > menu_yoffset && y1 < menu_yoffset+boxMY ) { // menu
    if ( x1 > xoffsetm[0] && x1 <= xoffsetm[nMenu-1]+boxMX ) {
      for (k=0; k<nMenu-2; k++) {
        if ( x1 >= xoffsetm[k] && x1 <= xoffsetm[k]+boxMX ) {
          return -(1+k);
	}
      }
    }
  } 
  // printf("getIndex() none \n");
  return -100;
}
    
  
// =====================================================================
// COLORS and IMAGES
// ---------------------------------------------------------------------

/** read a pixmap file
 * @param name   name of the file
 * @param map    struct for the data
 * @param box_width (out) pixmap width (assigned if not NULL)
 * @param box_height (out) pixmap height (assigned if not NULL)
 * @return 0 if successful, neg. on error
 */
int read_cmap(char * name, struct cmap * map, 
              int * box_width, int * box_height) 
{
  FILE * fp;
  int i, j, k, w, h, ww=0, hh=0, c=0, p, j0;
  char line[200], ch;
  int need_dims = 1;
  int need_cols = 1;

  fp = fopen(name, "r");
  if (fp == NULL) {
    fprintf(stderr, "Unable to open file %s\n", name);
    return (-1);
  }
  while( fscanf(fp, "%[^\n]s", line) != EOF ) {
    fscanf(fp, "%c", &ch);
    if ( line[0] == '/' && line[1] == '*' ) continue;
    if ( strncmp(line, "static", 6) == 0 ) continue;
    if ( need_dims ) {
      sscanf(&(line[1]), "%d%d%d%d", &w, &h, &c, &p);
      ww = w; // boxW; /* w/2 + 1;*/
      hh = h; // boxH; /* h/2 + 1;*/
      map->w = ww; map->h = hh; map->c = c;
      map->col = (char *)malloc(c*sizeof(char));
      map->color = (char **)malloc(c*sizeof(char *));
      for (j=0; j<c; j++) (map->color)[j]=(char *)malloc(10*sizeof(char));
      map->map = (unsigned char *)malloc(ww*hh*sizeof(char));
      memset( map->map, 0, ww*hh);
      map->pixel = (unsigned long *)malloc(ww*hh*sizeof(long));
      need_dims = 0;
      need_cols = c;
      continue;
    }
    if ( need_cols > 0 ) {
      need_cols --;
      sscanf(&(line[1]), "%c c #%6s", 
             &((map->col)[need_cols]), ((map->color)[need_cols]));
    } 
    if ( need_cols == 0 ) break;
  }

  for (i=0, j0=0; i<h; i++) {
    do {
      fscanf(fp, "%[^\n]s", line);
      fscanf(fp, "%c", &ch);
    } while ( line[0] == '/' && line[1] == '*' );
    for (j=1; j<=w; j++) {
      for (k=0; k<c; k++) if (map->col[k]==line[j]) break;
      if ( k == c ) {  
        fprintf(stderr, "\n color >>%c<< not found i %d j %d j0 %d\n", line[j], i, j, j0 );
        fprintf(stderr, "Line: >>%s<<\n", line );
      }
      (map->map)[j0]=k;
      j0 ++;
    }
  }

  fclose(fp);
  if ( box_width != NULL ) *box_width = ww;
  if ( box_height != NULL ) *box_height = hh;
  return(0);

} 

/** The pixmaps of the mscard[] (from index n1 to index n2) are 
 *  replaced by the pixel field of the corresponding XColor:  
 *  initially the pixmap entries contain the index of the color
 *  array (which has the RGB color name);                      
 *  the myColor structure with the same name is looked for      
 *  and the pixel of its XColor is put in the pixmap entry.      
 *
 * @param card   array of pixmap datas
 * @param n1     first index to remap
 * @param n2     last index to remap (C-like)
 */

void
colorRemap(struct cmap * card, int n1, int n2) 
{
  myColor * wkColor;
  int i, j, j0;
  char * name;
  for (i=n1; i<n2; i++) {
    j0 = card[i].w * card[i].h;
    for (j=0; j<j0; j++) {
      name = card[i].color[ card[i].map[j] ];
      for (wkColor=rootColor; wkColor!=NULL; wkColor=wkColor->next) 
        if (strncmp(name, wkColor->name, 6) == 0) break; 
      card[i].pixel[j] = wkColor->xc.pixel;
    }
  }
}

/** make the color table
 * @param card array of display icons
 * @param n1   first icon to consider
 * @param n2   one past the last icon to consider
 *
 * Finds the colors that appear in the mscard[] pixmaps
 * Considering only the card from n1 to n2 
 * The colors form a linked list of myColor structures 
 * each one with the color name (RGB format), the XColor
 */
void
colorDoTable(struct cmap * card, int n1, int n2) 
{
  myColor * wkColor;
  int i, j;
  unsigned long r,g,b;
  int notFound;
  char * name;

  // strncat(foreground.name, "00cccc", 6);
  /*
  wkColor = &foreground;
  {
      (wkColor->xc).flags = DoRed | DoGreen | DoBlue ;
      sscanf(&(name[0]), "%2lx", &r);
      sscanf(&(name[2]), "%2lx", &g);
      sscanf(&(name[4]), "%2lx", &b);
      (wkColor->xc).red   = (unsigned short)(256*r);
      (wkColor->xc).green = (unsigned short)(256*g);
      (wkColor->xc).blue  = (unsigned short)(256*b);

      if (rootColor==NULL) { wkColor->next = NULL; }
      else                 { wkColor->next = rootColor; }
      rootColor=wkColor;
  }
  */

  for (i=n1; i<n2; i++) for (j=0; j<card[i].c; j++) {
    name = card[i].color[j];
    notFound=1;
    for (wkColor=rootColor; wkColor!=NULL; wkColor=wkColor->next) {
      if (strncmp(name, wkColor->name, 6) == 0) { notFound = 0; break; }
    }
    if (notFound) {
      wkColor = (myColor *)malloc(sizeof(myColor));
      strncpy(wkColor->name, name, 6);
      if ( strncmp( name, "cc0000", 6) == 0 ) background = wkColor;

      (wkColor->xc).flags = DoRed | DoGreen | DoBlue ;
      sscanf(&(name[0]), "%2lx", &r);
      sscanf(&(name[2]), "%2lx", &g);
      sscanf(&(name[4]), "%2lx", &b);
      (wkColor->xc).red   = (unsigned short)(256*r);
      (wkColor->xc).green = (unsigned short)(256*g);
      (wkColor->xc).blue  = (unsigned short)(256*b);

      wkColor->next = rootColor;
      rootColor=wkColor;

      // printf("colorDoTable() New color %s %4ld %4ld %4ld\n", name, r, g, b);
  } }
}

// =====================================================================
// DRAWING PRIMITIVES
// ---------------------------------------------------------------------

void drawStart(  const Board & board )
{
  drawCard( -board.Start(0), 0 );
  drawCard( board.Start(1), 1 );
}

void drawHome(  const Board & board )
{
  drawCard( -board.Home(0), 32 );
  drawCard( board.Home(1), 33 );
}

/** display the sudoku board
 * @param sudoku    game struct
 */
void drawBoard( const Board & board )
{
  // board.print();
  for (int i=2; i<32; ++ i ) {
    if ( i == 29 ) continue;
    drawCard( board[i], i );
  }
  drawStart( board );
  drawHome( board );
  drawArrow( );
}

/** display the menus
 */
void drawThrowPlayMenu()
{
  // printf("drawThrowPlayMenu() to throw %d nr moves %d \n", to_throw, nr_my_moves );
  if ( to_throw > 0 ) {
    XPutImage(theDisplay, theWindow, theGC, mymenu[1].xi, 0, 0, 
          xoffsetm[1], menu_yoffset, mymenu[1].w, mymenu[1].h);
  } else if ( nr_my_moves > 0 ) {
    int k = 4;
    XPutImage(theDisplay, theWindow, theGC, mymenu[k].xi, 0, 0, 
          xoffsetm[1], menu_yoffset, mymenu[k].w, mymenu[k].h);
  } else {
    int k = 5;
    XPutImage(theDisplay, theWindow, theGC, mymenu[k].xi, 0, 0, 
          xoffsetm[1], menu_yoffset, mymenu[k].w, mymenu[k].h);
  }
  if ( players > 0 ) {
    int k = 5+players;
    XPutImage(theDisplay, theWindow, theGC, mymenu[k].xi, 0, 0, 
          xoffsetm[6], menu_yoffset, mymenu[k].w, mymenu[k].h);
  }
}

void drawMenu() 
{
  int i;
  XSetForeground(theDisplay, theGC, theWhite.pixel);
  for (i=0; i<4; i++) { // FIXME only 4 menus
    if ( i == 1 ) { // THROW, MOVE or PLAY
      drawThrowPlayMenu();
    } else {
      XPutImage(theDisplay, theWindow, theGC, mymenu[i].xi, 0, 0, 
        xoffsetm[i], menu_yoffset, mymenu[i].w, mymenu[i].h);
    }
  }
  if ( players == 0 ) {
    int k = 8 + nr_strategy;
    XPutImage(theDisplay, theWindow, theGC, mymenu[k].xi, 0, 0, 
      xoffsetm[i], menu_yoffset, mymenu[k].w, mymenu[k].h);
  }
}

/** display the window title
 * @param seed   game seed (unused)
 * @param diff   game difficulty
 * @param max    max choice depth
 */
void drawTitle( )
{
  char title[64];
  sprintf(title,"  Y U N N O R I " );
  XStoreName(theDisplay, theWindow, title);
  XFlush(theDisplay);
}

/** display a cell
 * @param ic   digit to diaply in the cell
 * @param col  column of the cell
 * @param row  row of the cell
 */
void drawCard( int b, int pos, bool use_z )
{
  // printf("drawCard(%d) at %d [%d]\n", b, pos, use_z );
  struct cmap * card = NULL;
  if ( b < 0 ) b = - b;
  else if ( b > 0 ) b += 4;
  if ( pos <= 1 || pos >= 32 ||
       pos == 6 || pos == 11 || pos == 16 || pos == 21 || pos == 24 ) {
    card = (use_z)?  &(mzcard[b]) : &(mscard[b]);
  } else {
    card = (use_z)?  &(mZcard[b]) : &(mScard[b]);
  }
  if ( card ) {
    int x, y;
    x = xoffset[pos];
    y = yoffset[pos];
    XPutImage(theDisplay, theWindow, theGC, card->xi, 0, 0, x, y, card->w, card->h);
    XFlush(theDisplay);
  }
}

void drawArrow()
{
  int x = xoffset[2] + 60;
  int y = yoffset[2];
  XPutImage( theDisplay, theWindow, theGC, arrow.xi, 0, 0, 
             x, y, arrow.w, arrow.h );
  x += 20;
  y -= 40;
  XPutImage( theDisplay, theWindow, theGC, start.xi, 0, 0, 
             x, y, start.w, start.h );
  x = 8;
  XPutImage( theDisplay, theWindow, theGC, home.xi, 0, 0, 
             x, y, home.w, home.h );
  XFlush(theDisplay);
}
void drawYut( int c, int pos )
{ 
  if ( pos >= 0 ) {
    int x, y;
    x = 20 + pos * 80;
    y = 40;
    XPutImage(theDisplay, theWindow, theGC, myyut[c].xi, 0, 0,
       x, y, myyut[c].w, myyut[c].h);
    XFlush(theDisplay);
  }
}

/** clear a cell
 * @param col  column of the cell
 * @param row  row of the cell
 */
void drawBox( int ic )
{
  if ( ic <= 1 || ic >= 32 ||
       ic == 6 || ic == 11 || ic == 16 || ic == 21 || ic == 24 ) {
    int x, y;
    x = xoffset[ic];
    y = yoffset[ic];
    XPutImage(theDisplay, theWindow, theGC, mscard[0].xi, 0, 0, 
      x, y, mscard[0].w, mscard[0].h);
    XFlush(theDisplay);
  } else if ( ic > 0 ) {
    int x, y;
    x = xoffset[ic];
    y = yoffset[ic];
    XPutImage(theDisplay, theWindow, theGC, mScard[0].xi, 0, 0, 
      x, y, mScard[0].w, mScard[0].h);
    XFlush(theDisplay);
  }
}

// =====================================================================
// GRAPHICS
// ---------------------------------------------------------------------
/** initialize the graphics
 */
void initGraphics() 
{
  // printf("initGraphics() \n");
  // int sW, sH;             //!< root window dimensions
  // Screen  * pScreen;      //!<   same as above
  // Font      theFont;         //!<   display font
  int       theScreen;       //!< the display screen
  theDisplay = XOpenDisplay(XDisplayName(NULL));
  if (theDisplay == NULL) {
    fprintf(stderr, "You need to run the program under X-window.\n\n");
    exit(1);
  }
  theScreen  = XDefaultScreen(theDisplay);
  rootW      = RootWindow(theDisplay, theScreen);
  theGC      = XDefaultGC(theDisplay, theScreen);
  theCmap    = XDefaultColormap(theDisplay, theScreen);
  theVisual  = DefaultVisual(theDisplay, theScreen);
  theDepth   = XDefaultDepth(theDisplay, theScreen);
  // printf("Visual %p Depth %d\n",  (void *)theVisual, theDepth);

  // pScreen    = XDefaultScreenOfDisplay(theDisplay);
  // sW         = XWidthOfScreen(pScreen);
  // sH         = XHeightOfScreen(pScreen);

  // Font theFont = XLoadFont( theDisplay,  FONT_NAME );
  Font theFont = XLoadFont(theDisplay, "-*-avantgarde-*");
  XSetFont(theDisplay, theGC, theFont);
}

/* ------------------------------------------------------------------ */ 
/** initialize the colors
 */
void initColors( )
{
  int status;
  myColor * wkColor;

  if (XAllocNamedColor(theDisplay, theCmap, BGCOLOR, &theBlack, &theBlack)==0) {
    fprintf(stderr, "Unable to allocate requested background color.\n");
    XAllocNamedColor(theDisplay, theCmap, "black", &theBlack, &theBlack);
  }
  if (XAllocNamedColor(theDisplay, theCmap, FGCOLOR, &theWhite, &theWhite)==0) {
    fprintf(stderr, "Unable to allocate the requested foreground color.\n");
    XAllocNamedColor(theDisplay, theCmap, "white", &theWhite, &theWhite);
  }
#ifdef USE_ACTIVE
  XAllocNamedColor(theDisplay, theCmap, "red", &theRed, &theRed);
#endif
  wkColor = rootColor;
  while (wkColor != NULL) {
    
    status = XAllocColor(theDisplay, theCmap, &(wkColor->xc) );
    /*
    if ( strcmp(wkColor->name, "cf0000") == 0) {
      status = XAllocNamedColor(theDisplay, theCmap, "yellow", 
        &(wkColor->xc), &(wkColor->xc));
    } else if ( strcmp(wkColor->name, "000000") == 0) {
      status = XAllocNamedColor(theDisplay, theCmap, "black", 
        &(wkColor->xc), &(wkColor->xc));
    } else {
      status = XAllocNamedColor(theDisplay, theCmap, "white", 
        &(wkColor->xc), &(wkColor->xc));
    }
    */

    wkColor = wkColor->next;
  }
  XSetForeground(theDisplay, theGC, theWhite.pixel);
  XSetBackground(theDisplay, theGC, theBlack.pixel);
  // XSetForeground(theDisplay, theGC, foreground.xc.pixel);
}

/* ------------------------------------------------------------------ */ 
/** initialize the window
 */
void initWindow( )
{
  unsigned long theFlag;
  int wW  = xoffset[1] + boxW + OFFX;
  int wH = yoffset[1] + boxH + OFFY;
  int wW1 = xoffsetm[nMenu-1] + boxW + OFFX;
  if ( wW1 > wW ) wW = wW1;
  XSynchronize(theDisplay, 1);
  xswa.background_pixmap = None;
  xswa.background_pixel  = theBlack.pixel;
  xswa.border_pixmap     = CopyFromParent;
  xswa.border_pixel      = theBlack.pixel;
  xswa.bit_gravity       = ForgetGravity;
  xswa.win_gravity       = NorthWestGravity;
  xswa.backing_store     = Always;
  xswa.backing_planes    = 0xffffffff;       /* default*/
  xswa.backing_pixel     = 0x00000000;       /* default*/
  xswa.save_under        = 0; // FALSE
  xswa.event_mask        = 
                           ButtonPressMask |
                           ButtonReleaseMask | 
                           ExposureMask |
                           ClientMessage |
                           // PropertyChangeMask |
                           ResizeRedirectMask |
                         0 ;
  xswa.do_not_propagate_mask = 
                           KeyPressMask |
                           KeyReleaseMask |
                           PointerMotionMask |
                           ButtonMotionMask | 
                           /* ResizeRedirectMask |*/
                         0 ;
  xswa.override_redirect = 0; // FALSE
  xswa.colormap          = theCmap;
  theFlag = 
         /* CWBackPixmap |*/
         CWBackPixel |
         /* CWBorderPixmap |*/
         /* CWBorderPixel |*/
         /* CWBitGravity |*/
         /* CWWinGravity |*/
         CWBackingStore |
         /* CWBackingPlanes |*/ 
         /* CWBackingPixel | */
         CWOverrideRedirect | 
         CWSaveUnder | 
         CWEventMask  | 
         CWDontPropagate | 
         CWColormap ;

  theWindow = XCreateWindow(theDisplay, rootW, 0, 0, wW, wH, 0, theDepth,
         InputOutput, theVisual, theFlag, &xswa);
  XMapWindow(theDisplay, theWindow);
}

/** initialize the display images
 * @param cmap  array of pixmap datas
 * @param n1    first index
 * @param n2    last index (C-like)
 */
void initImages(struct cmap * card, int n1, int n2) 
{
  int i, j, k;
  unsigned char * data;
  unsigned long tmp;
  int dstep;
  int dsize = card[0].w * card[0].h;

  /* 
  // dstep = (theDepth+7)/8;
  dstep = 4; // 24 -> 4 ???
             // 15 -> 2
             //  8 -> 1
  */
  if ( theDepth >= 24 ) {
    dstep = 4;
  } else if ( theDepth >= 15 ) {
    dstep = 2;
  } else {
    dstep = 1;
  }

  for (i=n1; i<n2; i++) {
    // this is to take into account that "depth" can be more than a char
    data = (unsigned char *)malloc(dstep * dsize * sizeof(char) );
    for (j=0; j<dsize; j++) {
      tmp = *(card[i].pixel + j);
      for ( k = 0; k<dstep; k++) {
        *(data+j*dstep + k) = (unsigned char)(tmp & 0xff);
        tmp >>= 8;
      }
    }

    card[i].xi = XCreateImage(theDisplay, theVisual, theDepth, ZPixmap,
      0, (char *)(data), card[i].w, card[i].h, 8, 0);
/*
    card[i].xi = XCreateImage(theDisplay, theVisual, theDepth, ZPixmap,
      0, (char *)(card[i].map), card[i].w, card[i].h, 8, 0);
*/
  }
}


/* =================================================================== */
/* catch a mouse press-n-release
 * @param ii1     where the mouse button has been pressed,
 * @param ii2     where the mouse button has been released
 * @param ev_time event time (msec)
 * @return
 *   - 0:  mouse click to move a card
 *   - 1:  mouse click on a menu item
 *   - 2:  activities not directly concerning the game.
 */
int 
mouseClick(int * ii1, int * ii2, unsigned long * ev_time )
{
  int button = 0;
  int i1=-1, i2=-1;
  XEvent event1, event2;   /* a couple of X-events*/
  do { 
    XNextEvent(theDisplay, &event1);
    // printf("event-1 type %d\n", event1.type );
    switch ( event1.type ) {
      case Expose:
        if (XPending( theDisplay ) <= 1) {
          if (event1.xexpose.count == 0)
            return (CLICK_EXPOSE);
        }
        break;
      case ButtonPress:   /*ButtonPress:4, ButtonRelease:5*/
        i1 = getIndex(event1.xbutton.x, event1.xbutton.y);
        do {
          XNextEvent(theDisplay, &event2); 
          // printf("event-2 type %d\n", event2.type );
        } while (event2.type != ButtonRelease);
        button = event2.xbutton.button;
        if (i1>=0) {
          i2 = getIndex(event2.xbutton.x, event2.xbutton.y);
	  // printf("mouseClick() %d %d (%d) - %d %d (%d)\n",
	  //   event1.xbutton.x, event1.xbutton.y, i1,
	  //   event2.xbutton.x, event2.xbutton.y, i2);
          *ev_time = event2.xbutton.time; 
        } else if (i1 > -10 ) {   /* MENU */
          i2 = getIndex(event2.xbutton.x, event2.xbutton.y);
          if ( i2 == i1 ) {
            *ii1 = -1;
            *ii2 = i1;
            // printf("Menu index %d \n", *ii2);
            return (CLICK_MENU);
          }
        }
        break;
      default:
        // printf("Event type %d \n", event1.type ); 
        break;
    } 
  } while (i2<0);

  *ii1 = i1;
  *ii2 = i2;
  return ( button == 3 ) ? CLICK_RIGHT : CLICK_NUMBER;
}

/* initGui must be called to initialize the structures used by the GUI
 */
int initGUI( )
{
  static int i_initGUI = 0;  //!< flag (1 if the gUI is initialized)
  int i;
  char cardName[50];
  int w, h;

  if (i_initGUI) return(-1);  /* check the flag of GUI initialized*/
  for (i=0; i<CARD_NUMBER; i++) {
    sprintf(cardName, "%s/s%1d.xpm", PIXMAPS, i);
    if ( read_cmap(cardName, &(mscard[i]), &boxW, &boxH) < 0 ) return -1;
  }
  for (i=0; i<CARD_NUMBER; i++) {
    sprintf(cardName, "%s/z%1d.xpm", PIXMAPS, i);
    if ( read_cmap(cardName, &(mzcard[i]), &boxW, &boxH) < 0 ) return -1;
  }
  for (i=0; i<CARD_NUMBER; i++) {
    sprintf(cardName, "%s/S%1d.xpm", PIXMAPS, i);
    if ( read_cmap(cardName, &(mScard[i]), &boxW, &boxH) < 0 ) return -1;
  }
  for (i=0; i<CARD_NUMBER; i++) {
    sprintf(cardName, "%s/Z%1d.xpm", PIXMAPS, i);
    if ( read_cmap(cardName, &(mZcard[i]), &boxW, &boxH) < 0 ) return -1;
  }
  for (i=0; i<YUT_NUMBER; i++) {
    sprintf(cardName, "%s/y%1d.xpm", PIXMAPS, i);
    if ( read_cmap(cardName, &(myyut[i]), &w, &h) < 0 ) return -1;
  }
  for (i=0; i<nMenu; i++) {
    sprintf(cardName, "%s/m%1d.xpm", PIXMAPS, i);
    if ( read_cmap(cardName, &(mymenu[i]), &boxMX, &boxMY) < 0 ) return -1;
  }
  sprintf(cardName, "%s/arrow.xpm", PIXMAPS );
  if ( read_cmap( cardName, &arrow, &w, &h ) < 0 ) return -1;
  sprintf(cardName, "%s/home.xpm", PIXMAPS );
  if ( read_cmap( cardName, &home, &w, &h ) < 0 ) return -1;
  sprintf(cardName, "%s/start.xpm", PIXMAPS );
  if ( read_cmap( cardName, &start, &w, &h ) < 0 ) return -1;

  initLayout();
  rootColor = NULL;
  colorDoTable(mscard, 0, CARD_NUMBER);
  colorDoTable(mzcard, 0, CARD_NUMBER);
  colorDoTable(mScard, 0, CARD_NUMBER);
  colorDoTable(mZcard, 0, CARD_NUMBER);
  colorDoTable(myyut, 0, YUT_NUMBER);
  colorDoTable(mymenu, 0, nMenu);
  colorDoTable(&arrow, 0, 1 );
  colorDoTable(&home, 0, 1 );
  colorDoTable(&start, 0, 1 );
  initGraphics();

  initColors();
  colorRemap(mscard, 0, CARD_NUMBER);
  colorRemap(mzcard, 0, CARD_NUMBER);
  colorRemap(mScard, 0, CARD_NUMBER);
  colorRemap(mZcard, 0, CARD_NUMBER);
  colorRemap(myyut, 0, YUT_NUMBER);
  colorRemap(mymenu, 0, nMenu);
  colorRemap(&arrow, 0, 1 );
  colorRemap(&home, 0, 1 );
  colorRemap(&start, 0, 1 );

  initWindow( );
  initImages(mscard, 0, CARD_NUMBER);
  initImages(mzcard, 0, CARD_NUMBER);
  initImages(mScard, 0, CARD_NUMBER);
  initImages(mZcard, 0, CARD_NUMBER);
  initImages(myyut, 0, YUT_NUMBER);
  initImages(mymenu, 0, nMenu);
  initImages(&arrow, 0, 1 );
  initImages(&home, 0, 1 );
  initImages(&start, 0, 1 );

  drawMenu();
  i_initGUI = 1;           /* set the flag of GUI initialized*/
  return(0);
}

/* ------------------------------------------------------------------ */ 

void addMove( int move )
{
  my_moves[ nr_my_moves ] = move;
  drawYut( move, nr_my_moves );
  ++nr_my_moves;
  if ( move < 4 ) to_throw --;
}

void
computerPlays( Yutnori * yutnori )
{
  yutnori->PlayOnce( true );
}

/** 
 * @param sudoku   game structure
 * @param code     menu code
 * @return 1 if to continue, 0 if to exit
 */
int 
handleMenu( Yutnori * yutnori, int code ) 
{
  switch (code) {
    case NEW:  //new
      XClearWindow(theDisplay, theWindow);
      checkLimits();
      XClearWindow(theDisplay, theWindow);
      drawMenu();
      yutnori->Reset();
      drawBoard( yutnori->GetBoard() );
      to_throw = 1;
      has_move_from = -1;
      nr_my_moves = 0;
      return 1;
    case THROW: // 
      if ( to_throw > 0 || nr_my_moves > 0 ) {
        if ( to_throw > 0 ) {
          addMove( yutnori->Throw() );
        } else {
          ringBell();
        }
      } else { 
        if ( players == 0 ) {
          computerPlays( yutnori );
        } else {
          players = 3 - players; // switch player
        }
        to_throw = 1;
        drawThrowPlayMenu();
      }
      return 1;
    case QUIT: // quit
      return 0;
    case HELP:
      if ( fork() == 0 ) { // child
        execv( (char const *)(help_cmd), (char * const *)(help_args) );
      }
      return 1;
    case STRAT:
      if ( players == 0 ) {
        yutnori->SetStrategy( toggleStrategy() );
        drawMenu();
      }
      return 1;
    default:
      fprintf(stderr, "handleMenu() unexpected case %d \n", code);
  }
  return 1;
}

/** handle a game move
 * @param sudoku  game struct
 * @param from    cell where the mouse was pressed
 * @param index   cell where the mouse was released
 * @param when    time of the mouse click
 */
void 
handleBoard( Yutnori * yutnori, int from, int to )
{
  Board & board = yutnori->GetBoard();
  if ( nr_my_moves > 0 ) {
    int k;
    for ( k=nr_my_moves-1; k>=0; --k) {
      if ( yutnori->CanMove( from, to, my_moves[k] ) ) {
        while ( k < nr_my_moves-1 ) {
          my_moves[k] = my_moves[k+1];
          drawYut(  my_moves[k], k );
          ++ k;
        }
        -- nr_my_moves;
        drawYut( 0, k );
        if ( yutnori->Move( from, to ) ) {
          to_throw ++;
        }
        if ( from > 1 ) {
          drawCard( board[from], from );
        } 
        if ( to < 32 ) {
          drawCard( board[to], to );
        }
        drawStart( board );
        drawHome( board );
        break;
      }
    }
    if ( k < 0 ) {
      // printf("cannot move from %d to %d \n", from, to );
      // int pos[2];
      // for ( k=0; k<nr_my_moves; ++k ) {
      //   board.nextPositions( from, my_moves[k], pos );
      //   printf("move %d: next %d %d\n", my_moves[k], pos[0], pos[1] );
      // }
      drawBoard( board );
      has_move_from = -1;
      ringBell();
    }
  }
}

#define HOF_XOFF 300
#define HOF_YOFF 60
#define HOF_WIDTH 100
#define HOF_HEIGHT 60
void
drawWinner( const char * msg )
{
  XSetForeground( theDisplay, theGC, theBlack.pixel );
  XSetBackground( theDisplay, theGC, theBlack.pixel );
  XFillRectangle( theDisplay, theWindow, theGC,
      HOF_XOFF-10, HOF_YOFF-30, HOF_WIDTH, HOF_HEIGHT );
  XSetForeground( theDisplay, theGC, theWhite.pixel );

  XDrawImageString(theDisplay, theWindow, theGC,
      HOF_XOFF, HOF_YOFF, msg, strlen(msg) );
}
   

/** --------------------------------------------------------------
 * main
 * ---------------------------------------------------------------
 */

int main( int argc, char ** argv )
{
  while ( argc > 1 ) { 
    if ( argv[1][0] == '-' ) {
      switch ( argv[1][1] ) {
        case 'p': // two players
          if ( argc > 2 ) {
            argc --;
            argv ++;
            int p = atoi( argv[1] );
            if ( p >=0 && p <= 2 ) {
              players = p;
            }
          }
          break;
        case 's': // strategy
          if ( argc > 2 ) {
            argc --;
            argv ++;
            int s = atoi( argv[1] );
            if ( s >=0 && s <= 1 )
            nr_strategy = s;
          }
          break;
        case 'h':
           printf("Yut-Nori\n");
           printf("Usage: xyutnori [options] \n");
           printf("Options: -p #   number of players, either 1 (default) or 2\n");
           printf("         -s #   strategy choice, 0 or 1 (default)\n");
           printf("         -h     this help\n");
           return 0;
        default:
           break;  
      }
      argc --;
      argv ++;
    }
  }
  bool do_work = true;
  GuiDrawer drawer;
  Board board; //  = yutnori.GetBoard();
  Yutnori yutnori1( board, +1 );
  Yutnori yutnori2( board, -1 );
  if ( players == 0 ) {
    strategies[0] = new Strategy1( board, +1 );
    strategies[1] = new Strategy2( board, +1 );
    yutnori1.SetStrategy( strategies[ nr_strategy ] );
  }
  yutnori1.SetDrawer( &drawer );
  yutnori2.SetDrawer( &drawer );

  Yutnori * yutnori = &yutnori1;
  int index = 0;
/* check if you played too many times today before doing anything */

  initGUI( );
  XClearWindow(theDisplay, theWindow);

  drawTitle( );

  drawMenu();
  drawBoard( board );

  while ( do_work ) {
    int from, to;
    unsigned long when;
    int winner = board.Winner();
    // printf(" do work - winner %d home %d %d\n", winner, board.Home(0), board.Home(1) );
    if ( winner != 0 ) {
      if (players > 0 ) {
        drawWinner( (winner == -1 )? "PLAYER 2 WON" : "PLAYER 1 WON" );
      } else {
        drawWinner( (winner == -1 )? "YOU WON" : "I WON" );
      }
    }
    drawThrowPlayMenu();
    if ( players == 0 || players == 2 ) {
      yutnori = &yutnori1;
      index = 0;
    } else {
      yutnori = &yutnori2;
      index = 1;
    }
    if ( winner == 0 && to_throw == 0 && nr_my_moves == 0 ) {
      sleep( SLEEP_TIME );
      do_work = handleMenu( yutnori, THROW );
      continue;
    }
    switch ( mouseClick( &from, &to, &when ) ) {
      case CLICK_NUMBER: // board
        if ( winner != 0 ) break;
        if ( from != to ) {
          has_move_from = -1;
          drawBoard( board );
        } else if  ( has_move_from >= 0 ) {
          handleBoard( yutnori, has_move_from, from );
          has_move_from = -1;
          // drawBoard( board );
        } else {
          int b = 0;
          int pos = from;
          if ( from <= 1 ) {
            b = - board.Start( index ) * (1-2*index);
          } else {
            b = board[from];
          }  
          if ( b*(1-2*index) < 0 ) {
            drawCard( b, pos, true );
            has_move_from = from;
          } else {
            // printf("***** from %d board[from] %d\n", from, b );
          }
        }
        break;
      case CLICK_MENU: // menu
        if ( winner != 0 && to == THROW ) break;
        do_work = handleMenu( yutnori, to );
        break;
      case CLICK_EXPOSE: // repaint
        drawMenu();
        drawBoard( board );
        break;
      default:
        fprintf(stderr, "unhandled mouse click %d %d\n", from, to );
        break;
    }
  }

  if ( players == 0 ) {
    delete strategies[0];
    delete strategies[1];
  }
  return 0;
}


