//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Interactor;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.LSpace.Focus.SelectionPoint;
import BritefuryJ.Math.Point2;

public interface TargetElementInteractor extends AbstractElementInteractor
{
	public SelectionPoint targetDragBegin(LSElement element, PointerButtonEvent event);
	public void targetDragEnd(LSElement startElement, LSElement elementBeneathPointer, PointerButtonEvent event, Point2 dragStartPos, int dragButton);
	public SelectionPoint targetDragMotion(LSElement startElement, LSElement elementBeneathPointer, PointerMotionEvent event, Point2 dragStartPos, int dragButton);
}
