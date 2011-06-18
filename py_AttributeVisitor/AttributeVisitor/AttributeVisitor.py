from BritefuryJ.AttributeVisitor import AttributeBase, Attribute, SynthesizedAttribute, InheritedAttribute, IncrementalAttributeVisitorEvaluator, NoAttributeEvaluationFunctionException


from Britefury.Dispatch.DMObjectNodeMethodDispatch import DMObjectNodeDispatchMethodWrapper





class _BoundAttribute (object):
	def __init__(self, attribute, visitorInstance):
		self._attribute = attribute
		self._visitorInstance = visitorInstance
		
	def __call__(self, node):
		return self._visitorInstance._evaluator.getAttributeValue( self._attribute, node )




class AttributeEvalFnMethodWrapper (DMObjectNodeDispatchMethodWrapper):
	def __init__(self, attribute, nodeClass, function):
		super( AttributeEvalFnMethodWrapper, self ).__init__( nodeClass, function )
		self._attribute = attribute
		
		
	def bind(self, visitorInstance):
		def fn(node):
			return self.callNoArgs( node, visitorInstance )
		return fn
	
		
		

def AttributeEvaluationMethod(attribute, nodeClass):
	def decorator(fn):
		return AttributeEvalFnMethodWrapper( attribute, nodeClass, fn )
	return decorator
	
		
class AttributeVisitorMetaClass (type):
	def __init__(cls, name, bases, clsDict):
		super( AttributeVisitorMetaClass, cls ).__init__( name, bases, clsDict )
		
		# Initialise attribute table with entries from base classes
		cls.__attribute_table__ = {}
		for base in bases:
			try:
				cls.__attribute_table__.update( base.__attribute_table__ )
			except AttributeError:
				pass
		clsDict['__attribute_table__'] = cls.__attribute_table__

		
		# Add entries to the attribute table
		for k, v in clsDict.items():
			if isinstance( v, AttributeBase ):
				cls.__attribute_table__[k] = v

		
				
		
		# Gather a table of attribute evaluation methods from base classes
		cls.__attrib_methods__ = {}
		for base in bases:
			try:
				for attribute, baseMethodTable in base.__attrib_methods__.items():
					methodTable = cls.__attrib_methods__.setdefault( attribute, {} )
					methodTable.update( baseMethodTable )
			except AttributeError:
				pass
		clsDict['__attrib_methods__'] = cls.__attrib_methods__

		
		# Add entries the attribute method table
		for k, v in clsDict.items():
			if isinstance( v, AttributeEvalFnMethodWrapper ):
				method = v
				method._init( 0 )
				
				attribute = method._attribute
				nodeClass = method._nodeClass
				
				methodTable = cls.__attrib_methods__.setdefault( attribute, {} )
				methodTable[nodeClass] = method

		

		
class IncrementalAttributeVisitor (object):
	__metaclass__ = AttributeVisitorMetaClass
	
	def __init__(self):
		# Build a list of attributes, and create bound attributes
		attributes = []
		for name, attribute in self.__attribute_table__.items():
			attributes.append( attribute )
			setattr( self, name, _BoundAttribute( attribute, self ) )
		
		evalFnSpecs = []
		for attribute, nodeClassToEvalFnTable in self.__attrib_methods__.items():
			for nodeClass, evalFn in nodeClassToEvalFnTable.items():
				spec = IncrementalAttributeVisitorEvaluator.EvalFnSpec( attribute, nodeClass, evalFn.bind( self ) )
				evalFnSpecs.append( spec )
		self._evaluator = IncrementalAttributeVisitorEvaluator( attributes, evalFnSpecs )
		
