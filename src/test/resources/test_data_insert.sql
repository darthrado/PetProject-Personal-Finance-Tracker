--fill the users
INSERT INTO users (username,password,email) 
VALUES ('testuser1','this_should_not_be_text','bobby_tables@mailg.moc');
INSERT INTO users (username,password,email) 
VALUES ('testuser2','this_should_not_be_text','bobby_tables2@mailg.moc');

--fill the categories
INSERT INTO movement_categories (user_id,name,description)
VALUES ( (SELECT id FROM users WHERE username = 'testuser1'), 'SALARY','All my income be here');
INSERT INTO movement_categories (user_id,name,description)
VALUES ( (SELECT id FROM users WHERE username = 'testuser1'), 'OTHER','For all the orphaned movements');
INSERT INTO movement_categories (user_id,name,description)
VALUES ( (SELECT id FROM users WHERE username = 'testuser1'), 'Games','Money spent on videogames');
INSERT INTO movement_categories (user_id,name,description)
VALUES ( (SELECT id FROM users where username = 'testuser2') , 'SALARY','All my income be here');
INSERT INTO movement_categories (user_id,name,description)
VALUES ( (SELECT id FROM users where username = 'testuser2') , 'OTHER','For all the orphaned movements');
INSERT INTO movement_categories (user_id,name,description)
VALUES ( (SELECT id FROM users where username = 'testuser2') , 'Trips','All the money I spend while on vacation somewhere');

--fill the movements
INSERT INTO movements(user_id,category_id,amount,value_date,name,description)
VALUES (
	(SELECT id FROM users WHERE username = 'testuser1'),
	(SELECT id FROM movement_categories WHERE user_id = (SELECT id FROM users WHERE username = 'testuser1') AND name = 'SALARY'),
	5000,
	to_date('15/06/2020','DD/MM/YYYY'),
	'Salary june',
	'Salary June');
INSERT INTO movements(user_id,category_id,amount,value_date,name,description)
VALUES (
	(SELECT id FROM users WHERE username = 'testuser1'),
	(SELECT id FROM movement_categories WHERE user_id = (SELECT id FROM users WHERE username = 'testuser1') AND name = 'SALARY'),
	5500,
	to_date('14/05/2020','DD/MM/YYYY'),
	'Salary may',
	'Salary may');

INSERT INTO movements(user_id,category_id,amount,value_date,name,description)
VALUES (
	(SELECT id FROM users WHERE username = 'testuser1'),
	(SELECT id FROM movement_categories WHERE user_id = (SELECT id FROM users WHERE username = 'testuser1') AND name = 'OTHER'),
	-57.23,
	to_date('17/05/2020','DD/MM/YYYY'),
	'Lunch: Happy',
	'');
INSERT INTO movements(user_id,category_id,amount,value_date,name,description)
VALUES (
	(SELECT id FROM users WHERE username = 'testuser1'),
	(SELECT id FROM movement_categories WHERE user_id = (SELECT id FROM users WHERE username = 'testuser1') AND name = 'OTHER'),
	-1234.42,
	to_date('22/05/2020','DD/MM/YYYY'),
	'Delivery: PC components',
	'');
INSERT INTO movements(user_id,category_id,amount,value_date,name,description)
VALUES (
	(SELECT id FROM users WHERE username = 'testuser1'),
	(SELECT id FROM movement_categories WHERE user_id = (SELECT id FROM users WHERE username = 'testuser1') AND name = 'Games'),
	-120.00,
	to_date('18/06/2020','DD/MM/YYYY'),
	'Steam Bundle Purchase',
	'"Star Wars: The Fallen Order", "Stellaris:Utopia DLC","The Witcher 3: GOTY edition"');
INSERT INTO movements(user_id,category_id,amount,value_date,name,description)
VALUES (
	(SELECT id FROM users WHERE username = 'testuser1'),
	(SELECT id FROM movement_categories WHERE user_id = (SELECT id FROM users WHERE username = 'testuser1') AND name = 'Games'),
	50.50,
	to_date('19/06/2020','DD/MM/YYYY'),
	'Steam Refund',
	'"Star Wars: The Fallen Order"');


INSERT INTO movements(user_id,category_id,amount,value_date,name,description)
VALUES (
	(SELECT id FROM users WHERE username = 'testuser2'),
	(SELECT id FROM movement_categories WHERE user_id = (SELECT id FROM users where username = 'testuser2')  AND name = 'SALARY'),
	3200,
	to_date('15/06/2020','DD/MM/YYYY'),
	'Salary june',
	'Salary June');

INSERT INTO movements(user_id,category_id,amount,value_date,name,description)
VALUES (
	(SELECT id FROM users WHERE username = 'testuser2'),
	(SELECT id FROM movement_categories WHERE user_id = (SELECT id FROM users where username = 'testuser2')  AND name = 'SALARY'),
	3250,
	to_date('14/05/2020','DD/MM/YYYY'),
	'Salary may',
	'Salary may');
INSERT INTO movements(user_id,category_id,amount,value_date,name,description)
VALUES (
	(SELECT id FROM users WHERE username = 'testuser2'),
	(SELECT id FROM movement_categories WHERE user_id = (SELECT id FROM users where username = 'testuser2')  AND name = 'OTHER'),
	-12.50,
	to_date('13/06/2020','DD/MM/YYYY'),
	'Cinema',
	'Token movie be here');
INSERT INTO movements(user_id,category_id,amount,value_date,name,description)
VALUES (
	(SELECT id FROM users WHERE username = 'testuser2'),
	(SELECT id FROM movement_categories WHERE user_id = (SELECT id FROM users where username = 'testuser2')  AND name = 'OTHER'),
	-68.99,
	to_date('01/05/2020','DD/MM/YYYY'),
	'Disco',
	'Parteeeh');
INSERT INTO movements(user_id,category_id,amount,value_date,name,description)
VALUES (
	(SELECT id FROM users WHERE username = 'testuser2'),
	(SELECT id FROM movement_categories WHERE user_id = (SELECT id FROM users where username = 'testuser2')  AND name = 'Trips'),
	-175.00,
	to_date('03/05/2020','DD/MM/YYYY'),
	'Varna',
	'');
INSERT INTO movements(user_id,category_id,amount,value_date,name,description)
VALUES (
	(SELECT id FROM users WHERE username = 'testuser2'),
	(SELECT id FROM movement_categories WHERE user_id = (SELECT id FROM users where username = 'testuser2')  AND name = 'Trips'),
	-2200,
	to_date('27/06/2020','DD/MM/YYYY'),
	'Italy',
	'');