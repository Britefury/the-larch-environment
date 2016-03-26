//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.Table.ObjectList;

import org.jsoup.nodes.Element;
import org.python.core.Py;

import BritefuryJ.Util.UnaryFn;

public abstract class AbstractColumn
{
	public abstract Object get(Object modelRow);
	public abstract void set(Object modelRow, Object value);
	
	
	public abstract Object defaultValue();
	
	public Object importHTML(Element importData)
	{
		return importPlainText( importData.text() );
	}
	
	public abstract Object importPlainText(String importData);
	
	
	public abstract Object presentHeader();
	
	
	protected static AbstractColumn coerce(Object x)
	{
		if ( x instanceof AbstractColumn )
		{
			return (AbstractColumn)x;
		}
		else if ( x instanceof String )
		{
			String sx = (String)x;
			return new AttributeColumn( sx, Py.newString( sx ) );
		}
		else
		{
			throw new RuntimeException( "Could not coerce type " + x.getClass().getName() + " to create an AbstractColumn" );
		}
	}
	
	
	protected UnaryFn createImportFn()
	{
		UnaryFn fn = new UnaryFn()
		{
			public Object invoke(Object x)
			{
				return AbstractColumn.this.importPlainText( (String)x );
			}
			
		};
		return fn;
	}
}
