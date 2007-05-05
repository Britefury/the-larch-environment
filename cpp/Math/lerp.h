//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef LERP_H__
#define LERP_H__



template <typename T, typename Real> inline T lerp(const T &a, const T &b, Real t)
{
	return a  +  ( b - a )  *  t;
}



#endif
