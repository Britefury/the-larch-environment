//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Input;

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
		return new LocalPointerInterface( this, concretePointer(), parentToX );
	}
	
	public AffineTransformedPointer transformed(AffineTransform parentToX)
	{
		return new AffineTransformedPointer( this, concretePointer(), parentToX );
	}

	
	abstract public Pointer concretePointer();
}
