#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <linux/input.h>
#include <time.h>
#include <sys/time.h>
#include <sys/select.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <dirent.h>

#include "checkservice.h"
#include "cJSON.h"

static int parse_album_file(char *file_name)
{
    FILE *read_file = NULL;
	long len = 0;
	char *data = NULL;

	if (NULL == file_name)
	{
		fprintf(stderr, "Failed to parse_album_file\n");
		return -1;
	}

    read_file = fopen(file_name, "rb");
	if (NULL == read_file)
	{
		fprintf(stderr, "Failed to open file:%s\n", file_name);
		return -1;
	}
	
    fseek(read_file, 0, SEEK_END);
    len = ftell(read_file);
    fseek(read_file, 0, SEEK_SET);
	
    data = (char*)malloc(len + 1);
	if (NULL == data)
	{
		fprintf(stderr, "Failed to malloc\n");
		return -2;
	}
	
    fread(data, 1, len, read_file);
    data[len] = '\0';
    fclose(read_file);

    cJSON *root = cJSON_Parse(data);
    if (NULL == root)
    {
    	free(data);
        return -3;
    }

    cJSON *albumName = cJSON_GetObjectItem(root, "albumName");
    //cJSON *albumID = cJSON_GetObjectItem(root, "albumID");
    cJSON *audiosNum = cJSON_GetObjectItem(root, "audiosNum");
    //cJSON *userId = cJSON_GetObjectItem(root, "userId");
    //cJSON *udId = cJSON_GetObjectItem(root, "udId");
    cJSON *hashID = cJSON_GetObjectItem(root, "hashID");
    cJSON *array = cJSON_GetObjectItem(root, "audios");

    if (NULL == albumName || NULL == audiosNum ||
            NULL == hashID || NULL == array) 
    {
        fprintf(stderr, "==== cJSON is null, albumName:%p, audiosNum:%p, hashID:%p array:%p\n",
                albumName, audiosNum, hashID, array);
		free(data);
		cJSON_Delete(root);
        return -4;
    }

    printf("{\n");
	printf("\"albumName\" : \"%s\",\n", albumName->valuestring);
	printf("\"audiosNum\" : \"%s\",\n", audiosNum->valuestring);
	printf("\"hashID\" : \"%s\"\n", hashID->valuestring);
	printf("}");


	cJSON_Delete(root);
    free(data);

	return 0;
}

static void scan_album_dir(char *root_dir)
{
    DIR *dp = NULL;
	int ret = 0;
    struct dirent *entry;
    struct stat statbuff;
	
	char album_list_file[4096];
    struct scan_dir *tmpDir = NULL; 
	struct scan_dir *first = NULL;
	struct scan_dir *last = NULL;
    int count=0;

    first = (struct scan_dir *)malloc(sizeof(struct scan_dir));
	memset(first, 0, sizeof(struct scan_dir));

	if (NULL == root_dir)
	{
		return;
	}

	if (NULL == opendir(root_dir))
	{
		fprintf(stderr, "There is now dir. dir:%s\n", root_dir);
		return;
	}

    strcpy(first->dir_name, root_dir);
    first->next = 0;
    tmpDir = last = first;

    while(NULL != first)
    {
        fprintf(stderr, "the current dir is %s\n", first->dir_name);
        chdir(first->dir_name);

        if (!(dp = opendir(first->dir_name)))
        {
            fprintf(stderr, "can't open %s\n", first->dir_name);
            first = first->next;
            continue;
        }

        char path_buff[1024];
		memset(path_buff, 0, sizeof(path_buff));
        strcpy(path_buff, first->dir_name);
        strcat(path_buff, "/");

        while((entry = readdir(dp)) != NULL)
        {
            lstat(entry->d_name, &statbuff);
            if(strcmp(".", entry->d_name) == 0 ||
                    strcmp("..", entry->d_name) == 0)
            {
                continue;
            }

            if(S_IFDIR & statbuff.st_mode)
            {
                tmpDir = (struct scan_dir *)malloc(sizeof(struct scan_dir));
				memset(tmpDir, 0, sizeof(struct scan_dir));
				
                strcpy(tmpDir->dir_name, path_buff);
                strcpy(tmpDir->dir_name + strlen(tmpDir->dir_name), entry->d_name);
                tmpDir->next = NULL;
                last->next = tmpDir;
                last = tmpDir;
            }
            else
            {
                if(is_album_file(entry->d_name))
                {
					memset(album_list_file, 0, sizeof(album_list_file));
                    memcpy(album_list_file, path_buff, strlen(path_buff));
                    memcpy(album_list_file + strlen(album_list_file), entry->d_name, strlen(entry->d_name));

                    fprintf(stderr, "scan file. uri:%s\n", album_list_file);

					if (0 == count)
					{
						printf("\"hashIDs\" : [\n");
					}
					else
					{
						printf(",\n");
					}

					ret = parse_album_file(entry->d_name);
					if (ret == 0)
					{
						count++;
					}
                    
                }
            }
            usleep(1000);
        }
        tmpDir = first;
        first = first->next;
		
        free(tmpDir);
        closedir(dp);
    }

	if (count)
	{
		printf("]\n");
	}

    fprintf(stderr, "album list file num:%d\n", count);

    return;
}


int main(void)
{
    fprintf(stderr, "===== begin==\n");

    printf("{\n");
    printf("\"message\" : \"ok\",\n");
    printf("\"responseCode\" : \"200\",\n");
    
	scan_album_dir(ALBUM_LIST_DIR);
	//scan_album_dir("/usr/app/public/");

	printf("}\n");
    return 0;
}

