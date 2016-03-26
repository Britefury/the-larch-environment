//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Input;

import java.awt.event.MouseEvent;

public interface DndController
{
	void dndInitiateDrag(DndDragSwing drop, MouseEvent mouseEvent, int requestedAction);
}
