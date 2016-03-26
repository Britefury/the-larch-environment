//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Interactor;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.PointerMotionEvent;

public interface MotionElementInteractor extends HoverElementInteractor
{
	public void pointerMotion(LSElement element, PointerMotionEvent event);
	public void pointerLeaveIntoChild(LSElement element, PointerMotionEvent event);
	public void pointerEnterFromChild(LSElement element, PointerMotionEvent event);
}
