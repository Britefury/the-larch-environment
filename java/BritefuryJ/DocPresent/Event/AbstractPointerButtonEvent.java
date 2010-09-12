//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Event;

import BritefuryJ.DocPresent.Input.PointerInterface;

public abstract class AbstractPointerButtonEvent extends PointerEvent
{
	protected int button;
	
	
	public AbstractPointerButtonEvent(PointerInterface pointer, int button)
	{
		super( pointer );
		
		this.button = button;
	}
	
	
	public int getButton()
	{
		return button;
	}
}
