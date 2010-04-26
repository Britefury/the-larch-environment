//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPViewport;
import BritefuryJ.DocPresent.Util.Range;

public class ScrolledViewport extends Control
{
	private DPViewport viewport;
	private DPElement element;
	private ScrollBar xScrollBar, yScrollBar;
	private Range xRange, yRange;
	
	
	
	public ScrolledViewport(DPViewport viewport, DPElement element, ScrollBar xScrollBar, ScrollBar yScrollBar, Range xRange, Range yRange)
	{
		this.viewport = viewport;
		this.element = element;
		this.xScrollBar = xScrollBar;
		this.yScrollBar = yScrollBar;
		this.xRange = xRange;
		this.yRange = yRange;
	}
	
	
	public DPViewport getViewportElement()
	{
		return viewport;
	}
	
	@Override
	public DPElement getElement()
	{
		return element;
	}
	
	public ScrollBar getXScrollBar()
	{
		return xScrollBar;
	}

	public ScrollBar getYScrollBar()
	{
		return yScrollBar;
	}

	public Range getXRange()
	{
		return xRange;
	}

	public Range getYRange()
	{
		return yRange;
	}
}
