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
import BritefuryJ.Parser.ItemStream.ItemStreamBuilder;

public abstract class DPParagraphMarker extends DPWidget
{
	public DPParagraphMarker()
	{
		super();
	}
	
	public DPParagraphMarker(WidgetStyleSheet styleSheet)
	{
		super( styleSheet );
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
		if ( filter == null  ||  filter.testElement( this ) )
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
		return "";
	}
	
	public int getTextRepresentationLength()
	{
		return 0;
	}

	protected void getTextRepresentationFromPathToEnd(StringBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
	{
	}

	protected void getTextRepresentationFromStartToPath(StringBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
	{
	}




	//
	//
	// LINEAR REPRESENTATION METHODS
	//
	//
	
	
	public void buildLinearRepresentation(ItemStreamBuilder builder)
	{
	}
	
	protected void getLinearRepresentationFromPathToEnd(ItemStreamBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
	{
	}

	protected void getLinearRepresentationFromStartToPath(ItemStreamBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
	{
	}
}
