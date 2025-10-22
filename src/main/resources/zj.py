import requests
import re
import json

# 设置请求头信息
headers = {
    'authority': 'webapi.sporttery.cn',
    'method': 'GET',
    'path': '/gateway/lottery/getHistoryPageListV1.qry?gameNo=85&provinceId=0&pageSize=30&isVerify=1&pageNo=1',
    'scheme': 'https',
    'accept': 'application/json, text/javascript, */*; q=0.01',
    'accept-encoding': 'gzip, deflate, br, zstd',
    'accept-language': 'zh-CN,zh;q=0.9',
    'origin': 'https://static.sporttery.cn',
    'priority': 'u=1, i',
    'referer': 'https://static.sporttery.cn/',
    'sec-ch-ua': '"Google Chrome";v="141", "Not?A_Brand";v="8", "Chromium";v="141"',
    'sec-ch-ua-mobile': '?0',
    'sec-ch-ua-platform': '"Windows"',
    'sec-fetch-dest': 'empty',
    'sec-fetch-mode': 'cors',
    'sec-fetch-site': 'same-site',
    'sec-gpc': '1',
    'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36'
}

result = {}
url_page = "https://webapi.sporttery.cn/gateway/lottery/getHistoryPageListV1.qry?gameNo=85&provinceId=0&pageSize=30&isVerify=1&pageNo=1"

# 使用带有headers的请求
response_page = requests.get(url_page, headers=headers)
json_data_page = response_page.json()
data_page = json_data_page['value']
pages = data_page['pages']

url = "https://webapi.sporttery.cn/gateway/lottery/getHistoryPageListV1.qry?gameNo=85&provinceId=0&pageSize=30&isVerify=1&pageNo={}"  # 使用占位符

for page_no in range(1, 2):  # 只处理第一页
    current_url = url.format(page_no)
    
    response = requests.get(current_url, headers=headers)  # 使用headers
    if response.status_code == 200:  # 检查请求是否成功
        try:
            json_data = response.json()  # 尝试解析 JSON
            data = json_data['value']
            for record in data['list'][:5]:  # 只获取前五条记录
                phone = record['lotteryDrawResult']
                modified_phone = re.sub(r'\s', '|', phone)
                draw_time = record['lotteryDrawTime']
                draw_num = record.get('lotteryDrawNum', '')  # 获取期号
                result[draw_time] = modified_phone
        except json.JSONDecodeError:
            print(f"Failed to decode JSON for page {page_no}. Response text: {response.text}")
        except Exception as e:
            print(f"处理数据时出错: {str(e)}")
    else:
        print(f"请求失败，状态码: {response.status_code}, 响应内容: {response.text[:200]}")

# 移动文件写入到循环外部
filename = "dlt_bak.json"
with open(filename, "w") as json_file:
    json.dump(result, json_file)
print(f"Data has been written to {filename}")

result2 = {}
url2 = "http://www.cwl.gov.cn/cwl_admin/front/cwlkj/search/kjxx/findDrawNotice?name=ssq&issueCount=&issueStart=&issueEnd=&dayStart=&dayEnd=&week=&systemType=PC&pageNo=1&pageSize=5"
response2 = requests.get(url2)
json_data2 = response2.json()
data_list2 = json_data2['result']
for record in data_list2:
    phone1 = record['red']
    phone2 = record['blue']
    modified_phone = re.sub(',', '|', phone1)+'|'+phone2
    result2[record['date'][0:10]] = modified_phone

# 指定要写入的文件名
filename2 = "ssq_bak.json"
# 使用with语句打开文件并将数据写入
with open(filename2, "w") as json_file2:
    json.dump(result2, json_file2)
print(f"Data has been written to {filename2}")