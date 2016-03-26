//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Event;

import java.awt.geom.AffineTransform;

import BritefuryJ.LSpace.Input.PointerInterface;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;

public class PointerNavigationPanEvent extends PointerNavigationEvent
{
	protected Vector2 pan;
	
	
	public PointerNavigationPanEvent(PointerInterface device, Vector2 pan)
	{
		super( device );
		
		this.pan = pan;
	}
	
	
	
	public Xform2 createXform()
	{
		return new Xform2( pan );
	}

	
	public PointerNavigationPanEvent transformed(Xform2 xToLocal)
	{
		return new PointerNavigationPanEvent( pointer.transformed( xToLocal ), xToLocal.transform( pan ) );
	}

	public PointerNavigationPanEvent transformed(AffineTransform xToLocal)
	{
		return new PointerNavigationPanEvent( pointer.transformed( xToLocal ), pan.transform( xToLocal ) );
	}
}
