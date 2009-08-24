//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Input;

import java.awt.geom.AffineTransform;

import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Xform2;




public abstract class PointerInterface
{
	abstract public Point2 getLocalPos();
	abstract public int getModifiers();
	
	
	public boolean isButtonPressed(int button)
	{
		return Modifier.getButton( getModifiers(), button );
	}
	
	public boolean isAButtonPressed()
	{
		return Modifier.isAButtonPressed( getModifiers() );
	}
	
	
	public LocalPointerInterface transformed(Xform2 parentToX)
	{
		return new LocalPointerInterface( this, parentToX );
	}
	
	public AffineTransformedPointer transformed(AffineTransform parentToX)
	{
		return new AffineTransformedPointer( this, parentToX );
	}

	
	abstract public PointerInterface concretePointer();
}
