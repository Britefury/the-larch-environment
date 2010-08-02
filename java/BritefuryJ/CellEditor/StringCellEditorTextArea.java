//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.CellEditor;

import BritefuryJ.Cell.LiteralCell;
import BritefuryJ.Controls.TextArea;

public class StringCellEditorTextArea extends LiteralCellEditor
{
	protected class StringEditor extends Editor
	{
		protected class Listener extends TextArea.TextAreaListener
		{
			@Override
			public void onAccept(TextArea.TextAreaControl textArea, String text)
			{
				setCellValue( text );
			}
		}
		

		public StringEditor()
		{
			String value = getCellValue( String.class );
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
			String text = getCellValue( String.class );
			if ( text != null )
			{
				setPres( new TextArea( text, new Listener() ) );
			}
			else
			{
				error( "not a string" );
			}
		}
	};
	
	
	public StringCellEditorTextArea(LiteralCell cell)
	{
		super( cell );
	}
	
	
	protected Editor createEditor()
	{
		return new StringEditor();
	}
}
