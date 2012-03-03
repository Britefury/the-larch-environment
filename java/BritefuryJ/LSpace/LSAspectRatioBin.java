//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeAspectRatioBin;
import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;

public class LSAspectRatioBin extends LSBin
{
	private double minWidth, aspectRatio;
	
	
	public LSAspectRatioBin(double minWidth, double aspectRatio)
	{
		this( ContainerStyleParams.defaultStyleParams, minWidth, aspectRatio );
	}
	
	public LSAspectRatioBin(ContainerStyleParams styleParams, double minWidth, double aspectRatio)
	{
		super( styleParams );
		
		this.minWidth = minWidth;
		this.aspectRatio = aspectRatio;
		
		layoutNode = new LayoutNodeAspectRatioBin( this );
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
	
	public double getAspectRatio()
	{
		return aspectRatio;
	}
}
