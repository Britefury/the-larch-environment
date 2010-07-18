//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.CellEditor;

import BritefuryJ.Cell.LiteralCell;
import BritefuryJ.DocPresent.Controls.ControlsStyleSheet;
import BritefuryJ.DocPresent.Controls.TextEntry;

public class StringCellEditorTextEntry extends LiteralCellEditor
{
	protected class StringEditor extends Editor
	{
		protected class Listener extends TextEntry.TextEntryListener
		{
			@Override
			public void onAccept(TextEntry textEntry, String text)
			{
				setCellValue( text );
			}
		}
		
		private TextEntry textEntry;
		
		
		
		public StringEditor()
		{
			String value = getCellValue( String.class );
			String text = value != null  ?  value  :  "";
			textEntry = styleSheet.textEntry( text, new Listener() );
			if ( value != null )
			{
				setElement( textEntry.getElement() );
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
				textEntry.setText( text );
			}
			else
			{
				error( "not a string" );
			}
		}
	};
	
	
	private ControlsStyleSheet styleSheet;
	
	
	public StringCellEditorTextEntry(LiteralCell cell, ControlsStyleSheet styleSheet)
	{
		super( cell );
		this.styleSheet = styleSheet;
	}
	
	
	protected Editor createEditor()
	{
		return new StringEditor();
	}
}
