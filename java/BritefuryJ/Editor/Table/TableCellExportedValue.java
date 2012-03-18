//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table;

import BritefuryJ.ClipboardFilter.ClipboardHTMLExporter;
import BritefuryJ.ClipboardFilter.ClipboardPlainTextExporter;

public class TableCellExportedValue
{
	private String plainText, html;
	
	
	public TableCellExportedValue(Object x)
	{
		plainText = (String)ClipboardPlainTextExporter.instance.exportObject( x );
		html = (String)ClipboardHTMLExporter.instance.exportObject( x );
		
		if ( html == null )
		{
			html = plainText;
		}
	}
	
	
	protected String getPlainText()
	{
		return plainText;
	}
	
	protected String getHtml()
	{
		return html;
	}
}
