//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Interactor;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.PointerScrollEvent;

public interface ScrollElementInteractor extends AbstractElementInteractor
{
	public boolean scroll(LSElement element, PointerScrollEvent event);
}
