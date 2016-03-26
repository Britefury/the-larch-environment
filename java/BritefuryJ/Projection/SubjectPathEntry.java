//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Projection;

import org.python.core.*;


public abstract class SubjectPathEntry extends AbstractSubjectPathEntry
{
	public boolean canPersist()
	{
		return true;
	}


	public abstract PyObject __getstate__();

	public abstract void __setstate__(PyObject state);

	public PyObject __reduce__()
	{
		return new PyTuple( Py.java2py( this ).getType(), new PyTuple(), __getstate__() );
	}
}
