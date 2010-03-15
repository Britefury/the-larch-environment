//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.TransferHandler;

public class DataTransfer
{
	private TransferHandler.TransferSupport support;
	
	
	
	public DataTransfer(TransferHandler.TransferSupport support)
	{
		this.support = support;
	}
	
	
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return support.isDataFlavorSupported( flavor );
	}
	
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		return support.getTransferable().getTransferData( flavor );
	}
}
