//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeSpacer;
import BritefuryJ.LSpace.StyleParams.ElementStyleParams;
import BritefuryJ.Math.Vector2;

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
	
	
	public void setMinWidth(double minWidth)
	{
		this.minWidth = minWidth;
		queueResize();
	}
	
	public void setMinHeight(double minHeight)
	{
		this.minHeight = minHeight;
		queueResize();
	}
	
	public void setMinSize(double minWidth, double minHeight)
	{
		this.minWidth = minWidth;
		this.minHeight = minHeight;
		queueResize();
	}

	public void setMinSize(Vector2 minSize)
	{
		this.minWidth = minSize.x;
		this.minHeight = minSize.y;
		queueResize();
	}
}
