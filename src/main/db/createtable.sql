CREATE TABLE IF NOT EXISTS `messages` (
    `id` INT UNSIGNED NOT NULL,
    `message` TEXT,
    `state` VARCHAR(20),
    `sender` VARCHAR(50),
    `username` VARCHAR(20) NOT NULL,
    PRIMARY KEY (`id`)
)  ENGINE=INNODB CHARACTER SET UTF8;