package com.orange.moviebackend.service.impl;

import com.orange.moviebackend.common.utils.JwtUtil;
import com.orange.moviebackend.common.utils.SaltUtils;
import com.orange.moviebackend.domain.LoginUser;
import com.orange.moviebackend.domain.SysUser;
import com.orange.moviebackend.domain.vo.SysUserVo;
import com.orange.moviebackend.mapper.SysUserMapper;
import com.orange.moviebackend.service.SysUserService;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Service("sysUserService")
public class SysUserServiceImpl implements SysUserService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public List<SysUser> findAllUsers(SysUser sysUser) {
        return sysUserMapper.findAllUsers(sysUser);
    }

    @Override
    public SysUser findUserById(Long id) {
        return sysUserMapper.findUserById(id);
    }

    @Override
    public SysUser findUserByName(String userName) {
        return sysUserMapper.findUserByName(userName);
    }

    /**
     * Handle registration logic
     *
     * @param sysUser the user to add
     * @return number of rows affected
     */
    @Override
    public int addUser(SysUser sysUser) {
        if (!isUserNameUnique(sysUser.getUserName(), -1L)) {
            throw new AuthenticationException("Username already exists");
        }
        // Process password: MD5 + salt + 1024 iterations
        String salt = SaltUtils.getSalt(8);
        Md5Hash md5Hash = new Md5Hash(sysUser.getPassword(), salt, 1024);

        sysUser.setPassword(md5Hash.toHex());
        sysUser.setSalt(salt);
        return sysUserMapper.addUser(sysUser);
    }

    @Override
    public int updateUser(SysUser userFromRequest) {
        if (userFromRequest == null || userFromRequest.getUserId() == null) {
            throw new IllegalArgumentException("User or User ID from request cannot be null for update.");
        }

        SysUser existingUser = sysUserMapper.findUserById(userFromRequest.getUserId());
        if (existingUser == null) {
            throw new AuthenticationException("User not found with ID: " + userFromRequest.getUserId() + ". Cannot update.");
        }

        existingUser.setEmail(userFromRequest.getEmail());

        if (StringUtils.hasText(userFromRequest.getPhoneNumber())) {
            existingUser.setPhoneNumber(userFromRequest.getPhoneNumber());
        } else {
            throw new IllegalArgumentException("Phone number cannot be empty for update based on request data.");
        }

        if (userFromRequest.getSex() != null) {
            existingUser.setSex(userFromRequest.getSex());
        } else {
            throw new IllegalArgumentException("Gender (sex) cannot be null for update based on request data.");
        }

        existingUser.setUserPicture(userFromRequest.getUserPicture());

        if (StringUtils.hasText(userFromRequest.getBirthday())) {
            existingUser.setBirthday(userFromRequest.getBirthday());
        } else {
            existingUser.setBirthday(null);
        }

        existingUser.setAutograph(userFromRequest.getAutograph());

        return sysUserMapper.updateUser(existingUser);
    }

    @Override
    public int deleteUser(Long[] ids) {
        int rows = 0;
        for (Long id : ids) {
            rows += sysUserMapper.deleteUser(id);
        }
        return rows;
    }

    @Override
    public LoginUser login(SysUserVo sysUserVo) {
        // Login: first query user information
        SysUser user = sysUserMapper.findUserByName(sysUserVo.getUserName());
        if (user == null) {
            throw new AuthenticationException("Username does not exist");
        }

        // Validate password
        Md5Hash md5Hash = new Md5Hash(sysUserVo.getPassword(), user.getSalt(), 1024);
        if (!user.getPassword().equals(md5Hash.toHex())) {
            throw new AuthenticationException("Incorrect username or password");
        }

        // Set login user object
        LoginUser loginUser = findLoginUser(sysUserVo);

        // Issue token
        String token = JwtUtil.sign(user.getUserName(), user.getPassword());
        loginUser.setToken(token);
        return loginUser;
    }

    @Override
    public LoginUser findLoginUser(SysUserVo sysUserVo) {
        return sysUserMapper.findLoginUser(sysUserVo);
    }

    @Override
    public boolean isUserNameUnique(String userName, Long userId) {
        List<Long> userIds = sysUserMapper.checkUserNameUnique(userName);
        for (Long id : userIds) {
            if (id.equals(userId)) {
                return true;
            }
        }
        return userIds.isEmpty();
    }
}
