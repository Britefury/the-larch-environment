##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2014.
##-*************************
from BritefuryJ.Util.RichString import RichStringBuilder, RichString

from BritefuryJ.Incremental import IncrementalValueMonitor

from BritefuryJ.Pres.Primitive import Column


from . import execution_pres



class RichStream (object):
	"""
	A named stream that contains textual and rich non-textual content in a sequence.
	"""
	def __init__(self, name):
		self.name = name
		self.__builder = None

	def write(self, text):
		if not ( isinstance( text, str )  or  isinstance( text, unicode ) ):
			raise TypeError, 'argument 1 must be string, not %s' % type( text )
		self._builder.appendTextValue( text )

	def display(self, value):
		self._builder.appendStructuralValue( value )


	@property
	def _builder(self):
		if self.__builder is None:
			self.__builder = RichStringBuilder()
		return self.__builder


	@property
	def rich_string(self):
		return self.__builder.richString()   if self.__builder is not None   else RichString()

	@property
	def text(self):
		return self.__builder.richString().textualValue()   if self.__builder is not None   else ''



class MultiplexedRichStream (object):
	"""
	Maintains a set of named rich streams. They can be written to in any order, with separation maintained between
	streams of different names.

	E.g.
	```
	mxs = MultiplexedRichStream(['stdout', 'stderr'])

	mxs.stdout.write('Hello ')
	mxs.stdout.write('world')
	mxs.stderr.write('An error')
	mxs.stdout.write('Goodbye')

	for x in mxs:
		print x.text
	```

	will result in:
	Hello world			# From stdout
	An error		# From stderr
	Goodbye		# From stdout
	"""
	class _SingleStream (object):
		"""
		A single stream that supports write and display methods.
		These are used as stdout, stderr, etc and receive content from code that generates
		output. They pass it on to the enclosing MultiplexedRichStream that retains sequence.
		"""
		def __init__(self, multiplexedStream, name):
			self.name = name
			self.__multi = multiplexedStream
			self.__stream = RichStream( name )

		def write(self, text):
			self.__stream.write( text )
			self.__multi._write( self.name, text )

		def display(self, value):
			self.__stream.display( value )
			self.__multi._display( self.name, value )

		@property
		def rich_string(self):
			return self.__stream.rich_string


	def __init__(self, streamNames=None):
		if streamNames is None:
			streamNames = ['stdout', 'stderr']
		self.__streams_by_name = { name : self._SingleStream( self, name )   for name in streamNames }
		self.__multiplexed = []
		self.__incr = IncrementalValueMonitor()


	def __getattr__(self, item):
		try:
			return self.__streams_by_name[item]
		except KeyError:
			raise AttributeError, 'No stream named {0}'.format( item )


	def __iter__(self):
		return iter( self.__multiplexed )

	def __getitem__(self, item):
		return self.__multiplexed[item]

	def __len__(self):
		return len( self.__multiplexed )


	def suppress_stream(self, name):
		result = MultiplexedRichStream([])
		for n, stream in self.__streams_by_name.items():
			if n != name:
				result.__streams_by_name[n] = stream
		for s in self.__multiplexed:
			if s.name != name:
				result.__multiplexed.append( s )
		return result


	def has_content_for(self, name):
		# Cannot use __streams_by_name; it
		for s in self.__multiplexed:
			if s.name == name:
				return True
		return False


	def _write(self, streamName, text):
		stream = self.__multiplexed_for_name(streamName)
		stream.write( text )
		self.__incr.onChanged()


	def _display(self, streamName, value):
		stream = self.__multiplexed_for_name(streamName)
		stream.display( value )
		self.__incr.onChanged()


	def __multiplexed_for_name(self, name):
		if len( self.__multiplexed ) > 0:
			top = self.__multiplexed[-1]
			if top.name == name:
				return top
		stream = RichStream( name )
		self.__multiplexed.append( stream )
		return stream


	def __present__(self, fragment, inherited_state):
		self.__incr.onAccess()
		column_contents = []
		for stream in self:
			column_contents.append(execution_pres.stream_pres(stream.rich_string, stream.name))
		return Column(column_contents)




class AbstractExecutionResult (object):
	def __init__(self, streams=None):
		if streams is None:
			streams = MultiplexedRichStream(['stdout', 'stderr'])
		self._streams = streams
		self.finished_callback = None


	@property
	def streams(self):
		return self._streams


	@property
	def caught_exception(self):
		raise NotImplementedError, 'abstract'


	def has_result(self):
		raise NotImplementedError, 'abstract'

	@property
	def result(self):
		raise NotImplementedError, 'abstract'


	def was_aborted(self):
		raise NotImplementedError, 'abstract'


	def errorsOnly(self):
		raise NotImplementedError, 'abstract'



	def hasErrors(self):
		raise NotImplementedError, 'abstract'


	def view(self, bUseDefaultPerspecitveForException=True, bUseDefaultPerspectiveForResult=True):
		raise NotImplementedError, 'abstract'


	def minimalView(self, bUseDefaultPerspecitveForException=True, bUseDefaultPerspectiveForResult=True):
		raise NotImplementedError, 'abstract'


