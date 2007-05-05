//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef PYCOLOUR3F_CPP
#define PYCOLOUR3F_CPP

#include <Math/Colour3f.h>

#include <boost/python.hpp>
using namespace boost::python;


void export_Colour3f()
{
	class_<Colour3f>( "Colour3f", init<>() )
		.def( init<float, float, float>() )
		.def( init<const Colour3f &>() )
		.def_readwrite( "r", &Colour3f::r )
		.def_readwrite( "g", &Colour3f::g )
		.def_readwrite( "b", &Colour3f::b )
		.def( self == self )
		.def( self != self )
		.def( self + self )
		.def( self += self )
		.def( self - self )
		.def( self -= self )
		.def( self * float() )
		.def( self *= float() )
		.def( self * self )
		.def( self *= self )
		.def( -self );
}


#endif
