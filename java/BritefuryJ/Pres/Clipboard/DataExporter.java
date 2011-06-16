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

import BritefuryJ.DocPresent.Clipboard.LocalDataFlavor;
import BritefuryJ.DocPresent.Selection.Selection;

public class DataExporter<SelectionContentsType, SelectionType extends Selection> extends DataExporterInterface<SelectionContentsType, SelectionType>
{
	public interface CanExportFn <SelectionContentsType>
	{
		public boolean canExport(SelectionContentsType selectionContents);
	}
	
	public interface ExportFn <SelectionContentsType, SelectionType extends Selection>
	{
		public Object export(SelectionContentsType selectionContents, SelectionType selection);
	}
	
	
	
	private DataFlavor flavor;
	private CanExportFn<SelectionContentsType> canExportFn;
	private ExportFn<SelectionContentsType, SelectionType> exportFn;
	
	public DataExporter(DataFlavor flavor, ExportFn<SelectionContentsType, SelectionType> exportFn, CanExportFn<SelectionContentsType> canExportFn)
	{
		this.flavor = flavor;
		this.exportFn = exportFn;
		this.canExportFn = canExportFn;
	}
	
	public DataExporter(DataFlavor flavor, ExportFn<SelectionContentsType, SelectionType> exportFn)
	{
		this( flavor, exportFn, null );
	}
	
	public DataExporter(Class<?> type, ExportFn<SelectionContentsType, SelectionType> exportFn, CanExportFn<SelectionContentsType> canExportFn)
	{
		this( new LocalDataFlavor( type ), exportFn, canExportFn );
	}
	
	public DataExporter(Class<?> type, ExportFn<SelectionContentsType, SelectionType> exportFn)
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

	protected Object getTransferData(SelectionContentsType selectionContents, SelectionType selection, DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		if ( canExportFn == null  ||  canExportFn.canExport( selectionContents ) )
		{
			if ( flavor.equals( this.flavor ) )
			{
				return exportFn.export( selectionContents, selection );
			}
		}
		
		throw new UnsupportedFlavorException( flavor );
	}
	
	
	
	public static <SelectionContentsType, SelectionType extends Selection> DataExporter<SelectionContentsType, SelectionType>
			stringExporter(ExportFn<SelectionContentsType, SelectionType> exportFn, CanExportFn<SelectionContentsType> canExportFn)
	{
		return new DataExporter<SelectionContentsType, SelectionType>( DataFlavor.stringFlavor, exportFn, canExportFn );
	}

	public static <SelectionContentsType, SelectionType extends Selection> DataExporter<SelectionContentsType, SelectionType>
			stringExporter(ExportFn<SelectionContentsType, SelectionType> exportFn)
	{
		return new DataExporter<SelectionContentsType, SelectionType>( DataFlavor.stringFlavor, exportFn );
	}
}
