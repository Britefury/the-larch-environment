//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Util.Range;

public class ScrollBar extends Control
{
	private Range range;
	
	private DPElement element;
	
	
	
	public ScrollBar(Range range, DPElement element)
	{
		this.range = range;
		this.element = element;
		element.setFixedValue( range );
	}
	
	
	public Range getRange()
	{
		return range;
	}
	
	
	@Override
	public DPElement getElement()
	{
		return element;
	}
}
