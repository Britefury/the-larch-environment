//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Input;

public class InputState {
	public class InvalidPointerException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	
	protected InputTable inputTable;
	protected PointerState mouse;
	
	
	
	
	public InputState()
	{
	}
	
	
	public void realise(InputTable inputTable)
	{
		this.inputTable = inputTable;
	}
	
	public void unrealise()
	{
		mouse = null;
		inputTable = null;
	}
	
	
	
	public void addPointerState(PointerInterface pointer)
	{
		if ( pointer == inputTable.mouse )
		{
			mouse = new PointerState( inputTable.mouse );
		}
		else
		{
			throw new InvalidPointerException();
		}
	}
	
	public void removePointerState(PointerInterface pointer)
	{
		if ( pointer == inputTable.mouse )
		{
			mouse = null;
		}
		else
		{
			throw new InvalidPointerException();
		}
	}
	
	public PointerState getPointerState(PointerInterface pointer)
	{
		if ( pointer == inputTable.mouse )
		{
			return mouse;
		}
		else
		{
			throw new InvalidPointerException();
		}
	}

	
	
	PointerState getMouse()
	{
		return mouse;
	}
	
}
