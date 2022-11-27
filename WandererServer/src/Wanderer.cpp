//============================================================================
// Name        : Wanderer.cpp
// Author      : 
// Version     :
// Copyright   : Your copyright notice
// Description : Hello World in C++, Ansi-style
//============================================================================

#include <iostream>
#include<unistd.h>
#include<string.h>
#include<arpa/inet.h>
#include<sys/socket.h>
#include<netinet/in.h>
#include<pthread.h>
#include<time.h>
#include<mysql/mysql.h>
#include<jsoncpp/json/json.h>
#include<vector>
#include<random>

#include"mysql_Injection.h"
#include"room.h"
#include"player.h"

#define JSON_IS_AMALGAMATION

#define BUF_SIZE 1024
#define MAX_ROOM 41

unsigned int mysql_timeout = 86400;

MYSQL *mysql;
MYSQL *MYSQL_Connection = NULL;

char error_sock[] = "socket() error";
char error_bind[] = "bind() error";
char error_listen[] = "listen() error";
char error_accept[] = "accept() error";
char check_message[] = "connect";
char game_start[] = "game start!!";
char unknown_Error[] = "Unknown Error Code!!";
char error_room[] = "cannot access to room";
char reg_succes[] = "true";

void error_handling(char *msg);
void* handle_clnt(void *arg);
void error_Mysql(char *msg);
void send_Json(Json::Value root, int player_sock);
void send_to_Room(Json::Value root, int room_num);

int clnt_cnt;
int clnt_socks[2000];

pthread_mutex_t mutx;
ROOM room[MAX_ROOM];
std::vector<int> room_sock[MAX_ROOM];

int main(int argc, char *argv[]) {
	int serv_sock, clnt_sock;
	struct sockaddr_in serv_adr, clnt_addr;
	socklen_t clnt_addr_sz;
	pthread_t t_id;

	struct tm *t;
	time_t timer = time(NULL);
	t = localtime(&timer);

	if(argc != 6 )
	{
		std::cout<<"Invalid argc :" << argc<<"\n";
		return 0;
	}

	std::string hostName = argv[2];
	std::string userId = argv[3];
	std::string password = argv[4];
	std::string DB = argv[5];

	int mysql_status = 0;
	MYSQL_RES *mysql_res = NULL;

	MYSQL_ROW mysqlRow;
	MYSQL_FIELD *mysqlFields;
	my_ulonglong numRows;
	unsigned int numFields;
	try {
		MYSQL_Connection = mysql_init(NULL);

		mysql_options(MYSQL_Connection, MYSQL_OPT_CONNECT_TIMEOUT,
				(const void*) &mysql_timeout);
		mysql = mysql_real_connect(MYSQL_Connection, hostName.c_str(),
				userId.c_str(), password.c_str(), DB.c_str(), 0, NULL, 0);

		if (mysql == NULL) {
			error_Mysql((char*) mysql_error(MYSQL_Connection));
			mysql_close(MYSQL_Connection);
			exit(1);
		}
		std::cout << "MYSQL Connection info "
				<< mysql_get_host_info(MYSQL_Connection) << "\n";
		std::cout << "MYSQL Client info " << mysql_get_client_info() << "\n";
		std::cout << "MYSQL Server info "
				<< mysql_get_server_info(MYSQL_Connection) << "\n";
		mysql_ping(MYSQL_Connection);
	} catch (SQLError e) {
		printf("%s\n", e.Label.c_str());
		exit(1);
	}

	std::cout << "Start Create ROOM\n";
	for (int i = 0; i < MAX_ROOM; i++) {
		room[i] = ROOM();
	}
	std::cout << "Successfully Create ROOM\nstarting server : in " << argv[1]
			<< " port\n";

	pthread_mutex_init(&mutx, NULL);
	serv_sock = socket(PF_INET, SOCK_STREAM, 0);
	if (serv_sock == -1) {
		error_handling(error_sock);
	}
	int serv_sock_opt = 1;
	setsockopt(serv_sock, SOL_SOCKET, SO_REUSEADDR, &serv_sock_opt,
			sizeof(serv_sock_opt));

	std::cout << "Successfully create server socket\n";

	memset(&serv_adr, 0, sizeof(serv_adr));
	serv_adr.sin_family = AF_INET;
	serv_adr.sin_addr.s_addr = htonl(INADDR_ANY);
	serv_adr.sin_port = htons(atoi(argv[1]));

	if (bind(serv_sock, (struct sockaddr*) &serv_adr, sizeof(serv_adr)) < 0) {
		error_handling(error_bind);
		exit(1);
	}
	std::cout << "Successful binding\nWaiting client\n";

	if (listen(serv_sock, 5) == -1) {
		error_handling(error_listen);
	}

	clnt_addr_sz = sizeof(clnt_addr);
	while (1) {
		t = localtime(&timer);
		clnt_sock = accept(serv_sock, (struct sockaddr*) &clnt_addr,
				&clnt_addr_sz);
		if (clnt_sock == -1) {
			error_handling(error_accept);
		}

		pthread_mutex_lock(&mutx);
		clnt_socks[clnt_cnt++] = clnt_sock;
		pthread_mutex_unlock(&mutx);

		pthread_create(&t_id, NULL, handle_clnt, (void*) &clnt_sock);

		pthread_detach(t_id);
		std::cout << " Connected client IP : " << inet_ntoa(clnt_addr.sin_addr)
				<< " ";
		std::cout << t->tm_year + 1900 << "-" << t->tm_mon + 1 << "-"
				<< t->tm_mday << " " << t->tm_hour << ":" << t->tm_min << "\n";
		std::cout << " chatter (" << clnt_cnt << "/100)\n";
	}
	close(serv_sock);
	return 0;
}

