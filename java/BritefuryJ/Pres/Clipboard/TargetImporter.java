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
import java.util.List;

import BritefuryJ.LSpace.Clipboard.DataTransfer;
import BritefuryJ.LSpace.Focus.Selection;
import BritefuryJ.LSpace.Focus.Target;

public class TargetImporter <TargetType extends Target>
{
	private Class<? extends Target> targetClass;
	protected ArrayList<DataImporterInterface<TargetType>> importers = new ArrayList<DataImporterInterface<TargetType>>();

	
	public TargetImporter(Class<? extends Target> targetClass, List<? extends DataImporterInterface<TargetType>> importers)
	{
		this.targetClass = targetClass;
		this.importers.addAll( importers );
	}


	public Class<? extends Target> getTargetClass()
	{
		return targetClass;
	}
	
	
	@SuppressWarnings("unchecked")
	protected boolean canImport(Target target, Selection selection, DataTransfer dataTransfer)
	{
		for (DataFlavor flavor: dataTransfer.getDataFlavors())
		{
			for (DataImporterInterface<TargetType> importer: importers)
			{
				if ( importer.canImportFlavor( flavor ) )
				{
					try
					{
						if ( importer.canImport( (TargetType)target, selection, dataTransfer, flavor ) )
						{
							return true;
						}
					}
					catch (UnsupportedFlavorException e)
					{
					}
					catch (IOException e)
					{
					}
				}
			}
		}
		
		return false;
	}


	@SuppressWarnings("unchecked")
	protected boolean importData(Target target, Selection selection, DataTransfer dataTransfer)
	{
		for (DataFlavor flavor: dataTransfer.getDataFlavors())
		{
			for (DataImporterInterface<TargetType> importer: importers)
			{
				if ( importer.canImportFlavor( flavor ) )
				{
					try
					{
						if ( importer.importData( (TargetType)target, selection, dataTransfer, flavor ) )
						{
							return true;
						}
					}
					catch (UnsupportedFlavorException e)
					{
					}
					catch (IOException e)
					{
					}
				}
			}
		}
		
		return false;
	}
}
