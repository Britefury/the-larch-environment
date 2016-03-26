//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Interactor;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.Math.Point2;


public interface DragElementInteractor extends AbstractElementInteractor
{
	public boolean dragBegin(LSElement element, PointerButtonEvent event);
	public void dragEnd(LSElement element, PointerButtonEvent event, Point2 dragStartPos, int dragButton);
	public void dragMotion(LSElement element, PointerMotionEvent event, Point2 dragStartPos, int dragButton);
}
