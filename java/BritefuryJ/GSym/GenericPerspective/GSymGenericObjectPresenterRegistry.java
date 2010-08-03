//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.GenericPerspective;

import java.awt.Color;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyList;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyTuple;
import org.python.core.PyType;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Box;
import BritefuryJ.DocPresent.Combinators.Primitive.HBox;
import BritefuryJ.DocPresent.Combinators.Primitive.Paragraph;
import BritefuryJ.DocPresent.Combinators.Primitive.ParagraphDedentMarker;
import BritefuryJ.DocPresent.Combinators.Primitive.ParagraphIndentMarker;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Span;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;
import BritefuryJ.DocPresent.Combinators.Primitive.Whitespace;
import BritefuryJ.DocPresent.Combinators.Sequence.SpanSequenceView;
import BritefuryJ.DocPresent.Combinators.Sequence.TrailingSeparator;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.GSym.GenericPerspective.PresCom.ErrorBoxWithFields;
import BritefuryJ.GSym.GenericPerspective.PresCom.GenericPerspectiveInnerFragment;
import BritefuryJ.GSym.GenericPerspective.PresCom.GenericStyle;
import BritefuryJ.GSym.GenericPerspective.PresCom.ObjectBorder;
import BritefuryJ.GSym.GenericPerspective.PresCom.ObjectBox;
import BritefuryJ.GSym.GenericPerspective.PresCom.ObjectBoxWithFields;
import BritefuryJ.GSym.GenericPerspective.PresCom.ObjectTitle;
import BritefuryJ.GSym.GenericPerspective.PresCom.VerticalField;
import BritefuryJ.GSym.ObjectPresentation.GSymObjectPresenterRegistry;
import BritefuryJ.GSym.ObjectPresentation.ObjectPresenter;
import BritefuryJ.GSym.ObjectPresentation.PyObjectPresenter;
import BritefuryJ.GSym.View.GSymFragmentView;

public class GSymGenericObjectPresenterRegistry extends GSymObjectPresenterRegistry
{
	public GSymGenericObjectPresenterRegistry()
	{
		registerJavaObjectPresenter( Character.class, presenter_Character );
		registerJavaObjectPresenter( String.class,  presenter_String );
		registerJavaObjectPresenter( Integer.class,  presenter_Integer );
		registerJavaObjectPresenter( Short.class,  presenter_Short );
		registerJavaObjectPresenter( Long.class,  presenter_Long );
		registerJavaObjectPresenter( Byte.class,  presenter_Byte );
		registerJavaObjectPresenter( Float.class,  presenter_Float );
		registerJavaObjectPresenter( Double.class,  presenter_Double );
		registerJavaObjectPresenter( Boolean.class,  presenter_Boolean );

		registerPythonObjectPresenter( PyTuple.TYPE,  presenter_PyTuple );
		registerPythonObjectPresenter( PyType.TYPE,  presenter_PyType );
		registerPythonObjectPresenter( PyFunction.TYPE,  presenter_PyFunction );
		
		registerJavaObjectPresenter( PyNone.class,  presenter_PyNone );
		registerJavaObjectPresenter( PyException.class,  presenter_PyException );

		registerJavaObjectPresenter( Exception.class,  presenter_Exception );
		registerJavaObjectPresenter( List.class,  presenter_List );
		registerJavaObjectPresenter( Map.class,  presenter_Map );
		registerJavaObjectPresenter( BufferedImage.class,  presenter_BufferedImage );
		registerJavaObjectPresenter( Shape.class,  presenter_Shape );
		registerJavaObjectPresenter( Color.class,  presenter_Color );
		registerJavaObjectPresenter( Class.class,  presenter_Class );
	}

	
	
