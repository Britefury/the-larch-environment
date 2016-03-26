//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Interactor;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.PointerButtonEvent;

public interface PushElementInteractor extends AbstractElementInteractor
{
	public boolean buttonPress(LSElement element, PointerButtonEvent event);
	public void buttonRelease(LSElement element, PointerButtonEvent event);
}
