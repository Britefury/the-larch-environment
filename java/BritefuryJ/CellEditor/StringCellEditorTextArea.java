//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.CellEditor;

import BritefuryJ.Cell.LiteralCell;
import BritefuryJ.DocPresent.DPElement;
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
				bResponding = true;
				setCellValue( text );
				bResponding = false;
			}
		}
		
		private TextArea textArea;
		private boolean bResponding = false;
		
		public StringEditor()
		{
			String text = getCellValue( String.class );
			textArea = styleSheet.textArea( text != null  ?  text  :  "<not a string>", new Listener() );
		}
		
		protected void onCellChanged()
		{
			if ( !bResponding )
			{
				String text = getCellValue( String.class );
				textArea.setText( text != null  ?  text  :  "<not a string>" );
			}
		}
		
		protected DPElement getElement()
		{
			return textArea.getElement();
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
