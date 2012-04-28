//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.Input;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Math.Point2;

//
// DndTarget represents a potential target element for a drag and drop operation
// A list of targets is built up
//
public class DndTarget
{
	private LSElement element;
	private DndHandler dndHandler;
	private Point2 elementSpacePos;
	
	public DndTarget(LSElement element, DndHandler dndHandler, Point2 elementSpacePos)
	{
		this.element = element;
		this.dndHandler = dndHandler;
		this.elementSpacePos = elementSpacePos;
	}
	
	
	public LSElement getElement()
	{
		return element;
	}
	
	public DndHandler getDndHandler()
	{
		return dndHandler;
	}
	
	public Point2 getElementSpacePos()
	{
		return elementSpacePos;
	}
	
	
	public boolean isSource()
	{
		return dndHandler.isSource( element );
	}

	public boolean isDest()
	{
		return dndHandler.isDest( element );
	}
	
	
	@Override
	public String toString()
	{
		return "DndTarget( element=" + element + ", dndHandler=" + dndHandler + " )";
	}
}