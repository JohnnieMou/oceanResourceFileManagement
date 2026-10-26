package com.example.pojo.dto.user;

import com.example.pojo.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPageDTO extends BaseDTO {

    // 当前页码
    @Schema(example = "1")
    private Integer current = 1;

    // 每页显示条数
    @Schema(example = "10")
    private Integer pageSize = 10;

    // 用户id
    @Schema(example = "1")
    private Long id;

    // 名字
    @Schema(example = "名字")
    private String name;

    // 用户名
    @Schema(example = "用户名")
    private String username;

}
