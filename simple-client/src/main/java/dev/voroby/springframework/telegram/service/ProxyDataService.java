package dev.voroby.springframework.telegram.service;

import dev.voroby.springframework.telegram.entity.ProxyData;
import org.springframework.data.domain.Page;

/**
 * (ProxyData)表服务接口
 *
 * @author makejava
 * @since 2023-08-27 13:09:57
 */
public interface ProxyDataService {

    /**
     * 通过ID查询单条数据
     *
     * @param poxyid 主键
     * @return 实例对象
     */
    ProxyData queryById(Integer poxyid);

    /**
     * 分页查询
     *
     * @return 查询结果
     */
    Page<ProxyData> queryByPage();

    /**
     * 新增数据
     *
     * @param proxyData 实例对象
     * @return 实例对象
     */
    ProxyData insert(ProxyData proxyData);

    /**
     * 修改数据
     *
     * @param proxyData 实例对象
     * @return 实例对象
     */
    ProxyData update(ProxyData proxyData);

    /**
     * 通过主键删除数据
     *
     * @param poxyid 主键
     * @return 是否成功
     */
    boolean deleteById(Integer poxyid);

    /**
     * 删除数据
     *
     * @return 影响行数
     */
    void deleteALL();
}
