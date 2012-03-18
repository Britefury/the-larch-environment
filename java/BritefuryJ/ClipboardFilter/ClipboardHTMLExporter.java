//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.ClipboardFilter;


public class ClipboardHTMLExporter extends ClipboardExporter
{
	protected ClipboardHTMLExporter()
	{
		super( "__export_to_html__" );
	}

	
	@Override
	protected Object exportWithJavaInterface(Object x)
	{
		if ( x instanceof ExportableToHTML )
		{
			ExportableToHTML plainText = (ExportableToHTML)x;
			return plainText.exportToHTML();
		}
		else
		{
			return null;
		}
	}

	@Override
	protected Object defaultExport(Object x)
	{
		return null;
	}


	public static final ClipboardHTMLExporter instance = new ClipboardHTMLExporter();
}
