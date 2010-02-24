//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.ArrayList;

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeHiddenContent;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleParams.WidgetStyleParams;
import BritefuryJ.Parser.ItemStream.ItemStreamBuilder;

public class DPHiddenContent extends DPWidget
{
	String textRepresentation;
	
	
	public DPHiddenContent()
	{
		this( WidgetStyleParams.defaultStyleParams, "" );
	}
	
	public DPHiddenContent(String textRepresentation)
	{
		this( WidgetStyleParams.defaultStyleParams, textRepresentation );
	}
	
	public DPHiddenContent(WidgetStyleParams styleParams)
	{
		this(styleParams, "" );
	}

	public DPHiddenContent(WidgetStyleParams styleParams, String textRepresentation)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeHiddenContent( this );
		
		this.textRepresentation = textRepresentation;
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




	//
	//
	// LINEAR REPRESENTATION METHODS
	//
	//
	
	
	public void buildLinearRepresentation(ItemStreamBuilder builder)
	{
		builder.appendTextValue( textRepresentation );
	}
	
	protected void getLinearRepresentationFromPathToEnd(ItemStreamBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
	{
	}

	protected void getLinearRepresentationFromStartToPath(ItemStreamBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
	{
	}
}
