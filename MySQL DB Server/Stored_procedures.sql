SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

CREATE SCHEMA IF NOT EXISTS `csc258` DEFAULT CHARACTER SET latin1 ;
USE `csc258` ;
USE `csc258` ;

-- -----------------------------------------------------
-- procedure assign_test
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `assign_test`(in tid int, in sid int)
BEGIN
	insert into csc258.test_assignment
	(test_id, student_id)	
	values (tid, sid);

END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure delete_question
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `delete_question`(in qid int, out res int)
BEGIN
	declare count int;
	set res = 0;
	select count(question_id) into count from csc258.test_content
	where question_id = qid and test_id in 
	(select test_id from csc258.test where exp_date > now() and active = '1');

	if count = 0 then
		delete from csc258.question where question_id = qid;
		set res = 1;
	end if;
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure insert_ans
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `insert_ans`(in sid int, in tid int, in qid int, in ans varchar(500))
BEGIN
	declare typ varchar(2);
	select type into typ from csc258.question where question_id = qid;
	if typ = 'MC' then
		insert into csc258.user_mc_answer 
		values (sid, tid, qid, ans);
	else 
		insert into csc258.user_sa_answer
		(user_id, test_id, question_id, answer)
		values (sid, tid, qid, ans);
	end if;
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure insert_mc
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `insert_mc`(in qid int, in ch varchar(200),
								in ch_seq varchar(1))
BEGIN
	insert into csc258.mc_q_choice
	(question_id, choice, choice_seq)
	values (qid, ch, ch_seq);
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure insert_q_in_t
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `insert_q_in_t`(IN t_id int, IN q_id int)
BEGIN
	insert into csc258.test_content
	values (t_id, q_id);
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure insert_question
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `insert_question`(in qtext varchar(200),
							in qtype varchar(2),
							in nparts int,
							in sample varchar(500))
BEGIN
	insert into csc258.question
	(question, type, num_parts, sample_answ)
	values (qtext, qtype, nparts, sample);

	select max(question_id) from csc258.question;
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure insert_sa_rubric
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `insert_sa_rubric`(in qid int,
							in rub varchar(200),
							in poor int,
							in avrg int,
							in good int,
							in excel int)
BEGIN
	insert into csc258.sa_q_rubric
	(question_id,  rubric, poorpts, avgpts, goodpts, excelpts)
	values (qid, rub, poor, avrg, good, excel);
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure insert_sa_score
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `insert_sa_score`(in sid int, 
					in tid int, in qid int, in score int)
BEGIN
	update csc258.user_sa_answer
	set points = score where
	user_id = sid and test_id = tid
	and question_id = qid;
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure insert_test
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `insert_test`(IN tname varchar(45), 
						IN ttime int,
						IN numQ int,
						IN aper int,
						IN bper int,
						IN cper int,
						IN dper int,
						in ex_date datetime)
BEGIN
	INSERT into csc258.test 
	(name, time, num_question, a_percent, b_percent, c_percent, d_percent, exp_date)
	values
	(tname, ttime, numQ, aper, bper, cper, dper, ex_date);

	select max(test_id)  
	from csc258.test;
	
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure retrieve_allquestions
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `retrieve_allquestions`()
BEGIN
	select question_id from csc258.question;
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure retrieve_answer_to_saq
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `retrieve_answer_to_saq`(in sid int, 
						in tid int, in qid int)
BEGIN
	select answer from csc258.user_sa_answer 
	where user_id = sid and test_id = tid 
	and question_id = qid;
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure retrieve_correctChoice
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `retrieve_correctChoice`(in qid int)
BEGIN
	select choice_seq from csc258.mc_q_choice
	where question_id = qid and is_answ = '1';
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure retrieve_partial_score
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `retrieve_partial_score`(in sid int,
							in tid int,
							out totalq int,
							out score int)
BEGIN

	select count(question_id) into totalq from csc258.question 
	where type = 'MC' and question_id in (
	select question_id from csc258.test_content where test_id = tid);

	select a.score into score from csc258.test_assignment a 
	where test_id = tid and student_id = sid;
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure retrieve_question
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `retrieve_question`(in qid int)
BEGIN
	select * from csc258.question where question_id = qid;
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure retrieve_questionparts
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `retrieve_questionparts`(in qid int,
							in typ varchar(10))
