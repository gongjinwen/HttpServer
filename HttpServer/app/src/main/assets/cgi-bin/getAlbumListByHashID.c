#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <linux/input.h>
#include <time.h>
#include <sys/time.h>
#include <sys/select.h>

#include "album_mgt.h"
#include "down_mgt.h"
#include "checkservice.h"
#include "cJSON.h"

void do_it(char *text)
{
    char *out = NULL;
    cJSON *json = NULL;

    fprintf(stderr, "do it now...");
    json = cJSON_Parse(text);
    if (!json)
    {
        fprintf(stderr, "Error before: [%s]\n",cJSON_GetErrorPtr());
    }
    else
    {

        cJSON *array = cJSON_GetObjectItem(json, "audios");
        if (NULL == array) 
        {
            fprintf(stderr, "==== cJSON is null\n");
            return;
        }

        int i = 0;
        char status[8];
        int count = cJSON_GetArraySize(array);
        for (i = 0; i < count; ++i)
        {
            cJSON *item = cJSON_GetArrayItem(array, i);
            if (NULL == item)
            {
                continue;
            }

            cJSON *url = cJSON_GetObjectItem(item, "url");
            cJSON *state = cJSON_GetObjectItem(item, "state");
            if (NULL == url || NULL == state)
            {
                fprintf(stderr, "==== array is null\n");
                return;
            }

            song_node_t song_node;
            char song_id[64];
            memset(song_id, 0, sizeof(song_id));
            memset(&song_node, 0, sizeof(song_node_t));

            album_get_songid_by_url(url->valuestring, &song_id);        
            if (strlen(song_id) && 0 == down_song_find(&song_id, &song_node))
            {
                memset(status, 0, sizeof(status));
                sprintf(status, "%d", song_node.song_status);
                cJSON_ReplaceItemInObject(state, "state", status);
            }                        
        }

        out = cJSON_Print(json);
        cJSON_Delete(json);
        printf("%s\n",out);
        fprintf(stderr, "%s\n", out);
        free(out);
    }

    return;
}

void do_file(char *filename)
{
    FILE *read_file;
    long len;
    char *data;

    read_file = fopen(filename, "rb");
    if (NULL == read_file)
    {
        return;
    }
    fseek(read_file, 0, SEEK_END);
    len = ftell(read_file);
    fseek(read_file, 0, SEEK_SET);

    data = (char*)malloc(len+1);
    fread(data, 1, len, read_file);
    data[len] = '\0';

    fclose(read_file);

    do_it(data);

    free(data);

    return;
}

int main(void)
{
    down_mgt_init();
    char *data = NULL;
    data = getenv("QUERY_STRING");
    if (NULL == data)
    {
        return -1;
    }

    char *decode_buf = NULL;
    decode_buf = url_decode(data);
    char *hashID = NULL;
    fprintf(stderr, "== decode_buf:%s\n", decode_buf); 
    if (NULL != decode_buf && strstr(decode_buf, "hashID="))
    {
        //hashID = get_select_str(decode_buf, '=', '\0');
    }

    hashID = decode_buf  + 7;
    fprintf(stderr, "==== hashID:%s\n", hashID);
    char file_path[1024];
    memset(file_path, 0, sizeof(file_path));

    //sprintf(file_path, "/usr/app/public/%s", "album_list.json");
    sprintf(file_path, "%s/%s_%s", ALBUM_LIST_DIR, hashID, ALBUM_LIST_SUFFIX);
    fprintf(stderr, "=== file_path:%s\n", file_path);

    do_file(file_path);

#if 0
    printf("{\n");
    printf("\"message\" : \"no album list\",\n");
    printf("\"responseCode\" : \"404\"\n");
    printf("}\n");
#endif

    return 0;
}

