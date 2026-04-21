package com.example.mapper;

import com.example.entity.BiomedSample;
import com.github.pagehelper.PageInfo;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 生物医学样本数据访问接口
 * 对应biomed_sample表，替换原有GoodsMapper
 */
public interface BiomedSampleMapper {
    /**
     * 新增样本
     */
    int insert(BiomedSample biomedSample);

    /**
     * 根据ID删除样本
     */
    int deleteById(Long id);

    /**
     * 根据ID修改样本
     */
    int updateById(BiomedSample biomedSample);

    /**
     * 根据ID查询样本
     */
    BiomedSample selectById(Long id);

    /**
     * 条件查询所有样本
     */
    List<BiomedSample> selectAll(BiomedSample biomedSample);

    // ========== 新增：数据分布分析相关查询方法（适配前端图表） ==========
    /**
     * 1. 队列样本占比分析*/
     List<Map<String, Object>> selectQueueRatio(
     @Param("createBy") Long createBy,
     @Param("queueName") String queueName,
     @Param("sampleTypeId") Long sampleTypeId,
     @Param("startDate") String startDate,
     @Param("endDate") String endDate
     );

    /**
     * 2. 采集时间分布分析（按日统计）*/
     List<Map<String, Object>> selectCollectTimeDist(
     @Param("start") LocalDateTime start,
     @Param("end") LocalDateTime end,
     @Param("createBy") Long createBy,
     @Param("queueName") String queueName,
     @Param("sampleTypeId") Long sampleTypeId
     );

    /**
     * 3. 样本类型数量分析*/
     List<Map<String, Object>> selectSampleTypeCount(
     @Param("createBy") Long createBy,
     @Param("queueName") String queueName,
     @Param("sampleTypeId") Long sampleTypeId,
     @Param("startDate") String startDate,
     @Param("endDate") String endDate
     );

    // ========== 批量导入：批量插入样本 ==========
    void insertBatch(List<BiomedSample> list);

    PageInfo<BiomedSample> selectPage(BiomedSample biomedSample);
}