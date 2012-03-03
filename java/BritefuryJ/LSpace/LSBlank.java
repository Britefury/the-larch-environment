//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeBlank;
import BritefuryJ.LSpace.StyleParams.ElementStyleParams;

public class LSBlank extends LSElement
{
	public LSBlank()
	{
		super();

		layoutNode = new LayoutNodeBlank( this );
	}
	
	public LSBlank(ElementStyleParams styleParams)
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
