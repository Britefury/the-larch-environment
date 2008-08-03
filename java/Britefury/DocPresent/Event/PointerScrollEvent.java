package Britefury.DocPresent.Event;

import Britefury.DocPresent.Input.PointerInterface;
import Britefury.Math.Xform2;

public class PointerScrollEvent extends PointerEvent {
	public int scrollX, scrollY;
	
	
	public PointerScrollEvent(PointerInterface device, int scrollX, int scrollY)
	{
		super( device );
		
		this.scrollX = scrollX;
		this.scrollY = scrollY;
	}
	
	
	
	public PointerScrollEvent transformed(Xform2 xToLocal)
	{
		return new PointerScrollEvent( pointer.transformed( xToLocal ), scrollX, scrollY );
	}
}
