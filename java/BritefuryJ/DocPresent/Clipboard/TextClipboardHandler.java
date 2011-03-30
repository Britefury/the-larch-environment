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
import BritefuryJ.DocPresent.Selection.TextSelection;

public abstract class TextClipboardHandler extends ClipboardHandler
{
	protected abstract void deleteText(TextSelection selection, Caret caret);
	protected abstract void insertText(Marker marker, String text);
	protected abstract void replaceText(TextSelection selection, Caret caret, String replacement);
	protected abstract String getText(TextSelection selection);
	
	
	@Override
	public void deleteSelection(Selection selection, Caret caret)
	{
		if ( selection instanceof TextSelection )
		{
			TextSelection ts = (TextSelection)selection;
			deleteText( ts, caret );
		}
	}

	@Override
	public void replaceSelectionWithText(Selection selection, Caret caret, String replacement)
	{
		if ( selection instanceof TextSelection )
		{
			TextSelection ts = (TextSelection)selection;
			replaceText( ts, caret, replacement );
			ts.clear();
		}
		else
		{
			caret.moveToStartOfNextItem();
			insertText( caret.getMarker(), replacement );
		}
	}



	@Override
	public int getExportActions(Selection selection)
	{
		return COPY_OR_MOVE;
	}

	@Override
	public Transferable createExportTransferable(Selection selection)
	{
		if ( selection instanceof TextSelection )
		{
			TextSelection ts = (TextSelection)selection;
			String selectedText = getText( ts );
			return new StringSelection( selectedText );
		}
		return null;
	}

	@Override
	public void exportDone(Selection selection, Caret caret, Transferable transferable, int action)
	{
		if ( action == MOVE )
		{
			if ( selection instanceof TextSelection )
			{
				TextSelection ts = (TextSelection)selection;
				deleteText( ts, caret );
				ts.clear();
			}
		}
	}

	
	@Override
	public boolean canImport(Caret caret, Selection selection, DataTransfer dataTransfer)
	{
		return dataTransfer.isDataFlavorSupported( DataFlavor.stringFlavor );
	}

	@Override
	public boolean importData(Caret caret, Selection selection, DataTransfer dataTransfer)
	{
		TextSelection ts = null;
		if ( selection instanceof TextSelection )
		{
			ts = (TextSelection)selection;
		}
		
		if ( canImport( caret, selection, dataTransfer ) )
		{
			try
			{
				String data = (String)dataTransfer.getTransferData( DataFlavor.stringFlavor );
				
				if ( ts != null )
				{
					replaceText( ts, caret, data );
					ts.clear();
				}
				else
				{
					caret.moveToStartOfNextItem();
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
