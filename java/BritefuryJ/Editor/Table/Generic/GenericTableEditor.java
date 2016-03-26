//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.Table.Generic;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Editor.Table.TableHeader;
import BritefuryJ.Editor.Table.TableHeaderText;
import org.python.core.PyList;

import BritefuryJ.Cell.EditableTextCell;
import BritefuryJ.Editor.Table.AbstractTableEditor;
import BritefuryJ.Editor.Table.AbstractTableEditorInstance;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Util.UnaryFn;

public class GenericTableEditor extends AbstractTableEditor<GenericTableModelInterface>
{
	protected Pres blankPres;
	protected TableHeader topHeader, leftHeader;


	public GenericTableEditor(TableHeader topHeader, TableHeader leftHeader, boolean growRight, boolean growDown, UnaryFn convertValueFn, Pres blankCell) {
		super( leftHeader != null, topHeader != null, growRight, growDown );
		this.topHeader = topHeader;
		this.leftHeader = leftHeader;
		this.blankPres = EditableTextCell.blankTextCell( "", convertValueFn );
		if (blankCell != null) {
			this.blankPres = new Row(new Pres[] {this.blankPres, blankCell});
		}
	}

	public GenericTableEditor(TableHeader topHeader, TableHeader leftHeader, boolean growRight, boolean growDown, UnaryFn convertValueFn) {
		this( topHeader, leftHeader, growRight, growDown, convertValueFn, null );
	}

	public GenericTableEditor(TableHeader topHeader, TableHeader leftHeader, boolean growRight, boolean growDown, Pres blankCell) {
		this( topHeader, leftHeader, growRight, growDown, UnaryFn.identity, blankCell );
	}

	public GenericTableEditor(TableHeader topHeader, TableHeader leftHeader, boolean growRight, boolean growDown) {
		this( topHeader, leftHeader, growRight, growDown, UnaryFn.identity, null );
	}


	public static GenericTableEditor withColumns(String columnTitles[], TableHeader leftHeader,boolean growRight, boolean growDown, UnaryFn convertValueFn, Pres blankCell)
	{
		return new GenericTableEditor(TableHeaderText.forArray(columnTitles), leftHeader, growRight, growDown, convertValueFn, blankCell);
	}

	public static GenericTableEditor withColumns(String columnTitles[], TableHeader leftHeader,boolean growRight, boolean growDown, UnaryFn convertValueFn)
	{
		return new GenericTableEditor(TableHeaderText.forArray(columnTitles), leftHeader, growRight, growDown, convertValueFn, null);
	}

	public static GenericTableEditor withColumns(String columnTitles[], TableHeader leftHeader,boolean growRight, boolean growDown, Pres blankCell)
	{
		return new GenericTableEditor(TableHeaderText.forArray(columnTitles), leftHeader, growRight, growDown, UnaryFn.identity, blankCell);
	}

	public static GenericTableEditor withColumns(String columnTitles[], TableHeader leftHeader, boolean growRight, boolean growDown)
	{
		return new GenericTableEditor(TableHeaderText.forArray(columnTitles), leftHeader, growRight, growDown, UnaryFn.identity, null);
	}
	

	
	
	
	private static GenericTableModel.ValueFactory stringFactory = new GenericTableModel.ValueFactory()
	{
		@Override
		public Object createValue()
		{
			return "";
		}
	};
	
	private static GenericTableModel.RowFactory pyListFactory = new GenericTableModel.RowFactory()
	{
		@SuppressWarnings("unchecked")
		@Override
		public List<Object> createRow()
		{
			return new PyList();
		}
	};
	
	private static GenericTableModel.RowFactory arrayListFactory = new GenericTableModel.RowFactory()
	{
		@Override
		public List<Object> createRow()
		{
			return new ArrayList<Object>();
		}
	};
	
	private static GenericTableModel.ValueCopier identityCopier = new GenericTableModel.ValueCopier()
	{
		@Override
		public Object copyValue(Object value)
		{
			return value;
		}
	};
	
	@Override
	protected GenericTableModelInterface coerceModel(Object model)
	{
		if ( model instanceof PyList )
		{
			@SuppressWarnings("unchecked")
			List<List<Object>> ls = (List<List<Object>>)model;
			return new GenericTableModel( ls, stringFactory, pyListFactory, identityCopier );
		}
		else if ( model instanceof List )
		{
			@SuppressWarnings("unchecked")
			List<List<Object>> ls = (List<List<Object>>)model;
			return new GenericTableModel( ls, stringFactory, arrayListFactory, identityCopier );
		}
		else
		{
			return (GenericTableModelInterface)model;
		}
	}

	@Override
	protected AbstractTableEditorInstance<GenericTableModelInterface> createInstance(GenericTableModelInterface model, boolean editable)
	{
		return new GenericTableEditorInstance( this, model, editable );
	}

	@Override
	protected Object[][] getBlock(GenericTableModelInterface model, int x, int y, int w, int h)
	{
		if ( y < model.getHeight() )
		{
			// Ensure the the height of the block does not go beyond the last row
			h = Math.min( h, model.getHeight() - y );
			
			return model.getBlock( x, y, w, h );
		}
		else
		{
			return new Object[0][];
		}
	}

	@Override
	protected void putBlock(GenericTableModelInterface model, int x, int y, Object[][] data, AbstractTableEditorInstance<GenericTableModelInterface> editorInstance)
	{
		model.putBlock( x, y, data );
	}

	@Override
	protected void deleteBlock(GenericTableModelInterface model, int x, int y, int w, int h, AbstractTableEditorInstance<GenericTableModelInterface> editorInstance)
	{
		model.deleteBlock( x, y, w, h );
	}

	@Override
	protected void insertRowBefore(GenericTableModelInterface model, int y, AbstractTableEditorInstance<GenericTableModelInterface> editorInstance) {
		model.insertRow(y, new Object[] {});
	}
	@Override
	protected void removeRow(GenericTableModelInterface model, int y, AbstractTableEditorInstance<GenericTableModelInterface> editorInstance) {
		model.removeRow(y);
	}
}
