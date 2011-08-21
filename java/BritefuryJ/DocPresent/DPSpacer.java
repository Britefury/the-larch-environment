//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeSpacer;
import BritefuryJ.DocPresent.StyleParams.ElementStyleParams;

public class DPSpacer extends DPBlank
{
	private double minWidth, minHeight;
	
	
	public DPSpacer(double minWidth, double minHeight)
	{
		this( ElementStyleParams.defaultStyleParams, minWidth, minHeight );
	}
	
	public DPSpacer(ElementStyleParams styleParams, double minWidth, double minHeight)
	{
		super( styleParams );
		
		this.minWidth = minWidth;
		this.minHeight = minHeight;
		
		layoutNode = new LayoutNodeSpacer( this );
	}
	
	protected DPSpacer(DPSpacer element)
	{
		super( element );
		
		this.minWidth = element.minWidth;
		this.minHeight = element.minHeight;
		
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
