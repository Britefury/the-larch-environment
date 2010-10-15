//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Input;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.Event.PointerEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Interactor.AbstractElementInteractor;
import BritefuryJ.Math.Point2;

public abstract class PointerInputElement
{
	public static class DndTarget
	{
		private PointerInputElement element;
		private DndHandler dndHandler;
		private Point2 elementSpacePos;
		
		public DndTarget(PointerInputElement element, DndHandler dndHandler, Point2 elementSpacePos)
		{
			this.element = element;
			this.dndHandler = dndHandler;
			this.elementSpacePos = elementSpacePos;
		}
		
		
		public PointerInputElement getElement()
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
	}
	
	
	
	protected abstract void handlePointerEnter(PointerMotionEvent event);
	protected abstract void handlePointerLeave(PointerMotionEvent event);
	
	protected abstract PointerInputElement getFirstPointerChildAtLocalPoint(Point2 localPos);
	protected abstract PointerInputElement getLastPointerChildAtLocalPoint(Point2 localPos);
	protected abstract PointerEvent transformParentToLocalEvent(PointerEvent event);
	protected abstract PointerInterface transformParentToLocalPointer(PointerInterface pointer);
	public abstract Point2 transformParentToLocalPoint(Point2 parentPos);
	public abstract AffineTransform getLocalToParentAffineTransform();
	public abstract AffineTransform getParentToLocalAffineTransform();
	
	
	public abstract Iterable<AbstractElementInteractor> getElementInteractors();
	public abstract Iterable<AbstractElementInteractor> getElementInteractors(Class<?> interactorClass);

	
	public abstract boolean isPointerInputElementRealised();
	public abstract boolean containsParentSpacePoint(Point2 parentPos);
	public abstract boolean containsLocalSpacePoint(Point2 localPos);
	
	public abstract DndHandler getDndHandler();



	public static ArrayList<DndTarget> getDndTargets(PointerInputElement element, Point2 localPos)
	{
		ArrayList<DndTarget> targets = new ArrayList<DndTarget>(); 
		getDndTargets( element, localPos, targets );
		return targets;
	}

	private static void getDndTargets(PointerInputElement element, Point2 localPos, List<DndTarget> targets)
	{
		PointerInputElement child = element.getFirstPointerChildAtLocalPoint( localPos );
		if ( child != null )
		{
			getDndTargets( child, child.transformParentToLocalPoint( localPos ), targets );
		}
		
		DndHandler handler = element.getDndHandler();
		if ( handler != null )
		{
			targets.add( new DndTarget( element, handler, localPos ) );
		}
	}

}
