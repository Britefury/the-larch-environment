##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
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





