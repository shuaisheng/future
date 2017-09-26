package com.kangyonggan.app.future.biz.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.kangyonggan.app.future.biz.service.CodeService;
import com.kangyonggan.app.future.biz.service.TableService;
import com.kangyonggan.app.future.common.util.FileUtil;
import com.kangyonggan.app.future.common.util.StringUtil;
import com.kangyonggan.app.future.model.annotation.LogTime;
import com.kangyonggan.app.future.model.constants.AppConstants;
import com.kangyonggan.app.future.model.dto.Step1;
import com.kangyonggan.app.future.model.dto.Step2;
import com.kangyonggan.app.future.model.dto.Step3;
import com.kangyonggan.app.future.model.vo.Code;
import com.kangyonggan.app.future.model.vo.DbColumn;
import com.kangyonggan.app.future.model.vo.DbTable;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kangyonggan
 * @since 2017/9/24 0024
 */
@Service
@Log4j2
public class CodeServiceImpl extends BaseService<Code> implements CodeService {

    @Autowired
    private TableService tableService;

    @Override
    @LogTime
    public List<Code> searchCodes(int pageNum, String tableName, String menuName) {
        Example example = new Example(Code.class);

        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotEmpty(tableName)) {
            criteria.andEqualTo("tableName", tableName);
        }
        if (StringUtils.isNotEmpty(menuName)) {
            criteria.andEqualTo("menuName", menuName);
        }

        example.setOrderByClause("id desc");

