SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

CREATE SCHEMA IF NOT EXISTS `csc258` DEFAULT CHARACTER SET latin1 ;
USE `csc258` ;

-- -----------------------------------------------------
-- Table `csc258`.`question`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `csc258`.`question` (
  `question_id` INT(11) NOT NULL AUTO_INCREMENT,
  `type` ENUM('MC','SA') NULL DEFAULT NULL,
  `question` VARCHAR(200) NULL DEFAULT NULL,
  `num_parts` INT(11) NOT NULL,
  `sample_answ` VARCHAR(500) NULL DEFAULT NULL,
  PRIMARY KEY (`question_id`),
  INDEX `question_idx` (`question_id` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 57
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `csc258`.`mc_q_choice`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `csc258`.`mc_q_choice` (
  `choice_id` INT(11) NOT NULL AUTO_INCREMENT,
  `question_id` INT(11) NULL DEFAULT NULL,
  `choice` VARCHAR(200) NULL DEFAULT NULL,
  `choice_seq` VARCHAR(1) NULL DEFAULT NULL,
  `is_answ` ENUM('0','1') NULL DEFAULT '0',
  PRIMARY KEY (`choice_id`),
  INDEX `MC_Q_CHOICE_IDX` (`question_id` ASC, `choice_seq` ASC),
  CONSTRAINT `FK_mc_q_choice`
    FOREIGN KEY (`question_id`)
    REFERENCES `csc258`.`question` (`question_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 103
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `csc258`.`sa_q_rubric`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `csc258`.`sa_q_rubric` (
  `rubric_id` INT(11) NOT NULL AUTO_INCREMENT,
  `question_id` INT(11) NULL DEFAULT NULL,
  `rubric` VARCHAR(200) NULL DEFAULT NULL,
  `poorpts` INT(11) NULL DEFAULT NULL,
  `avgpts` INT(11) NULL DEFAULT NULL,
  `goodpts` INT(11) NULL DEFAULT NULL,
  `excelpts` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`rubric_id`),
  INDEX `sa_q_rubric_idx` (`question_id` ASC),
  CONSTRAINT `sa_q_rubric_fk`
    FOREIGN KEY (`question_id`)
    REFERENCES `csc258`.`question` (`question_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 24
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `csc258`.`student`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `csc258`.`student` (
  `id` INT(11) NOT NULL,
  `lastname` VARCHAR(45) NULL DEFAULT NULL,
  `firstname` VARCHAR(45) NULL DEFAULT NULL,
  `username` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `STUDENT_IDX` (`id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `csc258`.`test`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `csc258`.`test` (
  `test_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NULL DEFAULT NULL,
  `time` INT(11) NOT NULL,
  `num_question` INT(11) NULL DEFAULT NULL,
  `total_points` INT(11) NULL DEFAULT '0',
  `a_percent` INT(11) NULL DEFAULT NULL,
  `b_percent` INT(11) NULL DEFAULT NULL,
  `c_percent` INT(11) NULL DEFAULT NULL,
  `d_percent` INT(11) NULL DEFAULT NULL,
  `exp_date` DATETIME NULL DEFAULT NULL,
  `active` ENUM('0','1') NULL DEFAULT '0',
  PRIMARY KEY (`test_id`),
  INDEX `test_idx` (`test_id` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 12
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `csc258`.`test_assignment`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `csc258`.`test_assignment` (
  `test_id` INT(11) NOT NULL,
  `student_id` INT(11) NOT NULL,
  `completed` ENUM('0','1') NULL DEFAULT '0',
  `graded` ENUM('0','1') NULL DEFAULT '0',
  `submitted` DATETIME NULL DEFAULT NULL,
  `score` INT(11) NULL DEFAULT '0',
  PRIMARY KEY (`test_id`, `student_id`),
  INDEX `test_assignment_fk` (`student_id` ASC),
  CONSTRAINT `test_assignment_fk`
    FOREIGN KEY (`student_id`)
    REFERENCES `csc258`.`student` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `test_assignment_ibfk_1`
    FOREIGN KEY (`test_id`)
    REFERENCES `csc258`.`test` (`test_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `csc258`.`test_content`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `csc258`.`test_content` (
  `test_id` INT(11) NOT NULL,
  `question_id` INT(11) NOT NULL,
  PRIMARY KEY (`test_id`, `question_id`),
  INDEX `test_content_fk` (`question_id` ASC),
  CONSTRAINT `test_content_fk`
    FOREIGN KEY (`question_id`)
    REFERENCES `csc258`.`question` (`question_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `test_content_fk_1`
    FOREIGN KEY (`test_id`)
    REFERENCES `csc258`.`test` (`test_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `csc258`.`user_mc_answer`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `csc258`.`user_mc_answer` (
  `user_id` INT(11) NOT NULL,
  `test_id` INT(11) NOT NULL,
  `question_id` INT(11) NOT NULL,
  `answer` VARCHAR(1) NULL DEFAULT NULL,
  PRIMARY KEY (`test_id`, `user_id`, `question_id`),
  INDEX `user_mc_answer_idx` (`test_id` ASC, `user_id` ASC, `question_id` ASC),
  INDEX `user_mc_asnwer_fk1_idx` (`user_id` ASC),
  INDEX `user_mc_asnwer_fk2_idx` (`question_id` ASC),
  CONSTRAINT `user_mc_asnwer_fk`
    FOREIGN KEY (`test_id`)
    REFERENCES `csc258`.`test` (`test_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `user_mc_asnwer_fk1`
    FOREIGN KEY (`user_id`)
    REFERENCES `csc258`.`student` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `user_mc_asnwer_fk2`
    FOREIGN KEY (`question_id`)
    REFERENCES `csc258`.`question` (`question_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `csc258`.`user_sa_answer`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `csc258`.`user_sa_answer` (
  `user_id` INT(11) NOT NULL,
  `test_id` INT(11) NOT NULL,
  `question_id` INT(11) NOT NULL,
  `answer` VARCHAR(500) NULL DEFAULT NULL,
  `points` INT(11) NULL DEFAULT '0',
  PRIMARY KEY (`user_id`, `test_id`, `question_id`),
  INDEX `user_sq_answer_idx` (`user_id` ASC, `test_id` ASC, `question_id` ASC),
  INDEX `user_sa_asnwer_fk1_idx` (`test_id` ASC),
  INDEX `user_sa_asnwer_fk2_idx` (`question_id` ASC),
  CONSTRAINT `user_sa_asnwer_fk`
    FOREIGN KEY (`user_id`)
    REFERENCES `csc258`.`student` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `user_sa_asnwer_fk1`
    FOREIGN KEY (`test_id`)
    REFERENCES `csc258`.`test` (`test_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `user_sa_asnwer_fk2`
    FOREIGN KEY (`question_id`)
    REFERENCES `csc258`.`question` (`question_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
