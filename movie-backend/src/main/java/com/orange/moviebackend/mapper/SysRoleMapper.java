package com.orange.moviebackend.mapper;

import com.orange.moviebackend.domain.SysRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface SysRoleMapper {

    List<SysRole> findAllRoles();

    SysRole findRoleById(Long id);

    int addRole(SysRole sysRole);

    int updateRole(SysRole sysRole);

    int deleteRole(Long id);

    int addRight(Long roleId, Long resourceId);

    int deleteRight(Long roleId, Long resourceId);

    List<Long> findAllRights(Long roleId);

}
