import pytest
from a import calculate

def test_calculate_add():
    assert calculate(2, 3, 'add') == 5
    assert calculate(-1, 1, 'add') == 0
    assert calculate(0, 0, 'add') == 0

def test_calculate_subtract():
    assert calculate(5, 3, 'subtract') == 2
    assert calculate(-1, 1, 'subtract') == -2
    assert calculate(0, 0, 'subtract') == 0

def test_calculate_multiply():
    assert calculate(2, 3, 'multiply') == 6
    assert calculate(-1, 1, 'multiply') == -1
    assert calculate(0, 5, 'multiply') == 0

def test_calculate_divide():
    assert calculate(6, 3, 'divide') == 2
    assert calculate(-4, 2, 'divide') == -2
    assert calculate(0, 1, 'divide') == 0

def test_calculate_divide_by_zero():
    with pytest.raises(ValueError) as e:
        calculate(5, 0, 'divide')
    assert str(e.value) == "除数不能为零"

def test_calculate_invalid_operation():
    with pytest.raises(ValueError) as e:
        calculate(5, 3, 'modulus')
    assert str(e.value) == "无效的操作符"