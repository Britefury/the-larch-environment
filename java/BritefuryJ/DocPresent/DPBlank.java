//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.ArrayList;

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeBlank;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleParams.ElementStyleParams;

public class DPBlank extends DPElement
{
	public DPBlank()
	{
		super();
	}
	
	public DPBlank(ElementStyleParams styleParams)
	{
		super(styleParams);

		layoutNode = new LayoutNodeBlank( this );
	}
	
	
	protected DPBlank(DPBlank element)
	{
		super( element );

		layoutNode = new LayoutNodeBlank( this );
	}
	
	
	
	
	//
	//
	// Presentation tree cloning
	//
	//
	
	public DPElement clonePresentationSubtree()
	{
		DPBlank clone = new DPBlank( this );
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
		return "";
	}
	
	public int getTextRepresentationLength()
	{
		return 0;
	}

	protected void getTextRepresentationFromPathToEnd(StringBuilder builder, Marker marker, ArrayList<DPElement> path, int pathMyIndex)
	{
	}

	protected void getTextRepresentationFromStartToPath(StringBuilder builder, Marker marker, ArrayList<DPElement> path, int pathMyIndex)
	{
	}




	//
	//
	// VALUE METHODS
	//
	//
	
	public Object getDefaultValue()
	{
		return null;
	}
}
