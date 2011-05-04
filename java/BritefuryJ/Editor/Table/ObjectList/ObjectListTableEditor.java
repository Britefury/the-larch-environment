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
import org.python.core.PyString;
import org.python.core.PyType;
import org.python.core.__builtin__;

import BritefuryJ.Editor.Table.AbstractTableEditor;
import BritefuryJ.Editor.Table.AbstractTableEditorInstance;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Text;

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
	
	
	public interface EmptyCellFactory
	{
		Pres createEmptyCell();
	}
	
	
	private static final EmptyCellFactory emptyTextCellFactory = new EmptyCellFactory()
	{
		@Override
		public Pres createEmptyCell()
		{
			return new Text( "" );
		}
	};
	
	
	
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
	}
	
	

	private static class PyObjectModelWrapper implements ObjectListInterface
	{
		private static final PyString append = __builtin__.intern( Py.newString( "append" ) );
		
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
	}
	
	

	protected AbstractColumn columns[];
	private RowFactory rowFactory;
	protected boolean showEmptyRowAtBottom, rowsAreLive;
	protected EmptyCellFactory emptyCellFac;

	
	public ObjectListTableEditor(List<Object> columns, RowFactory rowFactory, boolean showEmptyRowAtBottom, boolean rowsAreLive, EmptyCellFactory emptyCellFac)
	{
		this.columns = new AbstractColumn[columns.size()];
		for (int i = 0; i < this.columns.length; i++)
		{
			this.columns[i] = AbstractColumn.coerce( columns.get( i ) );
		}

		this.rowFactory = rowFactory;
		this.showEmptyRowAtBottom = showEmptyRowAtBottom;
		this.rowsAreLive = rowsAreLive;
		this.emptyCellFac = emptyCellFac;
	}
	
	public ObjectListTableEditor(List<Object> columns, RowFactory rowFactory, boolean showEmptyRowAtBottom, boolean rowsAreLive)
	{
		this( columns, rowFactory, showEmptyRowAtBottom, rowsAreLive, emptyTextCellFactory );
	}
	
	
	public ObjectListTableEditor(List<Object> columns, Class<?> rowClass, boolean showEmptyRowAtBottom, boolean rowsAreLive, EmptyCellFactory emptyCellFac)
	{
		this( columns, new ClassRowFactory( rowClass ), showEmptyRowAtBottom, rowsAreLive, emptyCellFac );
	}
	
	public ObjectListTableEditor(List<Object> columns, Class<?> rowClass, boolean showEmptyRowAtBottom, boolean rowsAreLive)
	{
		this( columns, new ClassRowFactory( rowClass ), showEmptyRowAtBottom, rowsAreLive, emptyTextCellFactory );
	}
	
	
	public ObjectListTableEditor(List<Object> columns, PyType rowType, boolean showEmptyRowAtBottom, boolean rowsAreLive, EmptyCellFactory emptyCellFac)
	{
		this( columns, new PyTypeRowFactory( rowType ), showEmptyRowAtBottom, rowsAreLive, emptyCellFac );
	}
	
	public ObjectListTableEditor(List<Object> columns, PyType rowType, boolean showEmptyRowAtBottom, boolean rowsAreLive)
	{
		this( columns, new PyTypeRowFactory( rowType ), showEmptyRowAtBottom, rowsAreLive, emptyTextCellFactory );
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
	protected AbstractTableEditorInstance<ObjectListInterface> createInstance()
	{
		return new ObjectListTableEditorInstance( this );
	}

	@Override
	protected Object[][] getBlock(ObjectListInterface model, int x, int y, int w, int h)
	{
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
}
