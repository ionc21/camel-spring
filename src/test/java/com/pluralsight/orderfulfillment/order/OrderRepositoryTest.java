package com.pluralsight.orderfulfillment.order;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;

import com.pluralsight.orderfulfillment.test.BaseJpaRepositoryTest;

public class OrderRepositoryTest extends BaseJpaRepositoryTest {

	@Inject
	private OrderRepository orderRepository;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test_findAllOrdersSuccess() throws Exception {
		Iterable<OrderEntity> orders = orderRepository.findAll();
		assertNotNull(orders);
		assertTrue(orders.iterator().hasNext());
	}

	@Test
	public void test_findOrderCustomerAndOrderItemsSuccess() throws Exception {
		Iterable<OrderEntity> orders = orderRepository.findAll();
		assertNotNull(orders);
		Iterator<OrderEntity> iterator = orders.iterator();
		assertTrue(iterator.hasNext());
		OrderEntity order = iterator.next();
		assertNotNull(order.getCustomer());
		Set<OrderItemEntity> orderItems = order.getOrderItems();
		assertNotNull(orderItems);
		assertFalse(orderItems.isEmpty());
	}

	@Test
	@Ignore
	public void test_findOrdersByOrderStatusOrderByTimeOrderPlacedAscSuccess() throws Exception {
		Iterable<OrderEntity> orders = orderRepository.findByStatus(OrderStatus.NEW.getCode(), new PageRequest(0, 5));
		assertNotNull(orders);
		assertTrue(orders.iterator().hasNext());
	}

	@Test
	public void test_findOrdersByOrderStatusOrderByTimeOrderPlacedAscFailInvalidStatus() throws Exception {
		Iterable<OrderEntity> orders = orderRepository.findByStatus("whefiehwi", new PageRequest(0, 5));
		assertNotNull(orders);
		assertFalse(orders.iterator().hasNext());
	}

	@Test
	public void test_updateStatusSuccess() throws Exception {
		List<Long> orderIds = new ArrayList<Long>();
		orderIds.add(1L);
		orderIds.add(2L);
		orderIds.add(3L);
		orderIds.add(4L);
		int count = orderRepository.updateStatus(OrderStatus.PROCESSING.getCode(), new Date(System.currentTimeMillis()), orderIds);
		assertTrue(count == 4);
	}
}
