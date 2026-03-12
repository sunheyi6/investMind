package com.investmind.util;

import com.investmind.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 安全工具类
 * 提供获取当前登录用户等安全相关的方法
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户的用户名
     *
     * @return 用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        return null;
    }

    /**
     * 获取当前登录用户的ID
     *
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof Long) {
            return (Long) authentication.getDetails();
        }
        // 从principal中获取（如果存储了用户信息）
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) authentication.getPrincipal()).getId();
        }
        return null;
    }

    /**
     * 获取当前认证信息
     *
     * @return 认证信息
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 检查用户是否已认证
     *
     * @return 是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()));
    }

    /**
     * 获取当前登录用户对象
     *
     * @return User对象
     */
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            User user = new User();
            user.setId(principal.getId());
            user.setUsername(principal.getUsername());
            user.setUserType(principal.getUserType());
            return user;
        }
        return null;
    }

    /**
     * 用户主体类
     * 实现UserDetails接口，用于Spring Security
     */
    public static class UserPrincipal implements UserDetails {

        private final Long id;
        private final String username;
        private final String password;
        private final String userType;
        private final boolean enabled;
        private final java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities;

        public UserPrincipal(Long id, String username, String password, String userType,
                             boolean enabled,
                             java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.userType = userType;
            this.enabled = enabled;
            this.authorities = authorities;
        }

        public Long getId() {
            return id;
        }

        public String getUserType() {
            return userType;
        }

        @Override
        public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }
    }
}
