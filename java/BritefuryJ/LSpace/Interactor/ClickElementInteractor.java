//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Interactor;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.AbstractPointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;

public interface ClickElementInteractor extends AbstractElementInteractor
{
	public boolean testClickEvent(LSElement element, AbstractPointerButtonEvent event);
	public boolean buttonClicked(LSElement element, PointerButtonClickedEvent event);
}
