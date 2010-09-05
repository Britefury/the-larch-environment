//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Interactor;

import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerNavigationEvent;
import BritefuryJ.DocPresent.Input.PointerInputElement;

public interface NavigationElementInteractor extends AbstractElementInteractor
{
	boolean navigationGestureBegin(PointerInputElement element, PointerButtonEvent event);
	void navigationGestureEnd(PointerInputElement element, PointerButtonEvent event);
	void navigationGesture(PointerInputElement element, PointerNavigationEvent event);
}
