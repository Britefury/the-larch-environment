package BritefuryJ.DocPresent.Event;

import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.Math.Xform2;



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
	
	
	public PointerButtonEvent(PointerInterface pointer, int button, Action action)
	{
		super( pointer );
		
		this.button = button;
		this.action = action;
	}
	
	
	public int getButton()
	{
		return button;
	}
	
	public Action getAction()
	{
		return action;
	}
	
	
	
	public PointerButtonEvent transformed(Xform2 xToLocal)
	{
		return new PointerButtonEvent( pointer.transformed( xToLocal ), button, action );
	}
}
