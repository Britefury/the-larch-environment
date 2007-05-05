//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2007.
//##************************
#ifndef DOCVIEWBOXTABLE_CPP__
#define DOCVIEWBOXTABLE_CPP__

#include <Math/BBox2.h>

#include <DocViewHelper/DocViewBoxTable.h>



DocViewBoxTable::DocViewBoxTable()
{
}


int DocViewBoxTable::addWidgetBox(const BBox2 &box)
{
	int nodeId = -1;

	if ( freeWidgetIds.size() == 0 )
	{
		nodeId = table.size();
		table.push_back( TableEntry( box ) );
		return nodeId;
	}
	else
	{
		nodeId = freeWidgetIds.back();
		freeWidgetIds.pop_back();
		table[nodeId] = TableEntry( box );
		return nodeId;
	}
}

void DocViewBoxTable::setWidgetBox(int nodeId, const BBox2 &box)
{
	table[nodeId] = TableEntry( box );
}

void DocViewBoxTable::removeWidgetBox(int nodeId)
{
	table[nodeId].bValid = false;
	freeWidgetIds.push_back( nodeId );
}



boost::python::list DocViewBoxTable::getIntersectingWidgetList(const BBox2 &box) const
{
	boost::python::list result;

	for (int entryI = 0; entryI < (int)table.size(); entryI++)
	{
		const TableEntry &entry = table[entryI];
		if ( entry.bValid )
		{
			if ( entry.box.intersects( box ) )
			{
				result.append( entryI );
			}
		}
	}

	return result;
}

int DocViewBoxTable::getWidgetAtPoint(const Point2 &point) const
{
	for (int entryI = table.size() - 1; entryI >= 0; entryI--)
	{
		const TableEntry &entry = table[entryI];
		if ( entry.bValid )
		{
			if ( entry.box.contains( point ) )
			{
				return entryI;
			}
		}
	}

	return -1;
}





#endif // DOCVIEWBOXTABLE_CPP__


