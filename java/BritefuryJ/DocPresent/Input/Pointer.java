//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Input;

import BritefuryJ.Math.Point2;



public class Pointer extends PointerInterface
{
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
