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

 Date: 22/07/2022 22:04:11
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for stop_times
-- ----------------------------
DROP TABLE IF EXISTS `stop_times`;
CREATE TABLE `stop_times`  (
  `trip_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `arrival_time` time(6) NULL DEFAULT NULL,
  `departure_time` time(6) NULL DEFAULT NULL,
  `stop_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `stop_sequence` int(0) NOT NULL,
  `pickup_type` int(0) NULL DEFAULT NULL,
  `drop_off_type` int(0) NULL DEFAULT NULL,
  PRIMARY KEY (`trip_id`, `stop_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
