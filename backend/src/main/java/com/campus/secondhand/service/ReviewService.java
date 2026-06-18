package com.campus.secondhand.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.secondhand.entity.Item;
import com.campus.secondhand.entity.Order;
import com.campus.secondhand.entity.Review;
import com.campus.secondhand.mapper.ItemMapper;
import com.campus.secondhand.mapper.OrderMapper;
import com.campus.secondhand.mapper.ReviewMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final ItemMapper itemMapper;
    private final CreditService creditService;

    public ReviewService(ReviewMapper reviewMapper, OrderMapper orderMapper, ItemMapper itemMapper, CreditService creditService) {
        this.reviewMapper = reviewMapper;
        this.orderMapper = orderMapper;
        this.itemMapper = itemMapper;
        this.creditService = creditService;
    }

    @Transactional
    public Review createReview(Long orderId, Long reviewerId, Integer rating, String content) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (order.getStatus() != 2) {
            throw new IllegalArgumentException("订单未完成，无法评价");
        }

        Item item = itemMapper.selectById(order.getItemId());
        if (item == null) {
            throw new IllegalArgumentException("商品不存在");
        }

        Long revieweeId;
        if (reviewerId.equals(order.getBuyerId())) {
            revieweeId = item.getSellerId();
        } else if (reviewerId.equals(item.getSellerId())) {
            revieweeId = order.getBuyerId();
        } else {
            throw new IllegalArgumentException("无权限操作");
        }

        Review existingReview = reviewMapper.selectOne(
            new QueryWrapper<Review>()
                .eq("order_id", orderId)
                .eq("reviewer_id", reviewerId)
        );
        if (existingReview != null) {
            throw new IllegalArgumentException("已评价，请勿重复提交");
        }

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("评分需1-5分");
        }

        Review review = new Review();
        review.setOrderId(orderId);
        review.setReviewerId(reviewerId);
        review.setRevieweeId(revieweeId);
        review.setRating(rating);
        review.setContent(content);
        reviewMapper.insert(review);

        if (rating >= 4) {
            creditService.updateCredit(revieweeId, 3);
        } else if (rating <= 2) {
            creditService.updateCredit(revieweeId, -2);
        }

        return review;
    }

    public List<Review> getReviewsByUser(Long userId) {
        return reviewMapper.selectList(
            new QueryWrapper<Review>()
                .eq("reviewee_id", userId)
                .orderByDesc("created_at")
        );
    }

    public List<Order> getCompletableOrders(Long userId) {
        // 买家侧：我买到的已完成订单
        List<Order> buyerOrders = orderMapper.selectList(
            new QueryWrapper<Order>()
                .eq("buyer_id", userId)
                .eq("status", 2)
                .eq("deleted", 0)
        );

        // 卖家侧：我卖出的已完成订单（通过 items 表关联）
        QueryWrapper<Order> sellerWrapper = new QueryWrapper<>();
        sellerWrapper.eq("status", 2)
                .eq("deleted", 0)
                .exists(
                    "SELECT 1 FROM items WHERE items.id = orders.item_id AND items.seller_id = " + userId
                );
        List<Order> sellerOrders = orderMapper.selectList(sellerWrapper);

        // 合并并去重
        java.util.Set<Long> orderIds = new java.util.HashSet<>();
        java.util.List<Order> allOrders = new java.util.ArrayList<>();
        for (Order order : buyerOrders) {
            if (orderIds.add(order.getId())) {
                allOrders.add(order);
            }
        }
        for (Order order : sellerOrders) {
            if (orderIds.add(order.getId())) {
                allOrders.add(order);
            }
        }

        return allOrders;
    }
}