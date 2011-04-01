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
import BritefuryJ.DocPresent.Target.Target;

public abstract class TextClipboardHandler extends ClipboardHandler
{
	protected abstract void deleteText(TextSelection selection, Caret caret);
	protected abstract void insertText(Marker marker, String text);
	protected abstract void replaceText(TextSelection selection, Caret caret, String replacement);
	protected abstract String getText(TextSelection selection);
	
	
	public Class<? extends Selection> getSelectionClass()
	{
		return TextSelection.class;
	}
	
	public Class<? extends Target> getTargetClass()
	{
		return Caret.class;
	}
	
	
	@Override
	public void deleteSelection(Selection selection, Target target)
	{
		if ( selection instanceof TextSelection  &&  target instanceof Caret )
		{
			TextSelection ts = (TextSelection)selection;
			deleteText( ts, (Caret)target );
		}
	}

	@Override
	public void replaceSelectionWithText(Selection selection, Target target, String replacement)
	{
		if ( target instanceof Caret )
		{
			Caret caret = (Caret)target;
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
	public void exportDone(Selection selection, Target target, Transferable transferable, int action)
	{
		if ( action == MOVE )
		{
			if ( selection instanceof TextSelection  &&  target instanceof Caret )
			{
				TextSelection ts = (TextSelection)selection;
				deleteText( ts, (Caret)target );
				ts.clear();
			}
		}
	}

	
	@Override
	public boolean canImport(Target target, Selection selection, DataTransfer dataTransfer)
	{
		return target instanceof Caret  &&  dataTransfer.isDataFlavorSupported( DataFlavor.stringFlavor );
	}

	@Override
	public boolean importData(Target target, Selection selection, DataTransfer dataTransfer)
	{
		TextSelection ts = null;
		if ( selection instanceof TextSelection )
		{
			ts = (TextSelection)selection;
		}
		
		if ( target instanceof Caret )
		{
			Caret caret = (Caret)target;

			if ( canImport( target, selection, dataTransfer ) )
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
		}
		
		return false;
	}
}
