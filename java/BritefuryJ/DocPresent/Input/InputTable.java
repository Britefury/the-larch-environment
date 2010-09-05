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

import javax.swing.JComponent;

import BritefuryJ.DocPresent.PresentationComponent;




public class InputTable
{
	private WeakHashMap<PointerInputElement, ArrayList<PointerInterface>> pointersWithinBoundsByElement;

	protected Pointer mouse;
	protected PointerInputElement rootElement;
	protected JComponent component;
	
	
	public InputTable(PointerInputElement rootElement, DndController dndController, PresentationComponent component)
	{
		pointersWithinBoundsByElement = new WeakHashMap<PointerInputElement, ArrayList<PointerInterface>>();
		this.rootElement = rootElement;
		this.component = component;
		mouse = new Pointer( this, rootElement, dndController, component );
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
	
	
	public boolean arePointersWithinBoundsOfElement(PointerInputElement element)
	{
		return pointersWithinBoundsByElement.containsKey( element );
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
