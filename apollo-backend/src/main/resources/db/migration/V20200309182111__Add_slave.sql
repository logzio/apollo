CREATE TABLE `slave` (
  `slave_id` varchar(100) NOT NULL,
  `environment_id` int(11) unsigned NOT NULL,
  `last_keepalive` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `slave_pair` (`slave_id`, `environment_id`),
  CONSTRAINT `slaves_environment_fk` FOREIGN KEY (`environment_id`) REFERENCES `environment` (`id`),
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
