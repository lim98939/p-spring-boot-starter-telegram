package dev.voroby.springframework.telegram.dao;

import dev.voroby.springframework.telegram.entity.ProxyData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * (ProxyData)表数据库访问层
 *
 * @author makejava
 * @since 2023-08-27 13:09:56
 */
public interface ProxyDataDao {

    /**
     * 通过ID查询单条数据
     *
     * @param poxyid 主键
     * @return 实例对象
     */
    ProxyData queryById(Integer poxyid);

    /**
     * 查询指定行数据
     *
     * @param proxyData 查询条件
     * @param pageable  分页对象
     * @return 对象列表
     */
    List<ProxyData> queryAllByLimit(ProxyData proxyData, @Param("pageable") Pageable pageable);

    /**
     * 统计总行数
     *
     * @param proxyData 查询条件
     * @return 总行数
     */
    long count(ProxyData proxyData);

    /**
     * 新增数据
     *
     * @param proxyData 实例对象
     * @return 影响行数
     */
    int insert(ProxyData proxyData);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<ProxyData> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<ProxyData> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<ProxyData> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<ProxyData> entities);

    /**
     * 修改数据
     *
     * @param proxyData 实例对象
     * @return 影响行数
     */
    int update(ProxyData proxyData);

    /**
     * 通过主键删除数据
     *
     * @param poxyid 主键
     * @return 影响行数
     */
    int deleteById(Integer poxyid);

    /**
     * 删除数据
     *
     * @return 影响行数
     */
    void deleteALL();
}

