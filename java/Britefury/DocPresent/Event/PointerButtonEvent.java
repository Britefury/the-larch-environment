package Britefury.DocPresent.Event;

import Britefury.DocPresent.Input.PointerInterface;
import Britefury.Math.Xform2;



public class PointerButtonEvent extends PointerEvent {
	public enum Action
	{
		DOWN,
		DOWN2,
		DOWN3,
		UP
	}
	
	public int button;
	public Action action;
	
	
	public PointerButtonEvent(PointerInterface device, int button, Action action)
	{
		super( device );
		
		this.button = button;
		this.action = action;
	}
	
	
	
	public PointerButtonEvent transformed(Xform2 xToLocal)
	{
		return new PointerButtonEvent( pointer.transformed( xToLocal ), button, action );
	}
}
