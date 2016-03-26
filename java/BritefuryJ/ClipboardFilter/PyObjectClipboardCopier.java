//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.ClipboardFilter;

import org.python.core.PyObject;

public interface PyObjectClipboardCopier
{
	public PyObject copyObject(PyObject x, ClipboardCopierMemo memo);
}
