grammar DMReader;


@lexer::header
{
package BritefuryJ.DocModel.Reader;
}

@header
{
package BritefuryJ.DocModel.Reader;

import java.util.HashMap;

import BritefuryJ.DocModel.DMModuleResolver;
import BritefuryJ.DocModel.DMModule;
import BritefuryJ.DocModel.DMObjectClass;
}


@members
{
	public static class CouldNotGetModuleException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}

	public static class CouldNotGetClassException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}

	public static class InvalidFieldException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}

	protected String unescape(String s)
	{
		s = s.replace( "\\n", "\n" ).replace( "\\r", "\r" ).replace( "\\t", "\t" ).replace( "\\\\", "\\" ).replace( "\\\"", "\"" );
		
		boolean bScanAgain = true;
		while ( bScanAgain )
		{
			bScanAgain = false;
			int start = s.indexOf( "\\x" );
			if ( start != -1 )
			{
				int end = s.substring( start+2 ).indexOf( "x" );
				if ( end != -1 )
				{
					end += start+2;
					String hexString = s.substring( start+2, end );
					char c = (char)Integer.valueOf( hexString, 16 ).intValue();
					s = s.substring( 0, start ) + new Character( c ).toString() + s.substring( end+1, s.length() );
					bScanAgain = true;
				}
			}
		}
		
		return s;
	}
	
	protected Object createObject(String moduleName, String className, ArrayList<String> names, ArrayList<Object> values)
	{
		DMModule module = bindings.get( moduleName );
		
		if ( module == null )
		{
			throw new CouldNotGetModuleException();
		}
		
		DMObjectClass cls;
		try
		{
			cls = module.get( className );
		}
		catch (DMModule.UnknownClassException e)
		{
			throw new CouldNotGetClassException();
		}
		
		try
		{
			return cls.newInstance( (String[])names.toArray(), values.toArray() );
		}
		catch (DMObjectClass.InvalidFieldNameException e)
		{
			throw new InvalidFieldException();
		}
	}
	
	protected void bind(String name, String moduleLocation)
	{
		DMModule module = moduleResolver.getModule( moduleLocation );
		bindings.put( name, module );
	}
	
	
	
	protected HashMap<String, DMModule> bindings = new HashMap<String, DMModule>();
	public DMModuleResolver moduleResolver = null;
}






prog returns [Object value]
	:	x=document { $value = $x.value; };

document returns [Object value]
	:	x=item		{ $value = $x.value; }  |
		x=bindings	{ $value = $x.value; };
	
item returns [Object value]
	:	x=atom  	{ $value = $x.value; }  |
		x=list  	{ $value = $x.value; }  |
		x=object	{ $value = $x.value; };

list returns [Object value]
	:	{ ArrayList<Object> xs = new ArrayList<Object>(); }
		'['
			WS?
			( i=item { xs.add( $i.value ); } WS? )*
		']'
		{ $value = xs; };
			
object returns [Object value]
	:
		'('
			{
				ArrayList<String> keys = new ArrayList<String>();
				ArrayList<Object> values = new ArrayList<Object>();
			}		
			WS?
			moduleName=IDENTIFIER
			WS
			className=IDENTIFIER
			WS?
			(
				k=IDENTIFIER WS? '=' WS? v=item WS?
				{
					keys.add( $k.getText() );
					values.add( $v.value );
				}
			)*
			{ $value = createObject( $moduleName.getText(), $className.getText(), keys, values ); }
		')';
			
bindings returns [Object value]
	:
		'{'
			WS?
			(
				name=IDENTIFIER WS? '=' WS? moduleLocation=atom WS
				{
					bind( $name.getText(), $moduleLocation.value );
				}
			)*
			x=item
			WS?
			{ $value = $x.value; }
		'}';
			
atom returns [String value]
	:	s=UNQUOTEDSTRING { $value = s.getText(); }  |
		s=QUOTEDSTRING { $value = unescape(s.getText()); };




fragment UNQUOTEDSTRING_PUNC
	:	( '+' | '-' | '*' | '/' | '%' | '^' | '&' | '|' | '!' | '~' | '$' | '@' | '.' );

fragment QUOTEDSTRING_PUNC
	:	( '+' | '-' | '*' | '/' | '%' | '^' | '&' | '|' | '!' | '~' | '$' | '@' | '.' | '\'' | '(' | ')' | '[' | ']' | '{' | '}' );


fragment STRING_ESCAPE
	:	'\\' ( 'n'  |  't'  |  'r'  |  '"'  |  ('x' ( '0'..'9' | 'A'..'F' | 'a'..'f' )+ 'x' ) );
	
UNQUOTEDSTRING
	:	( 'A'..'Z' | 'a'..'z' | '0'..'9' | '_' | UNQUOTEDSTRING_PUNC )+;

QUOTEDSTRING
	:	(
			'\"'
			( 'A'..'Z' | 'a'..'z' | '0'..'9' | '_' | QUOTEDSTRING_PUNC | STRING_ESCAPE )*
			'\"'
		) { setText( getText().substring( 1, getText().length() - 1 ) ); };
		
WS
	:	('\n' | '\t' | '\r' | ' ')+ {skip();}
	;
	
IDENTIFIER
	:	( 'A'..'Z' | 'a'..'z' | '_' ) ( 'A'..'Z' | 'a'..'z' | '0'..'9' | '_' )*
	;


