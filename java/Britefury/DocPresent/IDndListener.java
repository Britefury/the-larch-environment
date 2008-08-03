package Britefury.DocPresent;

import Britefury.Math.Point2;

public interface IDndListener {
	public Object onDndBegin(DndDrag drag);
	
	public boolean dndCanDropFrom(DndDrag drag, Widget dest, Point2 destLocalPos);
	
	public void onDndMotion(DndDrag drag, Widget dest, Point2 destLocalPos);

	public Object dndDragTo(DndDrag drag, Widget dest);
	public void dndDropFrom(DndDrag drag, Widget dest, Point2 destLocalPos);
}
