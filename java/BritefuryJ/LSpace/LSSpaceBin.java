//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeSpaceBin;
import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;

public class LSSpaceBin extends LSBin
{
	private double minWidth, minHeight;
	
	
	public LSSpaceBin(double minWidth, double minHeight)
	{
		this( ContainerStyleParams.defaultStyleParams, minWidth, minHeight );
	}
	
	public LSSpaceBin(ContainerStyleParams styleParams, double minWidth, double minHeight)
	{
		super( styleParams );
		
		this.minWidth = minWidth;
		this.minHeight = minHeight;
		
		layoutNode = new LayoutNodeSpaceBin( this );
	}
	
	
	
	//
	//
	// Space bin
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
