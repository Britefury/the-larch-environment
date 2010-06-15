//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.Selection;

public abstract class TextEditHandler implements EditHandler
{
	protected abstract void deleteText(Selection selection);
	protected abstract void insertText(Marker marker, String text);
	protected abstract void replaceText(Selection selection, String replacement);
	protected abstract String getText(Selection selection);
	
	
	public void deleteSelection(Selection selection)
	{
		if ( !selection.isEmpty() )
		{
			deleteText( selection );
		}
	}

	public void replaceSelectionWithText(Selection selection, Caret caret, String replacement)
	{
		if ( !selection.isEmpty() )
		{
			replaceText( selection, replacement );
		}
		else
		{
			insertText( caret.getMarker(), replacement );
		}
	}



	public int getExportActions(Selection selection)
	{
		return COPY_OR_MOVE;
	}

	public Transferable createExportTransferable(Selection selection)
	{
		if ( !selection.isEmpty() )
		{
			String selectedText = getText( selection );
			return new StringSelection( selectedText );
		}
		else
		{
			return null;
		}
	}

	public void exportDone(Selection selection, Transferable transferable, int action)
	{
		if ( action == MOVE )
		{
			if ( !selection.isEmpty() )
			{
				deleteText( selection );
			}
		}
	}

	
	public boolean canImport(Caret caret, Selection selection, DataTransfer dataTransfer)
	{
		return dataTransfer.isDataFlavorSupported( DataFlavor.stringFlavor );
	}

	public boolean importData(Caret caret, Selection selection, DataTransfer dataTransfer)
	{
		if ( canImport( caret, selection, dataTransfer ) )
		{
			try
			{
				String data = (String)dataTransfer.getTransferData( DataFlavor.stringFlavor );
				
				if ( !selection.isEmpty() )
				{
					replaceText( selection, data );
				}
				else
				{
					insertText( caret.getMarker(), data );
				}
			}
			catch (UnsupportedFlavorException e)
			{
				return false;
			}
			catch (IOException e)
			{
				return false;
			}
		}
		
		return false;
	}
}
