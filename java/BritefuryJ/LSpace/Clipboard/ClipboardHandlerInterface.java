//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Clipboard;

import java.awt.datatransfer.Transferable;

import javax.swing.TransferHandler;

import BritefuryJ.LSpace.Focus.Selection;
import BritefuryJ.LSpace.Focus.Target;


public abstract class ClipboardHandlerInterface
{
	public static int COPY = TransferHandler.COPY;
	public static int COPY_OR_MOVE = TransferHandler.COPY_OR_MOVE;
	public static int LINK = TransferHandler.LINK;
	public static int MOVE = TransferHandler.MOVE;
	public static int NONE = TransferHandler.NONE;
	
	
	public abstract boolean deleteSelection(Selection selection, Target target);
	public abstract boolean replaceSelectionWithText(Selection selection, Target target, String replacement);
	

	public abstract int getExportActions(Selection selection);
	public abstract Transferable createExportTransferable(Selection selection);
	public abstract void exportDone(Selection selection, Target target, Transferable transferable, int action);
	
	public abstract boolean canImport(Target target, Selection selection, DataTransfer dataTransfer);
	public abstract boolean importData(Target target, Selection selection, DataTransfer dataTransfer);
	
	
	public boolean canShareSelectionWith(ClipboardHandlerInterface clipboardHandler)
	{
		return false;
	}
}
