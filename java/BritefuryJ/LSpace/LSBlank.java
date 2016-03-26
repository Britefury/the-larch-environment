//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
	public String getLeafTextRepresentation()
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
