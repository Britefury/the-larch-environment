//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef CLAMP_H__
#define CLAMP_H__

#include <algorithm>


template <typename T> inline const T & clampLower(const T &x, const T &lower)
{
	return std::max( x, lower );
}

template <typename T> inline const T & clampUpper(const T &x, const T &upper)
{
	return std::min( x, upper );
}

template <typename T> inline const T & clamp(const T &x, const T &lower, const T &upper)
{
	return clampUpper( clampLower( x, lower ), upper );
}


#endif

