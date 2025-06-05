def log_execution(func):
    """测试装饰器功能"""
    def wrapper(*args, **kwargs):
        print(f"Executing {func.__name__}")
        return func(*args, **kwargs)
    return wrapper

@log_execution
def add_numbers(a, b):
    return a + b

