all: connect setting get_ssid current_ssid set_ssid sendAlbumWithList \
	getAlbumListByHashID deleteAlbumByHashId fetchFreeSpace \
	setDSPMode getDSPMode getAlbumListWithUserId

CC= mipsel-linux-gcc

ROOT_WEB=../../../minifs/usr/app/public/cgi-bin
DREAM_DIR=../../../app

CFLAGS=-Wall -O2 -I$(DREAM_DIR)/include -I$(DREAM_DIR)/include/upnp  -fPIC 

LDFLAGS= -L$(DREAM_DIR)/lib -lupnp -lthreadutil -lixml -lasound -lpthread  -lushare -lutil -lcjson

.cpp.o: $@.cpp
	$(CPP) $(CFLAGS) -c $^

.c.o:@.c
	$(CC) $(CFLAGS) -c $^

connect:connect.o
	$(CC) $(LDFLAGS) -o $@ connect.o
	cp connect $(ROOT_WEB)/

setting:setting.o
	$(CC) $(LDFLAGS) -o $@ setting.o
	cp setting $(ROOT_WEB)/
get_ssid:get_ssid.o
	$(CC) $(LDFLAGS) -o $@ get_ssid.o
	cp get_ssid $(ROOT_WEB)/

current_ssid:current_ssid.o
	$(CC) $(LDFLAGS) -o $@ current_ssid.o
	cp current_ssid $(ROOT_WEB)

set_ssid:set_ssid.o
	$(CC) $(LDFLAGS) -o $@ set_ssid.o
	cp set_ssid $(ROOT_WEB)

sendAlbumWithList:sendAlbumWithList.o
	$(CC) $(LDFLAGS) -o $@ sendAlbumWithList.o
	cp sendAlbumWithList $(ROOT_WEB)

getAlbumListByHashID:getAlbumListByHashID.o
	$(CC) $(LDFLAGS) -o $@ getAlbumListByHashID.o
	cp getAlbumListByHashID $(ROOT_WEB)

deleteAlbumByHashId:deleteAlbumByHashId.o
	$(CC) $(LDFLAGS) -o $@ deleteAlbumByHashId.o
	cp deleteAlbumByHashId $(ROOT_WEB)

fetchFreeSpace:fetchFreeSpace.o
	$(CC) $(LDFLAGS) -o $@ fetchFreeSpace.o
	cp fetchFreeSpace $(ROOT_WEB)
setDSPMode:setDSPMode.o
	$(CC) $(LDFLAGS) -o $@ setDSPMode.o
	cp setDSPMode $(ROOT_WEB)
getDSPMode:getDSPMode.o
	$(CC) $(LDFLAGS) -o $@ getDSPMode.o
	cp getDSPMode $(ROOT_WEB)

sendAlbum:sendAlbum.o
	$(CC) $(LDFLAGS) -o $@ sendAlbum.o
	cp sendAlbum $(ROOT_WEB)

getAlbumListWithUserId:getAlbumListWithUserId.o
	$(CC) $(LDFLAGS) -o $@ getAlbumListWithUserId.o
	cp getAlbumListWithUserId $(ROOT_WEB)

clean :
	rm -rf *.o connect setting get_ssid current_ssid set_ssid sendAlbumWithList setDSPMode getDSPMode getAlbumListWithUserId

