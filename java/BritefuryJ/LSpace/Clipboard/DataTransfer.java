//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Clipboard;

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
	
	public DataFlavor[] getDataFlavors()
	{
		return support.getDataFlavors();
	}
	
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		return support.getTransferable().getTransferData( flavor );
	}
}
