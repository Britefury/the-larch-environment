import math

def solveCubic(a, b, c, d):
    q=(3.0*a*c - b**2) / (9.0*a**2)
    r = (9.0*a*b*c - 27.0*a**2*d - 2.0*b**3)  /  ( 54.0*a**3 )
    k=q**3+r**2

    if t >= 0.0:
        s = (r + math.sqrt( q**3+r**2 )) ** (1.0/3.0)
        t = (r - math.sqrt( q**3+r**2 )) ** (1.0/3.0)
    else:
        rho = math.sqrt( r**2 - k )
        theta = math.acos( r/rho )
        
        s = rho**(1.0/3.0) + (theta/3.0) * 1j
        t = rho**(1.0/3.0) - (theta/3.0) * 1j
        
    x1 = s + t - b / (3.0*a)
    x2 = -(1.0/2.0) * (s+t) - b/(3.0*a) + (math.sqrt(3)/2)*(s-t)*1j
    x3 = -(1.0/2.0) * (s+t) - b/(3.0*a) - (math.sqrt(3)/2)*(s-t)*1j
    
    return x1, x2, x3


