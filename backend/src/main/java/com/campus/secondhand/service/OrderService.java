package com.campus.secondhand.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.secondhand.dto.OrderCreateRequest;
import com.campus.secondhand.entity.Item;
import com.campus.secondhand.entity.Order;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.ItemMapper;
import com.campus.secondhand.mapper.OrderMapper;
import com.campus.secondhand.mapper.ReviewMapper;
import com.campus.secondhand.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;
    private final CreditService creditService;
    private final NotificationService notificationService;
    private final ReviewMapper reviewMapper;

    public OrderService(OrderMapper orderMapper, ItemMapper itemMapper, UserMapper userMapper, 
                        CreditService creditService, NotificationService notificationService, ReviewMapper reviewMapper) {
        this.orderMapper = orderMapper;
        this.itemMapper = itemMapper;
        this.userMapper = userMapper;
        this.creditService = creditService;
        this.notificationService = notificationService;
        this.reviewMapper = reviewMapper;
    }

    @Transactional
    public Order createOrder(Long buyerId, OrderCreateRequest request) {
        Item item = itemMapper.selectById(request.getItemId());
        if (item == null || item.getDeleted() == 1) {
            throw new IllegalArgumentException("商品不存在");
        }
        if (item.getStatus() != 0) {
            throw new IllegalArgumentException("商品不在售");
        }
        if (item.getSellerId().equals(buyerId)) {
            throw new IllegalArgumentException("不能购买自己的商品");
        }

        User buyer = userMapper.selectById(buyerId);
        if (buyer == null || buyer.getStatus() == 1) {
            throw new IllegalArgumentException("用户不存在或已被封禁");
        }

        if (request.getTransactionType() == 2 && (request.getDeliveryAddress() == null || request.getDeliveryAddress().isEmpty())) {
            throw new IllegalArgumentException("送货上门需填写地址");
        }

        item.setStatus(1);
        itemMapper.updateById(item);

        Order order = new Order();
        order.setItemId(item.getId());
        order.setBuyerId(buyerId);
        order.setTransactionType(request.getTransactionType());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setTotalPrice(item.getPrice());
        order.setStatus(0);
        orderMapper.insert(order);

        notificationService.createNotification(
            item.getSellerId(),
            "order",
            "新订单提醒",
            "您的商品 '" + item.getTitle() + "' 已被下单，请及时处理",
            order.getId()
        );

        return order;
    }

    public Order getOrderById(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            return null;
        }
        return order;
    }

    public List<Map<String, Object>> getBuyerOrders(Long buyerId) {
        List<Order> orders = orderMapper.selectList(
            new QueryWrapper<Order>()
                .eq("buyer_id", buyerId)
                .eq("deleted", 0)
                .orderByDesc("created_at")
        );
        return enrichOrdersWithReviewStatus(orders, buyerId);
    }

    public List<Map<String, Object>> getSellerOrders(Long sellerId) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.exists(
            "SELECT 1 FROM items WHERE items.id = orders.item_id AND items.seller_id = " + sellerId
        ).eq("deleted", 0).orderByDesc("created_at");
        List<Order> orders = orderMapper.selectList(wrapper);
        return enrichOrdersWithReviewStatus(orders, sellerId);
    }

    private List<Map<String, Object>> enrichOrdersWithReviewStatus(List<Order> orders, Long userId) {
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Order order : orders) {
            Map<String, Object> map = new HashMap<>();
            map.put("order", order);
            int reviewCount = reviewMapper.countByOrderAndReviewer(order.getId(), userId);
            map.put("hasReviewed", reviewCount > 0);
            // 附加商品信息
            Item item = itemMapper.selectById(order.getItemId());
            if (item != null) {
                map.put("itemTitle", item.getTitle());
                map.put("itemPrice", item.getPrice());
                map.put("itemStatus", item.getStatus());
            }
            result.add(map);
        }
        return result;
    }

    @Transactional
    public Order confirmOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new IllegalArgumentException("订单不存在");
        }

        Item item = itemMapper.selectById(order.getItemId());
        if (!item.getSellerId().equals(userId)) {
            throw new IllegalArgumentException("无权限操作");
        }
        if (order.getStatus() != 0) {
            throw new IllegalArgumentException("该状态下无法确认");
        }

        order.setStatus(1);
        orderMapper.updateById(order);

        return order;
    }

    @Transactional
    public Order rejectOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new IllegalArgumentException("订单不存在");
        }

        Item item = itemMapper.selectById(order.getItemId());
        if (!item.getSellerId().equals(userId)) {
            throw new IllegalArgumentException("无权限操作");
        }
        if (order.getStatus() != 0) {
            throw new IllegalArgumentException("该状态下无法拒绝");
        }

        item.setStatus(0);
        itemMapper.updateById(item);

        order.setStatus(3);
        orderMapper.updateById(order);

        creditService.updateCredit(item.getSellerId(), -3);

        return order;
    }

    @Transactional
    public Order cancelOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new IllegalArgumentException("订单不存在");
        }

        if (!order.getBuyerId().equals(userId)) {
            throw new IllegalArgumentException("无权限操作");
        }
        if (order.getStatus() != 0) {
            throw new IllegalArgumentException("该状态下无法取消");
        }

        Item item = itemMapper.selectById(order.getItemId());
        item.setStatus(0);
        itemMapper.updateById(item);

        order.setStatus(3);
        orderMapper.updateById(order);

        creditService.updateCredit(order.getBuyerId(), -3);

        return order;
    }

    @Transactional
    public Order completeOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new IllegalArgumentException("订单不存在");
        }

        if (!order.getBuyerId().equals(userId)) {
            throw new IllegalArgumentException("无权限操作");
        }
        if (order.getStatus() != 1) {
            throw new IllegalArgumentException("该状态下无法确认收货");
        }

        Item item = itemMapper.selectById(order.getItemId());
        item.setStatus(2);
        itemMapper.updateById(item);

        order.setStatus(2);
        orderMapper.updateById(order);

        creditService.updateCredit(item.getSellerId(), 2);
        creditService.updateCredit(order.getBuyerId(), 2);

        notificationService.createNotification(
            item.getSellerId(),
            "review",
            "交易完成提醒",
            "您的商品 '" + item.getTitle() + "' 已完成交易，请评价买家",
            order.getId()
        );

        notificationService.createNotification(
            order.getBuyerId(),
            "review",
            "交易完成提醒",
            "您购买的 '" + item.getTitle() + "' 已完成交易，请对卖家进行评价",
            order.getId()
        );

        return order;
    }
}