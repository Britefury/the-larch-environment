package BritefuryJ.DocPresent.Event;

import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.Math.Xform2;



public class PointerEvent extends Event {
	public PointerInterface pointer;
	
	
	public PointerEvent(PointerInterface pointer)
	{
		this.pointer = pointer;
	}
	
	
	public PointerEvent transformed(Xform2 xToLocal)
	{
		return new PointerEvent( pointer.transformed( xToLocal ) );
	}
}