BEGIN
	if typ = 'MC' then
		select * from csc258.mc_q_choice where question_id = qid;
	elseif typ = 'SA' then
		select * from csc258.sa_q_rubric where question_id = qid;
	end if;
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure retrieve_students
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `retrieve_students`()
BEGIN
	select * from csc258.student;
	
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure retrieve_test
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `retrieve_test`()
BEGIN
	select * from csc258.test
	where exp_date > now();
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure retrieve_test_q_mc
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `retrieve_test_q_mc`(in tid int)
BEGIN
	select question_id from csc258.question
	where type = 'MC' and 
	question_id in (
	select question_id from csc258.test_content 
	where test_id = tid);
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure retrieve_test_q_sa
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `retrieve_test_q_sa`(in tid int)
BEGIN
	select question_id from csc258.question
	where type = 'SA' and 
	question_id in (
	select question_id from csc258.test_content
	where test_id = tid);
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure retrieve_test_saq
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `retrieve_test_saq`(in tid int)
BEGIN
	select question_id from csc258.question 
	where type = 'SA' and question_id in 
	(select question_id from csc258.test_content where test_id = tid);
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure retrieve_test_tobe_graded
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `retrieve_test_tobe_graded`()
BEGIN
	select test_id, name, id, lastname, firstname 
	from csc258.student, csc258.test
	where (id, test_id) in (
	select student_id, test_id 
	from csc258.test_assignment 
	where (completed, graded) = ('1','0'));

END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure retrieve_testassigned
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `retrieve_testassigned`(in sid int)
BEGIN
	select * from csc258.test
	where exp_date > now() and test_id in (
	select test_id from csc258.test_assignment 
	where student_id = sid and completed = '0');

END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure retrieve_testcompleted
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `retrieve_testcompleted`(in sid int)
BEGIN
	select * from csc258.test
	where test_id in (
	select test_id from csc258.test_assignment 
	where student_id = sid and completed = '1');
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure retrieve_testquestion
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `retrieve_testquestion`(in tid int)
BEGIN
	select question_id from csc258.test_content
	where test_id = tid;
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure retrieve_testscores
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `retrieve_testscores`(in sid int)
BEGIN
	select a.name, b.score, a.total_points 
	from csc258.test a, csc258.test_assignment b
	where a.test_id = b.test_id and b.student_id = sid 
	and completed = '1' and graded = '1';
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure retrieve_unassignedstudents
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `retrieve_unassignedstudents`(in tid int)
BEGIN
	select * from csc258.student
	where id not in 
	(select student_id from csc258.test_assignment
	where test_id = tid);
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure set_mc_ch
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `set_mc_ch`(in qid int, in ch varchar(1))
BEGIN
	update csc258.mc_q_choice 
	set is_answ = '1'
	where question_id = qid and choice_seq = ch;
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure set_test_active
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `set_test_active`(in tid int)
BEGIN
	declare mcq_score, sa_score int;
	select count(question_id) into mcq_score from csc258.question 
	where type = 'MC' and question_id in 
	(select question_id from csc258.test_content where test_id = 6);
	
	select sum(excelpts) into sa_score from csc258.sa_q_rubric 
	where question_id in 
	(select question_id from csc258.test_content where test_id = 6);

	update csc258.test set 
	active = '1', total_points = mcq_score + sa_score where 
	test_id = tid;
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure submit_graded_test
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `submit_graded_test`(in sid int, in tid int)
BEGIN
	declare sum int;
	select sum(points) into sum 
	from csc258.user_sa_answer 
	where test_id = tid and user_id = sid;

	update csc258.test_assignment
	set graded = '1', score = score + sum
	where test_id = tid and student_id = sid;

END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure submit_test
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `submit_test`(in sid int, in tid int)
BEGIN
	declare count int;
	select count(a.question_id) into count
	from csc258.user_mc_answer a 
	join csc258.mc_q_choice b 
	on a.question_id = b.question_id 
	where a.answer = b.choice_seq and b.is_answ = '1'
	and a.user_id = sid and a.test_id = tid;

	update csc258.test_assignment set
	completed = '1', submitted = now(),
	score = count
	where test_id = tid and student_id = sid;

END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure update_question
-- -----------------------------------------------------

DELIMITER $$
USE `csc258`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `update_question`(in qid int ,
						in qtext varchar(200),
						in sample varchar(500),
						in nparts int,
						in qtype varchar(2), 
						out res int)
BEGIN
	declare count int;
	set res = 0;
	select count(question_id) into count from csc258.test_content
	where question_id = qid and test_id in 
	(select test_id from csc258.test where exp_date > now() and active = '1');

	if count = 0 then
		if qtype = 'MC' then
			delete from csc258.mc_q_choice where question_id = qid;
		else
			delete from csc258.sa_q_rubric where question_id = qid;
		end if;
		update csc258.question
		set question = qtext,
			num_parts = nparts,
			sample_answ = sample
		where question_id = qid;
		set res = 1;
	end if;
END$$

DELIMITER ;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
