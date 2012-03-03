//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.LSpace.Clipboard.LocalDataFlavor;

public class DataExporter<SelectionContentsType> extends DataExporterInterface<SelectionContentsType>
{
	public interface CanExportFn <SelectionContentsType>
	{
		public boolean canExport(SelectionContentsType selectionContents);
	}
	
	public interface ExportFn <SelectionContentsType>
	{
		// DO NOT CHANGE BY ADDING THE SELECTION AS A PARAMETER
		// Made this change once previously, but selections or their contents may be modified by user action between the time of a copy/cut, and a paste action.
		// If you need to add context information here, implement a new SelectionContentsType that contains all the context that is needed.
		public Object export(SelectionContentsType selectionContents);
	}
	
	
	
	private DataFlavor flavor;
	private CanExportFn<SelectionContentsType> canExportFn;
	private ExportFn<SelectionContentsType> exportFn;
	
	public DataExporter(DataFlavor flavor, ExportFn<SelectionContentsType> exportFn, CanExportFn<SelectionContentsType> canExportFn)
	{
		this.flavor = flavor;
		this.exportFn = exportFn;
		this.canExportFn = canExportFn;
	}
	
	public DataExporter(DataFlavor flavor, ExportFn<SelectionContentsType> exportFn)
	{
		this( flavor, exportFn, null );
	}
	
	public DataExporter(Class<?> type, ExportFn<SelectionContentsType> exportFn, CanExportFn<SelectionContentsType> canExportFn)
	{
		this( new LocalDataFlavor( type ), exportFn, canExportFn );
	}
	
	public DataExporter(Class<?> type, ExportFn<SelectionContentsType> exportFn)
	{
		this( new LocalDataFlavor( type ), exportFn );
	}
	
	
	static ArrayList<DataFlavor> emptyFlavorList = new ArrayList<DataFlavor>();
	
	protected List<DataFlavor> getTransferDataFlavors(SelectionContentsType selectionContents)
	{
		if ( canExportFn == null  ||  canExportFn.canExport( selectionContents ) )
		{
			return Arrays.asList( flavor );
		}
		else
		{
			return emptyFlavorList;
		}
	}

	protected Object getTransferData(SelectionContentsType selectionContents, DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		if ( canExportFn == null  ||  canExportFn.canExport( selectionContents ) )
		{
			if ( flavor.equals( this.flavor ) )
			{
				return exportFn.export( selectionContents );
			}
		}
		
		throw new UnsupportedFlavorException( flavor );
	}
	
	
	
	public static <SelectionContentsType> DataExporter<SelectionContentsType>
			stringExporter(ExportFn<SelectionContentsType> exportFn, CanExportFn<SelectionContentsType> canExportFn)
	{
		return new DataExporter<SelectionContentsType>( DataFlavor.stringFlavor, exportFn, canExportFn );
	}

	public static <SelectionContentsType> DataExporter<SelectionContentsType>
			stringExporter(ExportFn<SelectionContentsType> exportFn)
	{
		return new DataExporter<SelectionContentsType>( DataFlavor.stringFlavor, exportFn );
	}
}
