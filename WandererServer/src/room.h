/*
 * room.h
 *
 *  Created on: 2022. 8. 15.
 *      Author: s9342931
 */

#ifndef ROOM_H_
#define ROOM_H_

#include <string>
#include <vector>
#include <utility>
#include "player.h"
#include <random>
#include <algorithm>
#include <set>

class ROOM {
public:
	int room_num;
	int room_player = 0;
	int max_player = 5;
	int host_num = 0;
	int status = 0;
	bool rank_game = false;
	bool pw = false;
	std::string room_Name;
	std::string room_PW;

	int ingame_player = 0;
	int submit_hand = 0;
	int turn = 0;
	bool hand_ready = false;
	bool chose_ready = false;
	int before_token = 0;

	std::vector<std::string> v;
	std::vector<std::pair<int, std::string>> hand_v;
	std::vector<std::string> defeat_v;
	std::set<std::string> defeat_s;

	ROOM() {
		room_num = 0;
		room_player = 0;
		max_player = 4;
		host_num = 0;
		room_Name = "";
		room_PW = "";
		pw = false;
		status = 0;
	}

	ROOM(int num, std::string name, int pMNum, int rank) {
		room_num = num;
		room_Name = name;
		host_num = 0;
		max_player = pMNum;
		pw = false;
		status = 1;
		if (rank == 1) {
			rank_game = true;
		} else {
			rank_game = false;
		}

	}
	ROOM(int num, std::string name, std::string pwd, int pMNum, int rank) {
		room_num = num;
		room_Name = name;
		room_PW = pwd;
		host_num = 0;
		max_player = pMNum;
		pw = true;
		status = 1;
		rank_game = true;
		if (rank == 1) {
			rank_game = true;
		} else {
			rank_game = false;
		}
	}

	Json::Value get_Room_Detail();
	Json::Value get_Room_semi_Detail();
	bool enter_room(std::string player_Name);
	bool out_room(std::string player_Name);
	void change_detail(int max, std::string name, std::string pw);
	void change_host(int num);

	bool ready();
	bool start_game();
	void end_game();
	void clear();
	bool defeat(std::string);
	bool hand_submit(std::string name, int num);
	bool check_end();

	std::pair<int, std::pair<int, std::string>> cal();
	Json::Value hand_check();
	Json::Value hand_plus();

	Json::Value set_order();
	Json::Value rating_cal(MYSQL *mysql);
};
Json::Value ROOM::get_Room_semi_Detail() {
	Json::Value ret_Value;
	ret_Value["num"] = room_num;
	ret_Value["cur"] = room_player;
	ret_Value["max"] = max_player;
	ret_Value["name"] = room_Name;
	if (pw) {
		ret_Value["pw"] = "1";
	} else {
		ret_Value["pw"] = "0";
	}
	return ret_Value;
}
Json::Value ROOM::get_Room_Detail() {
	Json::Value ret_Value;
	Json::Value player_Value;

	ret_Value["what"] = "303";
	ret_Value["name"] = room_Name;

	for (int i = 0; i < v.size(); i++) {
		player_Value["name"] = v[i];
		ret_Value["player"].append(player_Value);
	}

	for (int i = v.size(); i < max_player; i++) {
		player_Value["name"] = "";
		ret_Value["player"].append(player_Value);
	}

	return ret_Value;
}
bool ROOM::enter_room(std::string player_Name) {
	bool flag = false;

	if (room_player < max_player) {
		v.push_back(player_Name);
		room_player++;
		flag = true;
	}

	return flag;
}
bool ROOM::out_room(std::string player_Name) {
	bool flag = false;
	for (int i = 0; i < v.size(); i++) {
		if (v[i] == player_Name) {
			v.erase(v.begin() + i);
			room_player--;
			flag = true;
			break;
		}
	}
	if (room_player == 0) {
		status = 0;
	}
	return flag;
}
void ROOM::change_detail(int max, std::string name, std::string pw) {
	this->max_player = max;
	this->room_Name = name;
	this->room_PW = pw;
	if (pw.size() == 0) {
		this->pw = false;
	} else {
		this->pw = true;
	}
}
void ROOM::change_host(int num) {
	std::swap(v[0], v[num]);
}

bool ROOM::ready() {
	this->submit_hand++;
	if (submit_hand == room_player) {
		hand_ready = true;
	}
	return true;
}
bool ROOM::start_game() {
	if (room_player != 1 && this->status == 1) {
		this->status = 2;
		this->turn = 1;
		this->ingame_player = room_player;

		submit_hand = 0;
		before_token = 0;
		hand_ready = false;
		chose_ready = false;

		hand_v.clear();
		std::vector<std::pair<int, std::string>>().swap(hand_v);
		defeat_v.clear();
		std::vector<std::string>().swap(defeat_v);
		defeat_s.clear();
		std::set<std::string>().swap(defeat_s);
		return true;
	}
	return false;
}
void ROOM::end_game() {
	status = 1;
	ingame_player = 0;
	submit_hand = 0;
	turn = 0;
	hand_ready = false;
	chose_ready = false;
	before_token = 0;

	hand_v.clear();
	std::vector<std::pair<int, std::string>>().swap(hand_v);
	defeat_v.clear();
	std::vector<std::string>().swap(defeat_v);
	defeat_s.clear();
	std::set<std::string>().swap(defeat_s);
}
Json::Value ROOM::set_order() {
	Json::Value ret_Value;

	std::random_device rd;
	std::mt19937 gen(rd());
	std::uniform_int_distribution<int> dis(5, 12);

	std::random_device md;
	std::mt19937 men(md());
	std::uniform_int_distribution<int> mis(1, 100);

	int num = 0;
	do {
		num = mis(men);
		if (num < 16) {
			num = 1;
		} else if (num > 15 && num < 56) {
			num = 2;
		} else if (num > 55 && num < 85) {
			num = 3;
		} else {
			num = 4;
		}
	} while (before_token == num && (num == 3 || num == 4));
	before_token = num;
	ret_Value["token"] = std::to_string(num);
	ret_Value["order"] = std::to_string(dis(gen));

	return ret_Value;
}
bool ROOM::hand_submit(std::string name, int num) {
	hand_v.push_back( { num, name });
	if (hand_v.size() == ingame_player) {
		chose_ready = true;
	}
	return true;
}