void* handle_clnt(void *arg) {
	int clnt_sock = *((int*) arg);
	int str_len = 0;

	char msg[BUF_SIZE];
	memset(&msg, 0, sizeof(msg));

	char *ptr;
	int count = 0;
	int cur_Room = 0;
	int ranked = 0;

	int ranking_page_view = 1;
	int notice_page_view = 0;
	int friend_page_view = 1;

	Json::Value string_Value;
	Json::Reader string_Reader;

	MYSQL_RES *mysql_res = NULL;
	MYSQL_ROW mysqlRow;

	PLAYER player;

	while ((str_len = read(clnt_sock, msg, sizeof(msg))) != 0) {
		std::cout << "send::" << player.name << " " << msg << "\n";
		string_Reader.parse(msg, string_Value);

		Json::Value ret_Value;
		bool Check;

		if (mysql_res) {
			mysql_free_result(mysql_res);
			mysql_res = NULL;
		}

		if (string_Value["what"].asString() == "101") {
			mysql_res = get_myprofile_main(MYSQL_Connection,
					string_Value["id"].asString());
			if ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
				ret_Value["what"] = "101";
			} else {
				Check = reg_Player(MYSQL_Connection,
						string_Value["name"].asString(),
						string_Value["id"].asString());
				if (Check) {
					mysql_res = get_myprofile_main(MYSQL_Connection,
							string_Value["id"].asString());
					if ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
						ret_Value["what"] = "103";
						ret_Value["isUser"] = "1";
						ret_Value["name"] = mysqlRow[0];
						ret_Value["money"] = mysqlRow[1];
						player = PLAYER(mysqlRow[0]);
					}
				} else {
					ret_Value["what"] = "101";
				}
			}
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "102") {
			Check = del_Player(MYSQL_Connection, player.name);
			ret_Value["what"] = "102";
			if (Check) {
				ret_Value["isValidate"] = "1";
				player.name = "";
			} else {
				ret_Value["isValidate"] = "0";
			}
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "103") {
			ret_Value["what"] = "103";
			Check = select_Player(MYSQL_Connection,
					string_Value["id"].asString());
			if (Check) {
				mysql_res = get_myprofile_main(MYSQL_Connection,
						string_Value["id"].asString());
				if ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
					ret_Value["isUser"] = "1";
					ret_Value["name"] = mysqlRow[0];
					ret_Value["money"] = mysqlRow[1];
					player = PLAYER(mysqlRow[0]);
				} else {
					ret_Value["isUser"] = "0";
				}
			} else {
				ret_Value["isUser"] = "0";
			}
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "104") {
			ret_Value["what"] = "104";
			player.name = "";
			player.cur_Room = 0;
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "201") { // 내 정보의 이름, 메모, 재화, 레이팅 점수 조회
			ret_Value["what"] = "201";
			mysql_res = get_myprofile_all(MYSQL_Connection, player.name);
			if ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
				ret_Value["rating"] = mysqlRow[3];
				ret_Value["isValidate"] = "1";
				ret_Value["body"] = mysqlRow[1];
				ret_Value["name"] = player.name;
			} else {
				ret_Value["isValidate"] = "0";
			}
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "202") {
			ret_Value["what"] = "201";
			if (update_myprofile_body(MYSQL_Connection, player.name,
					string_Value["body"].asString())) {
				ret_Value["what"] = "202";
			} else {
				mysql_res = get_myprofile_all(MYSQL_Connection, player.name);
				if ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
					ret_Value["rating"] = mysqlRow[3];
					ret_Value["isValidate"] = "0";
					ret_Value["body"] = string_Value["body"].asString();
					ret_Value["name"] = player.name;
				} else {
					ret_Value["isValidate"] = "0";
				}
			}
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "203") {
			ret_Value["what"] = "103";
			ranking_page_view = 1;
			friend_page_view = 1;
			notice_page_view = 0;
			mysql_res = get_myprofile_main_name(MYSQL_Connection,
					string_Value["name"].asString());
			if ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
				ret_Value["isUser"] = "1";
				ret_Value["name"] = mysqlRow[0];
				ret_Value["money"] = mysqlRow[1];
				player = PLAYER(mysqlRow[0]);
			} else {
				ret_Value["isUser"] = "0";
			}
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "210") { // 초기 랭킹 조회
			ret_Value["what"] = "210";
			ranking_page_view = 1;
			mysql_res = select_rating_all(MYSQL_Connection, ranking_page_view);
			while ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
				Json::Value other_ranking;
				other_ranking["rank"] = mysqlRow[0];
				other_ranking["name"] = mysqlRow[1];
				other_ranking["rating"] = mysqlRow[2];
				ret_Value["ranking"].append(other_ranking);
			}
			mysql_res = select_rating(MYSQL_Connection, player.name);
			while ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
				Json::Value my_ranking;
				my_ranking["rank"] = mysqlRow[0];
				my_ranking["name"] = mysqlRow[1];
				my_ranking["rating"] = mysqlRow[2];
				ret_Value["info"].append(my_ranking);
			}
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "211") { // 이후 랭킹 조회
			ranking_page_view += 5;
			ret_Value["what"] = "211";
			mysql_res = select_rating_all(MYSQL_Connection, ranking_page_view);
			while ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
				Json::Value other_ranking;
				other_ranking["rank"] = mysqlRow[0];
				other_ranking["name"] = mysqlRow[1];
				other_ranking["rating"] = mysqlRow[2];
				ret_Value["ranking"].append(other_ranking);
			}
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "301") { // 룸 정보 조회
			ret_Value["what"] = "301";
			ranked = stoi(string_Value["rank"].asString());
			pthread_mutex_lock(&mutx);
			if (ranked == 1) {
				for (int i = 0; i < MAX_ROOM; i++) {
					if (room[i].status == 1 && room[i].rank_game == true) {
						ret_Value["detail"].append(
								room[i].get_Room_semi_Detail());
					}
				}
			} else if (ranked == 0) {
				for (int i = 0; i < MAX_ROOM; i++) {
					if (room[i].status == 1 && room[i].rank_game == false) {
						ret_Value["detail"].append(
								room[i].get_Room_semi_Detail());
					}
				}
			}

			pthread_mutex_unlock(&mutx);
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "302") { // 룸 생성 후 입장
			if (cur_Room == 0) {
				try {
					for (int i = 1; i < MAX_ROOM; i++) {
						if (room[i].status == 0) {
							if (string_Value["roomPW"].asString().size() != 0) {
								room[i] = ROOM(i,
										string_Value["name"].asString(),
										string_Value["roomPW"].asString(),
										stoi(string_Value["max"].asString()),
										ranked);
							} else {
								room[i] = ROOM(i,
										string_Value["name"].asString(),
										stoi(string_Value["max"].asString()),
										ranked);
							}
							if (room[i].enter_room(player.name)) {
								room_sock[i].push_back(clnt_sock);
								cur_Room = i;
								ret_Value = room[i].get_Room_Detail();
								ret_Value["isValidate"] = "1";
								send_to_Room(ret_Value, cur_Room);
							}
							break;
						}
					}
				} catch (std::exception e) {
					std::cout << e.what() << "\n";
					ret_Value["what"] = "302";
					send_Json(ret_Value, clnt_sock);
				}
			} else {
				int max = stoi(string_Value["max"].asString());
				std::string room_name = string_Value["name"].asString();
				std::string pw = string_Value["roomPW"].asString();
				room[cur_Room].change_detail(max, room_name, pw);
				ret_Value = room[cur_Room].get_Room_Detail();
				ret_Value["isValidate"] = "1";
				send_to_Room(ret_Value, cur_Room);
			}
		} else if (string_Value["what"].asString() == "303") { // 룸 입장
			int target_room = stoi(string_Value["num"].asString());
			if (room[target_room].status == 1) {
				if (room[target_room].enter_room(player.name)) {
					ret_Value = room[target_room].get_Room_Detail();
					room_sock[target_room].push_back(clnt_sock);
					cur_Room = target_room;
					ret_Value["isValidate"] = "1";
					send_to_Room(ret_Value, cur_Room);
				} else {
					ret_Value["isValidate"] = "0";
					send_Json(ret_Value, clnt_sock);
				}
			}
		} else if (string_Value["what"].asString() == "304") { // 룸 퇴장
			for (int i = 0; i < room_sock[cur_Room].size(); i++) {
				if (room_sock[cur_Room][i] == clnt_sock) {
					room_sock[cur_Room].erase(room_sock[cur_Room].begin() + i);
					break;
				}
			}
			room[cur_Room].out_room(player.name);
			if (room[cur_Room].room_player != 0) {
				ret_Value = room[cur_Room].get_Room_Detail();
				ret_Value["isValidate"] = "1";
				send_to_Room(ret_Value, cur_Room);
			} else {
				room[cur_Room].room_num = 0;
			}
			cur_Room = 0;
			Json::Value ret_Value2;
			ret_Value2["what"] = "304";
			send_Json(ret_Value2, clnt_sock);
		} else if (string_Value["what"].asString() == "305") {
			ret_Value = room[cur_Room].get_Room_Detail();
			ret_Value["isValidate"] = "1";
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "306") { // 호스트 위임
			room[cur_Room].change_host(string_Value["num"].asInt());
			ret_Value = room[cur_Room].get_Room_Detail();
			ret_Value["isValidate"] = "1";
			send_to_Room(ret_Value, cur_Room);
		} else if (string_Value["what"].asString() == "309") {
			ret_Value["what"] = "309";
			int target_room = stoi(string_Value["num"].asString());
			ret_Value["pw"] = room[target_room].room_PW;
			ret_Value["num"] = string_Value["num"].asString();
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "310") {
			ret_Value["what"] = "310";
			int target_player = stoi(string_Value["num"].asString());
			ret_Value["player"] = room[cur_Room].v[target_player];
			send_to_Room(ret_Value, cur_Room);
		} else if (string_Value["what"].asString() == "311") {
			ret_Value["what"] = "311";
			ret_Value["name"] = string_Value["name"].asString();
			mysql_res = get_myprofile_all(MYSQL_Connection,
					string_Value["name"].asString());
			if ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
				ret_Value["body"] = mysqlRow[1];
				ret_Value["rating"] = mysqlRow[3];
			}
			int fc = check_friend(MYSQL_Connection, player.name,
					string_Value["name"].asString());
			if (fc == 1) {
				ret_Value["isFriend"] = "1";
			} else if (fc == 2) {
				ret_Value["isFriend"] = "2";
			} else {
				ret_Value["isFriend"] = "0";
			}
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "312") {
			ret_Value["what"] = "312";
			ret_Value["name"] = string_Value["name"];
			send_to_Room(ret_Value, cur_Room);
		} else if (string_Value["what"].asString() == "501") {
			Json::Value notice_Value;
			ret_Value["what"] = "501";
			ret_Value["isValidate"] = "0";
			mysql_res = get_notice_top_num(MYSQL_Connection);
			if ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
				std::string tmp_string = mysqlRow[0];
				notice_page_view = std::stoi(tmp_string);
			}
			mysql_res = get_notice_summary(MYSQL_Connection, notice_page_view);
			while ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
				notice_Value["num"] = mysqlRow[0];
				notice_Value["title"] = mysqlRow[1];
				notice_Value["date"] = mysqlRow[2];
				ret_Value["notice"].append(notice_Value);
				ret_Value["isValidate"] = "1";
			}
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "502") {
			ret_Value["what"] = "502";
			ret_Value["isValidate"] = "0";
			mysql_res = get_notice_body(MYSQL_Connection,
					string_Value["num"].asString());
			if ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
				ret_Value["title"] = mysqlRow[2];
				ret_Value["date"] = mysqlRow[4];
				ret_Value["writer"] = mysqlRow[1];
				ret_Value["body"] = mysqlRow[3];
				ret_Value["isValidate"] = "1";
			}
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "503") {
			Json::Value notice_Value;
			ret_Value["what"] = "501";
			ret_Value["isValidate"] = "0";
			Check = create_notice(MYSQL_Connection,
					string_Value["title"].asString(),
					string_Value["body"].asString(),
					string_Value["name"].asString());
			if (Check) {
				ret_Value["isValidate"] = "1";
				mysql_res = get_notice_summary(MYSQL_Connection,
						notice_page_view);
				while ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
					notice_Value["num"] = mysqlRow[0];
					notice_Value["title"] = mysqlRow[1];
					notice_Value["date"] = mysqlRow[2];
					ret_Value["notice"].append(notice_Value);
				}
			}
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "504") {
			ret_Value["what"] = "502";
			ret_Value["isValidate"] = "0";
			Check = update_notice(MYSQL_Connection,
					string_Value["title"].asString(),
					string_Value["body"].asString(),
					string_Value["num"].asInt());
			if (Check) {
				mysql_res = get_notice_body(MYSQL_Connection,
						string_Value["num"].asString());
				if ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
					ret_Value["num"] = mysqlRow[0];
					ret_Value["writer"] = mysqlRow[1];
					ret_Value["title"] = mysqlRow[2];
					ret_Value["body"] = mysqlRow[3];
					ret_Value["date"] = mysqlRow[4];
					ret_Value["isValidate"] = "1";
				}
			}
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "505") {
			Json::Value notice_Value;
			ret_Value["what"] = "505";
			ret_Value["isValidate"] = "0";
			Check = delete_notice(MYSQL_Connection,
					string_Value["num"].asInt());
			if (Check) {
				mysql_res = get_notice_summary(MYSQL_Connection,
						notice_page_view);
				while ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
					notice_Value["num"] = mysqlRow[0];
					notice_Value["title"] = mysqlRow[1];
					notice_Value["date"] = mysqlRow[2];
					ret_Value["notice"].append(notice_Value);
					ret_Value["isValidate"] = "1";
				}
			}
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "506") {
			Json::Value notice_Value;
			ret_Value["what"] = "506";
			if (notice_page_view < 1 || notice_page_view - 5 < 1) {
				continue;
			}
			notice_page_view -= 5;
			if (notice_page_view < 1) {
				notice_page_view = 1;
			}
			mysql_res = get_notice_summary(MYSQL_Connection, notice_page_view);
			while ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
				notice_Value["num"] = mysqlRow[0];
				notice_Value["title"] = mysqlRow[1];
				notice_Value["date"] = mysqlRow[2];
				ret_Value["notice"].append(notice_Value);
				ret_Value["isValidate"] = "1";
			}
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "507") {
			ret_Value["what"] = "507";
			notice_page_view = 0;
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "601") {
			ret_Value["what"] = "601";
			ret_Value["name"] = player.name;
			ret_Value["chat"] = string_Value["chat"].asString();
			send_to_Room(ret_Value, cur_Room);
		} else if (string_Value["what"].asString() == "701") {
			if (room[cur_Room].start_game()) {
				for (int i = 5; i > 0; i--) {
					Json::Value start_Value;
					start_Value["what"] = "601";
					start_Value["name"] = "System";
					start_Value["chat"] = std::to_string(i);
					send_to_Room(start_Value, cur_Room);
					sleep(1);
				}
				ret_Value["what"] = "701";
				send_to_Room(ret_Value, cur_Room);
			}
		} else if (string_Value["what"].asString() == "702") {
			ret_Value["what"] = "702";
			if (room[cur_Room].ready()) {
				ret_Value["isValidate"] = "1";
			} else {
				ret_Value["isValidate"] = "0";
			}
			send_Json(ret_Value, clnt_sock);
			if (room[cur_Room].hand_ready) {
				ret_Value = room[cur_Room].set_order();
				ret_Value["what"] = "703";
				send_to_Room(ret_Value, cur_Room);
			}
		} else if (string_Value["what"].asString() == "703") {
			ret_Value = room[cur_Room].set_order();
			ret_Value["what"] = "703";
			send_to_Room(ret_Value, cur_Room);
		} else if (string_Value["what"].asString() == "704") {
			int submit_num = std::stoi(string_Value["num"].asString());
			if (room[cur_Room].hand_submit(player.name, submit_num)) {
				Json::Value ret_Value2;
				ret_Value["what"] = "704";
				ret_Value["isValidate"] = "1";
				ret_Value["num"] = std::to_string(submit_num);
				send_Json(ret_Value, clnt_sock);
				sleep(0.5);

				ret_Value2["what"] = "706";
				ret_Value2["name"] = player.name;
				send_to_Room(ret_Value2, cur_Room);
			}
			if (room[cur_Room].chose_ready && room[cur_Room].status == 2) {
				room[cur_Room].status = 3;
				Json::Value ret_Value3;
				Json::Value ret_Value4;
				Json::Value round_Value = room[cur_Room].hand_check();
				sleep(2);
				send_to_Room(round_Value, cur_Room);

				std::pair<int, std::pair<int, std::string>> winner =
						room[cur_Room].cal();
				ret_Value3["what"] = "705";
				ret_Value3["win_num"] = winner.first;
				ret_Value3["num"] = winner.second.first;
				ret_Value3["winner"] = winner.second.second;
				room[cur_Room].clear();
				sleep(2);
				send_to_Room(ret_Value3, cur_Room);

				if (room[cur_Room].turn % 4 == 0) {
					Json::Value plus_Value;
					plus_Value = room[cur_Room].hand_plus();
					sleep(2);
					send_to_Room(plus_Value, cur_Room);
				}

				ret_Value4 = room[cur_Room].set_order();
				ret_Value4["what"] = "703";
				sleep(3);
				send_to_Room(ret_Value4, cur_Room);
				room[cur_Room].status = 2;
			}
		} else if (string_Value["what"].asString() == "708") {
			ret_Value["what"] = "708";
			ret_Value["name"] = player.name;
			send_to_Room(ret_Value, cur_Room);
			if (room[cur_Room].defeat(player.name)
					&& room[cur_Room].status != 4) {
				room[cur_Room].status = 4;
				ret_Value = room[cur_Room].rating_cal(MYSQL_Connection);
				ret_Value["what"] = "709";
				sleep(2);
				send_to_Room(ret_Value, cur_Room);
				room[cur_Room].end_game();
			}
		} else if (string_Value["what"].asString() == "710") {
			ret_Value["what"] = "710";
			send_Json(ret_Value, clnt_sock);
			bool check = room[cur_Room].defeat(player.name);
			if (check) {
				ret_Value["what"] = "708";
				ret_Value["name"] = player.name;
				send_to_Room(ret_Value, cur_Room);
			}

			if (room[cur_Room].hand_v.size() == room[cur_Room].ingame_player
					&& room[cur_Room].status == 2) {
				room[cur_Room].status = 3;
				Json::Value ret_Value3;
				Json::Value ret_Value4;
				Json::Value round_Value = room[cur_Room].hand_check();
				sleep(2);
				send_to_Room(round_Value, cur_Room);

				std::pair<int, std::pair<int, std::string>> winner =
						room[cur_Room].cal();
				ret_Value3["what"] = "705";
				ret_Value3["win_num"] = winner.first;
				ret_Value3["num"] = winner.second.first;
				ret_Value3["winner"] = winner.second.second;
				room[cur_Room].clear();
				sleep(2);
				send_to_Room(ret_Value3, cur_Room);

				if (room[cur_Room].turn % 3 == 0) {
					Json::Value plus_Value;
					plus_Value = room[cur_Room].hand_plus();
					sleep(2);
					send_to_Room(plus_Value, cur_Room);
				}

				ret_Value4 = room[cur_Room].set_order();
				ret_Value4["what"] = "703";
				sleep(3);
				send_to_Room(ret_Value4, cur_Room);
				room[cur_Room].status = 2;
			} else if (check && room[cur_Room].status != 4) {
				room[cur_Room].status = 4;
				ret_Value = room[cur_Room].rating_cal(MYSQL_Connection);
				ret_Value["what"] = "709";
				sleep(2);
				send_to_Room(ret_Value, cur_Room);
				room[cur_Room].end_game();
			}
		} else if (string_Value["what"].asString() == "801") { // search friend
			friend_page_view = 1;
			ret_Value["what"] = "801";
			mysql_res = select_friends(MYSQL_Connection, player.name,
					friend_page_view);
			while ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
				Json::Value friend_Value;
				friend_Value["name"] = mysqlRow[2];
				ret_Value["friend"].append(friend_Value);
			}
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "802") { // del friend
			ret_Value["what"] = "311";
			ret_Value["name"] = string_Value["name"].asString();
			mysql_res = get_myprofile_all(MYSQL_Connection,
					string_Value["name"].asString());
			if ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
				ret_Value["body"] = mysqlRow[1];
				ret_Value["rating"] = mysqlRow[3];
			}
			if (delete_friends(MYSQL_Connection, player.name,
					string_Value["name"].asString())) {
				ret_Value["isValidate"] = "1";
				int fc = check_friend(MYSQL_Connection, player.name,
						string_Value["name"].asString());
				if (fc == 1) {
					ret_Value["isFriend"] = "1";
				} else if (fc == 2) {
					ret_Value["isFriend"] = "2";
				} else {
					ret_Value["isFriend"] = "0";
				}
				send_Json(ret_Value, clnt_sock);

				sleep(1);
				Json::Value ret_Value2;
				friend_page_view = 1;
				ret_Value2["what"] = "801";
				mysql_res = select_friends(MYSQL_Connection, player.name,
						friend_page_view);
				while ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
					Json::Value friend_Value;
					friend_Value["name"] = mysqlRow[2];
					ret_Value2["friend"].append(friend_Value);
				}
				send_Json(ret_Value2, clnt_sock);
			} else {
				ret_Value["isValidate"] = "0";
				int fc = check_friend(MYSQL_Connection, player.name,
						string_Value["name"].asString());
				if (fc == 1) {
					ret_Value["isFriend"] = "1";
				} else if (fc == 2) {
					ret_Value["isFriend"] = "2";
				} else {
					ret_Value["isFriend"] = "0";
				}
				send_Json(ret_Value, clnt_sock);
			}
		} else if (string_Value["what"].asString() == "803") { // search friend request
			friend_page_view = 1;
			ret_Value["what"] = "803";
			mysql_res = get_friend_req(MYSQL_Connection, player.name,
					friend_page_view);
			while ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
				Json::Value friend_Value;
				friend_Value["name"] = mysqlRow[1];
				ret_Value["request"].append(friend_Value);
			}
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "804") { // accept friend request
			ret_Value["what"] = "804";
			if (accept_friend_req(MYSQL_Connection, player.name,
					string_Value["name"].asString())) {
				ret_Value["isValidate"] = "1";
			} else {
				ret_Value["isValidate"] = "0";
			}
			send_Json(ret_Value, clnt_sock);

			sleep(1);
			friend_page_view = 1;
			Json::Value ret_Value2;
			ret_Value2["what"] = "803";
			mysql_res = get_friend_req(MYSQL_Connection, player.name,
					friend_page_view);
			while ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
				Json::Value friend_Value;
				friend_Value["name"] = mysqlRow[1];
				ret_Value2["request"].append(friend_Value);
			}
			send_Json(ret_Value2, clnt_sock);
		} else if (string_Value["what"].asString() == "805") { // deny friend request
			ret_Value["what"] = "805";
			if (deny_friend_request(MYSQL_Connection, player.name,
					string_Value["name"].asString())) {
				ret_Value["isValidate"] = "1";
			} else {
				ret_Value["isValidate"] = "0";
			}
			send_Json(ret_Value, clnt_sock);

			sleep(1);
			friend_page_view = 1;
			Json::Value ret_Value2;
			ret_Value2["what"] = "803";
			mysql_res = get_friend_req(MYSQL_Connection, player.name,
					friend_page_view);
			while ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
				Json::Value friend_Value;
				friend_Value["name"] = mysqlRow[1];
				ret_Value2["request"].append(friend_Value);
			}
			send_Json(ret_Value2, clnt_sock);
		} else if (string_Value["what"].asString() == "806") { // search request
			friend_page_view = 1;
			ret_Value["what"] = "806";
			mysql_res = get_req_friend(MYSQL_Connection, player.name,
					friend_page_view);
			while ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
				Json::Value friend_Value;
				friend_Value["name"] = mysqlRow[1];
				ret_Value["request"].append(friend_Value);
			}
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "807") { // add request
			ret_Value["what"] = "807";
			int fc = check_friend(MYSQL_Connection, player.name,
					string_Value["name"].asString());
			if (fc == 0) {
				int check = req_friend(MYSQL_Connection, player.name,
						string_Value["name"].asString());
				if (check == 1) {
					ret_Value["isValidate"] = "1";
				} else if (check == 2) {
					ret_Value["isValidate"] = "2";
				} else if (check == 3) {
					ret_Value["isValidate"] = "3";
				} else {
					ret_Value["isValidate"] = "0";
				}
			} else {
				ret_Value["isValidate"] = "1";
			}

			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "808") { // deny request
			ret_Value["what"] = "808";
			if (deny_request(MYSQL_Connection, player.name,
					string_Value["name"].asString())) {
				ret_Value["isValidate"] = "1";
			} else {
				ret_Value["isValidate"] = "0";
			}
			send_Json(ret_Value, clnt_sock);

			sleep(1);
			friend_page_view = 1;
			Json::Value ret_Value2;
			ret_Value2["what"] = "806";
			mysql_res = get_req_friend(MYSQL_Connection, player.name,
					friend_page_view);
			while ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
				Json::Value friend_Value;
				friend_Value["name"] = mysqlRow[1];
				ret_Value2["request"].append(friend_Value);
			}
			send_Json(ret_Value2, clnt_sock);
		} else if (string_Value["what"].asString() == "809") {
			friend_page_view += 5;
			ret_Value["what"] = "809";
			mysql_res = select_friends(MYSQL_Connection, player.name,
					friend_page_view);
			while ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
				Json::Value friend_Value;
				friend_Value["name"] = mysqlRow[2];
				ret_Value["name"].append(friend_Value);
			}
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "810") {
			friend_page_view += 5;
			ret_Value["what"] = "810";
			mysql_res = get_friend_req(MYSQL_Connection, player.name,
					friend_page_view);
			while ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
				Json::Value friend_Value;
				friend_Value["name"] = mysqlRow[1];
				ret_Value["name"].append(friend_Value);
			}
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "811") {
			friend_page_view += 5;
			ret_Value["what"] = "811";
			mysql_res = get_req_friend(MYSQL_Connection, player.name,
					friend_page_view);
			while ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
				Json::Value friend_Value;
				friend_Value["name"] = mysqlRow[1];
				ret_Value["name"].append(friend_Value);
			}
			send_Json(ret_Value, clnt_sock);
		} else if (string_Value["what"].asString() == "812") {
			ret_Value["what"] = "311";
			mysql_res = get_myprofile_all(MYSQL_Connection,
					string_Value["name"].asString());
			if ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
				ret_Value["body"] = mysqlRow[1];
				ret_Value["rating"] = mysqlRow[3];
				ret_Value["name"] = mysqlRow[0];
				int fc = check_friend(MYSQL_Connection, player.name,
						string_Value["name"].asString());
				if (fc == 1) {
					ret_Value["isFriend"] = "1";
				} else if (fc == 2) {
					ret_Value["isFriend"] = "2";
				} else {
					ret_Value["isFriend"] = "0";
				}
			} else {
				ret_Value["name"] = "";
				ret_Value["body"] = "";
				ret_Value["rating"] = "";
				ret_Value["isFriend"] = "0";
				ret_Value["isValidate"] = "0";
			}
			send_Json(ret_Value, clnt_sock);
		}
		memset(&msg, 0, sizeof(msg));
	}

	if (cur_Room != 0) {
		Json::Value ret_Value;
		if (room[cur_Room].status == 2) {
			if (room[cur_Room].defeat(player.name)) {
				ret_Value = room[cur_Room].rating_cal(MYSQL_Connection);
				ret_Value["what"] = "709";
				send_to_Room(ret_Value, cur_Room);
			}
		}
		for (int i = 0; i < room_sock[cur_Room].size(); i++) {
			if (room_sock[cur_Room][i] == clnt_sock) {
				room_sock[cur_Room].erase(room_sock[cur_Room].begin() + i);
				break;
			}
		}
		room[cur_Room].out_room(player.name);
		ret_Value = room[cur_Room].get_Room_Detail();
		ret_Value["isValidate"] = "1";
		if (room[cur_Room].room_player != 0) {
			send_to_Room(ret_Value, cur_Room);
		}
	}
	close(clnt_sock);
	std::cout << " player out (" << player.name << ")\n";
	return NULL;
}
void send_Json(Json::Value root, int player_sock) {
	std::string jsonadpter;
	jsonadpter.reserve(BUF_SIZE);
	Json::StyledWriter writer;
	jsonadpter = writer.write(root);
	std::cout << "indi Json send what = " << root["what"] << "\n";

	pthread_mutex_lock(&mutx);
	write(player_sock, jsonadpter.c_str(), jsonadpter.length());
	pthread_mutex_unlock(&mutx);
}
void send_to_Room(Json::Value root, int room_num) {
	std::string jsonadpter;
	jsonadpter.reserve(BUF_SIZE);
	Json::StyledWriter writer;
	jsonadpter = writer.write(root);
	std::cout << "Room Json send what = " << root["what"] << " room_num ="
			<< room_num << "\n";

	pthread_mutex_lock(&mutx);
	for (auto iter : room_sock[room_num]) {
		write(iter, jsonadpter.c_str(), jsonadpter.length());
	}
	pthread_mutex_unlock(&mutx);
}

void error_handling(char *msg) {
	fputs(msg, stderr);
	fputc('\n', stderr);
	mysql_close(MYSQL_Connection);
}
void error_Mysql(char *msg) {
	fputs(msg, stderr);
	fputc('\n', stderr);
}
