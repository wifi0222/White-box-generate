def reverse_and_upper(input_str):
    """测试字符串反转和大写转换"""
    if not isinstance(input_str, str):
        raise TypeError("Input must be a string")
    return input_str[::-1].upper()
