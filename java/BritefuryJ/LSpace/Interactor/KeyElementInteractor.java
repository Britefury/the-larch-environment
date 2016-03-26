//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Interactor;

import java.awt.event.KeyEvent;

import BritefuryJ.LSpace.LSElement;

public interface KeyElementInteractor extends AbstractElementInteractor
{
	public boolean keyPressed(LSElement element, KeyEvent event);
	public boolean keyReleased(LSElement element, KeyEvent event);
	public boolean keyTyped(LSElement element, KeyEvent event);
}
