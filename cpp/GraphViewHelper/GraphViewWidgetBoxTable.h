//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef GRAPHVIEWWIDGETBOXTABLE_H__
#define GRAPHVIEWWIDGETBOXTABLE_H__

#include <boost/python.hpp>
using namespace boost::python;



#include <Util/Array.h>

#include <Math/BBox2.h>

class _DllExport_ GraphViewWidgetBoxTable
{
private:
	class _DllExport_ TableEntry
	{
	public:
		BBox2 box;
		bool bValid;

		inline TableEntry() : bValid( false )
		{
		}

		inline TableEntry(const BBox2 &box)
					: box( box ), bValid( true )
		{
		}
	};

	Array<TableEntry> table;
	Array<int> freeWidgetIds;



public:
	GraphViewWidgetBoxTable();


	int addWidgetBox(const BBox2 &box);
	void setWidgetBox(int nodeId, const BBox2 &box);
	void removeWidgetBox(int nodeId);


	boost::python::list getIntersectingWidgetList(const BBox2 &box) const;
	int getWidgetAtPoint(const Point2 &point) const;
};



#endif // GRAPHVIEWWIDGETBOXTABLE_H__


