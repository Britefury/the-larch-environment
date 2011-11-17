//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Live.Editor;

import BritefuryJ.Controls.TextArea;
import BritefuryJ.Live.LiveValue;

public class StringUnitEditorTextArea extends LiteralUnitEditor
{
	protected class StringEditor extends Editor
	{
		protected class Listener extends TextArea.TextAreaListener
		{
			@Override
			public void onAccept(TextArea.TextAreaControl textArea, String text)
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
				setPres( new TextArea( text, new Listener() ) );
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
				setPres( new TextArea( text, new Listener() ) );
			}
			else
			{
				error( "not a string" );
			}
		}
	}


	public StringUnitEditorTextArea(LiveValue cell)
	{
		super( cell );
	}
	
	
	protected Editor createEditor()
	{
		return new StringEditor();
	}
}
