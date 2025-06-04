import os
import re
import sys
from zhipuai import ZhipuAI

client = ZhipuAI(api_key="1ab053b0d4194225b25b2139ba6fcd68.98mhImT6Nke9ofce")

# 定义输出保存路径
output_dir = r"C:\Users\Wangfei\Desktop\new\studentinfo\llm\python"

if __name__ == "__main__":
    if len(sys.argv) != 2:
        sys.exit(1)

    file_path = sys.argv[1]

    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            code_content = file.read()

        response = client.chat.completions.create(
            model="glm-4-plus",
            messages=[
                {"role": "user",
                 "content": f"请为以下 Python 代码生成单元测试用例，要符合 import pytest 的语法规范。开头只允许 import pytest 和 from my_module import xxx 不要说多余的废话，直接生成测试用例代码。代码如下：{code_content}"},
            ],
        )
        print(response)
    except FileNotFoundError:
        print(f"错误：未找到文件 {file_path}。")
    except Exception as e:
        print(f"发生未知错误：{e}")
    else:
        pattern = r'```python(.*?)```'
        match = re.search(pattern, response.choices[0].message.content, re.DOTALL)
        if match:
            python_code = match.group(1).strip()
            # 获取文件名不带后缀
            file_name_without_ext = os.path.splitext(os.path.basename(file_path))[0]
            # 替换 from my_module
            python_code = python_code.replace("from my_module", f"from {file_name_without_ext}")

            # 生成输出文件名
            output_file_name = f"{file_name_without_ext}_test.py"
            # 组合输出文件的完整路径
            output_file_path = os.path.join(output_dir, output_file_name)

            # 创建输出目录（如果不存在）
            if not os.path.exists(output_dir):
                os.makedirs(output_dir)

            try:
                with open(output_file_path, 'w', encoding='utf-8') as file:
                    file.write(python_code)
                print(f"代码已成功保存到 {output_file_path}")
            except Exception as e:
                print(f"保存文件时出错: {e}")
        else:
            print("未找到 Python 代码部分。")
