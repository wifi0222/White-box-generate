def quadratic_solver(a, b, c):
    """测试二次方程求解和浮点运算"""
    discriminant = b**2 - 4*a*c
    if discriminant < 0:
        return None
    x1 = (-b + discriminant**0.5) / (2*a)
    x2 = (-b - discriminant**0.5) / (2*a)
    return round(x1, 2), round(x2, 2)

