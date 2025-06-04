function generateEvoSuite() {
    fetch('/test/generate-evo-suite?file=your_file_path')
        .then(response => response.json())
        .then(data => {
            document.getElementById('testOutput').textContent = data.output;
        });
}

function generateLMM() {
    fetch('/test/generate-lmm?file=your_file_path')
        .then(response => response.json())
        .then(data => {
            document.getElementById('testOutput').textContent = data.output;
        });
}
document.getElementById('uploadForm').addEventListener('submit', async function (e) {
    e.preventDefault(); // 阻止表单的默认提交行为

    const form = e.target;
    const formData = new FormData(form);

    try {
        const response = await fetch('/test/upload', {
            method: 'POST',
            body: formData
        });

        const result = await response.json();

        const messageDiv = document.getElementById('uploadMessage');
        if (response.ok) {
            messageDiv.textContent = result.message;
            messageDiv.style.color = 'green';
        } else {
            messageDiv.textContent = result.message;
            messageDiv.style.color = 'red';
        }
    } catch (error) {
        console.error('上传失败:', error);
        const messageDiv = document.getElementById('uploadMessage');
        messageDiv.textContent = '上传失败：网络错误';
        messageDiv.style.color = 'red';
    }
});
