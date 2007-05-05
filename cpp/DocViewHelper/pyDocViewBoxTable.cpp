//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef PYDOCVIEWBOXTABLE_CPP__
#define PYDOCVIEWBOXTABLE_CPP__

#include <DocViewHelper/DocViewBoxTable.h>

#include <boost/python.hpp>
using namespace boost::python;


void export_DocViewBoxTable()
{
	class_<DocViewBoxTable>( "DocViewBoxTable", init<>() )
		.def( "addWidgetBox", &DocViewBoxTable::addWidgetBox )
		.def( "setWidgetBox", &DocViewBoxTable::setWidgetBox )
		.def( "removeWidgetBox", &DocViewBoxTable::removeWidgetBox )
		.def( "getIntersectingWidgetList", &DocViewBoxTable::getIntersectingWidgetList )
		.def( "getWidgetAtPoint", &DocViewBoxTable::getWidgetAtPoint );
}


#endif
