--create table users
create sequence if not exists seq_users ;

create table if not exists users (
	id BIGINT NOT NULL UNIQUE DEFAULT nextval('seq_users') PRIMARY KEY,
	username varchar(255) NOT NULL UNIQUE,
);

--create table categories
create sequence if not exists seq_mvt_categories;

create table if not exists movement_categories (
	id BIGINT NOT NULL UNIQUE DEFAULT nextval('seq_mvt_categories') PRIMARY KEY,
	user_id BIGINT NOT NULL references users(id),
	name VARCHAR(255) NOT NULL,
	description VARCHAR(1000),
	flag_active BOOLEAN NOT NULL DEFAULT true,
	fallback_category_id BIGINT,
	CONSTRAINT mvt_categ_uid_name_unique UNIQUE (user_id,name)
);

--create table movements
create sequence if not exists seq_movements;

create table if not exists movements (
	id BIGINT NOT NULL UNIQUE DEFAULT nextval('seq_movements') PRIMARY KEY,
	user_id BIGINT NOT NULL references users(id),
	category_id BIGINT NOT NULL references movement_categories(id), 
	value_date DATE NOT NULL,
	name VARCHAR(255) NOT NULL,
	description VARCHAR(1000),
	amount REAL NOT NULL,
        constraint mvts_amount_positive CHECK (amount!=0)
);

--create table targets
create sequence if not exists seq_targets;

create table if not exists targets (
	id BIGINT NOT NULL UNIQUE DEFAULT nextval('seq_targets') PRIMARY KEY,
	user_id BIGINT NOT NULL references users(id), 
	type VARCHAR(255) NOT NULL,
	external_id BIGINT,
	CONSTRAINT targets_uid_name_unique UNIQUE(user_id,type,external_id)
);

--create table target_details
create sequence if not exists seq_target_details;

create table if not exists target_details (
	id BIGINT NOT NULL UNIQUE DEFAULT nextval('seq_target_details') PRIMARY KEY,
	target_id BIGINT NOT NULL references targets(id), 
	value_date DATE NOT NULL,
	amount REAL,
	CONSTRAINT target_dets_id_date_unique UNIQUE(target_id,value_date)
);

create table if not exists users_cache_data (
	id BIGINT NOT NULL UNIQUE PRIMARY KEY REFERENCES users(id),
	min_movement_date DATE,
	max_movement_date DATE,
);

create table if not exists users_auth (
	id BIGINT NOT NULL UNIQUE PRIMARY KEY REFERENCES users(id),
	password varchar(255) NOT NULL ,
	email varchar(255) NOT NULL unique,
	role varchar(255),
	active boolean
);



