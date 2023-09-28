package com.yunketang.system.service.impl;

import com.yunketang.system.mapper.DictionaryMapper;
import com.yunketang.system.model.po.Dictionary;
import com.yunketang.system.service.DictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据字典 服务实现类
 */
@Service
public class DictionaryServiceImpl implements DictionaryService {

    @Autowired
    DictionaryMapper dictionaryMapper;

    public List<Dictionary> queryAll() {
        return dictionaryMapper.list();
    }

    public Dictionary getByCode(String code) {
        return dictionaryMapper.getByCode(code);
    }
}
