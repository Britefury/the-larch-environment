//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DefaultPerspective;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.python.core.Py;
import org.python.core.PyBaseException;
import org.python.core.PyBoolean;
import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PyFloat;
import org.python.core.PyFunction;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyModule;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PyProperty;
import org.python.core.PyString;
import org.python.core.PyStringMap;
import org.python.core.PyTuple;
import org.python.core.PyType;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Controls.DropDownExpander;
import BritefuryJ.DefaultPerspective.Presenters.PresentersAWT;
import BritefuryJ.DefaultPerspective.Presenters.PresentersConcurrency;
import BritefuryJ.DefaultPerspective.Presenters.PresentersJericho;
import BritefuryJ.DefaultPerspective.Presenters.PresentersSQL;
import BritefuryJ.DefaultPerspective.Presenters.PresentersSVG;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.ObjectPresentation.ObjectPresentationPerspective;
import BritefuryJ.ObjectPresentation.ObjectPresenter;
import BritefuryJ.ObjectPresentation.ObjectPresenterRegistry;
import BritefuryJ.ObjectPresentation.PyObjectPresenter;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.ErrorBox;
import BritefuryJ.Pres.ObjectPres.ErrorBoxWithFields;
import BritefuryJ.Pres.ObjectPres.ObjectBox;
import BritefuryJ.Pres.ObjectPres.ObjectBoxWithFields;
import BritefuryJ.Pres.ObjectPres.VerticalField;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.LineBreak;
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.Pres.Primitive.ParagraphDedentMarker;
import BritefuryJ.Pres.Primitive.ParagraphIndentMarker;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.Span;
import BritefuryJ.Pres.Primitive.StaticText;
import BritefuryJ.Pres.Primitive.Whitespace;
import BritefuryJ.Pres.RichText.NormalText;
import BritefuryJ.Pres.Sequence.SpanSequenceView;
import BritefuryJ.Pres.Sequence.TrailingSeparator;
import BritefuryJ.StyleSheet.StyleSheet;

public class DefaultObjectPresenterRegistry extends ObjectPresenterRegistry
{
	private PresentersAWT awt = new PresentersAWT();
	private PresentersSVG svg = new PresentersSVG();
	private PresentersSQL sql = new PresentersSQL();
	private PresentersConcurrency concurrency = new PresentersConcurrency();
	private PresentersJericho jericho = new PresentersJericho();
	
	private DefaultObjectPresenterRegistry()
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
		registerPythonObjectPresenter( PyTuple.TYPE,  presenter_PyTuple );
		registerPythonObjectPresenter( PyType.TYPE,  presenter_PyType );
		registerPythonObjectPresenter( PyFunction.TYPE,  presenter_PyFunction );
		registerPythonObjectPresenter( PyProperty.TYPE,  presenter_PyProperty );
		
		registerJavaObjectPresenter( PyNone.class,  presenter_PyNone );
		registerJavaObjectPresenter( PyException.class,  presenter_PyException );
		registerJavaObjectPresenter( PyBaseException.class,  presenter_PyBaseException );
		registerJavaObjectPresenter( PyModule.class,  presenter_PyModule );

