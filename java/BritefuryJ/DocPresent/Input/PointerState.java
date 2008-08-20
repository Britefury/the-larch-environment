package BritefuryJ.DocPresent.Input;


import BritefuryJ.DocPresent.Input.Modifier;
import BritefuryJ.Math.Point2;



public class PointerState {
	protected PointerInterface pointer;
	protected Point2 localPos;
	protected int modifiers;
	
	
	
	
	public PointerState(PointerInterface pointer)
	{
		this.pointer = pointer;
		
		localPos = pointer.getLocalPos().clone();

		modifiers = pointer.getModifiers();
	}
	
	

	public PointerInterface getPointer()
	{
		return pointer;
	}
	
	public boolean isButtonPressed(int button)
	{
		return Modifier.getButton( getModifiers(), button );
	}
	
	public Point2 getLocalPos()
	{
		return localPos;
	}
	
	public int getModifiers()
	{
		return modifiers;
	}
}
