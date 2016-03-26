##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from java.awt import Color


def lerp(x, y, t):
	return x +  ( y - x ) * t


def lerpColour(x, y, t):
	r = x.getRed()  +  float( y.getRed() - x.getRed() )  *  t
	g = x.getGreen()  +  float( y.getGreen() - x.getGreen() )  *  t
	b = x.getBlue()  +  float( y.getBlue() - x.getBlue() )  *  t
	a = x.getAlpha()  +  float( y.getAlpha() - x.getAlpha() )  *  t
	return Color( int(r), int(g), int(b), int(a) )





