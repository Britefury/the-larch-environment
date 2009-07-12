//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.ArrayList;

import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.WidgetStyleSheet;
import BritefuryJ.Math.Point2;

public abstract class DPStatic extends DPWidget
{
	public DPStatic()
	{
		super();
	}
	
	public DPStatic(WidgetStyleSheet styleSheet)
	{
		super( styleSheet );
	}

	
	
	protected DPWidget getLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		return null;
	}

	public String getTextRepresentation()
	{
		return "";
	}

	protected void getTextRepresentationFromPathToEnd(StringBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
	{
	}

	protected void getTextRepresentationFromStartToPath(StringBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
	{
	}

	public int getTextRepresentationLength()
	{
		return 0;
	}
}