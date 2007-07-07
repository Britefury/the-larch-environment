from Britefury.PyImport import PythonImporter


import sys
filename = sys.argv[1]

source = open( filename, 'r' ).read()
PythonImporter.importPythonSource( source )
