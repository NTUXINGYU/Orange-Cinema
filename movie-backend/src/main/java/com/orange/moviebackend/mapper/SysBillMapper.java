package com.orange.moviebackend.mapper;

import com.orange.moviebackend.domain.SysBill;
import com.orange.moviebackend.domain.vo.SysBillVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysBillMapper {

    /**
     * 根据条件动态查询订单列表，并使用 LEFT JOIN 关联查询详细信息。
     * 对应 'findByVo' in SysBillMapper.xml
     * @param sysBillVo 包含查询条件的 VO 对象
     * @return 包含完整关联信息的订单列表
     */
    List<SysBill> findByVo(SysBillVo sysBillVo);

    /**
     * 根据ID查询单个订单的完整信息，使用 LEFT JOIN。
     * 对应 'findBillById' in SysBillMapper.xml
     * @param id 订单ID
     * @return 包含完整关联信息的订单对象
     */
    SysBill findBillById(Long id);

    /**
     * 根据ID查询单个订单，并施加数据库行级锁（FOR UPDATE）。
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
     * 更新订单信息 (通常用于支付、取消等状态变更)
     * 对应 'updateBill' in SysBillMapper.xml
     * @param sysBill 要更新的订单对象
     * @return 影响的行数
     */
    int updateBill(SysBill sysBill);

    /**
     * (管理员) 物理删除订单记录。
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

    /**
     * [核心新增]
     * 查询指定场次的指定座位，是否存在于任何“未支付且未取消”的订单中。
     * 这是防止在创建订单时发生逻辑冲突的关键检查。
     * @param sessionId 场次ID
     * @param seats 要检查的座位列表，例如 ["5-7", "5-8"]
     * @return 包含冲突座位的待支付订单列表
     */
    List<SysBill> findUnpaidBillsForSeats(@Param("sessionId") Long sessionId, @Param("seats") List<String> seats);

}