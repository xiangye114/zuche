package com.zuche.test.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试报告生成器
 */
public class TestReportGenerator {

    private String moduleName;
    private String moduleCode;
    private List<TestCaseResult> results = new ArrayList<>();
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public TestReportGenerator(String moduleCode, String moduleName) {
        this.moduleCode = moduleCode;
        this.moduleName = moduleName;
        this.startTime = LocalDateTime.now();
    }

    public void addResult(String caseId, String caseName, boolean passed, String message) {
        results.add(new TestCaseResult(caseId, caseName, passed, message));
    }

    public void finish() {
        this.endTime = LocalDateTime.now();
    }

    public String generateMarkdownReport() {
        StringBuilder sb = new StringBuilder();
        
        // 标题
        sb.append("# ").append(moduleName).append(" 测试报告\n\n");
        
        // 模块信息
        sb.append("## 1. 模块信息\n\n");
        sb.append("| 项目 | 内容 |\n");
        sb.append("|------|------|\n");
        sb.append("| 模块编号 | ").append(moduleCode).append(" |\n");
        sb.append("| 模块名称 | ").append(moduleName).append(" |\n");
        sb.append("| 测试时间 | ").append(formatTime(startTime)).append(" ~ ").append(formatTime(endTime)).append(" |\n");
        sb.append("| 测试人员 | 自动化测试 |\n\n");
        
        // 测试统计
        sb.append("## 2. 测试统计\n\n");
        long passedCount = results.stream().filter(r -> r.passed).count();
        long failedCount = results.stream().filter(r -> !r.passed).count();
        double passRate = results.isEmpty() ? 0 : (double) passedCount / results.size() * 100;
        
        sb.append("| 统计项 | 数值 |\n");
        sb.append("|-------|------|\n");
        sb.append("| 用例总数 | ").append(results.size()).append(" |\n");
        sb.append("| 通过用例 | ").append(passedCount).append(" |\n");
        sb.append("| 失败用例 | ").append(failedCount).append(" |\n");
        sb.append("| 用例通过率 | ").append(String.format("%.2f%%", passRate)).append(" |\n\n");
        
        // 用例执行详情
        sb.append("## 3. 用例执行详情\n\n");
        sb.append("| 序号 | 用例编号 | 用例标题 | 状态 | 备注 |\n");
        sb.append("|------|---------|---------|------|------|\n");
        
        int index = 1;
        for (TestCaseResult result : results) {
            sb.append("| ").append(index++).append(" | ")
              .append(result.caseId).append(" | ")
              .append(result.caseName).append(" | ")
              .append(result.passed ? "✅ 通过" : "❌ 失败").append(" | ")
              .append(result.message != null ? result.message : "").append(" |\n");
        }
        sb.append("\n");
        
        // 测试结论
        sb.append("## 4. 测试结论\n\n");
        if (passRate >= 95) {
            sb.append("**✅ 通过** - 用例通过率达到 ").append(String.format("%.2f%%", passRate)).append("，符合验收标准\n");
        } else if (passRate >= 80) {
            sb.append("**⚠️ 有条件通过** - 用例通过率为 ").append(String.format("%.2f%%", passRate)).append("，存在部分问题需要修复\n");
        } else {
            sb.append("**❌ 不通过** - 用例通过率仅为 ").append(String.format("%.2f%%", passRate)).append("，需要修复后重新测试\n");
        }
        sb.append("\n");
        
        // 失败用例清单
        if (failedCount > 0) {
            sb.append("## 5. 失败用例清单\n\n");
            sb.append("| 用例编号 | 用例标题 | 失败原因 |\n");
            sb.append("|---------|---------|---------|\n");
            for (TestCaseResult result : results) {
                if (!result.passed) {
                    sb.append("| ").append(result.caseId).append(" | ")
                      .append(result.caseName).append(" | ")
                      .append(result.message).append(" |\n");
                }
            }
            sb.append("\n");
        }
        
        // 签字确认
        sb.append("## 6. 签字确认\n\n");
        sb.append("测试人员：________  日期：________\n");
        sb.append("开发人员：________  日期：________\n");
        sb.append("产品经理：________  日期：________\n");
        
        return sb.toString();
    }

    public void saveReport(String filePath) throws IOException {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(generateMarkdownReport());
        }
    }

    private String formatTime(LocalDateTime time) {
        if (time == null) return "";
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private static class TestCaseResult {
        String caseId;
        String caseName;
        boolean passed;
        String message;

        TestCaseResult(String caseId, String caseName, boolean passed, String message) {
            this.caseId = caseId;
            this.caseName = caseName;
            this.passed = passed;
            this.message = message;
        }
    }
}
