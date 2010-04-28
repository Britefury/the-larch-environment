//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeSpaceBin;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;

public class DPSpaceBin extends DPBin
{
	private double minWidth, minHeight;
	
	
	public DPSpaceBin(double minWidth, double minHeight)
	{
		this( ContainerStyleParams.defaultStyleParams, minWidth, minHeight );
	}
	
	public DPSpaceBin(ContainerStyleParams styleParams, double minWidth, double minHeight)
	{
		super( styleParams );
		
		this.minWidth = minWidth;
		this.minHeight = minHeight;
		
		layoutNode = new LayoutNodeSpaceBin( this );
	}
	
	protected DPSpaceBin(DPSpaceBin element)
	{
		super( element );

		minWidth = element.minWidth;
		minHeight = element.minHeight;
		
		layoutNode = new LayoutNodeSpaceBin( this );
	}
	
	
	
	//
	//
	// Presentation tree cloning
	//
	//
	
	public DPElement clonePresentationSubtree()
	{
		DPSpaceBin clone = new DPSpaceBin( this );
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
	
	public double getMinHeight()
	{
		return minHeight;
	}
}
