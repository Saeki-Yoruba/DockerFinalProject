package com.supernovapos.finalproject.auth.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.supernovapos.finalproject.auth.model.entity.Permission;
import com.supernovapos.finalproject.auth.model.entity.Role;
import com.supernovapos.finalproject.auth.model.entity.RolePermission;
import com.supernovapos.finalproject.auth.model.entity.UserRole;
import com.supernovapos.finalproject.user.model.entity.User;

import lombok.Getter;

@Getter
public class CustomUserDetails implements UserDetails {

	private final User user;

	public CustomUserDetails(User user) {
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
	    Set<GrantedAuthority> authorities = new HashSet<>();

	    for (UserRole ur : user.getUserRoles()) {
	        Role role = ur.getRole();

	        // 加角色 (ROLE_ADMIN / ROLE_MANAGER ...)
	        authorities.add(new SimpleGrantedAuthority(role.getCode()));

	        // 加權限 (USER_READ / USER_WRITE ...)
	        for (RolePermission rp : role.getRolePermissions()) {
	            Permission p = rp.getPermission();
	            if (Boolean.TRUE.equals(p.getIsAvailable())) { // 確認權限啟用
	                authorities.add(new SimpleGrantedAuthority(p.getCode()));
	            }
	        }
	    }

	    return authorities;
	}

	@Override
	public String getUsername() {
		return user.getEmail();
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	/** 判斷帳號是否啟用 */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /** 判斷帳號是否未被鎖定 */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
}
