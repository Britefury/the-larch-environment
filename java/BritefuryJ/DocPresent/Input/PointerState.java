package Britefury.DocPresent.Input;


import Britefury.DocPresent.Input.Modifier;
import Britefury.Math.Point2;



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
