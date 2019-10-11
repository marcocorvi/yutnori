CC = g++

CFLAGS = -g -O0 -Wall

OBJS = \
  Weight.o \
  Indices.o \
  Probability.o \
  Board.o \
  Strategy.o \
  Strategy1.o \
  Strategy2.o

XOBJS = \
  GuiDrawer.o \
  

XLIBS = \
  -L/usr/X11R6/lib -lX11

default: xyutnori

%.o:%.cpp
	$(CC) $(CFLAGS) -o $@ -c $^

yutnori: Yutnori.cpp $(OBJS)
	$(CC) $(CFLAGS) -DMAIN -o $@ $^

xyutnori: gui.cpp Yutnori.o $(OBJS) $(XOBJS)
	$(CC) $(CFLAGS) -o $@ $^ $(XLIBS)

clean:
	rm *.o

