package com.yunketang.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunketang.system.model.po.Dictionary;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 数据字典 Mapper 接口
 */
@Mapper
public interface DictionaryMapper extends BaseMapper<Dictionary> {

    List<Dictionary> list();

    Dictionary getByCode(String code);
}
