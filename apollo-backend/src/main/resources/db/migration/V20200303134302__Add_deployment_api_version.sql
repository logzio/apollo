CREATE TABLE `deployment_api_version` (
  `service_id` int(11) unsigned NULL,
  `environment_id` int(11) NULL,
  `api_version` varchar(1000) NOT NULL,
  CONSTRAINT `service_deployment_api_version_fk` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`),
  UNIQUE KEY `service_environment_api_version_pairs` (`service_id`, `environment_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;