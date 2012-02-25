//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeBlank;
import BritefuryJ.DocPresent.StyleParams.ElementStyleParams;

public class DPBlank extends DPElement
{
	public DPBlank()
	{
		super();

		layoutNode = new LayoutNodeBlank( this );
	}
	
	public DPBlank(ElementStyleParams styleParams)
	{
		super(styleParams);

		layoutNode = new LayoutNodeBlank( this );
	}
	
	
	
	
	//
	//
	// TEXT REPRESENTATION METHODS
	//
	//
	
	@Override
	protected String getLeafTextRepresentation()
	{
		return "";
	}
	
	
	//
	//
	// VALUE METHODS
	//
	//
	
	@Override
	public Object getDefaultValue()
	{
		return null;
	}
}
