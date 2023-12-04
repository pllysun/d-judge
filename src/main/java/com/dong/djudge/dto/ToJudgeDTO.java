package com.dong.djudge.dto;

import com.dong.djudge.util.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;


/**
 * @author ADong
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ToJudgeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 999L;

    /**
     * 判题数据实体类
     */
    private Constants.Judge judge;

    /**
     * 调用评测验证的token
     */
    private String token;

    /**
     * 远程判题不为空，hoj判题为null，例如HDU-1000
     */
    private String remoteJudgeProblem;

    /**
     * 是否为远程判题重判，仅限于已有远程OJ的提交id的重判
     */
    private Boolean isHasSubmitIdRemoteReJudge;

    /**
     * 远程判题所用账号
     */
    private String username;

    /**
     * 远程判题所用密码
     */
    private String password;

    /**
     * 调用判题机的ip
     */
    private String judgeServerIp;

    /**
     * 调用判题机的port
     */
    private Integer judgeServerPort;

    /**
     * VJ判題辅助选择判题机序号使用
     */
    private Integer index;

    private Integer size;

}