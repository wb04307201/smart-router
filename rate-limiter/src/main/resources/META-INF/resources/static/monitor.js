let refreshIntervalId;
let myChart;

document.addEventListener('DOMContentLoaded', function () {
    // 首次加载数据
    loadData();

    // 刷新按钮事件
    document.getElementById('refreshBtn').addEventListener('click', loadData);

    // 自动刷新切换
    document.getElementById('autoRefresh').addEventListener('change', function () {
        if (this.checked) {
            const interval = parseInt(document.getElementById('refreshInterval').value);
            refreshIntervalId = setInterval(loadData, interval);
        } else {
            clearInterval(refreshIntervalId);
        }
    });

    // 刷新间隔改变事件
    document.getElementById('refreshInterval').addEventListener('change', function () {
        if (document.getElementById('autoRefresh').checked) {
            clearInterval(refreshIntervalId);
            const interval = parseInt(this.value);
            refreshIntervalId = setInterval(loadData, interval);
        }
    });

    // 关闭弹出框（点击 × 按钮）
    document.getElementById("close-modal").addEventListener("click", () => {
        const modal = document.getElementById("modal");
        modal.style.display = "none";
    });

    // 关闭弹出框（点击 × 按钮）
    document.getElementById("close-modal1").addEventListener("click", () => {
        const modal = document.getElementById("modal1");
        modal.style.display = "none";
    });

    // 浏览器窗口的宽度（含滚动条）
    const windowWidth = window.innerWidth;

    // 浏览器窗口的高度（含滚动条）
    const windowHeight = window.innerHeight;

    myChart = echarts.init(document.getElementById('main'), null, {
        width: windowWidth - 120,
        height: windowHeight - 120
    });
});

function loadData() {
    fetch('/rate/limiter/monitor/static')
        .then(response => response.json())
        .then(data => {
            updateSummary(data);
            updateTable(data);
        })
        .catch(error => {
            console.error('加载数据失败:', error);
            alert('加载数据失败，请稍后重试');
        });
}

function updateSummary(data) {
    let totalCount = 0;
    let allowedCount = 0;

    data.forEach(item => {
        totalCount += item.total;
        allowedCount += item.allowed;
    });

    const avgAllowedRate = totalCount > 0 ? ((allowedCount / totalCount) * 100).toFixed(2) : 0;

    document.getElementById('totalCount').textContent = totalCount.toLocaleString();
    document.getElementById('allowedCount').textContent = allowedCount.toLocaleString();
    document.getElementById('avgAllowedRate').textContent = avgAllowedRate + '%';
}

function updateTable(data) {
    const methodtrace = document.getElementById('rateLimter');

    if (data.length === 0) {
        methodtrace.innerHTML = `
                    <div class="empty-state">
                        <p>暂无数据</p>
                    </div>
                `;
        return;
    }

    let tableHTML = `
            <div class="table-wrapper">
                <table>
                    <thead>
                    <tr>
                            <th>地址</th>
                            <th>状态</th>
                            <th>请求</th>
                            <th>通过</th>
                            <th>通拓率</th>
                            <th>QPS</th>
                            <th>详细</th>
                    </tr>
                </thead>
            <tbody>
            `;

    data.forEach(item => {
        tableHTML += `
                    <tr>
                        <td>${item.endpoint}</td>
                        <td>${item.isRateLimiter ? "被限流" : "无限制"}</td>
                        <td>${item.total.toLocaleString()}</td>
                        <td>${item.allowed.toLocaleString()}</td>
                        <td>${item.allowedRate.toFixed(2)}%</td>
                        <td>${item.qps.toLocaleString()}</td>
                        <td><a href="javascript:void(0);" onclick="openModal('${item.endpoint}')">查看</a></td>
                    </tr>
                `;
    });

    tableHTML += `
            </tbody>
        </table>
    </div>
            `;

    methodtrace.innerHTML = tableHTML;
}

function openModal(endpoint) {
    fetch('/rate/limiter/monitor/staticByEndpoint?endpoint=' + endpoint)
        .then(response => response.json())
        .then(data => {
            // 指定图表的配置项和数据
            var option = {
                title: {
                    text: '每小时请求情况'
                },
                xAxis: {
                    data: data.map(item => item.hour)
                },
                yAxis: {},
                series: [
                    {
                        name: '请求',
                        type: 'line',
                        data: data.map(item => item.total)
                    },
                    {
                        name: '通过',
                        type: 'line',
                        data: data.map(item => item.allowed)
                    }
                ]
            };

            // 使用刚指定的配置项和数据显示图表。
            myChart.setOption(option);


            const modal = document.getElementById("modal");
            modal.style.display = 'block';
        })
        .catch(error => {
            console.error('加载数据失败:', error);
        })
}

function updateRules() {
    fetch('/rate/limiter/monitor/rules')
        .then(response => response.json())
        .then(data => {
            const rules = document.getElementById('rules');
            rules.innerHTML = JSON.stringify(data,null,2)


            const modal = document.getElementById("modal1");
            modal.style.display = 'block';
        })
}

// 格式化 JSON（自动缩进）
function formatJSON() {
    const textarea = document.getElementById('rules');
    try {
        const obj = JSON.parse(textarea.value);
        textarea.value = JSON.stringify(obj,null,2); // 缩进 2 空格
    } catch (e) {
        showToast('❌ JSON 格式错误: ' + e.message);
    }
}

// 验证 JSON 合法性
function validateJSON() {
    const textarea = document.getElementById('rules');
    try {
        const obj = JSON.parse(textarea.value);
        console.log("保存的对象:", obj);

        // 发送 POST 请求
        fetch('/rate/limiter/monitor/rules', {
            method: 'POST', // 指定请求方法
            headers: {
                'Content-Type': 'application/json', // 设置内容类型
            },
            body: JSON.stringify(obj) // 将数据转换为JSON字符串
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('网络响应不正常');
                }
                return response.json(); // 解析JSON响应
            })
            .then(data => {
                const modal = document.getElementById("modal1");
                modal.style.display = "none";
                showToast('✅ 限流规则更新成功！');
            })
            .catch(error => {
                console.error('错误:', error); // 处理错误
            });

    } catch (e) {
        showToast('❌ JSON 格式错误: ' + e.message);
    }
}

function showToast(message) {
    var toast = document.getElementById("toast");
    toast.innerHTML = message;
    toast.className = "show";

    // 3秒后自动关闭
    setTimeout(function(){
        toast.className = toast.className.replace("show", "");
    }, 3000);
}