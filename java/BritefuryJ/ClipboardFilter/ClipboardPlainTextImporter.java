//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.ClipboardFilter;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.python.core.PyObject;
import org.python.core.PyType;

public class ClipboardPlainTextImporter extends ClipboardImporter<String>
{
	public ClipboardPlainTextImporter()
	{
		super( "__import_from_plain_text__" );
	}

	
	@Override
	protected Object defaultImportJava(Class<?> cls, String importData)
	{
		try
		{
			Constructor<?> cons = cls.getConstructor( String.class );
			return cons.newInstance( importData );
		}
		catch (SecurityException e)
		{
			return null;
		}
		catch (NoSuchMethodException e)
		{
			return null;
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
		catch (InstantiationException e)
		{
			return null;
		}
		catch (IllegalAccessException e)
		{
			return null;
		}
		catch (InvocationTargetException e)
		{
			return null;
		}
	}

	
	@Override
	protected PyObject defaultImportPython(PyType type, PyObject importData)
	{
		try
		{
			return type.__call__( importData );
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			return null;
		}
	}
	
	
	
	public static final ClipboardPlainTextImporter instance = new ClipboardPlainTextImporter();
	
	
	
	static
	{
		ObjectClipboardImporter<String> awtColorImporter = new ObjectClipboardImporter<String>()
		{
			@Override
			public Object importObject(String importData)
			{
				if ( importData.charAt( 0 ) == '#' )
				{
					int red = Integer.parseInt( importData.substring( 1, 3 ), 16 );
					int green = Integer.parseInt( importData.substring( 3, 5 ), 16 );
					int blue = Integer.parseInt( importData.substring( 5, 7 ), 16 );
					return new Color( red, green, blue );
				}
				else
				{
					return Color.BLACK;
				}
			}
		};
		
		
		instance.registerJavaObjectImporter( Color.class, awtColorImporter );
	}
}
