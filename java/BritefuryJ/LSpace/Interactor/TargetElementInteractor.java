//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.Interactor;

import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.LSpace.Input.PointerInputElement;
import BritefuryJ.LSpace.Selection.SelectionPoint;
import BritefuryJ.Math.Point2;

public interface TargetElementInteractor extends AbstractElementInteractor
{
	public SelectionPoint targetDragBegin(PointerInputElement element, PointerButtonEvent event);
	public void targetDragEnd(PointerInputElement element, PointerButtonEvent event, Point2 dragStartPos, int dragButton);
	public SelectionPoint targetDragMotion(PointerInputElement element, PointerMotionEvent event, Point2 dragStartPos, int dragButton);
}
