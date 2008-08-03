package Britefury.DocPresent.Input;

import Britefury.DocPresent.Input.Modifier;
import Britefury.Math.Point2;
import Britefury.Math.Xform2;




public abstract class PointerInterface {
	abstract public Point2 getLocalPos();
	abstract public int getModifiers();
	
	
	public boolean isButtonPressed(int button)
	{
		return Modifier.getButton( getModifiers(), button );
	}
	
	
	public Xform2 getLocalToGlobalXform()
	{
		return new Xform2();
	}
	
	public Xform2 getGlobalToLocalXform()
	{
		return new Xform2();
	}
	
	
	public LocalPointerInterface transformed(Xform2 xToLocal)
	{
		return new LocalPointerInterface( this, xToLocal );
	}
	
	
	abstract public PointerInterface concretePointer();
}
