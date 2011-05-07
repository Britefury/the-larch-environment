//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table.ObjectList;

import java.lang.reflect.Constructor;
import java.util.List;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PySlice;
import org.python.core.PyString;
import org.python.core.PyType;
import org.python.core.__builtin__;

import BritefuryJ.Editor.Table.AbstractTableEditor;
import BritefuryJ.Editor.Table.AbstractTableEditorInstance;

public class ObjectListTableEditor extends AbstractTableEditor<ObjectListInterface>
{
	public interface RowFactory
	{
		Object createRow();
	}
	
	private static class ClassRowFactory implements RowFactory
	{
		private Constructor<?> constructor;
		
		public ClassRowFactory(Class<?> cls) throws SecurityException
		{
			try
			{
				constructor = cls.getConstructor( new Class<?>[] {} );
			}
			catch (NoSuchMethodException e)
			{
				throw new RuntimeException( "Class " + cls.getName() + " has no default constructor" );
			}
		}
		
		
		public Object createRow()
		{
			try
			{
				return constructor.newInstance( new Object[] {} );
			}
			catch (Throwable t)
			{
				throw new RuntimeException( t );
			}
		}
	}
	
	private static class PyTypeRowFactory implements RowFactory
	{
		private PyType type;
		
		public PyTypeRowFactory(PyType type)
		{
			this.type = type;
		}
		
		
		public Object createRow()
		{
			PyObject x = type.__call__();
			return Py.tojava( x, Object.class );
		}
	}
	
	
	private static class ListModelWrapper implements ObjectListInterface
	{
		private List<Object> model;
		
		public ListModelWrapper(List<Object> model)
		{
			this.model = model;
		}

		@Override
		public int size()
		{
			return model.size();
		}

		@Override
		public Object get(int i)
		{
			return model.get( i );
		}

		@Override
		public void append(Object x)
		{
			model.add( x );
		}

		@Override
		public void removeRange(int start, int stop)
		{
			model.subList( start, stop ).clear();
		}
	}
	
	

	private static class PyObjectModelWrapper implements ObjectListInterface
	{
		private static final PyString append = __builtin__.intern( Py.newString( "append" ) );
		private static final PyString __delitem__ = __builtin__.intern( Py.newString( "__delitem__" ) );
		
		private PyObject model;
		
		public PyObjectModelWrapper(PyObject model)
		{
			this.model = model;
		}

		@Override
		public int size()
		{
			return model.__len__();
		}

		@Override
		public Object get(int i)
		{
			return model.__getitem__( i );
		}

		@Override
		public void append(Object x)
		{
			__builtin__.getattr( model, append ).__call__( Py.java2py( x ) );
		}

		@Override
		public void removeRange(int start, int end)
		{
			__builtin__.getattr( model, __delitem__ ).__call__( new PySlice( Py.newInteger( start ), Py.newInteger( end ), Py.None ) );
		}
	}
	
	

	protected AbstractColumn columns[];
	private RowFactory rowFactory;
	protected boolean showEmptyRowAtBottom, rowsAreLive;

	
	public ObjectListTableEditor(List<Object> columns, RowFactory rowFactory, boolean showEmptyRowAtBottom, boolean rowsAreLive)
	{
		this.columns = new AbstractColumn[columns.size()];
		for (int i = 0; i < this.columns.length; i++)
		{
			this.columns[i] = AbstractColumn.coerce( columns.get( i ) );
		}

		this.rowFactory = rowFactory;
		this.showEmptyRowAtBottom = showEmptyRowAtBottom;
		this.rowsAreLive = rowsAreLive;
	}
	
	
	public ObjectListTableEditor(List<Object> columns, Class<?> rowClass, boolean showEmptyRowAtBottom, boolean rowsAreLive)
	{
		this( columns, new ClassRowFactory( rowClass ), showEmptyRowAtBottom, rowsAreLive );
	}
	
	
	public ObjectListTableEditor(List<Object> columns, PyType rowType, boolean showEmptyRowAtBottom, boolean rowsAreLive)
	{
		this( columns, new PyTypeRowFactory( rowType ), showEmptyRowAtBottom, rowsAreLive );
	}
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	protected ObjectListInterface coerceModel(Object model)
	{
		if ( model instanceof ObjectListInterface )
		{
			return (ObjectListInterface)model;
		}
		else if ( model instanceof List )
		{
			return new ListModelWrapper( (List<Object>)model );
		}
		else if ( model instanceof PyObject )
		{
			return new PyObjectModelWrapper( (PyObject)model );
		}
		else
		{
			throw new RuntimeException( "Could not coerce object " + model + " to an ObjectListInterface" );
		}
	}

