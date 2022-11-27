/*
 * mysql_Injection.h
 *
 *  Created on: 2022. 8. 14.
 *      Author: s9342931
 */

#ifndef MYSQL_INJECTION_H_
#define MYSQL_INJECTION_H_

#include<iostream>
#include<mysql/mysql.h>
#include<string>
#include<time.h>

#define MAX_LENGTH 4086

class SQLError {
public:
	std::string Label;

	SQLError() {
		Label = (char*) "Generic Error";
	}
	SQLError(char *msg) {
		Label = msg;
	}
	~SQLError() {
	}
	;
	inline const char* GetMesssage() {
		return Label.c_str();
	}
};

bool reg_Player(MYSQL *mysql, std::string userName, std::string userID) {
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;
	std::string stmt;

	try {
		stmt = "INSERT INTO user_tb(uid,name) VALUES('" + userID + "','"
				+ userName + "');";
		mysql_status = mysql_query(mysql, stmt.c_str());
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
			return true;
		}
	} catch (SQLError e) {
		printf("%s\n", e.Label.c_str());
	}

	return false;
}
bool del_Player(MYSQL *mysql, std::string name) {
	std::string stmt;
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;

	try {
		stmt = "DELETE FROM user_tb where name = '" + name + "';";
		mysql_status = mysql_query(mysql, stmt.c_str());
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
			return true;
		}
	} catch (SQLError e) {
		printf("%s\n", e.Label.c_str());
	}

	return false;
}
bool select_Player(MYSQL *mysql, std::string userID) {
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;
	MYSQL_ROW mysqlRow;
	std::string stmt;

	if (mysql_res) {
		mysql_free_result(mysql_res);
		mysql_res = NULL;
	}

	try {
		stmt = "SELECT * FROM user_tb WHERE uid = '" + userID + "';";
		mysql_status = mysql_query(mysql, stmt.c_str());
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
		}
		if ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
			return true;
		}
	} catch (SQLError e) {
		printf("%s\n", e.Label.c_str());
	}

	return false;
}
MYSQL_RES* select_friends(MYSQL *mysql, std::string name, int count) {
	std::string stmt;
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;

	int end_count = count + 4;

	if (mysql_res) {
		mysql_free_result(mysql_res);
		mysql_res = NULL;
	}

	try {
		stmt =
				"SELECT Cnt, M, S, stat FROM (SELECT row_number() OVER (ORDER BY M DESC) as Cnt, M, S, stat FROM (select main as M, sub as S, stat from friend where main = '"
						+ name
						+ "' union select sub as M, main as S, stat from friend where sub ='"
						+ name + "') tmp_tb) tmp_tb2 WHERE Cnt BETWEEN + "
						+ std::to_string(count) + " AND "
						+ std::to_string(end_count) + " AND stat = 1;";
		mysql_status = mysql_query(mysql, stmt.c_str());
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
		}
		if (mysql_res) {
			return mysql_res;
		} else {
			return mysql_res;
		}
	} catch (SQLError e) {
		printf("%s\n", e.Label.c_str());
	}

	return mysql_res;
}
bool delete_friends(MYSQL *mysql, std::string player_name, std::string name) {
	std::string stmt;
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;

	if (mysql_res) {
		mysql_free_result(mysql_res);
		mysql_res = NULL;
	}

	try {
		stmt = "delete from friend where (main ='" + name + "' and sub = '"
				+ player_name + "') or (main = '" + player_name
				+ "' and sub = '" + name + "');";
		mysql_status = mysql_query(mysql, stmt.c_str());
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
			return true;
		}
	} catch (SQLError e) {
		printf("%s\n", e.Label.c_str());
	}

	return false;
}
MYSQL_RES* get_friend_req(MYSQL *mysql, std::string name, int count) {
	std::string stmt;
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;

	int end_count = count + 4;

	if (mysql_res) {
		mysql_free_result(mysql_res);
		mysql_res = NULL;
	}

	try {
		stmt =
				"select * from ( select row_number() over (order by main desc) cont, main, stat from friend where sub = '"
						+ name + "' and stat = 2 ) tmp where tmp.cont between "
						+ std::to_string(count) + " and "
						+ std::to_string(end_count) + ";";
		mysql_status = mysql_query(mysql, stmt.c_str());
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
		}
		if (mysql_res) {
			return mysql_res;
		} else {
			return mysql_res;
		}
	} catch (SQLError e) {
		printf("%s\n", e.Label.c_str());
	}

	return mysql_res;
}
bool accept_friend_req(MYSQL *mysql, std::string name, std::string req_name) {
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;
	std::string stmt;

	try {
		stmt = "update friend set stat = 1 where main = '" + req_name
				+ "' and sub ='" + name + "';";
		mysql_status = mysql_query(mysql, stmt.c_str());
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
			return true;
		}
	} catch (SQLError e) {
		printf("%s\n", e.Label.c_str());
	}

	return false;
}
int req_friend(MYSQL *mysql, std::string name, std::string req_name) {
	std::string stmt;
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;
	MYSQL_ROW mysqlRow;

	if (mysql_res) {
		mysql_free_result(mysql_res);
		mysql_res = NULL;
	}
	try {
		std::string check = "";
		if (mysql_res) {
			mysql_free_result(mysql_res);
			mysql_res = NULL;
		}
		stmt = "SELECT * FROM friend WHERE (sub = '" + name + "' AND main = '"
				+ req_name + "');";
		mysql_status = mysql_query(mysql, stmt.c_str());
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
		}
		if ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
			check = mysqlRow[2];
			if (check == "1") {
				return 0;
			} else if (check == "2") {
				if (mysql_res) {
					mysql_free_result(mysql_res);
					mysql_res = NULL;
				}
				stmt = "update friend set stat = 1 where (sub= '" + name
						+ "' and main ='" + req_name + "' and stat =2);";
				mysql_status = mysql_query(mysql, stmt.c_str());
				if (mysql_status) {
					throw SQLError((char*) mysql_error(mysql));
				} else {
					mysql_res = mysql_store_result(mysql);
					return 3;
				}
			}
		} else {
			if (mysql_res) {
				mysql_free_result(mysql_res);
				mysql_res = NULL;
			}
			stmt = "insert into friend values('" + name + "','" + req_name
					+ "',2);";
			mysql_status = mysql_query(mysql, stmt.c_str());
			if (mysql_status) {
				throw SQLError((char*) mysql_error(mysql));
			} else {
				mysql_res = mysql_store_result(mysql);
				return 1;
			}
		}
	} catch (SQLError e) {
		printf("%s\n", e.Label.c_str());
	}
	return 2;
}
MYSQL_RES* get_req_friend(MYSQL *mysql, std::string name, int count) {
	std::string stmt;
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;

	int end_count = count + 4;
	if (mysql_res) {
		mysql_free_result(mysql_res);
		mysql_res = NULL;
	}

	try {
		stmt =
				"SELECT * FROM (SELECT ROW_NUMBER() OVER (ORDER BY sub DESC) AS 'cont', sub, stat FROM friend WHERE main = '"
						+ name + "' and stat = 2 ) x where x.cont between "
						+ std::to_string(count) + " AND "
						+ std::to_string(end_count) + ";";
		mysql_status = mysql_query(mysql, stmt.c_str());
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
		}
		if (mysql_res) {
			return mysql_res;
		} else {
			return mysql_res;
		}
	} catch (SQLError e) {
		printf("%s\n", e.Label.c_str());
	}

	return mysql_res;
}
bool deny_friend_request(MYSQL *mysql, std::string name, std::string req_name) {
	std::string stmt;
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;

	if (mysql_res) {
		mysql_free_result(mysql_res);
		mysql_res = NULL;
	}

	try {
		stmt = "delete from friend where main = '" + req_name + "' and sub ='"
				+ name + "';";
		mysql_status = mysql_query(mysql, stmt.c_str());
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
			return true;
		}
	} catch (SQLError e) {
		printf("%s\n", e.Label.c_str());
	}

	return false;
}
bool deny_request(MYSQL *mysql, std::string name, std::string req_name) {
	std::string stmt;
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;

	if (mysql_res) {
		mysql_free_result(mysql_res);
		mysql_res = NULL;
	}

	try {
		stmt = "delete from friend where (sub = '" + req_name + "' and main ='"
				+ name + "' ) or ( main = '" + req_name + "' and sub = '" + name
				+ "');";
		mysql_status = mysql_query(mysql, stmt.c_str());
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
			return true;
		}
	} catch (SQLError e) {
		printf("%s\n", e.Label.c_str());
	}

	return false;
}
int check_friend(MYSQL *mysql, std::string name, std::string req_name) {
	std::string stmt;
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;
	MYSQL_ROW mysqlRow;

	std::string check = "";
	if (mysql_res) {
		mysql_free_result(mysql_res);
		mysql_res = NULL;
	}

	try {
		stmt = "SELECT * FROM friend WHERE (main ='" + name + "' AND sub = '"
				+ req_name + "' ) OR (sub = '" + name + "' AND main = '"
				+ req_name + "' );";
		mysql_status = mysql_query(mysql, stmt.c_str());
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
		}
		if ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
			check = mysqlRow[2];
			if (check == "1") {
				return 1;
			} else if (check == "2") {
				check = mysqlRow[0];
				if(check == name) {
					return 2;
				} else {
					return 0;
				}
			}
		}
	} catch (SQLError e) {
		printf("%s\n", e.Label.c_str());
	}

	return 0;
}

