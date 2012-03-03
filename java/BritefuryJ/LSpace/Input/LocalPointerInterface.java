//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.LSpace.Input;

import java.awt.geom.AffineTransform;

import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Xform2;

public class LocalPointerInterface extends PointerInterface
{
	protected PointerInterface pointer;
	protected Pointer concretePointer;
	protected Xform2 globalToLocal, localToGlobal;
	

	public LocalPointerInterface(PointerInterface pointer, Pointer concretePointer, Xform2 globalToLocal)
	{
		this.pointer = pointer;
		this.concretePointer = concretePointer;
		this.globalToLocal = globalToLocal;
		this.localToGlobal = globalToLocal.inverse();
	}
	
	
	public Point2 getLocalPos()
	{
		return globalToLocal.transform( pointer.getLocalPos() );
	}
	
	public int getModifiers()
	{
		return pointer.getModifiers();
	}


	public LocalPointerInterface transformed(Xform2 parentToX)
	{
		return new LocalPointerInterface( pointer, concretePointer, globalToLocal.concat( parentToX ) );
	}

	public AffineTransformedPointer transformed(AffineTransform parentToX)
	{
		AffineTransform x = globalToLocal.toAffineTransform();
		x.concatenate( parentToX );
		return new AffineTransformedPointer( pointer, concretePointer, x );
	}


	public Pointer concretePointer()
	{
		return concretePointer;
	}
}
