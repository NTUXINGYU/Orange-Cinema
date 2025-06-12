package com.orange.moviebackend.mapper;

import com.orange.moviebackend.domain.SysBill;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 订单数据访问层接口 (Mapper)
 */
@Mapper
public interface SysBillMapper {

    /**
     * 根据条件动态查询订单列表，并使用 LEFT JOIN 关联查询详细信息。
     * 对应 'findAllBills' in SysBillMapper.xml
     * @param sysBill 包含查询条件的 SysBill 对象
     * @return 包含完整关联信息的订单列表
     */
    List<SysBill> findAllBills(SysBill sysBill);

    /**
     * 根据ID查询单个订单的完整信息，使用 LEFT JOIN。
     * 对应 'findBillById' in SysBillMapper.xml
     * @param id 订单ID
     * @return 包含完整关联信息的订单对象
     */
    SysBill findBillById(Long id);

    /**
     * 【新增】根据ID查询单个订单，并施加数据库行级锁（FOR UPDATE）。
     * 用于支付确认等需要防止并发修改的场景。
     * 对应 'findBillByIdForUpdate' in SysBillMapper.xml
     * @param id 订单ID
     * @return 订单对象
     */
    SysBill findBillByIdForUpdate(Long id);

    /**
     * 插入一条新的订单记录。
     * 对应 'addBill' in SysBillMapper.xml
     * @param sysBill 要插入的订单对象
     * @return 影响的行数
     */
    int addBill(SysBill sysBill);

    /**
     * 【新增】更新订单状态为“已支付”。
     * 对应 'updateBillStatusToPaid' in SysBillMapper.xml
     * @param billId 订单ID
     * @return 影响的行数
     */
    int updateBillStatusToPaid(Long billId);

    /**
     * 【新增】更新订单状态为“已取消”。
     * 对应 'updateBillStatusToCanceled' in SysBillMapper.xml
     * @param billId 订单ID
     * @return 影响的行数
     */
    int updateBillStatusToCanceled(Long billId);

    /**
     * 物理删除一条订单记录（通常仅用于后台管理）。
     * 对应 'deleteBill' in SysBillMapper.xml
     * @param id 订单ID
     * @return 影响的行数
     */
    int deleteBill(Long id);

    /**
     * 查询所有支付超时且未处理的订单。
     * 对应 'findTimeoutBill' in SysBillMapper.xml
     * @return 超时订单列表
     */
    List<SysBill> findTimeoutBill();

}