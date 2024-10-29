package dev.voroby.springframework.telegram.service.impl;

import dev.voroby.springframework.telegram.dao.ProxyDataDao;
import dev.voroby.springframework.telegram.entity.ProxyData;
import dev.voroby.springframework.telegram.service.ProxyDataService;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * (ProxyData)表服务实现类
 *
 * @author makejava
 * @since 2023-08-27 13:09:57
 */
@Service("proxyDataService")
public class ProxyDataServiceImpl implements ProxyDataService {
    @Resource
    private ProxyDataDao proxyDataDao;

    /**
     * 通过ID查询单条数据
     *
     * @param poxyid 主键
     * @return 实例对象
     */
    @Override
    public ProxyData queryById(Integer poxyid) {
        return this.proxyDataDao.queryById(poxyid);
    }

    /**
     * 分页查询
     *
     * @return 查询结果
     */
    @Override
    public Page<ProxyData> queryByPage() {
        ProxyData proxyData = new ProxyData();
        long total = this.proxyDataDao.count(proxyData);
        return new PageImpl<>(this.proxyDataDao.queryAllByLimit(proxyData, PageRequest.of(0, (int) total)), PageRequest.of(0, (int) total), total);
    }

    /**
     * 新增数据
     *
     * @param proxyData 实例对象
     * @return 实例对象
     */
    @Override
    public ProxyData insert(ProxyData proxyData) {
        this.proxyDataDao.insert(proxyData);
        return proxyData;
    }

    /**
     * 修改数据
     *
     * @param proxyData 实例对象
     * @return 实例对象
     */
    @Override
    public ProxyData update(ProxyData proxyData) {
        this.proxyDataDao.update(proxyData);
        return this.queryById(proxyData.getPoxyid());
    }

    /**
     * 通过主键删除数据
     *
     * @param poxyid 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer poxyid) {
        return this.proxyDataDao.deleteById(poxyid) > 0;
    }

    @Override
    public void deleteALL() {
        this.proxyDataDao.deleteALL();
    }
}
