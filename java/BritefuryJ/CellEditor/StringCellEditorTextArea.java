//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.CellEditor;

import BritefuryJ.Cell.LiteralCell;
import BritefuryJ.DocPresent.Controls.ControlsStyleSheet;
import BritefuryJ.DocPresent.Controls.TextArea;

public class StringCellEditorTextArea extends LiteralCellEditor
{
	protected class StringEditor extends Editor
	{
		protected class Listener extends TextArea.TextAreaListener
		{
			@Override
			public void onAccept(TextArea textArea, String text)
			{
				setCellValue( text );
			}
		}
		
		private TextArea textArea;
		
		
		
		public StringEditor()
		{
			String value = getCellValue( String.class );
			String text = value != null  ?  value  :  "";
			textArea = styleSheet.textArea( text, new Listener() );
			if ( value != null )
			{
				setElement( textArea.getElement() );
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
				textArea.setText( text );
			}
			else
			{
				error( "not a string" );
			}
		}
	};
	
	
	private ControlsStyleSheet styleSheet;
	
	
	public StringCellEditorTextArea(LiteralCell cell, ControlsStyleSheet styleSheet)
	{
		super( cell );
		this.styleSheet = styleSheet;
	}
	
	
	protected Editor createEditor()
	{
		return new StringEditor();
	}
}
