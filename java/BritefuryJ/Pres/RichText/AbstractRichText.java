//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.RichText;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.LineBreak;
import BritefuryJ.Pres.Primitive.Text;

public abstract class AbstractRichText extends Pres
{
	private Object contents[];


	public AbstractRichText(Object contents[])
	{
		this.contents = new Object[contents.length];
		for (int i = 0; i < contents.length; i++)
		{
			Object x = contents[i];
			if ( x instanceof CharSequence )
			{
				this.contents[i] = x;
			}
			else
			{
				this.contents[i] = Pres.coerce( x );
			}
		}
	}
	
	public AbstractRichText(List<Object> contents)
	{
		int numItems = contents.size();
		this.contents = new Object[numItems];
		for (int i = 0; i < numItems; i++)
		{
			Object x = contents.get( i );
			if ( x instanceof CharSequence )
			{
				this.contents[i] = x;
			}
			else
			{
				this.contents[i] = Pres.coerce( x );
			}
		}
	}
	
	public AbstractRichText(String text)
	{
		if ( text.length() == 0 )
		{
			contents = new Object[0];
		}
		else
		{
			contents = new Object[] { text };
		}
	}

	
	protected boolean isEmpty()
	{
		return contents.length == 0;
	}
	
	
	protected ArrayList<Object> splitContents()
	{
		ArrayList<Object> result = new ArrayList<Object>();
	
		for (Object x: contents)
		{
			if ( x instanceof CharSequence )
			{
				String sx = (String)x;
				// Handle text
				int start = 0;
				int i = 0;
				
				// Leading whitespace
				while ( i < sx.length()  &&  sx.charAt( i ) == ' ' )
				{
					i++;
				}
				if ( i > start )
				{
					result.add( new Text( sx.substring( start, i ) ) );
					result.add( new LineBreak() );
				}
				
				while ( i < sx.length() )
				{
					int j = i;
					boolean gotWhitespace = false;
					
					// Consume word characters
					while ( j < sx.length()  &&  sx.charAt( j ) != ' ' )
					{
						j++;
					}
					
					// Consume whitespace
					while ( j < sx.length()  &&  sx.charAt( j ) == ' ' )
					{
						j++;
						gotWhitespace = true;
					}
					
					if ( j > i )
					{
						result.add( new Text( sx.substring( i, j ) ) );
						if ( gotWhitespace )
						{
							result.add( new LineBreak() );
						}
						i = j;
					}
				}
			}
			else
			{
				result.add( Pres.coerce( x ) );
			}
		}
		
		return result;
	}

	public AbstractRichText()
	{
		super();
	}

}