//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
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
