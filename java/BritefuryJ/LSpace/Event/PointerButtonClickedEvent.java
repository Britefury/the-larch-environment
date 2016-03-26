//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Event;

import java.awt.geom.AffineTransform;

import BritefuryJ.LSpace.Input.PointerInterface;
import BritefuryJ.Math.Xform2;

public class PointerButtonClickedEvent extends AbstractPointerButtonEvent
{
	protected int clickCount;
	
	
	public PointerButtonClickedEvent(PointerInterface pointer, int button, int clickCount)
	{
		super( pointer, button );
		
		this.clickCount = clickCount;
	}
	
	
	public int getClickCount()
	{
		return clickCount;
	}
	
	
	
	public PointerButtonClickedEvent transformed(Xform2 xToLocal)
	{
		return new PointerButtonClickedEvent( pointer.transformed( xToLocal ), button, clickCount );
	}

	public PointerEvent transformed(AffineTransform xToLocal)
	{
		return new PointerButtonClickedEvent( pointer.transformed( xToLocal ), button, clickCount );
	}
}
