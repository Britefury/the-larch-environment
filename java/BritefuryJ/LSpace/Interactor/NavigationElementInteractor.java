//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Interactor;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerNavigationEvent;

public interface NavigationElementInteractor extends AbstractElementInteractor
{
	boolean navigationGestureBegin(LSElement element, PointerButtonEvent event);
	void navigationGestureEnd(LSElement element, PointerButtonEvent event);
	void navigationGesture(LSElement element, PointerNavigationEvent event);
}
