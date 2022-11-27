/*
 * player.h
 *
 *  Created on: 2022. 8. 22.
 *      Author: s9342931
 */

#ifndef PLAYER_H_
#define PLAYER_H_

class PLAYER{
public:
	std::string name;
	int cur_Room;

	PLAYER() {
		name = "";
		cur_Room=0;
	}
	PLAYER(std::string name){
		this->name = name;
		this->cur_Room = 0;
	}

};



#endif /* PLAYER_H_ */
