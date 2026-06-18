package com.campus.secondhand.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.entity.Item;
import com.campus.secondhand.entity.Order;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.ItemMapper;
import com.campus.secondhand.mapper.OrderMapper;
import com.campus.secondhand.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final OrderMapper orderMapper;

    public UserService(UserMapper userMapper, ItemMapper itemMapper, OrderMapper orderMapper) {
        this.userMapper = userMapper;
        this.itemMapper = itemMapper;
        this.orderMapper = orderMapper;
    }

    public List<User> getAllUsers(int page, int size) {
        Page<User> userPage = userMapper.selectPage(new Page<>(page, size),
            new QueryWrapper<User>().orderByDesc("created_at"));
        return userPage.getRecords();
    }

    public Map<String, Object> getAllUsersWithCount(int page, int size) {
        Page<User> userPage = userMapper.selectPage(new Page<>(page, size),
            new QueryWrapper<User>().orderByDesc("created_at"));
        Map<String, Object> result = new HashMap<>();
        result.put("records", userPage.getRecords());
        result.put("total", userPage.getTotal());
        return result;
    }

    public void banUser(Long userId, Long adminId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setStatus(1);
        userMapper.updateById(user);
    }

    public void unbanUser(Long userId, Long adminId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setStatus(0);
        userMapper.updateById(user);
    }

    public Map<String, Object> getDashboardStats() {
        Long totalUsers = userMapper.selectCount(new QueryWrapper<>());
        Long totalItems = itemMapper.selectCount(new QueryWrapper<Item>().eq("deleted", 0));
        orderMapper.selectCount(new QueryWrapper<Order>().eq("deleted", 0));

        QueryWrapper<User> todayWrapper = new QueryWrapper<>();
        todayWrapper.apply("DATE(created_at) = CURDATE()");
        Long todayNewUsers = userMapper.selectCount(todayWrapper);

        QueryWrapper<Item> todayItemsWrapper = new QueryWrapper<>();
        todayItemsWrapper.apply("DATE(created_at) = CURDATE()").eq("deleted", 0);
        Long todayNewItems = itemMapper.selectCount(todayItemsWrapper);

        QueryWrapper<Order> completedOrdersWrapper = new QueryWrapper<>();
        completedOrdersWrapper.eq("status", 2).eq("deleted", 0);
        List<Order> completedOrders = orderMapper.selectList(completedOrdersWrapper);
        double totalRevenue = completedOrders.stream()
            .mapToDouble(o -> o.getTotalPrice().doubleValue())
            .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("todayNewUsers", todayNewUsers);
        stats.put("totalItems", totalItems);
        stats.put("todayNewItems", todayNewItems);
        stats.put("totalRevenue", totalRevenue);
        stats.put("todayRevenue", 0.0);
        return stats;
    }
}