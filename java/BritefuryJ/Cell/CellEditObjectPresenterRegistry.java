//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Cell;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.python.core.PyBoolean;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyObject;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Cell.Presenters.PresentersAWT;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.ObjectPresentation.ObjectPresentationPerspective;
import BritefuryJ.ObjectPresentation.ObjectPresenter;
import BritefuryJ.ObjectPresentation.ObjectPresenterRegistry;
import BritefuryJ.ObjectPresentation.PyObjectPresenter;
import BritefuryJ.Pres.Pres;

public class CellEditObjectPresenterRegistry extends ObjectPresenterRegistry
{
	private PresentersAWT awt = new PresentersAWT();

	
	private CellEditObjectPresenterRegistry()
	{
		registerJavaObjectPresenter( Character.class, presenter_Character );
		registerJavaObjectPresenter( String.class,  presenter_String );
		registerJavaObjectPresenter( Integer.class,  presenter_Integer );
		registerJavaObjectPresenter( Short.class,  presenter_Short );
		registerJavaObjectPresenter( Long.class,  presenter_Long );
		registerJavaObjectPresenter( BigInteger.class,  presenter_BigInteger );
		registerJavaObjectPresenter( BigDecimal.class,  presenter_BigDecimal );
		registerJavaObjectPresenter( Byte.class,  presenter_Byte );
		registerJavaObjectPresenter( Float.class,  presenter_Float );
		registerJavaObjectPresenter( Double.class,  presenter_Double );
		registerJavaObjectPresenter( Boolean.class,  presenter_Boolean );

		registerPythonObjectPresenter( PyInteger.TYPE,  presenter_PyInteger );
		registerPythonObjectPresenter( PyFloat.TYPE,  presenter_PyFloat );
		registerPythonObjectPresenter( PyBoolean.TYPE,  presenter_PyBoolean );
	}

	
	public void registerPerspective(ObjectPresentationPerspective perspective)
	{
		super.registerPerspective( perspective );
	
		awt.registerPerspective( perspective );
	}



	
	
	
	public static final ObjectPresenter presenter_Boolean = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return PrimitiveCellEditPresenter.presentBoolean( (Boolean)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_Byte = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return PrimitiveCellEditPresenter.presentByte( (Byte)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_Character = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return PrimitiveCellEditPresenter.presentChar( (Character)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_Short = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return PrimitiveCellEditPresenter.presentShort( (Short)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_Integer = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return PrimitiveCellEditPresenter.presentInt( (Integer)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_Long = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return PrimitiveCellEditPresenter.presentLong( (Long)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_BigInteger = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return PrimitiveCellEditPresenter.presentBigInteger( (BigInteger)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_BigDecimal = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return PrimitiveCellEditPresenter.presentBigDecimal( (BigDecimal)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_Float = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return PrimitiveCellEditPresenter.presentDouble( ((Float)x).doubleValue(), fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_Double = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return PrimitiveCellEditPresenter.presentDouble( (Double)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_String = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return PrimitiveCellEditPresenter.presentString( (String)x, fragment, inheritedState );
		}
	};
	
	
	

	public static final PyObjectPresenter presenter_PyInteger = new PyObjectPresenter()
	{
		public Pres presentObject(PyObject x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			PyInteger value = (PyInteger)x;
			return PrimitiveCellEditPresenter.presentInt( value.getValue(), fragment, inheritedState );
		}
	};
	
	public static final PyObjectPresenter presenter_PyFloat = new PyObjectPresenter()
	{
		public Pres presentObject(PyObject x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			PyFloat value = (PyFloat)x;
			return PrimitiveCellEditPresenter.presentDouble( value.getValue(), fragment, inheritedState );
		}
	};
	
	public static final PyObjectPresenter presenter_PyBoolean = new PyObjectPresenter()
	{
		public Pres presentObject(PyObject x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			PyBoolean value = (PyBoolean)x;
			return PrimitiveCellEditPresenter.presentBoolean( value.getBooleanValue(), fragment, inheritedState );
		}
	};
	
	
	
	
	
	
	
	public static final CellEditObjectPresenterRegistry instance = new CellEditObjectPresenterRegistry();
}
