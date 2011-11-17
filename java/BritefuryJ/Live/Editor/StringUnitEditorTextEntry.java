//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Live.Editor;

import BritefuryJ.Controls.TextEntry;
import BritefuryJ.Live.LiveValue;

public class StringUnitEditorTextEntry extends LiteralUnitEditor
{
	protected class StringEditor extends Editor
	{
		protected class Listener extends TextEntry.TextEntryListener
		{
			@Override
			public void onAccept(TextEntry.TextEntryControl textEntry, String text)
			{
				setUnitValue( text );
			}
		}
		
		
		public StringEditor()
		{
			String value = getUnitValue( String.class );
			String text = value != null  ?  value  :  "";
			if ( value != null )
			{
				setPres( new TextEntry( text, new Listener() ) );
			}
			else
			{
				error( "not a string" );
			}
		}
		
		
		protected void refreshEditor()
		{
			String text = getUnitValue( String.class );
			if ( text != null )
			{
				setPres( new TextEntry( text, new Listener() ) );
			}
			else
			{
				error( "not a string" );
			}
		}
	}


	public StringUnitEditorTextEntry(LiveValue cell)
	{
		super( cell );
	}
	
	
	protected Editor createEditor()
	{
		return new StringEditor();
	}
}
