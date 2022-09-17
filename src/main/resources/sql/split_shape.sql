/*
 Navicat Premium Data Transfer

 Source Server         : root
 Source Server Type    : MySQL
 Source Server Version : 80020
 Source Host           : localhost:3306
 Source Schema         : gtfs_data

 Target Server Type    : MySQL
 Target Server Version : 80020
 File Encoding         : 65001

 Date: 22/07/2022 22:03:52
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for split_shape
-- ----------------------------
DROP TABLE IF EXISTS `split_shape`;
CREATE TABLE `split_shape`  (
  `route_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `shape_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `split_id` int(0) NOT NULL,
  `pt_id` int(0) NOT NULL,
  `lat` float(255, 6) NOT NULL,
  `lon` float(255, 6) NOT NULL,
  PRIMARY KEY (`shape_id`, `split_id`, `pt_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
