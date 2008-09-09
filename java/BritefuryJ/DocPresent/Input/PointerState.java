//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
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
