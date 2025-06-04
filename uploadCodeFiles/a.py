# my_module.py
def calculate(a, b, operation):
    """
    根据操作符执行计算：
    - 'add': a + b
    - 'subtract': a - b
    - 'multiply': a * b
    - 'divide': a / b，如果 b 为 0，则抛出 ValueError
    """
    if operation == 'add':
        return a + b
    elif operation == 'subtract':
        return a - b
    elif operation == 'multiply':
        return a * b
    elif operation == 'divide':
        if b == 0:
            raise ValueError("除数不能为零")
        return a / b
    else:
        raise ValueError("无效的操作符")