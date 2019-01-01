
CREATE TABLE `stack` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(1000) NOT NULL,
  `is_enabled` tinyint(1) DEFAULT '1',
  `stack_type` varchar(1000) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `services_in_stacks` (
  `stack_id` int(11) unsigned NULL,
  `service_id` int(11) unsigned NULL,
  CONSTRAINT `services_in_stacks_service_id_fk` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`),
  CONSTRAINT `services_in_stacks_stack_id_fk` FOREIGN KEY (`stack_id`) REFERENCES `stack` (`id`),
  UNIQUE KEY `services_in_stacks_pairs` (`stack_id`, `service_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `environments_in_stacks` (
  `stack_id` int(11) unsigned NULL,
  `environment_id` int(11) unsigned NULL,
  CONSTRAINT `environments_in_stacks_environment_id_fk` FOREIGN KEY (`environment_id`) REFERENCES `environment` (`id`),
  CONSTRAINT `environments_in_stacks_stack_id_fk` FOREIGN KEY (`stack_id`) REFERENCES `stack` (`id`),
  UNIQUE KEY `environments_in_stacks_pairs` (`stack_id`, `environment_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;