		registerJavaObjectPresenter( Throwable.class,  presenter_Throwable );
		registerJavaObjectPresenter( Exception.class,  presenter_Exception );
		registerJavaObjectPresenter( List.class,  presenter_List );
		registerJavaObjectPresenter( Set.class,  presenter_Set );
		registerJavaObjectPresenter( Map.class,  presenter_Map );
		registerJavaObjectPresenter( Class.class,  presenter_Class );
		registerJavaObjectPresenter( Field.class,  presenter_Field );
		registerJavaObjectPresenter( Constructor.class,  presenter_Constructor );
		registerJavaObjectPresenter( Method.class,  presenter_Method );
	}

	
	public void registerPerspective(ObjectPresentationPerspective perspective)
	{
		super.registerPerspective( perspective );
		
		awt.registerPerspective( perspective );
		svg.registerPerspective( perspective );
		sql.registerPerspective( perspective );
		concurrency.registerPerspective( perspective );
		jericho.registerPerspective( perspective );
	}
	
	public static final ObjectPresenter presenter_Boolean = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return PrimitivePresenter.presentBoolean( (Boolean)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_Byte = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return PrimitivePresenter.presentByte( (Byte)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_Character = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return PrimitivePresenter.presentChar( (Character)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_Short = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return PrimitivePresenter.presentShort( (Short)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_Integer = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return PrimitivePresenter.presentInt( (Integer)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_Long = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return PrimitivePresenter.presentLong( (Long)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_BigInteger = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return PrimitivePresenter.presentBigInteger( (BigInteger)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_BigDecimal = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return PrimitivePresenter.presentBigDecimal( (BigDecimal)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_Float = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return PrimitivePresenter.presentDouble( ((Float)x).doubleValue(), fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_Double = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return PrimitivePresenter.presentDouble( (Double)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_String = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return PrimitivePresenter.presentString( (String)x, fragment, inheritedState );
		}
	};
	
	
	

	public static final PyObjectPresenter presenter_PyInteger = new PyObjectPresenter()
	{
		public Pres presentObject(PyObject x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			PyInteger value = (PyInteger)x;
			return PrimitivePresenter.presentInt( value.getValue(), fragment, inheritedState );
		}
	};
	
	public static final PyObjectPresenter presenter_PyFloat = new PyObjectPresenter()
	{
		public Pres presentObject(PyObject x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			PyFloat value = (PyFloat)x;
			return PrimitivePresenter.presentDouble( value.getValue(), fragment, inheritedState );
		}
	};
	
	public static final PyObjectPresenter presenter_PyBoolean = new PyObjectPresenter()
	{
		public Pres presentObject(PyObject x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			PyBoolean value = (PyBoolean)x;
			return PrimitivePresenter.presentBoolean( value.getBooleanValue(), fragment, inheritedState );
		}
	};
	
	
	public static final PyObjectPresenter presenter_PyTuple = new PyObjectPresenter()
	{
		public Pres presentObject(PyObject x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			PyTuple tuple = (PyTuple)x;
			
			ArrayList<Object> itemViews = new ArrayList<Object>();
			for (Object item: tuple)
			{
				itemViews.add( new InnerFragment( item ) );
			}
			
			return tupleView( itemViews );
		}
	};
	
	
	private static Pres presentPyType(PyType type, FragmentView fragment, SimpleAttributeTable inheritedState)
	{

		ArrayList<Object> contents = new ArrayList<Object>();
		
		
		// Header
		ArrayList<Object> headerItems = new ArrayList<Object>();
		headerItems.add( pythonKeywordStyle.applyTo( new StaticText( "Class " ) ) );
		headerItems.add( classNameStyle.applyTo( new StaticText( type.getName() ) ) );
		
		PyTuple bases = (PyTuple)type.getBases();
		
		if ( bases.size() > 0 )
		{
			headerItems.add( staticStyle.applyTo( new StaticText( " " ) ) );
			headerItems.add( classPunctuationStyle.applyTo( new StaticText( "(" ) ) );
			headerItems.add( new ParagraphIndentMarker() );
			Object baseNames[] = new Object[bases.getArray().length];
			int index = 0;
			for (PyObject base: bases.getArray())
			{
				PyType baseType = (PyType)base;
				baseNames[index++] = classNameStyle.applyTo( new StaticText( baseType.getName() ) );
			}
			headerItems.add( commaSeparatedListView( Arrays.asList( baseNames ) ) );
			headerItems.add( new ParagraphDedentMarker() );
			headerItems.add( classPunctuationStyle.applyTo( new StaticText( ")" ) ) );
		}
		
		contents.add( new Paragraph( headerItems ) );
		
		
		// Documentation
		PyObject doc = type.getDoc();
		if ( doc != null  &&  doc != Py.None )
		{
			Pres docPres = new Column( NormalText.paragraphs( doc.toString() ) );
			contents.add( new DropDownExpander( sectionHeadingStyle.applyTo( new Label( "Documentation" ) ), docPres ) );
		}
		
		
		ArrayList<Object> attributes = new ArrayList<Object>();
		ArrayList<Object> methods = new ArrayList<Object>();
		ArrayList<Object> properties = new ArrayList<Object>();
		// Attributes
		PyObject dict = type.fastGetDict();
		PyList dictItems;
		if ( dict instanceof PyDictionary )
		{
			dictItems = ((PyDictionary)dict).items();
		}
		else if ( dict instanceof PyStringMap )
		{
			dictItems = ((PyStringMap)dict).items();
		}
		else
		{
			throw new RuntimeException( "Expected a PyDictionary or a PyStringMap when acquiring __dict__ from a PyType" );
		}
		
		
		for (Object dictItem: dictItems)
		{
			PyTuple pair = (PyTuple)dictItem;
			PyObject key = pair.getArray()[0];
			PyObject value = pair.getArray()[1];
			String name = key.toString();
			
			if ( name.equals( "__dict__" )  ||  name.equals( "__module__" )  ||  name.equals(  "__doc__" ) )
			{
				break;
			}
			
			if ( value instanceof PyFunction )
			{
				methods.add( presentPyFunctionHeader( (PyFunction)value, name ) );
			}
			else if ( value instanceof PyProperty )
			{
				properties.add( presentPyPropertyHeader( (PyProperty)value, name ) );
			}
			else
			{
				Pres namePres = attributeNameStyle.applyTo( new Label( name ) );
				Pres valueView = new InnerFragment( value ).padX( 15.0, 0.0 );
				attributes.add( new Column( new Pres[] { namePres, valueView } ) );
			}
		}
		
		if ( methods.size() > 0 )
		{
			contents.add( new DropDownExpander( sectionHeadingStyle.applyTo( new Label( "Methods" ) ),   new Column( methods ) ) );
		}
		if ( properties.size() > 0 )
		{
			contents.add( new DropDownExpander( sectionHeadingStyle.applyTo( new Label( "Properties" ) ),   new Column( properties ) ) );
		}
		if ( attributes.size() > 0 )
		{
			contents.add( new DropDownExpander( sectionHeadingStyle.applyTo( new Label( "Attributes" ) ),   new Column( attributes ) ) );
		}
		
		
		return new Column( contents );
	}
	

	public static final PyObjectPresenter presenter_PyType = new PyObjectPresenter()
	{
		public Pres presentObject(PyObject x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			PyType type = (PyType)x;
			
			Pres classPres = presentPyType( type, fragment, inheritedState );
			
			return new ObjectBox( "Python Class", classPres );
		}
	};
	
	

	private static PyFunction pyFunction_inspectFn = null;
	private static PyFunction getPythonInspectFunction()
	{
		if ( pyFunction_inspectFn == null )
		{
			String code = "import inspect\n" +
			"\n" +
			"def _flatten(xs):\n" +
			"	ys = []\n" +
			"	for x in xs:\n" +
			"		if isinstance( x, list ):\n" +
			"			ys.extend( _flatten( x ) )\n" +
			"		else:\n" +
			"			ys.append( x )\n" +
			"	return ys\n" +
			"\n" +
			"def inspectFunction(f):\n" +
			"	args, varargs, varkw, defaults = inspect.getargspec( f )\n" +
			"	args = _flatten( args )\n" +
			"	kwargs = []\n" + 
			"	if defaults is not None:\n" +
			"		kwargs = args[-len(defaults):]\n" +
			"		del args[-len(defaults):]\n" +
			"	return args, kwargs, varargs, varkw\n";
			
			PyDictionary locals = new PyDictionary();
			
			Py.exec( new PyString( code ), locals, locals );
			
			pyFunction_inspectFn = (PyFunction)locals.get( new PyString( "inspectFunction" ) );
		}
		
		return pyFunction_inspectFn;
	}
	
	private static PyTuple inspectPyFunction(PyFunction fun)
	{
		return (PyTuple)getPythonInspectFunction().__call__( fun );
	}
	
	
	private static Pres presentPyFunctionHeader(PyFunction fun, String name)
	{
		PyTuple argSpec = inspectPyFunction( fun );
		PyObject args = argSpec.getArray()[0];
		PyObject kwargs = argSpec.getArray()[1];
		PyObject varargs = argSpec.getArray()[2];
		PyObject varkw = argSpec.getArray()[3];
		
		
		if ( name == null )
		{
			name = fun.__name__;
		}
		
		
		ArrayList<Object> header = new ArrayList<Object>();
		header.add( fnNameStyle.applyTo( new StaticText( name ) ) );
		header.add( fnPunctuationStyle.applyTo( new StaticText( "(" ) ) );
		boolean bFirst = true;
		for (PyObject arg: ((PyList)args).getArray())
		{
			if ( !bFirst )
			{
				header.add( fnPunctuationStyle.applyTo( new StaticText( ", " ) ) );
			}
			header.add( fnArgStyle.applyTo( new StaticText( arg.toString() ) ) );
			bFirst = false;
		}
		for (PyObject arg: ((PyList)kwargs).getArray())
		{
			if ( !bFirst )
			{
				header.add( fnPunctuationStyle.applyTo( new StaticText( ", " ) ) );
			}
			header.add( fnKWArgStyle.applyTo( new StaticText( arg.toString() ) ) );
			bFirst = false;
		}
		if ( varargs != Py.None )
		{
			if ( !bFirst )
			{
				header.add( fnPunctuationStyle.applyTo( new StaticText( ", " ) ) );
			}
			header.add( fnPunctuationStyle.applyTo( new StaticText( "*" ) ) );
			header.add( fnVarArgStyle.applyTo( new StaticText( varargs.toString() ) ) );
			bFirst = false;
		}
		if ( varkw != Py.None )
		{
			if ( !bFirst )
			{
				header.add( fnPunctuationStyle.applyTo( new StaticText( ", " ) ) );
			}
			header.add( fnPunctuationStyle.applyTo( new StaticText( "**" ) ) );
			header.add( fnVarArgStyle.applyTo( new StaticText( varkw.toString() ) ) );
			bFirst = false;
		}
		header.add( fnPunctuationStyle.applyTo( new StaticText( ")" ) ) );
		
		return new Paragraph( header );
	}

	
	
	public static final PyObjectPresenter presenter_PyFunction = new PyObjectPresenter()
	{
		public Pres presentObject(PyObject x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			PyFunction fun = (PyFunction)x;
			
			Pres header = presentPyFunctionHeader( fun, null );
			
			return new ObjectBox( "Python Function", header );
		}
	};

	

	
	private static Pres presentPyPropertyHeader(PyProperty prop, String name)
	{
		ArrayList<Object> header = new ArrayList<Object>();
		header.add( propertyNameStyle.applyTo( new StaticText( name ) ) );
		return new Paragraph( header );
	}

	
	
	public static final PyObjectPresenter presenter_PyProperty = new PyObjectPresenter()
	{
		public Pres presentObject(PyObject x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			PyProperty prop = (PyProperty)x;
			
			Pres header = presentPyPropertyHeader( prop, null );
			
			return new ObjectBox( "Python property", header );
		}
	};

	

	
	public static final ObjectPresenter presenter_PyModule = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			PyModule module = (PyModule)x;
			PyObject dict = module.fastGetDict();
			
			
			ArrayList<Object> contents = new ArrayList<Object>();
			
			
			// Header
			String moduleName = dict.__getitem__( new PyString( "__name__" ) ).toString();
			contents.add( pythonKeywordStyle.applyTo( new StaticText( "Module" ) ).alignHCentre() );
			contents.add( moduleNameStyle.applyTo( new StaticText( moduleName ) ).alignHCentre() );
			
			
			// Documentation
			PyObject doc = null;
			try
			{
				doc = dict.__getitem__( new PyString( "__doc__" ) );
			}
			catch (PyException e)
			{
			}
			
			if ( doc != null  &&  doc != Py.None )
			{
				Pres docPres = new Column( NormalText.paragraphs( doc.toString() ) );
				contents.add( new DropDownExpander( sectionHeadingStyle.applyTo( new Label( "Documentation" ) ), docPres ) );
			}
			
			
			ArrayList<Object> attributes = new ArrayList<Object>();
			// Attributes
			PyList dictItems;
			if ( dict instanceof PyDictionary )
			{
				dictItems = ((PyDictionary)dict).items();
			}
			else if ( dict instanceof PyStringMap )
			{
				dictItems = ((PyStringMap)dict).items();
			}
			else
			{
				throw new RuntimeException( "Expected a PyDictionary or a PyStringMap when acquiring __dict__ from a PyType" );
			}
			
			
			for (Object dictItem: dictItems)
			{
				PyTuple pair = (PyTuple)dictItem;
				PyObject key = pair.getArray()[0];
				PyObject value = pair.getArray()[1];
				String name = key.toString();
				
				if ( name.equals( "__dict__" )  ||  name.equals(  "__doc__" ) )
				{
					break;
				}
				
				Pres attrPres = null;
				
				if ( value instanceof PyFunction )
				{
					attrPres = presentPyFunctionHeader( (PyFunction)value, name );
				}
				else
				{
					Pres namePres = attributeNameStyle.applyTo( new Label( name ) );
					Pres valueView = new InnerFragment( value ).padX( 15.0, 0.0 );
					attrPres = new Column( new Pres[] { namePres, valueView } );
				}
				
				attributes.add( attrPres );
			}
			
			if ( attributes.size() > 0 )
			{
				contents.add( new DropDownExpander( sectionHeadingStyle.applyTo( new Label( "Attributes" ) ),   new Column( attributes ) ) );
			}
			
			
			return new ObjectBox( "Python Module", new Column( contents ) );
		}
	};

	
	
	
	public static final ObjectPresenter presenter_PyNone = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return PrimitivePresenter.presentNone();
		}
	};

	
	
	public static final ObjectPresenter presenter_PyException = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			PyException e = (PyException)x;
			
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			e.printStackTrace( new PrintStream( buf ) );
			String stackTrace = e.toString();
			String stackTraceLines[] = stackTrace.split( "\n" );
			Pres stackTraceElements[] = new Pres[stackTraceLines.length];
			
			for (int i = 0; i < stackTraceLines.length; i++)
			{
				stackTraceElements[i] = stackTraceStyle.applyTo( new StaticText( stackTraceLines[i] ) );
			}
			
			Pres fields[] = {
					new VerticalField( "Message", new InnerFragment( e.value ) ),
					new VerticalField( "Traceback", new Column( stackTraceElements ) )
			};
			
			return new ErrorBoxWithFields( "PYTHON EXCEPTION", fields );
		}
	};

	
	
	public static final ObjectPresenter presenter_PyBaseException = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			PyBaseException e = (PyBaseException)x;
			
			String lines[] = e.toString().split( "\n" );
			Pres lineTexts[] = new Pres[lines.length];
			
			for (int i = 0; i < lines.length; i++)
			{
				lineTexts[i] = new Label( lines[i] );
			}
			
			Pres contents = new Column( lineTexts );
			
			return new ErrorBox( "PYTHON EXCEPTION", contents );
		}
	};

	
	
	public static final ObjectPresenter presenter_Throwable = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			Throwable t = (Throwable)x;
			
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			t.printStackTrace( new PrintStream( buf ) );
			String stackTrace = t.toString();
			String stackTraceLines[] = stackTrace.split( "\n" );
			Pres stackTraceElements[] = new Pres[stackTraceLines.length];
			
			for (int i = 0; i < stackTraceLines.length; i++)
			{
				stackTraceElements[i] = stackTraceStyle.applyTo( new StaticText( stackTraceLines[i] ) );
			}
			
			Pres fields[] = {
					new VerticalField( "Message", new InnerFragment( t.getMessage() ) ),
					new VerticalField( "Traceback", new Column( stackTraceElements ) )
			};
			
			return new ObjectBoxWithFields( "JAVA THROWABLE", fields );
		}
	};


	
	public static final ObjectPresenter presenter_Exception = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			Exception e = (Exception)x;
			
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			e.printStackTrace( new PrintStream( buf ) );
			String stackTrace;
			try
			{
				stackTrace = buf.toString( "ISO-8859-1" );
			}
			catch (UnsupportedEncodingException e1)
			{
				stackTrace = e.toString();
			}
			String stackTraceLines[] = stackTrace.split( "\n" );
			Pres stackTraceElements[] = new Pres[stackTraceLines.length];
			
			for (int i = 0; i < stackTraceLines.length; i++)
			{
				stackTraceElements[i] = stackTraceStyle.applyTo( new StaticText( stackTraceLines[i] ) );
			}
			
			Pres fields[] = {
					new VerticalField( "Message", new InnerFragment( e.getMessage() ) ),
					new VerticalField( "Traceback", new Column( stackTraceElements ) )
			};
			
			return new ErrorBoxWithFields( "JAVA EXCEPTION", fields );
		}
	};


	
	public static final ObjectPresenter presenter_List = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			List<?> list = (List<?>)x;
			
			ArrayList<Object> itemViews = new ArrayList<Object>();
			for (Object item: list)
			{
				itemViews.add( new InnerFragment( item ) );
			}
			
			return listView( itemViews );
		}
	};
	
	public static final ObjectPresenter presenter_Set = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			Set<?> set = (Set<?>)x;
			
			ArrayList<Object> itemViews = new ArrayList<Object>();
			for (Object item: set)
			{
				itemViews.add( new InnerFragment( item ) );
			}
			
			return setView( itemViews );
		}
	};
	
	public static final ObjectPresenter presenter_Map = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			Map<?,?> map = (Map<?,?>)x;
			
			ArrayList<Object> itemViews = new ArrayList<Object>();
			for (Map.Entry<?,?> entry: map.entrySet())
			{
				Pres lineElems[] = { new InnerFragment( entry.getKey() ),
						staticStyle.applyTo( new Whitespace( " ", 10.0 ) ),
						delimStyle.applyTo( new StaticText( ":" ) ),
						staticStyle.applyTo( new Whitespace( " ", 10.0 ) ),
						new InnerFragment( entry.getValue() ) };
				itemViews.add( new Span( lineElems ) );
			}
			
			return mapView( itemViews );
		}
	};
	
	
	
	private static Pres presentClassHeader(Class<?> cls)
	{
		Class<?> superClass = cls.getSuperclass();
		Class<?> interfaces[] = cls.getInterfaces();
		
		ArrayList<Object> lines = new ArrayList<Object>();
		
		Pres headerName = new Row( new Pres[] { javaKeywordStyle.applyTo( new StaticText( "Class " ) ), 
				PrimitivePresenter.presentJavaClassName( cls, classNameStyle ) } );
		
		lines.add( headerName );
		
		if ( superClass != null  ||  interfaces.length > 0 )
		{
			ArrayList<Object> inheritanceItems = new ArrayList<Object>();
			if ( superClass != null )
			{
				inheritanceItems.add( javaKeywordStyle.applyTo( new StaticText( "Extends " ) ) );
				inheritanceItems.add( PrimitivePresenter.presentJavaClassName( superClass, classNameStyle ) );
			}
			
			if ( interfaces.length > 0 )
			{
				if ( superClass != null )
				{
					inheritanceItems.add( javaKeywordStyle.applyTo( new StaticText( " " ) ) );
					inheritanceItems.add( new LineBreak() );
				}
				inheritanceItems.add( javaKeywordStyle.applyTo( new StaticText( "Implements " ) ) );
				boolean bFirst = true;
				for (Class<?> iface: interfaces)
				{
					if ( !bFirst )
					{
						inheritanceItems.add( classPunctuationStyle.applyTo( new StaticText( ", " ) ) );
						inheritanceItems.add( new LineBreak() );
					}
					inheritanceItems.add( PrimitivePresenter.presentJavaClassName( iface, classNameStyle ) );
					bFirst = false;
				}
			}
			
			Pres inheritance = new Paragraph( inheritanceItems );
			
			return new Column( new Pres[] { headerName, inheritance.padX( 45.0, 0.0 ) } );
		}
		else
		{
			return headerName;
		}
	}


	private static Pres presentClassDeclaration(Class<?> cls)
	{
		Constructor<?> declaredConstructors[] = cls.getDeclaredConstructors();
		Method declaredMethods[] = cls.getDeclaredMethods();
		Field declaredFields[] = cls.getDeclaredFields();
		Class<?> declaredClasses[] = cls.getDeclaredClasses();
		
		ArrayList<Object> contents = new ArrayList<Object>();
		
		contents.add( presentClassHeader( cls ) );
		
		// Members
		ArrayList<Object> constructorDeclarations = new ArrayList<Object>();
		ArrayList<Object> staticFieldDeclarations = new ArrayList<Object>();
		ArrayList<Object> fieldDeclarations = new ArrayList<Object>();
		ArrayList<Object> staticMethodDeclarations = new ArrayList<Object>();
		ArrayList<Object> methodDeclarations = new ArrayList<Object>();
		ArrayList<Object> classDeclarations = new ArrayList<Object>();
		for (int i = 0; i < declaredConstructors.length; i++)
		{
			constructorDeclarations.add( presentConstructorDeclaration( declaredConstructors[i] ) );
		}
		for (int i = 0; i < declaredFields.length; i++)
		{
			if ( Modifier.isStatic( declaredFields[i].getModifiers() ) )
			{
				staticFieldDeclarations.add( presentFieldDeclaration( declaredFields[i] ) );
			}
			else
			{
				fieldDeclarations.add( presentFieldDeclaration( declaredFields[i] ) );
			}
		}
		for (int i = 0; i < declaredMethods.length; i++)
		{
			if ( Modifier.isStatic( declaredMethods[i].getModifiers() ) )
			{
				staticMethodDeclarations.add( presentMethodDeclaration( declaredMethods[i] ) );
			}
			else
			{
				methodDeclarations.add( presentMethodDeclaration( declaredMethods[i] ) );
			}
		}
		for (int i = 0; i < declaredClasses.length; i++)
		{
			classDeclarations.add( new InnerFragment( declaredClasses[i] ) );
		}
		
		if ( constructorDeclarations.size() > 0 )
		{
			contents.add( new DropDownExpander( sectionHeadingStyle.applyTo( new Label( "Constructors" ) ),   new Column( constructorDeclarations ) ) );
		}

		if ( methodDeclarations.size() > 0 )
		{
			contents.add( new DropDownExpander( sectionHeadingStyle.applyTo( new Label( "Methods" ) ),   new Column( methodDeclarations ) ) );
		}

		if ( staticMethodDeclarations.size() > 0 )
		{
			contents.add( new DropDownExpander( sectionHeadingStyle.applyTo( new Label( "Static methods" ) ),   new Column( staticMethodDeclarations ) ) );
		}

		if ( fieldDeclarations.size() > 0 )
		{
			contents.add( new DropDownExpander( sectionHeadingStyle.applyTo( new Label( "Fields" ) ),   new Column( fieldDeclarations ) ) );
		}

		if ( staticFieldDeclarations.size() > 0 )
		{
			contents.add( new DropDownExpander( sectionHeadingStyle.applyTo( new Label( "Static fields" ) ),   new Column( staticFieldDeclarations ) ) );
		}

		if ( classDeclarations.size() > 0 )
		{
			contents.add( new DropDownExpander( sectionHeadingStyle.applyTo( new Label( "Classes" ) ),   new Column( classDeclarations ) ) );
		}
		
		return new Column( contents );
	}

	
	
	public static final ObjectPresenter presenter_Class = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			Class<?> cls = (Class<?>)x;
			
			Pres clsPres = presentClassDeclaration( cls );
			
			return new ObjectBox( "Java Class", clsPres );
		}
	};
	

	
	
	
	private static Pres presentFieldDeclaration(Field field)
	{
		ArrayList<Object> words = new ArrayList<Object>();
		
		// type
		words.add( PrimitivePresenter.presentJavaClassName( field.getType(), typeNameStyle ) );
		words.add( space );
		words.add( new LineBreak() );
		// name
		words.add( PrimitivePresenter.getAccessNameStyle( field.getModifiers() ).applyTo( new Label( field.getName() ) ) );
		
		return new Paragraph( words );
	}

	public static final ObjectPresenter presenter_Field = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			Field field = (Field)x;
			return new ObjectBox( "Java Field", presentFieldDeclaration( field ) );
		}
	};

	
	
	
	private static Pres presentConstructorDeclaration(Constructor<?> constructor)
	{
		ArrayList<Object> words = new ArrayList<Object>();
		
		// type
		words.add( PrimitivePresenter.presentJavaClassName( constructor.getDeclaringClass(), PrimitivePresenter.getAccessNameStyle( constructor.getModifiers() ) ) );
		// open paren
		words.add( delimStyle.applyTo( new Label( "(" ) ) );
		Class<?> paramTypes[] = constructor.getParameterTypes(); 
		Object params[] = new Object[paramTypes.length];
		for (int i = 0; i < params.length; i++)
		{
			params[i] = PrimitivePresenter.presentJavaClassName( paramTypes[i], typeNameStyle );
		}
		words.add( commaSeparatedListView( Arrays.asList( params ) ) );
		// close paren
		words.add( delimStyle.applyTo( new Label( ")" ) ) );
		
		Class<?> exceptionTypes[] = constructor.getExceptionTypes();
		if ( exceptionTypes.length > 0 )
		{
			words.add( space );
			words.add( new LineBreak() );
			words.add( javaKeywordStyle.applyTo( new Label( "Throws" ) ) );
			words.add( space );
			Object exceptions[] = new Object[exceptionTypes.length];
			for (int i = 0; i < params.length; i++)
			{
				exceptions[i] = PrimitivePresenter.presentJavaClassName( exceptionTypes[i], typeNameStyle );
			}
			words.add( commaSeparatedListView( Arrays.asList( exceptions ) ) );
		}
		
		return new Paragraph( words );
	}

	public static final ObjectPresenter presenter_Constructor = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			Constructor<?> constructor = (Constructor<?>)x;
			return new ObjectBox( "Java Constructor", presentConstructorDeclaration( constructor ) );
		}
	};

	
	
	private static Pres presentMethodDeclaration(Method method)
	{
		ArrayList<Object> words = new ArrayList<Object>();
		
		// return type
		words.add( PrimitivePresenter.presentJavaClassName( method.getReturnType(), typeNameStyle ) );
		words.add( space );
		words.add( new LineBreak() );
		// name
		words.add( PrimitivePresenter.getAccessNameStyle( method.getModifiers() ).applyTo( new Label( method.getName() ) ) );
		// open paren
		words.add( delimStyle.applyTo( new Label( "(" ) ) );
		Class<?> paramTypes[] = method.getParameterTypes(); 
		Object params[] = new Object[paramTypes.length];
		for (int i = 0; i < paramTypes.length; i++)
		{
			params[i] = PrimitivePresenter.presentJavaClassName( paramTypes[i], typeNameStyle );
		}
		words.add( commaSeparatedListView( Arrays.asList( params ) ) );
		// close paren
		words.add( delimStyle.applyTo( new Label( ")" ) ) );
		
		Class<?> exceptionTypes[] = method.getExceptionTypes();
		if ( exceptionTypes.length > 0 )
		{
			words.add( space );
			words.add( new LineBreak() );
			words.add( javaKeywordStyle.applyTo( new Label( "Throws" ) ) );
			words.add( space );
			Object exceptions[] = new Object[exceptionTypes.length];
			for (int i = 0; i < exceptionTypes.length; i++)
			{
				exceptions[i] = PrimitivePresenter.presentJavaClassName( exceptionTypes[i], typeNameStyle );
			}
			words.add( commaSeparatedListView( Arrays.asList( exceptions ) ) );
		}
		
		return new Paragraph( words );
	}

	public static final ObjectPresenter presenter_Method = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			Method method = (Method)x;
			return new ObjectBox( "Java Method", presentMethodDeclaration( method ) );
		}
	};




	private static final StyleSheet staticStyle = StyleSheet.instance.withAttr( Primitive.editable, false );
	
	
	private static final StyleSheet punctuationStyle = staticStyle.withAttr( Primitive.foreground, Color.blue );
	private static final StyleSheet delimStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.1f, 0.3f, 0.4f ) ).withAttr( Primitive.fontBold, true ).withAttr( Primitive.fontSize, 14 );
	private static final StyleSheet sectionHeadingStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.0f, 0.5f ) ).withAttr( Primitive.fontBold, true ).withAttr( Primitive.fontFace, "Serif" );
	
	
	private static final StyleSheet stackTraceStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.75f, 0.1f, 0.4f ) );

	
	private static final StyleSheet javaKeywordStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.0f, 0.5f ) ).withAttr( Primitive.fontBold, true ).withAttr( Primitive.fontSmallCaps, true );
	private static final StyleSheet pythonKeywordStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.0f, 0.5f ) ).withAttr( Primitive.fontBold, true ).withAttr( Primitive.fontSmallCaps, true );
	private static final StyleSheet typeKeywordStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.3f, 0.5f ) ).withAttr( Primitive.fontBold, true ).withAttr( Primitive.fontSmallCaps, true );
	
	private static final StyleSheet classPunctuationStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.25f, 0.0f, 0.5f ) );
	
	private static final StyleSheet classNameStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.25f, 0.5f ) );

	private static final StyleSheet typeNameStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.4f ) );

	private static final StyleSheet attributeNameStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.0f, 0.25f ) );
	private static final StyleSheet propertyNameStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.5f, 0.0f, 0.15f ) );
	private static final StyleSheet moduleNameStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.0f ) ).withAttr( Primitive.fontSize, 18 ).withAttr( Primitive.fontBold, true );

	private static final StyleSheet fnPunctuationStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.25f, 0.0f, 0.5f ) );
	private static final StyleSheet fnNameStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.25f, 0.5f ) );
	private static final StyleSheet fnArgStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.25f ) );
	private static final StyleSheet fnKWArgStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.25f ) ).withAttr( Primitive.fontItalic, true );
	private static final StyleSheet fnVarArgStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.25f ) );


	private static final Pres comma = punctuationStyle.applyTo( new StaticText( "," ) );
	private static final Pres space = staticStyle.applyTo( new StaticText( " " ) );
	private static final Pres mapSpace = staticStyle.applyTo( new Whitespace( " ", 25.0 ) );
	private static final Pres openBracket = delimStyle.applyTo( new StaticText( "[" ) );
	private static final Pres closeBracket = delimStyle.applyTo( new StaticText( "]" ) );
	private static final Pres openParen = delimStyle.applyTo( new StaticText( "(" ) );
	private static final Pres closeParen = delimStyle.applyTo( new StaticText( ")" ) );
	private static final Pres openBrace = delimStyle.applyTo( new StaticText( "{" ) );
	private static final Pres closeBrace = delimStyle.applyTo( new StaticText( "}" ) );
	private static final Pres openChevronBracket = delimStyle.applyTo( new Label( "<[" ) );
	private static final Pres closeChevronBracket = delimStyle.applyTo( new Label( "]>" ) );
	private static final Pres setOpenDelim = new Row( new Pres[] { typeKeywordStyle.applyTo( new StaticText( "Set", "set" ) ),  delimStyle.applyTo( new StaticText( "(" ) ) } );
	private static final Pres setCloseDelim = delimStyle.applyTo( new StaticText( ")" ) );
	

	public static Pres arrayView(List<Object> children)
	{
		return new SpanSequenceView( children, openChevronBracket, closeChevronBracket, comma, space, TrailingSeparator.NEVER );
	}
	
	private static Pres listView(List<Object> children)
	{
		return new SpanSequenceView( children, openBracket, closeBracket, comma, space, TrailingSeparator.NEVER );
	}
	
	private static Pres tupleView(List<Object> children)
	{
		return new SpanSequenceView( children, openParen, closeParen, comma, space, TrailingSeparator.ONE_ELEMENT );
	}
	
	private static Pres setView(List<Object> children)
	{
		return new SpanSequenceView( children, setOpenDelim, setCloseDelim, comma, space, TrailingSeparator.NEVER );
	}
	
	private static Pres mapView(List<Object> children)
	{
		return new SpanSequenceView( children, openBrace, closeBrace, comma, mapSpace, TrailingSeparator.NEVER );
	}

	private static Pres commaSeparatedListView(List<Object> children)
	{
		return new SpanSequenceView( children, null, null, comma, space, TrailingSeparator.NEVER );
	}




	public static final DefaultObjectPresenterRegistry instance = new DefaultObjectPresenterRegistry();
}
