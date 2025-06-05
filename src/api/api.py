import json
import re
import sys
import os
from zhipuai import ZhipuAI


# 自定义 JSON 编码器
class CustomEncoder(json.JSONEncoder):
    def default(self, obj):
        if hasattr(obj, '__dict__'):
            return obj.__dict__
        return super().default(obj)


# 定义基础输出路径
base_output_dir = r"C:\Users\Wangfei\Desktop\new\studentinfo\llm\java"

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("用法: python script.py <输入文件路径>")
        sys.exit(1)

    input_file_path = sys.argv[1]
    try:
        with open(input_file_path, 'r') as file:
            code_content = file.read()
    except FileNotFoundError:
        print("文件未找到，请检查文件路径。")
    else:
        # 提取package语句
        package_match = re.search(r'package\s+([\w\.]+);', code_content)
        package_name = None
        package_path = ""
        if package_match:
            package_name = package_match.group(1)
            package_path = os.path.join(*package_name.split('.'))

        # 构建完整输出目录
        output_dir = os.path.join(base_output_dir, package_path)

        client = ZhipuAI(api_key="1ab053b0d4194225b25b2139ba6fcd68.98mhImT6Nke9ofce")
        response = client.chat.completions.create(
            model="glm-4-plus",
            messages=[
                {
                    "role": "user",
                    "content": f"请为以下 Java 代码生成单元测试用例，要符合 JUnit 5 的语法规范。不要说多余的废话，直接生成测试用例代码。只允许使用这两个包：import org.junit.Test;import static org.junit.Assert.*;请牢记不允许用其他的import。代码如下：{code_content} 务必牢记：只允许使用这两个包：import org.junit.Test;import static org.junit.Assert.*;请牢记不允许用其他的import。"
                }
            ]
        )

        # 假设 response 可以通过 __dict__ 方法获取其属性并转化为字典
        response_dict = response.__dict__
        try:
            # 使用自定义编码器将字典转换为 JSON 字符串
            response_str = json.dumps(response_dict, cls=CustomEncoder)

            json_start = response_str.find('{')
            json_end = response_str.rfind('}')
            if json_start != -1 and json_end != -1:
                json_str = response_str[json_start:json_end + 1]
                response_json = json.loads(json_str)
                content = response_json.get('choices', [{}])[0].get('message', {}).get('content', '')

                # 正则表达式提取 Java 代码
                pattern = r'```java([\s\S]*?)```'
                matches = re.findall(pattern, content)
                if matches:
                    java_code = matches[0].strip()
                    # 获取输入文件名（不带扩展名）
                    input_file_name = os.path.splitext(os.path.basename(input_file_path))[0]
                    # 生成输出文件名
                    output_file_name = f"{input_file_name}_test.java"
                    # 组合输出文件的完整路径
                    output_file_path = os.path.join(output_dir, output_file_name)

                    # 修改 public class 名
                    class_pattern = r'public class (\w+)'
                    new_class_name = f"{input_file_name}_test"
                    java_code = re.sub(class_pattern, f'public class {new_class_name}', java_code)

                    # 添加package语句到测试用例文件顶部
                    if package_name:
                        package_statement = f"package {package_name};\n\n"
                        # 检查是否已有package语句（避免重复添加）
                        if not java_code.startswith("package "):
                            # 查找import语句位置，在其之前插入package
                            import_index = java_code.find("import ")
                            if import_index == -1:
                                # 没有import语句，直接添加到顶部
                                java_code = package_statement + java_code
                            else:
                                # 在import之前插入package
                                java_code = java_code[:import_index] + package_statement + java_code[import_index:]

                    # 创建输出目录（如果不存在）
                    if not os.path.exists(output_dir):
                        os.makedirs(output_dir)

                    with open(output_file_path, 'w') as file:
                        file.write(java_code)
                    print(f"测试用例已保存至: {output_file_path}")
                else:
                    print("未找到 Java 代码。")
            else:
                print("未找到有效的 JSON 内容。")
        except json.JSONDecodeError:
            print("解析 JSON 时出错。")
        except AttributeError:
            print("对象不支持属性访问，可能需要调整处理方式。")