CREATE TABLE `environments_in_stacks` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  `environment_id` int(11) unsigned NULL,
  CONSTRAINT `stack_environment_fk` FOREIGN KEY (`environment_id`) REFERENCES `environment` (`id`)
)