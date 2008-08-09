package Britefury.DocPresent.Input;

import Britefury.Math.Point2;



public class Pointer extends PointerInterface {
	protected Point2 localPos;
	protected int modifiers;
	
	
	public Pointer()
	{
		localPos = new Point2();
		modifiers = 0;
	}
	
	

	public Point2 getLocalPos()
	{
		return localPos;
	}
	
	public int getModifiers()
	{
		return modifiers;
	}


	public PointerInterface concretePointer()
	{
		return this;
	}
	
	
	
	public void setLocalPos(Point2 pos)
	{
		localPos = pos;
	}
	
	public void setModifiers(int mods)
	{
		modifiers = mods;
	}
}
