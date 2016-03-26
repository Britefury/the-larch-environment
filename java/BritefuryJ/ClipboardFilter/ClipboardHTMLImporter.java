//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.ClipboardFilter;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.python.core.PyObject;
import org.python.core.PyType;

public class ClipboardHTMLImporter extends ClipboardImporter<Element>
{
	public ClipboardHTMLImporter()
	{
		super( "__import_from_html_segment__" );
	}
	

	@Override
	protected Object defaultImportJava(Class<?> cls, Element importData)
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