	@Override
	protected AbstractTableEditorInstance<ObjectListInterface> createInstance(ObjectListInterface model)
	{
		return new ObjectListTableEditorInstance( this, model );
	}

	@Override
	protected Object[][] getBlock(ObjectListInterface model, int x, int y, int w, int h)
	{
		if ( y < model.size() )
		{
			// Ensure the the height of the block does not go beyond the last row
			h = Math.min( h, model.size() - y );
			
			Object block[][] = new Object[h][];
			
			for (int j = y, b = 0; b < h; j++, b++)
			{
				Object blockRow[] = new Object[w];
				Object modelRow = model.get( j );
				
				for (int i = x, a = 0; a < w; i++, a++)
				{
					blockRow[a] = columns[i].get( modelRow );
				}
				block[b] = blockRow;
			}
			
			return block;
		}
		else
		{
			return new Object[0][];
		}
	}

	@Override
	protected void putBlock(ObjectListInterface model, int x, int y, Object[][] data, AbstractTableEditorInstance<ObjectListInterface> editorInstance)
	{
		ObjectListTableEditorInstance instance = (ObjectListTableEditorInstance)editorInstance;
		
		int width = columns.length;
		int h = data.length;
		
		growHeight( model, y + h );
		
		for (int j = y, b = 0; b < h; j++, b++)
		{
			Object dataRow[] = data[b];
			Object modelRow = model.get( j );
			
			int w = Math.min( dataRow.length, width - x );
			for (int i = x, a = 0; a < w; i++, a++)
			{
				columns[i].set( modelRow, dataRow[a] );
			}
			
			if ( !rowsAreLive )
			{
				instance.onRowChanged( modelRow );
			}
		}
	}
	
	@Override
	protected void deleteBlock(ObjectListInterface model, int x, int y, int w, int h, AbstractTableEditorInstance<ObjectListInterface> editorInstance)
	{
		ObjectListTableEditorInstance instance = (ObjectListTableEditorInstance)editorInstance;
		
		if ( x == 0  &&  w >= columns.length )
		{
			// We are removing entire rows
			model.removeRange( y, y + h );
		}
		else
		{
			int bottomRowIndex = Math.min( y + h - 1, model.size() - 1 );
			for (int j = bottomRowIndex; j >= y; j--)
			{
				Object modelRow = model.get( j );
				
				for (int i = x; i < x + w; i++)
				{
					AbstractColumn column = columns[i];
					column.set( modelRow, column.defaultValue() );
				}

				if ( !rowsAreLive )
				{
					instance.onRowChanged( modelRow );
				}
			}
		}
	}

	
	private void growHeight(ObjectListInterface model, int h)
	{
		if ( h > model.size() )
		{
			for (int j = model.size(); j < h; j++)
			{
				model.append( rowFactory.createRow() );
			}
		}
	}
	
	
	protected Object newRow(ObjectListInterface model)
	{
		Object row = rowFactory.createRow();
		model.append( row );
		return row;
	}


	@Override
	protected Object[][] textBlockToValueBlock(int posX, int posY, String[][] textBlock)
	{
		Object destBlock[][] = new Object[textBlock.length][];
		for (int b = 0; b < textBlock.length; b++)
		{
			String[] srcRow = textBlock[b];
			Object[] destRow = new Object[srcRow.length];
			destBlock[b] = destRow;
			
			for (int a = 0, i = posX; a < srcRow.length; a++, i++)
			{
				String cellText = srcRow[a];
				Object x;
				if ( i < columns.length )
				{
					x = columns[i].textToValue( cellText );
				}
				else
				{
					x = cellText;
				}
				destRow[a] = x;
			}
		}
		
		return destBlock;
	}
}
