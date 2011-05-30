//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Clipboard;

import java.awt.datatransfer.Transferable;
import java.util.List;

import BritefuryJ.DocPresent.Clipboard.ClipboardHandlerInterface;
import BritefuryJ.DocPresent.Clipboard.DataTransfer;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.Target.Target;
import BritefuryJ.Util.PolymorphicMap;

public class ClipboardHandler extends ClipboardHandlerInterface
{
	private PolymorphicMap<AbstractSelectionExporter<?,?>> exporters = null;
	private PolymorphicMap<TargetImporter<?>> importers = null;
	private PolymorphicMap<SelectionEditorInterface> selectionEditors = null;
	
	
	
	public ClipboardHandler()
	{
	}

	public ClipboardHandler(List<AbstractSelectionExporter<?,?>> exporters, List<TargetImporter<?>> importers, List<SelectionEditorInterface> selectionEditors)
	{
		if ( exporters != null  &&  exporters.size() > 0 )
		{
			this.exporters = new PolymorphicMap<AbstractSelectionExporter<?,?>>();
			for (AbstractSelectionExporter<?,?> exporter: exporters)
			{
				this.exporters.put( exporter.getSelectionClass(), exporter );
			}
		}

		if ( importers != null  &&  importers.size() > 0 )
		{
			this.importers = new PolymorphicMap<TargetImporter<?>>();
			for (TargetImporter<?> importer: importers)
			{
				this.importers.put( importer.getTargetClass(), importer );
			}
		}

		if ( selectionEditors != null  &&  selectionEditors.size() > 0 )
		{
			this.selectionEditors = new PolymorphicMap<SelectionEditorInterface>();
			for (SelectionEditorInterface selectionEditor: selectionEditors)
			{
				this.selectionEditors.put( selectionEditor.getSelectionClass(), selectionEditor );
			}
		}
	}
	
	
	public void addExporter(AbstractSelectionExporter<?,?> exporter)
	{
		if ( exporters == null )
		{
			exporters = new PolymorphicMap<AbstractSelectionExporter<?,?>>();
		}
		exporters.put( exporter.getSelectionClass(), exporter );
	}
	
	public void addImporter(TargetImporter<?> importer)
	{
		if ( importers == null )
		{
			importers = new PolymorphicMap<TargetImporter<?>>();
		}
		importers.put( importer.getTargetClass(), importer );
	}
	
	public void addSelectionEditor(SelectionEditorInterface selectionEditor)
	{
		if ( selectionEditors == null )
		{
			selectionEditors = new PolymorphicMap<SelectionEditorInterface>();
		}
		selectionEditors.put( selectionEditor.getSelectionClass(), selectionEditor );
	}
	
	
	@Override
	public boolean deleteSelection(Selection selection, Target target)
	{
		SelectionEditorInterface selectionEditor = getSelectionEditorForSelection( selection );
		
		return selectionEditor != null && selectionEditor.deleteSelection( selection, target );
	}

	@Override
	public boolean replaceSelectionWithText(Selection selection, Target target, String replacement)
	{
		SelectionEditorInterface selectionEditor = getSelectionEditorForSelection( selection );
		
		return selectionEditor != null && selectionEditor.replaceSelectionWithText( selection, target, replacement );
	}

	@Override
	public int getExportActions(Selection selection)
	{
		AbstractSelectionExporter<?,?> exporter = getExporterForSelection( selection );
		
		return exporter != null  ?  exporter.getActions()  :  0;
	}

	@Override
	public Transferable createExportTransferable(Selection selection)
	{
		AbstractSelectionExporter<?,?> exporter = getExporterForSelection( selection );
		
		if ( exporter != null )
		{
			return exporter.createExportTransferable( selection );
		}
		else
		{
			return null;
		}
	}

	@Override
	public void exportDone(Selection selection, Target target, Transferable transferable, int action)
	{
		AbstractSelectionExporter<?,?> exporter = getExporterForSelection( selection );
		
		if ( exporter != null )
		{
			exporter.exportTransferableDone( selection, target, transferable, action );
		}
	}

	@Override
	public boolean canImport(Target target, Selection selection, DataTransfer dataTransfer)
	{
		TargetImporter<?> importer = getImporterForTarget( target );
		
		if ( importer != null )
		{
			return importer.canImport( target, selection, dataTransfer );
		}
		else
		{
			return false;
		}
	}

	@Override
	public boolean importData(Target target, Selection selection, DataTransfer dataTransfer)
	{
		TargetImporter<?> importer = getImporterForTarget( target );
		
		if ( importer != null )
		{
			return importer.importData( target, selection, dataTransfer );
		}
		else
		{
			return false;
		}
	}
	
	
	private AbstractSelectionExporter<?,?> getExporterForSelection(Selection selection)
	{
		return exporters != null  ?  exporters.getForInstance( selection )  :  null;
	}

	private TargetImporter<?> getImporterForTarget(Target target)
	{
		return importers != null  ?  importers.getForInstance( target )  :  null;
	}
	
	private SelectionEditorInterface getSelectionEditorForSelection(Selection selection)
	{
		return selectionEditors != null  ?  selectionEditors.getForInstance( selection )  :  null;
	}
}
