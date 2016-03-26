//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.ClipboardFilter;

import java.awt.Color;

import org.python.core.PyObject;
import org.python.core.__builtin__;

public class ClipboardPlainTextExporter extends ClipboardExporter
{
	protected ClipboardPlainTextExporter()
	{
		super( "__export_to_plain_text__" );
	}

	
	@Override
	protected Object exportWithJavaInterface(Object x)
	{
		if ( x instanceof ExportableToPlainText )
		{
			ExportableToPlainText plainText = (ExportableToPlainText)x;
			return plainText.exportToPlainText();
		}
		else
		{
			return null;
		}
	}

	@Override
	protected Object defaultExport(Object x)
	{
		if ( x instanceof PyObject )
		{
			PyObject pyX = (PyObject)x;
			return __builtin__.repr( pyX ).toString();
		}
		else
		{
			return x.toString();
		}
	}
	
	
	public static final ClipboardPlainTextExporter instance = new ClipboardPlainTextExporter();
	
	
	
	static
	{
		ObjectClipboardExporter awtColorExporter = new ObjectClipboardExporter()
		{
			@Override
			public Object exportObject(Object x)
			{
				Color c = (Color)x;
				return String.format( "#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue() );
			}
		};
		
		
		instance.registerJavaObjectExporter( Color.class, awtColorExporter );
	}
}
