package com.orange.moviebackend.domain;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Objects;

@Data
public class SysUser implements Serializable {

    private final static Long serialVersionUID = 1L;

    // User ID
    private Long userId;

    // Username
    @NotBlank(message = "Username cannot be blank")
    private String userName;

    // Password
    @NotBlank(message = "Password cannot be blank")
    private String password;

    // Salt (used for password encryption)
    private String salt;

    // Email address
    @Email(message = "Invalid email format")
    private String email;

    // Phone number
    @Pattern(regexp = "^[0-9]{8,15}$", message = "Phone number must be between 8 and 15 digits and contain only digits.")
    private String phoneNumber;

    // Gender
    private Integer sex;

    // User profile picture
    private String userPicture;

    // Role ID associated with the user (1-to-1 mapping for simplicity)
    private Long roleId;

    // Birthday
    private String birthday;

    // User's personal signature
    private String autograph;

    // User role
    private SysRole sysRole;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SysUser sysUser = (SysUser) o;
        return Objects.equals(userId, sysUser.userId) &&
                Objects.equals(userName, sysUser.userName) &&
                Objects.equals(password, sysUser.password) &&
                Objects.equals(salt, sysUser.salt) &&
                Objects.equals(email, sysUser.email) &&
                Objects.equals(phoneNumber, sysUser.phoneNumber) &&
                Objects.equals(sex, sysUser.sex) &&
                Objects.equals(userPicture, sysUser.userPicture) &&
                Objects.equals(roleId, sysUser.roleId) &&
                Objects.equals(birthday, sysUser.birthday) &&
                Objects.equals(autograph, sysUser.autograph) &&
                Objects.equals(sysRole, sysUser.sysRole);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, userName, password, salt, email, phoneNumber, sex, userPicture, roleId, birthday, autograph, sysRole);
    }

}