        PageHelper.startPage(pageNum, AppConstants.PAGE_SIZE);
        return myMapper.selectByExample(example);
    }

    @Override
    @LogTime
    public void saveCode(Code code) {
        myMapper.insertSelective(code);
    }

    @Override
    @LogTime
    public Code findCodeById(Long id) {
        return myMapper.selectByPrimaryKey(id);
    }

    @Override
    @LogTime
    public void updateStep1(Step1 step1) {
        Code code = new Code();
        code.setId(step1.getCodeId());
        code.setStep1(JSON.toJSONString(step1));

        myMapper.updateByPrimaryKeySelective(code);
    }

    @Override
    @LogTime
    public void updateStep2(Step2 step2) {
        Code code = new Code();
        code.setId(step2.getCodeId());
        code.setStep2(JSON.toJSONString(step2));

        myMapper.updateByPrimaryKeySelective(code);
    }

    @Override
    @LogTime
    public void updateStep3(Step3 step3) {
        Code code = new Code();
        code.setId(step3.getCodeId());
        code.setStep3(JSON.toJSONString(step3));

        myMapper.updateByPrimaryKeySelective(code);
    }

    @Override
    @LogTime
    public void updateCode(Code code) {
        myMapper.updateByPrimaryKeySelective(code);
    }

    @Override
    @LogTime
    public void generateCode(Long id) {
        Code code = myMapper.selectByPrimaryKey(id);
        String tableName = code.getTableName();
        DbTable table = tableService.findTableByName(code.getTableName());
        List<DbColumn> columns = tableService.findTableColumns(code.getTableName());
        log.info("正在生成{}表对应的MBG", tableName);

        // 获取项目路径及项目名
        String bizDir = System.getProperty("user.dir");
        String baseBir = bizDir.substring(0, bizDir.lastIndexOf("/"));
        baseBir += bizDir.substring(bizDir.lastIndexOf("/"), bizDir.lastIndexOf("-"));
        String modelName = StringUtil.convertTableName(code.getTableName());

        // 各个包名
        String modelPackage = code.getPackageName() + ".model.vo";
        log.info("Model.java的包名为：{}", modelPackage);
        String mapperPackage = code.getPackageName() + ".mapper";
        log.info("Mapper.java的包名为：{}", mapperPackage);
        String mapperXmlPackage = "mapper";
        log.info("Mapper.xml的包名为：{}", mapperXmlPackage);
        String servicePackage = code.getPackageName() + ".biz.service";
        log.info("Service.java的包名为：{}", servicePackage);
        String serviceImplPackage = code.getPackageName() + ".biz.service.impl";
        log.info("ServiceImpl.java的包名为：{}", serviceImplPackage);

        // 准备数据
        Map<String, Object> rootMap = new HashMap();
        rootMap.put("modelName", modelName);
        rootMap.put("modelPackage", modelPackage);
        rootMap.put("mapperPackage", mapperPackage);
        rootMap.put("mapperXmlPackage", mapperXmlPackage);
        rootMap.put("servicePackage", servicePackage);
        rootMap.put("serviceImplPackage", serviceImplPackage);
        rootMap.put("author", "Generator");
        rootMap.put("code", code);
        rootMap.put("table", table);
        rootMap.put("columns", columns);
        rootMap.put("step1", JSON.parseObject(code.getStep1(), Step1.class));
        rootMap.put("step2", JSON.parseObject(code.getStep2(), Step2.class));
        rootMap.put("step3", JSON.parseObject(code.getStep3(), Step3.class));

        // 生成Model.java、Mapper.java、Mapper.xml
        generateMBG(baseBir, rootMap);

        // 生成Service.java
        generateService(baseBir, rootMap);

        // 生成ServiceImpl.java
        generateServiceImpl(baseBir, rootMap);

        // TODO 生成Controller.java
        // TODO 生成list.ftl及包含的ftl和对应的js
        // TODO 生成form-model.ftl及包含的ftl和对应的js
        // TODO 生成detail-model.ftl及包含的ftl和对应的js
    }

    /**
     * 生成ServiceImpl.java
     *
     * @param baseBir
     * @param rootMap
     */
    private void generateServiceImpl(String baseBir, Map<String, Object> rootMap) {
        try {
            String serviceContent = generate("ServiceImpl.java.ftl", rootMap);
            String serviceImplPackage = (String) rootMap.get("serviceImplPackage");
            String modelName = (String) rootMap.get("modelName");
            String fileName = baseBir + "-biz/src/main/java/" + serviceImplPackage.replaceAll("\\.", "/") + "/" + modelName + "ServiceImpl.java";
            FileUtil.writeTextToFile(fileName, serviceContent);
            log.info("{}已经生成完毕", fileName);

        } catch (Exception e) {
            log.warn("生成ServiceImpl.java异常", e);
        }
    }

    /**
     * 生成Service.java
     *
     * @param baseBir
     * @param rootMap
     */
    private void generateService(String baseBir, Map<String, Object> rootMap) {
        try {
            String serviceContent = generate("Service.java.ftl", rootMap);
            String servicePackage = (String) rootMap.get("servicePackage");
            String modelName = (String) rootMap.get("modelName");
            String fileName = baseBir + "-biz/src/main/java/" + servicePackage.replaceAll("\\.", "/") + "/" + modelName + "Service.java";
            FileUtil.writeTextToFile(fileName, serviceContent);
            log.info("{}已经生成完毕", fileName);

        } catch (Exception e) {
            log.warn("生成Service.java异常", e);
        }
    }

    /**
     * 生成MBG
     *
     * @param baseBir
     * @param rootMap
     */
    private void generateMBG(String baseBir, Map<String, Object> rootMap) {
        try {
            // 生成Model.java
            String modelContent = generate("Model.java.ftl", rootMap);
            String modelPackage = (String) rootMap.get("modelPackage");
            String modelName = (String) rootMap.get("modelName");
            String fileName = baseBir + "-model/src/main/java/" + modelPackage.replaceAll("\\.", "/") + "/" + modelName + ".java";
            FileUtil.writeTextToFile(fileName, modelContent);
            log.info("{}已经生成完毕", fileName);

            // 生成Mapper.java
            String mapperContent = generate("Mapper.java.ftl", rootMap);
            String mapperPackage = (String) rootMap.get("mapperPackage");
            fileName = baseBir + "-dao/src/main/java/" + mapperPackage.replaceAll("\\.", "/") + "/" + modelName + "Mapper.java";
            FileUtil.writeTextToFile(fileName, mapperContent);
            log.info("{}已经生成完毕", fileName);

            // 生成Mapper.xml
            String mapperXmlContent = generate("Mapper.xml.ftl", rootMap);
            String mapperXmlPackage = (String) rootMap.get("mapperXmlPackage");
            fileName = baseBir + "-dao/src/main/resources/" + mapperXmlPackage.replaceAll("\\.", "/") + "/" + modelName + "Mapper.xml";
            FileUtil.writeTextToFile(fileName, mapperXmlContent);
            log.info("{}已经生成完毕", fileName);
        } catch (Exception e) {
            log.warn("生成MBG异常", e);
        }
    }

    /**
     * 代码生成
     *
     * @param templateName
     * @param root
     * @return
     * @throws Exception
     */
    private String generate(String templateName, Map<String, Object> root) throws Exception {
        Configuration cfg = new Configuration();
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        Template template = cfg.getTemplate(templateName, "UTF-8");

        StringWriter writer = new StringWriter();

        template.process(root, writer);
        return writer.toString();
    }
}
