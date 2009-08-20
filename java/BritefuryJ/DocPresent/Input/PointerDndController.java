//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Input;

import java.awt.event.MouseEvent;

public interface PointerDndController
{
	void pointerDndInitiateDrag(Pointer pointer, DndDropLocal drop, MouseEvent mouseEvent, int requestedAction);
	
	void pointerDndSetCursorDrag(Pointer pointer);
	void pointerDndSetCursorArrow(Pointer pointer);
}
