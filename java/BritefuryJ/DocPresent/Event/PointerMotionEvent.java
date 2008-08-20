package BritefuryJ.DocPresent.Event;

import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.Math.Xform2;



public class PointerMotionEvent extends PointerEvent {
	public enum Action
	{
		ENTER,
		LEAVE,
		MOTION
	}
	
	public Action action;
	
	
	public PointerMotionEvent(PointerInterface device, Action action)
	{
		super( device );
		
		this.action = action;
	}
	
	
	
	public PointerMotionEvent transformed(Xform2 xToLocal)
	{
		return new PointerMotionEvent( pointer.transformed( xToLocal ), action );
	}
}
