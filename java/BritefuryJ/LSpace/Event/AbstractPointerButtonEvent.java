//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Event;

import BritefuryJ.LSpace.Input.PointerInterface;

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
