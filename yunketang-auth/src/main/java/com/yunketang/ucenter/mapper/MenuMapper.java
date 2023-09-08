package com.yunketang.ucenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunketang.ucenter.model.po.Menu;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MenuMapper extends BaseMapper<Menu> {
    @Select("SELECT	* FROM yunketang_menu WHERE id IN (SELECT menu_id FROM yunketang_permission WHERE role_id IN ( SELECT role_id FROM yunketang_user_role WHERE user_id = #{userId} ))")
    List<Menu> selectPermissionByUserId(@Param("userId") String userId);
}
