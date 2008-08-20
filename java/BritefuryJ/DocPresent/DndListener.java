package BritefuryJ.DocPresent;

import BritefuryJ.Math.Point2;

public interface DndListener {
	public Object onDndBegin(DndDrag drag);
	
	public boolean dndCanDropFrom(DndDrag drag, DPWidget dest, Point2 destLocalPos);
	
	public void onDndMotion(DndDrag drag, DPWidget dest, Point2 destLocalPos);

	public Object dndDragTo(DndDrag drag, DPWidget dest);
	public void dndDropFrom(DndDrag drag, DPWidget dest, Point2 destLocalPos);
}
