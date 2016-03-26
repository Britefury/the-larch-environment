//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Input;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import BritefuryJ.Math.Point2;

public class AffineTransformedPointer extends PointerInterface
{
	protected final PointerInterface pointer;
	protected final Pointer concretePointer;
	protected AffineTransform globalToLocal, localToGlobal;
	

	public AffineTransformedPointer(PointerInterface pointer, Pointer concretePointer, AffineTransform globalToLocal)
	{
		this.pointer = pointer;
		this.concretePointer = concretePointer;
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
		AffineTransform x = (AffineTransform)parentToX.clone();
		x.concatenate( globalToLocal );
		return new AffineTransformedPointer( pointer, concretePointer, x );
	}



	public Pointer concretePointer()
	{
		return concretePointer;
	}
}