MYSQL_RES* get_myprofile_main(MYSQL *mysql, std::string id) {
	std::string stmt;
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;

	if (mysql_res) {
		mysql_free_result(mysql_res);
		mysql_res = NULL;
	}

	try {
		stmt = "select name, money from user_tb where uid = '" + id + "';";
		mysql_status = mysql_query(mysql, stmt.c_str());
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
		}
		if (mysql_res) {
			return mysql_res;
		} else {
			return mysql_res;
		}
	} catch (SQLError e) {
		printf("%s\n", e.Label.c_str());
	}

	return mysql_res;
}
MYSQL_RES* get_myprofile_main_name(MYSQL *mysql, std::string name) {
	std::string stmt;
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;

	if (mysql_res) {
		mysql_free_result(mysql_res);
		mysql_res = NULL;
	}

	try {
		stmt = "select name, money from user_tb where name= '" + name + "';";
		mysql_status = mysql_query(mysql, stmt.c_str());
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
		}
		if (mysql_res) {
			return mysql_res;
		} else {
			return mysql_res;
		}
	} catch (SQLError e) {
		printf("%s\n", e.Label.c_str());
	}

	return mysql_res;
}
MYSQL_RES* get_myprofile_all(MYSQL *mysql, std::string name) {
	std::string stmt;
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;

	if (mysql_res) {
		mysql_free_result(mysql_res);
		mysql_res = NULL;
	}

	try {
		stmt = "select name, body, money, rating from user_tb where name = '"
				+ name + "';";
		mysql_status = mysql_query(mysql, stmt.c_str());
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
		}
		if (mysql_res) {
			return mysql_res;
		} else {
			return mysql_res;
		}
	} catch (SQLError e) {
		printf("%s\n", e.Label.c_str());
	}

	return mysql_res;
}
bool update_myprofile_body(MYSQL *mysql, std::string name, std::string body) {
	std::string stmt;
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;

	if (mysql_res) {
		mysql_free_result(mysql_res);
		mysql_res = NULL;
	}

	try {
		stmt = "update user_tb set body  = '" + body + "' where name ='" + name
				+ "';";
		mysql_status = mysql_query(mysql, stmt.c_str());
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
			return true;
		}
	} catch (SQLError e) {
		printf("%s\n", e.Label.c_str());
	}

	return false;
}
MYSQL_RES* get_notice_summary(MYSQL *mysql, int num) {
	std::string stmt;
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;

	if (mysql_res) {
		mysql_free_result(mysql_res);
		mysql_res = NULL;
	}
	int end_num = num - 4;
	if (end_num < 1) {
		end_num = 1;
	}

	try {
		stmt = "select n_num, title, c_date from notice where n_num between "
				+ std::to_string(end_num) + " and " + std::to_string(num)
				+ " order by n_num desc;";
		mysql_status = mysql_query(mysql, stmt.c_str());
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
		}
		if (mysql_res) {
			return mysql_res;
		} else {
			return mysql_res;
		}
	} catch (SQLError e) {
		printf("%s\n", e.Label.c_str());
	}

	return mysql_res;
}
MYSQL_RES* get_notice_top_num(MYSQL *mysql) {
	std::string stmt;
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;

	if (mysql_res) {
		mysql_free_result(mysql_res);
		mysql_res = NULL;
	}

	try {
		stmt = "select n_num from notice order by n_num desc limit 1;";
		mysql_status = mysql_query(mysql, stmt.c_str());
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
		}
		if (mysql_res) {
			return mysql_res;
		} else {
			return mysql_res;
		}
	} catch (SQLError e) {
		printf("%s\n", e.Label.c_str());
	}

	return mysql_res;
}
MYSQL_RES* get_notice_body(MYSQL *mysql, std::string num) {
	std::string stmt;
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;

	if (mysql_res) {
		mysql_free_result(mysql_res);
		mysql_res = NULL;
	}

	try {
		stmt = "select * from notice where n_num = '" + num + "';";
		mysql_status = mysql_query(mysql, stmt.c_str());
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
		}
		if (mysql_res) {
			return mysql_res;
		} else {
			return mysql_res;
		}
	} catch (SQLError e) {
		printf("%s\n", e.Label.c_str());
	}

	return mysql_res;
}
bool create_notice(MYSQL *mysql, std::string title, std::string body,
		std::string writer) {
	char sqlStatement[MAX_LENGTH];
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;

	struct tm *t;
	time_t timer = time(NULL);
	t = localtime(&timer);

	if (mysql_res) {
		mysql_free_result(mysql_res);
		mysql_res = NULL;
	}

	memset(&sqlStatement, 0, sizeof(sqlStatement));

	try {
		sprintf(sqlStatement,
				"insert into notice(writer, title, n_body,c_date) values('%s','%s','%s','%04d-%02d-%02d %02d:%02d:%02d')",
				writer.c_str(), title.c_str(), body.c_str(), t->tm_year + 1900,
				t->tm_mon + 1, t->tm_mday, t->tm_hour, t->tm_min, t->tm_sec);
		mysql_status = mysql_query(mysql, sqlStatement);
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
			return true;
		}
	} catch (SQLError e) {
		printf("%s\n", e.Label.c_str());
	}

	return mysql_res;
}
bool update_notice(MYSQL *mysql, std::string title, std::string body, int num) {
	char sqlStatement[MAX_LENGTH];
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;

	if (mysql_res) {
		mysql_free_result(mysql_res);
		mysql_res = NULL;
	}

	memset(&sqlStatement, 0, sizeof(sqlStatement));

	try {
		sprintf(sqlStatement,
				"update notice set n_body = '%s', title = '%s' where num = %d",
				body.c_str(), title.c_str(), num);
		mysql_status = mysql_query(mysql, sqlStatement);
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
			return true;
		}
	} catch (SQLError e) {
		printf("%s\n", e.Label.c_str());
	}

	return false;
}
bool delete_notice(MYSQL *mysql, int num) {
	char sqlStatement[MAX_LENGTH];
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;

	if (mysql_res) {
		mysql_free_result(mysql_res);
		mysql_res = NULL;
	}

	memset(&sqlStatement, 0, sizeof(sqlStatement));

	try {
		sprintf(sqlStatement, "delete from notice where num = %d", num);
		mysql_status = mysql_query(mysql, sqlStatement);
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
			return true;
		}
	} catch (SQLError e) {
		printf("%s\n", e.Label.c_str());
	}

	return false;
}

