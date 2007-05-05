//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef PYDOCVIEWHELPER_CPP
#define PYDOCVIEWELPER_CPP

#include <boost/python.hpp>
using namespace boost::python;



void export_DocViewBoxTable();



BOOST_PYTHON_MODULE(DocViewHelper)
{
	export_DocViewBoxTable();
}



#endif
