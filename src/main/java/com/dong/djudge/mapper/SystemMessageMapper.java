package com.dong.djudge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dong.djudge.pojo.SystemMetricsPojo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SystemMessageMapper extends BaseMapper<SystemMetricsPojo> {
    @Select("SELECT * FROM system_metrics WHERE datetime(create_time / 1000, 'unixepoch') < #{dateTime}")
    List<SystemMetricsPojo> selectMetricsBeforeDate(@Param("dateTime") String dateTime);

    @Select("SELECT * FROM system_metrics WHERE datetime(create_time / 1000, 'unixepoch') > #{dateTime} and SandboxSetting_id =#{sid}")
    List<SystemMetricsPojo> selectMetricsAfterDate(@Param("dateTime") String dateTime,@Param("sid") String sid);
}
