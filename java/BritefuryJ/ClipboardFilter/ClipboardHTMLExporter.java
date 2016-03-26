//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
