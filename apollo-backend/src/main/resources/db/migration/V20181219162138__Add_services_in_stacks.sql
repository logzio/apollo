CREATE TABLE `services_in_stacks` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  `service_id` int(11) unsigned NULL,
  CONSTRAINT `stack_service_fk` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`)
)