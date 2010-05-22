//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeAspectRatioBin;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;

public class DPAspectRatioBin extends DPBin
{
	private double minWidth, aspectRatio;
	
	
	public DPAspectRatioBin(double minWidth, double aspectRatio)
	{
		this( ContainerStyleParams.defaultStyleParams, minWidth, aspectRatio );
	}
	
	public DPAspectRatioBin(ContainerStyleParams styleParams, double minWidth, double aspectRatio)
	{
		super( styleParams );
		
		this.minWidth = minWidth;
		this.aspectRatio = aspectRatio;
		
		layoutNode = new LayoutNodeAspectRatioBin( this );
	}
	
	private DPAspectRatioBin(DPAspectRatioBin element)
	{
		super( element );

		minWidth = element.minWidth;
		aspectRatio = element.aspectRatio;
		
		layoutNode = new LayoutNodeAspectRatioBin( this );
	}
	
	
	
	//
	//
	// Presentation tree cloning
	//
	//
	
	public DPElement clonePresentationSubtree()
	{
		DPAspectRatioBin clone = new DPAspectRatioBin( this );
		clone.clonePostConstuct( this );
		return clone;
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
