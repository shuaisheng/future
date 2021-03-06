package com.kangyonggan.app.future.biz.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.kangyonggan.app.future.biz.service.StorageService;
import com.kangyonggan.app.future.biz.util.PropertiesUtil;
import com.kangyonggan.app.future.common.util.FileUtil;
import com.kangyonggan.app.future.common.util.HttpUtil;
import com.kangyonggan.app.future.mapper.StorageMapper;
import com.kangyonggan.app.future.model.constants.AppConstants;
import com.kangyonggan.app.future.model.vo.Storage;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kangyonggan
 * @since 9/8/17
 */
@Service
@Log4j2
public class StorageServiceImpl extends BaseService<Storage> implements StorageService {

    @Autowired
    private StorageMapper storageMapper;

    @Override
    public void saveJZTKStorages(String subject) {
        try {
            // 抓取科目
            log.info("开始抓取科目{}", subject);
            saveJZTK(subject);
            log.info("抓取科目{}完成", subject);
        } catch (Exception e) {
            log.warn("初始化驾照题库失败", e);
        }
    }

    @Override
    public List<Storage> findStoragesByPage(int pageNum, String subject) {
        Example example = new Example(Storage.class);
        example.createCriteria().andEqualTo("subject", subject).andEqualTo("isDeleted", AppConstants.IS_DELETED_NO);

        example.setOrderByClause("id desc");

        PageHelper.startPage(pageNum, 100);
        return myMapper.selectByExample(example);
    }

    /**
     * 抓取驾照题库
     *
     * @param subject
     * @throws Exception
     */
    private void saveJZTK(String subject) throws Exception {
        String params = "key=" + PropertiesUtil.getProperties("app.key") + "&subject=" + subject + "&testType=order";
        if (subject.equals("1")) {
            params = "key=" + PropertiesUtil.getProperties("app.key") + "&subject=" + subject + "&model=c1&testType=order";
        }
        String result = HttpUtil.sendGet("http://api.avatardata.cn/Jztk/Query", params);
        JSONObject jsonObject = JSON.parseObject(result);

        String errorCode = jsonObject.getString("error_code");
        String reason = jsonObject.getString("reason");
        log.info("错误码：{}", errorCode);
        log.info("错误描述：{}", reason);

        if ("0".equals(errorCode)) {
            JSONArray data = jsonObject.getJSONArray("result");

            List<Storage> storages = new ArrayList();
            for (int i = 0; i < data.size(); i++) {
                JSONObject item = data.getJSONObject(i);
                Storage storage = new Storage();
                storage.setType("jztk");
                storage.setModel("");
                storage.setSubject(subject);
                storage.setQuestion(item.getString("question"));
                storage.setAnswer(item.getString("answer"));
                storage.setOption1(item.getString("item1"));
                storage.setOption2(item.getString("item2"));
                storage.setOption3(item.getString("item3"));
                storage.setOption4(item.getString("item4"));
                storage.setExplains(item.getString("explains"));
                String url = item.getString("url");

                if (StringUtils.isNotEmpty(url)) {
                    String filename = "jztk/" + subject + "-" + i + url.substring(url.lastIndexOf("."));
                    FileUtil.downloadFromUrl(url, PropertiesUtil.getProperties(AppConstants.FILE_PATH_ROOT) + filename);
                    storage.setUrl(filename);
                } else {
                    storage.setUrl("");
                }

                storages.add(storage);
            }
            storageMapper.insertStorages(storages);
        } else {
            log.warn("抓取失败,{}", reason);
        }
    }
}
