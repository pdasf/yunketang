package com.yunketang.system.service;

import com.yunketang.system.model.po.Dictionary;

import java.util.List;

/**
 * 数据字典 服务类
 */
public interface DictionaryService {

    /**
     * 查询所有数据字典内容
     */
    List<Dictionary> queryAll();

    /**
     * 根据code查询数据字典
     */
    Dictionary getByCode(String code);
}
