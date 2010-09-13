//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Interactor;

import java.awt.event.KeyEvent;

import BritefuryJ.DocPresent.DPElement;

public interface KeyElementInteractor extends AbstractElementInteractor
{
	public boolean keyPressed(DPElement element, KeyEvent event);
	public boolean keyReleased(DPElement element, KeyEvent event);
	public boolean keyTyped(DPElement element, KeyEvent event);
}
