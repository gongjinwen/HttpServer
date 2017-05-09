#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

#include "checkservice.h"
#include "cJSON.h"

int main(void)
{
    char *data = NULL;
    data = getenv("QUERY_STRING");
    if (NULL == data)
    {
        return -1;
    }

    char *decode_buf = NULL;
    decode_buf = url_decode(data);

    char *hashID = NULL;
    if (NULL != decode_buf && strstr(decode_buf, "hashID="))
    {
        hashID = get_select_str(decode_buf, '=', '\0');
    }

	fprintf(stderr, "delete hashid:%s\n", hashID);
    char cmd[4096];
    memset(cmd, 0, sizeof(cmd));
    sprintf(cmd, "rm -f %s/%s_%s", ALBUM_LIST_DIR, hashID, ALBUM_LIST_SUFFIX);
    system(cmd);
	fprintf(stderr, "delete cmd:%s\n", cmd);
	system("sync");

    printf("{\n");
    printf("\"message\" : \"ok\",\n");
    printf("\"responseCode\" : \"200\",\n");
    printf("}\n");

    return;
}
