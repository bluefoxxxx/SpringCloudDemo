USE springcloud_demo;

-- Clean up old data (if it exists) to ensure a fresh start for each initialization
DROP TABLE IF EXISTS `t_order_item`;
DROP TABLE IF EXISTS `t_order`;
DROP TABLE IF EXISTS `t_product`;
DROP TABLE IF EXISTS `t_user`;


-- Create user table (t_user)
CREATE TABLE `t_user` (
                          `id` bigint NOT NULL AUTO_INCREMENT,
                          `username` varchar(255) DEFAULT NULL,
                          `level` varchar(255) DEFAULT NULL,
                          PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

-- Insert user test data
INSERT INTO `t_user` (id, username, level) VALUES (1, 'Alice', 'VIP1');
INSERT INTO `t_user` (id, username, level) VALUES (2, 'Bob', 'VIP2');
INSERT INTO `t_user` (id, username, level) VALUES (3, 'Charlie', 'VIP3');
INSERT INTO `t_user` (id, username, level) VALUES (4, 'David', 'NormalUser');


-- Create product table (t_product)
CREATE TABLE `t_product` (
                             `id` bigint NOT NULL AUTO_INCREMENT,
                             `name` varchar(255) DEFAULT NULL,
                             `price` decimal(10,2) DEFAULT NULL,
                             `stock` int DEFAULT NULL,
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

-- Insert product test data
INSERT INTO `t_product` (id, name, price, stock) VALUES (1, 'High-Performance Laptop', 1299.99, 100);
INSERT INTO `t_product` (id, name, price, stock) VALUES (2, 'Mechanical Keyboard', 79.99, 200);
INSERT INTO `t_product` (id, name, price, stock) VALUES (3, '4K Monitor', 299.00, 150);
INSERT INTO `t_product` (id, name, price, stock) VALUES (4, 'Noise-Cancelling Headphones', 149.50, 300);
INSERT INTO `t_product` (id, name, price, stock) VALUES (5, 'Smart Band', 49.99, 500);


-- Create order table (t_order)
CREATE TABLE `t_order` (
                           `id` bigint NOT NULL AUTO_INCREMENT,
                           `order_no` varchar(255) DEFAULT NULL,
                           `user_id` bigint DEFAULT NULL,
                           `create_time` datetime DEFAULT NULL,
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

-- Insert order test data
INSERT INTO `t_order` (id, order_no, user_id, create_time) VALUES (1, 'ORDER_SN_001', 1, NOW());
INSERT INTO `t_order` (id, order_no, user_id, create_time) VALUES (2, 'ORDER_SN_002', 2, NOW());
INSERT INTO `t_order` (id, order_no, user_id, create_time) VALUES (3, 'ORDER_SN_003', 1, NOW()); -- Alice's second order


-- Create order item table (t_order_item)
CREATE TABLE `t_order_item` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `order_id` bigint DEFAULT NULL,
                                `product_id` bigint DEFAULT NULL,
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

-- Insert order item test data

-- Order 1 (Alice bought a Laptop and a Keyboard)
INSERT INTO `t_order_item` (id, order_id, product_id) VALUES (1, 1, 1);
INSERT INTO `t_order_item` (id, order_id, product_id) VALUES (2, 1, 2);

-- Order 2 (Bob bought a Monitor, Headphones, and a Smart Band)
INSERT INTO `t_order_item` (id, order_id, product_id) VALUES (3, 2, 3);
INSERT INTO `t_order_item` (id, order_id, product_id) VALUES (4, 2, 4);
INSERT INTO `t_order_item` (id, order_id, product_id) VALUES (5, 2, 5);

-- Order 3 (Alice bought another pair of Headphones)
INSERT INTO `t_order_item` (id, order_id, product_id) VALUES (6, 3, 4);