package Britefury.DocPresent.Input;

import Britefury.Math.Point2;
import Britefury.Math.Xform2;

public class LocalPointerInterface extends PointerInterface {
	protected PointerInterface pointer;
	protected Xform2 globalToLocal, localToGlobal;
	

	public LocalPointerInterface(PointerInterface pointer, Xform2 localToGlobal)
	{
		this.pointer = pointer;
		this.localToGlobal = localToGlobal;
		this.globalToLocal = localToGlobal.inverse();
	}
	
	
	public Point2 getLocalPos()
	{
		return globalToLocal.transform( pointer.getLocalPos() );
	}
	
	public int getModifiers()
	{
		return pointer.getModifiers();
	}


	public Xform2 getLocalToGlobalXform()
	{
		return localToGlobal;
	}
	
	public Xform2 getGlobalToLocalXform()
	{
		return globalToLocal;
	}



	public LocalPointerInterface transformed(Xform2 xToLocal)
	{
		return new LocalPointerInterface( pointer, xToLocal.concat( localToGlobal ) );
	}



	public PointerInterface concretePointer()
	{
		return pointer;
	}
}