bool update_rating(MYSQL *mysql, std::string name, int num) {
	std::string stmt;
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;

	if (mysql_res) {
		mysql_free_result(mysql_res);
		mysql_res = NULL;
	}
	try {
		stmt =
				"update user_tb set user_tb.rating = ((select tmp_user_tb.rating from (select rating from user_tb where name ='"
						+ name + "') tmp_user_tb ) + " + std::to_string(num)
						+ ") where name = '" + name + "';";
		mysql_status = mysql_query(mysql, stmt.c_str());
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
			return true;
		}
	} catch (SQLError e) {
		std::cout << e.Label << "\n";
	}
	return false;
}
MYSQL_RES* select_rating(MYSQL *mysql, std::string name) {
	std::string stmt;
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;

	if (mysql_res) {
		mysql_free_result(mysql_res);
		mysql_res = NULL;
	}
	try {
		stmt =
				"SELECT rank_tb.rate_rank, NAME, rating FROM (SELECT NAME, RANK() OVER (ORDER BY rating desc) rate_rank, rating FROM user_tb ) rank_tb WHERE name = '"
						+ name + "';";
		mysql_status = mysql_query(mysql, stmt.c_str());
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
		}
		if (mysql_res) {
			return mysql_res;
		} else {
			return mysql_res;
		}
	} catch (SQLError e) {
		std::cout << e.Label << "\n";
	}
	return mysql_res;
}
MYSQL_RES* select_rating_all(MYSQL *mysql, int count) {
	std::string stmt;
	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;

	int end_count = count + 4;

	if (end_count > 100) {
		count = 96;
		end_count = 100;
	}
	if (mysql_res) {
		mysql_free_result(mysql_res);
		mysql_res = NULL;
	}
	try {
		stmt =
				"SELECT rank_tb.rate_rank, NAME, rating FROM (SELECT NAME, ROW_NUMBER() over (ORDER BY rating desc) rate_rank, rating FROM user_tb ) rank_tb WHERE rate_rank BETWEEN "
						+ std::to_string(count) + " AND "
						+ std::to_string(end_count) + ";";
		mysql_status = mysql_query(mysql, stmt.c_str());
		if (mysql_status) {
			throw SQLError((char*) mysql_error(mysql));
		} else {
			mysql_res = mysql_store_result(mysql);
		}
		if (mysql_res) {
			return mysql_res;
		} else {
			return mysql_res;
		}
	} catch (SQLError e) {
		std::cout << e.Label << "\n";
	}
	return mysql_res;
}

#endif /* MYSQL_INJECTION_H_ */
