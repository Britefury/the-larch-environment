//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Clipboard;

import java.awt.datatransfer.Transferable;

import javax.swing.TransferHandler;

import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.Target.Target;


public abstract class ClipboardHandler
{
	public static int COPY = TransferHandler.COPY;
	public static int COPY_OR_MOVE = TransferHandler.COPY_OR_MOVE;
	public static int LINK = TransferHandler.LINK;
	public static int MOVE = TransferHandler.MOVE;
	public static int NONE = TransferHandler.NONE;
	
	
	public abstract void deleteSelection(Selection selection, Target target);
	public abstract void replaceSelectionWithText(Selection selection, Target target, String replacement);
	

	public abstract int getExportActions(Selection selection);
	public abstract Transferable createExportTransferable(Selection selection);
	public abstract void exportDone(Selection selection, Target target, Transferable transferable, int action);
	
	public abstract boolean canImport(Target target, Selection selection, DataTransfer dataTransfer);
	public abstract boolean importData(Target target, Selection selection, DataTransfer dataTransfer);
	
	
	public boolean canShareSelectionWith(ClipboardHandler clipboardHandler)
	{
		return false;
	}
}
