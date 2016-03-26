//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Dispatch;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.IncrementalView.ViewFragmentFunction;
import BritefuryJ.Pres.Pres;

public class PyMethodDispatchViewFragmentFunction extends DispatchViewFragmentFunction implements ViewFragmentFunction
{
	private PyObject dispatchInstance;
	
	
	public PyMethodDispatchViewFragmentFunction(PyObject dispatchInstance)
	{
		this.dispatchInstance = dispatchInstance;
	}
	

	@Override
	public Pres createViewFragment(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		String name[] = { "" };
		long startTime = profile_start();
		PyObject pyPres = PyMethodDispatch.methodDispatchAndGetNameFromJava( dispatchInstance, x, new Object[] { fragment, inheritedState }, name );
		profile_stop( name[0], startTime );
		Pres pres = Py.tojava( pyPres, Pres.class );
		return pres.setDebugName( name[0] );
	}
}
