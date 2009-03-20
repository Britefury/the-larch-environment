import math

def gaussian(x, a, b, c):
    return a * math.e ** -( (x-b)**2 / c**2*2  )


class TestClass (object):
    def __init__(self, x):
        # Constructor
        self.x = x
        
    def getX(self):
        return self.x
