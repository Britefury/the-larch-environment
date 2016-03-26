//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;

import BritefuryJ.LSpace.Focus.Selection;

class SelectionContentsTransferable<SelectionContentsType, SelectionType extends Selection> implements Transferable
{
	protected final AbstractSelectionExporter<SelectionContentsType, SelectionType> selectionExporter;
	protected SelectionContentsType selectionContents;
	private ArrayList<DataFlavor> flavors = new ArrayList<DataFlavor>();
	
	
	protected SelectionContentsTransferable(AbstractSelectionExporter<SelectionContentsType, SelectionType> selectionExporter, SelectionContentsType selectionContents, SelectionType selection)
	{
		this.selectionExporter = selectionExporter;
		this.selectionContents = selectionContents;
		
		for (DataExporterInterface<SelectionContentsType> exporter: selectionExporter.getExporters())
		{
			flavors.addAll( exporter.getTransferDataFlavors( selectionContents ) );
		}
	}
	
	
	public DataFlavor[] getTransferDataFlavors()
	{
		return flavors.toArray( new DataFlavor[flavors.size()] );
	}

	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return flavors.contains( flavor );
	}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		for (DataExporterInterface<SelectionContentsType> exporter: selectionExporter.getExporters())
		{
			if ( exporter.getTransferDataFlavors( selectionContents ).contains( flavor ) )
			{
				return exporter.getTransferData( selectionContents, flavor );
			}
		}
		
		throw new UnsupportedFlavorException( flavor );
	}
}