package lk.ac.kln.unimart_backend.order.repository;

import lk.ac.kln.unimart_backend.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}