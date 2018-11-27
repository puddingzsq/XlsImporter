package com.yonyou.ny.yht.importer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yonyou.yht.sdk.UserCenter;
import com.yonyou.yht.sdkutils.YhtClientPropertyUtil;
import org.apache.poi.ss.usermodel.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author lipangeng, Email:lipg@outlook.com
 * @version 1.0 on 2018/11/13 3:08 PM
 * @since 1.0 Created by lipangeng on 2018/11/13 3:08 PM. Email:lipg@outlook.com.
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class Importer {
    private static final Logger log = LoggerFactory.getLogger(Importer.class);
    private static ObjectMapper mapper = new ObjectMapper();
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private String pwd = "jw123456";

    static {
        // 设置环境变量获取配置文件
        System.setProperty("YHT_SDK_FILEPATH", "src/test/resources/qiluys/test/sdk.properties");
        // 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        // 空对象不要抛出异常
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 美化输出
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * 启动类
     *
     * @since 1.0 Created by lipangeng on 2018/11/13 3:29 PM. Email:lipg@outlook.com
     */
    @Test
    public void start() throws IOException {
        // 成功添加的用户
        LinkedList<HashMap<String, String>> success = Lists.newLinkedList();
        // 添加失败的用户
        LinkedList<HashMap<String, String>> fails = Lists.newLinkedList();

        LinkedList<HashMap<String, String>> users = readUsers_qiluys();
        //LinkedList<HashMap<String, String>> users = readUsers_tyyunshang();
        //LinkedList<HashMap<String, String>> users = readUsers_ipu_snjt();
        for (HashMap<String, String> user : users) {
            // 添加默认信息
            // 设置随机的默认密码
            //user.put("userPassword", UUID.randomUUID().toString());
            user.put("userPassword",pwd);
            if (importUser(user)) {
                success.add(user);
            } else {
                fails.add(user);
            }
        }
        exportResult(success, fails);
    }

    /**
     * 导出结果
     *
     * @since 1.0 Created by lipangeng on 2018/11/13 3:32 PM. Email:lipg@outlook.com
     */
    public void exportResult(LinkedList<HashMap<String, String>> success,
                             LinkedList<HashMap<String, String>> fails) throws IOException {
        log.info("Import Complete,Success:{},Fail:{}", success.size(), fails.size());
        log.info("Success:{}", success.toString());
        log.error("Fails:{}", fails.toString());
        long time = System.currentTimeMillis();
        String dir = "target/" + time + "/" + YhtClientPropertyUtil.getPropertyByKey("sysid");
        if (! new File(dir).exists()) {
            new File(dir).mkdirs();
        }
        Files.write(Paths.get(dir + "/success.json"),
                    mapper.writeValueAsString(success).getBytes(StandardCharsets.UTF_8));
        Files.write(Paths.get(dir + "/fails.json"), mapper.writeValueAsString(fails).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 导入用户
     *
     * @since 1.0 Created by lipangeng on 2018/11/13 3:09 PM. Email:lipg@outlook.com
     */
    public boolean importUser(Map<String, String> user) {
        String result = UserCenter.addUser(user);
        user.put("importResult", result);
        if (Strings.isNullOrEmpty(result)) {
            return false;
        }
        Result resultObj = gson.fromJson(result, Result.class);
        return Objects.equals("1", resultObj.getStatus());
    }

    /**
     * 读取用户信息
     *
     * @since 1.0 Created by lipangeng on 2018/11/13 3:15 PM. Email:lipg@outlook.com
     */
    public LinkedList<HashMap<String, String>> readUsers_qiluys() throws IOException {
        // 用户列表
        LinkedList<HashMap<String, String>> users = Lists.newLinkedList();
        Workbook workbook = WorkbookFactory.create(new File("C:\\Users\\jw\\Desktop\\QiluYs_JW_Pro.xls"));
        Sheet sheet = workbook.getSheetAt(0);
        // System.out.print(sheet);
        // 获取总的列长度
        int lastRowNum = sheet.getLastRowNum();
        // 循环获取行信息
        for (int i = 1; i < lastRowNum; i++) {
            Row row = sheet.getRow(i);
            int finalI = i;
            users.add(new LinkedHashMap<String, String>() {{
                put("row", String.valueOf(finalI));
                // 用户名
                put("userCode",getCellVale(row.getCell(1)));
                // 用户姓名
                put("userName", getCellVale(row.getCell(2)));
                // 手机号
                put("userMobile", getCellVale(row.getCell(3)));
                // 邮箱
                put("userEmail", getCellVale(row.getCell(4)));
            }});
        }
        return users;
    }

    /**
     * 获取Cell值
     *
     * @since 1.0 Created by lipangeng on 2018/11/13 3:38 PM. Email:lipg@outlook.com
     */
    private String getCellVale(Cell cell) {
        if (Objects.isNull(cell)) {
            return null;
        }
        CellType cellType = cell.getCellType();
        if (cellType != null && cellType == CellType.NUMERIC) {
            double numericCellValue = cell.getNumericCellValue();
            return new DecimalFormat("0").format(numericCellValue);
        }
        return cell.toString();
    }

    /**
     * 读取用户信息
     *
     * @since 1.0 Created by lipangeng on 2018/11/13 3:15 PM. Email:lipg@outlook.com
     */
    public LinkedList<HashMap<String, String>> readUsers_tyyunshang() throws IOException {
        // 用户列表
        LinkedList<HashMap<String, String>> users = Lists.newLinkedList();
        Workbook workbook = WorkbookFactory.create(new File("C:\\Users\\jw\\Desktop\\dspt.xls"));

        Sheet sheet = workbook.getSheetAt(0);
        // System.out.print(sheet);
        // 获取总的列长度
        int lastRowNum = sheet.getLastRowNum();
        // 循环获取行信息
        for (int i = 1; i < lastRowNum; i++) {
            Row row = sheet.getRow(i);
            int finalI = i;
            users.add(new LinkedHashMap<String, String>() {{
                put("row", String.valueOf(finalI));
                // 用户名
                put("userCode",getCellVale(row.getCell(1)));
                // 用户姓名
                put("userName", getCellVale(row.getCell(2)));
                // 手机号
                put("userMobile", getCellVale(row.getCell(3)));
                // 邮箱
                put("userEmail", getCellVale(row.getCell(4)));
            }});
        }
        return users;
    }

    public LinkedList<HashMap<String, String>> readUsers_ipu_snjt() throws IOException {
        // 用户列表
        LinkedList<HashMap<String, String>> users = Lists.newLinkedList();
        Workbook workbook = WorkbookFactory.create(new File("C:\\Users\\jw\\Desktop\\dspt.xls"));
        Sheet sheet = workbook.getSheetAt(0);
        // System.out.print(sheet);
        // 获取总的列长度``
        int lastRowNum = sheet.getLastRowNum();
        // 循环获取行信息
        for (int i = 1; i < lastRowNum; i++) {
            Row row = sheet.getRow(i);
            int finalI = i;
            users.add(new LinkedHashMap<String, String>() {{
                put("row", String.valueOf(finalI));
                // 用户名
                put("userCode",getCellVale(row.getCell(1)));
                // 用户姓名
                put("userName", getCellVale(row.getCell(2)));
                // 手机号
                put("userMobile", getCellVale(row.getCell(3)));
                // 邮箱
                put("userEmail", getCellVale(row.getCell(4)));
            }});
        }
        return users;
    }
}