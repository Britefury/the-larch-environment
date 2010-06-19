//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Clipboard;

import java.awt.datatransfer.Transferable;

import javax.swing.TransferHandler;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Selection.Selection;


public interface EditHandler
{
	public static int COPY = TransferHandler.COPY;
	public static int COPY_OR_MOVE = TransferHandler.COPY_OR_MOVE;
	public static int LINK = TransferHandler.LINK;
	public static int MOVE = TransferHandler.MOVE;
	public static int NONE = TransferHandler.NONE;
	
	
	public void deleteSelection(Selection selection);
	public void replaceSelectionWithText(Selection selection, Caret caret, String replacement);
	

	public int getExportActions(Selection selection);
	public Transferable createExportTransferable(Selection selection);
	public void exportDone(Selection selection, Transferable transferable, int action);
	
	public boolean canImport(Caret caret, Selection selection, DataTransfer dataTransfer);
	public boolean importData(Caret caret, Selection selection, DataTransfer dataTransfer);
	
	
	public boolean canShareSelectionWith(EditHandler editHandler);
}
