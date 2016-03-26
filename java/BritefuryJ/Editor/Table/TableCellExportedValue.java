//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
