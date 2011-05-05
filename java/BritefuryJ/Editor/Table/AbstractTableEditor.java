//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table;

import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Clipboard.ClipboardHandlerInterface;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
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
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

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
	
	
	DataImporter.CanImportFn<TableTarget> canImportBuffer = new DataImporter.CanImportFn<TableTarget>()
	{
		@Override
		public boolean canImport(TableTarget target, Selection selection, Object data)
		{
			TableBuffer buffer = (TableBuffer)data;
			return canImportFromEditor( buffer.editor );
		}
	};
	
	DataImporter.ImportDataFn<TableTarget> importBufferData = new DataImporter.ImportDataFn<TableTarget>()
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
	
	DataImporter.CanImportFlavorFn canImportHtmlFlavor = new DataImporter.CanImportFlavorFn()
	{
		@Override
		public boolean canImportFlavor(DataFlavor flavor)
		{
			return flavor.getMimeType().startsWith( "text/html;" )  &&  flavor.getRepresentationClass() == String.class;
		}
	};

	DataImporter.ImportDataFn<TableTarget> importHtmlData = new DataImporter.ImportDataFn<TableTarget>()
	{
		@SuppressWarnings("unchecked")
		@Override
		public boolean importData(TableTarget target, Selection selection, Object data)
		{
			String htmlText = (String)data;
			
			Source html = new Source( htmlText );
			
			Element table = html.getFirstElement( "table" );
			
			if ( table != null )
			{
				ArrayList<String[]> tableData = new ArrayList<String[]>();
				
				List<Element> trs = table.getAllElements( "tr" );
				
				int rowPos = table.getBegin();
				for (Element tr: trs)
				{
					if ( tr.getBegin() > rowPos )
					{
						// We have found a non-nested TR
						ArrayList<String> rowData = new ArrayList<String>();
						
						// Read TD elements
						List<Element> tds = tr.getAllElements( "td" );
						
						int cellPos = tr.getBegin();
						for (Element td: tds)
						{
							if ( td.getBegin() > cellPos )
							{
								// We have found a non-nested TD
								TextExtractor extractor = td.getContent().getTextExtractor();
								
								String cellData = extractor.toString();
								
								rowData.add( cellData );
								
								cellPos = td.getEnd();
							}
						}
						// Done reading TD elements
						
						tableData.add( rowData.toArray( new String[0] ) );

						rowPos = tr.getEnd();
					}
				}

			
			
				DPElement tableElem = (DPElement)target.table;
				ModelType model = (ModelType)tableElem.getFixedValue();
				String textBlock[][] = tableData.toArray( new String[0][] );
				
				Object[][] subtable = textBlockToValueBlock( target.x, target.y, textBlock );
				
				putBlock( model, target.x, target.y, subtable, (AbstractTableEditorInstance<ModelType>)target.editorInstance );

				return true;
			}
			else
			{
				return false;
			}
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
		
		DataImporter<TableTarget> bufferImporter = new DataImporter<TableTarget>( TableBuffer.class, importBufferData, canImportBuffer );
		DataImporter<TableTarget> htmlImporter = new DataImporter<TableTarget>( canImportHtmlFlavor, importHtmlData );
		@SuppressWarnings("unchecked")
		DataImporterInterface<TableTarget> importers[] = new DataImporterInterface[] { bufferImporter, htmlImporter };
		TargetImporter<TableTarget> targetImporter = new TargetImporter<TableTarget>( TableTarget.class, Arrays.asList( importers ) );
		TargetImporter<?> targetImporters[] = new TargetImporter[] { targetImporter };
		
		
		
		clipboardHandler = new ClipboardHandler( Arrays.asList( selectionExporters ), Arrays.asList( targetImporters ), null );
	}

	
	


	protected boolean canImportFromEditor(AbstractTableEditor<?> editor)
	{
		return editor == this;
	}
	
	
	public Pres editTable(Object model)
	{
		final ModelType m = coerceModel( model );
		
		Pres p = new Pres()
		{
			@Override
			public DPElement present(PresentationContext ctx, StyleValues style)
			{
				StyleSheet styleSheet = TableEditorStyle.tableStyle( style );
				StyleValues used = TableEditorStyle.useTableAttrs( style );
				
				AbstractTableEditorInstance<ModelType> instance = createInstance();
				Pres editor = instance.editTable( m );
				
				return editor.present( ctx, used.withAttrs( styleSheet ) );
			}
		};
		return p;
	}


	protected abstract ModelType coerceModel(Object model);

	protected abstract AbstractTableEditorInstance<ModelType> createInstance();
	
	protected abstract Object[][] getBlock(ModelType model, int x, int y, int w, int h);

	protected abstract void putBlock(ModelType model, int x, int y, Object[][] data, AbstractTableEditorInstance<ModelType> editorInstance);

	protected Object[][] textBlockToValueBlock(int posX, int posY, String[][] textBlock)
	{
		return textBlock;
	}
}
