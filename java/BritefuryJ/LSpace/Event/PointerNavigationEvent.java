//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Event;

import BritefuryJ.LSpace.Input.PointerInterface;
import BritefuryJ.Math.Xform2;

public abstract class PointerNavigationEvent extends PointerEvent
{
	public PointerNavigationEvent(PointerInterface pointer)
	{
		super( pointer );
	}
	
	
	public abstract Xform2 createXform();
}