	public static final ObjectPresenter presenter_Boolean = new ObjectPresenter()
	{
		public Pres presentObject(Object x, GSymFragmentView fragment, AttributeTable inheritedState)
		{
			return GSymPrimitivePresenter.presentBoolean( (Boolean)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_Byte = new ObjectPresenter()
	{
		public Pres presentObject(Object x, GSymFragmentView fragment, AttributeTable inheritedState)
		{
			return GSymPrimitivePresenter.presentByte( (Byte)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_Character = new ObjectPresenter()
	{
		public Pres presentObject(Object x, GSymFragmentView fragment, AttributeTable inheritedState)
		{
			return GSymPrimitivePresenter.presentChar( (Character)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_Short = new ObjectPresenter()
	{
		public Pres presentObject(Object x, GSymFragmentView fragment, AttributeTable inheritedState)
		{
			return GSymPrimitivePresenter.presentShort( (Short)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_Integer = new ObjectPresenter()
	{
		public Pres presentObject(Object x, GSymFragmentView fragment, AttributeTable inheritedState)
		{
			return GSymPrimitivePresenter.presentInt( (Integer)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_Long = new ObjectPresenter()
	{
		public Pres presentObject(Object x, GSymFragmentView fragment, AttributeTable inheritedState)
		{
			return GSymPrimitivePresenter.presentLong( (Long)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_Float = new ObjectPresenter()
	{
		public Pres presentObject(Object x, GSymFragmentView fragment, AttributeTable inheritedState)
		{
			return GSymPrimitivePresenter.presentDouble( ((Float)x).doubleValue(), fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_Double = new ObjectPresenter()
	{
		public Pres presentObject(Object x, GSymFragmentView fragment, AttributeTable inheritedState)
		{
			return GSymPrimitivePresenter.presentDouble( (Double)x, fragment, inheritedState );
		}
	};
	
	public static final ObjectPresenter presenter_String = new ObjectPresenter()
	{
		public Pres presentObject(Object x, GSymFragmentView fragment, AttributeTable inheritedState)
		{
			return GSymPrimitivePresenter.presentString( (String)x, fragment, inheritedState );
		}
	};
	
	
	

	public static final PyObjectPresenter presenter_PyTuple = new PyObjectPresenter()
	{
		public Pres presentObject(PyObject x, GSymFragmentView fragment, AttributeTable inheritedState)
		{
			PyTuple tuple = (PyTuple)x;
			
			ArrayList<Object> itemViews = new ArrayList<Object>();
			for (Object item: tuple)
			{
				itemViews.add( new GenericPerspectiveInnerFragment( item ) );
			}
			
			return tupleView( itemViews );
		}
	};

	public static final PyObjectPresenter presenter_PyType = new PyObjectPresenter()
	{
		public Pres presentObject(PyObject x, GSymFragmentView fragment, AttributeTable inheritedState)
		{
			PyType type = (PyType)x;
			
			ArrayList<Object> lines = new ArrayList<Object>();
			
			ArrayList<Object> header = new ArrayList<Object>();
			header.add( classKeywordStyle.applyTo( new StaticText( "Class " ) ) );
			header.add( classNameStyle.applyTo( new StaticText( type.getName() ) ) );
			
			PyTuple bases = (PyTuple)type.getBases();
			
			if ( bases.size() > 0 )
			{
				header.add( staticStyle.applyTo( new StaticText( " " ) ) );
				header.add( classPunctuationStyle.applyTo( new StaticText( "(" ) ) );
				header.add( staticStyle.applyTo( new ParagraphIndentMarker() ) );
				boolean bFirst = true;
				for (PyObject base: bases.getArray())
				{
					PyType baseType = (PyType)base;
					if ( !bFirst )
					{
						header.add( classPunctuationStyle.applyTo( new StaticText( ", " ) ) );
					}
					header.add( classNameStyle.applyTo( new StaticText( baseType.getName() ) ) );
					bFirst = false;
				}
				header.add( staticStyle.applyTo( new ParagraphDedentMarker() ) );
				header.add( classPunctuationStyle.applyTo( new StaticText( ")" ) ) );
			}
			
			lines.add( new Paragraph( header ) );
			
			return new ObjectBoxWithFields( "Python Class", lines );
		}
	};

	private static PyFunction pyFunction_inspectFn = null;
	public static final PyObjectPresenter presenter_PyFunction = new PyObjectPresenter()
	{
		
		private PyFunction getInspectFunction()
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
		
		private PyTuple inspectFunction(PyFunction fun)
		{
			return (PyTuple)getInspectFunction().__call__( fun );
		}
		
		
		
		
		
		public Pres presentObject(PyObject x, GSymFragmentView fragment, AttributeTable inheritedState)
		{
			PyFunction fun = (PyFunction)x;
			
			PyTuple argSpec = inspectFunction( fun );
			PyObject args = argSpec.getArray()[0];
			PyObject kwargs = argSpec.getArray()[1];
			PyObject varargs = argSpec.getArray()[2];
			PyObject varkw = argSpec.getArray()[3];
			
			
			ArrayList<Object> lines = new ArrayList<Object>();
			
			ArrayList<Object> header = new ArrayList<Object>();
			header.add( fnNameStyle.applyTo( new StaticText( fun.__name__.toString() ) ) );
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
			
			lines.add( new Paragraph( header ) );
			
			return new ObjectBoxWithFields( "Python Function", lines );
		}
	};

	

	
	public static final ObjectPresenter presenter_PyNone = new ObjectPresenter()
	{
		public Pres presentObject(Object x, GSymFragmentView fragment, AttributeTable inheritedState)
		{
			return GSymPrimitivePresenter.presentNone();
		}
	};

	
	
	public static final ObjectPresenter presenter_PyException = new ObjectPresenter()
	{
		public Pres presentObject(Object x, GSymFragmentView fragment, AttributeTable inheritedState)
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
					new VerticalField( "Message", new GenericPerspectiveInnerFragment( e.value ) ),
					new VerticalField( "Traceback", new VBox( stackTraceElements ) )
			};
			
			return new ErrorBoxWithFields( "PYTHON EXCEPTION", fields );
		}
	};

	
	
	public static final ObjectPresenter presenter_Exception = new ObjectPresenter()
	{
		public Pres presentObject(Object x, GSymFragmentView fragment, AttributeTable inheritedState)
		{
			Exception e = (Exception)x;
			
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
					new VerticalField( "Message", new GenericPerspectiveInnerFragment( e.getMessage() ) ),
					new VerticalField( "Traceback", new VBox( stackTraceElements ) )
			};
			
			return new ErrorBoxWithFields( "JAVA EXCEPTION", fields );
		}
	};


	
	public static final ObjectPresenter presenter_List = new ObjectPresenter()
	{
		public Pres presentObject(Object x, GSymFragmentView fragment, AttributeTable inheritedState)
		{
			List<?> list = (List<?>)x;
			
			ArrayList<Object> itemViews = new ArrayList<Object>();
			for (Object item: list)
			{
				itemViews.add( new GenericPerspectiveInnerFragment( item ) );
			}
			
			return listView( itemViews );
		}
	};
	
	public static final ObjectPresenter presenter_Map = new ObjectPresenter()
	{
		public Pres presentObject(Object x, GSymFragmentView fragment, AttributeTable inheritedState)
		{
			Map<?,?> map = (Map<?,?>)x;
			
			ArrayList<Object> itemViews = new ArrayList<Object>();
			for (Map.Entry<?,?> entry: map.entrySet())
			{
				Pres lineElems[] = { new GenericPerspectiveInnerFragment( entry.getKey() ),
						staticStyle.applyTo( new Whitespace( " ", 10.0 ) ),
						delimStyle.applyTo( new StaticText( ":" ) ),
						staticStyle.applyTo( new Whitespace( " ", 10.0 ) ),
						new GenericPerspectiveInnerFragment( entry.getValue() ) };
				itemViews.add( new Span( lineElems ) );
			}
			
			return mapView( itemViews );
		}
	};
	
	public static final ObjectPresenter presenter_Shape = new ObjectPresenter()
	{
		public Pres presentObject(Object x, GSymFragmentView fragment, AttributeTable inheritedState)
		{
			Shape shape = (Shape)x;
//			Rectangle2D bounds = shape.getBounds2D();
//			double offsetX = -bounds.getMinX(), offsetY = -bounds.getMinY();
//			double width = bounds.getWidth(), height = bounds.getHeight();
//			
//			double scale = 1.0;
//			if ( width > height  &&  width > 96.0 )
//			{
//				scale = 96.0 / width;
//			}
//			else if ( height > width  &&  height > 96.0 )
//			{
//				scale = 96.0 / height;
//			}
			
			return new ObjectBox( x.getClass().getName(), new BritefuryJ.DocPresent.Combinators.Primitive.Shape( shape ) );
		}
	};

	public static final ObjectPresenter presenter_BufferedImage = new ObjectPresenter()
	{
		public Pres presentObject(Object x, GSymFragmentView fragment, AttributeTable inheritedState)
		{
			BufferedImage image = (BufferedImage)x;
			double width = (double)image.getWidth();
			double height = (double)image.getHeight();
			
			if ( width > height  &&  width > 96.0 )
			{
				height *= ( 96.0 / width );
				width = 96.0;
			}
			else if ( height > width  &&  height > 96.0 )
			{
				width *= ( 96.0 / height );
				height = 96.0;
			}
			
			return new BritefuryJ.DocPresent.Combinators.Primitive.Image( image, width, height );
		}
	};

	public static final ObjectPresenter presenter_Color = new ObjectPresenter()
	{
		public Pres presentObject(Object x, GSymFragmentView fragment, AttributeTable inheritedState)
		{
			Color colour = (Color)x;
			
			Pres title = colourObjectBoxStyle.applyTo( new ObjectTitle( "java.awt.Color" ) );
			
			Pres red = colourRedStyle.applyTo( new StaticText( "R=" + String.valueOf( colour.getRed() ) ) );
			Pres green = colourGreenStyle.applyTo( new StaticText( "G=" + String.valueOf( colour.getGreen() ) ) );
			Pres blue = colourBlueStyle.applyTo( new StaticText( "B=" + String.valueOf( colour.getBlue() ) ) );
			Pres alpha = colourAlphaStyle.applyTo( new StaticText( "A=" + String.valueOf( colour.getAlpha() ) ) );
			
			Pres components = colourBoxStyle.applyTo( new HBox( new Pres[] { red, green, blue, alpha } ) );
			
			Pres textBox = new VBox( new Pres[] { title, components } );
			
			Pres swatch = staticStyle.withAttr( Primitive.shapePainter, new FillPainter( colour ) ).applyTo( new Box( 50.0, 20.0 ) ).alignVExpand();
			
			Pres contents = colourBoxStyle.applyTo( new HBox( new Pres[] { textBox, swatch } ) );
			
			return colourObjectBoxStyle.applyTo( new ObjectBorder( contents ) );
		}
	};

	public static final ObjectPresenter presenter_Class = new ObjectPresenter()
	{
		public Pres presentObject(Object x, GSymFragmentView fragment, AttributeTable inheritedState)
		{
			Class<?> cls = (Class<?>)x;
			Class<?> superClass = cls.getSuperclass();
			Class<?> interfaces[] = cls.getInterfaces();
			
			ArrayList<Object> lines = new ArrayList<Object>();
			
			Pres title = classKeywordStyle.applyTo( new StaticText( "Class " ) );
			Pres name = classNameStyle.applyTo( new StaticText( cls.getName() ) );
			lines.add( new HBox( new Pres[] { title, name } ) );
			
			if ( superClass != null  ||  interfaces.length > 0 )
			{
				ArrayList<Object> inheritance = new ArrayList<Object>();
				if ( superClass != null )
				{
					inheritance.add( classKeywordStyle.applyTo( new StaticText( "Extends " ) ) );
					inheritance.add( classNameStyle.applyTo( new StaticText( superClass.getName() ) ) );
				}
				
				if ( interfaces.length > 0 )
				{
					if ( superClass != null )
					{
						inheritance.add( classKeywordStyle.applyTo( new StaticText( " " ) ) );
					}
					inheritance.add( classKeywordStyle.applyTo( new StaticText( "Implements " ) ) );
					boolean bFirst = true;
					for (Class<?> iface: interfaces)
					{
						if ( !bFirst )
						{
							inheritance.add( classPunctuationStyle.applyTo( new StaticText( ", " ) ) );
						}
						inheritance.add( classNameStyle.applyTo( new StaticText( iface.getName() ) ) );
						bFirst = false;
					}
				}
				
				Pres para = new Paragraph( inheritance );
				lines.add( para.padX( 45.0, 0.0 ) );
			}
			
			return new ObjectBoxWithFields( "Java Class", lines );
		}
	};




	private static final StyleSheet2 staticStyle = StyleSheet2.instance.withAttr( Primitive.editable, false );
	
	
	private static final StyleSheet2 punctuationStyle = staticStyle.withAttr( Primitive.foreground, Color.blue );
	private static final StyleSheet2 delimStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.1f, 0.3f, 0.4f ) ).withAttr( Primitive.fontBold, true ).withAttr( Primitive.fontSize, 14 );
	
	
	private static final StyleSheet2 stackTraceStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.75f, 0.1f, 0.4f ) );

	
	private static final StyleSheet2 colourObjectBoxStyle = staticStyle.withAttr( GenericStyle.objectBorderPaint, new Color( 0.0f, 0.1f, 0.4f ) ).withAttr(
			GenericStyle.objectTitlePaint, new Color( 0.0f, 0.1f, 0.4f ) );
	private static final StyleSheet2 colourRedStyle = staticStyle.withAttr( Primitive.fontSize, 12 ).withAttr( Primitive.foreground, new Color( 0.75f, 0.0f, 0.0f ) );
	private static final StyleSheet2 colourGreenStyle = staticStyle.withAttr( Primitive.fontSize, 12 ).withAttr( Primitive.foreground, new Color( 0.0f, 0.75f, 0.0f ) );
	private static final StyleSheet2 colourBlueStyle = staticStyle.withAttr( Primitive.fontSize, 12 ).withAttr( Primitive.foreground, new Color( 0.0f, 0.0f, 0.75f ) );
	private static final StyleSheet2 colourAlphaStyle = staticStyle.withAttr( Primitive.fontSize, 12 ).withAttr( Primitive.foreground, new Color( 0.3f, 0.3f, 0.3f ) );
	private static final StyleSheet2 colourBoxStyle = staticStyle.withAttr( Primitive.hboxSpacing, 5.0 );

	
	private static final StyleSheet2 classKeywordStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.0f, 0.5f ) ).withAttr( Primitive.fontBold, true ).withAttr( Primitive.textSmallCaps, true );
	private static final StyleSheet2 classPunctuationStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.25f, 0.0f, 0.5f ) );
	private static final StyleSheet2 classNameStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.25f, 0.5f ) );

	private static final StyleSheet2 fnPunctuationStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.25f, 0.0f, 0.5f ) );
	private static final StyleSheet2 fnNameStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.25f, 0.5f ) );
	private static final StyleSheet2 fnArgStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.25f ) );
	private static final StyleSheet2 fnKWArgStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.25f ) ).withAttr( Primitive.fontItalic, true );
	private static final StyleSheet2 fnVarArgStyle = staticStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.25f ) );


	private static final Pres comma = punctuationStyle.applyTo( new StaticText( "," ) );
	private static final Pres space = staticStyle.applyTo( new StaticText( " " ) );
	private static final Pres mapSpace = staticStyle.applyTo( new Whitespace( " ", 25.0 ) );
	private static final Pres openBracket = delimStyle.applyTo( new StaticText( "[" ) );
	private static final Pres closeBracket = delimStyle.applyTo( new StaticText( "]" ) );
	private static final Pres openParen = delimStyle.applyTo( new StaticText( "(" ) );
	private static final Pres closeParen = delimStyle.applyTo( new StaticText( ")" ) );
	private static final Pres openBrace = delimStyle.applyTo( new StaticText( "{" ) );
	private static final Pres closeBrace = delimStyle.applyTo( new StaticText( "}" ) );
	

	private static Pres listView(ArrayList<Object> children)
	{
		return new SpanSequenceView( children, openBracket, closeBracket, comma, space, TrailingSeparator.NEVER );
	}
	
	private static Pres tupleView(ArrayList<Object> children)
	{
		return new SpanSequenceView( children, openParen, closeParen, comma, space, TrailingSeparator.ONE_ELEMENT );
	}
	
	private static Pres mapView(ArrayList<Object> children)
	{
		return new SpanSequenceView( children, openBrace, closeBrace, comma, mapSpace, TrailingSeparator.NEVER );
	}
}
