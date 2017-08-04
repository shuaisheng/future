package com.kangyonggan.app.future.biz.service.impl;

import com.github.pagehelper.PageHelper;
import com.kangyonggan.app.future.biz.service.UserService;
import com.kangyonggan.app.future.common.util.Digests;
import com.kangyonggan.app.future.common.util.Encodes;
import com.kangyonggan.app.future.common.util.StringUtil;
import com.kangyonggan.app.future.mapper.RoleMapper;
import com.kangyonggan.app.future.mapper.UserMapper;
import com.kangyonggan.app.future.model.annotation.CacheDelete;
import com.kangyonggan.app.future.model.annotation.CacheDeleteAll;
import com.kangyonggan.app.future.model.annotation.CacheGetOrSave;
import com.kangyonggan.app.future.model.annotation.LogTime;
import com.kangyonggan.app.future.model.constants.AppConstants;
import com.kangyonggan.app.future.model.vo.ShiroUser;
import com.kangyonggan.app.future.model.vo.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.List;

/**
 * @author kangyonggan
 * @since 8/4/17
 */
@Service
public class UserServiceImpl extends BaseService<User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    @LogTime
    public ShiroUser getShiroUser() {
        return (ShiroUser) SecurityUtils.getSubject().getPrincipal();
    }

    @Override
    @LogTime
    @CacheGetOrSave("user:username:{0}")
    public User findUserByUsername(String username) {
        if (StringUtils.isEmpty(username)) {
            return null;
        }

        User user = new User();
        user.setUsername(username);
        return myMapper.selectOne(user);
    }

    @Override
    @LogTime
    public List<User> searchUsers(int pageNum, String username, String realname, String email) {
        Example example = new Example(User.class);
        Example.Criteria criteria = example.createCriteria();

        if (StringUtils.isNotEmpty(username)) {
            criteria.andLike("username", StringUtil.toLikeString(username));
        }

        if (StringUtils.isNotEmpty(realname)) {
            criteria.andLike("realname", StringUtil.toLikeString(realname));
        }

        if (StringUtils.isNotEmpty(email)) {
            criteria.andLike("email", StringUtil.toLikeString(email));
        }

        example.setOrderByClause("id desc");

        PageHelper.startPage(pageNum, AppConstants.PAGE_SIZE);
        return myMapper.selectByExample(example);
    }

    @Override
    @LogTime
    public void saveUserWithDefaultRole(User user) {
        entryptPassword(user);

        myMapper.insertSelective(user);

        saveUserRoles(user.getUsername(), AppConstants.DEFAULT_ROLE_CODE);
    }

    @Override
    @LogTime
    @CacheDelete("user:username:{0:username}||role:username:{0:username}||menu:username:{0:username}")
    public void updateUserByUsername(User user) {
        if (StringUtils.isEmpty(user.getUsername())) {
            return;
        }

        Example example = new Example(User.class);
        example.createCriteria().andEqualTo("username", user.getUsername());
        myMapper.updateByExampleSelective(user, example);
    }

    @Override
    public User findUserById(Long id) {
        return myMapper.selectByPrimaryKey(id);
    }

    @Override
    @LogTime
    @CacheDelete("user:username:{0:username}")
    public void updateUserPassword(User user) {
        entryptPassword(user);

        updateUserByUsername(user);
    }

    @Override
    @LogTime
    @CacheDelete("role:username:{0}||menu:username:{0}")
    public void updateUserRoles(String username, String roleCodes) {
        roleMapper.deleteAllRolesByUsername(username);

        if (StringUtils.isNotEmpty(roleCodes)) {
            saveUserRoles(username, roleCodes);
        }
    }

    @Override
    @LogTime
    public boolean existsUsername(String username) {
        User user = new User();
        user.setUsername(username);

        return super.exists(user);
    }

    /**
     * 批量保存用户角色
     *
     * @param username
     * @param roleCodes
     */
    private void saveUserRoles(String username, String roleCodes) {
        userMapper.insertUserRoles(username, Arrays.asList(roleCodes.split(",")));
    }

    /**
     * 设定安全的密码，生成随机的salt并经过N次 sha-1 hash
     *
     * @param user
     */
    private void entryptPassword(User user) {
        byte[] salt = Digests.generateSalt(AppConstants.SALT_SIZE);
        user.setSalt(Encodes.encodeHex(salt));

        byte[] hashPassword = Digests.sha1(user.getPassword().getBytes(), salt, AppConstants.HASH_INTERATIONS);
        user.setPassword(Encodes.encodeHex(hashPassword));
    }
}
