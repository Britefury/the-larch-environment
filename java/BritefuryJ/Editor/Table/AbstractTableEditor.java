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
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import BritefuryJ.ClipboardFilter.ClipboardCopier;
import BritefuryJ.ClipboardFilter.ClipboardCopierMemo;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Clipboard.ClipboardHandlerInterface;
import BritefuryJ.LSpace.Focus.Selection;
import BritefuryJ.LSpace.Focus.Target;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Clipboard.AbstractSelectionExporter;
import BritefuryJ.Pres.Clipboard.ClipboardHandler;
import BritefuryJ.Pres.Clipboard.DataExporter;
import BritefuryJ.Pres.Clipboard.DataExporter.ExportFn;
import BritefuryJ.Pres.Clipboard.DataExporterInterface;
import BritefuryJ.Pres.Clipboard.DataImporter;
import BritefuryJ.Pres.Clipboard.DataImporterInterface;
import BritefuryJ.Pres.Clipboard.SelectionEditor;
import BritefuryJ.Pres.Clipboard.SelectionEditorInterface;
import BritefuryJ.Pres.Clipboard.SelectionExporter;
import BritefuryJ.Pres.Clipboard.SelectionExporter.SelectionContentsFn;
import BritefuryJ.Pres.Clipboard.TargetImporter;
import BritefuryJ.Pres.Primitive.Primitive;
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



	private final ExportFn<TableSelectionContents> exportBuffer = new ExportFn<TableSelectionContents>()
	{
		public Object export(TableSelectionContents selectionContents)
		{
			return new TableBuffer( AbstractTableEditor.this, selectionContents.contents );
		}
	};
	
	private final ExportFn<TableSelectionContents> exportTextHtml = new ExportFn<TableSelectionContents>()
	{
		public Object export(TableSelectionContents selectionContents)
		{
			StringBuilder b = new StringBuilder();
			b.append( "<html>\n" );
			b.append( "\t<head>\n" );
			b.append( "\t\t<title>Table export</title>\n" );
			b.append( "\t</head>\n" );
			b.append( "\t<body>\n" );
			b.append( "\t\t<table>\n" );
			for (TableCellExportedValue row[]: selectionContents.exportContents)
			{
				b.append( "\t\t\t<tr>\n" );
				for (TableCellExportedValue cell: row)
				{
					b.append( "\t\t\t\t<td>" );
					b.append( cell.getHtml() );
					b.append( "</td>\n" );
				}
				b.append( "\t\t\t</tr>\n" );
			}
			b.append( "\t\t</table>\n" );
			b.append( "\t</body>\n" );
			b.append( "</html>\n" );
			return b.toString();
		}
	};
	
	private final ExportFn<TableSelectionContents> exportTextPlain = new ExportFn<TableSelectionContents>()
	{
		public Object export(TableSelectionContents selectionContents)
		{
			StringBuilder b = new StringBuilder();
			for (TableCellExportedValue row[]: selectionContents.exportContents)
			{
				boolean first = true;
				for (TableCellExportedValue cell: row)
				{
					if ( !first )
					{
						b.append( "\t" );
					}
					b.append( cell.getPlainText() );
					first = false;
				}
				b.append( "\n" );
			}
			return b.toString();
		}
	};
	
	private final SelectionContentsFn<TableSelectionContents, TableSelection> selectionContents = new SelectionContentsFn<TableSelectionContents, TableSelection>()
	{
		public TableSelectionContents getSelectionContents(TableSelection selection)
		{
			Object contents[][] = selection.getSelectedData();
			TableCellExportedValue exportContents[][] = exportBlock( selection.getX(), selection.getY(), contents );
			contents = copyBlock( selection.getX(), selection.getY(), contents );
			return new TableSelectionContents( contents, exportContents );
		}
	};
	
	
	private final DataImporter.CanImportFn<TableTarget> canImportBuffer = new DataImporter.CanImportFn<TableTarget>()
	{
		public boolean canImport(TableTarget target, Selection selection, Object data)
		{
			TableBuffer buffer = (TableBuffer)data;
			return canImportFromEditor( buffer.editor );
		}
	};
	
	private final DataImporter.ImportDataFn<TableTarget> importBufferData = new DataImporter.ImportDataFn<TableTarget>()
	{
		@SuppressWarnings("unchecked")
		public boolean importData(TableTarget target, Selection selection, Object data)
		{
			TableBuffer buffer = (TableBuffer)data;

			ModelType model = (ModelType)target.editorInstance.model;
			Object[][] subtable = buffer.contents;
			
			putBlock( model, target.x, target.y, subtable, (AbstractTableEditorInstance<ModelType>)target.editorInstance );

			return true;
		}
	};
	
	private final DataImporter.CanImportFlavorFn canImportHtmlFlavor = new DataImporter.CanImportFlavorFn()
	{
		public boolean canImportFlavor(DataFlavor flavor)
		{
			return flavor.getMimeType().startsWith( "text/html;" )  &&  flavor.getRepresentationClass() == String.class;
		}
	};

	private final DataImporter.ImportDataFn<TableTarget> importHtmlData = new DataImporter.ImportDataFn<TableTarget>()
	{
		@SuppressWarnings("unchecked")
		public boolean importData(TableTarget target, Selection selection, Object data)
		{
			String htmlText = (String)data;
			
			Source html = new Source( htmlText );
			
			Element table = html.getFirstElement( "table" );
			
			if ( table != null )
			{
				ArrayList<Segment[]> tableData = new ArrayList<Segment[]>();
				
				List<Element> trs = table.getAllElements( "tr" );
				
				int rowPos = table.getBegin();
				for (Element tr: trs)
				{
					if ( tr.getBegin() > rowPos )
					{
						// We have found a non-nested TR
						ArrayList<Segment> rowData = new ArrayList<Segment>();
						
						// Read TD elements
						List<Element> tds = tr.getAllElements( "td" );
						
						int cellPos = tr.getBegin();
						for (Element td: tds)
						{
							if ( td.getBegin() > cellPos )
							{
								// We have found a non-nested TD
								rowData.add( td.getContent() );
								
								cellPos = td.getEnd();
							}
						}
						// Done reading TD elements
						
						tableData.add( rowData.toArray( new Segment[rowData.size()] ) );

						rowPos = tr.getEnd();
					}
				}

			
			
				ModelType model = (ModelType)target.editorInstance.model;
				Segment htmlBlock[][] = tableData.toArray( new Segment[tableData.size()][] );
				
				Object[][] subtable = importHTMLBlock( target.x, target.y, htmlBlock );
				
				putBlock( model, target.x, target.y, subtable, (AbstractTableEditorInstance<ModelType>)target.editorInstance );

				return true;
			}
			else
			{
				return false;
			}
		}
	};
	
	
	private final SelectionEditor.DeleteSelectionFn<TableSelection> deleteSelection = new SelectionEditor.DeleteSelectionFn<TableSelection>()
	{
		@SuppressWarnings("unchecked")
		public boolean deleteSelection(TableSelection selection, Target target)
		{
			ModelType model = (ModelType)selection.editorInstance.model;

			deleteBlock( model, selection.getX(), selection.getY(), selection.getWidth(), selection.getHeight(), (AbstractTableEditorInstance<ModelType>)selection.editorInstance );

			return true;
		}
	};
	
	

	
	protected ClipboardHandlerInterface clipboardHandler;
	protected boolean hasLeftHeader, hasTopHeader, growRight, growDown;
	
	
	
	public AbstractTableEditor(boolean hasLeftHeader, boolean hasTopHeader, boolean growRight, boolean growDown)
	{
		DataFlavor textHtmlFlavor, textPlainFlavor;
		try
		{
			textHtmlFlavor = new DataFlavor( "text/html; class=java.lang.String" );
			textPlainFlavor = new DataFlavor( "text/plain; class=java.lang.String" );
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException( e );
		}
	
		DataExporter<TableSelectionContents> bufferExporter = new DataExporter<TableSelectionContents>( TableBuffer.class, exportBuffer );
		DataExporter<TableSelectionContents> textHtmlExporter = new DataExporter<TableSelectionContents>( textHtmlFlavor, exportTextHtml );
		DataExporter<TableSelectionContents> textPlainExporter = new DataExporter<TableSelectionContents>( textPlainFlavor, exportTextPlain );
		@SuppressWarnings("unchecked")
		DataExporterInterface<TableSelectionContents> exporters[] = new DataExporterInterface[] { bufferExporter, textHtmlExporter, textPlainExporter };
		SelectionExporter<TableSelectionContents, TableSelection> selectionExporter = new SelectionExporter<TableSelectionContents, TableSelection>( TableSelection.class, SelectionExporter.COPY, selectionContents,
				Arrays.asList( exporters ) );
		AbstractSelectionExporter<?, ?> selectionExporters[] = new AbstractSelectionExporter[] { selectionExporter };
		
		DataImporter<TableTarget> bufferImporter = new DataImporter<TableTarget>( TableBuffer.class, importBufferData, canImportBuffer );
		DataImporter<TableTarget> htmlImporter = new DataImporter<TableTarget>( canImportHtmlFlavor, importHtmlData );
		@SuppressWarnings("unchecked")
		DataImporterInterface<TableTarget> importers[] = new DataImporterInterface[] { bufferImporter, htmlImporter };
		TargetImporter<TableTarget> targetImporter = new TargetImporter<TableTarget>( TableTarget.class, Arrays.asList( importers ) );
		TargetImporter<?> targetImporters[] = new TargetImporter[] { targetImporter };
		
		SelectionEditor<TableSelection> selectionEditor = new SelectionEditor<TableSelection>( TableSelection.class, null, deleteSelection );
		SelectionEditorInterface selectionEditors[] = new SelectionEditorInterface[] { selectionEditor };
		
		clipboardHandler = new ClipboardHandler( Arrays.asList( selectionExporters ), Arrays.asList( targetImporters ), Arrays.asList( selectionEditors ) );
		
		this.hasLeftHeader = hasLeftHeader;
		this.hasTopHeader = hasTopHeader;
		this.growRight = growRight;
		this.growDown = growDown;
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
			public LSElement present(PresentationContext ctx, StyleValues style)
			{
				StyleSheet styleSheet = TableEditorStyle.tableStyle( style, hasTopHeader, hasLeftHeader );
				StyleValues used = TableEditorStyle.useTableAttrs( style );
				boolean editable = style.get( Primitive.editable, Boolean.class );
				
				AbstractTableEditorInstance<ModelType> instance = createInstance( m, editable );
				Pres editor = instance.editTable();
				
				return editor.present( ctx, used.withAttrs( styleSheet ) );
			}
		};
		return p;
	}


	protected abstract ModelType coerceModel(Object model);

	protected abstract AbstractTableEditorInstance<ModelType> createInstance(ModelType model, boolean editable);
	
	protected abstract Object[][] getBlock(ModelType model, int x, int y, int w, int h);
	protected abstract void putBlock(ModelType model, int x, int y, Object[][] data, AbstractTableEditorInstance<ModelType> editorInstance);
	protected abstract void deleteBlock(ModelType model, int x, int y, int w, int h, AbstractTableEditorInstance<ModelType> editorInstance);

	protected Object[][] importHTMLBlock(int posX, int posY, Segment[][] htmlBlock)
	{
		String[][] textBlock = new String[htmlBlock.length][];
		
		for (int rowIndex = 0; rowIndex < htmlBlock.length; rowIndex++)
		{
			Segment[] htmlRow = htmlBlock[rowIndex];
			String[] textRow = new String[htmlRow.length];
			
			for (int colIndex = 0; colIndex < htmlRow.length; colIndex++)
			{
				textRow[colIndex] = htmlRow[colIndex].getTextExtractor().toString();
			}
		}
		
		return textBlock;
	}

	protected Object[][] copyBlock(int posX, int posY, Object[][] valueBlock)
	{
		ClipboardCopierMemo memo = new ClipboardCopierMemo();
		
		Object destBlock[][] = new Object[valueBlock.length][];
		for (int b = 0; b < valueBlock.length; b++)
		{
			Object[] srcRow = valueBlock[b];
			Object[] destRow = new Object[srcRow.length];
			destBlock[b] = destRow;
			
			for (int a = 0; a < srcRow.length; a++)
			{
				destRow[a] = ClipboardCopier.instance.copyObject( srcRow[a], memo );
			}
		}
		
		return destBlock;
	}

	protected TableCellExportedValue[][] exportBlock(int posX, int posY, Object[][] valueBlock)
	{
		TableCellExportedValue destBlock[][] = new TableCellExportedValue[valueBlock.length][];
		for (int b = 0; b < valueBlock.length; b++)
		{
			Object[] srcRow = valueBlock[b];
			TableCellExportedValue[] destRow = new TableCellExportedValue[srcRow.length];
			destBlock[b] = destRow;
			
			for (int a = 0; a < srcRow.length; a++)
			{
				destRow[a] = new TableCellExportedValue( srcRow[a] );
			}
		}
		
		return destBlock;
	}
}
