//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Input;

import java.util.ArrayList;
import java.util.WeakHashMap;




public class InputTable
{
	private WeakHashMap<PointerInputElement, ArrayList<PointerInterface>> pointersWithinBoundsByElement;

	protected Pointer mouse;
	protected PointerInputElement rootElement;
	
	
	public InputTable(PointerInputElement rootElement, PointerDndController dndController)
	{
		pointersWithinBoundsByElement = new WeakHashMap<PointerInputElement, ArrayList<PointerInterface>>();
		this.rootElement = rootElement;
		mouse = new Pointer( this, rootElement, dndController );
	}
	
	
	public Pointer getMouse()
	{
		return mouse;
	}
	
	
	
	public void onElementUnrealised(PointerInputElement element)
	{
		mouse.onElementUnrealised( element );
	}
	
	public void onRootElementReallocate()
	{
		mouse.onRootElementReallocate();
	}
	
	public ArrayList<PointerInterface> getPointersWithinBoundsOfElement(PointerInputElement element)
	{
		return pointersWithinBoundsByElement.get( element );
	}
	
	
	protected void addPointerWithinElementBounds(PointerInterface pointer, PointerInputElement element)
	{
		ArrayList<PointerInterface> pointersWithinBounds = pointersWithinBoundsByElement.get( element );
		if ( pointersWithinBounds == null )
		{
			pointersWithinBounds = new ArrayList<PointerInterface>();
			pointersWithinBoundsByElement.put( element, pointersWithinBounds );
		}
		if ( !pointersWithinBounds.contains( pointer ) )
		{
			pointersWithinBounds.add( pointer );
		}
	}

	protected void removePointerWithinElementBounds(PointerInterface pointer, PointerInputElement element)
	{
		ArrayList<PointerInterface> pointersWithinBounds = pointersWithinBoundsByElement.get( element );
		if ( pointersWithinBounds != null )
		{
			pointersWithinBounds.remove( pointer );
			if ( pointersWithinBounds.isEmpty() )
			{
				pointersWithinBounds = null;
				pointersWithinBoundsByElement.remove( element );
			}
		}
	}
}
