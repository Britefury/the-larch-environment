//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef PYSIDE_CPP
#define PYSIDE_CPP

#include <Math/Side.h>

#include <boost/python.hpp>
using namespace boost::python;


void export_Side()
{
	enum_<Side>( "Side" )
		.value( "SIDE_NEGATIVE", SIDE_NEGATIVE )
		.value( "SIDE_ON", SIDE_ON )
		.value( "SIDE_POSITIVE", SIDE_POSITIVE )
		.value( "SIDE_BOTH", SIDE_BOTH );
}


#endif
