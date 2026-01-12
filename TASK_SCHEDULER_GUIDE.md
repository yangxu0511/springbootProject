# 彩票号码定时推送 - Windows 任务计划程序设置指南

## 方式一：使用任务计划程序（推荐）

### 步骤 1：打开任务计划程序
1. 按 `Win + R`，输入 `taskschd.msc`，回车

### 步骤 2：创建任务
1. 右侧点击 **创建任务**（不是基本任务）

### 步骤 3：常规设置
- 名称：`彩票号码每日推送`
- 描述：`每日定时生成彩票号码并推送到微信`
- 勾选：**不管用户是否登录都要运行**
- 勾选：**使用最高权限运行**

### 步骤 4：触发器设置
1. 点击 **新建**
2. 设置：
   - 开始任务：按预定计划
   - 设置：每天
   - 开始时间：**18:00:00**（建议在开奖前1-2小时）
3. 点击确定

### 步骤 5：操作设置
1. 点击 **新建**
2. 操作：启动程序
3. 程序或脚本：
   ```
   D:\idea-workspace\springbootProject\run_daily_lottery.bat
   ```
4. 起始于：
   ```
   D:\idea-workspace\springbootProject
   ```
5. 点击确定

### 步骤 6：条件设置
- 取消勾选：只有在计算机使用交流电源时才启动此任务

### 步骤 7：设置
- 勾选：如果任务失败，按以下频率重新启动
  - 重启间隔：1分钟
  - 最多重启次数：3次

---

## 方式二：使用 PowerShell 快速创建

以管理员身份运行 PowerShell，执行以下命令：

```powershell
$action = New-ScheduledTaskAction -Execute "D:\idea-workspace\springbootProject\run_daily_lottery.bat" -WorkingDirectory "D:\idea-workspace\springbootProject"
$trigger = New-ScheduledTaskTrigger -Daily -At "18:00"
$settings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries
Register-ScheduledTask -TaskName "彩票号码每日推送" -Action $action -Trigger $trigger -Settings $settings -Description "每日定时生成彩票号码并推送到微信"
```

---

## 开奖时间参考

| 彩票 | 开奖日 | 开奖时间 | 建议推送时间 |
|------|--------|----------|--------------|
| 大乐透 | 周一、三、五、六 | 21:10 | 18:00-19:00 |
| 双色球 | 周二、四、日 | 21:15 | 18:00-19:00 |

---

## 测试任务

创建完成后，可以右键任务 → **运行** 来测试是否正常工作。
