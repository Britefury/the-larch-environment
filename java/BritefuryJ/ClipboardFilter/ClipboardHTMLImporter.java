//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.ClipboardFilter;

import net.htmlparser.jericho.Segment;

import org.python.core.PyObject;
import org.python.core.PyType;

public class ClipboardHTMLImporter extends ClipboardImporter<Segment>
{
	public ClipboardHTMLImporter()
	{
		super( "__import_from_html_segment__" );
	}
	

	@Override
	protected Object defaultImportJava(Class<?> cls, Segment importData)
	{
		return null;
	}

	@Override
	protected PyObject defaultImportPython(PyType type, PyObject importData)
	{
		return null;
	}
	
	
	
	public static final ClipboardHTMLImporter instance = new ClipboardHTMLImporter();
}
