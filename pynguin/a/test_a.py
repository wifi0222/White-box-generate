# Test cases automatically generated by Pynguin (https://www.pynguin.eu).
# Please check them before you use them.
import pytest
import a as module_0


def test_case_0():
    none_type_0 = None
    with pytest.raises(ValueError):
        module_0.calculate(none_type_0, none_type_0, none_type_0)


def test_case_1():
    bool_0 = False
    str_0 = "multiply"
    var_0 = module_0.calculate(bool_0, bool_0, str_0)
    assert var_0 == 0
    with pytest.raises(ValueError):
        module_0.calculate(str_0, var_0, var_0)


@pytest.mark.xfail(strict=True)
def test_case_2():
    str_0 = "divide"
    tuple_0 = ()
    module_0.calculate(str_0, tuple_0, str_0)


def test_case_3():
    str_0 = "divide"
    bool_0 = False
    with pytest.raises(ValueError):
        module_0.calculate(str_0, bool_0, str_0)


@pytest.mark.xfail(strict=True)
def test_case_4():
    str_0 = "subtract"
    module_0.calculate(str_0, str_0, str_0)


def test_case_5():
    str_0 = "add"
    var_0 = module_0.calculate(str_0, str_0, str_0)
    assert var_0 == "addadd"
    str_1 = "i"
    none_type_0 = None
    with pytest.raises(ValueError):
        module_0.calculate(str_1, str_1, none_type_0)
