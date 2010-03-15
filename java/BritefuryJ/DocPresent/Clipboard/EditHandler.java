//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Clipboard;

import java.awt.datatransfer.Transferable;

import javax.swing.TransferHandler;


public interface EditHandler
{
	public static int COPY = TransferHandler.COPY;
	public static int COPY_OR_MOVE = TransferHandler.COPY_OR_MOVE;
	public static int LINK = TransferHandler.LINK;
	public static int MOVE = TransferHandler.MOVE;
	public static int NONE = TransferHandler.NONE;
	
	
	void deleteSelection();
	void replaceSelection(String replacement);
	

	public int getExportActions();
	public Transferable createExportTransferable();
	public void exportDone(Transferable transferable, int action);
	
	public boolean canImport(DataTransfer dataTransfer);
	public boolean importData(DataTransfer dataTransfer);
}
