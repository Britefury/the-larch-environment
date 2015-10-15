import sys

def unload_modules(pred_fn):
	"""
	Unload modules whose names pass the predicate `pred_fn`

	:param pred_fn: a function of the form function(string) -> bool that returns True if a module is to be unloaded
	:return: a set containing the names of the modules that were unloaded
	"""
	modules_to_unload = set()
	for name in sys.modules:
		if pred_fn(name):
			modules_to_unload.add(name)

	# Unload the modules
	for name in modules_to_unload:
		del sys.modules[name]

	return modules_to_unload
