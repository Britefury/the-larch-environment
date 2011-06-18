//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ClassGen;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import javax.tools.JavaFileObject.Kind;

public class ClassCompiler
{
	public static class CompilationError extends Exception
	{
		private static final long serialVersionUID = 1L;

		List<Diagnostic<? extends JavaFileObject>> diags;
		
		public CompilationError(List<Diagnostic<? extends JavaFileObject>> diags)
		{
			this.diags = diags;
		}
	}
	
	private static class StringJFO extends SimpleJavaFileObject
	{
		private String codeString;
		
		public StringJFO(String className, String codeString) throws URISyntaxException
		{
			super( new URI( className ), JavaFileObject.Kind.SOURCE );
			this.codeString = codeString;
		}
		
		
		public String getCharContent(boolean bErrors)
		{
			return codeString;
		}
	}
	
	private static class ByteArrayJFO extends SimpleJavaFileObject
	{
		private ByteArrayOutputStream outStream;
		
		public ByteArrayJFO(String className, Kind kind) throws URISyntaxException
		{
			super( new URI( className ), kind );
			
			outStream = new ByteArrayOutputStream();
		}
		
		
		public InputStream openInputStream()
		{
			return new ByteArrayInputStream( getByteArray() );
		}
		
		public OutputStream openOutputStream()
		{
			return outStream;
		}
		
		private byte[] getByteArray()
		{
			return outStream.toByteArray();
		}
		
	}
	
	private static class ByteJavaFileManager extends ForwardingJavaFileManager<JavaFileManager>
	{
		private ByteArrayJFO code;
		
		protected ByteJavaFileManager(JavaFileManager fileManager)
		{
			super( fileManager );
		}
		
		public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling)
		{
			try
			{
				code = new ByteArrayJFO( className, kind );
			}
			catch (URISyntaxException e)
			{
				throw new RuntimeException( "Invalid class name" );
			}
			return code;
		}
	}
	
	private static class ByteClassLoader extends URLClassLoader
	{
		private ByteArrayJFO codeJFO;
		
		public ByteClassLoader(ByteArrayJFO codeJFO)
		{
			super( new URL[] {} );
			this.codeJFO = codeJFO;
		}
		
		public Class<?> findClass(String className)
		{
			byte code[] = codeJFO.getByteArray();
			Class<?> cl = defineClass( className, code, 0, code.length );
			if ( cl == null )
			{
				throw new RuntimeException( "Could not find class " + className );
			}
			else
			{
				return cl;
			}
		}
	}

	
	private JavaCompiler compiler;
	
	
	
	public ClassCompiler()
	{
		compiler = ToolProvider.getSystemJavaCompiler();
	}
	
	public ByteArrayJFO compileClass(String className, String codeString, List<String> flags) throws URISyntaxException, CompilationError
	{
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		ByteJavaFileManager jfm = new ByteJavaFileManager( compiler.getStandardFileManager( diagnostics, null, null ) );
		ArrayList<String> fl = new ArrayList<String>();
		fl.addAll( flags );
		fl.add( "-cp" ); 
		fl.add( System.getProperty( "java.class.path" ) );
		JavaCompiler.CompilationTask task = compiler.getTask( null, jfm, diagnostics, fl, null, Arrays.asList( new JavaFileObject[] { new StringJFO( className, codeString ) } ) );
		if ( !task.call() )
		{
			throw new CompilationError( diagnostics.getDiagnostics() );
		}
		return jfm.code;
	}
	
	public ByteArrayJFO compileClass(String className, String codeString) throws URISyntaxException, CompilationError
	{
		return compileClass( className, codeString, new ArrayList<String>() );
	}
	

	public Class<?> createClass(String className, String codeString, List<String> flags) throws ClassNotFoundException, URISyntaxException, CompilationError
	{
		ByteArrayJFO compiled = compileClass( className, codeString, flags );
		ByteClassLoader loader = new ByteClassLoader( compiled );
		return loader.loadClass( className );
	}
	
	public Class<?> createClass(String className, String codeString) throws ClassNotFoundException, URISyntaxException, CompilationError
	{
		return createClass( className, codeString, new ArrayList<String>() );
	}
}
