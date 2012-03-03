//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeSpacer;
import BritefuryJ.LSpace.StyleParams.ElementStyleParams;

public class LSSpacer extends LSBlank
{
	private double minWidth, minHeight;
	
	
	public LSSpacer(double minWidth, double minHeight)
	{
		this( ElementStyleParams.defaultStyleParams, minWidth, minHeight );
	}
	
	public LSSpacer(ElementStyleParams styleParams, double minWidth, double minHeight)
	{
		super( styleParams );
		
		this.minWidth = minWidth;
		this.minHeight = minHeight;
		
		layoutNode = new LayoutNodeSpacer( this );
	}
	
	
	//
	//
	// Space requirements
	//
	//
	
	public double getMinWidth()
	{
		return minWidth;
	}
	
	public double getMinHeight()
	{
		return minHeight;
	}
}
