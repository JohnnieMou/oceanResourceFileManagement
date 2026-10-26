package com.example.pojo.dto.user;

import com.example.pojo.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserDTO extends BaseDTO {

    // 用户id
    @NotNull(message = "用户id不能为空")
    @Schema(example = "1")
    private Long id;

    // 名字
    @Schema(example = "新名字")
    private String name;

    // 用户名
    @Schema(example = "新用户名")
    private String username;

    // 角色编号
    @Schema(example = "2")
    private Long roleCode;

}
