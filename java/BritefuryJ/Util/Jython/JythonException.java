//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Util.Jython;

import org.python.core.Py;
import org.python.core.PyException;

import BritefuryJ.Pres.Pres;

public class JythonException
{
	public static PyException getCurrentException()
	{
		return Py.getThreadState().exception;
	}
	
	public static Pres presentCurrentException()
	{
		return Pres.coerce( getCurrentException() );
	}
}
