//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Dispatch;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocModel.DMObject;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.IncrementalView.ViewFragmentFunction;
import BritefuryJ.Pres.Pres;

public class ObjectDispatchViewFragmentFunction implements ViewFragmentFunction
{
	private PyObject dispatchInstance;
	
	
	public ObjectDispatchViewFragmentFunction(PyObject dispatchInstance)
	{
		this.dispatchInstance = dispatchInstance;
	}
	

	@Override
	public Pres createViewFragment(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		if ( !( x instanceof DMObject ) )
		{
			throw new RuntimeException( "DMObjectNodeDispatchViewFragmentFunction can only present DMObject instances - not instances of " + x.getClass().getName() );
		}
		String name[] = { "" };
		PyObject pyPres = ObjectPyMethodDispatch.objectMethodDispatchAndGetNameFromJava( dispatchInstance, x, new Object[] { fragment, inheritedState }, name );
		Pres pres = Py.tojava( pyPres, Pres.class );
		return pres.setDebugName( name[0] );
	}
}
