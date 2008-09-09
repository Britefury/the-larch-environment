//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.Math.Point2;

public interface DndListener {
	public Object onDndBegin(DndDrag drag);
	
	public boolean dndCanDropFrom(DndDrag drag, DPWidget dest, Point2 destLocalPos);
	
	public void onDndMotion(DndDrag drag, DPWidget dest, Point2 destLocalPos);

	public Object dndDragTo(DndDrag drag, DPWidget dest);
	public void dndDropFrom(DndDrag drag, DPWidget dest, Point2 destLocalPos);
}
