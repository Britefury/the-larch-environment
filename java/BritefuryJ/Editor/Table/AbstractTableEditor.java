//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table;

import java.util.Arrays;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Clipboard.ClipboardHandlerInterface;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Clipboard.AbstractSelectionExporter;
import BritefuryJ.Pres.Clipboard.ClipboardHandler;
import BritefuryJ.Pres.Clipboard.DataExporter;
import BritefuryJ.Pres.Clipboard.DataExporter.ExportFn;
import BritefuryJ.Pres.Clipboard.DataExporterInterface;
import BritefuryJ.Pres.Clipboard.DataImporter;
import BritefuryJ.Pres.Clipboard.DataImporterInterface;
import BritefuryJ.Pres.Clipboard.SelectionExporter;
import BritefuryJ.Pres.Clipboard.SelectionExporter.SelectionContentsFn;
import BritefuryJ.Pres.Clipboard.TargetImporter;

public abstract class AbstractTableEditor<ModelType>
{
	private static class TableBuffer
	{
		private AbstractTableEditor<?> editor;
		private Object[][] contents;
		
		
		public TableBuffer(AbstractTableEditor<?> editor, Object[][] contents)
		{
			this.editor = editor;
			this.contents = contents;
		}
	}



	ExportFn<Object[][]> export = new ExportFn<Object[][]>()
	{
		@Override
		public Object export(Object[][] selectionContents)
		{
			return new TableBuffer( AbstractTableEditor.this, selectionContents );
		}
	};
	
	SelectionContentsFn<Object[][], TableSelection> selectionContents = new SelectionContentsFn<Object[][], TableSelection>()
	{
		@Override
		public Object[][] getSelectionContents(TableSelection selection)
		{
			return selection.getSelectedData();
		}
	};
	
	
	DataImporter.CanImportFn<TableTarget> canImport = new DataImporter.CanImportFn<TableTarget>()
	{
		@Override
		public boolean canImport(TableTarget target, Selection selection, Object data)
		{
			TableBuffer buffer = (TableBuffer)data;
			return canImportFromEditor( buffer.editor );
		}
	};
	
	DataImporter.ImportDataFn<TableTarget> importData = new DataImporter.ImportDataFn<TableTarget>()
	{
		@SuppressWarnings("unchecked")
		@Override
		public boolean importData(TableTarget target, Selection selection, Object data)
		{
			TableBuffer buffer = (TableBuffer)data;

			DPElement tableElem = (DPElement)target.table;
			ModelType model = (ModelType)tableElem.getFixedValue();
			Object[][] subtable = buffer.contents;
			
			putBlock( model, target.x, target.y, subtable, (AbstractTableEditorInstance<ModelType>)target.editorInstance );

			return true;
		}
	};
	
	

	
	protected ClipboardHandlerInterface clipboardHandler;
	
	
	
	public AbstractTableEditor()
	{
		DataExporter<Object[][]> bufferExporter = new DataExporter<Object[][]>( TableBuffer.class, export );
		@SuppressWarnings("unchecked")
		DataExporterInterface<Object[][]> exporters[] = new DataExporterInterface[] { bufferExporter };
		SelectionExporter<Object[][], TableSelection> selectionExporter = new SelectionExporter<Object[][], TableSelection>( TableSelection.class, SelectionExporter.COPY, selectionContents,
				Arrays.asList( exporters ) );
		AbstractSelectionExporter<?, ?> selectionExporters[] = new AbstractSelectionExporter[] { selectionExporter };
		
		DataImporter<TableTarget> bufferImporter = new DataImporter<TableTarget>( TableBuffer.class, importData, canImport );
		@SuppressWarnings("unchecked")
		DataImporterInterface<TableTarget> importers[] = new DataImporterInterface[] { bufferImporter };
		TargetImporter<TableTarget> targetImporter = new TargetImporter<TableTarget>( TableTarget.class, Arrays.asList( importers ) );
		TargetImporter<?> targetImporters[] = new TargetImporter[] { targetImporter };
		
		
		
		clipboardHandler = new ClipboardHandler( Arrays.asList( selectionExporters ), Arrays.asList( targetImporters ), null );
	}

	
	


	protected boolean canImportFromEditor(AbstractTableEditor<?> editor)
	{
		return editor == this;
	}
	
	
	public Pres editTable(ModelType model)
	{
		checkModel( model );
		AbstractTableEditorInstance<ModelType> instance = createInstance();
		return instance.editTable( model );
	}


	protected abstract void checkModel(ModelType model);

	protected abstract AbstractTableEditorInstance<ModelType> createInstance();
	
	protected abstract Object[][] getBlock(ModelType model, int x, int y, int w, int h);

	protected abstract void putBlock(ModelType model, int x, int y, Object[][] data, AbstractTableEditorInstance<ModelType> editorInstance);
}
