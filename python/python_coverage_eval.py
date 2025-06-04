import coverage
import importlib.util
import json

def run_python_test(file_path, test_function_name):
    cov = coverage.Coverage()
    cov.start()

    spec = importlib.util.spec_from_file_location("module", file_path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    getattr(module, test_function_name)()

    cov.stop()
    cov.save()
    cov.json_report(outfile="coverage.json")

    with open("coverage.json") as f:
        data = json.load(f)
        return data['files'][file_path]['summary']['percent_covered']