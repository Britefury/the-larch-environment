//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;

import BritefuryJ.DocPresent.Selection.Selection;

class SelectionContentsTransferable<SelectionContentsType extends Object, SelectionType extends Selection> implements Transferable
{
	protected final AbstractSelectionExporter<SelectionContentsType, SelectionType> selectionExporter;
	protected SelectionContentsType selectionContents;
	private ArrayList<DataFlavor> flavors = new ArrayList<DataFlavor>();
	
	
	protected SelectionContentsTransferable(AbstractSelectionExporter<SelectionContentsType, SelectionType> selectionExporter, SelectionContentsType selectionContents)
	{
		this.selectionExporter = selectionExporter;
		this.selectionContents = selectionContents;
		
		for (DataExporterInterface<SelectionContentsType> exporter: selectionExporter.getExporters())
		{
			flavors.addAll( exporter.getTransferDataFlavors( selectionContents ) );
		}
	}
	
	
	@Override
	public DataFlavor[] getTransferDataFlavors()
	{
		return flavors.toArray( new DataFlavor[] {} );
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return flavors.contains( flavor );
	}

	@Override
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