std::pair<int, std::pair<int, std::string>> ROOM::cal() {
	std::sort(hand_v.rbegin(), hand_v.rend());
	int max = hand_v[0].first;
	bool flag = false;
	int index = 0;
	this->turn++;
	while (hand_v.size() < room_player) {
		hand_v.push_back( { 0, "" });
	}
	for (int i = 1; i < hand_v.size(); i++) {
		if (max == hand_v[i].first) {
			flag = true;
			continue;
		}
		if (flag && max > hand_v[i].first) {
			flag = false;
			max = hand_v[i].first;
			index = i;
		}
	}
	if (flag) {
		return {0, {0,""}};
	}
	std::random_device rd;
	std::mt19937 gen(rd());
	std::uniform_int_distribution<int> dis(0, room_player - 1);
	return {hand_v[index].first, {hand_v[dis(gen)].first, hand_v[index].second}};
}
void ROOM::clear() {
	this->chose_ready = false;
	hand_v.clear();
	std::vector<std::pair<int, std::string>>().swap(hand_v);
}
bool ROOM::defeat(std::string name) {
	if (this->status == 4 || this->status == 1) {
		return false;
	}
	this->ingame_player--;
	auto iter = defeat_s.insert(name);
	if (iter.second) {
		defeat_v.push_back(name);
	}

	if (check_end()) {
		return true;
	} else {
		return false;
	}
}
bool ROOM::check_end() {
	if ((room_player - defeat_v.size()) <= 1) {
		return true;
	} else {
		return false;
	}
}
Json::Value ROOM::rating_cal(MYSQL *mysql) {
	MYSQL_RES *mysql_res = NULL;
	MYSQL_ROW mysqlRow;

	Json::Value ret_Val;

	for (int i = 0; i < v.size(); i++) {
		auto iter = defeat_s.insert(v[i]);
		if (iter.second) {
			defeat_v.push_back(v[i]);
		}
	}

	if (this->rank_game) {
		int d_rating[4] = { -3, 0, 3, 5 };
		for (int i = 0; i < defeat_v.size(); i++) {
			if (mysql_res) {
				mysql_free_result(mysql_res);
				mysql_res = NULL;
			}
			Json::Value player_rating;
			if (update_rating(mysql, defeat_v[i], d_rating[i])) {
				mysql_res = select_rating(mysql, defeat_v[i]);
				if ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
					player_rating["score"] = mysqlRow[2];
					player_rating["name"] = defeat_v[i];
					player_rating["grade"] = std::to_string(
							defeat_v.size() - i);
					ret_Val["player"].append(player_rating);
				}

			}
		}
	} else {
		for (int i = 0; i < defeat_v.size(); i++) {
			if (mysql_res) {
				mysql_free_result(mysql_res);
				mysql_res = NULL;
			}
			Json::Value player_rating;
			mysql_res = select_rating(mysql, defeat_v[i]);
			if ((mysqlRow = mysql_fetch_row(mysql_res)) != NULL) {
				player_rating["score"] = mysqlRow[2];
				player_rating["name"] = defeat_v[i];
				player_rating["grade"] = std::to_string(defeat_v.size() - i);
				ret_Val["player"].append(player_rating);
			}
		}
	}

	return ret_Val;
}
Json::Value ROOM::hand_check() {
	Json::Value ret_Value;

	for (int i = 0; i < hand_v.size(); i++) {
		Json::Value hand_Value;
		hand_Value["num"] = std::to_string(hand_v[i].first);
		hand_Value["name"] = hand_v[i].second;
		ret_Value["player"].append(hand_Value);
	}
	ret_Value["what"] = "707";
	return ret_Value;
}
Json::Value ROOM::hand_plus() {
	Json::Value ret_Value;
	ret_Value["what"] = "711";

	std::random_device rd;
	std::mt19937 gen(rd());
	std::uniform_int_distribution<int> dis(1, 15);

	for (auto iter : v) {
		if (defeat_s.find(iter) == defeat_s.end()) {
			Json::Value card_Value;
			card_Value["name"] = iter;
			card_Value["num"] = std::to_string(dis(gen));
			ret_Value["player"].append(card_Value);
		}
	}
	this->turn = 1;
	return ret_Value;
}
#endif /* ROOM_H_ */
