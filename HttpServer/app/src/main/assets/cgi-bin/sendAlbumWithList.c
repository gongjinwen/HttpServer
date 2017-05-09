#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <sys/time.h>
#include <sys/types.h>
#include <dirent.h>

#include "album_mgt.h"
#include "down_mgt.h"
#include "mtdutil.h"
#include "checkservice.h"
#include "cJSON.h"

static void write_to_file(char *content_buf, char *hash_id)
{

    FILE *write_file = NULL;
    int rank_num = 0;
    int value = 0;

    char file[2048];
    struct timeval tv;
    memset(&tv, 0, sizeof(tv));
    gettimeofday(&tv, NULL);
    srand(tv.tv_usec);
    rank_num = rand();
    value = rank_num % 10000;


    sprintf(file, "/tmp/tmp_album_list_hashid_%d", value);
    fprintf(stderr, "=== write_to_file file:%s\n", file);

    if (0 == check_file_is_exsit(file))
    {
        fprintf(stderr, "=== file exsit. file:%s\n", file);
        printf("{\n");
        printf("\"message\" : \"failed\",\n");
        printf("\"responseCode\" : \"300\"\n");
        printf("}\n");

        return;
    }

    write_file = fopen(file, "wb");
    if (NULL == write_file)
    {
        printf("{\n");
        printf("\"message\" : \"failed\",\n");
        printf("\"responseCode\" : \"300\"\n");
        printf("}\n");

        return;
    }

    fprintf(write_file, "%s", content_buf);
    fclose(write_file);



    char cmd[4096];
    memset(cmd, 0, sizeof(cmd));
    sprintf(cmd, "mv %s %s/%s_%s", file, ALBUM_LIST_DIR, hash_id, ALBUM_LIST_SUFFIX);
    system(cmd);
    system("sync");

    fprintf(stderr, "cmd is %s\n", cmd);

}

void echo_success_resp()
{
    printf("{\n");
    printf("\"message\" : \"ok\",\n");
    printf("\"responseCode\" : \"200\"\n");
    printf("}\n");

    return;
}

int main(void)
{
    char *data;
    int content_len = 0;
    int i = 0;

    check_and_create_dir(ALBUM_LIST_DIR);

    if (NULL == opendir(ALBUM_LIST_DIR))
    {
        fprintf(stderr, "There is now dir. dir:%s\n", ALBUM_LIST_DIR);
        printf("{\n");
        printf("\"message\" : \"no album dir\",\n");
        printf("\"responseCode\" : \"400\"\n");
        printf("}\n");

        return;
    }

    album_mgt_init();
    down_mgt_init();

    data = getenv("CONTENT_LENGTH");
    if (NULL == data)
    {
        return -1;
    }
    content_len = atoi(data);

    char content_buf[40960];
    memset(content_buf, 0, sizeof(content_buf));
    int read_len = fread(&content_buf, sizeof(char), content_len, stdin);
    if (read_len != content_len)
    {
        return -2;
    }

    //fprintf(stderr, "==buf:%s\n", content_buf);
    cJSON *root = cJSON_Parse(content_buf);
    if (NULL == root)
    {
        return -3;
    }

    album_info_t album_info;
    memset(&album_info, 0, sizeof(album_info));

    cJSON *albumName = cJSON_GetObjectItem(root, "albumName");
    cJSON *albumID = cJSON_GetObjectItem(root, "albumID");
    cJSON *audiosNum = cJSON_GetObjectItem(root, "audiosNum");
    cJSON *userId = cJSON_GetObjectItem(root, "userId");
    cJSON *udId = cJSON_GetObjectItem(root, "udId");
    cJSON *hashID = cJSON_GetObjectItem(root, "hashID");
    cJSON *array = cJSON_GetObjectItem(root, "audios");

    if (NULL == albumName || NULL == albumID || NULL == audiosNum ||
            NULL == hashID || NULL == array) 
    {
        fprintf(stderr, "==== cJSON is null, albumName:%p albumID:%p, audiosNum:%p, hashID:%p array:%p\n",
                albumName, albumID, audiosNum, hashID, array);
        return -4;
    }

    memcpy(album_info.album_hashid, hashID->valuestring, strlen(hashID->valuestring));

    int count = cJSON_GetArraySize(array);

    album_info.album_url_num = count;


    for (i = 0; i < count; ++i)
    {
        cJSON *item = cJSON_GetArrayItem(array, i);
        if (NULL == item)
        {
            continue;
        }

        cJSON *audioID = cJSON_GetObjectItem(item, "audioID");
        cJSON *audioTitle = cJSON_GetObjectItem(item, "audioTitle");
        cJSON *url = cJSON_GetObjectItem(item, "url");
        cJSON *state = cJSON_GetObjectItem(item, "state");
        if (NULL == audioID || NULL == audioTitle || NULL == url || NULL == state)
        {
            fprintf(stderr, "==== array is null\n");
            return -5;
        }

        char song_id[64];
        memset(song_id, 0, sizeof(song_id));
        album_get_songid_by_url(url->valuestring, song_id);
        if (!strlen(song_id))
        {
            album_info.album_url_num--;
            continue;
        }

        memcpy(album_info.url_list[i].song_id, song_id, strlen(song_id));

        song_node_t song_node;
        memset(&song_node, 0, sizeof(song_node_t));
        if (0 != down_song_find(song_id, &song_node))
        {
            fprintf(stderr, "== down_song_insert song_id:%s\n", song_id);
            memcpy(song_node.song_id, song_id, strlen(song_id));
            memcpy(song_node.song_url, url->valuestring, strlen(url->valuestring));
            song_node.song_status = SONG_STATUS_IDEL;
            down_song_insert(song_id, &song_node);
        }
    }


    write_to_file(content_buf, hashID->valuestring);

    fprintf(stderr, "==== end write_to_file\n");

    album_add_album_info(&album_info);

    fprintf(stderr, "==== end album_add_album_info\n");

    echo_success_resp();


    cJSON_Delete(root);
    return 0;
}

