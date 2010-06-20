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
import BritefuryJ.DocPresent.StyleParams.ElementStyleParams;
import BritefuryJ.Parser.ItemStream.ItemStreamBuilder;

public class DPHiddenContent extends DPElement
{
	String textRepresentation;
	
	
	public DPHiddenContent()
	{
		this( ElementStyleParams.defaultStyleParams, "" );
	}
	
	public DPHiddenContent(String textRepresentation)
	{
		this( ElementStyleParams.defaultStyleParams, textRepresentation );
	}
	
	public DPHiddenContent(ElementStyleParams styleParams)
	{
		this(styleParams, "" );
	}

	public DPHiddenContent(ElementStyleParams styleParams, String textRepresentation)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeHiddenContent( this );
		
		this.textRepresentation = textRepresentation;
	}
	
	protected DPHiddenContent(DPHiddenContent element)
	{
		super( element );
		
		layoutNode = new LayoutNodeHiddenContent( this );
		
		this.textRepresentation = element.textRepresentation;
	}
	
	
	//
	//
	// Presentation tree cloning
	//
	//
	
	public DPElement clonePresentationSubtree()
	{
		DPHiddenContent clone = new DPHiddenContent( this );
		clone.clonePostConstuct( this );
		return clone;
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

	protected void getTextRepresentationFromPathToEnd(StringBuilder builder, Marker marker, ArrayList<DPElement> path, int pathMyIndex)
	{
	}

	protected void getTextRepresentationFromStartToPath(StringBuilder builder, Marker marker, ArrayList<DPElement> path, int pathMyIndex)
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

	protected void getLinearRepresentationFromStartToPath(ItemStreamBuilder builder, Marker marker, ArrayList<DPElement> path, int pathMyIndex)
	{
		super.getLinearRepresentationFromStartToPath( builder, marker, path, pathMyIndex );
	}
	
	protected void getLinearRepresentationFromPathToEnd(ItemStreamBuilder builder, Marker marker, ArrayList<DPElement> path, int pathMyIndex)
	{
		super.getLinearRepresentationFromPathToEnd( builder, marker, path, pathMyIndex );
	}
}
