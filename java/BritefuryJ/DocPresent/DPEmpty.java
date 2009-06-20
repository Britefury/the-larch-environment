//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.ArrayList;

import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.WidgetStyleSheet;
import BritefuryJ.Math.Point2;

public class DPEmpty extends DPWidget
{
	String textRepresentation;
	
	
	public DPEmpty()
	{
		this( WidgetStyleSheet.defaultStyleSheet, "" );
	}
	
	public DPEmpty(String textRepresentation)
	{
		this( WidgetStyleSheet.defaultStyleSheet, textRepresentation );
	}
	
	public DPEmpty(WidgetStyleSheet styleSheet)
	{
		this( styleSheet, "" );
	}

	public DPEmpty(WidgetStyleSheet styleSheet, String textRepresentation)
	{
		super( styleSheet );
		
		this.textRepresentation = textRepresentation;
	}

	
	
	protected void updateRequisitionX()
	{
		layoutReqBox.clearRequisitionX();
	}

	protected void updateRequisitionY()
	{
		layoutReqBox.clearRequisitionY();
	}
	



	protected DPWidget getLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		if ( filter.testEmpty( this ) )
		{
			return this;
		}
		else
		{
			return null;
		}
	}
	
	
	
	//
	//
	// TEXT REPRESENTATION METHODS
	//
	//
	
	
	public String getTextRepresentation()
	{
		return textRepresentation;
	}
	
	public int getTextRepresentationLength()
	{
		return textRepresentation.length();
	}

	protected void getTextRepresentationFromPathToEnd(StringBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
	{
	}

	protected void getTextRepresentationFromStartToPath(StringBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
	{
	}
}
