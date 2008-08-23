package BritefuryJ.DocPresent.Input;

import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Xform2;

public class LocalPointerInterface extends PointerInterface
{
	protected PointerInterface pointer;
	protected Xform2 globalToLocal, localToGlobal;
	

	public LocalPointerInterface(PointerInterface pointer, Xform2 globalToLocal)
	{
		this.pointer = pointer;
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


	public Xform2 getLocalToGlobalXform()
	{
		return localToGlobal;
	}
	
	public Xform2 getGlobalToLocalXform()
	{
		return globalToLocal;
	}



	public LocalPointerInterface transformed(Xform2 parentToX)
	{
		return new LocalPointerInterface( pointer, globalToLocal.concat( parentToX ) );
	}



	public PointerInterface concretePointer()
	{
		return pointer;
	}
}
