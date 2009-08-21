//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Input;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import BritefuryJ.Math.Point2;

public class AffineTransformedPointer extends PointerInterface
{
	protected PointerInterface pointer;
	protected AffineTransform globalToLocal, localToGlobal;
	

	public AffineTransformedPointer(PointerInterface pointer, AffineTransform globalToLocal)
	{
		this.pointer = pointer;
		this.globalToLocal = globalToLocal;
		try
		{
			this.localToGlobal = globalToLocal.createInverse();
		}
		catch (NoninvertibleTransformException e)
		{
			this.localToGlobal = new AffineTransform();
		}
	}
	
	
	public Point2 getLocalPos()
	{
		Point2 globalPos = pointer.getLocalPos();
		Point2D.Double localPos = new Point2D.Double( globalPos.x, globalPos.y );
		globalToLocal.transform( localPos, localPos );
		return new Point2( localPos.x, localPos.y );
	}
	
	public int getModifiers()
	{
		return pointer.getModifiers();
	}


	public AffineTransformedPointer transformed(AffineTransform parentToX)
	{
		AffineTransform x = (AffineTransform)globalToLocal.clone();
		x.concatenate( parentToX );
		return new AffineTransformedPointer( pointer, x );
	}



	public PointerInterface concretePointer()
	{
		return pointer;
	}
}
