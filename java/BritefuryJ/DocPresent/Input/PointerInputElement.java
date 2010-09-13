//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Input;

import java.awt.geom.AffineTransform;

import BritefuryJ.DocPresent.Event.PointerEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Interactor.AbstractElementInteractor;
import BritefuryJ.Math.Point2;

public abstract class PointerInputElement
{
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
	
	protected abstract PointerInputElement getDndElement(Point2 localPos, Point2 targetPos[]);				// targetPos is an output parameter
	public abstract DndHandler getDndHandler();
}
