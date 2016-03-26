//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeAspectRatioBin;
import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;

public class LSAspectRatioBin extends LSBin
{
	private double minWidth, aspectRatio;
	
	
	public LSAspectRatioBin(ContainerStyleParams styleParams, double minWidth, double aspectRatio, LSElement child)
	{
		super( styleParams, child );
		
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
